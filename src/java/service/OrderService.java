/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import dao.IngredientDAO;
import dao.OrderDAO;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.Order;
import model.OrderDetail;
import model.Product;
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
    private SimpleDateFormat dateTimeFormat;

    public OrderService() {
        this.orderDAO = new OrderDAO();
        this.ingredientDAO = new IngredientDAO();
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
            if ("Cancelled".equalsIgnoreCase(o.getStatus())) {
                continue;
            }
            // Tự động ẩn các đơn đã hoàn thành hoặc đã thanh toán khỏi màn hình khách sau 10 phút
            if ("Delivered".equalsIgnoreCase(o.getStatus()) || "Completed".equalsIgnoreCase(o.getStatus()) || "Paid".equalsIgnoreCase(o.getStatus())) {
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

        // Kiểm tra điều kiện khóa 5 phút nếu đơn đang ở trạng thái Cancelled
        if ("Cancelled".equalsIgnoreCase(currentOrder.getStatus())) {
            validateCancelModificationWindow(currentOrder);
            if (!"Cancelled".equalsIgnoreCase(newStatus)) {
                // Hoàn tác hủy -> kiểm tra tồn kho và trừ lại nguyên liệu
                validateOrderIngredientsForRestoration(currentOrder);
                deductOrderIngredients(currentOrder);
            }
        } else if ("Cancelled".equalsIgnoreCase(newStatus)) {
            // Chuyển sang Cancelled -> hoàn trả kho nguyên liệu
            restoreOrderIngredients(currentOrder);
        }

        boolean updated = orderDAO.updateOrderStatus(orderId, newStatus);
        if (!updated) {
            throw new ValidationException(Constants.ERROR_ORDER_NOT_FOUND);
        }

        if ("Cancelled".equalsIgnoreCase(newStatus)) {
            utility.OrderRuntimeCache.recordCancelTime(orderId);
        } else {
            utility.OrderRuntimeCache.clearCancelTime(orderId);
        }
    }

    public void cancelOrder(int orderId) throws ValidationException {
        Order order = validateAndGetOrderForModification(orderId);
        restoreOrderIngredients(order);
        boolean cancelled = orderDAO.cancelOrder(orderId);
        if (!cancelled) {
            throw new ValidationException(Constants.ERROR_ORDER_NOT_FOUND);
        }
        utility.OrderRuntimeCache.recordCancelTime(orderId);
    }

    // ==========================================
    // ========== 2. DTO/MAP CONVERSION =========
    // ==========================================
    private List<Map<String, Object>> convertOrdersToViewList(List<Order> orders) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (orders != null) {
            for (Order o : orders) {
                result.add(convertOrderToMap(o));
            }
        }
        return result;
    }

    private Map<String, Object> convertOrderToMap(Order order) {
        Map<String, Object> map = new HashMap<>();
        map.put("orderId", order.getOrderId());
        map.put("orderType", order.getOrderType());
        map.put("tableId", order.getTableId());
        map.put("staffId", order.getStaffId());
        map.put("customerName", order.getCustomerName());
        map.put("customerPhone", order.getCustomerPhone());
        map.put("status", order.getStatus());
        map.put("totalAmount", order.getTotalAmount());
        map.put("createdAt", formatTimestamp(order.getCreatedAt()));
        boolean canCancel = order.getCookingStartedAt() == null
                && !"Cancelled".equalsIgnoreCase(order.getStatus())
                && !"Delivered".equalsIgnoreCase(order.getStatus())
                && !"Paid".equalsIgnoreCase(order.getStatus());
        map.put("canCancelOrder", canCancel);

        boolean isCancelled = "Cancelled".equalsIgnoreCase(order.getStatus());
        boolean isCancelLocked = false;
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
        map.put("isCancelled", isCancelled);
        map.put("isCancelLocked", isCancelLocked);
        map.put("remainingCancelSeconds", remainingCancelSeconds);

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
                    dMap.put("productName", "Món #" + d.getProductId());
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
        if ("Cancelled".equalsIgnoreCase(status) || "Delivered".equalsIgnoreCase(status) || "Paid".equalsIgnoreCase(status)) {
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
