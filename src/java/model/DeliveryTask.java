package model;

public class DeliveryTask {

    private int taskId;
    private String orderCode;
    private String customerAddress;
    private String status; // "Chờ điều phối", "Đang giao", "Hoàn thành"
    private String shipperUsername; // Tên tài khoản shipper được gán

    public DeliveryTask() {
    }

    public DeliveryTask(int taskId, String orderCode, String customerAddress, String status, String shipperUsername) {
        this.taskId = taskId;
        this.orderCode = orderCode;
        this.customerAddress = customerAddress;
        this.status = status;
        this.shipperUsername = shipperUsername;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public String getCustomerAddress() {
        return customerAddress;
    }

    public void setCustomerAddress(String customerAddress) {
        this.customerAddress = customerAddress;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getShipperUsername() {
        return shipperUsername;
    }

    public void setShipperUsername(String shipperUsername) {
        this.shipperUsername = shipperUsername;
    }
}
