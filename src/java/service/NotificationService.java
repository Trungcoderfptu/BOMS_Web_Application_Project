package service;

import dao.NotificationDAO;
import dao.UserDAO;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.Notification;
import utility.Constants;
import utility.ValidationException;
import utility.Validator;

/**
 * Service xử lý nghiệp vụ Thông báo & Phát thanh toàn cục (Task 11)
 * Tuân thủ Rule_BOMS: 100% logic tại Service, đóng gói Map trả về Controller, cấm để lọt Model.
 */
public class NotificationService {

    private NotificationDAO notificationDAO;
    private UserDAO userDAO;
    private SimpleDateFormat dateTimeFormat;

    public NotificationService() {
        this.notificationDAO = new NotificationDAO();
        this.userDAO = new UserDAO();
        this.dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    }

    public int getUnreadCount(int userId) throws ValidationException {
        if (userId <= 0) {
            return 0;
        }
        return notificationDAO.countUnreadNotifications(userId);
    }

    public List<Map<String, Object>> getUnreadNotificationsForView(int userId) throws ValidationException {
        if (userId <= 0) {
            return new ArrayList<>();
        }
        List<Notification> rawList = notificationDAO.getUnreadNotificationsByUserId(userId);
        return convertNotificationsToView(rawList);
    }

    public List<Map<String, Object>> getRecentNotificationsForView(int userId, int limit) throws ValidationException {
        if (userId <= 0) {
            return new ArrayList<>();
        }
        List<Notification> rawList = notificationDAO.getRecentNotificationsByUserId(userId, limit);
        return convertNotificationsToView(rawList);
    }

    public boolean markAsRead(int notificationId, int userId) throws ValidationException {
        if (notificationId <= 0 || userId <= 0) {
            throw new ValidationException(Constants.ERROR_NOTIFICATION_NOT_FOUND);
        }
        return notificationDAO.markAsRead(notificationId, userId);
    }

    public boolean markAllAsRead(int userId) throws ValidationException {
        if (userId <= 0) {
            return false;
        }
        return notificationDAO.markAllAsRead(userId);
    }

    public void createNotification(int recipientId, String type, Integer refId, String message) throws ValidationException {
        if (recipientId <= 0 || message == null || message.trim().isEmpty()) {
            return;
        }
        notificationDAO.insertNotification(recipientId, type, refId, message.trim());
    }

    public int broadcastMessage(String message, int senderId) throws ValidationException {
        Validator.checkEmpty(message, Constants.ERROR_EMPTY_BROADCAST_MESSAGE);
        Validator.checkMaxLength(message, 500, Constants.ERROR_EMPTY_BROADCAST_MESSAGE);

        List<Integer> activeUserIds = userDAO.getAllActiveUserIds();
        int sentCount = 0;
        for (Integer uid : activeUserIds) {
            if (uid != null && uid > 0) {
                notificationDAO.insertNotification(uid, "Broadcast", senderId, message.trim());
                sentCount++;
            }
        }
        return sentCount;
    }

    private List<Map<String, Object>> convertNotificationsToView(List<Notification> rawList) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (rawList != null) {
            for (Notification n : rawList) {
                Map<String, Object> map = new HashMap<>();
                map.put("notificationId", n.getNotificationId());
                map.put("recipientId", n.getRecipientId());
                map.put("type", n.getType());
                map.put("refId", n.getRefId());
                map.put("message", n.getMessage());
                map.put("isRead", n.isIsRead());
                String createdStr = n.getCreatedAt() != null ? dateTimeFormat.format(n.getCreatedAt()) : "";
                map.put("createdAt", createdStr);

                String icon = "bell";
                if ("Broadcast".equalsIgnoreCase(n.getType())) {
                    icon = "megaphone";
                } else if ("ShiftAssignment".equalsIgnoreCase(n.getType())) {
                    icon = "calendar";
                } else if ("AttendanceApproval".equalsIgnoreCase(n.getType())) {
                    icon = "clock";
                } else if ("InventoryAlert".equalsIgnoreCase(n.getType())) {
                    icon = "triangle-warning";
                } else if ("Delivery".equalsIgnoreCase(n.getType())) {
                    icon = "moped";
                }
                map.put("icon", icon);

                list.add(map);
            }
        }
        return list;
    }
}
