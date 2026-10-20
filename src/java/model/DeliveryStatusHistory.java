package model;

import java.sql.Timestamp;

/**
 * Entity đại diện cho bảng DeliveryStatusHistory trong CSDL
 */
public class DeliveryStatusHistory {

    private int historyId;
    private int orderId;
    private int shipperId;
    private String status;
    private String failReason;
    private String proofImage;
    private Timestamp updatedAt;

    public DeliveryStatusHistory() {
    }

    public DeliveryStatusHistory(int historyId, int orderId, int shipperId, String status, String failReason, String proofImage, Timestamp updatedAt) {
        this.historyId = historyId;
        this.orderId = orderId;
        this.shipperId = shipperId;
        this.status = status;
        this.failReason = failReason;
        this.proofImage = proofImage;
        this.updatedAt = updatedAt;
    }

    public int getHistoryId() {
        return historyId;
    }

    public void setHistoryId(int historyId) {
        this.historyId = historyId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getShipperId() {
        return shipperId;
    }

    public void setShipperId(int shipperId) {
        this.shipperId = shipperId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFailReason() {
        return failReason;
    }

    public void setFailReason(String failReason) {
        this.failReason = failReason;
    }

    public String getProofImage() {
        return proofImage;
    }

    public void setProofImage(String proofImage) {
        this.proofImage = proofImage;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
}
