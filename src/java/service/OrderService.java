/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import dao.DeliveryDAO;
import dao.IngredientDAO;
import dao.OrderDAO;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.DeliveryArea;
import model.DeliveryStatusHistory;
import model.Order;
import model.OrderDetail;
import model.ProductIngredient;
import utility.Constants;
import utility.ValidationException;

/**
 * Service xử lý nghiệp vụ Quản lý Đơn hàng (Order Management & UC-02)
 *
 * @author AD
 */
public class OrderService {

    private OrderDAO orderDAO;
    private IngredientDAO ingredientDAO;
    private DeliveryDAO deliveryDAO;
    private SimpleDateFormat dateTimeFormat;

    public OrderService() {
        this.orderDAO = new OrderDAO();
        this.ingredientDAO = new IngredientDAO();
        this.deliveryDAO = new DeliveryDAO();
        this.dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    // ==========================================
    // ========== 1. MAIN SERVICE (FACADE) ======
    // ==========================================
    public List<Map<String, Object>> getOrdersForView(String filterDate, String filterStatus) throws ValidationException {
        List<Order> orders = orderDAO.filterOrders(filterDate, filterStatus);
        return convertOrdersToViewList(orders);
    }

    public List<Map<String, Object>> getOrdersForTableView(String tableIdStr) throws ValidationException {
        return getOrdersForTableView(tableIdStr, null);
    }

    public List<Map<String, Object>> getOrdersForTableView(String tableIdStr, List<Integer> orderIds) throws ValidationException {
        if (tableIdStr == null || tableIdStr.trim().isEmpty() || orderIds == null || orderIds.isEmpty()) {
            return new ArrayList<>();
        }
        int tableId;
        try {
            tableId = Integer.parseInt(tableIdStr.replaceAll("\\D+", ""));
        } catch (NumberFormatException e) {
            return new ArrayList<>();
        }
        List<Order> orders = orderDAO.getOrdersByTableId(tableId);
        List<Map<String, Object>> result = new ArrayList<>();
        long now = System.currentTimeMillis();
        long tenMinutesMillis = 10 * 60 * 1000;

        for (Order o : orders) {
            // Chỉ hiển thị các đơn thuộc phiên của khách hiện tại đã đặt trên thiết bị này
            if (!orderIds.contains(o.getOrderId())) {
                continue;
            }
            // Ẩn đơn đã hủy khỏi giao diện của bàn khách
            if (Constants.STATUS_CANCELLED.equalsIgnoreCase(o.getStatus())) {
                continue;
            }
            // Tự động ẩn các đơn đã hoàn thành hoặc đã thanh toán khỏi màn hình khách sau 10 phút
            if (Constants.STATUS_COMPLETED.equalsIgnoreCase(o.getStatus()) || "Completed".equalsIgnoreCase(o.getStatus()) || Constants.STATUS_PAID.equalsIgnoreCase(o.getStatus())) {
                if (o.getCreatedAt() != null && (now - o.getCreatedAt().getTime() > tenMinutesMillis)) {
                    continue;
                }
            }
            result.add(convertOrderToMap(o));
        }
        return result;
    }

    public void updateOrderStatus(int orderId, String newStatus) throws ValidationException {
        validateStatusParam(orderId, newStatus);
        Order currentOrder = orderDAO.getOrderById(orderId);
        if (currentOrder == null) {
            throw new ValidationException(Constants.ERROR_ORDER_NOT_FOUND);
        }

        String curStatus = currentOrder.getStatus();

        // 1. Khóa State Machine: Nếu đơn là loại Delivery và đang thuộc luồng kiểm soát của Shipper
        if (Constants.ORDER_TYPE_DELIVERY.equalsIgnoreCase(currentOrder.getOrderType())) {
            if (Constants.STATUS_WAITING_DELIVERY.equalsIgnoreCase(curStatus)
                    || Constants.STATUS_PICKED_UP.equalsIgnoreCase(curStatus)
                    || Constants.STATUS_DELIVERING.equalsIgnoreCase(curStatus)) {
                throw new ValidationException(Constants.ERROR_DELIVERY_CONTROLLED_BY_SHIPPER);
            }
        }

        // 2. Khóa State Machine: Nếu đơn đang ở trạng thái kết thúc (Delivered, Returned, Cancelled, hoặc Paid)
        if (Constants.STATUS_COMPLETED.equalsIgnoreCase(curStatus)
                || "Delivered".equalsIgnoreCase(curStatus)
                || Constants.STATUS_FAILED.equalsIgnoreCase(curStatus)
                || "Returned".equalsIgnoreCase(curStatus)
                || Constants.STATUS_CANCELLED.equalsIgnoreCase(curStatus)
                || Constants.STATUS_PAID.equalsIgnoreCase(curStatus)) {
            throw new ValidationException(Constants.ERROR_ORDER_CLOSED_STATUS);
        }

        // Xử lý thu hồi đơn giao hàng về phục vụ tại quầy / tại bàn
        if (Constants.ORDER_TYPE_DELIVERY.equalsIgnoreCase(currentOrder.getOrderType())
                && (Constants.STATUS_ACCEPTED.equalsIgnoreCase(newStatus) || Constants.STATUS_PREPARED.equalsIgnoreCase(newStatus))) {
            revertDeliveryOrder(currentOrder, newStatus);
            return;
        }

        if (Constants.STATUS_CANCELLED.equalsIgnoreCase(newStatus)) {
            if (Constants.ORDER_TYPE_DELIVERY.equalsIgnoreCase(currentOrder.getOrderType()) && currentOrder.getShipperId() != null && currentOrder.getShipperId() > 0) {
                int sId = currentOrder.getShipperId();
                deliveryDAO.insertStatusHistory(new DeliveryStatusHistory(
                        0, orderId, sId, Constants.STATUS_CANCELLED, Constants.NOTE_STAFF_CANCEL_ORDER, null, new java.sql.Timestamp(System.currentTimeMillis())
                ));
                int activeCount = deliveryDAO.countActiveOrdersByShipperId(sId);
                deliveryDAO.updateShipperStatusAndCount(sId, (activeCount == 0 ? Constants.SHIPPER_STATUS_READY : Constants.SHIPPER_STATUS_DELIVERING), activeCount);
            }
            // Chuyển sang Cancelled -> hoàn trả kho nguyên liệu
            restoreOrderIngredients(currentOrder);
        }

        boolean updated = orderDAO.updateOrderStatus(orderId, newStatus);
        if (!updated) {
            throw new ValidationException(Constants.ERROR_ORDER_NOT_FOUND);
        }

        if (Constants.STATUS_CANCELLED.equalsIgnoreCase(newStatus)) {
            utility.OrderRuntimeCache.recordCancelTime(orderId);
        } else {
            utility.OrderRuntimeCache.clearCancelTime(orderId);
        }
    }

    public void cancelOrder(int orderId) throws ValidationException {
        Order order = validateAndGetOrderForModification(orderId);
        if (Constants.ORDER_TYPE_DELIVERY.equalsIgnoreCase(order.getOrderType()) && order.getShipperId() != null && order.getShipperId() > 0) {
            int sId = order.getShipperId();
            deliveryDAO.insertStatusHistory(new DeliveryStatusHistory(
                    0, orderId, sId, Constants.STATUS_CANCELLED, Constants.NOTE_STAFF_CANCEL_ORDER, null, new java.sql.Timestamp(System.currentTimeMillis())
            ));
            int activeCount = deliveryDAO.countActiveOrdersByShipperId(sId);
            deliveryDAO.updateShipperStatusAndCount(sId, (activeCount == 0 ? Constants.SHIPPER_STATUS_READY : Constants.SHIPPER_STATUS_DELIVERING), activeCount);
        }
        restoreOrderIngredients(order);
        boolean cancelled = orderDAO.cancelOrder(orderId);
        if (!cancelled) {
            throw new ValidationException(Constants.ERROR_ORDER_NOT_FOUND);
        }
        utility.OrderRuntimeCache.recordCancelTime(orderId);
    }

    private void revertDeliveryOrder(Order order, String newStatus) throws ValidationException {
        String curStatus = order.getStatus();
        if (Constants.STATUS_DELIVERING.equalsIgnoreCase(curStatus) || Constants.STATUS_COMPLETED.equalsIgnoreCase(curStatus) || Constants.STATUS_FAILED.equalsIgnoreCase(curStatus)) {
            throw new ValidationException(Constants.ERROR_CANNOT_REVERT_DELIVERY_IN_PROGRESS);
        }

        // Xác định loại đơn gốc: nếu có TableID -> Table, ngược lại -> Counter
        String targetType = (order.getTableId() != null && order.getTableId() > 0) ? "Table" : "Counter";

        // Giải phóng Shipper nếu đã gán
        Integer shipperId = order.getShipperId();
        if (shipperId != null && shipperId > 0) {
            DeliveryStatusHistory history = new DeliveryStatusHistory(
                    0, order.getOrderId(), shipperId, newStatus,
                    Constants.NOTE_REVERT_DELIVERY_ORDER, null, new java.sql.Timestamp(System.currentTimeMillis())
            );
            deliveryDAO.insertStatusHistory(history);

            int activeCount = deliveryDAO.countActiveOrdersByShipperId(shipperId);
            String shipperStatus = (activeCount == 0) ? Constants.SHIPPER_STATUS_READY : Constants.SHIPPER_STATUS_DELIVERING;
            deliveryDAO.updateShipperStatusAndCount(shipperId, shipperStatus, activeCount);
        }

        boolean updated = orderDAO.revertOrderFromDelivery(order.getOrderId(), targetType, newStatus);
        if (!updated) {
            throw new ValidationException(Constants.ERROR_DATABASE_UPDATE_ORDER);
        }
        utility.OrderRuntimeCache.clearCancelTime(order.getOrderId());
    }

    public List<Map<String, Object>> getAllDeliveryAreas() throws ValidationException {
        List<DeliveryArea> areas = deliveryDAO.getAllDeliveryAreas();
        List<Map<String, Object>> result = new ArrayList<>();
        if (areas != null) {
            for (DeliveryArea a : areas) {
                Map<String, Object> map = new HashMap<>();
                map.put("areaId", a.getAreaId());
                map.put("areaName", a.getAreaName());
                result.add(map);
            }
        }
        return result;
    }

    public void updateOrderToDelivery(int orderId, String customerName, String customerPhone, String deliveryAddress, int areaId) throws ValidationException {
        utility.Validator.checkEmpty(customerName, Constants.ERROR_EMPTY_CUSTOMER_NAME);
        utility.Validator.checkMaxLength(customerName, 100, Constants.ERROR_EMPTY_CUSTOMER_NAME);
        utility.Validator.checkEmpty(customerPhone, Constants.ERROR_EMPTY_CUSTOMER_PHONE);
        utility.Validator.checkPhoneFormat(customerPhone, Constants.ERROR_EMPTY_CUSTOMER_PHONE);
        utility.Validator.checkEmpty(deliveryAddress, Constants.ERROR_EMPTY_DELIVERY_ADDRESS);
        utility.Validator.checkMaxLength(deliveryAddress, 255, Constants.ERROR_EMPTY_DELIVERY_ADDRESS);
        if (areaId <= 0) {
            throw new ValidationException(Constants.ERROR_INVALID_DELIVERY_AREA);
        }

        Order order = orderDAO.getOrderById(orderId);
        if (order == null) {
            throw new ValidationException(Constants.ERROR_ORDER_NOT_FOUND);
        }

        String curStatus = order.getStatus();
        if (Constants.STATUS_COMPLETED.equalsIgnoreCase(curStatus) || Constants.STATUS_PAID.equalsIgnoreCase(curStatus) || Constants.STATUS_CANCELLED.equalsIgnoreCase(curStatus)) {
            throw new ValidationException(Constants.ERROR_ORDER_CANNOT_DISPATCH);
        }

        boolean updated = orderDAO.updateOrderToDelivery(orderId, customerName.trim(), customerPhone.trim(), deliveryAddress.trim(), areaId);
        if (!updated) {
            throw new ValidationException(Constants.ERROR_DATABASE_UPDATE_ORDER);
        }

        utility.OrderRuntimeCache.clearCancelTime(orderId);
    }

    public Map<String, Integer> calculateOrderCounts(List<Map<String, Object>> orders) {
        Map<String, Integer> counts = new HashMap<>();
        int all = 0;
        int counter = 0;
        int table = 0;
        int delivery = 0;

        if (orders != null) {
            for (Map<String, Object> o : orders) {
                all++;
                String type = (String) o.get("orderType");
                if ("Counter".equalsIgnoreCase(type)) {
                    counter++;
                } else if ("Table".equalsIgnoreCase(type)) {
                    table++;
                } else if (Constants.ORDER_TYPE_DELIVERY.equalsIgnoreCase(type)) {
                    delivery++;
                }
            }
        }

        counts.put("all", all);
        counts.put("counter", counter);
        counts.put("table", table);
        counts.put("delivery", delivery);
        return counts;
    }

    // ==========================================
    // ========== 2. DTO/MAP CONVERSION =========
    // ==========================================
    private List<Map<String, Object>> convertOrdersToViewList(List<Order> orders) {
        List<Map<String, Object>> result = new ArrayList<>();
        Map<Integer, String> areaMap = new HashMap<>();
        Map<Integer, String> shipperMap = new HashMap<>();
        try {
            List<DeliveryArea> areas = deliveryDAO.getAllDeliveryAreas();
            if (areas != null) {
                for (DeliveryArea a : areas) {
                    areaMap.put(a.getAreaId(), a.getAreaName());
                }
            }
            shipperMap = deliveryDAO.getShipperNameMap();
        } catch (Exception e) {
            // Keep empty maps if error occurs
        }

        if (orders != null) {
            for (Order o : orders) {
                result.add(convertOrderToMap(o, areaMap, shipperMap));
            }
        }
        return result;
    }

    private Map<String, Object> convertOrderToMap(Order order) {
        return convertOrderToMap(order, null, null);
    }

    private Map<String, Object> convertOrderToMap(Order order, Map<Integer, String> areaMap, Map<Integer, String> shipperMap) {
        Map<String, Object> map = new HashMap<>();
        map.put("orderId", order.getOrderId());
        map.put("orderType", order.getOrderType());
        map.put("tableId", order.getTableId());
        map.put("staffId", order.getStaffId());
        map.put("customerName", order.getCustomerName());
        map.put("customerPhone", order.getCustomerPhone());
        map.put("deliveryAddress", order.getDeliveryAddress() != null ? order.getDeliveryAddress() : "");
        map.put("areaId", order.getAreaId());
        String areaName = (order.getAreaId() != null && areaMap != null && areaMap.containsKey(order.getAreaId()))
                ? areaMap.get(order.getAreaId()) : "";
        map.put("areaName", areaName);

        map.put("shipperId", order.getShipperId());
        String shipperName = (order.getShipperId() != null && shipperMap != null && shipperMap.containsKey(order.getShipperId()))
                ? shipperMap.get(order.getShipperId()) : "";
        map.put("shipperName", shipperName);

        String curStatus = order.getStatus();
        String orderType = order.getOrderType();

        boolean isDeliveryUnderShipper = Constants.ORDER_TYPE_DELIVERY.equalsIgnoreCase(orderType)
                && (Constants.STATUS_WAITING_DELIVERY.equalsIgnoreCase(curStatus)
                || Constants.STATUS_PICKED_UP.equalsIgnoreCase(curStatus)
                || Constants.STATUS_DELIVERING.equalsIgnoreCase(curStatus));

        boolean isClosedStatus = Constants.STATUS_COMPLETED.equalsIgnoreCase(curStatus)
                || "Delivered".equalsIgnoreCase(curStatus)
                || Constants.STATUS_FAILED.equalsIgnoreCase(curStatus)
                || "Returned".equalsIgnoreCase(curStatus)
                || Constants.STATUS_CANCELLED.equalsIgnoreCase(curStatus)
                || Constants.STATUS_PAID.equalsIgnoreCase(curStatus);

        boolean isCancelled = Constants.STATUS_CANCELLED.equalsIgnoreCase(curStatus);
        boolean isCancelLocked = isCancelled || isClosedStatus;
        long remainingCancelSeconds = 0;
        if (isCancelled) {
            Long cancelTime = utility.OrderRuntimeCache.getCancelTime(order.getOrderId());
            if (cancelTime != null) {
                long elapsed = System.currentTimeMillis() - cancelTime;
                long remaining = 300 - (elapsed / 1000);
                if (remaining <= 0) {
                    isCancelLocked = true;
                    remainingCancelSeconds = 0;
                } else {
                    remainingCancelSeconds = remaining;
                }
            } else {
                isCancelLocked = true;
                remainingCancelSeconds = 0;
            }
        }

        boolean isStatusUpdateLocked = isDeliveryUnderShipper || isClosedStatus || isCancelLocked;

        boolean canCancel = order.getCookingStartedAt() == null
                && !isClosedStatus
                && !isDeliveryUnderShipper;
        map.put("canCancelOrder", canCancel);

        map.put("status", curStatus);
        map.put("totalAmount", order.getTotalAmount());
        map.put("createdAt", formatTimestamp(order.getCreatedAt()));
        map.put("isCancelled", isCancelled);
        map.put("isCancelLocked", isCancelLocked);
        map.put("remainingCancelSeconds", remainingCancelSeconds);
        map.put("isDeliveryUnderShipper", isDeliveryUnderShipper);
        map.put("isClosedStatus", isClosedStatus);
        map.put("isStatusUpdateLocked", isStatusUpdateLocked);

        map.put("details", convertOrderDetailsToMapList(order.getOrderDetails()));
        return map;
    }

    private List<Map<String, Object>> convertOrderDetailsToMapList(List<OrderDetail> details) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (details != null) {
            for (OrderDetail d : details) {
                Map<String, Object> dMap = new HashMap<>();
                dMap.put("productId", d.getProductId());
                dMap.put("quantity", d.getQuantity());
                dMap.put("unitPrice", d.getUnitPrice());
                if (d.getProduct() != null) {
                    dMap.put("productName", d.getProduct().getProductName());
                } else {
                    dMap.put("productName", Constants.FALLBACK_PRODUCT_PREFIX + d.getProductId());
                }
                list.add(dMap);
            }
        }
        return list;
    }

