/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 * Entity đại diện cho bảng ProductIngredients (Công thức định lượng món ăn)
 * @author AD
 */
public class ProductIngredient {

    private int productId;
    private int ingredientId;
    private double quantityUsed;

    public ProductIngredient() {
    }

    public ProductIngredient(int productId, int ingredientId, double quantityNeeded) {
        this.productId = productId;
        this.ingredientId = ingredientId;
        this.quantityUsed = quantityNeeded;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getIngredientId() {
        return ingredientId;
    }

    public void setIngredientId(int ingredientId) {
        this.ingredientId = ingredientId;
    }

    public double getQuantityUsed() {
        return quantityUsed;
    }

    public void setQuantityUsed(double quantityNeeded) {
        this.quantityUsed = quantityNeeded;
    }
}
