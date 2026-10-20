package model;

/**
 * Entity đại diện cho bảng ShipperProfiles trong CSDL
 */
public class ShipperProfile {

    private int userId;
    private String vehiclePlate;
    private String shipperStatus; // 'Offline', 'Ready', 'Delivering'
    private int activeOrders;
    private int maxOrders;

    public ShipperProfile() {
        this.shipperStatus = "Offline";
        this.activeOrders = 0;
        this.maxOrders = 3;
    }

    public ShipperProfile(int userId, String vehiclePlate, String shipperStatus, int activeOrders, int maxOrders) {
        this.userId = userId;
        this.vehiclePlate = vehiclePlate;
        this.shipperStatus = shipperStatus;
        this.activeOrders = activeOrders;
        this.maxOrders = maxOrders;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getVehiclePlate() {
        return vehiclePlate;
    }

    public void setVehiclePlate(String vehiclePlate) {
        this.vehiclePlate = vehiclePlate;
    }

    public String getShipperStatus() {
        return shipperStatus;
    }

    public void setShipperStatus(String shipperStatus) {
        this.shipperStatus = shipperStatus;
    }

    public int getActiveOrders() {
        return activeOrders;
    }

    public void setActiveOrders(int activeOrders) {
        this.activeOrders = activeOrders;
    }

    public int getMaxOrders() {
        return maxOrders;
    }

    public void setMaxOrders(int maxOrders) {
        this.maxOrders = maxOrders;
    }
}
