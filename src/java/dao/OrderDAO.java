/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import java.util.ArrayList;
import java.util.List;
import model.Order;
import model.OrderDetail;
import utility.Constants;
import utility.ValidationException;

/**
 * Data Access Object cho Don hang (Order)
 * Hiện tại lưu trữ tạm trên RAM (In-Memory Fake Database)
 * Sẵn sàng thay thế bằng các câu truy vấn JDBC trong tương lai
 *
 * @author AD
 */
public class OrderDAO {

    // Kho lưu trữ tạm trên RAM mô phỏng Database
    private static List<Order> mockOrderList = new ArrayList<>();
    private static int orderIdCounter = 1;

    public int insertOrder(Order order) throws ValidationException {
        try {
            if (order == null) {
                throw new ValidationException(Constants.ERROR_DATABASE_INSERT_ORDER);
            }
            int newId = orderIdCounter++;
            order.setOrderId(newId);

            if (order.getOrderDetails() != null) {
                List<OrderDetail> savedDetails = new ArrayList<>();
                for (OrderDetail detail : order.getOrderDetails()) {
                    OrderDetail newDetail = new OrderDetail(newId, detail.getProductId(), detail.getQuantity(), detail.getUnitPrice());
                    newDetail.setProduct(detail.getProduct());
                    savedDetails.add(newDetail);
                }
                order.setOrderDetails(savedDetails);
            }

            mockOrderList.add(order);
            return newId;
        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new ValidationException(Constants.ERROR_DATABASE_INSERT_ORDER);
        }
    }

    public Order getOrderById(int orderId) throws ValidationException {
        try {
            for (Order o : mockOrderList) {
                if (o.getOrderId() == orderId) {
                    return o;
                }
            }
            return null;
        } catch (Exception e) {
            throw new ValidationException(Constants.ERROR_DATABASE_INSERT_ORDER);
        }
    }

    public Order getLatestOrderByTableId(int tableId) throws ValidationException {
        try {
            for (int i = mockOrderList.size() - 1; i >= 0; i--) {
                Order o = mockOrderList.get(i);
                if (o.getTableId() != null && o.getTableId() == tableId && !"Cancelled".equalsIgnoreCase(o.getStatus())) {
                    return o;
                }
            }
            return null;
        } catch (Exception e) {
            throw new ValidationException(Constants.ERROR_DATABASE_INSERT_ORDER);
        }
    }

    public List<Order> getAllOrders() throws ValidationException {
        try {
            return new ArrayList<>(mockOrderList);
        } catch (Exception e) {
            throw new ValidationException(Constants.ERROR_DATABASE_INSERT_ORDER);
        }
    }

    public boolean updateOrderStatus(int orderId, String newStatus) throws ValidationException {
        try {
            for (Order o : mockOrderList) {
                if (o.getOrderId() == orderId) {
                    o.setStatus(newStatus);
                    if (("Prepared".equalsIgnoreCase(newStatus) || "Delivering".equalsIgnoreCase(newStatus)) && o.getCookingStartedAt() == null) {
                        o.setCookingStartedAt(new java.sql.Timestamp(System.currentTimeMillis()));
                    }
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            throw new ValidationException(Constants.ERROR_DATABASE_INSERT_ORDER);
        }
    }

    public List<Order> filterOrders(String dateStr, String status) throws ValidationException {
        try {
            List<Order> result = new ArrayList<>();
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
            for (Order o : mockOrderList) {
                boolean matchesDate = true;
                if (dateStr != null && !dateStr.trim().isEmpty() && o.getCreatedAt() != null) {
                    String orderDate = sdf.format(new java.util.Date(o.getCreatedAt().getTime()));
                    matchesDate = dateStr.trim().equals(orderDate);
                }

                boolean matchesStatus = true;
                if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status.trim())) {
                    matchesStatus = status.trim().equalsIgnoreCase(o.getStatus());
                }

                if (matchesDate && matchesStatus) {
                    result.add(o);
                }
            }
            result.sort((o1, o2) -> {
                if (o1.getCreatedAt() == null || o2.getCreatedAt() == null) {
                    return 0;
                }
                return o2.getCreatedAt().compareTo(o1.getCreatedAt());
            });
            return result;
        } catch (Exception e) {
            throw new ValidationException(Constants.ERROR_DATABASE_INSERT_ORDER);
        }
    }

    public synchronized boolean cancelOrder(int orderId) throws ValidationException {
        try {
            Order o = getOrderById(orderId);
            if (o != null) {
                o.setStatus("Cancelled");
                return true;
            }
            return false;
        } catch (Exception e) {
            throw new ValidationException(Constants.ERROR_DATABASE_INSERT_ORDER);
        }
    }

    public synchronized boolean replaceOrderItem(int orderId, int oldProductId, int newProductId, int newQuantity, double newUnitPrice, model.Product newProduct) throws ValidationException {
        try {
            Order o = getOrderById(orderId);
            if (o == null || o.getOrderDetails() == null) {
                return false;
            }
            OrderDetail targetDetail = null;
            for (OrderDetail d : o.getOrderDetails()) {
                if (d.getProductId() == oldProductId) {
                    targetDetail = d;
                    break;
                }
            }
            if (targetDetail == null) {
                return false;
            }

            targetDetail.setProductId(newProductId);
            targetDetail.setQuantity(newQuantity);
            targetDetail.setUnitPrice(newUnitPrice);
            targetDetail.setProduct(newProduct);

            double total = 0;
            for (OrderDetail d : o.getOrderDetails()) {
                total += (d.getUnitPrice() * d.getQuantity());
            }
            o.setTotalAmount(total);
            return true;
        } catch (Exception e) {
            throw new ValidationException(Constants.ERROR_DATABASE_INSERT_ORDER);
        }
    }
}
