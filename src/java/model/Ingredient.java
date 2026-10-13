/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 * Entity đại diện cho bảng Ingredients trong cơ sở dữ liệu
 * @author AD
 */
public class Ingredient {

    private int ingredientId;
    private int categoryId;
    private String ingredientName;
    private String unit;
    private double quantity;
    private double price;
    private String storageLocation;
    private double warningThreshold;
    private boolean isActive;

    public Ingredient() {
    }

    public Ingredient(int ingredientId, int categoryId, String ingredientName, String unit, double quantity, double warningThreshold, boolean isActive) {
        this.ingredientId = ingredientId;
        this.categoryId = categoryId;
        this.ingredientName = ingredientName;
        this.unit = unit;
        this.quantity = quantity;
        this.price = 0.0;
        this.storageLocation = null;
        this.warningThreshold = warningThreshold;
        this.isActive = isActive;
    }

    public Ingredient(int ingredientId, int categoryId, String ingredientName, String unit, double quantity, double price, String storageLocation, double warningThreshold, boolean isActive) {
        this.ingredientId = ingredientId;
        this.categoryId = categoryId;
        this.ingredientName = ingredientName;
        this.unit = unit;
        this.quantity = quantity;
        this.price = price;
        this.storageLocation = storageLocation;
        this.warningThreshold = warningThreshold;
        this.isActive = isActive;
    }

    public int getIngredientId() {
        return ingredientId;
    }

    public void setIngredientId(int ingredientId) {
        this.ingredientId = ingredientId;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getIngredientName() {
        return ingredientName;
    }

    public void setIngredientName(String ingredientName) {
        this.ingredientName = ingredientName;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public double getWarningThreshold() {
        return warningThreshold;
    }

    public void setWarningThreshold(double warningThreshold) {
        this.warningThreshold = warningThreshold;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getStorageLocation() {
        return storageLocation;
    }

    public void setStorageLocation(String storageLocation) {
        this.storageLocation = storageLocation;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }
}
