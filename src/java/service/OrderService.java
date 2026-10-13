/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import dao.IngredientDAO;
import dao.OrderDAO;
import dao.ProductDAO;
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
    private ProductDAO productDAO;
    private IngredientDAO ingredientDAO;
    private SimpleDateFormat dateTimeFormat;

    public OrderService() {
        this.orderDAO = new OrderDAO();
        this.productDAO = new ProductDAO();
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

    public void updateOrderStatus(int orderId, String newStatus) throws ValidationException {
        validateStatusParam(orderId, newStatus);
        boolean updated = orderDAO.updateOrderStatus(orderId, newStatus);
        if (!updated) {
            throw new ValidationException(Constants.ERROR_ORDER_NOT_FOUND);
        }
    }

    public void cancelOrder(int orderId) throws ValidationException {
        Order order = validateAndGetOrderForModification(orderId);
        restoreOrderIngredients(order);
        boolean cancelled = orderDAO.cancelOrder(orderId);
        if (!cancelled) {
            throw new ValidationException(Constants.ERROR_ORDER_NOT_FOUND);
        }
    }

    public double exchangeDish(int orderId, int oldProductId, int newProductId, int newQuantity) throws ValidationException {
        validateExchangeParams(oldProductId, newProductId, newQuantity);
        Order order = validateAndGetOrderForModification(orderId);
        OrderDetail oldDetail = findDetailByProductId(order, oldProductId);
        if (oldDetail == null) {
            throw new ValidationException(Constants.ERROR_ORDER_NOT_FOUND);
        }

        Product newProduct = productDAO.getProductById(newProductId);
        if (newProduct == null || !newProduct.isAvailable()) {
            throw new ValidationException(Constants.ERROR_INVALID_EXCHANGE_PRODUCT);
        }

        // Hoàn tạm nguyên liệu của món cũ
        restoreItemIngredients(oldDetail.getProductId(), oldDetail.getQuantity());

        // Kiểm tra xem món mới có đủ nguyên liệu không
        if (!ingredientDAO.hasSufficientStockForProduct(newProductId, newQuantity)) {
            // Rollback lại kho của món cũ nếu món mới thiếu nguyên liệu
            deductItemIngredients(oldDetail.getProductId(), oldDetail.getQuantity());
            throw new ValidationException(Constants.ERROR_INSUFFICIENT_INGREDIENTS);
        }

        // Trừ kho nguyên liệu cho món mới
        deductItemIngredients(newProductId, newQuantity);

        // Tính tiền chênh lệch (dương: thu thêm, âm: hoàn trả khách)
        double oldSubtotal = oldDetail.getUnitPrice() * oldDetail.getQuantity();
        double newSubtotal = newProduct.getPrice() * newQuantity;
        double priceDiff = newSubtotal - oldSubtotal;

        // Cập nhật chi tiết đơn hàng
        orderDAO.replaceOrderItem(orderId, oldProductId, newProductId, newQuantity, newProduct.getPrice(), newProduct);
        return priceDiff;
    }

    public List<Map<String, Object>> getAvailableProductsForExchange() throws ValidationException {
        List<Map<String, Object>> result = new ArrayList<>();
        List<Product> products = productDAO.getAllProducts();
        if (products != null) {
            for (Product p : products) {
                if (p.isAvailable()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("productId", p.getProductId());
                    map.put("productName", p.getProductName());
                    map.put("price", p.getPrice());
                    result.add(map);
                }
            }
        }
        return result;
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
        boolean canChange = order.getCookingStartedAt() == null
                && !"Cancelled".equalsIgnoreCase(order.getStatus())
                && !"Delivered".equalsIgnoreCase(order.getStatus())
                && !"Paid".equalsIgnoreCase(order.getStatus());
        map.put("canChangeOrder", canChange);
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

    private void validateExchangeParams(int oldProductId, int newProductId, int newQuantity) throws ValidationException {
        if (oldProductId <= 0 || newProductId <= 0 || oldProductId == newProductId || newQuantity <= 0) {
            throw new ValidationException(Constants.ERROR_INVALID_EXCHANGE_PRODUCT);
        }
    }

    private OrderDetail findDetailByProductId(Order order, int productId) {
        if (order.getOrderDetails() != null) {
            for (OrderDetail d : order.getOrderDetails()) {
                if (d.getProductId() == productId) {
                    return d;
                }
            }
        }
        return null;
    }

    private void restoreOrderIngredients(Order order) {
        if (order.getOrderDetails() != null) {
            for (OrderDetail d : order.getOrderDetails()) {
                restoreItemIngredients(d.getProductId(), d.getQuantity());
            }
        }
    }

    private void restoreItemIngredients(int productId, int quantity) {
        List<ProductIngredient> recipes = ingredientDAO.getIngredientsByProductId(productId);
        for (ProductIngredient pi : recipes) {
            ingredientDAO.restoreStock(pi.getIngredientId(), pi.getQuantityUsed() * quantity);
            recheckProductAvailabilityAfterStockRestore(pi.getIngredientId());
        }
    }

    private void deductItemIngredients(int productId, int quantity) {
        List<ProductIngredient> recipes = ingredientDAO.getIngredientsByProductId(productId);
        for (ProductIngredient pi : recipes) {
            ingredientDAO.deductStock(pi.getIngredientId(), pi.getQuantityUsed() * quantity);
        }
    }

    private void recheckProductAvailabilityAfterStockRestore(int ingredientId) {
        List<Integer> productIds = ingredientDAO.getProductIdsUsingIngredient(ingredientId);
        for (Integer pid : productIds) {
            if (ingredientDAO.hasSufficientStockForProduct(pid, 1)) {
                productDAO.updateProductAvailability(pid, true);
            }
        }
    }
}
