/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author AD
 */
public class Order {

    private int orderId;
    private String orderType; // 'Counter', 'Table', 'Delivery'
    private Integer staffId;
    private Integer tableId;
    private Integer areaId;
    private Integer shipperId;
    private String customerName;
    private String customerPhone;
    private String deliveryAddress;
    private String status;
    private double totalAmount;
    private Timestamp createdAt;
    private Timestamp cookingStartedAt; // Cốt lõi: Nếu null -> cho phép sửa món

    // Danh sách các món trong đơn hàng này (Đóng vai trò như cái Giỏ Hàng) tóm lại là cái để chứa các món khách gọi
    private List<OrderDetail> orderDetails;

    public Order() {
        this.orderDetails = new ArrayList<>();
    }

    public Order(int orderId, String orderType, Integer staffId, Integer tableId, Integer areaId, Integer shipperId, String customerName, String customerPhone, String deliveryAddress, String status, double totalAmount, Timestamp createdAt, Timestamp cookingStartedAt) {
        this.orderId = orderId;
        this.orderType = orderType;
        this.staffId = staffId;
        this.tableId = tableId;
        this.areaId = areaId;
        this.shipperId = shipperId;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.deliveryAddress = deliveryAddress;
        this.status = status;
        this.totalAmount = totalAmount;
        this.createdAt = createdAt;
        this.cookingStartedAt = cookingStartedAt;
        this.orderDetails = new ArrayList<>();
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public Integer getStaffId() {
        return staffId;
    }

    public void setStaffId(Integer staffId) {
        this.staffId = staffId;
    }

    public Integer getTableId() {
        return tableId;
    }

    public void setTableId(Integer tableId) {
        this.tableId = tableId;
    }

    public Integer getAreaId() {
        return areaId;
    }

    public void setAreaId(Integer areaId) {
        this.areaId = areaId;
    }

    public Integer getShipperId() {
        return shipperId;
    }

    public void setShipperId(Integer shipperId) {
        this.shipperId = shipperId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getCookingStartedAt() {
        return cookingStartedAt;
    }

    public void setCookingStartedAt(Timestamp cookingStartedAt) {
        this.cookingStartedAt = cookingStartedAt;
    }

    public List<OrderDetail> getOrderDetails() {
        return orderDetails;
    }

    public void setOrderDetails(List<OrderDetail> orderDetails) {
        this.orderDetails = orderDetails;
    }
}
