package dao;

import dal.DBContext;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.Notification;
import utility.Constants;
import utility.ValidationException;

/**
 * Data Access Object cho bảng Notifications (dùng chung toàn hệ thống - Task 11)
 * Tuân thủ Rule_BOMS: bọc try-with-resources, không System.out.println.
 */
public class NotificationDAO extends DBContext {

    public void insertNotification(int recipientId, String type, Integer refId, String message) throws ValidationException {
        String sql = "INSERT INTO Notifications (RecipientID, Type, RefID, Message) VALUES (?, ?, ?, ?)";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, recipientId);
            st.setString(2, type);
            if (refId == null) {
                st.setNull(3, java.sql.Types.INTEGER);
            } else {
                st.setInt(3, refId);
            }
            st.setNString(4, message);
            st.executeUpdate();
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_INSERT_NOTIFICATION);
        }
    }

    public List<Notification> getUnreadNotificationsByUserId(int recipientId) throws ValidationException {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT NotificationID, RecipientID, Type, RefID, Message, IsRead, CreatedAt "
                + "FROM Notifications WHERE RecipientID = ? AND IsRead = 0 "
                + "ORDER BY CreatedAt DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, recipientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToNotification(rs));
                }
            }
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_GET_NOTIFICATION);
        }
        return list;
    }

    public List<Notification> getRecentNotificationsByUserId(int recipientId, int limit) throws ValidationException {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT TOP (?) NotificationID, RecipientID, Type, RefID, Message, IsRead, CreatedAt "
                + "FROM Notifications WHERE RecipientID = ? "
                + "ORDER BY CreatedAt DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, limit > 0 ? limit : 10);
            ps.setInt(2, recipientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToNotification(rs));
                }
            }
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_GET_NOTIFICATION);
        }
        return list;
    }

    public int countUnreadNotifications(int recipientId) throws ValidationException {
        String sql = "SELECT COUNT(1) FROM Notifications WHERE RecipientID = ? AND IsRead = 0";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, recipientId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return 0;
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_GET_NOTIFICATION);
        }
    }

    public boolean markAsRead(int notificationId, int recipientId) throws ValidationException {
        String sql = "UPDATE Notifications SET IsRead = 1 WHERE NotificationID = ? AND RecipientID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, notificationId);
            ps.setInt(2, recipientId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_UPDATE_NOTIFICATION);
        }
    }

    public boolean markAllAsRead(int recipientId) throws ValidationException {
        String sql = "UPDATE Notifications SET IsRead = 1 WHERE RecipientID = ? AND IsRead = 0";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, recipientId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_UPDATE_NOTIFICATION);
        }
    }

    private Notification mapResultSetToNotification(ResultSet rs) throws SQLException {
        int refId = rs.getInt("RefID");
        Integer ref = rs.wasNull() ? null : refId;
        return new Notification(
                rs.getInt("NotificationID"),
                rs.getInt("RecipientID"),
                rs.getString("Type"),
                ref,
                rs.getNString("Message"),
                rs.getBoolean("IsRead"),
                rs.getTimestamp("CreatedAt")
        );
    }
}

