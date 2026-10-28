package model;

/**
 * Entity đại diện cho bảng MenuSizes (size ly dùng chung, có tiền cộng thêm)
 */
public class MenuSize {

    private int sizeId;
    private String sizeName;
    private double extraPrice;
    private int sortOrder;
    private boolean isActive;

    public MenuSize() {
    }

    public MenuSize(int sizeId, String sizeName, double extraPrice, int sortOrder, boolean isActive) {
        this.sizeId = sizeId;
        this.sizeName = sizeName;
        this.extraPrice = extraPrice;
        this.sortOrder = sortOrder;
        this.isActive = isActive;
    }

    public int getSizeId() {
        return sizeId;
    }

    public void setSizeId(int sizeId) {
        this.sizeId = sizeId;
    }

    public String getSizeName() {
        return sizeName;
    }

    public void setSizeName(String sizeName) {
        this.sizeName = sizeName;
    }

    public double getExtraPrice() {
        return extraPrice;
    }

    public void setExtraPrice(double extraPrice) {
        this.extraPrice = extraPrice;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
