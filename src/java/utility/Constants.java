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
    public static final String ERROR_DATABASE_GETALL_USER = "DE0002";
    public static final String ERROR_DATABASE_UPDATE_STATUS = "DE0003";
    public static final String ERROR_DATABASE_GET_USER_BY_USERNAME = "DE0004";
    public static final String ERROR_DATABASE_GET_USER_BY_ID = "DE0005";
    public static final String ERROR_DATABASE_GET_USER_BY_EMAIL = "DE0006";
    public static final String ERROR_DATABASE_UPDATE_INFO = "DE0007";
    public static final String ERROR_DATABASE_INSERT_USER = "DE008";
    public static final String ERROR_DATABASE_TRANSACTION = "DE009";
    public static final String ERROR_DATABASE_UPDATE_UID_FOR_KEY = "DE0010";
    public static final String ERROR_DATABASE_INSERT_ORDER = "DE0011";
    public static final String ERROR_DATABASE_GET_REPORT = "DE0012";
    public static final String ERROR_DATABASE_INSERT_REPORT = "DE0013";
    public static final String ERROR_DATABASE_UPDATE_REPORT = "DE0014";
    public static final String ERROR_DATABASE_INSERT_NOTIFICATION = "DE0015";

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

    // Dải S020X: Thành công liên quan đến Đơn hàng (Order)
    public static final String SUCCESS_CHECKOUT = "S0200";
    public static final String SUCCESS_ADD_TO_CART = "S0201";
    public static final String SUCCESS_UPDATE_CART = "S0202";
    public static final String SUCCESS_REMOVE_FROM_CART = "S0203";
    public static final String SUCCESS_CLEAR_CART = "S0204";
    public static final String SUCCESS_UPDATE_ORDER_STATUS = "S0205";
    public static final String SUCCESS_CANCEL_ORDER = "S0206";
    public static final String SUCCESS_CHANGE_DISH = "S0207";

    // Dải S030X: Thành công liên quan đến Báo cáo ẩn danh (UC-07, UC-08)
    public static final String SUCCESS_SUBMIT_REPORT = "S0300";
    public static final String SUCCESS_UPDATE_REPORT = "S0301";
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

    // Dải E020X: Lỗi liên quan đến Đơn hàng (Order)
    public static final String ERROR_EMPTY_CART = "E0200";
    public static final String ERROR_INVALID_TABLE = "E0201";
    public static final String ERROR_PRODUCT_NOT_AVAILABLE = "E0202";
    public static final String ERROR_ORDER_NOT_FOUND = "E0203";
    public static final String ERROR_INVALID_ORDER_STATUS = "E0204";
    public static final String ERROR_INSUFFICIENT_INGREDIENTS = "E0205";
    public static final String ERROR_ORDER_ALREADY_COOKING = "E0206";
    public static final String ERROR_ORDER_CANNOT_BE_CANCELLED = "E0207";
    public static final String ERROR_INVALID_EXCHANGE_PRODUCT = "E0208";

    // Dải E030X: Lỗi liên quan đến Báo cáo ẩn danh (UC-07, UC-08)
    public static final String ERROR_REPORT_EMPTY_CATEGORY = "E0300";
    public static final String ERROR_REPORT_EMPTY_TITLE = "E0301";
    public static final String ERROR_REPORT_EMPTY_DESCRIPTION = "E0302";
    public static final String ERROR_REPORT_TITLE_TOO_LONG = "E0303";
    public static final String ERROR_REPORT_NO_EVIDENCE = "E0304";
    public static final String ERROR_REPORT_INVALID_FILE_TYPE = "E0305";
    public static final String ERROR_REPORT_FILE_TOO_LARGE = "E0306";
    public static final String ERROR_REPORT_TOO_MANY_FILES = "E0307";
    public static final String ERROR_REPORT_NOT_FOUND = "E0308";
    public static final String ERROR_REPORT_TICKET_NOT_FOUND = "E0309";
    public static final String ERROR_REPORT_INVALID_STATUS = "E0310";
    public static final String ERROR_REPORT_REJECT_NEED_FEEDBACK = "E0311";
    public static final String ERROR_REPORT_ALREADY_CLOSED = "E0312";
    public static final String ERROR_REPORT_NO_PERMISSION = "E0313";
    public static final String ERROR_EVIDENCE_NOT_FOUND = "E0314";
    public static final String ERROR_REPORT_UPLOAD_FAILED = "E0315";
    public static final String ERROR_REPORT_EMPTY_TICKET = "E0316";

    // Hằng số nghiệp vụ Báo cáo ẩn danh
    public static final String REPORT_STATUS_PENDING = "Pending";
    public static final String REPORT_STATUS_IN_PROGRESS = "In Progress";
    public static final String REPORT_STATUS_RESOLVED = "Resolved";
    public static final String REPORT_STATUS_REJECTED = "Rejected";
    public static final String REPORT_FILTER_ALL = "ALL";
    public static final String REPORT_TICKET_PREFIX = "TK-";
    public static final String NOTIFICATION_TYPE_REPORT = "Report";
    public static final int REPORT_TITLE_MAX_LENG = 200;
    public static final int REPORT_MAX_EVIDENCE_FILES = 5;
    public static final long REPORT_MAX_EVIDENCE_SIZE = 20L * 1024 * 1024; // 20 MB (7.0.E1)
    public static final String[] REPORT_ALLOWED_FILE_TYPES = {"jpg", "jpeg", "png", "mp4", "pdf", "docx"};
    public static final String UPLOAD_CONFIG_FILE = "../Upload.properties";
    public static final String UPLOAD_CONFIG_KEY = "upload.dir";
    public static final String DEFAULT_UPLOAD_DIR = "C:/BOMS_uploads/reports";

    // Dải E9XXXX: Lỗi liên quan đến hệ thống SYSTEM
    public static final String SERROR_INVALID_SETTING_TAB = "E9000";
}
