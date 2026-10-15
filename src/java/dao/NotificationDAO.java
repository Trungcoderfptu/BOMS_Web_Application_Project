/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import dal.DBContext;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import utility.Constants;
import utility.ValidationException;

/**
 * Data Access Object cho bảng Notifications (dùng chung toàn hệ thống)
 *
 * @author AD
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
            st.setString(4, message);
            st.executeUpdate();
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_INSERT_NOTIFICATION);
        }
    }
}
