package model;

/**
 * Entity đại diện cho bảng MenuToppings (topping dùng chung, có giá riêng)
 */
public class MenuTopping {

    private int toppingId;
    private String toppingName;
    private double price;
    private boolean isActive;

    public MenuTopping() {
    }

    public MenuTopping(int toppingId, String toppingName, double price, boolean isActive) {
        this.toppingId = toppingId;
        this.toppingName = toppingName;
        this.price = price;
        this.isActive = isActive;
    }

    public int getToppingId() {
        return toppingId;
    }

    public void setToppingId(int toppingId) {
        this.toppingId = toppingId;
    }

    public String getToppingName() {
        return toppingName;
    }

    public void setToppingName(String toppingName) {
        this.toppingName = toppingName;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