    private String formatTimestamp(java.sql.Timestamp ts) {
        if (ts == null) {
            return null;
        }
        return dateTimeFormat.format(new java.util.Date(ts.getTime()));
    }

    // ==========================================
    // ========== 3. VALIDATION =================
    // ==========================================
    private void validateStatusParam(int orderId, String newStatus) throws ValidationException {
        if (orderId <= 0) {
            throw new ValidationException(Constants.ERROR_ORDER_NOT_FOUND);
        }
        if (newStatus == null || newStatus.trim().isEmpty()) {
            throw new ValidationException(Constants.ERROR_INVALID_ORDER_STATUS);
        }
    }

    // ==========================================
    // ========== 4. UC-02 HELPERS ==============
    // ==========================================
    private Order validateAndGetOrderForModification(int orderId) throws ValidationException {
        if (orderId <= 0) {
            throw new ValidationException(Constants.ERROR_ORDER_NOT_FOUND);
        }
        Order order = orderDAO.getOrderById(orderId);
        if (order == null) {
            throw new ValidationException(Constants.ERROR_ORDER_NOT_FOUND);
        }
        if (order.getCookingStartedAt() != null) {
            throw new ValidationException(Constants.ERROR_ORDER_ALREADY_COOKING);
        }
        String status = order.getStatus();
        if (Constants.STATUS_CANCELLED.equalsIgnoreCase(status) || Constants.STATUS_COMPLETED.equalsIgnoreCase(status) || Constants.STATUS_PAID.equalsIgnoreCase(status)) {
            throw new ValidationException(Constants.ERROR_ORDER_CANNOT_BE_CANCELLED);
        }
        return order;
    }

