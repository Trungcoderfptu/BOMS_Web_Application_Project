package model;

/**
 * Entity đại diện cho bảng DeliveryAreas trong CSDL
 */
public class DeliveryArea {

    private int areaId;
    private String areaName;

    public DeliveryArea() {
    }

    public DeliveryArea(int areaId, String areaName) {
        this.areaId = areaId;
        this.areaName = areaName;
    }

    public int getAreaId() {
        return areaId;
    }

    public void setAreaId(int areaId) {
        this.areaId = areaId;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }
}
