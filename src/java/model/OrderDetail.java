/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author AD
 */
public class OrderDetail {

    private int orderId;
    private int productId;
    private int quantity;
    private double unitPrice;

    // Tuỳ chọn món (bản sao tại thời điểm đặt). Mức đá/đường lưu dạng mã (ICE_100, SUGAR_70...)
    private String sizeName;
    private String iceLevel;
    private String sugarLevel;
    private String toppings;
    private String note;

    // Chỉ dùng trong giỏ hàng (session), không lưu database:
    // khóa phân biệt 2 dòng cùng món nhưng khác tuỳ chọn
    private String lineKey;

    //chỉ dùng hiện thị và support không lưu database thuộc tính này
    private Product product;

    public OrderDetail() {
    }

    public OrderDetail(int orderId, int productId, int quantity, double unitPrice) {
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public String getSizeName() {
        return sizeName;
    }

    public void setSizeName(String sizeName) {
        this.sizeName = sizeName;
    }

    public String getIceLevel() {
        return iceLevel;
    }

    public void setIceLevel(String iceLevel) {
        this.iceLevel = iceLevel;
    }

    public String getSugarLevel() {
        return sugarLevel;
    }

    public void setSugarLevel(String sugarLevel) {
        this.sugarLevel = sugarLevel;
    }

    public String getToppings() {
        return toppings;
    }

    public void setToppings(String toppings) {
        this.toppings = toppings;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getLineKey() {
        return lineKey;
    }

    public void setLineKey(String lineKey) {
        this.lineKey = lineKey;
    }
}
