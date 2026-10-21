package dao;

import dal.DBContext;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.DeliveryArea;
import model.DeliveryStatusHistory;
import model.ShipperProfile;
import utility.Constants;
import utility.ValidationException;

public class DeliveryDAO extends DBContext {

    public List<Map<String, Object>> getAllTasks() throws ValidationException {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT o.OrderID, o.OrderType, o.CustomerName, o.CustomerPhone, o.DeliveryAddress, "
                + "o.Status, o.TotalAmount, o.AreaID, da.AreaName, u.Username AS ShipperUsername "
                + "FROM Orders o "
                + "LEFT JOIN DeliveryAreas da ON o.AreaID = da.AreaID "
                + "LEFT JOIN Users u ON o.ShipperID = u.UserID "
                + "WHERE o.OrderType = 'Delivery' "
                + "ORDER BY o.OrderID DESC";

        try (PreparedStatement st = connection.prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> task = mapResultSetToTask(rs);
                list.add(task);
            }
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_GET_ORDER);
        }
        return list;
    }

    public List<Map<String, Object>> getTasksByShipper(String shipperUsername) throws ValidationException {
        List<Map<String, Object>> result = new ArrayList<>();
        if (shipperUsername == null) {
            return result;
        }

        String sql = "SELECT o.OrderID, o.OrderType, o.CustomerName, o.CustomerPhone, o.DeliveryAddress, "
                + "o.Status, o.TotalAmount, o.AreaID, da.AreaName, u.Username AS ShipperUsername "
                + "FROM Orders o "
                + "LEFT JOIN DeliveryAreas da ON o.AreaID = da.AreaID "
                + "JOIN Users u ON o.ShipperID = u.UserID "
                + "WHERE o.OrderType = 'Delivery' AND u.Username = ? "
                + "ORDER BY o.OrderID DESC";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setString(1, shipperUsername);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> task = mapResultSetToTask(rs);
                    result.add(task);
                }
            }
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_GET_ORDER);
        }
        return result;
    }

    public List<Map<String, Object>> getAvailableTasksForShipper(String shipperUsername) throws ValidationException {
        List<Map<String, Object>> list = new ArrayList<>();
        if (shipperUsername == null) {
            return list;
        }

        String sql = "SELECT o.OrderID, o.OrderType, o.CustomerName, o.CustomerPhone, o.DeliveryAddress, "
                + "o.Status, o.TotalAmount, o.AreaID, da.AreaName, u.Username AS ShipperUsername "
                + "FROM Orders o "
                + "LEFT JOIN DeliveryAreas da ON o.AreaID = da.AreaID "
                + "LEFT JOIN Users u ON o.ShipperID = u.UserID "
                + "WHERE o.OrderType = 'Delivery' "
                + "  AND o.Status = 'Waiting for dispatch' "
                + "  AND o.ShipperID IS NULL "
                + "  AND ( "
                + "      o.AreaID IN ( "
                + "          SELECT sa.AreaID FROM ShipperAreas sa "
                + "          JOIN Users u2 ON sa.ShipperID = u2.UserID "
                + "          WHERE u2.Username = ? "
                + "      ) "
                + "      OR NOT EXISTS ( "
                + "          SELECT 1 FROM ShipperAreas sa2 "
                + "          JOIN Users u3 ON sa2.ShipperID = u3.UserID "
                + "          WHERE u3.Username = ? "
                + "      ) "
                + "  ) "
                + "ORDER BY o.OrderID DESC";

        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setString(1, shipperUsername);
            st.setString(2, shipperUsername);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> task = mapResultSetToTask(rs);
                    list.add(task);
                }
            }
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_GET_ORDER);
        }
        return list;
    }

    public Map<String, Object> getTaskById(int taskId) throws ValidationException {
        String sql = "SELECT o.OrderID, o.OrderType, o.CustomerName, o.CustomerPhone, o.DeliveryAddress, "
                + "o.Status, o.TotalAmount, o.AreaID, da.AreaName, u.Username AS ShipperUsername "
                + "FROM Orders o "
                + "LEFT JOIN DeliveryAreas da ON o.AreaID = da.AreaID "
                + "LEFT JOIN Users u ON o.ShipperID = u.UserID "
                + "WHERE o.OrderID = ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, taskId);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTask(rs);
                }
            }
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_GET_ORDER);
        }
        return null;
    }

    public void updateTaskStatus(int taskId, String newStatus) throws ValidationException {
        String sql = "UPDATE Orders SET Status = ? WHERE OrderID = ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setString(1, newStatus);
            st.setInt(2, taskId);
            st.executeUpdate();
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_UPDATE_ORDER);
        }
    }

    public void updateTaskShipper(int taskId, String shipperUsername) throws ValidationException {
        String sql;
        if (shipperUsername == null || shipperUsername.trim().isEmpty()) {
            sql = "UPDATE Orders SET ShipperID = NULL WHERE OrderID = ?";
        } else {
            sql = "UPDATE Orders SET ShipperID = (SELECT UserID FROM Users WHERE Username = ?) WHERE OrderID = ?";
        }

        try (PreparedStatement st = connection.prepareStatement(sql)) {
            if (shipperUsername == null || shipperUsername.trim().isEmpty()) {
                st.setInt(1, taskId);
            } else {
                st.setString(1, shipperUsername);
                st.setInt(2, taskId);
            }
            st.executeUpdate();
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_UPDATE_ORDER);
        }
    }

    public void claimTask(int taskId, String shipperUsername) throws ValidationException {
        String sql = "UPDATE Orders SET ShipperID = (SELECT UserID FROM Users WHERE Username = ?), "
                + "Status = ? WHERE OrderID = ? AND ShipperID IS NULL AND Status = ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setString(1, shipperUsername);
            st.setString(2, Constants.STATUS_DELIVERING);
            st.setInt(3, taskId);
            st.setString(4, Constants.STATUS_WAITING_DISPATCH);
            int rows = st.executeUpdate();
            if (rows == 0) {
                throw new ValidationException(Constants.ERROR_TASK_ALREADY_ASSIGNED);
            }
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_UPDATE_ORDER);
        }
    }

    private Map<String, Object> mapResultSetToTask(ResultSet rs) throws SQLException {
        Map<String, Object> task = new HashMap<>();
        task.put("taskId", rs.getInt("OrderID"));
        task.put("orderCode", "ORD-" + rs.getInt("OrderID"));
        task.put("customerAddress", rs.getString("DeliveryAddress"));
        task.put("status", rs.getString("Status"));
        task.put("shipperUsername", rs.getString("ShipperUsername"));
        task.put("customerName", rs.getString("CustomerName"));
        task.put("customerPhone", rs.getString("CustomerPhone"));
        task.put("areaName", rs.getString("AreaName"));
        task.put("totalAmount", rs.getDouble("TotalAmount"));
        return task;
    }

    public void insertStatusHistory(DeliveryStatusHistory history) throws ValidationException {
        String sql = "INSERT INTO DeliveryStatusHistory (OrderID, ShipperID, Status, FailReason, ProofImage, UpdatedAt) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, history.getOrderId());
            st.setInt(2, history.getShipperId());
            st.setString(3, history.getStatus());
            st.setString(4, history.getFailReason());
            st.setString(5, history.getProofImage());
            st.setTimestamp(6, history.getUpdatedAt() != null ? history.getUpdatedAt() : new Timestamp(System.currentTimeMillis()));
            st.executeUpdate();
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_INSERT_HISTORY);
        }
    }

    public ShipperProfile getShipperProfileByUserId(int userId) throws ValidationException {
        String sql = "SELECT UserID, VehiclePlate, ShipperStatus, ActiveOrders, MaxOrders FROM ShipperProfiles WHERE UserID = ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, userId);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return new ShipperProfile(
                            rs.getInt("UserID"),
                            rs.getString("VehiclePlate"),
                            rs.getString("ShipperStatus"),
                            rs.getInt("ActiveOrders"),
                            rs.getInt("MaxOrders")
                    );
                }
            }
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_DELIVERY);
        }
        return null;
    }

    public ShipperProfile getShipperProfileByUsername(String username) throws ValidationException {
        String sql = "SELECT sp.UserID, sp.VehiclePlate, sp.ShipperStatus, sp.ActiveOrders, sp.MaxOrders "
                + "FROM ShipperProfiles sp JOIN Users u ON sp.UserID = u.UserID WHERE u.Username = ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setString(1, username);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return new ShipperProfile(
                            rs.getInt("UserID"),
                            rs.getString("VehiclePlate"),
                            rs.getString("ShipperStatus"),
                            rs.getInt("ActiveOrders"),
                            rs.getInt("MaxOrders")
                    );
                }
            }
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_DELIVERY);
        }
        return null;
    }

    public int getUserIdByUsername(String username) throws ValidationException {
        String sql = "SELECT UserID FROM Users WHERE Username = ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setString(1, username);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("UserID");
                }
            }
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_DELIVERY);
        }
        return 0;
    }

    public void updateShipperStatusAndCount(int userId, String status, int activeOrders) throws ValidationException {
        String sql = "UPDATE ShipperProfiles SET ShipperStatus = ?, ActiveOrders = ? WHERE UserID = ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setString(1, status);
            st.setInt(2, activeOrders);
            st.setInt(3, userId);
            st.executeUpdate();
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_DELIVERY);
        }
    }

    public List<DeliveryArea> getAllDeliveryAreas() throws ValidationException {
        List<DeliveryArea> list = new ArrayList<>();
        String sql = "SELECT AreaID, AreaName FROM DeliveryAreas ORDER BY AreaID";
        try (PreparedStatement st = connection.prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                list.add(new DeliveryArea(rs.getInt("AreaID"), rs.getString("AreaName")));
            }
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_DELIVERY);
        }
        return list;
    }

    public Map<Integer, String> getShipperNameMap() throws ValidationException {
        Map<Integer, String> map = new HashMap<>();
        String sql = "SELECT UserID, FullName, Username FROM Users WHERE Role = 'Shipper'";
        try (PreparedStatement st = connection.prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("UserID");
                String name = rs.getString("FullName");
                if (name == null || name.trim().isEmpty()) {
                    name = rs.getString("Username");
                }
                map.put(id, name);
            }
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_DELIVERY);
        }
        return map;
    }

    public int countActiveOrdersByShipperId(int shipperId) throws ValidationException {
        String sql = "SELECT COUNT(*) AS Total FROM Orders WHERE ShipperID = ? AND Status = 'Delivering'";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, shipperId);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("Total");
                }
            }
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_DELIVERY);
        }
        return 0;
    }
}