    private void restoreOrderIngredients(Order order) throws ValidationException {
        if (order.getOrderDetails() != null) {
            for (OrderDetail d : order.getOrderDetails()) {
                restoreItemIngredients(d.getProductId(), d.getQuantity());
            }
        }
    }

    private void restoreItemIngredients(int productId, int quantity) throws ValidationException {
        List<ProductIngredient> recipes = ingredientDAO.getIngredientsByProductId(productId);
        for (ProductIngredient pi : recipes) {
            ingredientDAO.restoreStock(pi.getIngredientId(), pi.getQuantityUsed() * quantity);
        }
    }

    private void deductItemIngredients(int productId, int quantity) throws ValidationException {
        List<ProductIngredient> recipes = ingredientDAO.getIngredientsByProductId(productId);
        for (ProductIngredient pi : recipes) {
            ingredientDAO.deductStock(pi.getIngredientId(), pi.getQuantityUsed() * quantity);
        }
    }

    private void validateCancelModificationWindow(Order order) throws ValidationException {
        Long cancelTime = utility.OrderRuntimeCache.getCancelTime(order.getOrderId());
        if (cancelTime == null) {
            throw new ValidationException(Constants.ERROR_CANCELLED_ORDER_LOCKED);
        }
        long elapsed = System.currentTimeMillis() - cancelTime;
        if (elapsed > 5 * 60 * 1000) {
            throw new ValidationException(Constants.ERROR_CANCELLED_ORDER_LOCKED);
        }
    }

    private void validateOrderIngredientsForRestoration(Order order) throws ValidationException {
        if (order.getOrderDetails() != null) {
            for (OrderDetail d : order.getOrderDetails()) {
                if (!ingredientDAO.hasSufficientStockForProduct(d.getProductId(), d.getQuantity())) {
                    throw new ValidationException(Constants.ERROR_INSUFFICIENT_INGREDIENTS);
                }
            }
        }
    }

    private void deductOrderIngredients(Order order) throws ValidationException {
        if (order.getOrderDetails() != null) {
            for (OrderDetail d : order.getOrderDetails()) {
                deductItemIngredients(d.getProductId(), d.getQuantity());
            }
        }
    }
}
