
package model;

/**
 * Enum chứa các loại cảnh báo 
 * LowStock: quantity ít hơn threshold
 * OutOfStock: quantity = 0
 * Expired: hết hạn
 */
public enum AlertType {
    LowStock,
    OutOfStock,
    ExpiringSoon,
    Expired;
}
