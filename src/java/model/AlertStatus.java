
package model;

/**
 * Enum chứa các trạng thái cảnh báo
 * Active: đang phát cảnh báo
 * Confirmed: đã được xác nhận bởi quản lí hoặc nhân viên
 * Closed: cảnh báo đã tắt
 */
public enum AlertStatus {
    Active,
    Confirmed,
    Closed;
}
