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
    public static final int MIN_USER_LENG = 3;
    public static final int MAX_USER_LENG = 20;

    // =========================================================================
    // 2. KHÓA PHIÊN & THUỘC TÍNH DÙNG CHUNG (SESSION & ATTRIBUTES)
    // =========================================================================
    public static final String USER_SESSION = "USER_SESSION";
    public static final String USER_PROFILE = "USER_PROFILE";
    public static final String ERROR_MESSAGE = "ERROR_MESSAGE";
    public static final String SUCCESS_MESSAGE = "SUCCESS_MESSAGE";
    public static final String ACTIVE_TAB = "ACTIVE_TAB";
    public static final String CURRENT_CART = "CURRENT_CART";
    public static final String CURRENT_TABLE = "CURRENT_TABLE";
    public static final String LAST_ORDER_ID = "LAST_ORDER_ID";
    public static final String PLACED_ORDER_IDS = "PLACED_ORDER_IDS";
    public static final String LANG = "LANG";
    public static final String LANG_VI = "vi";
    public static final String LANG_EN = "en";
    public static final String LANG_JA = "ja";
    public static final String LAST_TICKET_ID = "LAST_TICKET_ID";

    // =========================================================================
    // 3. DẢI MÃ THÀNH CÔNG (SUCCESS CODES - SXXX)
    // =========================================================================
    // Dải S001X: Bảo mật tài khoản & Đăng ký
    public static final String SUCCESS_PSW_CHANGE = "S0010";
    public static final String SUCCESS_REGISTER = "S0011";
    public static final String SUCCESS_GEN_KEY = "S0012";

    // Dải S010X: Hồ sơ cá nhân (Profile)
    public static final String SUCCESS_UPDATE_PROFILE = "S0100";
    public static final String SUCCESS_UPDATE_AVATAR = "S0103";
    public static final String SUCCESS_DELETE_AVATAR = "S0104";

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
    public static final String SUCCESS_CREATE_MENU_CATEGORY = "S0213";
    public static final String SUCCESS_DELETE_MENU_CATEGORY = "S0214";
    // Dải S023X: Tuỳ chọn món (size, topping) - quản lý thực đơn
    public static final String SUCCESS_CREATE_MENU_SIZE = "S0230";
    public static final String SUCCESS_UPDATE_MENU_SIZE = "S0231";
    public static final String SUCCESS_TOGGLE_MENU_SIZE = "S0232";
    public static final String SUCCESS_CREATE_MENU_TOPPING = "S0233";
    public static final String SUCCESS_UPDATE_MENU_TOPPING = "S0234";
    public static final String SUCCESS_TOGGLE_MENU_TOPPING = "S0235";

    // Dải S070X: Quản lý Nhân sự & Chấm công (Time Attendance & HR - Task 10)
    public static final String SUCCESS_CREATE_SHIFT = "S0701";
    public static final String SUCCESS_UPDATE_SHIFT = "S0702";
    public static final String SUCCESS_ASSIGN_SHIFT = "S0703";
    public static final String SUCCESS_REMOVE_ASSIGNMENT = "S0704";
    public static final String SUCCESS_APPROVE_ATTENDANCE = "S0705";
    public static final String SUCCESS_REJECT_ATTENDANCE = "S0706";
    public static final String SUCCESS_BULK_ASSIGN_SHIFT = "S0707";
    public static final String SUCCESS_ATTENDANCE_CHECK_IN = "S0708";
    public static final String SUCCESS_ATTENDANCE_CHECK_OUT = "S0709";

    // Dải S080X: Hệ thống Thông báo & Phát thanh (Task 11)
    public static final String SUCCESS_MARK_NOTIFICATION_READ = "S0801";
    public static final String SUCCESS_MARK_ALL_NOTIFICATIONS_READ = "S0802";
    public static final String SUCCESS_BROADCAST_MESSAGE = "S0803";
    // Dải S030X: Báo cáo ẩn danh (UC-07, UC-08)
    public static final String SUCCESS_SUBMIT_REPORT = "S0300";
    public static final String SUCCESS_UPDATE_REPORT = "S0301";

    // Dải S040X: Quản lý Kho nguyên liệu (Inventory)
    public static final String SUCCESS_UPDATE_THRESHOLD = "S0401";
    public static final String SUCCESS_CREATE_INGREDIENT = "S0402";
    public static final String SUCCESS_UPDATE_INGREDIENT = "S0403";
    public static final String SUCCESS_DELETE_INGREDIENT = "S0404";
    public static final String SUCCESS_CREATE_INGREDIENT_CATEGORY = "S0405";
    public static final String SUCCESS_DELETE_INGREDIENT_CATEGORY = "S0406";

    // Dải S050X: Cảnh báo Tồn kho & Hết hạn (Alerts)
    public static final String SUCCESS_CONFIRM_ALERT = "S0501";
    public static final String SUCCESS_CLOSE_ALERT = "S0502";

    // Dải S9XXX: Nghiệp vụ Quản trị hệ thống (Admin)
    public static final String SUCCESS_ACCOUNT_STATE_CHANGE = "S9000";
    public static final String SUCCESS_ADMIN_RESET_PSW = "S9001";
    public static final String SUCCESS_ADMIN_CHANGE_ROLE = "S9002";
    public static final String SUCCESS_REVOKE_KEY = "S9003";

    // Dải S060X: Điều phối & Giao hàng (Dispatch & Shipper - UC-09 & UC-10)
    public static final String SUCCESS_ASSIGN_SHIPPER = "S0601";
    public static final String SUCCESS_CLAIM_TASK = "S0602";
    public static final String SUCCESS_ACCEPT_TASK = "S0603";
    public static final String SUCCESS_REJECT_TASK = "S0604";
    public static final String SUCCESS_COMPLETE_DELIVERY = "S0605";
    public static final String SUCCESS_FAIL_DELIVERY = "S0606";
    public static final String SUCCESS_UPDATE_DELIVERY_STATUS = "S0607";
    public static final String SUCCESS_REVOKE_SHIPPER_TASK = "S0608";

    // =========================================================================
    // 4. DẢI MÃ LỖI NGHIỆP VỤ & XÁC THỰC (EXCEPTION CODES - EXXX)
    // =========================================================================
    // Dải E000X: Xác thực & Đăng nhập (Login)
    public static final String ERROR_SYSTEM = "E0000";
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
    public static final String ERROR_EMPTY_AVATAR = "E0105";

    // Dải E100X: Validation định dạng dữ liệu thô (Tầng 1 - Utility)
    public static final String ERROR_USER_LENG = "E1000";
    public static final String ERROR_INVALID_PSW = "E1001";
    public static final String ERROR_PSW_WHITESPACE = "E0111";
    public static final String ERROR_INVALID_USERNAME_CHARS = "E0112";
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
    public static final String ERROR_MENU_CATEGORY_EXISTS = "E0219";
    public static final String ERROR_MENU_CATEGORY_IN_USE = "E0220";
    public static final String ERROR_DELIVERY_CONTROLLED_BY_SHIPPER = "E0224";
    public static final String ERROR_ORDER_CLOSED_STATUS = "E0225";
    // Dải E023X: Tuỳ chọn món khi đặt (size, đá, đường, topping, ghi chú) và quản lý size/topping
    public static final String ERROR_INVALID_MENU_SIZE = "E0230";
    public static final String ERROR_INVALID_ICE_LEVEL = "E0231";
    public static final String ERROR_INVALID_SUGAR_LEVEL = "E0232";
    public static final String ERROR_INVALID_MENU_TOPPING = "E0233";
    public static final String ERROR_ITEM_NOTE_TOO_LONG = "E0234";
    public static final String ERROR_INVALID_ITEM_QUANTITY = "E0235";
    public static final String ERROR_MENU_OPTION_NAME = "E0236";
    public static final String ERROR_MENU_OPTION_EXISTS = "E0237";
    public static final String ERROR_MENU_OPTION_PRICE = "E0238";
    public static final String ERROR_MENU_OPTION_FORBIDDEN = "E0239";
    public static final String ERROR_PRODUCT_DESCRIPTION_TOO_LONG = "E0240";
    public static final String ERROR_PRODUCT_VIDEO_URL = "E0241";

    // Dải E070X: Quản lý Nhân sự & Chấm công (HR & Shift Management - Task 10)
    public static final String ERROR_SHIFT_NAME_EMPTY = "E0701";
    public static final String ERROR_SHIFT_TIME_INVALID = "E0702";
    public static final String ERROR_SHIFT_EXISTS = "E0703";
    public static final String ERROR_ASSIGNMENT_EXISTS = "E0704";
    public static final String ERROR_CANNOT_DELETE_ASSIGNMENT = "E0705";
    public static final String ERROR_CANNOT_DELETE_SHIFT = "E0706";
    public static final String ERROR_ATTENDANCE_NOT_FOUND = "E0707";
    public static final String ERROR_INVALID_DATE_FORMAT = "E0708";
    public static final String ERROR_USER_NOT_FOUND = "E0709";
    public static final String ERROR_ATTENDANCE_TOO_EARLY = "E0710";
    public static final String ERROR_ATTENDANCE_TOO_EARLY_CHECKOUT = "E0711";
    public static final String ERROR_INVALID_DATE_RANGE = "E0712";
    public static final String ERROR_EMPTY_DAYS_OF_WEEK = "E0713";
    public static final String ERROR_NO_ASSIGNMENT_CREATED = "E0714";
    public static final String ERROR_CANNOT_ASSIGN_NON_STAFF = "E0715";
    public static final String ERROR_ATTENDANCE_NO_SHIFT = "E0716";
    public static final String ERROR_ATTENDANCE_ALREADY_CHECKED_IN = "E0717";
    public static final String ERROR_ATTENDANCE_NOT_CHECKED_IN = "E0718";
    public static final String ERROR_ATTENDANCE_ALREADY_CHECKED_OUT = "E0719";
    public static final String ERROR_ATTENDANCE_PENDING_ORDERS = "E0720";
    public static final String ERROR_ATTENDANCE_EMPTY_OT_REASON = "E0721";

    // Dải E080X: Hệ thống Thông báo & Phát thanh (Task 11)
    public static final String ERROR_EMPTY_BROADCAST_MESSAGE = "E0801";
    public static final String ERROR_NOTIFICATION_NOT_FOUND = "E0802";
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
    public static final String ERROR_EMPTY_INGREDIENT_CATEGORY = "E0409";
    public static final String ERROR_INGREDIENT_CATEGORY_EXISTS = "E0410";
    public static final String ERROR_INGREDIENT_CATEGORY_IN_USE = "E0411";

    // Dải E050X: Cảnh báo Tồn kho & Hết hạn (Alerts)
    public static final String ERROR_ALERT_NOT_FOUND = "E0501";

    // Dải E9XXX: Cấu hình & Nghiệp vụ Quản trị (Admin / System)
    public static final String ERROR_INVALID_SETTING_TAB = "E9006";
    public static final String ERROR_INVALID_ACTION = "E9000";
    public static final String ERROR_ADMIN_CANNOT_RESET_SELF_PSW = "E9001";
    public static final String ERROR_ADMIN_CANNOT_CHANGE_SELF_ROLE = "E9002";
    public static final String ERROR_INVALID_ROLE_SELECTION = "E9003";
    public static final String ERROR_KEY_CANNOT_REVOKE = "E9004";
    public static final String ERROR_SHIPPER_HAS_ACTIVE_ORDERS = "E9005";

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
    public static final String ERROR_DATABASE_GET_ATTENDANCE = "DE0056";
    public static final String ERROR_DATABASE_INSERT_ATTENDANCE = "DE0057";
    public static final String ERROR_DATABASE_UPDATE_ATTENDANCE = "DE0058";

    // Nhóm Quản lý Nhân sự & Ca làm việc (HR & Shift Management DAO)
    public static final String ERROR_DATABASE_INSERT_SHIFT = "DE0051";
    public static final String ERROR_DATABASE_UPDATE_SHIFT = "DE0052";
    public static final String ERROR_DATABASE_ASSIGN_SHIFT = "DE0053";
    public static final String ERROR_DATABASE_DELETE_ASSIGNMENT = "DE0054";
    public static final String ERROR_DATABASE_APPROVE_ATTENDANCE = "DE0055";

    // Nhóm Thông báo & Phát thanh (Notification DAO - Task 11)
    public static final String ERROR_DATABASE_GET_NOTIFICATION = "DE0061";
    public static final String ERROR_DATABASE_UPDATE_NOTIFICATION = "DE0062";
    public static final String ERROR_DATABASE_MENU_OPTION = "DE0070";
    public static final String ERROR_DATABASE_BEST_SELLER = "DE0071";

    // =========================================================================
    // TUỲ CHỌN MÓN KHI ĐẶT (kiểu menu online)
    // Mã mức đá / đường được lưu vào OrderDetails; chữ hiển thị nằm trong properties
    // (option.ice.ICE_100, option.sugar.SUGAR_70 ...)
    // =========================================================================
    public static final String[] ICE_LEVELS = {"ICE_100", "ICE_50", "ICE_0"};
    public static final String[] SUGAR_LEVELS = {"SUGAR_100", "SUGAR_70", "SUGAR_0"};
    public static final String DEFAULT_ICE_LEVEL = "ICE_100";
    public static final String DEFAULT_SUGAR_LEVEL = "SUGAR_100";
    public static final int MAX_ITEM_NOTE_LENG = 200;
    public static final int MAX_ITEM_QUANTITY = 99;
    public static final int MAX_PRODUCT_DESCRIPTION_LENG = 500;
    public static final int MAX_MENU_OPTION_NAME_LENG = 50;
    public static final int BEST_SELLER_LIMIT = 6;
    public static final String TOPPING_SEPARATOR = ", ";
    public static final String OPEN_CHECKOUT = "OPEN_CHECKOUT";
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
    
    // ================= ORDER & DELIVERY STATUSES (CK_Order_Status) =================
    public static final String STATUS_ACCEPTED = "Accepted";
    public static final String STATUS_PAID = "Paid";
    public static final String STATUS_PREPARED = "Prepared";
    public static final String STATUS_CANCELLED = "Cancelled";
    public static final String STATUS_WAITING_DISPATCH = "Waiting for dispatch";
    public static final String STATUS_WAITING_DELIVERY = "Waiting for delivery";
    public static final String STATUS_PICKED_UP = "Picked up";
    public static final String STATUS_DELIVERING = "Delivering";
    public static final String STATUS_COMPLETED = "Delivered";
    public static final String STATUS_FAILED = "Returned";
    public static final String STATUS_PENDING = STATUS_WAITING_DELIVERY;

    // ================= DELIVERY CONFIG & ROLES =================
    public static final int DEFAULT_SHIPPER_MAX_ORDERS = 3;
    public static final int MAX_SHIPPER_ORDERS = 5;
    public static final String ROLE_ADMIN = "Admin";
    public static final String ROLE_MANAGER = "Manager";
    public static final String ROLE_STAFF = "Staff";
    public static final String ROLE_SHIPPER = "Shipper";

    // ================= ORDER TYPES =================
    public static final String ORDER_TYPE_DELIVERY = "Delivery";
    public static final String ORDER_TYPE_COUNTER = "Counter";
    public static final String ORDER_TYPE_TABLE = "Table";
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
    public static final String NOTE_STAFF_CANCEL_ORDER = "Don hang bi huy boi nhan vien";
    public static final String NOTE_REVERT_DELIVERY_ORDER = "Thu hoi don giao hang ve phuc vu tai cho";
    public static final String NOTE_DISPATCH_REVOKE = "Dieu phoi thu hoi don do shipper khong tiep nhan";

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

    // ================= HR & SHIFT MANAGEMENT =================
    public static final String ATTENDANCE_STATUS_ON_TIME = "On Time";
    public static final String ATTENDANCE_STATUS_LATE = "Late";
    public static final String ATTENDANCE_STATUS_PENDING_EARLY = "Pending Early Approval";
    public static final String ATTENDANCE_STATUS_LEFT_EARLY = "Left Early";
    public static final String ATTENDANCE_STATUS_PENDING_OT = "Pending OT Approval";
    public static final String ATTENDANCE_STATUS_APPROVED = "Approved";
    public static final String ATTENDANCE_STATUS_REJECTED = "Rejected";

    // ================= ATTENDANCE THRESHOLDS =================
    public static final int ATTENDANCE_THRESHOLD_EARLY_CHECKIN_MINUTES = 15;
    public static final int ATTENDANCE_THRESHOLD_LATE_CHECKIN_MINUTES = 15;
    public static final int ATTENDANCE_THRESHOLD_EARLY_CHECKOUT_MINUTES = 15;
    public static final int ATTENDANCE_THRESHOLD_OVERTIME_MINUTES = 15;
    public static final int EARLY_CHECKIN_THRESHOLD_MINUTES = ATTENDANCE_THRESHOLD_EARLY_CHECKIN_MINUTES;
    public static final int LATE_CHECKIN_THRESHOLD_MINUTES = ATTENDANCE_THRESHOLD_LATE_CHECKIN_MINUTES;
    public static final int EARLY_CHECKOUT_THRESHOLD_MINUTES = ATTENDANCE_THRESHOLD_EARLY_CHECKOUT_MINUTES;
    public static final int OVERTIME_THRESHOLD_MINUTES = ATTENDANCE_THRESHOLD_OVERTIME_MINUTES;

    // ================= SHIFT & ATTENDANCE NOTIFICATION TEMPLATES =================
    public static final String NOTIF_SHIFT_ASSIGN_SINGLE = "B\u1ea1n v\u1eeba \u0111\u01b0\u1ee3c ph\u00e2n c\u00f4ng ca: %s (%s)";
    public static final String NOTIF_SHIFT_ASSIGN_BULK = "B\u1ea1n v\u1eeba \u0111\u01b0\u1ee3c ph\u00e2n c\u00f4ng %d ca l\u00e0m vi\u1ec7c (%s) t\u1eeb %s \u0111\u1ebfn %s.";
    public static final String NOTIF_ATTENDANCE_APPROVED = "Qu\u1ea3n l\u00fd \u0111\u00e3 ph\u00ea duy\u1ec7t y\u00eau c\u1ea7u %s c\u1ee7a b\u1ea1n.";
    public static final String NOTIF_ATTENDANCE_REJECTED = "Qu\u1ea3n l\u00fd \u0111\u00e3 t\u1eeb ch\u1ed1i y\u00eau c\u1ea7u %s c\u1ee7a b\u1ea1n.";
    public static final String LABEL_OT = "t\u0103ng ca (OT)";
    public static final String LABEL_EARLY_CHECKIN = "v\u00e0o ca s\u1edbm";
    public static final String[] WEEK_DAY_NAMES = {
        "Th\u1ee9 2", "Th\u1ee9 3", "Th\u1ee9 4", "Th\u1ee9 5", "Th\u1ee9 6", "Th\u1ee9 7", "Ch\u1ee7 Nh\u1eadt"
    };

    // ================= INVENTORY & ORDER FALLBACKS & ALERTS =================
    public static final String FALLBACK_CATEGORY_OTHER = "Kh\u00e1c";
    public static final String FALLBACK_INGREDIENT_PREFIX = "Nguy\u00ean li\u1ec7u #";
    public static final String FALLBACK_PRODUCT_PREFIX = "M\u00f3n #";
    public static final String INVENTORY_ALERT_OUT_OF_STOCK = "[\u0110\u00e3 h\u1ebft h\u00e0ng] Nguy\u00ean li\u1ec7u %s \u0111\u00e3 h\u1ebft h\u00e0ng trong kho (S\u1ed1 l\u01b0\u1ee3ng: 0 %s)!";
    public static final String INVENTORY_ALERT_LOW_STOCK = "[S\u1eafp h\u1ebft h\u00e0ng] Nguy\u00ean li\u1ec7u %s s\u1eafp h\u1ebft (C\u00f2n %s %s, d\u01b0\u1edbi ng\u01b0\u1ee1ng %s %s)!";
    public static final String INVENTORY_ALERT_EXPIRED = "[\u0110\u00e3 h\u1ebft h\u1ea1n] Nguy\u00ean li\u1ec7u %s c\u00f3 l\u00f4 h\u00e0ng \u0111\u00e3 qu\u00e1 h\u1ea1n s\u1eed d\u1ee5ng!";
    public static final String INVENTORY_ALERT_EXPIRING_SOON = "[S\u1eafp h\u1ebft h\u1ea1n] Nguy\u00ean li\u1ec7u %s c\u00f3 l\u00f4 h\u00e0ng s\u1eafp h\u1ebft h\u1ea1n trong v\u00f2ng 3 ng\u00e0y t\u1edbi!";
    public static final String INVENTORY_ALERT_GENERAL = "[C\u1ea3nh b\u00e1o kho] Nguy\u00ean li\u1ec7u %s c\u1ea7n ki\u1ec3m tra (%s)!";
}
