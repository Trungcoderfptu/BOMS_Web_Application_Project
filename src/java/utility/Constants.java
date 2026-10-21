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
    public static final String SUCCESS_CREATE_PRODUCT = "S0208";
    public static final String SUCCESS_UPDATE_PRODUCT = "S0209";
    public static final String SUCCESS_CHANGE_PRODUCT_STATUS = "S0210";
    public static final String SUCCESS_DISPATCH_ORDER = "S0211";
    public static final String SUCCESS_REVERT_DELIVERY_ORDER = "S0212";

    // Dải S030X: Quản lý Chấm công & Ca làm việc (Time Attendance - UC-05 & UC-06)
    public static final String SUCCESS_ATTENDANCE_CHECK_IN = "S0301";
    public static final String SUCCESS_ATTENDANCE_CHECK_OUT = "S0302";
    // Dải S030X: Báo cáo ẩn danh (UC-07, UC-08)
    public static final String SUCCESS_SUBMIT_REPORT = "S0300";
    public static final String SUCCESS_UPDATE_REPORT = "S0301";

    // Dải S040X: Quản lý Kho nguyên liệu (Inventory)
    public static final String SUCCESS_UPDATE_THRESHOLD = "S0401";
    public static final String SUCCESS_CREATE_INGREDIENT = "S0402";
    public static final String SUCCESS_UPDATE_INGREDIENT = "S0403";
    public static final String SUCCESS_DELETE_INGREDIENT = "S0404";

    // Dải S050X: Cảnh báo Tồn kho & Hết hạn (Alerts)
    public static final String SUCCESS_CONFIRM_ALERT = "S0501";
    public static final String SUCCESS_CLOSE_ALERT = "S0502";

    // Dải S9XXX: Nghiệp vụ Quản trị hệ thống (Admin)
    public static final String SUCCESS_ACCOUNT_STATE_CHANGE = "S9000";

    // Dải S060X: Điều phối & Giao hàng (Dispatch & Shipper - UC-09 & UC-10)
    public static final String SUCCESS_ASSIGN_SHIPPER = "S0601";
    public static final String SUCCESS_CLAIM_TASK = "S0602";
    public static final String SUCCESS_ACCEPT_TASK = "S0603";
    public static final String SUCCESS_REJECT_TASK = "S0604";
    public static final String SUCCESS_COMPLETE_DELIVERY = "S0605";
    public static final String SUCCESS_FAIL_DELIVERY = "S0606";
    public static final String SUCCESS_UPDATE_DELIVERY_STATUS = "S0607";

    // =========================================================================
    // 4. DẢI MÃ LỖI NGHIỆP VỤ & XÁC THỰC (EXCEPTION CODES - EXXX)
    // =========================================================================
    // Dải E000X: Xác thực & Đăng nhập (Login)
    public static final String ERROR_USERNAME = "E0001";
    public static final String ERROR_PASSWORD = "E0002";
    public static final String ERROR_ROLE = "E0003";
    public static final String ERROR_LOGIN_FAIL_ROLE = "E0004";
    public static final String ERROR_UNAUTHORIZED = "E0004";
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
    public static final String ERROR_CANCELLED_ORDER_LOCKED = "E0209";
    public static final String ERROR_PRODUCT_NAME_EXISTS = "E0210";
    public static final String ERROR_PRODUCT_NOT_FOUND = "E0211";
    public static final String ERROR_INVALID_RECIPE_QUANTITY = "E0212";
    public static final String ERROR_EMPTY_CUSTOMER_NAME = "E0213";
    public static final String ERROR_EMPTY_CUSTOMER_PHONE = "E0214";
    public static final String ERROR_EMPTY_DELIVERY_ADDRESS = "E0215";
    public static final String ERROR_INVALID_DELIVERY_AREA = "E0216";
    public static final String ERROR_ORDER_CANNOT_DISPATCH = "E0217";
    public static final String ERROR_CANNOT_REVERT_DELIVERY_IN_PROGRESS = "E0218";

    // Dải E030X: Quản lý Chấm công & Ca làm việc (Time Attendance - UC-05 & UC-06)
    public static final String ERROR_ATTENDANCE_NO_SHIFT = "E0301";
    public static final String ERROR_ATTENDANCE_ALREADY_CHECKED_IN = "E0302";
    public static final String ERROR_ATTENDANCE_NOT_CHECKED_IN = "E0303";
    public static final String ERROR_ATTENDANCE_ALREADY_CHECKED_OUT = "E0304";
    public static final String ERROR_ATTENDANCE_PENDING_ORDERS = "E0305";
    public static final String ERROR_ATTENDANCE_EMPTY_OT_REASON = "E0306";
    // Dải E030X: Báo cáo ẩn danh (UC-07, UC-08)
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

    // Dải E040X: Quản lý Kho nguyên liệu (Inventory)
    public static final String ERROR_INGREDIENT_NOT_FOUND = "E0401";
    public static final String ERROR_INGREDIENT_NAME_EXISTS = "E0402";
    public static final String ERROR_INVALID_THRESHOLD = "E0403";
    public static final String ERROR_EMPTY_INGREDIENT_NAME = "E0404";
    public static final String ERROR_INVALID_INGREDIENT_PRICE = "E0405";
    public static final String ERROR_INVALID_INGREDIENT_QTY = "E0406";
    public static final String ERROR_INSUFFICIENT_PERMISSION = "E0407";
    public static final String ERROR_INVALID_INGREDIENT_DATA = "E0408";

    // Dải E050X: Cảnh báo Tồn kho & Hết hạn (Alerts)
    public static final String ERROR_ALERT_NOT_FOUND = "E0501";

    // Dải E9XXX: Cấu hình & Nghiệp vụ Quản trị (Admin / System)
    public static final String SERROR_INVALID_SETTING_TAB = "E9000";
    public static final String ERROR_INVALID_ACTION = "E9000";

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

    // Nhóm Chấm công & Ca làm việc (Attendance DAO)
    public static final String ERROR_DATABASE_GET_ATTENDANCE = "DE0018";
    public static final String ERROR_DATABASE_INSERT_ATTENDANCE = "DE0019";
    public static final String ERROR_DATABASE_UPDATE_ATTENDANCE = "DE0020";
    // Nhóm Báo cáo ẩn danh & Thông báo (Report DAO, Notification DAO)
    public static final String ERROR_DATABASE_GET_REPORT = "DE0030";
    public static final String ERROR_DATABASE_INSERT_REPORT = "DE0031";
    public static final String ERROR_DATABASE_UPDATE_REPORT = "DE0032";
    public static final String ERROR_DATABASE_INSERT_NOTIFICATION = "DE0033";

    // =========================================================================
    // 6. HẰNG SỐ NGHIỆP VỤ BÁO CÁO ẨN DANH (UC-07, UC-08)
    // =========================================================================
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
    
    // ================= DELIVERY STATUSES (CK_Order_Status) =================
    public static final String STATUS_WAITING_DISPATCH = "Waiting for dispatch";
    public static final String STATUS_WAITING_DELIVERY = "Waiting for delivery";
    public static final String STATUS_PICKED_UP = "Picked up";
    public static final String STATUS_DELIVERING = "Delivering";
    public static final String STATUS_COMPLETED = "Delivered";
    public static final String STATUS_FAILED = "Returned";
    public static final String STATUS_PENDING = STATUS_WAITING_DELIVERY;

    // ================= DELIVERY CONFIG & ROLES =================
    public static final int DEFAULT_SHIPPER_MAX_ORDERS = 3;
    public static final String ROLE_SHIPPER = "Shipper";
    public static final String SHIPPER_STATUS_OFFLINE = "Offline";
    public static final String SHIPPER_STATUS_READY = "Ready";
    public static final String SHIPPER_STATUS_DELIVERING = "Delivering";
    public static final String SHIPPER_WORK_READY = "READY";
    public static final String SHIPPER_WORK_DELIVERING = "DELIVERING";

    // ================= DELIVERY STATUS HISTORY NOTES =================
    public static final String NOTE_DISPATCH_ASSIGN = "Dieu phoi gan shipper";
    public static final String NOTE_SHIPPER_CLAIM = "Shipper chu dong tiep nhan don";
    public static final String NOTE_SHIPPER_ACCEPT = "Shipper xac nhan nhan don";
    public static final String NOTE_SHIPPER_REJECT = "Shipper tu choi nhan don";

    // ================= DELIVERY DATABASE ERROR CODES =================
    public static final String ERROR_DATABASE_DELIVERY = "DE0040";
    public static final String ERROR_DATABASE_INSERT_HISTORY = "DE0041";
    
    // ================= DELIVERY ERROR & VALIDATION CODES (Dải E060X) =================
    public static final String ERROR_INVALID_TASK = "E0601";
    public static final String ERROR_UNAUTHORIZED_SHIPPER = "E0602";
    public static final String ERROR_INVALID_STATUS_FLOW = "E0603";
    public static final String ERROR_TASK_ALREADY_ASSIGNED = "E0604";
    public static final String ERROR_CANNOT_REJECT = "E0605";
    public static final String ERROR_SHIPPER_FULL = "E0606";
    public static final String ERROR_EMPTY_FAIL_REASON = "E0607";
}
