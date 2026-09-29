/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utility;

/**
 *
 * @author AD
 */
public class Constants {

    private Constants() {

    }
    // INT CONSTANTS:
    public static final int minLeng = 3;

    // STRING CONSTAINS:
    //CONSTANS Dùng chung cơ bản
    public static final String USER_SESSION = "USER_SESSION";
    public static final String ERROR_MESSAGE = "ERROR_MESSAGE";
    public static final String USER_PROFILE = "USER_PROFILE";
    public static final String SUCCESS_MESSAGE = "SUCCESS_MESSAGE";
    //EXCEPTION
    public static final String ERROR_MESSAGE_PROFILE = "Không tìm thấy thông tin tài khoản!";
    public static final String ERROR_MESSAGE_USERNAME = "Tên đăng nhập không hợp lệ! kiểm tra lại tên đăng nhập: không được bỏ trống; không được chứa kí tự đặc biệt";
    public static final String ERROR_MESSAGE_PASSWORD = "Mật khẩu không hợp lệ! Không được bỏ trống hoạc ít nhất dài " + minLeng + " ký tự.";
    public static final String ERROR_MESSAGE_ROLE = "Vai trò không hợp lệ.";
    public static final String ERROR_MESSAGE_LOGIN_FALL = "Tên đăng nhập hoạc mật khẩu chưa đúng, vui lòng kiểm tra lại.\n"
            + "Nếu vẫn không được có thể tài khoản đã bị khóa hãy liên hệ với quản trị viên để biết thêm chi tiết.";
    public static final String ERROR_MESSAGE_LOGIN_FAIL_ROLE = "Tài khoản này không có quyền truy cập vào vai trò này.";
    public static final String ERROR_MESSAGE_PASSWORD_CHANGE_FALL_OLD_PASS = "Mật khẩu cũ không chính xác!";
    public static final String ERROR_MESSAGE_PASSWORD_CHANGE_FALL_NEW_PASS = "Mật khẩu xác nhận không khớp!";

}
