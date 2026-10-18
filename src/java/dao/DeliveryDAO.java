package dao;

import dal.DBContext;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.DeliveryTask;
import utility.Constants;
import utility.ValidationException;

public class DeliveryDAO extends DBContext {

    public List<DeliveryTask> getAllTasks() throws ValidationException {
        List<DeliveryTask> list = new ArrayList<>();
        String sql = "SELECT o.OrderID, o.OrderType, o.DeliveryAddress, o.Status, u.Username AS ShipperUsername "
                + "FROM Orders o "
                + "LEFT JOIN Users u ON o.ShipperID = u.UserID "
                + "WHERE o.OrderType = 'Delivery' "
                + "ORDER BY o.OrderID DESC";

        try {
            PreparedStatement st = connection.prepareStatement(sql);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                DeliveryTask task = mapResultSetToTask(rs);
                list.add(task);
            }
        } catch (SQLException e) {
            throw new ValidationException("Lỗi truy xuất cơ sở dữ liệu (getAllTasks): " + e.getMessage());
        }
        return list;
    }

    public List<DeliveryTask> getTasksByShipper(String shipperUsername) throws ValidationException {
        List<DeliveryTask> result = new ArrayList<>();
        if (shipperUsername == null) {
            return result;
        }

        String sql = "SELECT o.OrderID, o.OrderType, o.DeliveryAddress, o.Status, u.Username AS ShipperUsername "
                + "FROM Orders o "
                + "JOIN Users u ON o.ShipperID = u.UserID "
                + "WHERE o.OrderType = 'Delivery' AND u.Username = ? "
                + "ORDER BY o.OrderID DESC";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setString(1, shipperUsername);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                DeliveryTask task = mapResultSetToTask(rs);
                result.add(task);
            }
        } catch (SQLException e) {
            throw new ValidationException("Lỗi truy xuất đơn của Shipper: " + e.getMessage());
        }
        return result;
    }

    public DeliveryTask getTaskById(int taskId) throws ValidationException {
        String sql = "SELECT o.OrderID, o.OrderType, o.DeliveryAddress, o.Status, u.Username AS ShipperUsername "
                + "FROM Orders o "
                + "LEFT JOIN Users u ON o.ShipperID = u.UserID "
                + "WHERE o.OrderID = ?";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setInt(1, taskId);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return mapResultSetToTask(rs);
            }
        } catch (SQLException e) {
            throw new ValidationException("Lỗi tìm kiếm đơn hàng (getTaskById): " + e.getMessage());
        }
        return null;
    }

    public void updateTaskStatus(int taskId, String newStatus) throws ValidationException {
        String sql = "UPDATE Orders SET Status = ? WHERE OrderID = ?";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setString(1, newStatus);
            st.setInt(2, taskId);
            st.executeUpdate();
        } catch (SQLException e) {
            throw new ValidationException("Lỗi cập nhật trạng thái đơn (updateTaskStatus): " + e.getMessage());
        }
    }

    public void updateTaskShipper(int taskId, String shipperUsername) throws ValidationException {
        String sql = "UPDATE Orders SET ShipperID = (SELECT UserID FROM Users WHERE Username = ?) WHERE OrderID = ?";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            if (shipperUsername == null || shipperUsername.trim().isEmpty()) {
                st.setNull(1, java.sql.Types.INTEGER);
            } else {
                st.setString(1, shipperUsername);
            }
            st.setInt(2, taskId);
            st.executeUpdate();
        } catch (SQLException e) {
            throw new ValidationException("Lỗi gán Shipper cho đơn (updateTaskShipper): " + e.getMessage());
        }
    }

private DeliveryTask mapResultSetToTask(ResultSet rs) throws SQLException {
        DeliveryTask task = new DeliveryTask();
        task.setTaskId(rs.getInt("OrderID")); 
        task.setOrderCode("ORD-" + rs.getInt("OrderID")); 
        task.setCustomerAddress(rs.getString("DeliveryAddress"));
        
        // Giữ nguyên bản chữ cái từ DB (Ví dụ: 'Returned', 'Delivered', 'Delivering')
        task.setStatus(rs.getString("Status")); 
        
        task.setShipperUsername(rs.getString("ShipperUsername"));
        return task;
    }
}
