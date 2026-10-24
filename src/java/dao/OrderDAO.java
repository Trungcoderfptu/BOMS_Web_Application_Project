package dao;

import dal.DBContext;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import model.Order;
import model.OrderDetail;
import model.Product;
import utility.Constants;
import utility.ValidationException;

/**
 * Data Access Object quản lý đơn hàng kết nối MS SQL Server (Orders & OrderDetails)
 * @author AD
 */
public class OrderDAO extends DBContext {

    public int insertOrder(Order order) throws ValidationException {
        if (order == null) {
            throw new ValidationException(Constants.ERROR_DATABASE_INSERT_ORDER);
        }

        String sqlOrder = "INSERT INTO Orders (OrderType, StaffID, TableID, AreaID, ShipperID, CustomerName, CustomerPhone, DeliveryAddress, Status, TotalAmount, CreatedAt, CookingStartedAt) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String sqlDetail = "INSERT INTO OrderDetails (OrderID, ProductID, Quantity, UnitPrice) VALUES (?, ?, ?, ?)";

        boolean originalAutoCommit = true;
        try {
            originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            int newOrderId = 0;
            try (PreparedStatement stOrder = connection.prepareStatement(sqlOrder, Statement.RETURN_GENERATED_KEYS)) {
                stOrder.setString(1, order.getOrderType());
                setNullableInt(stOrder, 2, order.getStaffId());
                setNullableInt(stOrder, 3, order.getTableId());
                setNullableInt(stOrder, 4, order.getAreaId());
                setNullableInt(stOrder, 5, order.getShipperId());
                stOrder.setString(6, order.getCustomerName());
                stOrder.setString(7, order.getCustomerPhone());
                stOrder.setString(8, order.getDeliveryAddress());
                stOrder.setString(9, order.getStatus() != null ? order.getStatus() : Constants.STATUS_ACCEPTED);
                stOrder.setDouble(10, order.getTotalAmount());
                stOrder.setTimestamp(11, order.getCreatedAt() != null ? order.getCreatedAt() : new Timestamp(System.currentTimeMillis()));
                stOrder.setTimestamp(12, order.getCookingStartedAt());
                stOrder.executeUpdate();

                try (ResultSet rs = stOrder.getGeneratedKeys()) {
                    if (rs.next()) {
                        newOrderId = rs.getInt(1);
                        order.setOrderId(newOrderId);
                    }
                }
            }

            if (newOrderId <= 0) {
                throw new SQLException("Failed to obtain generated OrderID");
            }

            if (order.getOrderDetails() != null && !order.getOrderDetails().isEmpty()) {
                try (PreparedStatement stDetail = connection.prepareStatement(sqlDetail)) {
                    for (OrderDetail detail : order.getOrderDetails()) {
                        stDetail.setInt(1, newOrderId);
                        stDetail.setInt(2, detail.getProductId());
                        stDetail.setInt(3, detail.getQuantity());
                        stDetail.setDouble(4, detail.getUnitPrice());
                        stDetail.addBatch();
                    }
                    stDetail.executeBatch();
                }
            }

            connection.commit();
            return newOrderId;
        } catch (Exception e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                java.util.logging.Logger.getLogger(OrderDAO.class.getName())
                        .log(java.util.logging.Level.WARNING, "Failed to rollback order transaction", rollbackEx);
            }
            throw new ValidationException(Constants.ERROR_DATABASE_INSERT_ORDER);
        } finally {
            try {
                connection.setAutoCommit(originalAutoCommit);
            } catch (SQLException ex) {
                java.util.logging.Logger.getLogger(OrderDAO.class.getName())
                        .log(java.util.logging.Level.WARNING, "Failed to restore auto-commit", ex);
            }
        }
    }

    public Order getOrderById(int orderId) throws ValidationException {
        String sql = "SELECT OrderID, OrderType, StaffID, TableID, AreaID, ShipperID, CustomerName, CustomerPhone, "
                + "DeliveryAddress, Status, TotalAmount, CreatedAt, CookingStartedAt FROM Orders WHERE OrderID = ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, orderId);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    Order order = mapResultSetToOrder(rs);
                    order.setOrderDetails(loadOrderDetails(orderId));
                    return order;
                }
            }
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_GET_ORDER);
        }
        return null;
    }

    public Order getLatestOrderByTableId(int tableId) throws ValidationException {
        String sql = "SELECT TOP 1 OrderID, OrderType, StaffID, TableID, AreaID, ShipperID, CustomerName, CustomerPhone, "
                + "DeliveryAddress, Status, TotalAmount, CreatedAt, CookingStartedAt FROM Orders "
                + "WHERE TableID = ? AND Status <> '" + Constants.STATUS_CANCELLED + "' ORDER BY CreatedAt DESC";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, tableId);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    Order order = mapResultSetToOrder(rs);
                    order.setOrderDetails(loadOrderDetails(order.getOrderId()));
                    return order;
                }
            }
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_GET_ORDER);
        }
        return null;
    }

    public List<Order> getOrdersByTableId(int tableId) throws ValidationException {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT OrderID, OrderType, StaffID, TableID, AreaID, ShipperID, CustomerName, CustomerPhone, "
                + "DeliveryAddress, Status, TotalAmount, CreatedAt, CookingStartedAt FROM Orders "
                + "WHERE TableID = ? ORDER BY CreatedAt DESC";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, tableId);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    Order order = mapResultSetToOrder(rs);
                    order.setOrderDetails(loadOrderDetails(order.getOrderId()));
                    list.add(order);
                }
            }
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_GET_ORDER);
        }
        return list;
    }

    public List<Order> getAllOrders() throws ValidationException {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT OrderID, OrderType, StaffID, TableID, AreaID, ShipperID, CustomerName, CustomerPhone, "
                + "DeliveryAddress, Status, TotalAmount, CreatedAt, CookingStartedAt FROM Orders ORDER BY CreatedAt DESC";
        try (PreparedStatement st = connection.prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                Order order = mapResultSetToOrder(rs);
                order.setOrderDetails(loadOrderDetails(order.getOrderId()));
                list.add(order);
            }
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_GET_ORDER);
        }
        return list;
    }

    public List<Order> filterOrders(String dateStr, String status) throws ValidationException {
        List<Order> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT OrderID, OrderType, StaffID, TableID, AreaID, ShipperID, CustomerName, CustomerPhone, "
                + "DeliveryAddress, Status, TotalAmount, CreatedAt, CookingStartedAt FROM Orders WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (dateStr != null && !dateStr.trim().isEmpty()) {
            sql.append("AND CAST(CreatedAt AS DATE) = ? ");
            params.add(dateStr.trim());
        }

        if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status.trim())) {
            sql.append("AND Status = ? ");
            params.add(status.trim());
        }

        sql.append("ORDER BY CreatedAt DESC");

        try (PreparedStatement st = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                st.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    Order order = mapResultSetToOrder(rs);
                    order.setOrderDetails(loadOrderDetails(order.getOrderId()));
                    list.add(order);
                }
            }
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_GET_ORDER);
        }
        return list;
    }

    public boolean updateOrderStatus(int orderId, String newStatus) throws ValidationException {
        String sql;
        boolean isCooking = Constants.STATUS_PREPARED.equalsIgnoreCase(newStatus) || Constants.STATUS_DELIVERING.equalsIgnoreCase(newStatus);
        if (isCooking) {
            sql = "UPDATE Orders SET Status = ?, CookingStartedAt = COALESCE(CookingStartedAt, GETDATE()) WHERE OrderID = ?";
        } else {
            sql = "UPDATE Orders SET Status = ? WHERE OrderID = ?";
        }

        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setString(1, newStatus);
            st.setInt(2, orderId);
            return st.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_UPDATE_ORDER);
        }
    }

    public boolean cancelOrder(int orderId) throws ValidationException {
        String sql = "UPDATE Orders SET Status = '" + Constants.STATUS_CANCELLED + "' WHERE OrderID = ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, orderId);
            return st.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_UPDATE_ORDER);
        }
    }

    public boolean revertOrderFromDelivery(int orderId, String newOrderType, String newStatus) throws ValidationException {
        String sql;
        boolean isCooking = Constants.STATUS_PREPARED.equalsIgnoreCase(newStatus);
        if (isCooking) {
            sql = "UPDATE Orders SET OrderType = ?, Status = ?, ShipperID = NULL, CookingStartedAt = COALESCE(CookingStartedAt, GETDATE()) WHERE OrderID = ?";
        } else {
            sql = "UPDATE Orders SET OrderType = ?, Status = ?, ShipperID = NULL WHERE OrderID = ?";
        }
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setString(1, newOrderType);
            st.setString(2, newStatus);
            st.setInt(3, orderId);
            return st.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_UPDATE_ORDER);
        }
    }

    public boolean updateOrderToDelivery(int orderId, String customerName, String customerPhone, String deliveryAddress, int areaId) throws ValidationException {
        String sql = "UPDATE Orders SET OrderType = '" + Constants.ORDER_TYPE_DELIVERY + "', CustomerName = ?, CustomerPhone = ?, DeliveryAddress = ?, AreaID = ?, Status = '" + Constants.STATUS_WAITING_DISPATCH + "', ShipperID = NULL WHERE OrderID = ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setString(1, customerName);
            st.setString(2, customerPhone);
            st.setString(3, deliveryAddress);
            st.setInt(4, areaId);
            st.setInt(5, orderId);
            return st.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_UPDATE_ORDER);
        }
    }

    private List<OrderDetail> loadOrderDetails(int orderId) throws SQLException {
        List<OrderDetail> details = new ArrayList<>();
        String sql = "SELECT od.OrderID, od.ProductID, od.Quantity, od.UnitPrice, "
                + "p.CategoryID, p.ProductName, p.Price, p.ImageUrl, p.IsAvailable "
                + "FROM OrderDetails od LEFT JOIN Products p ON od.ProductID = p.ProductID "
                + "WHERE od.OrderID = ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, orderId);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    OrderDetail detail = new OrderDetail(
                            rs.getInt("OrderID"),
                            rs.getInt("ProductID"),
                            rs.getInt("Quantity"),
                            rs.getDouble("UnitPrice")
                    );

                    Product product = new Product(
                            rs.getInt("ProductID"),
                            rs.getInt("CategoryID"),
                            rs.getString("ProductName"),
                            rs.getDouble("Price"),
                            rs.getString("ImageUrl"),
                            rs.getBoolean("IsAvailable")
                    );
                    detail.setProduct(product);
                    details.add(detail);
                }
            }
        }
        return details;
    }

    private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setOrderId(rs.getInt("OrderID"));
        order.setOrderType(rs.getString("OrderType"));
        order.setStaffId(getNullableInt(rs, "StaffID"));
        order.setTableId(getNullableInt(rs, "TableID"));
        order.setAreaId(getNullableInt(rs, "AreaID"));
        order.setShipperId(getNullableInt(rs, "ShipperID"));
        order.setCustomerName(rs.getString("CustomerName"));
        order.setCustomerPhone(rs.getString("CustomerPhone"));
        order.setDeliveryAddress(rs.getString("DeliveryAddress"));
        order.setStatus(rs.getString("Status"));
        order.setTotalAmount(rs.getDouble("TotalAmount"));
        order.setCreatedAt(rs.getTimestamp("CreatedAt"));
        order.setCookingStartedAt(rs.getTimestamp("CookingStartedAt"));
        return order;
    }

    private void setNullableInt(PreparedStatement st, int index, Integer value) throws SQLException {
        if (value == null) {
            st.setNull(index, java.sql.Types.INTEGER);
        } else {
            st.setInt(index, value);
        }
    }

    private Integer getNullableInt(ResultSet rs, String colName) throws SQLException {
        int val = rs.getInt(colName);
        return rs.wasNull() ? null : val;
    }
}
