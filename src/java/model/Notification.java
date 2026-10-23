package model;

import java.sql.Timestamp;

/**
 * Entity Model đại diện 1-1 cho bảng Notifications trong CSDL BOMS
 * Tuân thủ Rule_BOMS: Chỉ chứa thuộc tính private, constructor, getter/setter.
 */
public class Notification {

    private int notificationId;
    private int recipientId;
    private String type;
    private Integer refId;
    private String message;
    private boolean isRead;
    private Timestamp createdAt;

    public Notification() {
    }

    public Notification(int notificationId, int recipientId, String type, Integer refId, String message, boolean isRead, Timestamp createdAt) {
        this.notificationId = notificationId;
        this.recipientId = recipientId;
        this.type = type;
        this.refId = refId;
        this.message = message;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }

    public int getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(int notificationId) {
        this.notificationId = notificationId;
    }

    public int getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(int recipientId) {
        this.recipientId = recipientId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getRefId() {
        return refId;
    }

    public void setRefId(Integer refId) {
        this.refId = refId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isIsRead() {
        return isRead;
    }

    public void setIsRead(boolean isRead) {
        this.isRead = isRead;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
