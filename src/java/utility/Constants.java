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
    // =========================================================================
    // 1. CẤU HÌNH HỆ THỐNG & GIỚI HẠN DỮ LIỆU (VALIDATION LIMITS)
    // =========================================================================
    public static final int MIN_PASSWORD_LENG = 3;
    public static final int MIN_USER_LENG = 5;

    // =========================================================================
    // 2. KHÓA PHIÊN & THUỘC TÍNH DÙNG CHUNG (SESSION & ATTRIBUTES)
    // =========================================================================
    public static final String USER_SESSION = "USER_SESSION";
    public static final String USER_PROFILE = "USER_PROFILE";
    public static final String ERROR_MESSAGE = "ERROR_MESSAGE";
    public static final String SUCCESS_MESSAGE = "SUCCESS_MESSAGE";
    public static final String ACTIVE_TAB = "ACTIVE_TAB";

    // =========================================================================
    // 3. DẢI MÃ THÀNH CÔNG (SUCCESS CODES - SXXX)
    // =========================================================================
    // Dải S001X: Bảo mật tài khoản & Đăng ký
    public static final String SUCCESS_PSW_CHANGE = "S0010";
    public static final String SUCCESS_REGISTER = "S0011";
    public static final String SUCCESS_GEN_KEY = "S0012";

    // Dải S010X: Hồ sơ cá nhân (Profile)
    public static final String SUCCESS_UPDATE_PROFILE = "S0100";

    // Dải S020X: Bán hàng, Giỏ hàng & Đơn hàng (POS / QR Table)
    public static final String SUCCESS_CHECKOUT = "S0200";
    public static final String SUCCESS_ADD_TO_CART = "S0201";
    public static final String SUCCESS_UPDATE_CART = "S0202";
    public static final String SUCCESS_REMOVE_FROM_CART = "S0203";
    public static final String SUCCESS_CLEAR_CART = "S0204";
    public static final String SUCCESS_UPDATE_ORDER_STATUS = "S0205";
    public static final String SUCCESS_CANCEL_ORDER = "S0206";
    public static final String SUCCESS_CHANGE_DISH = "S0207";
    public static final String SUCCESS_CREATE_PRODUCT = "S0208";
    public static final String SUCCESS_UPDATE_PRODUCT = "S0209";
    public static final String SUCCESS_CHANGE_PRODUCT_STATUS = "S0210";

    // Dải S030X: Quản lý Kho nguyên liệu (Inventory)
    public static final String SUCCESS_UPDATE_THRESHOLD = "S0301";
    public static final String SUCCESS_CREATE_INGREDIENT = "S0302";
    public static final String SUCCESS_UPDATE_INGREDIENT = "S0303";
    public static final String SUCCESS_DELETE_INGREDIENT = "S0304";

    // Dải S040X: Cảnh báo Tồn kho & Hết hạn (Alerts)
    public static final String SUCCESS_CONFIRM_ALERT = "S0401";
    public static final String SUCCESS_CLOSE_ALERT = "S0402";

    // Dải S9XXX: Nghiệp vụ Quản trị hệ thống (Admin)
    public static final String SUCCESS_ACCOUNT_STATE_CHANGE = "S9000";

    // =========================================================================
    // 4. DẢI MÃ LỖI NGHIỆP VỤ & XÁC THỰC (EXCEPTION CODES - EXXX)
    // =========================================================================
    // Dải E000X: Xác thực & Đăng nhập (Login)
    public static final String ERROR_USERNAME = "E0001";
    public static final String ERROR_PASSWORD = "E0002";
    public static final String ERROR_ROLE = "E0003";
    public static final String ERROR_LOGIN_FAIL_ROLE = "E0004";
    public static final String ERROR_LOGIN_FALL = "E0005";

    // Dải E001X: Bảo mật tài khoản & Đăng ký
    public static final String ERROR_WORONG_OPW = "E0010";
    public static final String ERROR_CONFIRM_PSW = "E0011";
    public static final String ERROR_NEW_PSW_MATCH_OLD = "E0012";
    public static final String ERROR_USERNAME_EXIST = "E0013";
    public static final String ERROR_EMAIL_EXIST = "E0014";
    public static final String ERROR_LOCK_ACC = "E0016";

    // Dải E010X: Hồ sơ nhân sự (Profile & Info)
    public static final String ERROR_PROFILE = "E0100";
    public static final String ERROR_NOT_UPDATED = "IE0101";

    // Dải E100X: Validation định dạng dữ liệu thô (Tầng 1 - Utility)
    public static final String ERROR_USER_LENG = "E1000";
    public static final String ERROR_INVALID_PSW = "E1001";
    public static final String ERROR_EMPTY_EMAIL = "E1002";
    public static final String ERROR_INVALID_EMAIL_FORMAT = "E1003";
    public static final String ERROR_EMPTY_PHONE = "E1004";
    public static final String ERROR_INVALID_PHONE_FORMAT = "E1005";
    public static final String ERROR_EMPTY_ADDRESS = "E1006";
    public static final String ERROR_INVALID_SECURITY_CODE = "E1007";
    public static final String ERROR_EMPTY_FULLNAME = "E1008";
    public static final String ERROR_EMPTY_PRODUCT_NAME = "E1009";
    public static final String ERROR_INVALID_PRODUCT_PRICE = "E1010";
    public static final String ERROR_EMPTY_PRODUCT_CATEGORY = "E1011";
    public static final String ERROR_INVALID_IMAGE_TYPE = "E1012";
    public static final String ERROR_IMAGE_SIZE_EXCEEDED = "E1013";
    public static final String ERROR_IMAGE_UPLOAD_FAILED = "E1014";
    public static final String ERROR_IMAGE_URL_TOO_LONG = "E1015";

    // Dải E020X: Bán hàng, Giỏ hàng & Đơn hàng (POS / QR Table / Bếp)
    public static final String ERROR_EMPTY_CART = "E0200";
    public static final String ERROR_INVALID_TABLE = "E0201";
    public static final String ERROR_PRODUCT_NOT_AVAILABLE = "E0202";
    public static final String ERROR_ORDER_NOT_FOUND = "E0203";
    public static final String ERROR_INVALID_ORDER_STATUS = "E0204";
    public static final String ERROR_INSUFFICIENT_INGREDIENTS = "E0205";
    public static final String ERROR_ORDER_ALREADY_COOKING = "E0206";
    public static final String ERROR_ORDER_CANNOT_BE_CANCELLED = "E0207";
    public static final String ERROR_INVALID_EXCHANGE_PRODUCT = "E0208";
    public static final String ERROR_CANCELLED_ORDER_LOCKED = "E0209";
    public static final String ERROR_PRODUCT_NAME_EXISTS = "E0210";
    public static final String ERROR_PRODUCT_NOT_FOUND = "E0211";

    // Dải E030X: Quản lý Kho nguyên liệu (Inventory)
    public static final String ERROR_INGREDIENT_NOT_FOUND = "E0301";
    public static final String ERROR_INGREDIENT_NAME_EXISTS = "E0302";
    public static final String ERROR_INVALID_THRESHOLD = "E0303";
    public static final String ERROR_EMPTY_INGREDIENT_NAME = "E0304";
    public static final String ERROR_INVALID_INGREDIENT_PRICE = "E0305";
    public static final String ERROR_INVALID_INGREDIENT_QTY = "E0306";
    public static final String ERROR_INSUFFICIENT_PERMISSION = "E0307";
    public static final String ERROR_INVALID_INGREDIENT_DATA = "E0308";

    // Dải E040X: Cảnh báo Tồn kho & Hết hạn (Alerts)
    public static final String ERROR_ALERT_NOT_FOUND = "E0401";

    // Dải E9XXX: Cấu hình & Nghiệp vụ Quản trị (Admin / System)
    public static final String SERROR_INVALID_SETTING_TAB = "E9000";

    // =========================================================================
    // 5. DẢI MÃ LỖI CƠ SỞ DỮ LIỆU (DATABASE EXCEPTION CODES - DEXXX)
    // =========================================================================
    // Nhóm Hệ thống & Transaction
    public static final String ERROR_DATABASE_TRANSACTION = "DE009";

    // Nhóm Tài khoản & Người dùng (User DAO)
    public static final String ERROR_DATABASE_GETALL_USER = "DE0002";
    public static final String ERROR_DATABASE_UPDATE_STATUS = "DE0003";
    public static final String ERROR_DATABASE_GET_USER_BY_USERNAME = "DE0004";
    public static final String ERROR_DATABASE_GET_USER_BY_ID = "DE0005";
    public static final String ERROR_DATABASE_GET_USER_BY_EMAIL = "DE0006";
    public static final String ERROR_DATABASE_UPDATE_INFO = "DE0007";
    public static final String ERROR_DATABASE_INSERT_USER = "DE008";

    // Nhóm Mã bảo mật tài khoản (AccountManager DAO)
    public static final String ERROR_DATABASE_GETALL_KEY = "DE0000";
    public static final String ERROR_DATABASE_INSERT_KEY = "DE0001";
    public static final String ERROR_DATABASE_UPDATE_UID_FOR_KEY = "DE0010";

    // Nhóm Thực đơn & Món ăn (Product DAO)
    public static final String ERROR_DATABASE_GET_PRODUCT = "DE0012";
    public static final String ERROR_DATABASE_UPDATE_PRODUCT = "DE0013";

    // Nhóm Kho & Công thức nguyên liệu (Ingredient DAO)
    public static final String ERROR_DATABASE_GET_INGREDIENT = "DE0014";
    public static final String ERROR_DATABASE_UPDATE_INGREDIENT = "DE0015";
    public static final String ERROR_DATABASE_UPDATE_THRESHOLD = "DE0018";

    // Nhóm Cảnh báo Kho (Alert DAO)
    public static final String ERROR_DATABASE_GET_ALERT = "DE0019";
    public static final String ERROR_DATABASE_UPDATE_ALERT = "DE0020";

    // Nhóm Đơn hàng & Chi tiết bán lẻ (Order DAO)
    public static final String ERROR_DATABASE_INSERT_ORDER = "DE0011";
    public static final String ERROR_DATABASE_GET_ORDER = "DE0016";
    public static final String ERROR_DATABASE_UPDATE_ORDER = "DE0017";
}
