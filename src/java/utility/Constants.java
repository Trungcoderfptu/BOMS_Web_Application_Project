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
    //Chưa xếp
    public static final String ERROR_DATABASE_GETALL_KEY = "DE0000";
    public static final String ERROR_LOCK_ACC = "E0016";
    public static final String ERROR_DATABASE_INSERT_KEY = "DE0001";

    // INT CONSTANTS:
    public static final int MIN_PASSWORD_LENG = 3;
    public static final int MIN_USER_LENG = 5;

    //CONSTANS Dùng chung cơ bản
    public static final String USER_SESSION = "USER_SESSION";
    public static final String ERROR_MESSAGE = "ERROR_MESSAGE";
    public static final String USER_PROFILE = "USER_PROFILE";
    public static final String SUCCESS_MESSAGE = "SUCCESS_MESSAGE";
    public static final String ACTIVE_TAB = "ACTIVE_TAB";
    //SUCCESFULL MSG
    //=================SUCCESSFULL MSG====================
    // Dải S000X: thành công liên quan đến đăng nhập (Login)

    // Dải S001X: Thành công liên quan đến bảo mật tài khoản (Đổi mật khẩu/Đăng ký)
    public static final String SUCCESS_PSW_CHANGE = "S0010";
    public static final String SUCCESS_REGISTER = "S0011";
    public static final String SUCCESS_GEN_KEY = "S0012";
    // Dải S9XXX Thành công đối với nghiệp vụ admin
    public static final String SUCCESS_ACCOUNT_STATE_CHANGE = "S9000";

    // Dải S010X: Thành công liên quan đến hồ sơ (Profile)
    public static final String SUCCESS_UPDATE_PROFILE = "S0100";
    //====================================================
    //EXCEPTION
    // ================= EXCEPTION CODES =================
    // Dải E000X: Lỗi liên quan đến đăng nhập (Login)
    public static final String ERROR_USERNAME = "E0001";
    public static final String ERROR_PASSWORD = "E0002";
    public static final String ERROR_ROLE = "E0003";
    public static final String ERROR_LOGIN_FAIL_ROLE = "E0004";
    public static final String ERROR_LOGIN_FALL = "E0005";

    // Dải E001X: Lỗi liên quan đến bảo mật tài khoản (Đổi mật khẩu/Đăng ký)
    public static final String ERROR_WORONG_OPW = "E0010";
    public static final String ERROR_CONFIRM_PSW = "E0011";
    public final static String ERROR_NEW_PSW_MATCH_OLD = "E0012";
    public static final String ERROR_USERNAME_EXIST = "E0013";
    public static final String ERROR_EMAIL_EXIST = "E0014";

    // Dải E010X: Lỗi liên quan đến hồ sơ (Profile)
    public static final String ERROR_PROFILE = "E0100";
    public static final String ERROR_NOT_UPDATED = "IE0101";
    //Dải E1XXX Lỗi liên quan đến valid fomat dữ liệu
    public static final String ERROR_USER_LENG = "E1000";
    public static final String ERROR_INVALID_PSW = "E1001";
    public static final String ERROR_EMPTY_EMAIL = "E1002";
    public static final String ERROR_INVALID_EMAIL_FORMAT = "E1003";
    public static final String ERROR_EMPTY_PHONE = "E1004";
    public static final String ERROR_INVALID_PHONE_FORMAT = "E1005";
    public static final String ERROR_EMPTY_ADDRESS = "E1006";
    public static final String ERROR_INVALID_SECURITY_CODE = "E1007";
    public static final String ERROR_EMPTY_FULLNAME = "E1008";

    // Dải E9XXXX: Lỗi liên quan đến hệ thống SYSTEM
    public static final String SERROR_INVALID_SETTING_TAB = "E9000";
}
