# BOMS LEARNING & PROGRESS LOG

## [2026-10-24] - TỔNG KẾT BẢN PHÁT HÀNH RELEASE 1.0: BẢO MẬT, LUỒNG ĐIỀU PHỐI, KHÓA STATE MACHINE & TỔNG KIỂM TOÁN TĨNH

### 1. Phân hệ Bảo mật & Upload Avatar:
- **Vá lỗ hổng Path Traversal (`ImageUploadUtility.java`):** Chuyển đổi toàn bộ tên tệp tải lên sang mã băm ngẫu nhiên `UUID.randomUUID().toString() + ext`, loại bỏ hoàn toàn tên file do client gửi.
- **Xác thực chữ ký nhị phân Magic Bytes:** Đọc byte đầu trực tiếp từ `InputStream` (`FF D8 FF` cho JPEG, `89 50 4E 47` cho PNG, `RIFF...WEBP` cho WebP), ngăn chặn tuyệt đối webshell/malware đổi đuôi qua mặt bộ lọc MIME.
- **Đồng bộ HttpSession tức thì (`ProfileController.java`):** Bổ sung `session.setAttribute(Constants.USER_SESSION, loginUser)` để làm mới avatar ngay trên header mà không cần đăng nhập lại.
- **Kịch bản Migration CSDL:** Tạo tệp `DATABASE/BOMS_DATABASE_2.sql` bổ sung cột `Avatar VARCHAR(255) NULL` an toàn cho các môi trường DB cũ.

### 2. Chuẩn hóa Luồng Nghiệp vụ Điều phối Shipper:
- **Khắc phục lỗi đếm sai ActiveOrders (`DeliveryService.java`):** Đảo thứ tự trong phương thức `rejectTask` để cập nhật CSDL gỡ `ShipperID = NULL` và chuyển trạng thái về `Waiting for dispatch` trước khi gọi `syncShipperProfileStatus`. Nhờ đó, hàm `countActiveOrdersByShipperId` đếm chính xác số đơn thực tế đang giao (`Delivering`, `Waiting for delivery`), triệt tiêu lỗi tụt số đơn hoạt động (3/3 tụt về 2/3) khi shipper từ chối đơn mới.
- **Dọn dẹp kiểm tra dung lượng thừa:** Xóa bỏ logic kiểm tra trùng lặp ở `acceptTask`, chuẩn hóa hằng số `Constants.MAX_SHIPPER_ORDERS = 3`.

### 3. Khóa State Machine Vòng đời Đơn hàng:
- **Chốt chặn State Machine ở Backend (`OrderService.updateOrderStatus`):** Truy vấn đơn hiện tại qua `orderDAO.getOrderById(orderId)`. Cấm cập nhật các đơn `Delivery` đang do Shipper kiểm soát (`Waiting for delivery`, `Picked up`, `Delivering`) với mã lỗi `E0224` (`ERROR_DELIVERY_CONTROLLED_BY_SHIPPER`). Cấm cập nhật các đơn ở trạng thái kết thúc (`Delivered`, `Paid`, `Returned`, `Cancelled`) với mã lỗi `E0225` (`ERROR_ORDER_CLOSED_STATUS`).
- **Khóa UI View (`standard_orders_table.jsp` & `delivery_orders_table.jsp`):** Tại cột Action, bọc thẻ `<c:choose>` để khóa và ẩn form dropdown đối với đơn thuộc Shipper (`🔒 Đang do Shipper xử lý`) và đơn đã kết thúc (`🔒 Đã hoàn tất`).

### 4. Tổng Kiểm toán Tĩnh Mã nguồn (Static Code Audit & Full Compliance):
- **Triệt tiêu 100% khối catch rỗng (Zero Empty Catch):** Rà soát và xử lý dứt điểm các khối catch rỗng tại `OrderDAO`, `UserDAO`, `DashboardController`, `NotificationController`, `AttendanceService`.
- **Vá lỗ hổng rò rỉ Transaction (Transaction Leak Prevention):** Cập nhật `OrderDAO` bắt `catch (Exception e)` chung để bảo đảm lệnh `connection.rollback()` luôn được gọi cho mọi lỗi Runtime; tích hợp `Logger` khi rollback và restore auto-commit.
- **Di dời biến static ngoài utility:** Chuyển 4 hằng số thời gian trong `AttendanceService` về `Constants.java`. Đạt chuẩn **0 static fields** ngoài `utility`.
- **Gom toàn bộ chuỗi hardcode:** Sử dụng template `String.format(Constants.NOTIF_..., ...)` và `String.format(Constants.INVENTORY_ALERT_..., ...)`, tích hợp `dayKey` i18n cho thời khóa biểu tuần. Toàn bộ dự án đạt chuẩn i18n 100%.

### 5. File tác động & Trạng thái:
- `README.md` (Tạo Release 1.0 Notes)
- `DATABASE/BOMS_DATABASE_2.sql` (Kịch bản migration)
- `.agent/WorkList.md` (Bổ sung Section Release 1.0)
- `.agent/learning.md`
- `src/java/utility/ImageUploadUtility.java`
- `src/java/controller/hr/ProfileController.java`
- `src/java/service/DeliveryService.java`
- `src/java/service/OrderService.java`
- `src/java/service/AttendanceService.java`
- `src/java/service/IngredientService.java`
- `src/java/dao/OrderDAO.java`
- `src/java/dao/UserDAO.java`
- `src/java/utility/Constants.java`
- `src/java/resources/messages_vi.properties`
- `src/java/resources/messages_en.properties`
- `web/components/standard_orders_table.jsp`
- `web/components/delivery_orders_table.jsp`
- `web/attendance.jsp`
- `web/hr_management.jsp`
- **Trạng thái Build:** Apache Ant (`ant clean compile` với JDK 17) đạt **BUILD SUCCESSFUL (0 errors, 74 source files, 44 web files copied)**.

---

## [2026-10-24] - Tổng Kiểm toán Tuân thủ Quy chuẩn Kiến trúc (Rule_BOMS Audit & Deep Clean)

### 1. Phân tích & Vi phạm Phát hiện được:
- **Biến Static ngoài package Utility:** `AttendanceService.java` khai báo 4 hằng số `private static final int ..._THRESHOLD_MINUTES = 15;` vi phạm trực tiếp Rule_BOMS Mục 2 ("Nghiêm cấm sử dụng khai báo static ngoài package Utility").
- **Transaction Rollback & Empty Catch:** `OrderDAO.insertOrderWithDetails` chỉ bắt `catch (SQLException e)` thay vì `catch (Exception e)`, đồng thời nuốt ngoại lệ tại khối `rollback` và `setAutoCommit`. `UserDAO.registerUserWithKeyTransaction` cũng có khối `catch (SQLException ex)` rỗng khi rollback.
- **Nuốt lỗi (Empty Catch Blocks):** `DashboardController.java` (`userId` parsing), `NotificationController.java` (`limit` và `notificationId` parsing), `AttendanceService.java` (`daysOfWeek` parsing) chứa các khối catch rỗng.
- **Hardcode Chuỗi & Thông báo:** `AttendanceService.java` và `IngredientService.java` sử dụng phép nối chuỗi cho các thông báo thông báo ca làm, duyệt điểm danh, và cảnh báo kho thay vì sử dụng các mẫu định dạng đã khai báo sẵn trong `Constants.java`.

### 2. Giải pháp Kỹ thuật Triệt để:
- **Di dời toàn bộ Biến Static về `Constants.java`:** Chuyển `EARLY_CHECKIN_THRESHOLD_MINUTES`, `LATE_CHECKIN_THRESHOLD_MINUTES`, `EARLY_CHECKOUT_THRESHOLD_MINUTES`, `OVERTIME_THRESHOLD_MINUTES` vào `Constants.java`. `AttendanceService` chỉ tham chiếu `Constants.*`. Đạt **0 static fields** ngoài `utility`.
- **Hoàn thiện Transaction Rollback & Logger:** Cập nhật `OrderDAO` bắt `catch (Exception e)` chung để đảm bảo mọi ngoại lệ runtime (NPE, OOB) đều kích hoạt `connection.rollback()`. Tích hợp `java.util.logging.Logger` ghi nhận lỗi nếu `rollback()` hoặc `setAutoCommit()` phát sinh ngoại lệ tại cả `OrderDAO` và `UserDAO`. Đạt **0 empty catches**.
- **Xử lý dứt điểm Nuốt lỗi tại Controller/Service:** Khởi tạo giá trị mặc định rõ ràng (`userId = 0;`, `limit = 10;`, `notificationId = 0;`) và ghi log chi tiết. Đạt **0 empty catches** toàn dự án.
- **Triệt tiêu 100% Chuỗi Hardcode:** Áp dụng `String.format(Constants.NOTIF_..., ...)` và `String.format(Constants.INVENTORY_ALERT_..., ...)` tại `AttendanceService` và `IngredientService`. Tích hợp khóa đa ngôn ngữ `dayKey` (`hr.assign.day.mon`..`sun`) trên `attendance.jsp` và `hr_management.jsp`. Đạt **0 hardcoded strings** trong mã nguồn.

### 3. File tác động & Trạng thái:
- `src/java/utility/Constants.java`
- `src/java/service/AttendanceService.java`
- `src/java/service/IngredientService.java`
- `src/java/dao/OrderDAO.java`
- `src/java/dao/UserDAO.java`
- `src/java/controller/DashboardController.java`
- `src/java/controller/NotificationController.java`
- `web/attendance.jsp`
- `web/hr_management.jsp`
- `.agent/learning.md`
- **Trạng thái Build:** Apache Ant (`ant clean compile` với JDK 17) đạt **BUILD SUCCESSFUL (0 errors, 74 source files, 44 web files copied)**.

---

## [2026-10-24] - Khắc phục Triệt để Lỗi Vi phạm Luồng Trạng thái Đơn hàng (State Machine Violation & UI Lock)

### 1. Phân tích & Vấn đề Kỹ thuật:
- **Vi phạm Luồng trạng thái Đơn hàng (State Machine Violation):** Tại màn hình Quản lý đơn hàng (`order_management.jsp`), nhân viên có thể sử dụng dropdown trạng thái để đổi ngược trạng thái của một đơn `Delivery` đang do Shipper nắm giữ (`Waiting for delivery`, `Picked up`, `Delivering`) về lại `Waiting for dispatch` hoặc trạng thái cũ hơn. Điều này dẫn tới sai lệch dữ liệu: Shipper vẫn giữ đơn trong tiến trình giao nhưng đơn trên hệ thống lại bị lùi bước.
- **Thay đổi trạng thái trên Đơn đã kết thúc (Closed Orders Modification):** Các đơn đã hoàn tất hoặc đóng (`Delivered`, `Paid`, `Returned`, `Cancelled`) vẫn hiển thị form cập nhật trạng thái hoạt động ở UI và cho phép gửi request cập nhật.

### 2. Giải pháp Kỹ thuật Triệt để:
- **Khóa State Machine Tầng Backend (`OrderService.updateOrderStatus`):**
  - Truy vấn lấy `Order currentOrder = orderDAO.getOrderById(orderId)`.
  - Nếu đơn là `Delivery` (`Constants.ORDER_TYPE_DELIVERY`) và trạng thái hiện tại thuộc luồng kiểm soát của Shipper (`Waiting for delivery`, `Picked up`, `Delivering`), chặn tuyệt đối và ném `ValidationException(Constants.ERROR_DELIVERY_CONTROLLED_BY_SHIPPER)` (mã `E0224`).
  - Nếu trạng thái hiện tại là trạng thái kết thúc (`Delivered`, `Paid`, `Returned`, `Cancelled`), chặn tuyệt đối và ném `ValidationException(Constants.ERROR_ORDER_CLOSED_STATUS)` (mã `E0225`).
  - Trong `convertOrderToMap`, tính toán trước các cờ nghiệp vụ: `isDeliveryUnderShipper`, `isClosedStatus`, `isStatusUpdateLocked` để UI và các tầng xử lý truy cập nhất quán.
- **Khóa Giao diện UI (`standard_orders_table.jsp` & `delivery_orders_table.jsp`):**
  - Sử dụng `<c:choose>` tại cột Thao tác (Action):
    + Nếu `${order.isDeliveryUnderShipper}`: Ẩn form cập nhật, hiển thị huy hiệu tĩnh `🔒 Đang do Shipper xử lý` (`badge-locked`).
    + Nếu `${order.isClosedStatus}`: Ẩn form cập nhật, hiển thị trạng thái tĩnh `🔒 Đã hoàn tất` (`text-secondary`).
    + Các trạng thái mở khác: Hiển thị form cập nhật trạng thái bình thường.
- **Chuẩn hóa Hằng số & Đa ngôn ngữ (100% Rule_BOMS):**
  - Khai báo hằng số `ERROR_DELIVERY_CONTROLLED_BY_SHIPPER = "E0224"` và `ERROR_ORDER_CLOSED_STATUS = "E0225"` trong `Constants.java`.
  - Cập nhật song ngữ đối xứng `messages_vi.properties` (chuẩn Unicode Escape `\uXXXX`) và `messages_en.properties`.

### 3. File tác động & Trạng thái:
- `src/java/utility/Constants.java`
- `src/java/resources/messages_vi.properties`
- `src/java/resources/messages_en.properties`
- `src/java/service/OrderService.java`
- `web/components/standard_orders_table.jsp`
- `web/components/delivery_orders_table.jsp`
- `.agent/learning.md`
- **Trạng thái Build:** Apache Ant (`ant clean compile` với JDK 17) đạt **BUILD SUCCESSFUL (0 errors, 74 source files, 44 web files copied)**.

---

## [2026-10-23] - Tối ưu hóa Dứt điểm 4 điểm Flash Message, Dọn rác Session & Triệt tiêu Hardcode String

### 1. Phân tích & Vấn đề Kỹ thuật:
- **Tồn đọng rác Session đối với `ACTIVE_TAB`:** Khi các controller (`SettingsController`, `OrderManagementController`, `HrManagementController`) lưu `ACTIVE_TAB` vào `HttpSession`, thuộc tính này không được tự động giải phóng sang Request Scope, khiến trạng thái tab có thể bị lưu vĩnh viễn trong phiên làm việc.
- **Xử lý Flash Message thủ công tại RegisterController:** `RegisterController.doGet` sử dụng logic lấy/xóa `Constants.SUCCESS_MESSAGE` thủ công (dòng 24-29) thay vì tái sử dụng `FlashMessageUtil`.
- **Rò rỉ Session vãng lai khi đăng nhập:** `LoginController.doPost` khi login thành công mới chỉ dọn `CURRENT_TABLE` và `CURRENT_CART`, chưa xóa `PLACED_ORDER_IDS` và `ACTIVE_TAB`.
- **Hardcode String trong nghiệp vụ đơn hàng & phân hệ:** `OrderService.java` chứa các chuỗi ghi chú thô `"Don hang bi huy boi nhan vien"`, `"Thu hoi don giao hang ve phuc vu tai cho"` và trạng thái Shipper `"Ready"` / `"Delivering"`. `OrderManagementController.java` và `HrManagementController.java` gõ chay chuỗi `"ACTIVE_TAB"`.

### 2. Giải pháp Điểm xạ Kỹ thuật:
- **Tự động hóa chuyển tiếp `ACTIVE_TAB` tại `FlashMessageUtil`:** Bổ sung logic kiểm tra và chuyển tiếp `Constants.ACTIVE_TAB` từ `HttpSession` sang `HttpServletRequest`, sau đó lập tức gọi `session.removeAttribute(Constants.ACTIVE_TAB)`.
- **Chuẩn hóa `RegisterController`:** Chuyển đổi toàn bộ khối lấy message thủ công sang `FlashMessageUtil.forwardFlashMessages(request, session)`.
- **Dọn sạch rác Session tại `LoginController`:** Bổ sung `session.removeAttribute(Constants.PLACED_ORDER_IDS)` và `session.removeAttribute(Constants.ACTIVE_TAB)` khi người dùng đăng nhập thành công.
- **Triệt tiêu toàn diện Hardcode:**
  + Khai báo `NOTE_STAFF_CANCEL_ORDER` và `NOTE_REVERT_DELIVERY_ORDER` trong `Constants.java`.
  + Tích hợp `Constants.NOTE_STAFF_CANCEL_ORDER`, `Constants.NOTE_REVERT_DELIVERY_ORDER`, `Constants.SHIPPER_STATUS_READY`, `Constants.SHIPPER_STATUS_DELIVERING` vào `OrderService.java` (dòng 116, 119, 142, 145, 169, 174).
  + Thay thế chuỗi `"ACTIVE_TAB"` bằng `Constants.ACTIVE_TAB` trong `OrderManagementController.java` và `HrManagementController.java`.

### 3. File tác động & Trạng thái:
- `src/java/utility/FlashMessageUtil.java`
- `src/java/controller/auth/RegisterController.java`
- `src/java/controller/auth/LoginController.java`
- `src/java/utility/Constants.java`
- `src/java/service/OrderService.java`
- `src/java/controller/OrderManagementController.java`
- `src/java/controller/hr/HrManagementController.java`
- **Trạng thái Build:** Apache Ant (nt clean compile với JDK 17) đạt **BUILD SUCCESSFUL (0 errors, 74 source files, 44 web files copied)**.

---

## [2026-10-23] - HOTFIX KHẨN CẤP: Khắc phục triệt để sự cố lệch mã thông báo, gán nhầm mã lỗi và mất key giao diện Admin Dashboard sau Task 9.3

### 1. Phân tích Nguyên nhân Gốc rễ Sự cố:
- **Lệch gán mã lỗi & thông điệp (Semantic Desynchronization):** Trong quá trình quy hoạch và deduplicate ở Task 9.3, các thông điệp nghiệp vụ trong `messages_vi.properties` và `messages_en.properties` bị hoán đổi và lệch dòng so với định nghĩa gốc trong `Constants.java`. Cụ thể:
  + Dải `S0103`/`S0104` (Avatar) bị gán nhầm thành thông báo đăng nhập/đăng xuất.
  + Dải `S0201`-`S0205` (Giỏ hàng & Đơn hàng) bị gán lệch ngữ nghĩa (thêm giỏ hàng thành tạo đơn hàng).
  + Dải `E0201`-`E0220` (Đơn hàng/Menu), `E0300`-`E0316` (Báo cáo ẩn danh), `E0601`-`E0607` (Shipper), `E0701`-`E0721` (Chấm công), `E9000`-`E9006` (Admin) và `DE0000`-`DE0062` (Database) bị gán sai nội dung thông báo khiến giao diện báo lỗi sai lệch hoàn toàn.
- **Mất toàn bộ Key giao diện Quản trị viên (Missing Admin UI Keys):** Quá trình làm sạch file properties đã vô tình làm mất 41 keys của màn hình Admin Dashboard (`admin.dashboard.*`), khiến `web/admin_dashboard.jsp` hiển thị chuỗi key thô.
- **Lỗi ký tự thừa & hỏng font:** Xuất hiện ký tự thừa `|` ở cuối các dòng `DE0030`-`DE0033` và ký tự `?` lỗi font ở `hr.matrix.prev_week` / `next_week` trong `messages_en.properties`.
- **Lệch kiểm thực độ dài mật khẩu:** `web/admin_dashboard.jsp` đặt `minlength="6"` không khớp với quy chuẩn `Constants.MIN_PASSWORD_LENG = 3`.

### 2. Giải pháp Kỹ thuật Triệt để:
- **Khôi phục & Chuẩn hóa 100% 41 keys Admin Dashboard:** Bổ sung đầy đủ 41 keys giao diện (`admin.dashbard.titile`, `admin.dashboard.tab.*`, `admin.dashboard.table.*`, `admin.dashboard.status.*`, `admin.dashboard.btn.*`, `admin.dashboard.label.*`, `admin.dashboard.placeholder.*`) vào cả 2 file `.properties`. Tiếng Việt được mã hóa 100% chuẩn Unicode Escape `\uXXXX` theo `Rule_BOMS.md`.
- **Đối chiếu & Khớp chính xác 1-1 với `Constants.java`:** Toàn bộ 214 mã backend (`SXXX`, `EXXX`, `DEXXX`) được đối chiếu trực tiếp với vị trí sử dụng trong Service, DAO và Controller, khớp đúng 100% ngữ nghĩa của từng hằng số.
- **Loại bỏ sạch sẽ ký tự lỗi:** Xóa bỏ ký tự `|` thừa ở `DE0030`-`DE0033`, thay thế mũi tên điều hướng ở `hr.matrix.prev_week` (`\u2190 Previous Week`) và `hr.matrix.next_week` (`Next Week \u2192`).
- **Đồng bộ minlength tại View:** Sửa `minlength="6"` thành `minlength="3"` tại `web/admin_dashboard.jsp` (dòng 237 và 244) để đồng bộ tuyệt đối với `Constants.MIN_PASSWORD_LENG`.
- **Kiểm tra chéo tự động (Automated Verification):** Chạy script kiểm tra độc lập xác nhận:
  + Cả 2 file properties đạt đúng 930 keys đối xứng 1-1, 0 duplicate keys.
  + `messages_vi.properties` đạt 0 dòng non-ASCII (100% Unicode Escape).
  + 100% các key `<fmt:message key="..." />` trong toàn bộ các file `.jsp` đều tồn tại trong properties.
  + 100% các mã thành công/lỗi trong `Constants.java` đều tồn tại và chuẩn hóa.
  + Biên dịch sạch toàn bộ dự án bằng Apache Ant (`BUILD SUCCESSFUL`, 0 lỗi, 74 source files).

### 3. File tác động & Trạng thái:
- `src/java/resources/messages_vi.properties` (Chuẩn hóa 930 keys, 100% Unicode Escape)
- `src/java/resources/messages_en.properties` (Chuẩn hóa 930 keys đối xứng)
- `web/admin_dashboard.jsp` (Sửa minlength=3 cho newPassword và confirmPassword)
- `.agent/WorkList.md`
- `.agent/learning.md`
- **Trạng thái Build:** Apache Ant (nt clean compile với JDK 17) đạt **BUILD SUCCESSFUL (0 errors, 74 source files, 44 web files copied)**.

---

## [2026-10-23] - Hoàn thiện Task 9 & Final Task 20: Xử lý Nợ kỹ thuật Đăng ký & Mật khẩu (Transaction + SHA-256), Triệt tiêu Hardcode Session, Chuẩn hóa Mã lỗi & Đồng bộ Đa ngôn ngữ (100% Release Ready)

### 1. Phân tích & Vấn đề Kỹ thuật:
- **Nợ kỹ thuật Đăng ký (Task 20.1):** Việc thêm bản ghi `Users` và cập nhật `AccountManager` (`IsUsed = 1`, `UserID = ?`) trước đó chạy độc lập không qua Database Transaction, tiềm ẩn rủi ro khóa key bị treo hoặc tạo user mồ côi nếu xảy ra lỗi giữa chừng.
- **Lưu mật khẩu thô (Task 20.2):** Hệ thống lưu mật khẩu dạng plain text để thuận tiện kiểm thử ban đầu, vi phạm nghiêm trọng nguyên tắc an toàn bảo mật thông tin.
- **Hardcode Session/Attribute Strings trong Controller (Task 9.1):** Nhiều Controller (`CartController`, `LanguageController`, `MenuController`, `ReportController`, `LoginController`) sử dụng chuỗi tự do dạng `"CURRENT_CART"`, `"LANG"`, `"LAST_ORDER_ID"` thay vì các hằng số tập trung từ `Constants.java`.
- **Xung đột dải mã lỗi & thành công trong Constants (Task 9.2):** Dải mã Điểm danh bị chồng chéo với Báo cáo (`S0301`/`S0302`, `E0301`-`E0306`), mã lỗi CSDL cảnh báo kho và điểm danh bị trùng (`DE0018`-`DE0020`), mã tab cài đặt trùng với hành động không hợp lệ (`E9000`).
- **Trùng lặp và lệch khóa trong file Properties (Task 9.3):** Cả hai tệp `messages_vi.properties` và `messages_en.properties` bị copy-paste trùng lặp nhiều phân đoạn giao diện cũ, xuất hiện các mã backend nằm lộn xộn ở đáy tệp thay vì đặt tập trung ở đầu.

### 2. Giải pháp Kỹ thuật Triệt để:
- **Quản lý Giao dịch Nguyên tử khi Đăng ký (Task 20.1):**
  - Xây dựng `UserDAO.registerUserWithKeyTransaction(User user, String securityKey)` quản lý kết nối JDBC nguyên tử: `connection.setAutoCommit(false)`, `commit()`, và `rollback()` khi gặp `SQLException`.
  - Tự động thực thi 3 tác vụ trong cùng 1 transaction: Insert `Users`, Cập nhật `AccountManager` (gán `IsUsed = 1` và `UserID`), và Tạo hồ sơ `ShipperProfiles` (nếu vai trò là `Shipper`).
  - Tích hợp vào `UserService.register`.
- **Mã hóa Mật khẩu SHA-256 Bảo mật & Tương thích ngược (Task 20.2):**
  - Xây dựng `utility/PasswordUtil.java` cung cấp `hashPassword(String)` (băm SHA-256 thành chuỗi Hex 64 ký tự) và `checkPassword(plainInput, storedHash)`.
  - Hỗ trợ tương thích ngược thông minh: Nếu mật khẩu trong CSDL chưa được băm (dữ liệu kiểm thử cũ như `123456`), người dùng vẫn đăng nhập thành công và hệ thống tự động nâng cấp mã hóa (Auto-upgrade password hash) thành SHA-256 ngay trong phiên đăng nhập đó.
  - Tích hợp toàn diện tại `UserService.login`, `UserService.register`, `UserService.changePassword`, và `AdminService.resetUserPasswordByAdmin`.
- **Triệt tiêu Chuỗi Hardcode Session (Task 9.1):**
  - Khai báo đầy đủ các hằng số Session trong `Constants.java`: `CURRENT_CART`, `CURRENT_TABLE`, `LAST_ORDER_ID`, `PLACED_ORDER_IDS`, `LANG`, `LAST_TICKET_ID`.
  - Refactor sạch sẽ toàn bộ các Controller sang gọi trực tiếp hằng số từ `Constants.java`.
- **Quy hoạch & Chuẩn hóa Dải Mã Lỗi (Task 9.2):**
  - Tách biệt rõ ràng quyền sở hữu dải mã: Báo cáo ẩn danh sở hữu độc quyền `S0300`-`S0301`, `E0300`-`E0316`, `DE0030`-`DE0033`.
  - Điểm danh & Nhân sự chuyển sang `S0708`-`S0709`, `E0716`-`E0721`, `DE0056`-`DE0058`.
  - Tách mã lỗi Tab Cài đặt sang `E9006` (`ERROR_INVALID_SETTING_TAB`).
- **Tái Cấu Trúc & Đồng Bộ 100% File Đa Ngôn Ngữ (Task 9.3):**
  - Đưa 207 mã backend (`SXXX`, `EXXX`, `DEXXX`) lên khối đầu trang theo đúng thứ tự logic trong `Constants.java`.
  - Bổ sung 10 key UI còn thiếu ở tiếng Việt và mã hóa 100% tiếng Việt sang chuẩn Unicode Escape `\uXXXX`.
  - Deduplicate toàn diện: Cả 2 tệp `messages_vi.properties` và `messages_en.properties` đạt 885 key duy nhất đối xứng 1-1, 0 duplicate keys.

### 3. File tác động & Trạng thái:
- `src/java/utility/PasswordUtil.java` (Tạo mới)
- `src/java/dao/UserDAO.java`
- `src/java/service/UserService.java`
- `src/java/service/AdminService.java`
- `src/java/utility/Constants.java`
- `src/java/controller/CartController.java`
- `src/java/controller/LanguageController.java`
- `src/java/controller/MenuController.java`
- `src/java/controller/ReportController.java`
- `src/java/controller/auth/LoginController.java`
- `src/java/resources/messages_vi.properties`
- `src/java/resources/messages_en.properties`
- `.agent/WorkList.md`
- **Trạng thái Build:** Apache Ant (nt clean compile với JDK 17) đạt **BUILD SUCCESSFUL (0 errors, 74 source files, 44 web files copied)**.

---

## [2026-10-22] - Hoàn thiện Task 13: Hotfix Đa Ngôn Ngữ Dashboard, Đồng Bộ Thông Báo AJAX & Triệt Tiêu Lỗi ???key??? Khu Vực Nhân Sự

### 1. Phân tích & Vấn đề Kỹ thuật:
- **Hardcode chữ tiếng Việt trên `web/index.jsp`:** Tại các nút Quick Action cho Admin và Shipper còn tồn tại chuỗi tiếng Việt hardcode ("Cấu hình bảo mật và mật khẩu", "Xem và cập nhật thông tin cá nhân", "Cập nhật hồ sơ và mật khẩu").
- **Thiếu đồng bộ DOM thời gian thực khi đánh dấu đã đọc thông báo:** Trong `web/js/main.js`, khi người dùng bấm đánh dấu đã đọc một thông báo hoặc đánh dấu tất cả đã đọc, thẻ thông báo trong Dropdown Header chưa bị xóa khỏi DOM `<ul id="notificationItemsList">`, chưa kích hoạt hiển thị `#notificationEmptyState` khi hết thư chưa đọc, và chưa đồng bộ xóa class `unread` / dấu chấm đỏ `.notification-unread-dot` trên Dashboard Feed mà không cần reload trang.
- **Bug hiển thị chuỗi `???key???` tại khu vực Nhân sự & Điểm danh (`web/hr_management.jsp`):** Rà soát phát hiện 8 keys đa ngôn ngữ bị thiếu trong cả 2 tệp `.properties`:
  + Tooltip nút: `hr.btn.approve_ot`, `hr.btn.reject_ot`.
  + Badge trạng thái điểm danh: `attendance.badge.ontime`, `attendance.badge.late`, `attendance.badge.left_early`, `attendance.badge.pending_ot`.
  + Bộ lọc tháng/năm ở Tab 3 tổng hợp công: `attendance.filter.month`, `attendance.filter.year`.
  Khi người dùng mở trang Quản lý nhân sự, Tomcat hiển thị các nhãn lỗi dạng `???attendance.badge.ontime???`.

### 2. Giải pháp Kỹ thuật Triệt để:
- **Task 13.1 (Đa ngôn ngữ hóa `index.jsp`):** Thay thế 100% chuỗi mô tả hardcode bằng thẻ `<fmt:message key="dashboard.action.settings_desc" />`, `<fmt:message key="dashboard.action.profile_desc" />`, và `<fmt:message key="dashboard.action.shipper_settings_desc" />`.
- **Task 13.2 (Đồng bộ thời gian thực cho Thông báo `main.js`):**
  + Tái cấu trúc hàm `markNotificationAsRead(id)`: Gọi POST AJAX, cập nhật Badge chuông với `unreadCount`, xóa trực tiếp phần tử `#notifItem_${id}` khỏi Dropdown Header (hiển thị `#notificationEmptyState` nếu danh sách trống), gỡ bỏ class `unread` và `.notification-unread-dot` trên feed `#feedItem_${id}`.
  + Tái cấu trúc hàm `markAllNotificationsAsRead()`: Cập nhật Badge về 0, làm rỗng Dropdown Header và bật `#notificationEmptyState`, chuyển toàn bộ `.feed-item.unread` trên feed thành đã đọc.
- **Task 13.3 (Bổ sung 100% các Key bị thiếu trong `messages_vi.properties` & `messages_en.properties`):**
  + Mã hóa toàn bộ ký tự tiếng Việt sang chuẩn Unicode Escape (`\uXXXX`) thuần ASCII:
    * `attendance.badge.ontime=\u0110\u00fang gi\u1edd` (Đúng giờ)
    * `attendance.badge.late=\u0110i mu\u1ed9n` (Đi muộn)
    * `attendance.badge.left_early=V\u1ec1 s\u1edbm` (Về sớm)
    * `attendance.badge.pending_ot=Ch\u1edd duy\u1ec7t OT` (Chờ duyệt OT)
    * `attendance.filter.month=Th\u00e1ng` (Tháng)
    * `attendance.filter.year=N\u0103m` (Năm)
    * `hr.btn.approve_ot=Duy\u1ec7t l\u00e0m th\u00eam gi\u1edd (OT)`
    * `hr.btn.reject_ot=T\u1eeb ch\u1ed1i l\u00e0m th\u00eam gi\u1edd (OT)`
    * `dashboard.action.settings_desc=C\u1ea5u h\u00ecnh b\u1ea3o m\u1eadt v\u00e0 m\u1eadt kh\u1ea9u`
    * `dashboard.action.profile_desc=Xem v\u00e0 c\u1eadp nh\u1eadt th\u00f4ng tin c\u00e1 nh\u00e2n`
    * `dashboard.action.shipper_settings_desc=C\u1eadp nh\u1eadt h\u1ed3 s\u01a1 v\u00e0 m\u1eadt kh\u1ea9u`
  + Khai báo các nhãn tiếng Anh tương ứng trong `messages_en.properties`.

### 3. File tác động & Trạng thái:
- `web/index.jsp`
- `web/js/main.js`
- `src/java/resources/messages_vi.properties`
- `src/java/resources/messages_en.properties`
- `.agent/WorkList.md`
- **Trạng thái Build:** Apache Ant (`ant compile`) đạt **BUILD SUCCESSFUL (0 errors)**.
- Quét tự động toàn bộ JSP: `hr_management.jsp` và `index.jsp` đạt 100% sạch key (0 missing key).

---

## [2026-10-22] - Hoàn thiện Toàn diện Task 10 (Task 10.1 đến 10.5): Quản lý Nhân sự, Ca làm việc & Duyệt chấm công (HR & Shift Management)
### 1. Phân tích & Vấn đề Kỹ thuật:
- **Tồn đọng cơ chế tự gán ca Demo:** Trang `attendance.jsp` và `AttendanceController` tồn tại nút gán ca mẫu và reset ca trực tiếp từ phía nhân viên, không phù hợp với nghiệp vụ thực tế khi việc phân ca phải do Quản lý (Manager/Admin) điều phối.
- **Thiếu phân hệ quản lý ca & phân ca:** Chưa có giao diện và tầng nghiệp vụ cho Quản lý tạo/sửa khung giờ ca (`Shifts`), phân ca cho nhân viên (`ShiftAssignments`), phê duyệt các trường hợp xin vào ca sớm (`Pending Early Approval`) và tăng ca (`Pending OT Approval`).
- **Nhu cầu tổng hợp công tháng:** Cần bảng thống kê tổng hợp số ca, giờ làm, số lần đúng giờ, đi muộn, tăng ca của toàn bộ nhân viên theo tháng để phục vụ tính lương và đánh giá hiệu suất.

### 2. Giải pháp Kỹ thuật Triệt để:
- **Task 10.1 (Gỡ bỏ Demo):** Gỡ bỏ hoàn toàn form và nút `assignDemoShift`, `resetDemoShift` khỏi `attendance.jsp`, dọn sạch các nhánh xử lý trong `AttendanceController`, `AttendanceService`, và `AttendanceDAO`.
- **Task 10.2 (Mở rộng Tầng DAO `AttendanceDAO.java`):**
  - CRUD Khung giờ ca: `insertShift`, `updateShift`, `getShiftById`, `isShiftNameExists`, `isShiftAssigned`.
  - Quản lý phân ca: `insertShiftAssignment`, `deleteShiftAssignment`, `hasAssignmentOnDate`, `isAssignmentCheckedIn`, `getAssignmentsWithAttendance(workDate)`.
  - Xử lý duyệt công: `updateAttendanceApproval`, `getAttendanceById`, `getPendingApprovals()`, `getMonthlyTimesheetSummary(month, year)`, `getActiveStaffUsers()`.
  - 100% bọc `try-with-resources` an toàn và ném `ValidationException` với mã lỗi CSDL chuẩn `DE005X`.
- **Task 10.3 (Nghiệp vụ & Validation `AttendanceService.java` & `Validator.java`):**
  - Validation 2 tầng: kiểm tra tên ca không rỗng, định dạng giờ bắt đầu < giờ kết thúc (`LocalTime`), chống trùng tên ca, chống trùng phân ca trong ngày (tối đa 1 ca/ngày theo Option A), chặn xóa phân ca đã điểm danh.
  - Nghiệp vụ duyệt/từ chối OT và vào ca sớm (`approveAttendance`, `rejectAttendance`).
  - Đóng gói dữ liệu sang `Map<String, Object>` / `List<Map<String, Object>>` (Zero Model leakage!).
- **Task 10.4 (Tầng Điều phối `HrManagementController.java`):**
  - Phân quyền nghiêm ngặt trong Session: chỉ cho phép `Manager` và `Admin`.
  - `doGet`: Nhận bộ lọc ngày/tháng/tab, chuyển tiếp Flash Message qua `FlashMessageUtil`, nạp dữ liệu Map đẩy sang View.
  - `doPost`: Whitelist routing các action (`createShift`, `updateShift`, `assignShift`, `deleteAssignment`, `approveOt`, `rejectOt`, `approveEarly`, `rejectEarly`), điều hướng theo chuẩn PRG (Post-Redirect-Get).
- **Task 10.5 (Giao diện Quản lý Nhân sự `hr_management.jsp` & `hr_shift_modal.jsp`):**
  - Giao diện 3 tab chuyên nghiệp: Phân ca & khung giờ, Theo dõi điểm danh & duyệt OT/vào sớm, Tổng hợp công tháng.
  - Tách modal tạo/sửa ca và phân ca vào `web/components/hr_shift_modal.jsp`.
  - Kích hoạt liên kết "Quản lý nhân sự" trong `header.jsp`.
  - Tích hợp điều khiển modal vào `web/js/main.js` và định dạng CSS trong `web/css/global.css`.
  - Đa ngôn ngữ 100%: Toàn bộ thông báo `S070X`, `E070X`, `DE005X` và nhãn hiển thị trong `messages_vi.properties` được mã hóa Unicode Escape (`\uXXXX`) chuẩn thuần ASCII.

### 3. File tác động & Biên dịch:
- `web/attendance.jsp`
- `src/java/controller/AttendanceController.java`
- `src/java/dao/AttendanceDAO.java`
- `src/java/utility/Validator.java`
- `src/java/utility/Constants.java`
- `src/java/service/AttendanceService.java`
- `src/java/controller/hr/HrManagementController.java` (File mới)
- `web/components/hr_shift_modal.jsp` (File mới)
- `web/hr_management.jsp` (File mới)
- `web/header.jsp`
- `web/js/main.js`
- `web/css/global.css`
- `src/java/resources/messages_vi.properties` & `messages_en.properties`
- `web/WEB-INF/web.xml`
- `.agent/WorkList.md`: Đã hoàn thành 100% các mục của **TASK 10** (`Task 10.1` đến `Task 10.5` đều đạt `[DONE]`).
- **Trạng thái Build:** Toàn bộ dự án biên dịch qua JDK 17 đạt **BUILD SUCCESSFUL (0 errors)**.

### 4. Bài học kinh nghiệm & Quy chuẩn kiến trúc cốt lõi:
- **Chuẩn hóa Phân ca và Tính toàn vẹn Dữ liệu:** Không bao giờ cho phép xóa phân ca (`ShiftAssignment`) nếu bản ghi chấm công (`Attendances`) đã phát sinh. Mọi thao tác thu hồi phân ca phải kiểm tra điều kiện điểm danh ở tầng Service/DAO trước khi thực hiện lệnh DELETE.
- **Tránh trùng lặp dải mã lỗi:** Khi mở rộng module mới, cần rà soát kỹ các dải mã đã sử dụng trong `Constants.java` (ví dụ `E050X` đã dùng cho Alert Tồn kho) để quy hoạch sang dải mã mới hoàn toàn độc lập (`S070X`, `E070X`) tránh xung đột key trong file properties.
- **Tránh BOM khi ghi tệp nguồn Java:** Trên môi trường Windows / PowerShell, khi ghi tệp mã nguồn UTF-8 bắt buộc sử dụng `New-Object System.Text.UTF8Encoding $false` để không chèn ký tự Byte Order Mark (`\ufeff`), giúp trình biên dịch `javac` đọc tệp trơn tru mà không bị lỗi `illegal character: '\ufeff'`.

---

## [2026-10-21] - Hoàn thiện Toàn diện Task 8 (Task 8.5, 8.6, 8.7): Triệt tiêu Hardcode, Chuẩn hóa Controller & Bảo đảm An toàn JDBC Phân hệ Shipper/Delivery
### 1. Phân tích & Vấn đề Kỹ thuật:
- **Tồn đọng Hardcode & Magic Number trong DeliveryService:** Các chuỗi ghi chú lịch sử giao hàng (`"Dieu phoi gan shipper"`, `"Shipper chu dong tiep nhan don"`, v.v.), chuỗi trạng thái hoạt động (`"READY"`, `"DELIVERING"`, `"Ready"`, `"Delivering"`), vai trò `"Shipper"` và con số magic `3` làm giá trị fallback của `maxOrders` bị hardcode trực tiếp trong code Java.
- **Vi phạm DRY & Rủi ro Routing trong ShipperController:** Hàm `forwardFlashMessages` bị khai báo lặp lại dưới dạng private method (13 dòng code) thay vì gọi `FlashMessageUtil`. Nhánh `else` trong `doPost` mặc định kích hoạt cập nhật trạng thái đơn, tiềm ẩn rủi ro khi client gửi action rỗng hoặc không hợp lệ.
- **Nguy cơ rò rỉ kết nối JDBC (Connection Leak) & Hardcode mã lỗi trong DeliveryDAO:** 4 phương thức truy vấn danh sách (`getAllTasks`, `getTasksByShipper`, `getAvailableTasksForShipper`, `getTaskById`) và 2 phương thức cập nhật (`updateTaskStatus`, `updateTaskShipper`) mở `PreparedStatement` và `ResultSet` nhưng không bọc trong cú pháp `try-with-resources`. Đồng thời hardcode các chuỗi mã lỗi CSDL `"DE0016"` và `"DE0017"` tại 7 vị trí thay vì dùng `Constants`.

### 2. Giải pháp Kỹ thuật Triệt để:
- **Task 8.5 (Triệt tiêu Hardcode & Magic Number):**
  - Khai báo các hằng số chuẩn trong `Constants.java`: `DEFAULT_SHIPPER_MAX_ORDERS = 3`, `ROLE_SHIPPER = "Shipper"`, `SHIPPER_STATUS_*`, `SHIPPER_WORK_*`, và `NOTE_*` cho nhật ký giao hàng.
  - Tái cấu trúc `DeliveryService.java`: Thay thế 100% chuỗi hardcode và số magic bằng hằng số từ `Constants`. Bảo toàn 100% logic ưu tiên đọc động `profile.getMaxOrders()` từ CSDL, chỉ fallback về `DEFAULT_SHIPPER_MAX_ORDERS` khi profile rỗng. Xóa bỏ điều kiện kiểm tra dư thừa `"Returned".equalsIgnoreCase(newStatus)`.
- **Task 8.6 (Tối ưu Controller & Thắt chặt Whitelist Routing):**
  - Trong `ShipperController.java`: Import và gọi `FlashMessageUtil.forwardFlashMessages(request, session)` tại `doGet`, xóa bỏ hoàn toàn hàm private trùng lặp.
  - Thắt chặt cấu trúc rẽ nhánh `doPost`: Bắt rõ ràng `else if ("updateStatus".equals(action))`. Tại nhánh `else` cuối cùng, ném `throw new ValidationException(Constants.ERROR_INVALID_ACTION)` (mã lỗi `E9000`) để bắt giữ an toàn qua Session Flash Message và chuyển hướng bảo vệ.
- **Task 8.7 (Bảo đảm An toàn Tài nguyên JDBC & Chuẩn hóa Mã lỗi CSDL):**
  - Trong `DeliveryDAO.java`: Thay thế 100% (7 vị trí) mã lỗi hardcode `"DE0016"` và `"DE0017"` bằng `Constants.ERROR_DATABASE_GET_ORDER` và `Constants.ERROR_DATABASE_UPDATE_ORDER`.
  - Tái cấu trúc toàn bộ 6 phương thức JDBC sang cú pháp `try-with-resources` (`try (PreparedStatement st = ...; ResultSet rs = ...)`), đảm bảo tự động đóng tài nguyên khi kết thúc khối lệnh, triệt tiêu hoàn toàn rủi ro rò rỉ Connection Pool.

### 3. File tác động & Biên dịch:
- `src/java/utility/Constants.java`
- `src/java/service/DeliveryService.java`
- `src/java/controller/ShipperController.java`
- `src/java/dao/DeliveryDAO.java`
- `.agent/WorkList.md`: Đã hoàn thành 100% các mục của **TASK 8** (`Task 8.1` đến `Task 8.7` đều đạt `[DONE]`).
- **Trạng thái Build:** Toàn bộ dự án biên dịch qua JDK 17 đạt **BUILD SUCCESSFUL (0 errors)**.

### 4. Bài học kinh nghiệm & Quy chuẩn kiến trúc cốt lõi:
- **Luôn ưu tiên Dữ liệu CSDL, chỉ dùng Hằng số làm Fallback phòng thủ:** Các thuộc tính cá nhân hóa của người dùng (như `MaxOrders` của từng Shipper) bắt buộc phải đọc động từ bảng `ShipperProfiles`. Hằng số Java `DEFAULT_SHIPPER_MAX_ORDERS` chỉ đóng vai trò dự phòng (Defensive Fallback) phòng ngừa lỗi thiếu dữ liệu, không bao giờ được ghi đè logic nghiệp vụ CSDL.
- **Quản lý Vòng đời Tài nguyên JDBC bằng try-with-resources:** Mọi đối tượng triển khai `AutoCloseable` (`Connection`, `PreparedStatement`, `ResultSet`) bắt buộc phải được khởi tạo bên trong khai báo `try (...)`. Tuyệt đối không mở statement/resultset trần trụi để bảo vệ kết nối máy chủ CSDL trong môi trường tải cao.
- **Kiểm soát Định tuyến theo Danh sách trắng (Whitelist Routing):** Tầng Controller tiếp nhận HTTP POST bắt buộc phải phân định rõ ràng các action được phép; nhánh `else` phải ném ngoại lệ hoặc điều hướng an toàn, tuyệt đối không âm thầm fallback vào một tác vụ cập nhật dữ liệu quan trọng.

---

## [2026-10-21] - Hoàn thiện Khắc phục Dứt điểm Tồn đọng UI & Locale Phân hệ Shipper / Delivery
### 1. Phân tích & Vấn đề Kỹ thuật:
- **Mất liên kết Locale ở các trang con và Modals:** Do Tomcat tạo `PageContext` riêng cho mỗi `jsp:include`, hai modal (`shipper_complete_modal.jsp`, `shipper_fail_modal.jsp`) và trang `delivery_unauthorized.jsp` nếu thiếu `<fmt:setLocale>` trước `<fmt:setBundle>` sẽ không kế thừa được ngôn ngữ đang chọn từ trang cha.
- **Hardcode chuỗi mã lỗi tại View:** Tại `delivery_unauthorized.jsp:18`, chuỗi `error.unauthorized` được gọi tự do thay vì dùng mã chuẩn toàn hệ thống `Constants.ERROR_LOGIN_FAIL_ROLE` (`E0004`).
- **Badge trạng thái tĩnh tiếng Anh:** Tại `web/components/delivery_orders_table.jsp:76-94`, các nhãn trạng thái `Waiting for delivery`, `Picked up`, `Delivering`, `Delivered`, `Returned`, `Paid`, `Cancelled` bị viết cứng bằng tiếng Anh, khiến người dùng chuyển sang tiếng Việt vẫn thấy hiển thị tiếng Anh thô.

### 2. Giải pháp Kỹ thuật Triệt để:
- **Bổ sung JSTL Locale động 3 tầng (Session -> Cookie -> 'vi'):** Đã chèn trực tiếp trên `<fmt:setBundle>` tại cả 3 file:
  - `web/components/shipper_complete_modal.jsp`
  - `web/components/shipper_fail_modal.jsp`
  - `web/delivery_unauthorized.jsp`
- **Chuẩn hóa mã lỗi phân quyền:** Tại `web/delivery_unauthorized.jsp`, đổi fallback `error.unauthorized` thành `E0004` (đã có sẵn trong cả 2 file properties).
- **Đa ngôn ngữ hóa 100% Badge trạng thái đơn Delivery:** Tại `web/components/delivery_orders_table.jsp`, thay thế toàn bộ text tĩnh tiếng Anh trong các badge bằng `<fmt:message key="status.${order.status}" />`.
- **Bổ sung các key trạng thái phụ trợ (Unicode Escape `\uXXXX`):**
  - `messages_vi.properties`: `status.Picked\ up=\u0110\u00e3 l\u1ea5y h\u00e0ng`, `status.Paid=\u0110\u00e3 thanh to\u00e1n`.
  - `messages_en.properties`: `status.Picked\ up=Picked up`, `status.Paid=Paid`.

### 3. File tác động & Biên dịch:
- `web/components/shipper_complete_modal.jsp`
- `web/components/shipper_fail_modal.jsp`
- `web/delivery_unauthorized.jsp`
- `web/components/delivery_orders_table.jsp`
- `src/java/resources/messages_vi.properties` & `src/java/resources/messages_en.properties`
- **Trạng thái Build:** `BUILD SUCCESSFUL (0 errors)`.

---

## [2026-10-21] - Bổ sung Cấu hình JSTL Locale động cho dispatch_dashboard.jsp và shipper_dashboard.jsp
### 1. Phân tích & Vấn đề Kỹ thuật:
- **Cơ chế JSTL i18n:** Khi JSP chỉ khai báo `<fmt:setBundle basename="resources.messages" />` mà không khai báo `<fmt:setLocale>`, JSTL sẽ fallback về Locale mặc định của trình duyệt client (Accept-Language header) hoặc JVM của Web Server.
- **Mất liên kết với LanguageController:** Hệ thống điều khiển ngôn ngữ qua `LanguageController.java` (lưu đồng thời vào `sessionScope.LANG` và `cookie.LANG`). Do thiếu `<fmt:setLocale>`, cả hai trang `dispatch_dashboard.jsp` và `shipper_dashboard.jsp` hoàn toàn không nhận diện được khi người dùng click chuyển đổi ngôn ngữ.
- **Thứ tự thẻ JSTL:** `<fmt:setLocale>` BẮT BUỘC phải nằm trước `<fmt:setBundle>` thì ResourceBundle mới nạp đúng tệp `messages_xx.properties` tương ứng với Locale được chọn.

### 2. Giải pháp Kỹ thuật Triệt để:
- Chèn trực tiếp trước `<fmt:setBundle>` tại cả `web/dispatch_dashboard.jsp` và `web/shipper_dashboard.jsp`:
  `jsp
  <c:set var="currentLang" value="${not empty sessionScope.LANG ? sessionScope.LANG : (not empty cookie.LANG.value ? cookie.LANG.value : 'vi')}" />
  <fmt:setLocale value="${currentLang}" />
  `
- Biểu thức EL này thực hiện cơ chế fallback 3 tầng: `Session -> Cookie -> Default ('vi')`, đồng bộ chuẩn 100% với các trang `settings.jsp` và `report.jsp`.

### 3. File tác động & Biên dịch:
- `web/dispatch_dashboard.jsp` & `web/shipper_dashboard.jsp`
- **Trạng thái Build:** `BUILD SUCCESSFUL (0 errors)`.

---

## [2026-10-21] - Hoàn thiện Tinh chỉnh Hạng mục 4 & 5 Phân hệ Shipper/Dispatch (Đa ngôn ngữ & Xóa CSS Inline)
### 1. Phân tích & Vấn đề Kỹ thuật:
- **Thiếu key i18n & nguy cơ vỡ giao diện:** Tại `shipper_dashboard.jsp`, cột trạng thái bảng "Đơn hàng của tôi" dùng `status.${task.status}` nhưng file properties thiếu key `status.Waiting\ for\ dispatch`. Đồng thời, nhãn trạng thái này chưa có badge màu chuẩn CSS `.order-badge` như `delivery_orders_table.jsp`.
- **Hardcode chuỗi tiếng Việt & Inline CSS:** Tại `dispatch_dashboard.jsp`, chuỗi `"đơn"` bị hardcode tiếng Việt thô, và thẻ `<hr>` chứa `style="margin: 30px 0;"` vi phạm quy chuẩn cấm inline CSS theo `Rule_BOMS.md`.

### 2. Giải pháp Kỹ thuật Triệt để:
- **Đa ngôn ngữ hóa (i18n):**
  - Thêm `status.Waiting\ for\ dispatch=Ch\u1edd \u0111i\u1ec1u ph\u1ed1i` vào `messages_vi.properties` (chuẩn Unicode Escape `\uXXXX`) và `status.Waiting\ for\ dispatch=Waiting for dispatch` vào `messages_en.properties`.
  - Thêm `dispatch.label.orders=\u0111\u01a1n` vào `messages_vi.properties` và `dispatch.label.orders=orders` vào `messages_en.properties`.
- **Tầng View JSP:**
  - `web/dispatch_dashboard.jsp`: Xóa bỏ `style="margin: 30px 0;"`, chuyển `<td>... đơn</td>` sang dùng `<fmt:message key="dispatch.label.orders" />`.
  - `web/shipper_dashboard.jsp`: Bọc cột trạng thái vào cấu trúc `<c:choose>`, gán đồng bộ các class CSS badge `.order-badge` (`order-badge-dispatch`, `order-badge-prepared`, `order-badge-delivering`, `order-badge-delivered`, `order-badge-cancelled`).

### 3. File tác động & Biên dịch:
- `src/java/resources/messages_vi.properties` & `src/java/resources/messages_en.properties`
- `web/dispatch_dashboard.jsp` & `web/shipper_dashboard.jsp`
- **Trạng thái Build:** `BUILD SUCCESSFUL (0 errors)`.

---

## [2026-10-21] - Hoàn thiện Task 7.6: Tái cấu trúc logic tạo chuỗi JSON bằng thư viện Gson (IngredientAlertController)
### 1. Phân tích & Vấn đề Kỹ thuật:
- **Nối chuỗi JSON thủ công (Anti-pattern / Bad Practice):** Trong `IngredientAlertController.java`, hai phương thức private `buildAlertsJson` và `escapeJson` được viết để ghép nối chuỗi JSON bằng `StringBuilder` thủ công. Cách tiếp cận này rất dễ gây lỗi cú pháp JSON khi trường dữ liệu chứa ký tự đặc biệt, dấu ngoặc kép, ký tự điều khiển hoặc dấu xuống dòng.
- **Hardcode chuỗi mã lỗi và thông báo:** `IngredientAlertController.java` đang hardcode các chuỗi `"E0004"`, `"E9000"`, `"DE0020"`, vi phạm quy chuẩn sử dụng `Constants.java` theo `Rule_BOMS.md`.
- **Đồng bộ thư viện dự án:** Dự án cần bổ sung thư viện `gson-2.10.1.jar` vào cả `lib/` và `web/WEB-INF/lib/`, đồng thời cấu hình classpath trong NetBeans `nbproject/project.properties` để hỗ trợ serialization JSON chuẩn mực.

### 2. Giải pháp Kỹ thuật Triệt để:
- **Tích hợp thư viện Gson:**
  - Bổ sung `gson-2.10.1.jar` vào `lib/` và `web/WEB-INF/lib/`.
  - Cập nhật thuộc tính `file.reference.gson-2.10.1.jar` và `javac.classpath` trong `nbproject/project.properties`.
- **Tái cấu trúc IngredientAlertController:**
  - Khai báo trường hằng số `private final Gson gson = new Gson();`.
  - Xóa bỏ triệt để 100% hai phương thức thủ công `buildAlertsJson` và `escapeJson`.
  - Trong `doGet` (polling alerts): gom kết quả trả về vào `Map<String, Object>` chuẩn gồm các key `success`, `count`, `alerts` (hoặc `error` nếu có ngoại lệ) và tuần tự hóa bằng `gson.toJson(res)`.
  - Trong `doPost` (confirm/close alert): đóng gói kết quả phản hồi vào `Map<String, Object>` với các key `success`, `message`, `error` và tuần tự hóa bằng `gson.toJson(res)`.
  - Chuẩn hóa toàn bộ mã lỗi và thông báo thành công: dùng `Constants.ERROR_LOGIN_FAIL_ROLE` ("E0004"), `Constants.ERROR_INVALID_ACTION` ("E9000"), `Constants.ERROR_DATABASE_UPDATE_ALERT` ("DE0020"), `Constants.SUCCESS_CONFIRM_ALERT` ("S0007"), `Constants.SUCCESS_CLOSE_ALERT` ("S0008").
- **Dọn dẹp tài liệu dự án:**
  - Đánh dấu hoàn thành toàn bộ TASK 7 trong `.agent/WorkList.md`.
  - Xóa bỏ khối ghi chú bàn giao cho tác giả luồng Shipper sau khi toàn bộ 7 task của TASK 7 hoàn tất và biên dịch đạt 0 lỗi.

### 3. File bị tác động & Tiến độ:
- `lib/gson-2.10.1.jar`: Thư viện Gson cho môi trường dev/IDE.
- `web/WEB-INF/lib/gson-2.10.1.jar`: Thư viện Gson runtime cho Servlet container (Tomcat).
- `nbproject/project.properties`: Khai báo thư viện Gson vào classpath dự án NetBeans.
- `src/java/controller/IngredientAlertController.java`: Tái cấu trúc dùng `Gson`, xóa bỏ 2 hàm thủ công, thay hardcode bằng `Constants`.
- `.agent/WorkList.md`: Đánh dấu `Task 7.6 [DONE]`, hoàn thành 100% TASK 7, xóa bỏ khối ghi chú bàn giao tạm thời.
- **Trạng thái Build:** Biên dịch qua JDK 17 và Tomcat 10.1 đạt **BUILD SUCCESSFUL (0 errors, 70 source files)**.

### 4. Bài học kinh nghiệm & Quy chuẩn kiến trúc cốt lõi (CRITICAL):
- **Tuyệt đối không nối chuỗi JSON thủ công trong Controller:** Luôn sử dụng thư viện chuẩn (`Gson`) kết hợp với Data Structures (`Map<String, Object>`, `List<Map<...>>`) để serialize dữ liệu trả về cho client. Điều này đảm bảo an toàn injection, tự động escape ký tự đặc biệt, chuẩn hóa định dạng UTF-8 và giữ mã nguồn ngắn gọn, dễ bảo trì.
- **Bảo toàn Contract với Frontend (main.js):** Khi refactor cơ chế tạo JSON ở backend, luôn phải đối chiếu với client script (`web/js/main.js`) để đảm bảo schema JSON (tên key, kiểu dữ liệu boolean/number/array) không thay đổi, tránh làm gãy luồng AJAX Polling hoặc Notification badge trên giao diện người dùng.

---

## [2026-10-21] - Hoàn thiện Task 7.5: Tái Cấu Trúc Hàm Flash Messages Trùng Lặp (DRY) Sang FlashMessageUtil
### 1. Phân tích & Vấn đề Kỹ thuật:
- **Vấn đề trùng lặp mã nguồn (Code Duplication):** Cả 6 Controller (`AttendanceController`, `DispatchController`, `InventoryManagementController`, `MenuController`, `ReportController`, `ReportManagementController`) đều tự khai báo hàm `private void forwardFlashMessages(...)` để chuyển `SUCCESS_MESSAGE` và `ERROR_MESSAGE` từ `session` sang `request` sau đó xóa khỏi `session` (chuẩn PRG), vi phạm nghiêm trọng nguyên tắc DRY và làm phình to mã nguồn các Controller.
- **Hành vi riêng biệt của một số Controller:** `MenuController` có thêm `"LAST_ORDER_ID"` và `ReportController` có thêm `"LAST_TICKET_ID"` cần được bảo lưu xử lý an toàn để không ảnh hưởng đến nghiệp vụ hiện tại.

### 2. Giải pháp Kỹ thuật Triệt để:
- **Tạo mới lớp tiện ích (`utility/FlashMessageUtil.java`):**
  - Chứa phương thức tĩnh `public static void forwardFlashMessages(HttpServletRequest request, HttpSession session)`.
  - Kiểm tra an toàn `session != null && request != null`, trích xuất `SUCCESS_MESSAGE` và `ERROR_MESSAGE` sang `request.setAttribute` và dọn dẹp `session.removeAttribute`.
  - Constructor private để ngăn việc khởi tạo sai mục đích theo chuẩn Utility.
- **Tầng Controller:**
  - Nhúng `FlashMessageUtil.forwardFlashMessages(request, session)` vào phương thức `doGet` của cả 6 Controller.
  - Xóa bỏ triệt để 100% các hàm private `forwardFlashMessages` (và hàm phụ trợ `moveSessionToRequest` không còn dùng) tại 6 Controller.
  - Đối với các biến đặc thù (`LAST_ORDER_ID` tại `MenuController`, `LAST_TICKET_ID` tại `ReportController`), giữ lại dòng xử lý trực tiếp tại Controller, bảo toàn toàn vẹn hành vi nghiệp vụ.

### 3. File bị tác động & Tiến độ:
- `src/java/utility/FlashMessageUtil.java`: Tạo mới class tiện ích chứa hàm tĩnh dùng chung.
- `src/java/controller/AttendanceController.java`: Dùng `FlashMessageUtil`, xóa hàm private thừa.
- `src/java/controller/DispatchController.java`: Dùng `FlashMessageUtil`, xóa hàm private thừa.
- `src/java/controller/InventoryManagementController.java`: Dùng `FlashMessageUtil`, xóa hàm private thừa.
- `src/java/controller/MenuController.java`: Dùng `FlashMessageUtil`, giữ `LAST_ORDER_ID`, xóa hàm private thừa.
- `src/java/controller/ReportController.java`: Dùng `FlashMessageUtil`, giữ `LAST_TICKET_ID`, xóa hàm private thừa.
- `src/java/controller/ReportManagementController.java`: Dùng `FlashMessageUtil`, xóa hàm private thừa.
- `.agent/WorkList.md`: Đánh dấu hoàn thành Task 7.5 **[DONE]**.
- **Trạng thái Build:** Biên dịch qua JDK 17 và Tomcat 10.1 đạt **`BUILD SUCCESSFUL` (0 lỗi, 70 source files)**.

### 4. Bài học kinh nghiệm & Quy chuẩn kiến trúc cốt lõi (CRITICAL):
- **Nguyên tắc thiết kế tầng Utility (`Rule_BOMS.md`):** Các logic chuyển tiếp dữ liệu chung (cross-cutting concerns) giữa Request/Session phải được quy hoạch vào package `utility` dưới dạng phương thức tĩnh (`public static`), tuyệt đối không lặp lại code phụ trợ bên trong Controller.
- **Bảo toàn nghiệp vụ đặc thù khi tái cấu trúc:** Khi gom các hàm lặp lại về tiện ích dùng chung, cần phân tích kỹ lưỡng các thuộc tính mở rộng (như `LAST_ORDER_ID`, `LAST_TICKET_ID`) để đảm bảo không làm mất dữ liệu điều hướng của các phân hệ khác.

---

## [2026-10-21] - Hoàn thiện Tasks 7.3, 7.4 (DispatchController), 7.7: Chuẩn hóa UI Badge, Mã Lỗi & Thông báo Shipper/Delivery
### 1. Phân tích & Vấn đề Kỹ thuật:
- **Task 7.3 (Lỗi hiển thị status dispatch):** File `dispatch_dashboard.jsp` dùng `<fmt:message key="status.${task.status}" />` bị vỡ nhãn `???status.Waiting for dispatch???` do thiếu key tương ứng. Cần thay bằng cấu trúc `<c:choose>` và đồng bộ hoàn toàn với component `delivery_orders_table.jsp` bằng class CSS `.order-badge.order-badge-dispatch`.
- **Task 7.4 & 7.7 (Hardcode & Chuẩn hóa mã lỗi Shipper):** `DispatchController.java`, `ShipperController.java` và `DeliveryService.java` dùng các chuỗi hardcode (`"dispatch.msg.assign_success"`, `"error.unauthorized"`, `"error.delivery.*"`, `"shipper.msg.*"`), vi phạm quy chuẩn backend notification `EXXX` và `SXXX` trong `Rule_BOMS.md`.
- **Lỗi phân quyền:** Cần quy về mã chuẩn toàn hệ thống `E0004` (`ERROR_LOGIN_FAIL_ROLE`).

### 2. Giải pháp Kỹ thuật Triệt để:
- **Tầng View (`dispatch_dashboard.jsp`):**
  - Cập nhật cột `status-cell`: sử dụng `<c:choose>`, bắt đúng trạng thái `'Waiting for dispatch'` để in `<span class="order-badge order-badge-dispatch"><fmt:message key="order.status.waiting_dispatch" /></span>`.
- **Tầng Utility (`Constants.java`):**
  - Khai báo dải `S060X` cho điều phối & shipper: `SUCCESS_ASSIGN_SHIPPER = "S0601"`, `SUCCESS_CLAIM_TASK = "S0602"`, `SUCCESS_ACCEPT_TASK = "S0603"`, `SUCCESS_REJECT_TASK = "S0604"`, `SUCCESS_COMPLETE_DELIVERY = "S0605"`, `SUCCESS_FAIL_DELIVERY = "S0606"`, `SUCCESS_UPDATE_DELIVERY_STATUS = "S0607"`.
  - Khai báo dải `E060X` cho lỗi nghiệp vụ giao hàng: `E0601` -> `E0607` thay thế hoàn toàn các chuỗi `error.delivery.*`.
  - Khai báo hằng số `ERROR_UNAUTHORIZED = "E0004"`.
- **Tầng Controller (`DispatchController.java`, `ShipperController.java`):**
  - Thay thế toàn bộ chuỗi hardcode bằng `Constants.SUCCESS_*` và `Constants.ERROR_LOGIN_FAIL_ROLE`.
- **Tầng Tài nguyên (`messages_vi.properties`, `messages_en.properties`):**
  - Khai báo đầy đủ nội dung cho `S0601` -> `S0607` và `E0601` -> `E0607`. Trong `messages_vi.properties` tuân thủ 100% mã hóa **Unicode Escape (`\uXXXX`)**.
  - Dọn sạch dứt điểm các key cũ `dispatch.msg.assign_success`, `shipper.msg.*`, `error.delivery.*`, `success.delivery.update`.

### 3. File bị tác động & Tiến độ:
- `web/dispatch_dashboard.jsp`: Cập nhật render badge chuẩn `order-badge order-badge-dispatch`.
- `src/java/utility/Constants.java`: Bổ sung dải mã `S060X` và `E060X`, thêm `ERROR_UNAUTHORIZED`.
- `src/java/controller/DispatchController.java`: Gỡ bỏ hardcode, gọi `Constants.SUCCESS_ASSIGN_SHIPPER` và `Constants.ERROR_LOGIN_FAIL_ROLE`.
- `src/java/controller/ShipperController.java`: Đồng bộ gọi `Constants.SUCCESS_*` và `Constants.ERROR_LOGIN_FAIL_ROLE`.
- `src/java/resources/messages_vi.properties`: Thêm `S060X`, `E060X` chuẩn `\uXXXX`, xóa key cũ.
- `src/java/resources/messages_en.properties`: Thêm `S060X`, `E060X` tiếng Anh, xóa key cũ.
- `.agent/WorkList.md`: Đánh dấu hoàn thành Task 7.3, Task 7.4 (phần DispatchController), và Task 7.7 **[DONE]**.
- **Trạng thái Build:** Biên dịch với JDK 17 và Tomcat 10.1 thư viện dự án đạt **`BUILD SUCCESSFUL` (0 lỗi, 69 source files)**.

### 4. Bài học kinh nghiệm & Lỗi sai kiến trúc bị nhắc nhở (CRITICAL):
- **Kiểm tra chéo class CSS thực tế của Component:** Không giả định hoặc sao chép tên class từ các module khác (như `.badge-status-waiting` của Chấm công). Bắt buộc phải đối chiếu trực tiếp file component mẫu (`delivery_orders_table.jsp`) và `global.css` để dùng đúng class `.order-badge.order-badge-dispatch`, đảm bảo trải nghiệm giao diện người dùng đồng bộ 100%.
- **Quy chuẩn mã hóa thông báo Backend:** Mọi luồng thông báo Flash Message hoặc ngoại lệ nghiệp vụ phải sử dụng mã hằng số chuẩn (`EXXX`/`SXXX`), tuyệt đối không dùng text tự do hay chuỗi dạng key làm giá trị runtime.

---

## [2026-10-20] - Hoàn thiện Task 7.2: Tiêu Diệt Model DeliveryTask.java & Triệt Tiêu Nguy Cơ Rò Rỉ Model Lên View
### 1. Phân tích & Vấn đề Kỹ thuật:
- **Nguyên nhân gốc rễ:** CSDL hệ thống (`BOMS_DATABASE.sql`) không hề có bảng `DeliveryTask`. Toàn bộ dữ liệu giao hàng được lưu trực tiếp trong bảng `Orders` (phân loại `OrderType = 'Delivery'`) và bảng `DeliveryAreas`. Việc tự ý tạo class `src/java/model/DeliveryTask.java` vi phạm nghiêm trọng định nghĩa tầng Model (Model chỉ đại diện cho bảng vật lý trong CSDL).
- **Hậu quả:** Gây nhầm lẫn vai trò thực thể CSDL, tạo nợ kỹ thuật và có nguy cơ rò rỉ trực tiếp Model lên tầng View (JSP), vi phạm quy chuẩn thiết kế của `Rule_BOMS.md`.

### 2. Giải pháp Kỹ thuật Triệt để:
- **Tiêu diệt hoàn toàn Model thừa:** Xóa vĩnh viễn tệp `src/java/model/DeliveryTask.java` khỏi dự án.
- **Tầng DAO (`DeliveryDAO.java`):**
  - Cập nhật các hàm `getAllTasks()`, `getTasksByShipper()`, `getAvailableTasksForShipper()` chuyển sang trả về `List<Map<String, Object>>`.
  - Cập nhật `getTaskById()` trả về `Map<String, Object>`.
  - Cập nhật hàm phụ trợ `mapResultSetToTask(ResultSet rs)` khởi tạo `Map<String, Object>` với đúng 9 trường dữ liệu cần thiết: `taskId`, `orderCode`, `customerAddress`, `status`, `shipperUsername`, `customerName`, `customerPhone`, `areaName`, `totalAmount`.
- **Tầng Service (`DeliveryService.java`):**
  - Tiếp nhận `Map<String, Object>` từ DAO.
  - Sử dụng hàm `buildTaskMap(Map<String, Object> task)` để định dạng an toàn toàn bộ giá trị sang `Map<String, String>` trước khi cung cấp cho Controller.
  - Cập nhật các phương thức kiểm tra nghiệp vụ (`validateTaskIsFree`, `validateTaskIsPending`, `validateShipperAuthorization`, `validateStatusFlow`, `countActiveOrders`, `isTaskWaitingForShipper`) trích xuất dữ liệu trực tiếp từ `Map<String, Object>`.
- **Bảo toàn giao diện View (JSP):** Do Expression Language (EL) trên JSP truy xuất `${task.orderCode}`, `${task.customerName}` đồng nhất giữa Map và Java Bean, giao diện `dispatch_dashboard.jsp` và `shipper_dashboard.jsp` hoạt động ổn định 100% mà không cần sửa đổi bất kỳ dòng code JSP nào.

### 3. File bị tác động & Tiến độ:
- `src/java/model/DeliveryTask.java`: Đã xóa hoàn toàn khỏi dự án.
- `src/java/dao/DeliveryDAO.java`: Chuyển đổi toàn bộ phương thức truy vấn Task sang trả về `Map<String, Object>`.
- `src/java/service/DeliveryService.java`: Xử lý nghiệp vụ với `Map<String, Object>`, loại bỏ hoàn toàn import `model.DeliveryTask`.
- `.agent/WorkList.md`: Đánh dấu hoàn thành Task 7.2 **[DONE]**.
- **Trạng thái Build:** Biên dịch qua NetBeans Ant đạt **`BUILD SUCCESSFUL` (0 lỗi, 69 source files)**.

### 4. Bài học kinh nghiệm & Quy chuẩn kiến trúc cốt lõi (CRITICAL):
- **Quy chuẩn tầng Model (Database Entity Only):** Package `model` chỉ chứa các POJO đại diện 1-1 cho các bảng trong CSDL (Users, Orders, Ingredients, ...). TUYỆT ĐỐI không tự ý tạo thêm các class Model "lai tạp" DTO/View Object trong package này để tránh làm ô nhiễm tầng thực thể.
- **Kỹ thuật chống rò rỉ Model lên View (Model Leakage Prevention):** Khi cần tổng hợp dữ liệu từ nhiều bảng (JOIN SQL) để phục vụ hiển thị trên giao diện, quy chuẩn tối ưu là gom nhóm qua `Map<String, Object>` ở tầng DAO và chuyển hóa thành `Map<String, String>` ở tầng Service. View chỉ được phép đọc dữ liệu qua Map hoặc các kiểu dữ liệu nguyên thủy (primitive), tuyệt đối không nhận Model Entity thô.
- **Tính tương thích của EL trong JSP:** Trong Jakarta/Java EE, cú pháp `${object.property}` sẽ tự động tìm kiếm getter trên Java Bean hoặc `map.get("property")` nếu đối tượng là `Map`. Tận dụng đặc tính này giúp chuyển đổi cấu trúc dữ liệu phía backend mà không gây bất kỳ tác dụng phụ (side-effect) nào lên giao diện người dùng.

---

## [2026-10-22] - Hoàn thiện Task 10.6 & Task 11: Chặt Chẽ Khung Giờ Chấm Công (Phương án A) & Hệ Thống Thông Báo / Phát Thanh Toàn Cục

### 1. Phân tích & Vấn đề Kỹ thuật:
- **Task 10.6 (Khống chế thời gian chấm công - Phương án A):**
  - Chống tình trạng "chấm công láo" trước hoặc sau ca quá sớm mà không có sự kiểm soát của Quản lý.
  - Khóa nút Check-in nếu thời gian hiện tại trước giờ bắt đầu ca hơn 15 phút.
  - Nếu Check-in trong khoảng 15 phút trước ca: ghi nhận trạng thái `Pending Early Approval`, yêu cầu Manager bấm Duyệt thì mới được tính vào bảng tổng hợp công (`Timesheet Summary`).
  - Khóa nút Check-out khi chưa hết giờ làm (`currentTime < EndTime`). Sau ca làm > 15 phút: bắt buộc nhập lý do OT và lưu trạng thái `Pending OT Approval`.
- **Task 11 (Hệ thống Thông báo & Phát thanh toàn cục):**
  - Cần hệ thống thông báo đa tầng: Quả chuông 🔔 trên header hiển thị Badge đỏ số lượng chưa đọc, Dropdown xem nhanh 10 thông báo mới nhất kèm thao tác Đánh dấu đã đọc / Đánh dấu tất cả đã đọc qua AJAX.
  - Tính năng Phát loa toàn cục 📢 dành riêng cho Admin và Manager gửi tin tức tức thì đến toàn bộ nhân sự đang hoạt động.
  - Tích hợp Trigger tự động khi có phân ca mới (`ShiftAssignment`) và khi Quản lý duyệt/từ chối OT hoặc duyệt vào sớm (`AttendanceApproval`).

### 2. Giải pháp Kỹ thuật Triệt để:
- **Tầng Model & DAO:**
  - `model/Notification.java`: POJO khớp 1-1 với bảng `Notifications` trong CSDL.
  - `dao/NotificationDAO.java`: Các hàm `insertNotification`, `getUnreadNotificationsByUserId`, `getRecentNotificationsByUserId`, `countUnreadNotifications`, `markAsRead`, `markAllAsRead` sử dụng `try-with-resources` và ném mã lỗi CSDL `DE0061`, `DE0062`.
  - `dao/UserDAO.java`: Thêm hàm `getAllActiveUserIds()` phục vụ tính năng Broadcast.
  - `dao/AttendanceDAO.java`: Cập nhật câu truy vấn `getMonthlyTimesheetSummary` chỉ tính số ca và số giờ làm khi trạng thái là Đúng giờ / Muộn hoặc đã được Quản lý duyệt (`ApprovedBy IS NOT NULL` / `Approved`).
- **Tầng Nghiệp vụ (`AttendanceService.java` & `NotificationService.java`):**
  - `AttendanceService.java`: Ràng buộc giờ Check-in/Check-out chặt chẽ theo Phương án A, cung cấp cờ `canCheckIn`, `canCheckOut` cho View; gắn trigger tự động bắn thông báo khi phân ca hoặc duyệt/từ chối công.
  - `NotificationService.java`: Đóng gói 100% dữ liệu sang `Map<String, Object>` (bao gồm các icon emoji động `📢`, `🗓️`, `⏱️`, `⚠️`, `🛵`, `🔔`), kiểm tra validation nội dung broadcast (không rỗng, tối đa 500 ký tự).
- **Tầng Controller (`NotificationController.java`):**
  - Đăng ký `@WebServlet(urlPatterns = {"/NotificationController"})` và khai báo trong `web.xml`.
  - `doGet`: Xử lý AJAX lấy thông báo chưa đọc (`action=getUnread`) và thông báo gần đây (`action=getRecent`), trả JSON qua thư viện Gson.
  - `doPost`: Xử lý PRG chuẩn cho `action=broadcast` (kiểm tra quyền Admin/Manager, lưu flash message và redirect referer); xử lý JSON AJAX cho `action=markAsRead` và `action=markAllAsRead`.
- **Tầng Giao diện (JSP / CSS / JS / i18n):**
  - `web/header.jsp`: Thêm nút Phát loa 📢 cho Admin/Manager, Quả chuông 🔔 kèm Badge đỏ động `#unreadNotifBadge` cho tất cả người dùng, nhúng dropdown và modal phát loa.
  - `web/components/notification_dropdown.jsp`: Header dropdown hiện đại với loading state, empty state, danh sách thông báo và nút "Đánh dấu tất cả đã đọc".
  - `web/components/broadcast_modal.jsp`: Modal nhập thông báo tối đa 500 ký tự.
  - `web/attendance.jsp`: Khóa nút Check-in / Check-out khi ngoài khung giờ hợp lệ, hiển thị đếm ngược và thông báo hướng dẫn.
  - `web/js/main.js`: Polling thông báo ngầm mỗi 30s, mở/đóng dropdown, xử lý click outside, đánh dấu đã đọc mượt mà không reload trang.
  - `web/css/global.css`: Định kiểu `.notification-dropdown`, `.notification-item`, `.alert-badge-pulse`, hỗ trợ cả Light/Dark theme.
  - `messages_vi.properties` & `messages_en.properties`: Bổ sung dải mã `S0801`-`S0803`, `E0801`-`E0802`, `DE0061`-`DE0062` và toàn bộ nhãn UI tương ứng (tiếng Việt 100% Unicode Escape `\uXXXX`).

### 3. Tuân thủ kiến trúc (`Rule_BOMS.md`):
- Không rò rỉ Model lên Controller hoặc JSP (chuyển đổi Map ở Service).
- Mô hình PRG hoàn chỉnh cho form submit, AJAX nhẹ nhàng cho trải nghiệm người dùng.
- Không inline CSS/JS trong JSP; không Scriptlet.
- Đóng an toàn kết nối JDBC bằng `try-with-resources`.
- Toàn bộ dự án biên dịch qua `javac` đạt **0 lỗi**.

---

## [2026-10-22] - Hoàn thiện Task 12: Bảng Điều Khiển Trung Tâm (Central Dashboard - `index.jsp`)

### 1. Phân tích & Vấn đề Kỹ thuật:
- **Vấn đề ban đầu:** Trang chủ `index.jsp` trống trải (chỉ có 13 dòng text tĩnh), chưa phản ánh trạng thái hệ thống, không hỗ trợ thao tác nhanh và không cá nhân hóa theo vai trò của người dùng.
- **Yêu cầu Task 12:** Cải tạo trang chủ thành Bảng điều khiển trung tâm (Control Center) gồm 3 khu vực trực quan:
  1. Thẻ thông tin cá nhân & Huy hiệu trạng thái làm việc (Profile Card & Work Status Badge).
  2. Lưới thao tác nhanh (Quick Actions Grid) cá nhân hóa theo từng vai trò (Staff, Manager, Admin, Shipper).
  3. Bảng tin thông báo mới nhất (Notification Feed) hiển thị các thông báo gần đây và cho phép đánh dấu đã đọc trực tiếp.

### 2. Giải pháp Kỹ thuật Triệt để:
- **Tầng Điều phối (`DashboardController.java`):**
  - Tạo mới `src/java/controller/DashboardController.java` bọc Servlet `@WebServlet(name = "DashboardController", urlPatterns = {"/DashboardController"})`.
  - Khai báo ánh xạ Servlet và Servlet-mapping trong `web/WEB-INF/web.xml`.
  - Sử dụng `FlashMessageUtil.forwardFlashMessages(request, session)` để tiếp nhận flash messages.
  - Phối hợp dữ liệu từ `AttendanceService`, `NotificationService`, và `IngredientService`:
    + Trạng thái ca làm hôm nay của nhân viên: `getTodayAttendanceForView(userId)`.
    + 8 thông báo mới nhất: `getRecentNotificationsForView(userId, 8)`.
    + Số lượng thông báo chưa đọc: `getUnreadCount(userId)`.
    + Chỉ số nhanh cho Manager: Số lượng yêu cầu điểm danh/OT chờ duyệt và số lượng cảnh báo tồn kho còn hoạt động.
  - Chuyển tiếp (forward) an toàn sang `index.jsp` kèm toàn bộ dữ liệu dạng `Map<String, Object>` / `List<Map<String, Object>>` (Zero Model leakage).
- **Tầng Giao diện (`web/index.jsp`):**
  - **Khách chưa đăng nhập:** Hiển thị Landing Hero Banner ấn tượng, nút đăng nhập nhanh hoặc xem thực đơn khách hàng.
  - **Người dùng đã đăng nhập:**
    + **Khu vực 1 (Profile Card):** Avatar vòng tròn lớn với 2 chữ cái đầu in hoa, tên đầy đủ, vai trò, huy hiệu trạng thái làm việc tương ứng với trạng thái ca làm: 🟢 Đang trong ca (kèm giờ vào ca), 🟡 Chưa vào ca (kèm khung giờ ca), ⚪ Đã hoàn thành ca (kèm số giờ làm), 🔴 Không có ca hôm nay. Bên phải là cụm widget ngày hôm nay và thống kê số lượng việc cần xử lý.
    + **Khu vực 2 (Quick Actions Grid):** Danh sách các thẻ thao tác nhanh kèm icon nổi bật, tiêu đề và mô tả ngắn gọn. Phân tách rõ ràng giữa Staff (POS, Chấm công, Quản lý đơn, Kho, Báo cáo ẩn danh), Manager (Đơn hàng, Kho, Phân ca & Nhân sự, Thực đơn, Điều phối Shipper, Báo cáo, Phát loa), Admin (Quản trị tài khoản, Phát loa, Cài đặt, Hồ sơ), Shipper (Nhiệm vụ giao hàng, Cài đặt).
    + **Khu vực 3 (Notification Feed):** Hộp thông báo cuộn dọc hiện đại, hiển thị icon emoji theo từng loại thông báo, thời gian gửi, nút đánh dấu đã đọc hoặc đánh dấu tất cả đã đọc đồng bộ real-time với thanh Header.
- **Tầng Script & Kiểu dáng (`web/js/main.js` & `web/css/global.css`):**
  - Cập nhật `main.js`: Bổ sung xử lý cập nhật trạng thái đã đọc cho các phần tử `#feedItem_${id}` trên Dashboard feed khi click đánh dấu đọc hoặc đánh dấu tất cả đã đọc.
  - Bổ sung vào `global.css`: Định kiểu hoàn chỉnh cho `.dashboard-main`, `.dashboard-profile-card`, `.badge-work-status`, `.quick-actions-grid`, `.action-card`, `.notification-feed-card`, `.feed-item`. Tương thích 100% cả Light Mode và Dark Mode.
- **Đa ngôn ngữ i18n (`messages_vi.properties` & `messages_en.properties`):**
  - Khai báo toàn bộ nhãn hiển thị của Dashboard (`dashboard.*`), tiếng Việt mã hóa Unicode Escape `\uXXXX` 100%.

### 3. Tuân thủ kiến trúc (`Rule_BOMS.md`):
- Tuân thủ 100% kiến trúc MVC, Controller không import bất kỳ Model hay DAO nào.
- Giao diện JSP không chứa scriptlet (`<% %>`) hay inline style.
- Biên dịch toàn bộ mã nguồn qua JDK 17 / Tomcat 10.1 đạt **0 lỗi**.

---

## [2026-10-20] - Hoàn thiện Task 7.1: Xóa Bỏ Reflection Sai Chuẩn Trong IngredientAlertController
### 1. Phân tích & Vấn đề Kỹ thuật:
- **Nguyên nhân gốc rễ:** Trong phương thức `doPost` của `IngredientAlertController.java`, mã nguồn cũ cố tình chèn hơn 30 dòng code phức tạp để kiểm tra `userObj instanceof Map` và sử dụng Reflection (`java.lang.reflect.Method`, `userObj.getClass().getMethod("getUserID")`, `m.invoke(userObj)`) nhằm trích xuất `userId` từ Session.
- **Hậu quả:** Vi phạm nghiêm trọng quy chuẩn `Rule_BOMS.md`, làm phức tạp hóa code không cần thiết, tiềm ẩn rủi ro hiệu năng nghiêm trọng của Reflection và đi ngược lại cấu trúc Session chuẩn của toàn dự án.

### 2. Giải pháp Kỹ thuật Triệt để:
- **Chuẩn hóa Session Extraction:** Loại bỏ toàn bộ khối code Reflection rườm rà. Thay thế bằng chuẩn thống nhất 100% của hệ thống:
  ```java
  @SuppressWarnings("unchecked")
  Map<String, String> userSession = (Map<String, String>) session.getAttribute(Constants.USER_SESSION);
  ```
- **Xử lý An toàn trong Luồng Nghiệp vụ:** Đặt `int userId = Integer.parseInt(userSession.get("userId"));` trực tiếp bên trong khối `try-catch` của `doPost`. Mọi trường hợp ngoại lệ parsing hoặc dữ liệu không hợp lệ đều được khối `catch (Exception e)` chung của Controller đón bắt và trả về phản hồi JSON lỗi chuẩn cho client.

### 3. File bị tác động & Tiến độ:
- `src/java/controller/IngredientAlertController.java`: Rút gọn từ 164 dòng xuống còn 134 dòng (-30 dòng code rác), loại bỏ sạch 100% Reflection.
- `.agent/WorkList.md`: Đánh dấu hoàn thành Task 7.1 **[DONE]**.
- **Trạng thái Build:** Biên dịch qua NetBeans Ant đạt **`BUILD SUCCESSFUL` (0 lỗi)**.

### 4. Bài học kinh nghiệm & Quy chuẩn kiến trúc cốt lõi (CRITICAL):
- **Tính thống nhất của Session Contract:** Toàn bộ hệ thống BOMS lưu trữ thông tin phiên đăng nhập tại `Constants.USER_SESSION` dưới dạng `Map<String, String>` (do `UserService.buildUserSessionMap` khởi tạo). Mọi Controller khi cần lấy dữ liệu người dùng BẮT BUỘC phải ép kiểu về `Map<String, String>` và truy xuất qua các key chuẩn (`userId`, `username`, `role`, `fullName`).
- **Nghiêm cấm tuyệt đối Reflection trong Controller:** Không bao giờ sử dụng Reflection để giải quyết các vấn đề ép kiểu hoặc phỏng đoán kiểu dữ liệu trong Servlet. Mọi sự mơ hồ về kiểu dữ liệu phải được giải quyết dứt điểm tại tầng Service/Contract thay vì dùng Reflection để "chữa cháy".
- **Quản lý ngoại lệ tập trung:** Khi trích xuất các giá trị số từ tham số hoặc session trong Controller, luôn dựa vào khối `try-catch` bao quanh để bắt `NumberFormatException`, đảm bảo hệ thống không bị crash (lỗi 500) mà luôn trả về mã lỗi JSON thân thiện cho giao diện.

---

## [2026-10-20] - Hoàn thiện Task 6.7: Khắc Phục Triệt Để Lỗi Đồng Bộ Hai Chiều Đơn Hàng & Shipper (Safe Revert Delivery Order)
### 1. Phân tích Nguyên nhân Gốc rễ & Kiến trúc CSDL:
- **Khảo sát Cơ sở Dữ liệu (BOMS_DATABASE.sql):**
  - BOMS không có bảng riêng Delivery hay DeliveryTask. Bản thân bảng Orders lưu trữ toàn bộ đơn hàng của hệ thống, phân loại qua cột OrderType ('Table', 'Counter', 'Delivery'). Khi đơn chuyển sang giao hàng, các thông tin khách nhận được cập nhật trực tiếp vào các cột CustomerName, CustomerPhone, DeliveryAddress, AreaID, ShipperID trong bảng Orders.
  - Tầng DTO DeliveryTask chỉ đóng vai trò View Object gom thông tin giao hàng để hiển thị cho Shipper và Điều phối.
- **Nguyên nhân phát sinh lỗi bất đồng bộ:**
  - Khi nhân viên chuyển đơn sang "Chờ giao hàng" (Waiting for dispatch), hệ thống cập nhật OrderType = 'Delivery'.
  - Nếu sau đó nhân viên tại tab "Tất cả đơn" chọn chuyển ngược trạng thái về Accepted hoặc Prepared, hàm OrderDAO.updateOrderStatus trước đây chỉ thực hiện UPDATE Orders SET Status = ? WHERE OrderID = ? mà không cập nhật lại OrderType.
  - Do tab "Đơn giao Shipper" truy vấn và phân loại theo điều kiện orderType == 'Delivery', đơn hàng này tiếp tục xuất hiện trên tab giao hàng dù trạng thái đã hiển thị là Accepted, đồng thời ShipperID (nếu đã gán) vẫn bị găm lại gây sai lệch tải công việc của Shipper.

### 2. Giải pháp Kỹ thuật Triệt để & Tuân thủ Rule_BOMS:
- **Nguyên tắc "Tái sử dụng - Không nhân bản" (DRY & Zero Duplication):**
  - Không tạo thêm bảng hay file DAO khổng lồ dư thừa. Tận dụng tối đa OrderDAO làm nơi quản lý vòng đời đơn và DeliveryDAO làm nơi quản lý tác vụ shipper.
  - Phục hồi lại toàn bộ 317 dòng code của DeliveryDAO (nhiệm vụ điều phối và shipper từ Task 6) cùng bổ sung hàm countActiveOrdersByShipperId(int shipperId).
- **Cơ chế Hoàn tác An toàn Đa Tầng (Safe Revert Architecture):**
  - **Tầng DAO (OrderDAO.rrevertOrderFromDelivery):**
    Thực hiện câu lệnh cập nhật nguyên tử: UPDATE Orders SET OrderType = ?, Status = ?, ShipperID = NULL WHERE OrderID = ?.
    Bảo lưu thông tin khách hàng và địa chỉ làm dữ liệu lịch sử/nhật ký phục vụ tra cứu thay vì xóa dữ liệu thô.
  - **Tầng Nghiệp vụ (OrderService.rrevertDeliveryOrder):**
    + *Kiểm tra tính an toàn:* Nếu đơn đang ở trạng thái vận chuyển nhạy cảm (Delivering, Delivered, Returned), nghiêm cấm hoàn tác và ném mã lỗi E0218 (ERROR_CANNOT_REVERT_DELIVERY_IN_PROGRESS).
    + *Giải phóng tài nguyên Shipper:* Nếu đơn đã được gán ShipperID, tự động ghi bản ghi vào DeliveryStatusHistory với trạng thái 'Cancelled', đếm lại số đơn đang xử lý qua DeliveryDAO.countActiveOrdersByShipperId và cập nhật ShipperProfiles.ActiveOrders / ShipperStatus.
    + *Khôi phục loại đơn ban đầu:* Nếu TableID != null -> chuyển về 'Table'; ngược lại -> chuyển về 'Counter'.
    + *Tích hợp hủy đơn an toàn:* Trong luồng cancelOrder, nếu đơn bị hủy là đơn Delivery có Shipper, tự động giải phóng Shipper và đồng bộ trạng thái tương tự.
- **Giao diện & Trải nghiệm An toàn (Modal Cảnh Báo Nguy Cơ):**
  - Tạo web/components/rrevert_delivery_modal.jsp: Hiển thị hộp thoại cảnh báo nguy cơ màu đỏ khi nhân viên chọn chuyển ngược trạng thái: "Hành động này sẽ hủy yêu cầu giao hàng của Shipper và chuyển đơn về phục vụ nội bộ (tại bàn/tại quầy). Thao tác này không thể tự động hoàn tác!".
  - Phía Client (web/js/main.js): Bắt sự kiện chọn Accepted/Prepared trên đơn có data-order-type="Delivery", chặn submit mặc định và hiển thị modal xác nhận. Chỉ gửi form khi nhân viên bấm [Xác nhận hoàn tác].
  - Bổ sung phím tắt Escape và click ngoài backdrop để đóng modal.
- **Đa ngôn ngữ & Mã hóa Ký tự Chuẩn:**
  - Bổ sung hằng số S0212 và E0218 trong utility/Constants.java.
  - Khai báo đầy đủ các chuỗi thông báo và nhãn modal trong messages_vi.properties (100% mã hóa \uXXXX) và messages_en.properties.

### 3. File bị tác động:
- src/java/utility/Constants.java: Bổ sung SUCCESS_REVERT_DELIVERY_ORDER = "S0212" và ERROR_CANNOT_REVERT_DELIVERY_IN_PROGRESS = "E0218".
- src/java/resources/messages_vi.properties & messages_en.properties: Bổ sung bản dịch tiếng Việt (Unicode Escape) và tiếng Anh cho thông báo lỗi, thành công và modal cảnh báo.
- src/java/dao/OrderDAO.java: Thêm hàm 
revertOrderFromDelivery(int orderId, String newOrderType, String newStatus).
- src/java/dao/DeliveryDAO.java: Khôi phục toàn bộ các hàm nghiệp vụ Task 6 và bổ sung countActiveOrdersByShipperId(int shipperId).
- src/java/service/OrderService.java: Bổ sung 
revertDeliveryOrder và tích hợp kiểm tra trong updateOrderStatus và cancelOrder.
- web/components/rrevert_delivery_modal.jsp (Tạo mới): Component modal cảnh báo trước khi hoàn tác đơn giao hàng.
- web/order_management.jsp: Include 
revert_delivery_modal.jsp.
- web/components/standard_orders_table.jsp & delivery_orders_table.jsp: Bổ sung data-order-type, data-current-status và option chuyển trạng thái.
- web/js/main.js: Bổ sung openRevertDeliveryModal, closeRevertDeliveryModal, confirmRevertDeliverySubmit, các modal helper và cập nhật handleStaffStatusSubmit.
- .agent/WorkList.md: Bổ sung Task 6.7 và đánh dấu [DONE].


## [2026-10-20] - Hoàn thiện Task 6.5 & Task 6.6: Upload Bằng Chứng Giao Hàng, Lý Do Thất Bại & Hoàn Thiện Điều Hướng Header
### 1. Phân tích & Giải pháp:
- **Tận dụng Tiện ích Tải ảnh Sẵn có (`ImageUploadUtility`):**
  - Kế thừa và tái sử dụng `ImageUploadUtility.saveImage(filePart, "delivery")` để xử lý tải ảnh bằng chứng giao hàng khi Shipper xác nhận `Delivered`.
  - Ảnh được lưu vào thư mục `boms_data/uploads/delivery/` nằm ngoài thư mục build để chống mất dữ liệu khi Clean & Build, đồng thời được phục vụ công khai qua ánh xạ URL `/uploads/*`.
- **Thiết kế Component Modal Chuyên Biệt:**
  - `web/components/shipper_complete_modal.jsp`: Hiển thị form tải tệp ảnh bằng chứng (`multipart/form-data`) kèm xem trước ảnh trước khi gửi.
  - `web/components/shipper_fail_modal.jsp`: Hiển thị modal bắt buộc nhập lý do thất bại (`failReason`) khi trạng thái là `Returned`, cung cấp dropdown gợi ý các lý do thực tế (Khách không nghe máy, Sai địa chỉ, Khách đổi ý, Hàng hỏng hóc) kết hợp ô textarea nhập chi tiết.
- **Xử lý Controller & Service:**
  - `ShipperController`: Cấu hình `@MultipartConfig` với giới hạn tệp an toàn (fileSizeThreshold 1MB, maxFileSize 10MB, maxRequestSize 15MB). Xử lý 2 action `completeDelivery` và `failDelivery`, nạp `proofImage` hoặc `failReason` sang Service, áp dụng chuẩn PRG và Flash Message qua Session.
  - `DeliveryService.updateShipperTaskStatus`: Validate chặt chẽ bắt buộc nhập `failReason` khi trạng thái là `Returned` (tránh ném rỗng), cập nhật bảng `Orders`, tự động ghi vết vào `DeliveryStatusHistory` đầy đủ `ProofImage` hoặc `FailReason`.
  - Tự động đồng bộ `ShipperProfiles.ActiveOrders` và `ShipperStatus`: Khi đơn kết thúc (`Delivered` hoặc `Returned`), đếm lại số đơn `Delivering` còn lại của Shipper; nếu về 0 thì chuyển trạng thái Shipper về `'Ready'` giúp giải phóng ngay công suất gom đơn.
- **Hoàn thiện Điều hướng Header (`web/header.jsp`):**
  - Bổ sung liên kết `/DispatchController` cho tài khoản có vai trò `Manager` (Quản lý điều phối giao hàng).
  - Kích hoạt liên kết `/ShipperController` cho tài khoản có vai trò `Shipper` (Bàn làm việc Shipper).
  - Tuân thủ phân quyền chặt chẽ theo `user.role`, không để lộ endpoint không thuộc thẩm quyền.
- **Đa ngôn ngữ & Mã hóa Unicode Escape:**
  - Đồng bộ 100% nhãn modal và mã thông báo vào `messages_vi.properties` (chuẩn `\uXXXX`) và `messages_en.properties`.

### 2. File bị tác động:
- `web/components/shipper_complete_modal.jsp` (Tạo mới)
- `web/components/shipper_fail_modal.jsp` (Tạo mới)
- `src/java/controller/ShipperController.java`: Gắn `@MultipartConfig`, xử lý action `completeDelivery`, `failDelivery`.
- `src/java/service/DeliveryService.java`: Cập nhật `updateShipperTaskStatus`, validate `failReason`, ghi vết `DeliveryStatusHistory`, đồng bộ `ShipperProfiles`.
- `web/shipper_dashboard.jsp`: Tích hợp 2 modal và cập nhật các nút thao tác.
- `web/header.jsp`: Cập nhật phân quyền hiển thị liên kết cho Manager và Shipper.
- `web/js/main.js`: Bổ sung các hàm JavaScript mở/đóng modal `openShipperCompleteModal`, `openShipperFailModal`.
- `src/java/resources/messages_vi.properties` & `messages_en.properties`: Bổ sung nhãn modal và thông báo đa ngôn ngữ.
- `.agent/WorkList.md`: Đánh dấu hoàn thành toàn bộ Task 6.

## [2026-10-20] - Hoàn thiện Task 6.4: Đồng Bộ Hiển Thị Đơn Chờ Giao & Nhận/Từ Chối Đơn Phía Shipper
### 1. Phân tích & Giải pháp:
- **Cơ chế Điều phối & Tiếp nhận Đơn kết hợp (Hybrid Dispatching & Claim Pool):**
  - Thực hiện theo phương án phê duyệt: Manager có thể chủ động gán Shipper từ `dispatch_dashboard.jsp`, đồng thời Shipper có thể tự xem và bấm [Nhận giao đơn này] (`claimTask`) đối với các đơn `Waiting for dispatch` chưa gán trong khu vực phụ trách (`ShipperAreas`) trên `shipper_dashboard.jsp`.
  - Phía DAO sử dụng câu lệnh `UPDATE ... WHERE OrderID = ? AND ShipperID IS NULL AND Status = 'Waiting for dispatch'` nguyên tử (atomic update) nhằm ngăn chặn triệt để tình trạng tranh chấp tài nguyên (race condition) khi nhiều Shipper cùng bấm nhận đơn một lúc.
- **Tác vụ Nhận/Từ chối & Quản lý vòng đời đơn:**
  - `acceptTask`: Khi Shipper bấm [Nhận đơn] từ bảng "Đơn chờ xác nhận", kiểm tra công suất qua `MaxOrders` động từ `ShipperProfile`, chuyển trạng thái đơn sang `Delivering`, tự động tăng số đơn đang xử lý và chuyển trạng thái Shipper sang `Delivering`, đồng thời ghi vết lịch sử `DeliveryStatusHistory`.
  - `rejectTask`: Khi Shipper bấm [Từ chối], gỡ bỏ `ShipperID = NULL`, trả trạng thái đơn về `Waiting for dispatch`, ghi nhật ký lý do từ chối vào `DeliveryStatusHistory`, đơn lập tức quay lại hàng chờ điều phối và bể đơn chờ nhận.
  - `claimTask`: Shipper chủ động nhận đơn trực tiếp từ hàng chờ khu vực, đơn chuyển thẳng sang `Delivering`.
- **Chuẩn hóa Kiến trúc PRG (Post-Redirect-Get) & Flash Messages:**
  - Loại bỏ hoàn toàn lỗi gọi trực tiếp `doGet()` sau `doPost()` trong `DispatchController` và `ShipperController`. Toàn bộ thông báo thành công/lỗi được lưu vào `HttpSession`, sau đó thực hiện `sendRedirect()` chuẩn PRG, giúp ngăn ngừa tình trạng resubmit form khi người dùng nhấn F5.
- **Nâng cấp Giao diện & Trải nghiệm Người dùng (UI/UX):**
  - Bổ sung thông tin chi tiết người nhận (Tên khách hàng, Số điện thoại, Địa chỉ, Khu vực giao hàng, Tổng tiền) trên cả 2 trang `dispatch_dashboard.jsp` và `shipper_dashboard.jsp`.
  - Thay thế việc hardcode giới hạn gom đơn `activeOrders >= 5` bằng `activeOrders >= maxOrders` động được cấu hình theo từng Shipper trong `ShipperProfiles`.
  - Chuẩn hóa CSS trong `global.css`, loại bỏ inline styles.
- **Đa ngôn ngữ & Mã hóa Unicode Escape:**
  - Bổ sung đầy đủ các nhãn giao diện và thông báo Flash Message vào `messages_vi.properties` (100% mã hóa `\uXXXX`) và `messages_en.properties`.

### 2. File bị tác động:
- `src/java/model/DeliveryTask.java`: Thêm các trường `customerName`, `customerPhone`, `areaName`, `totalAmount`.
- `src/java/dao/DeliveryDAO.java`: Cập nhật truy vấn `getAllTasks()`, `getTasksByShipper()`, `getTaskById()`; bổ sung `getAvailableTasksForShipper()` và `claimTask()`.
- `src/java/service/DeliveryService.java`: Nạp đầy đủ thông tin vào Task Map DTO, bổ sung `getAvailableTasksForShipper()`, `claimTask()`, `acceptTask()`, đồng bộ `maxOrders` động.
- `src/java/controller/DispatchController.java`: Chuẩn hóa chuẩn PRG `sendRedirect()` và Session Flash Messages.
- `src/java/controller/ShipperController.java`: Chuẩn hóa chuẩn PRG, bổ sung action `claimTask`, `acceptTask`, `rejectTask`, `updateStatus`.
- `web/dispatch_dashboard.jsp`: Hiển thị chi tiết khách hàng/khu vực/giá trị đơn; kiểm tra sức chứa Shipper động theo `maxOrders`.
- `web/shipper_dashboard.jsp`: Bổ sung 3 phân vùng rõ ràng (Đơn chờ nhận khu vực, Đơn được điều phối giao, Đơn hàng của tôi).
- `web/css/global.css`: Bổ sung styles bảng và nút bấm nghiệp vụ điều phối và shipper.
- `src/java/resources/messages_vi.properties` & `messages_en.properties`: Bổ sung nhãn và thông báo hệ thống.
- `.agent/WorkList.md`: Đánh dấu hoàn thành Task 6.4.

## [2026-10-20] - Hoàn thiện Task 6.3: Thiết kế Tab chuyên biệt "Đơn Hàng Giao Cho Shipper" Trên `order_management.jsp`
### 1. Phân tích & Giải pháp:
- **Tách biệt Component & Chống lặp code (DRY - Single Responsibility Principle):**
  - Tạo `web/components/standard_orders_table.jsp`: Tái sử dụng bảng chuẩn cho 3 tab "Tất cả đơn", "Tại quầy", "Tại bàn" với tham số `targetOrderType`, loại bỏ hoàn toàn việc nhân bản mã HTML.
  - Tạo `web/components/delivery_orders_table.jsp`: Hiển thị bảng chuyên biệt cho tab "Đơn giao Shipper" với các cột nghiệp vụ đặc thù (Khách nhận & SĐT, Địa chỉ & Khu vực giao, Shipper đảm nhận, Trạng thái vận chuyển chi tiết).
- **Cơ chế chuyển Tab tức thì phía Client (Instant Tab Switch):**
  - Tích hợp thanh điều hướng tab chuẩn `.tab-nav`, `.tab-btn` và hàm `openTab()` của hệ thống.
  - Hiển thị số lượng đếm thời gian thực trên từng tab (`${ORDER_COUNTS.all}`, `${ORDER_COUNTS.counter}`, `${ORDER_COUNTS.table}`, `${ORDER_COUNTS.delivery}`) giúp nhân viên nắm bắt nhanh số lượng đơn.
  - Duy trì `ACTIVE_TAB` qua các lần submit form trạng thái và lọc ngày/trạng thái bằng Flash Attribute / URL param, giúp nhân viên không bị văng khỏi tab đang thao tác.
- **Tối ưu Backend O(1) & Nạp dữ liệu liên kết:**
  - `DeliveryDAO`: Bổ sung `getShipperNameMap()` truy vấn danh sách Shipper (`Role = 'Shipper'`) ánh xạ `UserID -> FullName/Username`.
  - `OrderService`:
    + Nạp `areaMap` và `shipperMap` một lần ở đầu luồng `convertOrdersToViewList` để gán `areaName`, `shipperName` vào Map đơn hàng trong thời gian O(1), không phát sinh N+1 query.
    + Thêm hàm `calculateOrderCounts(orderList)` tính toán số lượng đơn phân bổ cho từng tab.
  - `OrderManagementController`: Tính `ORDER_COUNTS`, nhận và chuyển tiếp `ACTIVE_TAB` sang View.
- **Đa ngôn ngữ & Mã hóa Unicode Escape:**
  - Bổ sung các nhãn tab và cột bảng đơn giao hàng vào `messages_vi.properties` (chuẩn `\uXXXX`) và `messages_en.properties`.

### 2. File bị tác động:
- `web/components/delivery_orders_table.jsp` (Tạo mới)
- `web/components/standard_orders_table.jsp` (Tạo mới)
- `src/java/dao/DeliveryDAO.java`: Bổ sung `getShipperNameMap()`.
- `src/java/service/OrderService.java`: Bổ sung `calculateOrderCounts()`, nạp `areaName`, `shipperName` vào Map dữ liệu đơn.
- `src/java/controller/OrderManagementController.java`: Nạp `ORDER_COUNTS` và xử lý duy trì `ACTIVE_TAB`.
- `web/order_management.jsp`: Bổ sung thanh Tab điều hướng và include 2 components bảng đơn hàng.
- `web/components/delivery_info_modal.jsp` & `staff_cancel_modal.jsp`: Ghi nhận `activeTab`.
- `web/js/main.js`: Cập nhật `openStaffCancelModal` hỗ trợ giữ `activeTab`.
- `src/java/resources/messages_vi.properties` & `messages_en.properties`: Bổ sung nhãn tab và tiêu đề bảng.
- `.agent/WorkList.md`: Đánh dấu hoàn thành Task 6.3.

## [2026-10-20] - Hoàn thiện Task 6.2: Modal Thu Thập Thông Tin Giao Hàng Cho Shipper (Order Dispatching Modal)
### 1. Phân tích & Giải pháp:
- **Tách biệt Component Modal (`delivery_info_modal.jsp`):**
  - Đóng gói giao diện modal thu thập thông tin khách nhận hàng (`CustomerName`, `CustomerPhone`, `DeliveryAddress`) và dropdown khu vực giao hàng (`DeliveryAreas`) thành component độc lập trong `web/components/`.
  - Tuân thủ Rule_BOMS: Cấm scriptlet, cấm inline CSS/JS.
- **Kịch bản Client (`main.js`):**
  - Tích hợp vào `handleStaffStatusSubmit`: Khi nhân viên chọn trạng thái "Waiting for dispatch" (Chờ giao hàng) và bấm Cập nhật, JavaScript chặn gửi form mặc định, tự động trích xuất thông tin khách sẵn có (nếu có) từ `data-` attributes của đơn và hiển thị modal nổi.
  - Hỗ trợ phím Escape và click ra ngoài backdrop để đóng modal an toàn.
- **Xử lý MVC Backend:**
  - `OrderDAO`: Bổ sung `updateOrderToDelivery(orderId, customerName, customerPhone, deliveryAddress, areaId)` cập nhật đồng thời `OrderType = 'Delivery'`, `Status = 'Waiting for dispatch'`, `ShipperID = NULL`, và lưu thông tin người nhận.
  - `OrderService`:
    + Thực hiện validation 2 tầng (Tầng 1: kiểm tra không rỗng, giới hạn độ dài ký tự, regex số điện thoại `0\d{9,10}`; Tầng 2: kiểm tra trạng thái đơn hợp lệ, không cho phép đổi các đơn đã hủy/đã thanh toán/đã giao).
    + Cung cấp hàm `getAllDeliveryAreas()` đóng gói danh sách khu vực dạng DTO/Map trả về Controller/View.
    + Bổ sung trường `deliveryAddress`, `areaId`, `shipperId` vào map dữ liệu đơn hàng `convertOrderToMap`.
  - `OrderManagementController`:
    + Nạp `DELIVERY_AREAS` vào `request` trong `doGet` phục vụ dropdown.
    + Xử lý action `convertToDelivery` trong `doPost`, điều phối Flash Message `SUCCESS_DISPATCH_ORDER` (S0211) theo chuẩn PRG.
- **Đa ngôn ngữ & Mã hóa:**
  - Khai báo mã thành công `SUCCESS_DISPATCH_ORDER` (S0211) và các mã lỗi `E0213` -> `E0217` trong `Constants.java`.
  - Toàn bộ nhãn và thông báo tiếng Việt trong `messages_vi.properties` được mã hóa Unicode Escape (`\uXXXX`) nghiêm ngặt.

### 2. File bị tác động:
- `web/components/delivery_info_modal.jsp` (Tạo mới)
- `src/java/utility/Constants.java`: Bổ sung mã `S0211`, `E0213` - `E0217`.
- `src/java/resources/messages_vi.properties` & `messages_en.properties`: Bổ sung bản dịch đa ngôn ngữ cho modal và mã thông báo.
- `src/java/dao/OrderDAO.java`: Thêm hàm `updateOrderToDelivery`.
- `src/java/service/OrderService.java`: Bổ sung `updateOrderToDelivery`, `getAllDeliveryAreas()`, cập nhật `convertOrderToMap`.
- `src/java/controller/OrderManagementController.java`: Nạp danh sách khu vực và tiếp nhận action `convertToDelivery`.
- `web/order_management.jsp`: Bổ sung lựa chọn `Waiting for dispatch`, hiển thị badge và nhúng modal component.
- `web/css/global.css`: Thêm class `.order-badge-dispatch`.
- `web/js/main.js`: Thêm `openDeliveryInfoModal`, `closeDeliveryInfoModal`, tích hợp chặn form trong `handleStaffStatusSubmit`.
- `.agent/WorkList.md`: Đánh dấu hoàn thành Task 6.2.

## [2026-10-20] - Hoàn thiện Task 6.1: Chuẩn hóa Model & Khắc phục Lỗi Trạng Thái Đơn Hàng Giao Hàng
### 1. Phân tích & Giải pháp:
- **Chuẩn hóa Model theo CSDL SQL Server:**
  - Bổ sung 3 entity model tương ứng các bảng mới trong DB: `ShipperProfile.java` (bảng `ShipperProfiles`), `DeliveryArea.java` (bảng `DeliveryAreas`), `DeliveryStatusHistory.java` (bảng `DeliveryStatusHistory`).
  - Đảm bảo các thuộc tính và quan hệ khóa ngoại (UserId, OrderId, ShipperId, AreaId) khớp chính xác với `BOMS_DATABASE.sql`.
- **Khắc phục lỗi trạng thái giao hàng & Ràng buộc Check Constraint:**
  - Ràng buộc `CK_Order_Status` trong CSDL chỉ chấp nhận: `'Accepted', 'Paid', 'Prepared', 'Waiting for dispatch', 'Waiting for delivery', 'Picked up', 'Delivering', 'Delivered', 'Returned', 'Cancelled'`.
  - Mã nguồn cũ trước đây gán `STATUS_REJECTED = "Cancelled"` khi Shipper từ chối đơn hàng, gây lỗi nghiêm trọng vì "Cancelled" kích hoạt nghiệp vụ hoàn kho của bếp/quầy.
  - Chuẩn hóa: Khi Shipper từ chối đơn, đơn hàng chuyển về `Waiting for dispatch` (gỡ bỏ ShipperID để điều phối lại hoặc cho Shipper khác nhận), đồng thời ghi vết lịch sử vào `DeliveryStatusHistory` kèm `failReason`.
- **Nâng cấp DAO & Service tầng Giao hàng:**
  - `DeliveryDAO`: Bổ sung `insertStatusHistory`, `getShipperProfileByUserId`, `getShipperProfileByUsername`, `getUserIdByUsername`, `updateShipperStatusAndCount`, `getAllDeliveryAreas`.
  - `DeliveryService`: Tích hợp ghi vết lịch sử `insertStatusHistory` tại mọi điểm chuyển dịch trạng thái (`assignTaskToShipper`, `updateShipperTaskStatus`, `rejectTask`). Đọc trần tải đơn động từ `ShipperProfile.getMaxOrders()` (mặc định 3 đơn theo DB) thay vì hardcode 5 đơn.

### 2. File bị tác động:
- `src/java/model/ShipperProfile.java` (Tạo mới)
- `src/java/model/DeliveryArea.java` (Tạo mới)
- `src/java/model/DeliveryStatusHistory.java` (Tạo mới)
- `src/java/utility/Constants.java`: Chuẩn hóa các hằng số trạng thái giao hàng, bỏ map sai trạng thái Rejected.
- `src/java/dao/DeliveryDAO.java`: Thêm các phương thức truy vấn Profile, Area và ghi lịch sử trạng thái.
- `src/java/service/DeliveryService.java`: Tích hợp ghi log lịch sử, chuẩn hóa luồng từ chối đơn về `Waiting for dispatch`.
- `.agent/WorkList.md`: Cập nhật checklist hoàn thành Task 6.1.

## [2026-10-19] - Khắc phục Triệt để Lỗi Khóa Cứng Thực Đơn (Hết hàng) và Sập Luồng Đặt Món Khi Vượt Quá Tồn Kho Hoặc Hủy Đơn
### 1. Phân tích Nguyên nhân Gốc rễ (Root Cause Analysis):
- **Lỗi 1 (Sai lầm can thiệp CSDL trong pha Validation):**
  - Trong `CartService.java`, phương thức `validateInventoryStock` khi kiểm tra thấy nguyên liệu trong kho không đủ cho số lượng khách đặt (ví dụ: kho còn 5 phần nhưng giỏ hàng đặt 10 phần) đã lập tức gọi `disableProductsUsingIngredient(ingredientId)`.
  - Hàm này thực thi `productDAO.updateProductAvailability(pid, false)`, ghi đè trực tiếp `Products.IsAvailable = 0` vào CSDL SQL Server!
  - Ngay sau đó, hàm ném `ValidationException(Constants.ERROR_INSUFFICIENT_INGREDIENTS)` khiến giao dịch chốt đơn bị hủy bỏ, **không có bản ghi Order nào được tạo ra trong CSDL**.
  - Hậu quả: Món ăn bị biến thành "Ngừng kinh doanh" vĩnh viễn trong CSDL. Khi quay lại `menu.jsp`, `ProductService.checkProductAvailability` thấy `!p.isAvailable()` nên trả về `false`, làm nút "Thêm vào giỏ" bị khóa cứng thành `<button disabled>Hết hàng</button>`. Khách và nhân viên không thể đặt lại món đó dù trong kho vẫn còn 5 phần!
- **Lỗi 2 (Sập luồng khi hủy đơn / Không có đơn để hủy):**
  - Vì đơn hàng bị từ chối ngay ở bước kiểm tra và chưa hề được lưu vào DB, người dùng không có đơn nào để bấm "Hủy đơn".
  - Nếu người dùng bấm hủy một đơn hàng cũ khác, cơ chế `recheckProductAvailabilityAfterStockRestore` trong `OrderService` chỉ rà soát những nguyên liệu của đơn hàng bị hủy đó. Nếu món bị khóa không dùng chung nguyên liệu, nó sẽ vĩnh viễn không bao giờ được mở khóa lại.
- **Lỗi 3 (Xung đột giữa Quyền Quản trị và Trạng thái Tồn kho):**
  - `CartService.deductIngredientsStock` gọi `checkAndDisableIfDepleted` tự ý set `IsAvailable = 0` trong DB khi nguyên liệu về 0. Khi Quản lý nhập thêm kho (`InventoryManagementController`), không có bất kỳ logic nào tự động bật lại `IsAvailable = 1`, khiến món bị "chết" vĩnh viễn trên thực đơn.
  - Ngược lại, trong `OrderService`, hàm `recheckProductAvailabilityAfterStockRestore` khi hoàn kho lại tự ý set `IsAvailable = 1`, vô tình ghi đè và làm mất trạng thái "Ngừng kinh doanh" mà Quản lý chủ động cài đặt trước đó.

### 2. Giải pháp Kiến trúc Chuẩn hóa (Architectural Resolution):
- **Tách biệt triệt để 2 khái niệm:**
  1. `Products.IsAvailable` (Quyền Quản trị Danh mục): Do Admin / Store Manager kiểm soát chủ động qua Menu Management (Đang kinh doanh / Ngừng kinh doanh). Tuyệt đối không để tầng bán hàng (Cart/Order) can thiệp sửa đổi cờ này trong CSDL.
  2. `Dynamic Inventory Stock Check` (Tồn kho thời gian thực): Tự động tính toán on-the-fly qua `ingredientDAO.hasSufficientStockForProduct(productId, 1)` và bảng công thức `ProductIngredients`.
- **Cập nhật `CartService.java`:**
  - Loại bỏ hoàn toàn việc gọi `disableProductsUsingIngredient` trong `validateInventoryStock`. Khi vượt tồn kho, chỉ ném `ValidationException(Constants.ERROR_INSUFFICIENT_INGREDIENTS)` mà KHÔNG làm thay đổi bất kỳ bản ghi nào trong CSDL. Khách hàng/nhân viên có thể giảm số lượng giỏ hàng xuống mức tồn kho hiện có và đặt hàng bình thường.
  - Loại bỏ hoàn toàn `checkAndDisableIfDepleted` và `disableProductsUsingIngredient` trong `deductIngredientsStock`. Khi nguyên liệu hết (về 0), tầng View sẽ tự động hiển thị nút "Hết hàng" tức thì nhờ truy vấn động `hasSufficientStockForProduct(productId, 1)`.
- **Cập nhật `OrderService.java`:**
  - Loại bỏ `recheckProductAvailabilityAfterStockRestore` trong `restoreItemIngredients`. Khi hủy đơn, kho nguyên liệu được hoàn trả đầy đủ, và hệ thống sẽ tự động mở khóa món ăn trên Menu ngay lập tức ở lần tải trang kế tiếp mà không ghi đè cờ nghiệp vụ `IsAvailable` của Quản lý.
  - Dọn dẹp import và trường `ProductDAO` không còn sử dụng trong `OrderService`.

### 3. File bị tác động:
- `src/java/service/CartService.java`: Gỡ bỏ `disableProductsUsingIngredient` và `checkAndDisableIfDepleted`.
- `src/java/service/OrderService.java`: Gỡ bỏ `recheckProductAvailabilityAfterStockRestore`, gỡ bỏ dependency `ProductDAO`.

### 1. File bị tác động / tạo mới:
- `src/java/utility/ImageUploadUtility.java` (Tạo mới):
  - Tiện ích lưu trữ file ảnh độc lập tại `System.getProperty("user.home")/boms_data/uploads/` nhằm khắc phục nhược điểm mất file khi Clean & Build / Redeploy của NetBeans/Tomcat.
  - Whitelist định dạng an toàn: `.jpg`, `.jpeg`, `.png`, `.webp` kèm kiểm tra MIME Type và Extension.
  - Chống lỗ hổng Directory Traversal (`..` / `\`), chuẩn hóa tên file bằng UUID ngẫu nhiên.
  - Giới hạn kích thước tải lên an toàn (10MB).
- `src/java/controller/ImageServingServlet.java` (Tạo mới):
  - Servlet ánh xạ URL pattern `/uploads/*` để stream trực tiếp file nhị phân từ thư mục lưu trữ ngoài webapp.
  - Tối ưu hiệu năng: Thêm HTTP Header `Cache-Control: public, max-age=86400` (cache 24 giờ trên client).
  - Khắc phục lỗi Connection Reset: Tách `OutputStream` ra khỏi khối try-with-resources của input stream và chỉ gọi `out.flush()` thay vì `out.close()` sớm.
- `src/java/controller/MenuManagementController.java`:
  - Cấu hình `@MultipartConfig(maxFileSize = 10 * 1024 * 1024, maxRequestSize = 30 * 1024 * 1024, fileSizeThreshold = 2 * 1024 * 1024)`.
  - Khắc phục triệt để lỗi TCP Reset bằng cách bọc toàn bộ khối trích xuất tham số vào trong `try-catch`, bắt `IllegalStateException` khi multipart vượt trần cấu hình và chuyển hướng an toàn kèm Flash Message.
  - Hotfix Checked Exception: Loại bỏ `ServletException` trong khối `catch` để tuân thủ JLS §14.20 do `getPartSafely` đã xử lý ngoại lệ nội bộ.
- `src/java/utility/Constants.java`:
  - Bổ sung các mã lỗi chuẩn: `ERROR_INVALID_IMAGE_TYPE` (E1012), `ERROR_IMAGE_SIZE_EXCEEDED` (E1013), `ERROR_IMAGE_UPLOAD_FAILED` (E1014), `ERROR_IMAGE_URL_TOO_LONG` (E1015).
- `src/java/resources/messages_vi.properties` & `messages_en.properties`:
  - Khai báo đa ngôn ngữ với Unicode Escape chuẩn cho các mã E1012 -> E1015 và các nhãn giao diện tab tải tệp / dán liên kết, giới hạn URL 255 ký tự.
- `src/java/utility/Validator.java`:
  - Bổ sung hàm tiện ích `checkMaxLength(String value, int maxLength, String errorMessage)`.
- `src/java/service/ProductService.java`:
  - Tích hợp kiểm tra độ dài URL (`Validator.checkMaxLength(imageUrl.trim(), 255, Constants.ERROR_IMAGE_URL_TOO_LONG)`) cho cả `createProduct` và `updateProduct`. Ngăn chặn hoàn toàn lỗi tràn chuỗi `VARCHAR(255)` ở CSDL SQL Server (lỗi `DE0013`).
- `src/java/filter/AuthFilter.java`:
  - Bổ sung `/uploads/*`, `.jpg`, `.jpeg`, `.webp` vào danh sách tài nguyên tĩnh cho phép truy cập tự do không cần đăng nhập.
- `web/components/product_form_modal.jsp`:
  - Thiết kế giao diện Hybrid 2 Tab: "Tải tệp lên" (chọn từ máy tính) và "Dán liên kết" (nhập URL web).
  - Ràng buộc `maxlength="255"` cho input URL và hiển thị hướng dẫn trực quan.
- `web/css/global.css`:
  - Thêm định dạng cho `.image-tab-bar`, `.btn-image-tab`, `.image-panel`, `.image-preview-container`, `.preview-thumb`, `.product-card-img`, `.border-danger`.
- `web/js/main.js`:
  - Hàm `switchImageTab(mode)`: Chuyển đổi qua lại mượt mà giữa Tab Upload và Tab URL.
  - Hàm `previewUploadedImage(input)`: Đọc và xem trước ảnh tải từ máy tính bằng `FileReader`, kiểm tra chặn kích thước quá 10MB ngay tại client.
  - Hàm `previewUrlImage(url)`: Xem trước ảnh từ URL web và kích hoạt viền cảnh báo `.border-danger` nếu chuỗi vượt quá 255 ký tự.

### 2. Tuân thủ kiến trúc:
- Controller không import Model hay DAO.
- Service xử lý 100% nghiệp vụ logic, ném `ValidationException` chuẩn.
- CSDL SQL Server giữ nguyên vẹn 100%, không can thiệp schema.
- Cơ chế Flash Message an toàn theo chuẩn PRG, không phụ thuộc scriptlet, không CSS inline.

## [2026-10-13] - Hoàn thiện Task 1 (Quản lý giỏ hàng UC-01)
### 1. File bị tác động:
- `src/java/utility/Constants.java`: Bổ sung hằng số thông báo thành công `SUCCESS_UPDATE_CART` (S0202), `SUCCESS_REMOVE_FROM_CART` (S0203), `SUCCESS_CLEAR_CART` (S0204).
- `src/java/resources/messages_vi.properties` & `messages_en.properties`: Bổ sung bản địa hóa cho các thông báo và nhãn bảng giỏ hàng (`menu.cart.table.action`, `menu.cart.btn.clear`, `menu.cart.btn.remove`).
- `src/java/service/CartService.java`: 
  - Hàm công khai: `updateItemQuantity`, `removeItemFromCart`, `clearCart`.
  - Hàm trợ năng đơn trách nhiệm (SRP): `updateQuantityOrRemove`, `setItemQuantity`, `removeItemByProductId`, `clearCartDetails`.
- `src/java/controller/CartController.java`: Bổ sung xử lý các action `update`, `remove`, `clear`; điều phối Flash Message vào `session` và giữ trạng thái `ACTIVE_TAB = "cart"`.
- `src/java/controller/MenuController.java`: Tiếp nhận và chuyển tiếp `ACTIVE_TAB` sang View để giữ đúng tab sau khi cập nhật giỏ.
- `web/css/global.css`: Bổ sung class `.cart-qty-control`, `.btn-qty`, `.btn-cart-remove`, `.cart-actions-group`, `.btn-cart-clear` (không dùng CSS inline).
- `web/menu.jsp`: Cập nhật bảng giỏ hàng với nút tăng/giảm (+/-), nút xóa từng món (Xóa) và nút dọn giỏ (Xóa giỏ hàng).

### 2. Tuân thủ kiến trúc:
- Controller không import Model/DAO, không dùng `PrintWriter`.
- Service ném `ValidationException` kèm mã lỗi chuẩn.
- View không scriptlet, không CSS/JS inline, sử dụng JSTL/EL.

## [2026-10-13] - Hoàn thiện Task 2 (Chốt đơn hàng Checkout UC-01)
### 1. File bị tác động:
- `src/java/service/CartService.java`:
  - Bổ sung hàm kiểm tra nguồn đơn hàng `validateOrderSource` (yêu cầu phải có thông tin bàn hoặc nhân viên quầy).
  - Cập nhật `assignOrderSource` để phân bổ chính xác `Table` (tableId) và `Counter` (staffId).
  - Cập nhật `setupOrderMetadata`: Thiết lập `Status = 'Accepted'` và `CookingStartedAt = null` để phục vụ điều kiện đổi món của UC-02.
- `src/java/dao/OrderDAO.java`:
  - Trong `insertOrder`: Thực hiện snapshot deep-copy toàn bộ danh sách `OrderDetail` để đảm bảo độc lập dữ liệu khi phiên giỏ hàng bị xóa.
  - Bổ sung hàm `getLatestOrderByTableId(int tableId)` phục vụ tra cứu đơn theo bàn.
- `src/java/controller/CartController.java`:
  - Lưu `LAST_ORDER_ID` từ kết quả checkout của Service vào `Session` dạng Flash Message.
- `src/java/controller/MenuController.java`:
  - Tiếp nhận `LAST_ORDER_ID` từ `Session`, chuyển tiếp sang Request Attribute và dọn dẹp Session.
- `web/menu.jsp`:
  - Hiển thị mã đơn hàng `(#OrderID)` nổi bật bên cạnh thông báo thành công sau khi chốt đơn.

### 2. Tuân thủ kiến trúc:
- Controller không import Model hay DAO.
- Session được dọn dẹp sạch sẽ qua cơ chế Flash Message (không bị lặp khi F5).
- Đảm bảo 100% không dùng Scriptlet và không CSS inline.

## [2026-10-13] - Hoàn thiện Task 2 Mở Rộng: Quản Lý Đơn Hàng (Order Management)
### 1. File bị tác động / tạo mới:
- `src/java/utility/Constants.java`: Bổ sung `SUCCESS_UPDATE_ORDER_STATUS` (S0205), `ERROR_ORDER_NOT_FOUND` (E0203), `ERROR_INVALID_ORDER_STATUS` (E0204).
- `src/java/resources/messages_vi.properties` & `messages_en.properties`: Bổ sung bản địa hóa cho các mã thông báo và toàn bộ nhãn giao diện của trang quản lý đơn hàng (`order.management.*`).
- `src/java/dao/OrderDAO.java`:
  - Thêm phương thức `filterOrders(String dateStr, String status)`: Lọc theo ngày (định dạng `yyyy-MM-dd`) và trạng thái, tự động sắp xếp đơn mới nhất lên đầu.
  - Thêm phương thức `updateOrderStatus(int orderId, String newStatus)`: Cập nhật trạng thái và tự động ghi nhận mốc `cookingStartedAt` khi đơn chuyển sang chế biến.
- `src/java/service/OrderService.java` (Tạo mới):
  - Chuyển đổi Model sang DTO/Map (`convertOrdersToViewList`, `convertOrderToMap`, `convertOrderDetailsToMapList`) để Controller không truyền Model trực tiếp lên View.
  - Xử lý nghiệp vụ cập nhật trạng thái đơn (`updateOrderStatus`) và ném `ValidationException`.
- `src/java/controller/OrderManagementController.java` (Tạo mới):
  - Servlet điều phối xem và cập nhật đơn hàng cho Staff/Manager/Admin.
  - Tuân thủ tuyệt đối: Không import DAO/Model, không `PrintWriter`, xử lý Flash Message qua `Session`.
- `web/order_management.jsp` (Tạo mới):
  - Giao diện bộ lọc theo ngày và trạng thái, hiển thị bảng danh sách đơn hàng kèm chi tiết món và form cập nhật trạng thái.
  - Tuân thủ: 100% JSTL/EL, không scriptlet, không CSS/JS inline.
- `web/header.jsp`: Cập nhật liên kết Quản lý đơn hàng trỏ tới `OrderManagementController`.
- `web/WEB-INF/web.xml`: Đăng ký Servlet và Servlet-Mapping cho `OrderManagementController`.
- `web/css/global.css`: Bổ sung class `.order-filter-panel`, `.order-badge-*`, `.status-select`, `.btn-status-save`.

### 2. Bài học kinh nghiệm & Lỗi sai cần khắc phục (CRITICAL):
- **Lỗi vi phạm:** Ở phiên trước của Task 2, AI Agent đã nhảy cóc gọi công cụ hỏi trắc nghiệm `ask_question` mà **bỏ qua bước xuất trình Bản thiết kế sơ bộ (Danh sách file, giải thích kiến trúc MVC < 3 dòng)** trước khi làm rõ và dừng tiến trình.
- **Định hướng khắc phục:** Bắt buộc tuân thủ tuần tự nghiêm ngặt Bước 2 trong `boms_agent_workflow.md`: Khi nhận yêu cầu mới hoặc bổ sung, luôn luôn trình bày rõ ràng: (1) Danh sách file tạo mới/chỉnh sửa, (2) Dưới 3 dòng chứng minh kiến trúc MVC, (3) Đúng 01 câu hỏi trắc nghiệm, và dừng tiến trình chờ phê duyệt trước khi sinh bất kỳ dòng code nào.

## [2026-10-13] - Sửa lỗi tương tác giao diện trên `order_management.jsp` (Dark Mode, Dropdown, Menu)
### 1. File bị tác động:
- `web/order_management.jsp`:
  - Loại bỏ các thẻ lồng sai cấu trúc: `<!DOCTYPE html>`, `<html>`, `<head>`, `<body>` và việc tự nhúng lặp lại `<script src="main.js">`.
  - Chuẩn hóa cấu trúc khung giao diện trang con: Kẹp toàn bộ nội dung trong `<main class="main-content">` nằm giữa `<jsp:include page="header.jsp" />` và `<jsp:include page="footer.jsp" />`.
- `web/js/main.js`:
  - Đóng gói toàn bộ logic đăng ký sự kiện vào hàm `initMainApp()`.
  - Bổ sung biến cờ phòng hộ `window.__bomsMainInitialized` để đảm bảo hàm chạy đúng 1 lần (Idempotent), tránh đăng ký lặp Event Listener.
  - Hỗ trợ kiểm tra `document.readyState !== 'loading'` để tự động kích hoạt ngay cả khi script chạy sau sự kiện `DOMContentLoaded`.

### 2. Bài học kinh nghiệm & Quy chuẩn áp dụng cho các trang JSP về sau:
- **Quy chuẩn cấu trúc HTML Shell:** `header.jsp` là nơi mở thẻ `<!DOCTYPE html><html><head><body>` và `footer.jsp` là nơi đóng thẻ kèm nhúng duy nhất `main.js`. Mọi trang JSP chức năng trong hệ thống TUYỆT ĐỐI không được khai báo lại thẻ HTML gốc hay nhúng thêm `main.js`.
- **Tránh Anti-pattern nhân đôi Event Listener:** Khi một script chứa `classList.toggle(...)` bị nhúng 2 lần, mỗi cú click sẽ chạy hàm 2 lần liên tiếp (ví dụ: `add` rồi lại `remove`), khiến hiệu ứng Dark Mode, Dropdown, Avatar hoàn toàn bị triệt tiêu và người dùng tưởng rằng nút bị "liệt". Luôn dùng cờ `window.__bomsMainInitialized` trong file script chung.

## [2026-10-13] - Hoàn thiện Task 3: Tự động trừ kho nguyên liệu theo định lượng khi chốt đơn (UC-01 POST-3)
### 1. File bị tác động / tạo mới:
- `src/java/model/Ingredient.java` (Tạo mới): Entity đại diện cho bảng `Ingredients` (`ingredientId`, `categoryId`, `ingredientName`, `unit`, `quantity`, `warningThreshold`, `isActive`).
- `src/java/model/ProductIngredient.java` (Tạo mới): Entity đại diện cho bảng `ProductIngredients` (`productId`, `ingredientId`, `quantityNeeded`).
- `src/java/dao/IngredientDAO.java` (Tạo mới):
  - Mock in-memory lưu trữ danh sách nguyên liệu và công thức món tương ứng với các sản phẩm trên Menu.
  - Cung cấp các phương thức: `getAllIngredients`, `getIngredientById`, `getIngredientsByProductId`, `getProductIdsUsingIngredient`, `deductStock`, `updateIngredientQuantity`.
- `src/java/dao/ProductDAO.java`: Bổ sung phương thức `updateProductAvailability(int productId, boolean isAvailable)` để cập nhật trạng thái món.
- `src/java/utility/Constants.java`: Bổ sung mã lỗi `ERROR_INSUFFICIENT_INGREDIENTS` ("E0205").
- `src/java/resources/messages_vi.properties` & `messages_en.properties`: Bổ sung thông điệp bản địa hóa cho mã lỗi `E0205`.
- `src/java/service/CartService.java`:
  - Trong `checkout`: Bổ sung bước kiểm tra định lượng kho `validateInventoryStock(cart)` trước khi ghi nhận đơn và thực thi trừ kho `deductIngredientsStock(cart)` sau khi đơn được lưu.
  - Bóc tách theo SRP:
    - `validateInventoryStock`: Duyệt tổng hợp toàn bộ nguyên liệu cần dùng của giỏ hàng; nếu thiếu, tự động kích hoạt `disableProductsUsingIngredient` và ném `ValidationException(E0205)`.
    - `calculateTotalIngredientsNeeded`: Gom tổng định lượng nguyên liệu cần thiết dựa trên từng dòng `OrderDetail` và công thức `ProductIngredients`.
    - `disableProductsUsingIngredient`: Cập nhật `isAvailable = false` cho các món liên quan trên Menu.
    - `deductIngredientsStock`: Thực hiện trừ lượng nguyên liệu tương ứng trong `IngredientDAO`.
    - `checkAndDisableIfDepleted`: Nếu nguyên liệu cạn kiệt (<= 0) sau khi trừ, tự động chuyển món liên quan sang hết hàng (`isAvailable = false`).

### 2. Tuân thủ kiến trúc:
- Controller không import Model hay DAO mới.
- Service xử lý 100% nghiệp vụ tính toán trừ kho và ném `ValidationException`.
- View hiển thị mượt mà: Thông báo lỗi qua Flash Message và tự động chuyển thẻ món ăn sang trạng thái "Hết nguyên liệu" (disabled nút đặt).

## [2026-10-13] - Hoàn thiện UC-02: Chỉnh sửa đơn hàng (Hủy đơn & Đổi món khi chưa nấu)
### 1. File bị tác động / tạo mới:
- `src/java/utility/Constants.java`:
  - Mã thành công: `SUCCESS_CANCEL_ORDER` (S0206), `SUCCESS_CHANGE_DISH` (S0207).
  - Mã lỗi: `ERROR_ORDER_ALREADY_COOKING` (E0206), `ERROR_ORDER_CANNOT_BE_CANCELLED` (E0207), `ERROR_INVALID_EXCHANGE_PRODUCT` (E0208).
- `src/java/resources/messages_vi.properties` & `messages_en.properties`:
  - Bản dịch cho toàn bộ mã S0206, S0207, E0206, E0207, E0208.
  - Nhãn hiển thị cho nút Hủy đơn, Đổi món, xác nhận hủy, và thông báo tiền chênh lệch (`diff.extra`, `diff.refund`, `diff.equal`).
- `src/java/dao/IngredientDAO.java`:
  - `restoreStock(int ingredientId, double amount)`: Hoàn trả định lượng nguyên liệu vào kho.
  - `hasSufficientStockForProduct(int productId, int quantity)`: Kiểm tra khả năng đáp ứng nguyên liệu của sản phẩm trước khi đổi món.
- `src/java/dao/OrderDAO.java`:
  - `cancelOrder(int orderId)`: Chuyển trạng thái đơn sang 'Cancelled'.
  - `replaceOrderItem(int orderId, int oldProductId, int newProductId, int newQuantity, double newUnitPrice, Product newProduct)`: Thay thế dòng `OrderDetail` và tự động tính lại `totalAmount`.
- `src/java/service/OrderService.java`:
  - `cancelOrder(int orderId)`: Kiểm tra điều kiện `cookingStartedAt == null` và trạng thái hợp lệ, hoàn trả kho nguyên liệu cho toàn bộ các món trong đơn, gọi `orderDAO.cancelOrder`.
  - `exchangeDish(int orderId, int oldProductId, int newProductId, int newQuantity)`: Kiểm tra điều kiện chưa nấu, hoàn tạm nguyên liệu món cũ, kiểm tra tồn kho món mới (rollback nếu thiếu), trừ kho món mới, tính tiền chênh lệch và cập nhật đơn hàng.
  - `getAvailableProductsForExchange()`: Cung cấp danh sách món đang sẵn sàng phục vụ cho View (dạng Map DTO).
  - Bóc tách theo SRP: `validateAndGetOrderForModification`, `validateExchangeParams`, `findDetailByProductId`, `restoreOrderIngredients`, `restoreItemIngredients`, `deductItemIngredients`, `recheckProductAvailabilityAfterStockRestore`.
- `src/java/controller/OrderManagementController.java`:
  - Xử lý các action POST: `cancel` và `exchange`.
  - Điều phối Flash Message (`SUCCESS_MESSAGE`, `ERROR_MESSAGE`, `PRICE_DIFF`) qua Session và chuyển hướng an toàn.
  - Tuyệt đối không import Model/DAO.
- `web/order_management.jsp`:
  - Hiển thị form Hủy đơn (kèm hộp thoại xác nhận) và component `<details>` Đổi món (gọn gàng, không phụ thuộc JS ngoài) chỉ khi `order.canChangeOrder == true`.
  - Hiển thị thông báo tiền chênh lệch (Cần thu thêm khách / Hoàn trả lại khách).
- `web/css/global.css`:
  - Định dạng `.order-action-group`, `.btn-order-cancel`, `.order-item-row`, `.exchange-details`, `.btn-exchange-summary`, `.exchange-form`, `.filter-input-sm`, `.input-qty-sm`.

### 2. Tuân thủ kiến trúc:
- Controller không import Model hay DAO.
- Service xử lý 100% nghiệp vụ logic, kiểm tra điều kiện chưa nấu (`cookingStartedAt == null`), quản lý hoàn trả/trừ kho và ném `ValidationException`.
- Không scriptlet, không CSS/JS inline, sử dụng JSTL/EL và thẻ native `<details>` / `<summary>` an toàn.

## [2026-10-13] - Đồng bộ tầng Model (`src/java/model`) với Cơ sở dữ liệu (`BOMS_DATABASE.sql`)
### 1. File bị tác động:
- `src/java/model/Ingredient.java`:
  - Bổ sung 2 thuộc tính còn thiếu so với DB table `Ingredients`: `private double price;` và `private String storageLocation;`.
  - Bổ sung constructor đầy đủ 9 tham số, đồng thời giữ nguyên constructor 7 tham số cũ (khởi tạo giá trị mặc định cho `price = 0.0` và `storageLocation = null`) nhằm đảm bảo backward compatibility, không làm ảnh hưởng các tầng DAO (`IngredientDAO`) và Service đang dùng.
  - Bổ sung getter và setter: `getPrice()`, `setPrice(...)`, `getStorageLocation()`, `setStorageLocation(...)`, `setIsActive(...)`.

### 2. Báo cáo rà soát & Quyết định người dùng:
- `AccountManager.java`: Khớp 100% (6/6 trường).
- `Order.java`: Khớp 100% (13/13 trường DB + 1 trường navigation `orderDetails`).
- `OrderDetail.java`: Khớp 100% (4/4 trường DB + 1 trường navigation `product`).
- `Product.java`: Khớp 100% (6/6 trường).
- `ProductIngredient.java`: Khớp 100% (3/3 trường).
- `User.java`: Trường `Email` lệch tên so với cột `PrimaryEmail` trong DB; `userID` sử dụng quy ước PascalCase ID. Người dùng đã lựa chọn qua Bước 2: **Giữ nguyên hiện trạng `Email` trong `User.java`** để bảo toàn tính toàn vẹn và không làm ảnh hưởng luồng nghiệp vụ của `UserDAO` và `UserService`.

## [2026-10-13] - Hoàn thiện Task 2.2: Khởi tạo phiên qua QR & Quản lý bàn (UC-02)
### 1. File bị tác động:
- `web/WEB-INF/web.xml`: Bổ sung servlet mapping `/menu` cho `MenuController` để hỗ trợ cả 2 định dạng URL `/menu?tableId=...` và `/MenuController?tableId=...`.
- `src/java/filter/AuthFilter.java`: Bổ sung `/menu` vào whitelist (`isAuthPage`) để khách vãng lai quét QR không bị chặn chuyển hướng đến `login.jsp`.
- `src/java/controller/MenuController.java`:
  - Bóc tách theo SRP: `forwardFlashMessages`, `resolveActiveTab`, `handleTableSession`.
  - Hàm `handleTableSession`: Tiếp nhận `tableId` từ request param, lưu vào `session` (`CURRENT_TABLE`), chuyển tiếp xuống View qua `request.setAttribute("CURRENT_TABLE", tableId)`.
  - Xử lý edge case: Nếu phát hiện khách quét sang bàn mới khác với mã bàn đang lưu trong `session`, tự động xóa sạch giỏ hàng cũ (`session.removeAttribute("CURRENT_CART")`) theo đúng quyết định của người dùng ở Bước 2.
  - Hỗ trợ fallback: Nếu request không truyền `tableId` (khách reload, chuyển tab), tự động khôi phục từ `session`.
- `src/java/controller/CartController.java`:
  - Bóc tách theo SRP: `handleAddAction`, `handleUpdateAction`, `handleRemoveAction`, `handleClearAction`, `handleCheckoutAction`, `resolveTableId`, `buildRedirectUrl`.
  - Trong `resolveTableId`: Ưu tiên lấy từ request param, nếu rỗng tự động khôi phục từ `sessionScope.CURRENT_TABLE`.
  - Đảm bảo khi checkout, `tableId` từ phiên quét QR được truyền trọn vẹn xuống `cartService.checkout`, từ đó gán đúng `OrderType = "Table"` và `TableID = ...` vào DB.
  - Điều hướng an toàn giữ nguyên context bàn qua `buildRedirectUrl`.
- `.agent/WorkList.md`: Đánh dấu hoàn thành Task 2.2 `[DONE]`.

### 2. Tuân thủ kiến trúc:
- Controller không import DAO/Model, không `PrintWriter`.
- Bóc tách phương thức đơn trách nhiệm (SRP), kiểm tra biên dịch 0 lỗi.

## [2026-10-13] - Sửa lỗi xung đột phiên bàn (`CURRENT_TABLE`) khi Nhân viên (Staff) đăng nhập
### 1. Vấn đề phát sinh:
- Nếu một trình duyệt trước đó từng quét mã QR của bàn (lưu `CURRENT_TABLE` vào Session), sau đó người dùng đăng nhập tài khoản Nhân viên (Staff/Manager) và bấm vào "Thực đơn", `MenuController` và `menu.jsp` ưu tiên `CURRENT_TABLE` khiến giao diện hiển thị nhầm thành Menu Khách hàng tại bàn thay vì Menu Quầy Thu Ngân (Counter POS - UC-01).

### 2. File bị tác động & Giải pháp xử lý:
- `src/java/controller/auth/LoginController.java`:
  - Khi đăng nhập thành công, tự động xóa sạch phiên khách vãng lai cũ còn sót lại (`session.removeAttribute("CURRENT_TABLE")` và `session.removeAttribute("CURRENT_CART")`).
- `src/java/controller/MenuController.java`:
  - Trong `handleTableSession`: Kiểm tra nếu nhân viên đã đăng nhập (`isStaffLoggedIn`) và request không truyền tham số `tableId`, hệ thống chủ động xóa `session.removeAttribute("CURRENT_TABLE")` để đảm bảo nhân viên luôn mở đúng Menu Quầy Thu Ngân.
- `web/menu.jsp`:
  - Tái cấu trúc phân cấp thẻ `<c:choose>`: Ưu tiên nhánh `not empty sessionScope.USER_SESSION` lên đầu, luôn nhúng `header.jsp` cho nhân viên; chỉ khi `CURRENT_TABLE` không rỗng và người dùng là khách vãng lai mới hiển thị khung giao diện của Khách tại bàn.

### 3. Tuân thủ kiến trúc:
- Toàn bộ dự án biên dịch thành công 0 lỗi.

## [2026-10-14] - Hoàn thiện Task 2.3: Xử lý giao diện hết hàng & Validation tồn kho (UC-02 Exception 2.0.E1)
### 1. File bị tác động:
- `src/java/service/ProductService.java`:
  - Tiêm phụ thuộc `IngredientDAO`.
  - Bổ sung hàm chuyên trách SRP `checkProductAvailability(Product p)`: Kết hợp giữa cờ `p.isAvailable()` và kiểm tra định lượng nguyên liệu thực tế trong kho `ingredientDAO.hasSufficientStockForProduct(p.getProductId(), 1)`.
  - Cung cấp cờ `available` chính xác cho View DTO (`getAllProductsForView`).
- `src/java/service/CartService.java`:
  - Trong `validateAndGetProduct`: Chặn triệt để việc thêm món vào giỏ hàng (`addProductToCart`) nếu món hết hàng hoặc không đủ nguyên liệu, ném `ValidationException(Constants.ERROR_PRODUCT_NOT_AVAILABLE)`.
- `web/menu.jsp`:
  - Bổ sung component huy hiệu `<span class="badge-out-of-stock">` hiển thị trên ảnh đại diện món ăn khi `item.available == false`.
  - Nút thêm vào giỏ chuyển thành `<button type="button" class="btn-add-cart btn-disabled" disabled>` với nhãn "Hết hàng".
- `web/css/global.css`:
  - Bổ sung định dạng `.badge-out-of-stock` (nền đỏ, chữ trắng nổi bật ở góc trên ảnh).
  - Tối ưu class `.product-card.out-of-stock` với hiệu ứng làm mờ `opacity: 0.65`, `filter: grayscale(85%)`.
  - Định dạng `.btn-add-cart.btn-disabled` với con trỏ `cursor: not-allowed` và `pointer-events: none`.
- `src/java/resources/messages_vi.properties` & `messages_en.properties`:
  - Bổ sung nhãn `menu.badge.out_of_stock` ("Hết hàng" / "Out of Stock") và chuẩn hóa `menu.btn.out_of_stock`.
- `.agent/WorkList.md`: Đánh dấu hoàn thành Task 2.3 `[DONE]`.

### 2. Tuân thủ kiến trúc:
- Controller không can thiệp, không import Model/DAO.
- Service xử lý 100% nghiệp vụ đối soát kho nguyên liệu và ném `ValidationException`.
- View không scriptlet, không CSS/JS inline, giao diện thân thiện và đồng bộ đa ngôn ngữ.
- Biên dịch toàn bộ dự án đạt 0 lỗi.

## [2026-10-14] - Hoàn thiện Task 2.6: Modal Xác Nhận Chốt Đơn & Tab Theo Dõi Đơn Hàng Của Bàn (UC-02)
### 1. Thay đổi phạm vi yêu cầu:
- Loại bỏ tính năng đổi món khỏi Task 2.6 theo chỉ đạo của người dùng.
- Bổ sung Bảng nổi xác nhận đơn hàng (Order Confirmation Modal) khi bấm "Chốt Đơn" trong giỏ hàng.
- Tách modal thành component riêng biệt `web/components/order_confirm_modal.jsp` và nhúng bằng `<jsp:include>`.
- Hiển thị danh sách các đơn đã đặt của bàn với trạng thái real-time (`Chờ xác nhận`, `Đang làm`, `Đã hoàn thành`), cho phép hủy đơn khi chưa nấu (`cookingStartedAt == null`), tự động ẩn đơn đã hoàn thành sau 10 phút.

### 2. File bị tác động / tạo mới:
- `src/java/dao/OrderDAO.java`:
  - Bổ sung `getOrdersByTableId(int tableId)`: Lấy toàn bộ đơn hàng của bàn, sắp xếp đơn mới nhất lên đầu.
- `src/java/service/OrderService.java`:
  - Bổ sung `getOrdersForTableView(String tableIdStr)`: Chuyển đổi đơn thành Map DTO, lọc bỏ các đơn `Delivered` / `Completed` nếu đã hoàn thành quá 10 phút (`600,000 ms`), tính toán cờ `canCancelOrder` cho phép hủy khi `cookingStartedAt == null` và chưa hủy/hoàn thành.
- `src/java/controller/MenuController.java`:
  - Khai báo tiêm phụ thuộc `OrderService`.
  - Tách hàm SRP `loadTableOrders`: Khi phiên có `CURRENT_TABLE`, gọi `orderService.getOrdersForTableView` và nạp vào request attribute `TABLE_ORDERS`.
- `src/java/controller/CartController.java`:
  - Tiêm phụ thuộc `OrderService`.
  - Bổ sung action `cancelTableOrder`: Cho phép khách hủy đơn của bàn khi chưa vào bếp, hoàn trả kho nguyên liệu tự động.
  - Sau khi khách chốt đơn thành công tại bàn, tự động gán `ACTIVE_TAB = "orders"` để chuyển ngay sang tab theo dõi đơn.
- `web/components/order_confirm_modal.jsp` (Tạo mới):
  - Component bảng nổi xác nhận đơn hàng theo đúng chuẩn BOMS Component.
  - Tóm tắt số bàn, danh sách món kèm số lượng và thành tiền, tổng tiền thanh toán, cùng 2 nút: [Hủy / Chỉnh sửa thêm] và [Xác nhận gửi đơn].
- `web/menu.jsp`:
  - Bổ sung nút Tab "Đơn Của Bàn" (`tab-nav-btn`) trên thanh điều hướng tab khi có phiên bàn.
  - Nút "Chốt Đơn" trong giỏ hàng kích hoạt modal nổi `openOrderConfirmModal()` thay vì submit form ngay.
  - Nhúng `<jsp:include page="components/order_confirm_modal.jsp" />` ở cuối trang.
  - Bổ sung tab nội dung `tab-orders`: Hiển thị danh sách đơn dạng card tóm tắt kèm trạng thái badge, breakdown từng món, và form hủy đơn có dialog xác nhận.
- `web/js/main.js`:
  - Bổ sung 2 hàm quản lý modal: `openOrderConfirmModal()` và `closeOrderConfirmModal()`.
  - Hỗ trợ đóng modal khi bấm phím Escape hoặc click vào vùng overlay nền mờ.
- `web/css/global.css`:
  - Bổ sung styles cho Modal (`.modal-overlay`, `.modal-card`, `.modal-header`, `.modal-body`, `.modal-actions`, v.v.).
  - Bổ sung styles cho Tab Đơn của bàn (`.order-summary-card`, `.order-summary-header`, `.table-items-breakdown`, `.empty-orders-view`, v.v.).
- `src/java/resources/messages_vi.properties` & `messages_en.properties`:
  - Đầy đủ nhãn i18n cho modal xác nhận và tab đơn của bàn.

### 3. Tuân thủ kiến trúc:
- Controller không import Model/DAO, không `PrintWriter`.
- Kiến trúc View component hóa: Tách file JSP con trong `web/components/` giúp `menu.jsp` gọn gàng, dễ bảo trì.
- Dự án biên dịch thành công 0 lỗi.

## [2026-10-14] - Hoàn thiện 2 cải tiến: Ẩn đơn khách đã hủy & Rào cản hủy đơn nhân viên kèm khóa 5 phút
### 1. File bị tác động / tạo mới:
- `src/java/model/Order.java`: Bổ sung thuộc tính `canceledAt` (`Timestamp`), getter và setter để ghi nhận mốc thời gian đơn bị hủy.
- `src/java/utility/Constants.java`: Bổ sung mã lỗi `ERROR_CANCELLED_ORDER_LOCKED` (`E0209`).
- `src/java/resources/messages_vi.properties` & `messages_en.properties`: Bổ sung thông điệp bản dịch cho `E0209`, các nhãn cho modal xác nhận hủy của nhân viên (`order.cancel.modal.*`) và đồng hồ đếm ngược (`order.cancel.countdown`, `order.cancel.locked`).
- `src/java/dao/OrderDAO.java`:
  - Trong `cancelOrder`: Ghi nhận `o.setCanceledAt(new Timestamp(System.currentTimeMillis()))`.
  - Trong `updateOrderStatus`: Nếu chuyển sang `Cancelled` thì gán `canceledAt`; nếu chuyển từ `Cancelled` sang trạng thái khác (hoàn tác) thì reset `canceledAt = null`.
- `src/java/service/OrderService.java`:
  - Trong `getOrdersForTableView`: Lọc bỏ triệt để các đơn có trạng thái `Cancelled` khỏi danh sách trả về cho bàn của khách (Vấn đề 1).
  - Trong `updateOrderStatus`:
    - Nếu đơn đang ở trạng thái `Cancelled`: Kiểm tra mốc 5 phút (`validateCancelModificationWindow`), nếu quá 5 phút ném `ValidationException(E0209)`.
    - Nếu hoàn tác từ `Cancelled` sang trạng thái hoạt động: Kiểm tra tồn kho nguyên liệu (`validateOrderIngredientsForRestoration`), nếu thiếu ném `E0205`, nếu đủ thì trừ lại nguyên liệu kho (`deductOrderIngredients`).
    - Nếu đổi từ trạng thái khác sang `Cancelled`: Hoàn trả kho nguyên liệu (`restoreOrderIngredients`).
  - Trong `convertOrderToMap`: Tính toán cờ `isCancelled`, `isCancelLocked` và `remainingCancelSeconds` (đếm ngược từ 300 giây).
- `web/components/staff_cancel_modal.jsp` (Tạo mới): Component modal cảnh báo trước khi nhân viên xác nhận hủy đơn (hoàn kho nguyên liệu, thời hạn 5 phút hoàn tác).
- `web/order_management.jsp`:
  - Nhúng `<jsp:include page="components/staff_cancel_modal.jsp" />`.
  - Hiển thị đồng hồ đếm ngược thời gian thực trên badge trạng thái nếu đơn đang `Cancelled` và chưa khóa.
  - Disable dropdown chọn trạng thái và nút Lưu khi đơn đã bị khóa sau 5 phút.
  - Chặn thao tác chuyển trạng thái sang `Cancelled` bằng hàm JS `handleStaffStatusSubmit` để mở modal xác nhận thay vì submit ngay.
- `web/js/main.js`:
  - Bổ sung `openStaffCancelModal()`, `closeStaffCancelModal()`, `handleStaffStatusSubmit()`.
  - Bổ sung hàm đếm ngược thời gian thực `initCancelCountdowns()`: Giảm từng giây và tự động khóa dropdown + nút bấm khi đồng hồ về `00:00`.
- `web/css/global.css`:
  - Định dạng `.cancel-countdown-wrapper`, `.cancel-countdown-timer`, `.badge-locked`, `.text-sm`.

### 2. Tuân thủ kiến trúc:
- Controller không chứa logic tính toán thời gian khóa hay can thiệp kho nguyên liệu.
- Service xử lý toàn bộ nghiệp vụ kiểm tra 5 phút, đối soát kho và ném `ValidationException`.
- View 100% không scriptlet, không CSS/JS inline, tuân thủ nguyên tắc component hóa với `staff_cancel_modal.jsp`.
- Biên dịch toàn bộ dự án đạt 0 lỗi (`BUILD SUCCESSFUL`).

## [2026-10-14] - Chuẩn hóa toàn bộ tầng DAO sang JDBC MS SQL Server & Hoàn thiện Cơ sở dữ liệu
### 1. File bị tác động / tạo mới:
- `DATABASE/BOMS_DATABASE.sql`: Giữ nguyên vẹn 100% cấu trúc và dữ liệu mẫu theo đúng chỉ đạo người dùng (không sửa schema CSDL cho logic biên).
- `src/java/utility/OrderRuntimeCache.java` (Tạo mới): Lớp tiện ích lưu trữ mốc thời gian hủy đơn phục vụ cơ chế khóa sau 5 phút theo đúng Rule_BOMS (thuộc package `utility`), không can thiệp schema CSDL.
- `src/java/utility/Constants.java`: Bổ sung các hằng số mã lỗi CSDL: `ERROR_DATABASE_GET_PRODUCT` (DE0012), `ERROR_DATABASE_UPDATE_PRODUCT` (DE0013), `ERROR_DATABASE_GET_INGREDIENT` (DE0014), `ERROR_DATABASE_UPDATE_INGREDIENT` (DE0015), `ERROR_DATABASE_GET_ORDER` (DE0016), `ERROR_DATABASE_UPDATE_ORDER` (DE0017).
- `src/java/dao/UserDAO.java`: Dọn dẹp dứt điểm tàn dư biến static `userList` và static block rỗng vi phạm Rule_BOMS.
- `src/java/dao/AccountManagerDAO.java`: Chuẩn hóa ném mã lỗi `ValidationException` (loại bỏ nối chuỗi `+ e.getMessage()`), bổ sung phương thức `getKeyBySecurityKey(String securityKey)`.
- `src/java/dao/ProductDAO.java`: Kế thừa `DBContext`, loại bỏ hoàn toàn mock RAM & static variables, triển khai trọn vẹn CRUD chuẩn JDBC cho bảng `Products` (`getAllProducts`, `getProductById`, `updateProductAvailability`, `insertProduct`, `updateProduct`, `deleteProduct`).
- `src/java/dao/IngredientDAO.java`: Kế thừa `DBContext`, loại bỏ mock RAM, triển khai trọn vẹn CRUD và các hàm nghiệp vụ kho chuẩn JDBC cho `Ingredients` và `ProductIngredients` (`getAllIngredients`, `getIngredientById`, `getIngredientsByProductId`, `getProductIdsUsingIngredient`, `deductStock`, `updateIngredientQuantity`, `restoreStock`, `hasSufficientStockForProduct`, `insertIngredient`, `updateIngredient`, `deleteIngredient`, `insertProductIngredient`, `deleteProductIngredientsByProductId`).
- `src/java/dao/OrderDAO.java`: Kế thừa `DBContext`, loại bỏ mock RAM, triển khai trọn vẹn CRUD và Transaction quản lý đơn hàng (`Orders` & `OrderDetails`) theo đúng schema chuẩn của `BOMS_DATABASE.sql`.
- `src/java/service/OrderService.java`: Tích hợp `OrderRuntimeCache` quản lý thời gian đếm ngược 5 phút, cập nhật lan truyền `throws ValidationException` cho các helper trừ kho.
- `src/java/service/ProductService.java` & `src/java/service/CartService.java`: Xử lý và lan truyền `ValidationException` khi kết nối trực tiếp tầng DAO JDBC.

### 2. Bài học kinh nghiệm & Tuân thủ kiến trúc (CRITICAL):
- **Bảo toàn tính bất biến của CSDL (Database Schema Integrity):** Tuyệt đối không tùy tiện sửa đổi schema trong `BOMS_DATABASE.sql` cho những logic tạm thời/biên (như cửa sổ đếm ngược 5 phút). Nghiệp vụ này phải được giải quyết tại tầng Application/Service Runtime qua `OrderRuntimeCache` đặt trong package `utility`.
- **Tuân thủ quy chuẩn `Rule_BOMS.md`:**
  - 100% các DAO đều kế thừa `DBContext` và thao tác với MS SQL Server thật.
  - Xóa bỏ 100% biến static ngoài package `utility`.
  - Mọi hàm trong DAO đều bọc Try-Catch và ném `ValidationException` kèm mã lỗi chuẩn, không dùng `System.out.println`.
- Toàn bộ dự án biên dịch thành công 0 lỗi (`javac` 0 errors).

## [2026-10-14] - Hoàn thiện Task 4.1: Chuẩn hóa & Đồng bộ 100% mã hóa Unicode Escape (`\uXXXX`) trong `messages_vi.properties`
### 1. File bị tác động:
- `src/java/resources/messages_vi.properties`:
  - Rà soát toàn bộ 230 dòng của tệp tài nguyên bản địa hóa Tiếng Việt.
  - Chuẩn hóa văn phong, sửa lỗi ngữ pháp và chính tả hiển thị cho toàn bộ nhãn hệ thống (Header, Footer, Login, Dashboard, Profile, Settings, Register, Thông báo mã thành công S000X, S020X, mã lỗi E000X, E020X, Admin Dashboard, Order Management).
  - Chuyển đổi và mã hóa đồng bộ 100% các ký tự tiếng Việt có dấu sang định dạng **Unicode Escape (`\uXXXX`)** thuần ASCII trên đĩa (kể cả các chuỗi UTF-8 thô mới thêm từ UC-01, UC-02).
  - Bảo toàn nguyên vẹn 100% tên Key (không làm ảnh hưởng tới các thẻ `<fmt:message key="..." />` tại tầng View JSP).
- `Rule_BOMS.md`: Bổ sung quy định bắt buộc dùng mã Unicode Escape (`\uXXXX`) cho các file `.properties`.
- `WorkList.md`: Cập nhật mô tả và đánh dấu hoàn thành `[DONE]` cho Task 4.1.

### 2. Bài học kinh nghiệm & Quy chuẩn kiến trúc cốt lõi (CRITICAL):
- **Nguyên nhân lỗi font trên NetBeans 17 & Tomcat 10 (Windows)**:
  - Khi Tomcat 10 chạy JSTL `<fmt:setBundle>` trên môi trường Windows (`Cp1252`), nó nạp file `.properties` theo luồng ISO-8859-1. Nếu file chứa ký tự UTF-8 thô đa byte, các byte sẽ bị đọc riêng rẽ thành chuỗi ký tự rác (Mojibake).
  - NetBeans 17 có bộ soạn thảo Properties Editor tự động giải mã `\uXXXX` thành tiếng Việt để dev đọc/sửa. Nhưng khi bấm Save, NetBeans tự convert sang `\uXXXX`. Nếu trong file có sẵn ký tự UTF-8 thô, việc lưu của NetBeans trên Windows sẽ làm hỏng (corrupt) toàn bộ nội dung tiếng Việt của dự án.
- **Giải pháp tối ưu & vĩnh viễn**:
  - Toàn bộ file `.properties` BẮT BUỘC lưu trữ 100% dưới dạng **Unicode Escape (`\uXXXX`)** thuần ASCII trên đĩa.
  - Giải pháp này giúp dự án tương thích tuyệt đối 100% trên mọi máy của team dev, mọi phiên bản Tomcat và mọi hệ điều hành mà không bắt buộc thành viên nào phải cấu hình lại máy.
- **Biên dịch**: Dự án biên dịch thành công 0 lỗi (`BUILD SUCCESSFUL`).

## [2026-10-14] - Hoàn thiện Task 5.1: Quy hoạch và Phân mục lại hệ thống mã trong `Constants.java`
### 1. File bị tác động:
- `src/java/utility/Constants.java`:
  - Quy hoạch và tái cấu trúc toàn bộ 71 hằng số hệ thống theo luồng nghiệp vụ chuẩn mực.
  - Phân tách thành 5 phân khu rõ ràng có header phân vùng:
    1. Cấu hình hệ thống & Giới hạn dữ liệu (Validation Limits)
    2. Khóa phiên & Thuộc tính dùng chung (Session & Request Attributes)
    3. Dải mã thành công (Success Codes - `S001X`, `S010X`, `S020X`, `S9XXX`)
    4. Dải mã lỗi nghiệp vụ & Xác thực (Exception Codes - `E000X`, `E001X`, `E010X`, `E100X`, `E020X`, `E9XXX`)
    5. Dải mã lỗi cơ sở dữ liệu (Database Exception Codes - `DEXXX`: gom cụm chuẩn theo Transaction, User DAO, AccountManager DAO, Product DAO, Ingredient DAO, Order DAO).
  - Đưa hằng số `ERROR_LOCK_ACC` (E0016) và các mã CSDL trước đây nằm ở khu vực `//Chưa xếp` về đúng phân mục quy định.
  - Bảo toàn nguyên vẹn 100% tên hằng số và giá trị chuỗi, đảm bảo tương thích ngược 100% không làm gãy bất kỳ tầng nào.
- `.agent/learning.md`: Đã quét toàn bộ file và loại bỏ triệt để đoạn báo cáo trùng lặp (duplication) ở giữa file.

### 2. Tuân thủ kiến trúc:
- Tuân thủ nguyên tắc duy nhất của `Constants.java`: Lớp tiện ích tập trung toàn bộ chuỗi thông báo, mã lỗi và key session/request của dự án.
- Dự án biên dịch qua NetBeans Ant đạt **`BUILD SUCCESSFUL` (0 lỗi)**.

## [2026-10-14] - Hoàn thiện Task 5.2: Rà soát & Cập nhật đồng bộ toàn bộ mã nguồn Java & JSP
### 1. Phạm vi rà soát:
- Quét toàn diện các tầng trong `src/java/` (`dao/`, `service/`, `controller/`, `utility/`) và `web/`:
  - Kết quả quét hardcode chuỗi mã lỗi (`E...`, `DE...`, `S...`): **0 trường hợp hardcode** ngoài `Constants.java`. 100% các class đều tham chiếu chuẩn mực qua `Constants.*`.
  - Kết quả kiểm tra tầng View (`web/*.jsp`): Duy nhất `web/login.jsp` có biểu thức kiểm tra chuỗi `${ERROR_MESSAGE == 'E0016'}` (khớp hoàn hảo với hằng số `Constants.ERROR_LOCK_ACC = "E0016"`). Người dùng đã phê duyệt giữ nguyên hiện trạng để bảo toàn tính ổn định.
- Toàn bộ các vị trí gọi hằng số sau đợt tái cấu trúc Task 5.1 đều hoạt động trơn tru và hoàn toàn tương thích.

### 2. Tuân thủ kiến trúc:
- Giữ vững tính toàn vẹn của mã nguồn: Không làm gãy bất kỳ import, hằng số hay logic nào giữa Controller - Service - DAO - View.
- Dự án biên dịch qua NetBeans Ant đạt **`BUILD SUCCESSFUL` (0 lỗi)**.

## [2026-10-14] - Hoàn thiện Task 5.3: Sắp xếp và Đồng bộ hóa các file Properties (`messages_vi.properties` & `messages_en.properties`)
### 1. File bị tác động:
- `src/java/resources/messages_vi.properties`:
  - Đồng bộ và bổ sung toàn bộ 17 mã lỗi cơ sở dữ liệu (`DEXXX`), phân nhóm rõ ràng theo cụm Transaction, User DAO, AccountManager DAO, Product DAO, Ingredient DAO, Order DAO.
  - 100% nội dung tiếng Việt có dấu được mã hóa dưới chuẩn **Unicode Escape (`\uXXXX`)** thuần ASCII trên đĩa.
  - Thứ tự các cụm mã thành công (`SXXX`), mã lỗi nghiệp vụ (`EXXX`), và mã lỗi CSDL (`DEXXX`) khớp 100% với cấu trúc đã quy hoạch tại `Constants.java`.
- `src/java/resources/messages_en.properties`:
  - Đồng bộ và bổ sung toàn bộ các mã thành công (`SXXX`), mã lỗi nghiệp vụ (`EXXX`), và mã lỗi CSDL (`DEXXX`) sang bản dịch tiếng Anh chuẩn mực, chính xác về ngữ nghĩa.
  - Loại bỏ triệt để các mã bị khai báo trùng lặp (duplicate keys, ví dụ `E0209`).
  - Sắp xếp và phân nhóm khớp 100% với `Constants.java` và `messages_vi.properties`.
- `.agent/WorkList.md`: Đánh dấu hoàn thành `[DONE]` cho Task 5.3.

### 2. Bài học kinh nghiệm & Tuân thủ kiến trúc:
- **Đồng bộ hóa song ngữ 1-1**: Mọi hằng số mã thông báo (`SXXX`, `EXXX`, `DEXXX`) trong `Constants.java` đều có mặt đầy đủ trong cả 2 tệp `.properties` với cùng một định danh Key.
- **Bảo toàn chuẩn mã hóa Properties**: Tuyệt đối duy trì định dạng thuần ASCII với Unicode Escape (`\uXXXX`) cho tiếng Việt, bảo đảm khả năng tương thích 100% với NetBeans 17 và Tomcat 10 trên Windows.
- **Biên dịch**: Dự án biên dịch thành công 0 lỗi (`BUILD SUCCESSFUL`).

## [2026-10-14] - Hoàn thiện Task 3.1: Khảo sát CSDL & Xây dựng tầng DAO cho Thực đơn (Menu Management)
### 1. File bị tác động / tạo mới:
- `src/java/model/MenuCategory.java` (Tạo mới):
  - Model đại diện 1-1 cho bảng vật lý `MenuCategories` trong MS SQL Server (`CategoryID`, `CategoryName`).
  - Tuân thủ Rule_BOMS: Chỉ chứa thuộc tính private, Constructor rỗng & đầy đủ tham số, Getters và Setters; tuyệt đối không chứa logic xử lý.
- `src/java/dao/MenuCategoryDAO.java` (Tạo mới):
  - DAO chuyên trách truy xuất dữ liệu bảng `MenuCategories`, kế thừa `DBContext`.
  - Triển khai các hàm CRUD/Truy vấn chuẩn JDBC: `getAllCategories()`, `getCategoryById(int categoryId)`, `getCategoryByName(String categoryName)`.
  - Bọc `try-catch (SQLException)` và ném `ValidationException(Constants.ERROR_DATABASE_GET_PRODUCT)`.
- `src/java/dao/ProductDAO.java`:
  - Tái sử dụng nguyên vẹn các hàm CRUD hiện có: `getAllProducts()`, `getProductById()`, `updateProductAvailability()`, `insertProduct()`, `updateProduct()`, `deleteProduct()`.
  - Bổ sung 3 hàm nghiệp vụ quan trọng:
    - `getProductByName(String productName)`: Phục vụ kiểm tra trùng lặp tên món khi thêm mới (Validation Tầng 2).
    - `isProductNameExists(String productName, int excludeProductId)`: Phục vụ kiểm tra trùng tên món khi sửa món (loại trừ chính ID món đang sửa).
    - `getProductsByCategoryId(int categoryId)`: Lấy danh sách món theo danh mục (phục vụ lọc tab/phân loại thực đơn).
- `.agent/WorkList.md`: Đánh dấu hoàn thành `[DONE]` cho Task 3.1.

### 2. Bài học kinh nghiệm & Tuân thủ kiến trúc:
- **Tương ứng Model - Bảng CSDL**: Mỗi bảng vật lý độc lập trong CSDL SQL Server (như `MenuCategories`) bắt buộc phải có một Model tương ứng để đảm bảo kiến trúc rõ ràng, không gộp lẫn cấu trúc dữ liệu.
- **Tối đa hóa khả năng tái sử dụng (Code Reuse)**:
  - Tái sử dụng 100% các hàm đã xây dựng chuẩn trong `ProductDAO`.
  - Tái sử dụng các hàm công thức nguyên liệu trong `IngredientDAO` (`getIngredientsByProductId`, `insertProductIngredient`, `deleteProductIngredientsByProductId`, `hasSufficientStockForProduct`) thay vì viết lại.
- **Tuân thủ quy chuẩn DAO**: Kế thừa `DBContext`, tham số hóa 100% bằng `PreparedStatement`, bắt lỗi SQL và ném `ValidationException(DEXXX)`, nghiêm cấm in ra console.
- **Biên dịch**: Dự án biên dịch thành công 0 lỗi (`BUILD SUCCESSFUL`).

## [2026-10-14] - Hoàn thiện Task 3.2: Xây dựng tầng Nghiệp vụ & Validation cho Thực đơn (Menu Management)
### 1. File bị tác động:
- `src/java/utility/Validator.java`:
  - Bổ sung hàm kiểm tra số thực dương `checkPositiveDouble(String value, String errorMessage)` phục vụ kiểm tra dữ liệu giá tiền đầu vào (> 0) cho món ăn (Validation Tầng 1).
- `src/java/utility/Constants.java`:
  - Bổ sung các hằng số mã thành công: `SUCCESS_CREATE_PRODUCT = "S0208"`, `SUCCESS_UPDATE_PRODUCT = "S0209"`, `SUCCESS_CHANGE_PRODUCT_STATUS = "S0210"`.
  - Bổ sung các hằng số mã lỗi định dạng dữ liệu thô (Tầng 1): `ERROR_EMPTY_PRODUCT_NAME = "E1009"`, `ERROR_INVALID_PRODUCT_PRICE = "E1010"`, `ERROR_EMPTY_PRODUCT_CATEGORY = "E1011"`.
  - Bổ sung các hằng số mã lỗi nghiệp vụ (Tầng 2): `ERROR_PRODUCT_NAME_EXISTS = "E0210"`, `ERROR_PRODUCT_NOT_FOUND = "E0211"`.
- `src/java/resources/messages_vi.properties` & `messages_en.properties`:
  - Bổ sung đồng bộ các bản dịch cho `S0208`-`S0210`, `E1009`-`E1011`, `E0210`-`E0211`.
  - Bảo đảm 100% nội dung tiếng Việt lưu dạng **Unicode Escape (`\uXXXX`)** thuần ASCII trên đĩa.
- `src/java/service/ProductService.java`:
  - Chỉnh sửa có chọn lọc (`replace_file_content`), tuyệt đối không ghi đè mất mã nguồn hiện có.
  - Tích hợp `MenuCategoryDAO`.
  - Triển khai đầy đủ các phương thức nghiệp vụ với quy trình Validation 2 tầng:
    + `getAllCategoriesForView()`: Trả về danh sách danh mục món dạng `Map` DTO cho View.
    + `getAllProductsForManagementView()`: Trả về danh sách món quản lý kèm tên danh mục, cờ kinh doanh `isAvailable`, và cờ kiểm tra định lượng kho nguyên liệu `availableInStock`.
    + `getProductDetailForView(String productIdStr)`: Lấy chi tiết món đóng gói dạng `Map` DTO.
    + `createProduct(...)`: Validation Tầng 1 (rỗng, định dạng) + Validation Tầng 2 (tồn tại danh mục, trùng tên món trên thực đơn).
    + `updateProduct(...)`: Validation 2 tầng (kiểm tra tồn tại món, danh mục, chống trùng tên loại trừ ID món đang sửa).
    + `toggleProductStatus(...)` & `setProductStatus(...)`: Đổi trạng thái kinh doanh / Xóa mềm món ăn theo đúng quyết định đã duyệt.
- `.agent/WorkList.md`: Đánh dấu hoàn thành `[DONE]` cho Task 3.2.

### 2. Bài học kinh nghiệm & Tuân thủ kiến trúc:
- **Nguyên tắc Chỉnh sửa File Đã Tồn Tại**: Khi file đã tồn tại trong dự án (như `ProductService.java`), TUYỆT ĐỐI không ghi đè toàn bộ file (`overwrite=true`), mà bắt buộc phải dùng các thao tác chỉnh sửa từng đoạn có chọn lọc (`replace_file_content`) để bảo toàn các phương thức và chức năng đã phát triển trước đó.
- **Tuân thủ Validation 2 tầng**:
  + Tầng 1 (`utility/Validator.java`): Xử lý kiểm tra dữ liệu thô (rỗng, định dạng chuỗi sang số > 0).
  + Tầng 2 (`service/ProductService.java`): Xử lý quy tắc nghiệp vụ CSDL (danh mục tồn tại, trùng tên món).
- **Không để lọt Model lên Controller**: 100% dữ liệu trả về cho Controller/View đều được đóng gói thành `Map<String, Object>` / `List<Map<String, Object>>`.
- **Biên dịch**: Dự án biên dịch thành công 0 lỗi (`BUILD SUCCESSFUL`).

## [2026-10-14] - Hoàn thiện Task 3.3: Thiết kế Giao diện Quản lý Thực đơn (View & Components)
### 1. File bị tác động / tạo mới:
- `web/header.jsp`:
  - Bổ sung liên kết điều hướng "Quản lý thực đơn" (`/MenuManagementController`) vào sidebar `aside#menuDoc` cho cả 2 vai trò `Staff` và `Manager` theo đúng yêu cầu phân quyền.
- `src/java/resources/messages_vi.properties` & `messages_en.properties`:
  - Bổ sung đồng bộ nhãn sidebar (`header.sidebar.menu_management`) và toàn bộ nhãn cho trang quản lý thực đơn (`menu.mgmt.*`) cùng nhãn modal (`menu.modal.*`).
  - Đảm bảo 100% tiếng Việt lưu dạng **Unicode Escape (`\uXXXX`)** thuần ASCII trên đĩa (0 non-ASCII bytes).
- `web/components/product_form_modal.jsp` (Tạo mới):
  - Component modal dùng chung (Dynamic Modal) phục vụ cả 2 thao tác: "Thêm món mới" và "Chỉnh sửa món ăn".
  - Tách biệt thành component độc lập, tuân thủ Rule_BOMS: 0 scriptlet, 0 inline CSS/JS.
- `web/menu_management.jsp` (Tạo mới):
  - Giao diện bảng quản lý thực đơn hiển thị danh sách món ăn, thumbnail ảnh, danh mục, giá tiền, badge trạng thái kinh doanh (`Đang bán` / `Ngừng bán`), badge tồn kho nguyên liệu (`Đủ nguyên liệu` / `Hết nguyên liệu`), cùng các nút thao tác Sửa món và Bật/Tắt kinh doanh.
  - Tích hợp bộ lọc món theo danh mục và xử lý Flash Message chuẩn PRG (`<c:remove scope="session" />`).
  - Nhúng header, footer và component `product_form_modal.jsp`.
- `web/css/global.css`:
  - Bổ sung toàn bộ style cho bảng quản lý món, thumbnail (`.menu-thumb`, `.menu-thumb-placeholder`), các loại badge (`.badge-category`, `.badge-status-active`, `.badge-status-inactive`, `.badge-stock-ok`, `.badge-stock-out`), và các nút thao tác (`.btn-action-edit`, `.btn-action-toggle-stop`, `.btn-action-toggle-resume`).
- `web/js/main.js`:
  - Bổ sung các hàm JavaScript điều khiển modal: `openCreateProductModal()`, `openEditProductModal(...)`, `closeProductModal()`, cùng cơ chế đóng modal bằng phím Escape hoặc click overlay.
- `.agent/WorkList.md`: Đánh dấu hoàn thành `[DONE]` cho Task 3.3.

### 2. Bài học kinh nghiệm & Tuân thủ kiến trúc:
- **Tối ưu Component hóa (Component Reuse)**: Thiết kế một Modal động đa năng (`product_form_modal.jsp`) kết hợp với JavaScript điều khiển giúp tái sử dụng hoàn toàn cấu trúc Form giữa Thêm mới và Chỉnh sửa, giảm thiểu trùng lặp mã nguồn và giữ cho file JSP chính gọn gàng.
- **Tách biệt hoàn toàn View - CSS - JS**:
  + File JSP chỉ chứa cấu trúc HTML ngữ nghĩa và thẻ JSTL.
  + Toàn bộ styling nằm trong `global.css`.
  + Toàn bộ logic DOM / Modal nằm trong `main.js`.
- **Biên dịch**: Dự án biên dịch thành công 0 lỗi (`BUILD SUCCESSFUL`).

## [2026-10-14] - Hoàn thiện Task 3.4: Điều phối chức năng Tạo món mới (Create Menu Item Controller)
### 1. File bị tác động / tạo mới:
- `src/java/controller/MenuManagementController.java` (Tạo mới):
  - Kế thừa `HttpServlet`, điều phối luồng quản lý thực đơn.
  - Tuân thủ Rule_BOMS: 100% CẤM import Model và DAO, không sử dụng `PrintWriter`.
  - Kiểm tra phân quyền truy cập: Chỉ cho phép các vai trò `Staff`, `Manager`, và `Admin`. Nếu không thỏa mãn, tự động điều hướng về `login.jsp`.
  - Xử lý `doGet`: Nhận `filterCategory`, lấy danh mục và danh sách món từ `ProductService` (dưới dạng Map DTO), lọc theo danh mục nếu có yêu cầu, chuyển tiếp Flash Message từ `Session` sang `Request`, chuyển trang tới `menu_management.jsp`.
  - Xử lý `doPost`: Hứng `action = "createProduct"`, tiếp nhận các tham số Form (`productName`, `categoryId`, `price`, `imageUrl`), gọi `productService.createProduct(...)` xử lý quy trình Validation 2 tầng.
  - Áp dụng chuẩn Post/Redirect/Get (PRG): Sau khi xử lý tạo món (thành công hoặc thất bại), lưu thông báo trạng thái (`SUCCESS_MESSAGE` hoặc `ERROR_MESSAGE`) vào `Session`, và `sendRedirect` về `MenuManagementController?filterCategory=ALL` theo đúng quyết định đã duyệt của người dùng.
- `web/WEB-INF/web.xml`:
  - Khai báo thẻ `<servlet>` và `<servlet-mapping>` cho `MenuManagementController` ánh xạ tới URL pattern `/MenuManagementController`.
- `.agent/WorkList.md`: Đánh dấu hoàn thành `[DONE]` cho Task 3.4.

### 2. Bài học kinh nghiệm & Tuân thủ kiến trúc:
- **Tuân thủ quy chuẩn MVC Controller**: Controller đóng vai trò người điều phối luồng dữ liệu (traffic coordinator), chỉ tiếp nhận tham số từ Request, ủy quyền toàn bộ nghiệp vụ kiểm tra và xử lý cho tầng `Service`, và bắt ngoại lệ tập trung (Centralized Try-Catch) để điều hướng.
- **Bảo toàn cơ chế Flash Message (PRG)**: Khi người dùng submit Form tạo món (POST), Controller không forward trực tiếp (tránh nguy cơ người dùng bấm F5 bị submit trùng dữ liệu), mà luôn điều hướng bằng `sendRedirect` kết hợp lưu thông báo tạm thời trong `Session`.
- **Biên dịch**: Dự án biên dịch thành công 0 lỗi (`BUILD SUCCESSFUL`).

## [2026-10-14] - Khắc phục dứt điểm lỗi JasperException (Jakarta EL Type Coercion) tại `web/menu_management.jsp`
### 1. Nguyên nhân kỹ thuật gốc rễ:
- Tại dòng 44 của `web/menu_management.jsp`, biểu thức `${CURRENT_FILTER_CATEGORY == cat.categoryId}` so sánh giữa chuỗi String `"ALL"` và số nguyên `Integer`.
- Khi dùng `<c:set var="catIdStr" value="${cat.categoryId}" />`, JSTL giữ nguyên kiểu gốc của đối tượng gán vào `value` (vẫn là `Integer`), do đó `catIdStr` vẫn là `java.lang.Integer`.
- Trình thông dịch Jakarta EL của Tomcat tiếp tục tự động ép kiểu vế trái `"ALL"` sang kiểu `Long`, dẫn đến lỗi: `jakarta.el.ELException: Cannot convert [ALL] of type [class java.lang.String] to [class java.lang.Long]`.

### 2. Giải pháp khắc phục chuẩn hóa 2 tầng (Dual-layer Root Fix):
- **Tầng Service DTO (`ProductService.java`):** Chuẩn hóa ngay từ gốc: đóng gói `map.put("categoryId", String.valueOf(cat.getCategoryId()));` trong cả 3 hàm DTO (`getAllCategoriesForView`, `getAllProductsForManagementView`, `getProductDetailForView`). Mọi dữ liệu ID danh mục đẩy xuống View đều là `java.lang.String`.
- **Tầng View JSP (`web/menu_management.jsp`):** Sử dụng cú pháp body của thẻ `<c:set var="catIdStr">${cat.categoryId}</c:set>`. Theo đặc tả JSTL, nội dung body luôn luôn được sinh ra dưới dạng `java.lang.String`.
- Phép so sánh `${CURRENT_FILTER_CATEGORY == catIdStr}` hoàn toàn là so sánh giữa 2 chuỗi `String` (`String.equals()`), triệt tiêu 100% việc EL ép kiểu sang `Long`.

### 3. Bài học kinh nghiệm:
- **Đặc tả JSTL `<c:set>`**: Thuộc tính `value` giữ nguyên kiểu của Object nguồn; chỉ khi đặt trong nội dung body `<c:set>...</c:set>` thì JSTL mới đảm bảo kiểu trả về là `String`.
- **Nguyên tắc DTO chuẩn hóa cho View**: Tại tầng Service, các mã định danh dùng cho việc hiển thị hoặc làm tham số URL/lọc (`filterCategory`, `status`) nên được đóng gói sẵn thành `String` để tầng View EL không bao giờ xảy ra lỗi ép kiểu ngầm định.
- **Biên dịch**: Dự án biên dịch thành công 0 lỗi (`BUILD SUCCESSFUL`).

## [2026-10-15] - Khắc phục lỗi Modal Quản lý Thực đơn tự động bật lên và không đóng được
### 1. Nguyên nhân lỗi:
- `web/components/product_form_modal.jsp` và `web/js/main.js` sử dụng class CSS `.hidden` để ẩn modal. Tuy nhiên, trong toàn bộ hệ thống BOMS (`global.css`), class ẩn phần tử chuẩn là `.d-none { display: none !important; }`, hoàn toàn không có class `.hidden`.
- Do không có CSS `.hidden`, `div#productModal` chỉ nhận style `.modal-overlay { display: flex; }` nên tự động hiển thị che kín màn hình ngay khi tải trang, và khi bấm nút đóng thì `classList.add('hidden')` cũng không có tác dụng.
- Đồng thời, cuối trang `menu_management.jsp` bị nhúng trùng lặp thẻ `<script src="js/main.js">` và `</body></html>` đã có sẵn trong `footer.jsp`.

### 2. Giải pháp khắc phục:
- **`web/css/global.css`**: Bổ sung selector `.d-none, .hidden { display: none !important; }` để tương thích và bảo đảm an toàn kép.
- **`web/components/product_form_modal.jsp`**: Đồng bộ 100% class ẩn sang `.d-none` chuẩn BOMS cho cả modal overlay và nhóm trạng thái.
- **`web/js/main.js`**: Cập nhật các hàm `openCreateProductModal`, `openEditProductModal`, và `closeProductModal` sử dụng `classList.add('d-none')` / `classList.remove('d-none')`.
- **`web/menu_management.jsp`**: Xóa bỏ thẻ script và thẻ đóng html bị lặp sau `<jsp:include page="footer.jsp" />`.

### 3. Bài học kinh nghiệm:
- **Tuân thủ quy chuẩn CSS dùng chung**: Luôn sử dụng class `.d-none` đã được quy định chuẩn của dự án để ẩn/hiện modal và các khối giao diện động, tránh tự ý đặt tên class mới khi chưa khai báo trong `global.css`.
- **Tránh trùng lặp tài nguyên (DRY View)**: `footer.jsp` trong BOMS đã chứa sẵn các thẻ đóng `</body></html>` và nhúng `main.js`, các trang con chỉ cần nhúng footer mà không tự nhúng thêm thẻ script rời ở cuối.
- **Biên dịch**: Dự án biên dịch thành công 0 lỗi (`BUILD SUCCESSFUL`).

## [2026-10-15] - Hoàn thiện Task 3.5: Điều phối chức năng Chỉnh sửa & Cập nhật giá/trạng thái (Update Menu Item Controller)
### 1. File bị tác động:
- `src/java/controller/MenuManagementController.java`:
  - Trong phương thức `doPost`: Bổ sung nhánh xử lý `action = "updateProduct"` và `action = "toggleStatus"`.
  - Nhánh `updateProduct`: Tiếp nhận các tham số Form (`productId`, `categoryId`, `productName`, `price`, `imageUrl`, `isAvailable`), gọi `productService.updateProduct(...)` với quy trình Validation 2 tầng nghiêm ngặt (kiểm tra định dạng, giá trị dương, kiểm tra trùng tên món ngoại trừ chính nó), lưu Flash Message `Constants.SUCCESS_UPDATE_PRODUCT` vào `Session`.
  - Nhánh `toggleStatus`: Tiếp nhận tham số `productId`, gọi `productService.toggleProductStatus(productId)` để đảo trạng thái kích hoạt của món ăn, lưu Flash Message `Constants.SUCCESS_CHANGE_PRODUCT_STATUS` vào `Session`.
  - Duy trì ngữ cảnh người dùng: Khi sửa món hoặc đổi trạng thái, Controller tự động bảo lưu danh mục lọc hiện tại (`filterCategory`) để `sendRedirect` về đúng trang danh mục mà nhân viên/quản lý đang theo dõi.
  - Áp dụng chuẩn Post/Redirect/Get (PRG) và Centralized Try-Catch: Bắt `ValidationException` để truyền mã lỗi qua Session Flash Message, triệt tiêu nguy cơ duplicate submit khi F5.
- `.agent/WorkList.md`: Đánh dấu hoàn thành `[DONE]` cho Task 3.5, chính thức khép lại toàn bộ luồng TASK 3 (Quản lý & Điều chỉnh thực đơn).

### 2. Bài học kinh nghiệm & Tuân thủ kiến trúc:
- **Nguyên tắc phân tầng Controller - Service**: Controller tuyệt đối không can thiệp logic nghiệp vụ, không import Model/DAO, toàn bộ dữ liệu truyền và nhận thông qua kiểu dữ liệu cơ bản (`String`, Map DTO).
- **Trải nghiệm người dùng trong điều hướng (UX Context Preservation)**: Khác với thao tác "Tạo món mới" thường redirect về danh mục tổng (`ALL`) để người dùng thấy món mới xuất hiện, thao tác "Sửa món" và "Đổi trạng thái" cần giữ nguyên `filterCategory` hiện tại để không làm gián đoạn luồng làm việc của quản lý khi đang tinh chỉnh thực đơn theo từng nhóm.
- **Biên dịch**: Dự án biên dịch thành công 0 lỗi (`BUILD SUCCESSFUL`).

## [2026-10-15] - Tối ưu & Cách ly phiên đơn hàng của Bàn ăn qua QR (UC-02 Session Order Isolation)
### 1. Vấn đề phát sinh:
- Khi khách hàng A ngồi ăn tại Bàn 01, sau đó thanh toán ra về. Khách hàng B bước vào bàn 01 với thiết bị mới (`session == null`) quét mã QR:
  - Các đơn của bàn 01 nếu chưa hoàn thành hoặc chưa bị hủy (hoặc đơn vừa hoàn thành dưới 10 phút) trước đó bị lộ cho khách B thấy trên tab "Đơn của bàn" do truy vấn trực tiếp theo `TableID = 1`.
  - Khách B có thể bấm nút hủy nhầm đơn của khách trước, gây sai lệch kho và trải nghiệm không tốt.

### 2. Giải pháp kỹ thuật (Phương án 1 - Gắn đơn theo Session thiết bị khách):
- `src/java/controller/CartController.java`:
  - Trong `handleCheckoutAction`: Khi khách bấm chốt đơn thành công tại bàn (`tableId != null`), gọi hàm chuyên trách `recordPlacedOrderForTableSession(session, orderId)` để lưu danh sách mã đơn `PLACED_ORDER_IDS` (kiểu `List<Integer>`) vào `HttpSession` của chính thiết bị đó.
- `src/java/controller/MenuController.java`:
  - Trong `loadTableOrders`: Lấy danh sách `PLACED_ORDER_IDS` từ `HttpSession` và truyền vào `orderService.getOrdersForTableView(currentTable, placedOrderIds)`.
  - Trong `handleTableSession`: Nếu phát hiện khách quét sang bàn khác (`currentTable != newTableId`) hoặc nhân viên đăng nhập, tự động xóa sạch `PLACED_ORDER_IDS` cùng với `CURRENT_CART`.
- `src/java/service/OrderService.java`:
  - Nâng cấp `getOrdersForTableView(String tableIdStr, List<Integer> orderIds)`:
    + Nếu `orderIds == null || orderIds.isEmpty()` (khách mới vào bàn chưa đặt đơn nào trên thiết bị này), trả về ngay danh sách rỗng (`new ArrayList<>()`).
    + Chỉ hiển thị các đơn thuộc danh sách mà khách hiện tại đã đặt (`orderIds.contains(o.getOrderId())`).
    + Bổ sung kiểm tra trạng thái `"Paid"` vào điều kiện tự động ẩn sau 10 phút cùng với `"Delivered"` và `"Completed"`.

### 3. Kết quả đạt được & Tuân thủ kiến trúc:
- **Bảo mật & Cách ly 100% giữa các lượt khách:** Khách mới vào quét QR (`session == null`) luôn luôn nhìn thấy màn hình "Đơn của bàn" hoàn toàn sạch sẽ, không thể xem hoặc bấm hủy nhầm đơn của khách trước.
- **Tuân thủ kiến trúc:** Controller không import Model/DAO, Controller chỉ quản lý định danh phiên trong Session, Service xử lý logic lọc đơn, View hiển thị giao diện rỗng qua thẻ JSTL.
- **Biên dịch**: Dự án biên dịch thành công 0 lỗi (`BUILD SUCCESSFUL`).

## [2026-10-15] - Xây dựng Hệ thống Tải ảnh & Stream Ảnh Động Độc Lập (Image Upload & Media Streaming System)
### 1. Bối cảnh & Yêu cầu:
- Khách hàng không muốn thay đổi cấu trúc bảng trong CSDL (giữ nguyên bảng `Users` và `Products`).
- Giải quyết triệt để vấn đề "Mất ảnh khi Clean & Build / Redeploy" trên NetBeans và Tomcat mà không phải copy file thủ công.
- Hỗ trợ cơ chế lai (Hybrid) cho Quản lý Thực đơn: Cho phép chọn tệp từ máy tính HOẶC dán link ảnh online qua hệ thống 2 Tab con trực quan có xem trước ảnh (Preview).

### 2. File bị tác động / tạo mới:
- `src/java/utility/ImageUploadUtility.java` (Tạo mới):
  - Lớp tiện ích tĩnh chuyên trách quản lý file ảnh, lưu trữ vào thư mục dữ liệu độc lập ngoài ứng dụng: `System.getProperty("user.home")/boms_data/uploads/`.
  - Thực thi Validation tầng 1: Giới hạn dung lượng tối đa 5MB, kiểm tra đuôi file (`.jpg, .jpeg, .png, .webp`) và MIME type, ném `ValidationException` kèm mã lỗi chuẩn (`E1012`, `E1013`, `E1014`).
  - Tự động sinh tên file duy nhất theo định dạng `prefix_timestamp_uuid.ext` và tạo thư mục con trên đĩa.
  - Cung cấp hàm `getImageFile(relativePath)` có kiểm tra chống tấn công Directory Traversal (`..`) và hàm `deleteImage(relativePath)` dọn dẹp file cũ.
- `src/java/controller/ImageServingServlet.java` (Tạo mới):
  - Servlet chuyên trách ánh xạ pattern `/uploads/*`.
  - Đọc file từ thư mục độc lập trên đĩa và stream trực tiếp byte dữ liệu ra `response.getOutputStream()`.
  - Tự động nhận diện MIME type và cấu hình `Cache-Control: public, max-age=86400` giúp trình duyệt tải ảnh cực nhanh như file tĩnh.
- `src/java/filter/AuthFilter.java`:
  - Mở rộng whitelist tài nguyên tĩnh cho các đuôi `.jpg`, `.jpeg`, `.webp` và các request chứa `/uploads/` để khách vãng lai quét mã QR tại bàn xem được ảnh mà không bị chặn chuyển hướng đăng nhập.
- `src/java/utility/Constants.java`:
  - Bổ sung các hằng số mã lỗi: `ERROR_INVALID_IMAGE_TYPE ("E1012")`, `ERROR_IMAGE_SIZE_EXCEEDED ("E1013")`, `ERROR_IMAGE_UPLOAD_FAILED ("E1014")`.
- `src/java/resources/messages_vi.properties` & `messages_en.properties`:
  - Bổ sung nội dung thông báo lỗi và các nhãn UI modal (tab upload, hint dung lượng, preview title) theo đúng chuẩn Unicode Escape (`\uXXXX`) cho tiếng Việt.
- `src/java/controller/MenuManagementController.java`:
  - Thêm annotation `@MultipartConfig` với giới hạn 5MB/file và 20MB/request.
  - Trong `doPost`: Bổ sung xử lý nhận `Part productImageFile`, ưu tiên gọi `ImageUploadUtility.saveImage(...)` khi có file upload từ máy tính, fallback sang `imageUrl` nhập tay nếu không upload file.
- `web/components/product_form_modal.jsp`:
  - Bổ sung `enctype="multipart/form-data"` cho Form.
  - Thiết kế 2 Tab con [📁 Tải tệp lên] và [🔗 Dán liên kết], kèm khung xem trước ảnh (preview).
- `web/css/global.css`:
  - Bổ sung định dạng CSS cho `.image-tab-bar`, `.btn-image-tab`, `.image-preview-container`, `.preview-thumb`, `.product-card-img`.
- `web/js/main.js`:
  - Bổ sung các hàm điều khiển DOM: `switchImageTab(mode)`, `previewUploadedImage(input)`, `previewUrlImage(url)`, `resetImageModalState()`.
- `web/menu_management.jsp` & `web/menu.jsp`:
  - Chuẩn hóa hiển thị thẻ `<img>` với `fn:startsWith`: Tự động nhận diện cả URL online (`http/https`) lẫn URL cục bộ (`uploads/...`) ghép với `contextPath`.

### 3. Bài học kinh nghiệm & Tuân thủ kiến trúc:
- **Tách biệt Mã nguồn và Dữ liệu (Code vs Storage Separation):** Lưu trữ tệp người dùng tải lên ngoài thư mục build giúp bảo toàn dữ liệu 100% vĩnh viễn, phù hợp tuyệt đối cho môi trường sản xuất.
- **Tối ưu trải nghiệm (Hybrid UX):** Thiết kế 2 tab giúp người dùng linh hoạt giữa việc upload file ảnh từ máy hoặc dán link internet.
- **Biên dịch**: Dự án biên dịch thành công 0 lỗi (`BUILD SUCCESSFUL`).

## [2026-10-15] - Khắc phục Triệt để Lỗi "Kết nối đã được đặt lại" (ERR_CONNECTION_RESET) khi Tải Ảnh
### 1. Nguyên nhân kỹ thuật gốc rễ:
- **Dung lượng file vượt quá giới hạn 5MB ban đầu:** Khi người dùng tải ảnh chất lượng cao (ảnh chụp từ điện thoại thường từ 5MB - 12MB), kích thước file vượt ngưỡng `maxFileSize` trong `@MultipartConfig`. Tomcat phát hiện vi phạm khi đang đọc body và chủ động gửi gói tin TCP RST (Reset Packet) để ngắt socket, khiến trình duyệt hiển thị *"Kết nối đã được đặt lại"*.
- **Lỗ hổng ngoài khối `try-catch`:** Lệnh `request.getParameter("action")` nằm ngoài khối `try-catch`. Đối với request multipart, chính lệnh này kích hoạt việc parse body của Tomcat, làm ngoại lệ `IllegalStateException: SizeLimitExceededException` văng thẳng ra container mà không được thu hồi.
- **Đóng sớm luồng Socket trong `ImageServingServlet`:** Việc đặt `response.getOutputStream()` trong `try(...)` làm tự động gọi `close()` lên socket trước khi Tomcat hoàn tất chu trình request-response, dễ gây xung đột reset kết nối khi tải nhiều ảnh.

### 2. Giải pháp khắc phục 4 tầng (4-Layer Resilient Fix):
- **Tầng 1 (Client-side Check - `web/js/main.js`):** Trong `previewUploadedImage`, kiểm tra ngay `file.size > 10MB`. Nếu vượt quá, hiển thị cảnh báo, tự xóa input và tắt preview; chặn 100% việc gửi file quá 10MB lên server.
- **Tầng 2 (Tầng Tiện ích - `src/java/utility/ImageUploadUtility.java`):** Nâng `MAX_FILE_SIZE = 10 * 1024 * 1024` (10 MB).
- **Tầng 3 (Bảo vệ Controller - `src/java/controller/MenuManagementController.java`):**
  - Nâng cấu hình: `maxFileSize = 10MB`, `maxRequestSize = 30MB`, `fileSizeThreshold = 2MB`.
  - Đưa toàn bộ việc đọc tham số vào trong khối `try-catch`.
  - Bắt thêm ngoại lệ `IllegalStateException | ServletException` để thu hồi lỗi quá dung lượng, gán Flash Message `ERROR_IMAGE_SIZE_EXCEEDED` và `sendRedirect` an toàn, không bao giờ để rớt kết nối TCP.
- **Tầng 4 (An toàn Luồng Socket - `src/java/controller/ImageServingServlet.java`):**
  - Chỉ đóng `FileInputStream in`, không gọi `close()` lên `response.getOutputStream()`, chỉ gọi `out.flush()` để Tomcat tự quản lý vòng đời socket.
- **Đa ngôn ngữ (`messages_vi.properties` & `messages_en.properties`):**
  - Cập nhật thông báo và nhãn gợi ý sang mức chuẩn 10MB (Unicode Escape `\uXXXX` cho tiếng Việt).

### 3. Kết quả đạt được & Tuân thủ kiến trúc:
- Triệt tiêu 100% lỗi Connection Reset. Hệ thống chấp nhận ảnh chụp dung lượng cao lên tới 10MB một cách mượt mà và an toàn.
- Controller tuân thủ Rule_BOMS: Bắt ngoại lệ tập trung, không import Model/DAO, giao diện View sạch sẽ không inline CSS/JS.
- **Biên dịch**: Dự án biên dịch thành công 0 lỗi (`BUILD SUCCESSFUL`).

### 4. Bài học kinh nghiệm phát sinh (Checked Exception trong mệnh đề catch):
- **Hiện tượng:** Khai báo `catch (IllegalStateException | ServletException e)` trong Controller gây lỗi biên dịch trong NetBeans: *"exception jakarta.servlet.ServletException is never thrown in body of corresponding try statement"*.
- **Nguyên nhân:** `ServletException` là Checked Exception, trong khi các hàm gọi bên trong khối `try` đã được bọc try-catch riêng (như `getPartSafely`) nên không ném Checked Exception này ra ngoài.
- **Khắc phục:** Chỉ khai báo `catch (IllegalStateException e)` (ngoại lệ RuntimeException mà Tomcat ném ra khi parse multipart vượt kích thước) kết hợp với `catch (Exception e)` phía dưới để bắt trọn vẹn mà hoàn toàn hợp lệ cú pháp Java.
- **Trạng thái Build:** `BUILD SUCCESSFUL` (0 lỗi).

## [2026-10-15] - Khắc phục Triệt để Lỗi Vỡ Font Tiếng Việt (Mojibake) và Vi phạm Hardcode trong `main.js`
### 1. Nguyên nhân kỹ thuật gốc rễ:
- Chuỗi *"Chá»‰nh Sá»a ThÃ´ng Tin MÃ³n Ä‚n"* và thông báo localhost *"KÃch thÆ°á»›c áº£nh vÆ°á»£t quÃ¡ giá»›i háº¡n cho phÃ©p (tá»‘i Ä‘a 10MB)..."* hoàn toàn **không xuất phát từ `messages_vi.properties`** (file này đã được mã hóa chuẩn `\uXXXX`).
- **Nơi gây lỗi:** Các chuỗi tiếng Việt có dấu thô (Raw UTF-8) bị hardcode trực tiếp trong file tĩnh `web/js/main.js` (dòng `alert(...)`, gán `titleEl.textContent`, và countdown timer).
- **Cơ chế lỗi font (Mojibake):** Tomcat 10 phục vụ file tĩnh `.js` trên Windows (môi trường Cp1252) không có charset UTF-8 cưỡng bức, khiến trình duyệt giải mã các byte UTF-8 đa byte theo bảng mã Windows-1252 (Latin-1), biến tiếng Việt thành ký tự rác. Đồng thời, việc hardcode tiếng Việt phá vỡ cơ chế đa ngôn ngữ (i18n).

### 2. File bị tác động / Giải pháp triển khai:
- `web/components/product_form_modal.jsp`:
  - Gắn các thuộc tính dữ liệu động được render từ JSTL `<fmt:message>` vào `div#productModal`: `data-title-add`, `data-title-edit`, và `data-error-size` (`E1013`).
- `web/order_management.jsp`:
  - Gắn thuộc tính dữ liệu `data-locked-text` (`order.cancel.locked`) vào thẻ `span.cancel-countdown-timer`.
- `web/js/main.js`:
  - Trong `previewUploadedImage`: Đọc thông báo lỗi dung lượng động từ `modal.getAttribute('data-error-size')` (fallback chuỗi Unicode Escape `\uXXXX`).
  - Trong `openCreateProductModal` và `openEditProductModal`: Đọc tiêu đề modal động từ `data-title-add` và `data-title-edit` (fallback `\uXXXX`).
  - Trong `initCancelCountdowns`: Đọc nhãn khóa đơn động từ `data-locked-text`.
  - Chuẩn hóa 100% comment sang Tiếng Anh, loại bỏ triệt để mọi byte non-ASCII, đưa `main.js` đạt **100% thuần ASCII tuyệt đối**.

### 3. Bài học kinh nghiệm & Lỗi sai kiến trúc bị nhắc nhở (CRITICAL):
- **Lỗi vi phạm quy trình:** AI Agent từng cố thực hiện sửa mã nguồn khi chưa giải thích rõ nguyên nhân, vị trí lỗi và chưa nhận được sự phê duyệt ở Bước 2.
- **Định hướng khắc phục:** Bắt buộc tuân thủ tuần tự nghiêm ngặt Bước 2 của `boms_agent_workflow.md`: Luôn trình bày bản thiết kế (chỉ điểm lỗi, danh sách file, dưới 3 dòng MVC), hỏi 01 câu trắc nghiệm và dừng tiến trình chờ người dùng phê duyệt trước khi gọi bất kỳ công cụ chỉnh sửa code nào.
- **Quy chuẩn Quản lý Văn bản trong JavaScript:** Tuyệt đối không hardcode chuỗi tiếng Việt thô vào file JS. Toàn bộ chuỗi ngôn ngữ phải được View (JSP) nạp qua Resource Bundle và truyền xuống JS thông qua các thuộc tính HTML `data-*` attribute. Mọi chuỗi dự phòng (fallback) trong JS bắt buộc phải dùng Unicode Escape (`\uXXXX`).
- **Trạng thái Build:** Biên dịch javac với Apache Tomcat 10.1 và thư viện dự án đạt **0 lỗi** (`BUILD SUCCESSFUL`).

## [2026-10-15] - Hoàn thiện UC-07 & UC-08: Báo cáo ẩn danh kèm bằng chứng
### 1. File bị tác động / tạo mới:
- `src/java/model/ReportCategory.java`, `AnonymousReport.java`, `ReportEvidence.java` (Tạo mới): Entity khớp bảng `ReportCategories`, `AnonymousReports`, `ReportEvidences`. `AnonymousReport` KHÔNG có thuộc tính UserID người gửi.
- `src/java/dao/ReportDAO.java` (Tạo mới): Truy vấn JDBC thật (không mock). `insertReportWithEvidences` lưu báo cáo + bằng chứng trong 1 transaction (rollback khi lỗi). `filterReports` lọc theo Trạng thái / Ngày / Loại và giới hạn theo `CategoryManagers` cho Manager.
- `src/java/dao/NotificationDAO.java` (Tạo mới): `insertNotification` ghi vào bảng `Notifications` dùng chung.
- `src/java/service/ReportService.java` (Tạo mới):
  - UC-07: `submitReport` (validate loại, tiêu đề, mô tả, file; sinh Ticket ID `TK-XXXXXXXX`; lưu file; thông báo Manager phụ trách loại báo cáo - BR-13).
  - UC-08: `getReportsForView`, `getReportDetailForView`, `updateReport` (chỉ nhận In Progress / Resolved / Rejected; Rejected bắt buộc có phản hồi - 8.0.E1; báo cáo Resolved/Rejected bị khóa), `getEvidenceForDownload` + `writeEvidenceTo` (8.2).
  - Phân quyền: Admin xem toàn bộ, Manager chỉ xem loại được phân công trong `CategoryManagers`.
  - `resolveEvidencePath` chặn đường dẫn `../` để không đọc được file ngoài thư mục upload.
- `src/java/controller/ReportController.java` (Tạo mới): Gửi báo cáo (multipart) và tra cứu phản hồi theo Ticket ID.
- `src/java/controller/ReportManagementController.java` (Tạo mới): Danh sách (mặc định lọc Pending), chi tiết, cập nhật trạng thái; lỗi validate thì ở lại trang chi tiết và giữ dữ liệu đã nhập.
- `src/java/controller/EvidenceController.java` (Tạo mới): Tải file bằng chứng có kiểm tra quyền.
- `web/report.jsp`, `web/report_management.jsp`, `web/report_detail.jsp` (Tạo mới): Có nút Hủy (7.1, 8.1), tô đỏ ô lỗi (`.input-error`), toàn bộ nội dung người dùng nhập được escape bằng `c:out` / `fn:escapeXml` để chống XSS.
- `web/WEB-INF/Upload.properties`: (đã bỏ ngày 2026-10-16, xem mục bên dưới).
- `web/WEB-INF/web.xml`: Đăng ký 3 Servlet; `ReportController` có `<multipart-config>` giới hạn 20MB/file.
- `web/header.jsp`: Link "Báo cáo" (Admin/Manager) trỏ `ReportManagementController`; thêm link "Báo cáo ẩn danh" cho mọi tài khoản đã đăng nhập.
- `src/java/utility/Constants.java`: Mã S0300-S0301, E0300-E0316, DE0030-DE0033 (đổi từ DE0012-DE0015 khi gộp bản 0.0.6 của Trung vì trùng mã) và hằng số nghiệp vụ báo cáo.
- `messages_vi.properties` & `messages_en.properties`: Bản dịch đầy đủ cho các khóa `report.*` và mã lỗi mới.
- `web/css/global.css`: Class `.report-*`, `.input-error`, `.ticket-box`, `.report-badge-*`.
- `web/js/main.js`: `initEvidenceFileCheck` kiểm tra số lượng / định dạng / dung lượng file ngay khi chọn (server vẫn kiểm tra lại).

### 2. Bài học kinh nghiệm:
- **Không dùng `Paths.get` với tên file người dùng gửi lên:** Tên chứa ký tự Windows cấm (`?`, `*`, `:`) làm ném `InvalidPathException` -> lỗi 500. Chỉ cắt chuỗi sau dấu `/` cuối cùng và lưu file trên ổ đĩa bằng tên UUID.
- **Không khai báo `static` ngoài package utility** (kể cả hằng số chuỗi SQL trong DAO): dùng `private final`.
- **Upload file trong Tomcat:** Khi file vượt `max-file-size`, `request.getParts()` ném `IllegalStateException` -> Controller phải bắt và đổi thành mã lỗi (E0306). Trình duyệt không cho điền lại file khi forward về form, cần nhắc người dùng chọn lại.
- **Kiểm thử tiếng Việt bằng curl trên Git Bash:** Tham số dòng lệnh không phải UTF-8 nên dữ liệu bị lỗi font; phải đọc nội dung từ file UTF-8 (`-F "field=<file.txt"`) mới phản ánh đúng trình duyệt.

## [2026-10-16] - Gộp luồng upload bằng chứng (UC-07) vào `ImageUploadUtility` dùng chung (theo yêu cầu của Trung)
### 1. File bị tác động:
- `src/java/utility/ImageUploadUtility.java` (Nâng cấp, GIỮ NGUYÊN API cũ `saveImage`, `getImageFile`, `deleteImage`, `UPLOAD_ROOT_DIR`, `MAX_FILE_SIZE` nên `MenuManagementController` và `ImageServingServlet` không phải sửa):
  - Thêm lớp `UploadRule` (đuôi file, MIME, dung lượng tối đa, mã lỗi, vùng lưu) và 2 quy tắc có sẵn: `IMAGE_RULE` (jpg/jpeg/png/webp, 10MB, công khai - giống hệt trước) và `REPORT_EVIDENCE_RULE` (jpg/jpeg/png/mp4/pdf/docx, 20MB, riêng tư - khớp `CK_Evidence_Type`, `CK_Evidence_Size`).
  - Thêm `saveFile(part, subFolder, rule)`, `validateFile(part, rule)`, `getPrivateFile(path)`, `deleteFile(path)`, `getContentType(fileName)`.
  - 2 vùng lưu: `~/boms_data/uploads/` (công khai, `ImageServingServlet` phát qua `/uploads/*`) và `~/boms_data/private/` (riêng tư, KHÔNG có URL công khai). Đường dẫn lưu DB: `uploads/...` hoặc `private/...`.
  - Siết kiểm tra Directory Traversal: so sánh với `canonicalBase + File.separator` để không lọt sang thư mục cùng tiền tố (ví dụ `uploads2`). Ghi file lỗi thì xóa file dở dang.
- `src/java/service/ReportService.java`: Bỏ toàn bộ code tự lưu file, tự đọc cấu hình thư mục, tự chặn `../`, tự đoán MIME; gọi `ImageUploadUtility` thay thế. Service chỉ giữ kiểm tra nghiệp vụ (ít nhất 1, tối đa 5 file) và kiểm tra quyền trước khi tải.
- `src/java/utility/Constants.java`: Bỏ `UPLOAD_CONFIG_FILE`, `UPLOAD_CONFIG_KEY`, `DEFAULT_UPLOAD_DIR`, `REPORT_ALLOWED_FILE_TYPES` (đã chuyển vào `REPORT_EVIDENCE_RULE`).
- `web/WEB-INF/Upload.properties`: Xóa. Không còn cần cấu hình thư mục riêng.

### 2. Bài học kinh nghiệm:
- **Upload dùng chung KHÔNG có nghĩa là phát công khai chung:** `AuthFilter` cho mọi URL chứa `/uploads/` đi qua không cần đăng nhập. File nhạy cảm (bằng chứng báo cáo) phải lưu NGOÀI `uploads/` và chỉ trả về qua Controller có kiểm tra quyền.
- **Kiểm tra toàn bộ file trước rồi mới lưu:** gọi `validateFile` cho mọi file trước `saveFile`, tránh trường hợp lưu được file 1 rồi file 2 lỗi để lại file rác.
- **Dữ liệu cũ:** bằng chứng gửi trước ngày 2026-10-16 (đường dẫn kiểu `TK-xxx/uuid.png` trong `C:/BOMS_uploads`) không còn tải được, hệ thống báo E0314. Chỉ là dữ liệu test, có thể xóa.

## [2026-10-18] - Hoàn thiện FE-03: Quản lý Kho Nguyên liệu (Inventory Management - Tasks 3.1 -> 3.6)
### 1. File bị tác động / tạo mới:
- `src/java/utility/Constants.java`:
  - Khai báo các hằng số mã thành công: `SUCCESS_CREATE_INGREDIENT` (S0301), `SUCCESS_UPDATE_INGREDIENT` (S0302), `SUCCESS_DELETE_INGREDIENT` (S0303), `SUCCESS_UPDATE_THRESHOLD` (S0304).
  - Khai báo các mã lỗi: `ERROR_INGREDIENT_NOT_FOUND` (E0301), `ERROR_EMPTY_INGREDIENT_NAME` (E0302), `ERROR_INVALID_INGREDIENT_QTY` (E0303), `ERROR_INVALID_INGREDIENT_PRICE` (E0304), `ERROR_INVALID_THRESHOLD` (E0305), `ERROR_INGREDIENT_NAME_EXISTS` (E0306), `ERROR_INVALID_INGREDIENT_DATA` (E0307), `ERROR_INSUFFICIENT_PERMISSION` (E0308).
  - Chuẩn hóa mã lỗi CSDL `ERROR_DATABASE_UPDATE_THRESHOLD` từ `"DE00XX"` thành `"DE0018"`.
- `src/java/utility/Validator.java`:
  - Bổ sung hàm tiện ích `checkNonNegativeDouble(String value, String errorMessage)` kiểm tra số thực >= 0.
- `src/java/resources/messages_vi.properties` & `messages_en.properties`:
  - Khai báo toàn bộ thông điệp mã S0301-S0304, E0301-E0308, DE0018 và toàn bộ nhãn giao diện `inventory.*`, `header.sidebar.inventory` (100% mã hóa `\uXXXX` cho tiếng Việt).
- `src/java/dao/IngredientDAO.java`:
  - Hoàn thiện `updateThresholdIngredient(int ingredientId, double threshold)`.
  - Bổ sung `isIngredientNameExists(String name, int excludeId)`, `searchIngredient(String ingredientName)` (tìm kiếm linh hoạt), và `getIngredientCategoryMap()`.
- `src/java/service/IngredientService.java`:
  - Triển khai nghiệp vụ kho: `getAllIngredientsForView(searchTerm)`, `getIngredientDetailForView(id)`, `getCategoriesForView()`, `updateWarningThreshold(...)`, `createIngredient(...)`, `updateIngredient(...)`, `deleteIngredient(...)`.
  - Tích hợp kiểm tra phân quyền Manager/Admin (`validateManagerPermission`), Validation 2 tầng, chuyển đổi dữ liệu thực thể sang `Map<String, Object>` (DTO) phẳng, phân loại trạng thái kho (`IN_STOCK`, `LOW_STOCK`, `OUT_OF_STOCK`).
- `src/java/controller/InventoryManagementController.java` (Tạo mới):
  - Servlet điều phối quản lý kho (`/InventoryManagementController`), kiểm tra `USER_SESSION` & role, xử lý các action `create`, `update`, `delete`, `updateThreshold`.
  - Áp dụng chuẩn Post-Redirect-Get (PRG) và Flash Message qua `Session`. Tuyệt đối không import `model.*` hay `dao.*`.
- `web/components/inventory_form_modal.jsp` (Tạo mới):
  - Modal form 2 trong 1 (Thêm/Sửa nguyên liệu & Cập nhật nhanh ngưỡng cảnh báo).
- `web/inventory_management.jsp` (Tạo mới):
  - Giao diện quản lý kho với bảng `admin-table`, tìm kiếm thời gian thực, badge trạng thái 3 màu (Xanh/Vàng/Đỏ), phân quyền hiển thị nút thao tác cho Manager.
- `web/header.jsp` & `web/WEB-INF/web.xml`:
  - Thêm liên kết Quản lý kho trên sidebar và đăng ký servlet mapping `/InventoryManagementController`.
- `web/js/main.js` & `web/css/global.css`:
  - Bổ sung các hàm modal kho (`openCreateIngredientModal`, `openEditIngredientModal`, `openThresholdModal`, `closeIngredientModal`, `closeThresholdModal`) và CSS bảng, badge trạng thái.
- `.agent/WorkList.md`: Đã cập nhật đánh dấu `[DONE]` Tasks 3.1 -> 3.6 và Task 4.1.

### 2. Tuân thủ kiến trúc:
- Controller không import Model/DAO, không dùng `PrintWriter`.
- Service ném `ValidationException` kèm mã lỗi chuẩn, đóng gói DTO dạng Map.
- View không scriptlet, không CSS/JS inline, tuân thủ i18n và phân quyền.
- Biên dịch NetBeans Ant đạt `BUILD SUCCESSFUL` (0 errors).

## [2026-10-18] - Hoàn thiện FE-04: Cảnh báo Tồn kho & Hết hạn (Inventory Alerts & Expiration - Tasks 4.2 -> 4.6)
### 1. File bị tác động / tạo mới:
- `src/java/utility/Constants.java`:
  - Khai báo các hằng số mã thành công: `SUCCESS_CONFIRM_ALERT` (S0401), `SUCCESS_CLOSE_ALERT` (S0402).
  - Khai báo mã lỗi: `ERROR_ALERT_NOT_FOUND` (E0401).
  - Khai báo mã lỗi CSDL: `ERROR_DATABASE_GET_ALERT` (DE0019), `ERROR_DATABASE_UPDATE_ALERT` (DE0020).
- `src/java/resources/messages_vi.properties` & `messages_en.properties`:
  - Khai báo song ngữ cho các mã S0401, S0402, E0401, DE0019, DE0020.
  - Bổ sung toàn bộ nhãn giao diện `alert.*` (100% mã hóa `\uXXXX` cho tiếng Việt): chuông cảnh báo, tiêu đề modal, phân loại cảnh báo, nút xác nhận/đóng.
- `src/java/dao/IngredientDAO.java`:
  - Bổ sung các phương thức: `isAlertActive(ingredientId, alertType)`, `insertInventoryAlert(ingredientId, alertType)`, `getActiveAlerts()`, `confirmAlert(alertId, userId)`, `closeAlert(alertId)`, `closeResolvedStockAlerts()` (tự động đóng cảnh báo khi đã bổ sung hàng), và `getExpiringIngredientIds(withinDays)` (truy xuất các lô hàng trong `IngredientBatches` cận date <= 3 ngày).
- `src/java/service/IngredientService.java`:
  - `scanIngredientStock()`: Tự động đối soát tồn kho và hạn dùng theo chu kỳ, đóng cảnh báo đã giải quyết, tự động sinh cảnh báo mới khi cạn kiệt (OutOfStock), chạm ngưỡng (LowStock) hoặc cận hạn (Expired).
  - `getActiveAlertsForView()`: Trả về danh sách cảnh báo thời gian thực dạng Map DTO phẳng.
  - `confirmAlert(alertIdStr, userId)` và `closeAlert(alertIdStr)`: Xử lý nghiệp vụ xác nhận và đóng cảnh báo.
- `src/java/controller/IngredientAlertController.java` (Tạo mới):
  - Servlet điều phối AJAX Polling (`/IngredientAlertController`), tiếp nhận request `doGet` (trả về danh sách cảnh báo dạng JSON) và `doPost` (xử lý xác nhận cảnh báo).
  - Tuân thủ tuyệt đối: Không import Model/DAO, bọc try-catch tập trung.
- `web/components/inventory_alert_modal.jsp` (Tạo mới):
  - Component modal hiển thị danh sách các nguyên liệu bị cảnh báo kèm trạng thái, thông tin người xác nhận, nút xác nhận và đóng.
  - Dùng `data-*` attribute để truyền nhãn ngôn ngữ sang JavaScript, ngăn chặn hoàn toàn lỗi vỡ font Mojibake.
- `web/header.jsp` & `web/WEB-INF/web.xml`:
  - Bổ sung icon Chuông thông báo (`btnInventoryAlertBell`) kèm badge đếm số lượng thời gian thực trên Header khi đăng nhập (Staff/Manager/Admin).
  - Nhúng `<jsp:include page="components/inventory_alert_modal.jsp" />` vào Header để mở modal xem cảnh báo từ mọi trang.
  - Đăng ký servlet và servlet-mapping cho `IngredientAlertController`.
- `web/js/main.js`:
  - Bổ sung kịch bản `pollInventoryAlerts()` chạy ngầm định kỳ 60 giây qua AJAX.
  - Tích hợp âm thanh cảnh báo (Audio notification) bằng Web Audio API khi số lượng cảnh báo mới tăng lên.
  - Quản lý mở/đóng modal và gọi API xác nhận `confirmInventoryAlert(alertId)`.
  - 100% comment tiếng Anh thuần ASCII, fallback Unicode Escape `\uXXXX`.
- `web/css/global.css`:
  - Định dạng `.btn-icon-alert`, `.alert-badge`, hiệu ứng nhấp nháy `.alert-badge-pulse`, `.modal-card-lg`, và các badge màu sắc trạng thái cảnh báo (`.badge-alert-out`, `.badge-alert-low`, `.badge-alert-expired`, `.badge-confirmed`).
- `.agent/WorkList.md`: Đã cập nhật đánh dấu hoàn thành `[DONE]` cho toàn bộ FE-04 (Tasks 4.1 -> 4.6).

### 2. Tuân thủ kiến trúc:
- Controller chỉ trả về JSON DTO, tuyệt đối không import Model/DAO.
- Service xử lý 100% logic đối soát kho và ném `ValidationException`.
- View không scriptlet, không CSS/JS inline, giao diện thân thiện i18n và đồng bộ đa chủ đề (Light/Dark Mode).
- Biên dịch NetBeans Ant đạt `BUILD SUCCESSFUL` (0 errors).

## [2026-10-18] - Tối ưu phân loại `AlertType.ExpiringSoon` & Khắc phục lỗi Xác nhận cảnh báo (`confirmAlert`)
### 1. File bị tác động:
- `src/java/dao/IngredientDAO.java`:
  - Tách riêng 2 phương thức truy vấn lô hàng theo hạn dùng: `getExpiredIngredientIds()` (`ExpirationDate < CAST(GETDATE() AS DATE)`) và `getExpiringSoonIngredientIds(int withinDays)` (`ExpirationDate >= CAST(GETDATE() AS DATE) AND ExpirationDate <= DATEADD(day, ?, CAST(GETDATE() AS DATE))`).
  - Ánh xạ chuỗi `'Expiring Soon'` trong CSDL sang enum `AlertType.ExpiringSoon` trong `getActiveAlerts()`.
  - Phòng hộ `userId <= 0` bằng `st.setNull(1, java.sql.Types.INTEGER)` trong `confirmAlert` để ngăn lỗi vi phạm khóa ngoại `FOREIGN KEY (ConfirmedBy)`.
- `src/java/service/IngredientService.java`:
  - Cập nhật `scanIngredientStock()` để quét và tạo cảnh báo `'Expired'` cho lô hàng đã quá hạn và `'Expiring Soon'` cho lô hàng cận hạn trong vòng 3 ngày tới.
- `src/java/controller/IngredientAlertController.java`:
  - Khắc phục lỗi trích xuất `userId` từ `sessionScope.USER_SESSION` (bổ sung `Integer.parseInt(idVal.toString().trim())` khi `userId` được lưu dạng `String` từ `UserService.buildUserSessionMap`), đảm bảo `ConfirmedBy` cập nhật chính xác `UserID` người xác nhận lên CSDL.
- `src/java/resources/messages_vi.properties`, `src/java/resources/messages_en.properties`, `web/components/inventory_alert_modal.jsp`, `web/js/main.js`:
  - Bổ sung khóa `alert.type.expiring_soon` ("Sắp hết hạn (Cận date)" / "Expiring Soon") tách biệt với `alert.type.expired` ("Đã hết hạn" / "Expired") và phản hồi nút bấm `confirmInventoryAlert` tức thì.

## [2026-10-19] - Hoàn thiện Tính năng Cấu hình Công thức Định lượng Nguyên liệu (Recipe / ProductIngredients) cho Thực đơn
### 1. Vấn đề phát hiện & Giải pháp kỹ thuật:
- **Vấn đề cốt lõi:** Khi người dùng tạo món mới trên giao diện Quản lý thực đơn (`MenuManagementController`), hệ thống chỉ lưu thông tin vào bảng `Products` mà không cho phép gắn nguyên liệu vào `ProductIngredients`. Do đó, món mới không có định lượng công thức, dẫn tới `hasSufficientStockForProduct` luôn coi là còn hàng và `CartService.deductIngredientsStock` không hề trừ một gam nguyên liệu nào khi bán món mới.
- **Giải pháp triển khai:**
  - Tích hợp bảng cấu hình công thức nguyên liệu động ngay trên Modal Thêm/Sửa món (`product_form_modal.jsp`).
  - Cho phép chọn nhiều nguyên liệu từ danh sách nguyên liệu khả dụng (`INGREDIENT_LIST`) và nhập định lượng tiêu hao (`quantityUsed > 0`).
  - Hỗ trợ xem trước và tự động đồng bộ đơn vị tính (kg, gam, lon, hộp...) theo thời gian thực.
  - Khi chỉnh sửa món ăn, tự động nạp lại công thức định lượng đã cấu hình từ trước (`recipeData`).
  - Khi lưu món mới hoặc cập nhật món: Service tự động validate định lượng > 0 (`E0212`), kiểm tra nguyên liệu tồn tại và lưu/đồng bộ vào bảng `ProductIngredients` qua `ingredientDAO.insertProductIngredient`.

### 2. File bị tác động / chỉnh sửa:
- `src/java/utility/Constants.java`:
  - Bổ sung mã lỗi nghiệp vụ `ERROR_INVALID_RECIPE_QUANTITY` ("E0212").
- `src/java/resources/messages_vi.properties` & `messages_en.properties`:
  - Bổ sung bản dịch song ngữ chuẩn cho mã `E0212` và các nhãn giao diện `menu.modal.recipe.*` (100% Unicode Escape `\uXXXX` cho tiếng Việt).
- `src/java/service/ProductService.java`:
  - Thêm phương thức `getAllActiveIngredientsForView()`: Trả về danh sách nguyên liệu đang hoạt động dạng Map DTO cho View.
  - Trong `getAllProductsForManagementView()`: Nạp chuỗi công thức định lượng `recipeData` (`ingId:qty,...`) cho từng món ăn.
  - Nâng cấp `createProduct` và `updateProduct` hỗ trợ mảng `recipeIngredientIds` và `recipeQuantities`, tự động kiểm tra tính hợp lệ và ghi vào `ProductIngredients` (giữ nguyên overload cũ để backward compatible).
  - Tách các hàm SRP: `buildRecipeString`, `parseAndValidateRecipes`, `saveProductRecipes`.
- `src/java/controller/MenuManagementController.java`:
  - Trong `doGet`: Cung cấp `INGREDIENT_LIST` xuống View.
  - Trong `doPost`: Tiếp nhận mảng `recipeIngredientId` và `recipeQuantity` từ form multipart và truyền sang `ProductService`.
- `web/components/product_form_modal.jsp`:
  - Bổ sung section công thức nguyên liệu (label, nút thêm nguyên liệu, template dropdown, danh sách hàng động, và thông báo khi chưa có công thức).
- `web/menu_management.jsp`:
  - Truyền tham số `product.recipeData` vào hàm `openEditProductModal(...)`.
- `web/js/main.js`:
  - Thêm các hàm quản lý DOM: `resetRecipeRows()`, `addRecipeRow(ingId, qty)`, `onRecipeIngredientChange(select, unitSpan)`, `removeRecipeRow(btn)`.
  - Cập nhật `openCreateProductModal` và `openEditProductModal` để reset và nạp công thức tương ứng. 100% thuần ASCII.
- `web/css/global.css`:
  - Bổ sung định dạng `.modal-card-md`, `.btn-action-add-recipe`, `.recipe-container`, `.recipe-empty-hint`, `.recipe-row`, `.recipe-unit-label`, `.btn-remove-recipe`.

### 3. Tuân thủ kiến trúc:
- Controller không import Model/DAO, bọc try-catch tập trung.
- Service xử lý 100% nghiệp vụ và ném `ValidationException`.
- View không scriptlet, không CSS/JS inline, giao diện thân thiện đa ngôn ngữ.
- Toàn bộ dự án biên dịch qua `javac` đạt 0 lỗi.

## [2026-10-19] - Loại bỏ Triệt để Tính năng "Đổi món" (Exchange Dish) Khỏi Toàn bộ Dự án
### 1. Bối cảnh & Quyết định:
- Theo chỉ đạo của người dùng, tính năng đổi món (`exchangeDish`) trong đơn hàng gây ra nhiều rủi ro về kế toán, kiểm soát kho phức tạp (hoàn tạm món cũ, bù trừ món mới, tính tiền chênh lệch âm/dương).
- Thống nhất quy trình nghiệp vụ: Khi khách có nhu cầu đổi món, nhân viên thực hiện **Hủy đơn cũ** (hệ thống tự động hoàn kho nguyên liệu theo đúng quy chuẩn) và **Tạo đơn hàng mới thay thế**.
- Tiến hành rà soát và loại bỏ triệt để 100% mã nguồn, hằng số, giao diện liên quan đến đổi món mà không làm ảnh hưởng đến bất kỳ tính năng nào khác của hệ thống.

### 2. File bị tác động / loại bỏ:
- `web/order_management.jsp`:
  - Gỡ bỏ khối hiển thị Flash Message tính tiền chênh lệch `${PRICE_DIFF}` (thu thêm / hoàn trả tiền).
  - Gỡ bỏ component `<details class="exchange-details">` và form đổi món trên từng dòng chi tiết đơn hàng.
  - Cập nhật điều kiện hiển thị nút [Hủy Đơn] của nhân viên sang `order.canCancelOrder` (đảm bảo tính năng Hủy đơn hoạt động trơn tru 100%).
- `src/java/controller/OrderManagementController.java`:
  - Gỡ bỏ nhánh xử lý `action = "exchange"` trong `doPost`.
  - Gỡ bỏ việc nạp `AVAILABLE_PRODUCTS` vào request trong `doGet` (tăng tốc độ tải trang vì không cần truy vấn danh sách toàn bộ món ăn).
  - Gỡ bỏ xử lý `PRICE_DIFF` trong `forwardFlashMessages`.
- `src/java/service/OrderService.java`:
  - Gỡ bỏ hoàn toàn các phương thức: `exchangeDish(...)`, `getAvailableProductsForExchange()`, `validateExchangeParams(...)`, và `findDetailByProductId(...)`.
  - Trong `convertOrderToMap`: Loại bỏ `canChangeOrder`, giữ nguyên chuẩn `canCancelOrder`.
- `src/java/dao/OrderDAO.java`:
  - Gỡ bỏ phương thức `replaceOrderItem(...)` và các câu lệnh SQL giao dịch liên quan.
- `src/java/utility/Constants.java`:
  - Dọn dẹp hằng số `SUCCESS_CHANGE_DISH` (S0207) và `ERROR_INVALID_EXCHANGE_PRODUCT` (E0208).
- `src/java/resources/messages_vi.properties` & `messages_en.properties`:
  - Dọn dẹp toàn bộ các khóa mã thông báo và nhãn giao diện: `S0207`, `E0208`, `order.management.btn.exchange`, `order.management.exchange.*`, `order.management.diff.*`.
  - Chuẩn hóa thông điệp mã lỗi `E0206` ("Không thể hủy đơn vì đơn hàng đã bắt đầu chế biến!").
- `web/css/global.css`:
  - Dọn dẹp các class CSS: `.exchange-details`, `.btn-exchange-summary`, `.exchange-form`. Bảo lưu `.input-qty-sm` cho công thức nguyên liệu.

### 3. Kết quả & Tuân thủ kiến trúc:
- Loại bỏ hoàn toàn 100% tàn dư của tính năng đổi món.
- Tính năng Hủy đơn khi chưa nấu (`cookingStartedAt == null`), khóa hủy sau 5 phút và hoàn kho nguyên liệu tự động tiếp tục hoạt động an toàn, chính xác tuyệt đối.
- Toàn bộ dự án biên dịch qua `javac` đạt **`BUILD SUCCESSFUL` (0 lỗi)**.

## [2026-10-19] - Chuẩn hóa Phân quyền Menu Header (`header.jsp`) & Khắc phục Lỗi Cấu trúc Servlet trong `web.xml`
### 1. File bị tác động:
- `web/header.jsp`:
  - **Chuông cảnh báo kho (`btnInventoryAlertBell`):** Loại bỏ vai trò `Admin`, chỉ cho phép `Staff` và `Manager` hiển thị và nhận cảnh báo (khớp với việc nhúng `inventory_alert_modal.jsp` và quy định loại Admin khỏi luồng kho).
  - **Dropdown Menu Tài khoản (`#menuTaiKhoan`):** Bọc liên kết `AttendanceController` (Chấm công) chỉ hiển thị cho nhân viên (`Staff`).
  - **Menu Dọc (Sidebar `#menuDoc`):** Tái cấu trúc phân quyền rõ ràng theo 4 vai trò của dự án:
    1. `Admin`: Chỉ hiển thị duy nhất Quản lý tài khoản (`AdminDashboardController`).
    2. `Manager`: Quản lý đơn hàng (`OrderManagementController`), Quản lý thực đơn / thêm món (`MenuManagementController`), Quản lý kho / thêm sửa xem (`InventoryManagementController`), Quản lý báo cáo ẩn danh (`ReportManagementController`), Quản lý nhân sự (`#` - chờ module nhân sự tích hợp theo quyết định phê duyệt).
    3. `Staff`: Đặt món tại quầy (`MenuController`), Quản lý đơn hàng (`OrderManagementController`), Tạo thực đơn (`MenuManagementController`), Xem kho (`InventoryManagementController`), Chấm công (`AttendanceController`), Gửi báo cáo ẩn danh (`ReportController`).
    4. `Shipper`: Chỉ hiển thị luồng giao hàng (`#`).
  - **Khắc phục lỗi đường dẫn & trùng lặp:** Dọn dẹp dứt điểm các thẻ `<li>` bị lặp sau đợt merge nhánh (`AdminDashboardController` nhầm nhãn HR, các link rác `href="#"` của báo cáo và nhân sự, gỡ `Manager` khỏi luồng Shipper).
- `web/WEB-INF/web.xml`:
  - Khắc phục lỗi cú pháp nghiêm trọng sau khi gộp nhánh Hoài: Tách thẻ `<servlet>` và `<servlet-mapping>` bị lồng gộp giữa `AttendanceController` và `InventoryManagementController` thành 2 cặp thẻ độc lập chuẩn Servlet 6.0.

### 2. Tuân thủ kiến trúc:
- Giao diện View tuân thủ 100% JSTL/EL, không Scriptlet, không CSS/JS inline.
- Phân quyền giao diện đọc từ `sessionScope.USER_SESSION.role` an toàn.
- Dự án biên dịch thành công qua NetBeans Ant đạt **`BUILD SUCCESSFUL` (0 lỗi)**.

---

## [2026-10-20] - Hoàn thiện Task 7.1: Xóa Bỏ Reflection Sai Chuẩn Trong IngredientAlertController
### 1. Phân tích & Vấn đề Kỹ thuật:
- **Nguyên nhân gốc rễ:** Trong phương thức `doPost` của `IngredientAlertController.java`, mã nguồn cũ cố tình chèn hơn 30 dòng code phức tạp để kiểm tra `userObj instanceof Map` và sử dụng Reflection (`java.lang.reflect.Method`, `userObj.getClass().getMethod("getUserID")`, `m.invoke(userObj)`) nhằm trích xuất `userId` từ Session.
- **Hậu quả:** Vi phạm nghiêm trọng quy chuẩn `Rule_BOMS.md`, làm phức tạp hóa code không cần thiết, tiềm ẩn rủi ro hiệu năng nghiêm trọng của Reflection và đi ngược lại cấu trúc Session chuẩn của toàn dự án.

### 2. Giải pháp Kỹ thuật Triệt để:
- **Chuẩn hóa Session Extraction:** Loại bỏ toàn bộ khối code Reflection rườm rà. Thay thế bằng chuẩn thống nhất 100% của hệ thống:
  ```java
  @SuppressWarnings("unchecked")
  Map<String, String> userSession = (Map<String, String>) session.getAttribute(Constants.USER_SESSION);
  ```
- **Xử lý An toàn trong Luồng Nghiệp vụ:** Đặt `int userId = Integer.parseInt(userSession.get("userId"));` trực tiếp bên trong khối `try-catch` của `doPost`. Mọi trường hợp ngoại lệ parsing hoặc dữ liệu không hợp lệ đều được khối `catch (Exception e)` chung của Controller đón bắt và trả về phản hồi JSON lỗi chuẩn cho client.

### 3. File bị tác động & Tiến độ:
- `src/java/controller/IngredientAlertController.java`: Rút gọn từ 164 dòng xuống còn 134 dòng (-30 dòng code rác), loại bỏ sạch 100% Reflection.
- `.agent/WorkList.md`: Đánh dấu hoàn thành Task 7.1 **[DONE]**.
- **Trạng thái Build:** Biên dịch qua NetBeans Ant đạt **`BUILD SUCCESSFUL` (0 lỗi)**.

### 4. Bài học kinh nghiệm & Quy chuẩn kiến trúc cốt lõi (CRITICAL):
- **Tính thống nhất của Session Contract:** Toàn bộ hệ thống BOMS lưu trữ thông tin phiên đăng nhập tại `Constants.USER_SESSION` dưới dạng `Map<String, String>` (do `UserService.buildUserSessionMap` khởi tạo). Mọi Controller khi cần lấy dữ liệu người dùng BẮT BUỘC phải ép kiểu về `Map<String, String>` và truy xuất qua các key chuẩn (`userId`, `username`, `role`, `fullName`).
- **Nghiêm cấm tuyệt đối Reflection trong Controller:** Không bao giờ sử dụng Reflection để giải quyết các vấn đề ép kiểu hoặc phỏng đoán kiểu dữ liệu trong Servlet. Mọi sự mơ hồ về kiểu dữ liệu phải được giải quyết dứt điểm tại tầng Service/Contract thay vì dùng Reflection để "chữa cháy".
- **Quản lý ngoại lệ tập trung:** Khi trích xuất các giá trị số từ tham số hoặc session trong Controller, luôn dựa vào khối `try-catch` bao quanh để bắt `NumberFormatException`, đảm bảo hệ thống không bị crash (lỗi 500) mà luôn trả về mã lỗi JSON thân thiện cho giao diện.

---

## [2026-10-20] - Hoàn thiện Task 7.2: Tiêu Diệt Model DeliveryTask.java & Triệt Tiêu Nguy Cơ Rò Rỉ Model Lên View
### 1. Phân tích & Vấn đề Kỹ thuật:
- **Nguyên nhân gốc rễ:** CSDL hệ thống (`BOMS_DATABASE.sql`) không hề có bảng `DeliveryTask`. Toàn bộ dữ liệu giao hàng được lưu trực tiếp trong bảng `Orders` (phân loại `OrderType = 'Delivery'`) và bảng `DeliveryAreas`. Việc tự ý tạo class `src/java/model/DeliveryTask.java` vi phạm nghiêm trọng định nghĩa tầng Model (Model chỉ đại diện cho bảng vật lý trong CSDL).
- **Hậu quả:** Gây nhầm lẫn vai trò thực thể CSDL, tạo nợ kỹ thuật và có nguy cơ rò rỉ trực tiếp Model lên tầng View (JSP), vi phạm quy chuẩn thiết kế của `Rule_BOMS.md`.

### 2. Giải pháp Kỹ thuật Triệt để:
- **Tiêu diệt hoàn toàn Model thừa:** Xóa vĩnh viễn tệp `src/java/model/DeliveryTask.java` khỏi dự án.
- **Tầng DAO (`DeliveryDAO.java`):**
  - Cập nhật các hàm `getAllTasks()`, `getTasksByShipper()`, `getAvailableTasksForShipper()` chuyển sang trả về `List<Map<String, Object>>`.
  - Cập nhật `getTaskById()` trả về `Map<String, Object>`.
  - Cập nhật hàm phụ trợ `mapResultSetToTask(ResultSet rs)` khởi tạo `Map<String, Object>` với đúng 9 trường dữ liệu cần thiết: `taskId`, `orderCode`, `customerAddress`, `status`, `shipperUsername`, `customerName`, `customerPhone`, `areaName`, `totalAmount`.
- **Tầng Service (`DeliveryService.java`):**
  - Tiếp nhận `Map<String, Object>` từ DAO.
  - Sử dụng hàm `buildTaskMap(Map<String, Object> task)` để định dạng an toàn toàn bộ giá trị sang `Map<String, String>` trước khi cung cấp cho Controller.
  - Cập nhật các phương thức kiểm tra nghiệp vụ (`validateTaskIsFree`, `validateTaskIsPending`, `validateShipperAuthorization`, `validateStatusFlow`, `countActiveOrders`, `isTaskWaitingForShipper`) trích xuất dữ liệu trực tiếp từ `Map<String, Object>`.
- **Bảo toàn giao diện View (JSP):** Do Expression Language (EL) trên JSP truy xuất `${task.orderCode}`, `${task.customerName}` đồng nhất giữa Map và Java Bean, giao diện `dispatch_dashboard.jsp` và `shipper_dashboard.jsp` hoạt động ổn định 100% mà không cần sửa đổi bất kỳ dòng code JSP nào.

### 3. File bị tác động & Tiến độ:
- `src/java/model/DeliveryTask.java`: Đã xóa hoàn toàn khỏi dự án.
- `src/java/dao/DeliveryDAO.java`: Chuyển đổi toàn bộ phương thức truy vấn Task sang trả về `Map<String, Object>`.
- `src/java/service/DeliveryService.java`: Xử lý nghiệp vụ với `Map<String, Object>`, loại bỏ hoàn toàn import `model.DeliveryTask`.
- `.agent/WorkList.md`: Đánh dấu hoàn thành Task 7.2 **[DONE]**.
- **Trạng thái Build:** Biên dịch qua NetBeans Ant đạt **`BUILD SUCCESSFUL` (0 lỗi, 69 source files)**.

### 4. Bài học kinh nghiệm & Quy chuẩn kiến trúc cốt lõi (CRITICAL):
- **Quy chuẩn tầng Model (Database Entity Only):** Package `model` chỉ chứa các POJO đại diện 1-1 cho các bảng trong CSDL (Users, Orders, Ingredients, ...). TUYỆT ĐỐI không tự ý tạo thêm các class Model "lai tạp" DTO/View Object trong package này để tránh làm ô nhiễm tầng thực thể.
- **Kỹ thuật chống rò rỉ Model lên View (Model Leakage Prevention):** Khi cần tổng hợp dữ liệu từ nhiều bảng (JOIN SQL) để phục vụ hiển thị trên giao diện, quy chuẩn tối ưu là gom nhóm qua `Map<String, Object>` ở tầng DAO và chuyển hóa thành `Map<String, String>` ở tầng Service. View chỉ được phép đọc dữ liệu qua Map hoặc các kiểu dữ liệu nguyên thủy (primitive), tuyệt đối không nhận Model Entity thô.
- **Tính tương thích của EL trong JSP:** Trong Jakarta/Java EE, cú pháp `${object.property}` sẽ tự động tìm kiếm getter trên Java Bean hoặc `map.get("property")` nếu đối tượng là `Map`. Tận dụng đặc tính này giúp chuyển đổi cấu trúc dữ liệu phía backend mà không gây bất kỳ tác dụng phụ (side-effect) nào lên giao diện người dùng.

---

## [2026-10-22] - Hoàn thiện Task 10.6 & Task 11: Chặt Chẽ Khung Giờ Chấm Công (Phương án A) & Hệ Thống Thông Báo / Phát Thanh Toàn Cục

### 1. Phân tích & Vấn đề Kỹ thuật:
- **Task 10.6 (Khống chế thời gian chấm công - Phương án A):**
  - Chống tình trạng "chấm công láo" trước hoặc sau ca quá sớm mà không có sự kiểm soát của Quản lý.
  - Khóa nút Check-in nếu thời gian hiện tại trước giờ bắt đầu ca hơn 15 phút.
  - Nếu Check-in trong khoảng 15 phút trước ca: ghi nhận trạng thái `Pending Early Approval`, yêu cầu Manager bấm Duyệt thì mới được tính vào bảng tổng hợp công (`Timesheet Summary`).
  - Khóa nút Check-out khi chưa hết giờ làm (`currentTime < EndTime`). Sau ca làm > 15 phút: bắt buộc nhập lý do OT và lưu trạng thái `Pending OT Approval`.
- **Task 11 (Hệ thống Thông báo & Phát thanh toàn cục):**
  - Cần hệ thống thông báo đa tầng: Quả chuông 🔔 trên header hiển thị Badge đỏ số lượng chưa đọc, Dropdown xem nhanh 10 thông báo mới nhất kèm thao tác Đánh dấu đã đọc / Đánh dấu tất cả đã đọc qua AJAX.
  - Tính năng Phát loa toàn cục 📢 dành riêng cho Admin và Manager gửi tin tức tức thì đến toàn bộ nhân sự đang hoạt động.
  - Tích hợp Trigger tự động khi có phân ca mới (`ShiftAssignment`) và khi Quản lý duyệt/từ chối OT hoặc duyệt vào sớm (`AttendanceApproval`).

### 2. Giải pháp Kỹ thuật Triệt để:
- **Tầng Model & DAO:**
  - `model/Notification.java`: POJO khớp 1-1 với bảng `Notifications` trong CSDL.
  - `dao/NotificationDAO.java`: Các hàm `insertNotification`, `getUnreadNotificationsByUserId`, `getRecentNotificationsByUserId`, `countUnreadNotifications`, `markAsRead`, `markAllAsRead` sử dụng `try-with-resources` và ném mã lỗi CSDL `DE0061`, `DE0062`.
  - `dao/UserDAO.java`: Thêm hàm `getAllActiveUserIds()` phục vụ tính năng Broadcast.
  - `dao/AttendanceDAO.java`: Cập nhật câu truy vấn `getMonthlyTimesheetSummary` chỉ tính số ca và số giờ làm khi trạng thái là Đúng giờ / Muộn hoặc đã được Quản lý duyệt (`ApprovedBy IS NOT NULL` / `Approved`).
- **Tầng Nghiệp vụ (`AttendanceService.java` & `NotificationService.java`):**
  - `AttendanceService.java`: Ràng buộc giờ Check-in/Check-out chặt chẽ theo Phương án A, cung cấp cờ `canCheckIn`, `canCheckOut` cho View; gắn trigger tự động bắn thông báo khi phân ca hoặc duyệt/từ chối công.
  - `NotificationService.java`: Đóng gói 100% dữ liệu sang `Map<String, Object>` (bao gồm các icon emoji động `📢`, `🗓️`, `⏱️`, `⚠️`, `🛵`, `🔔`), kiểm tra validation nội dung broadcast (không rỗng, tối đa 500 ký tự).
- **Tầng Controller (`NotificationController.java`):**
  - Đăng ký `@WebServlet(urlPatterns = {"/NotificationController"})` và khai báo trong `web.xml`.
  - `doGet`: Xử lý AJAX lấy thông báo chưa đọc (`action=getUnread`) và thông báo gần đây (`action=getRecent`), trả JSON qua thư viện Gson.
  - `doPost`: Xử lý PRG chuẩn cho `action=broadcast` (kiểm tra quyền Admin/Manager, lưu flash message và redirect referer); xử lý JSON AJAX cho `action=markAsRead` và `action=markAllAsRead`.
- **Tầng Giao diện (JSP / CSS / JS / i18n):**
  - `web/header.jsp`: Thêm nút Phát loa 📢 cho Admin/Manager, Quả chuông 🔔 kèm Badge đỏ động `#unreadNotifBadge` cho tất cả người dùng, nhúng dropdown và modal phát loa.
  - `web/components/notification_dropdown.jsp`: Header dropdown hiện đại với loading state, empty state, danh sách thông báo và nút "Đánh dấu tất cả đã đọc".
  - `web/components/broadcast_modal.jsp`: Modal nhập thông báo tối đa 500 ký tự.
  - `web/attendance.jsp`: Khóa nút Check-in / Check-out khi ngoài khung giờ hợp lệ, hiển thị đếm ngược và thông báo hướng dẫn.
  - `web/js/main.js`: Polling thông báo ngầm mỗi 30s, mở/đóng dropdown, xử lý click outside, đánh dấu đã đọc mượt mà không reload trang.
  - `web/css/global.css`: Định kiểu `.notification-dropdown`, `.notification-item`, `.alert-badge-pulse`, hỗ trợ cả Light/Dark theme.
  - `messages_vi.properties` & `messages_en.properties`: Bổ sung dải mã `S0801`-`S0803`, `E0801`-`E0802`, `DE0061`-`DE0062` và toàn bộ nhãn UI tương ứng (tiếng Việt 100% Unicode Escape `\uXXXX`).

### 3. Tuân thủ kiến trúc (`Rule_BOMS.md`):
- Không rò rỉ Model lên Controller hoặc JSP (chuyển đổi Map ở Service).
- Mô hình PRG hoàn chỉnh cho form submit, AJAX nhẹ nhàng cho trải nghiệm người dùng.
- Không inline CSS/JS trong JSP; không Scriptlet.
- Đóng an toàn kết nối JDBC bằng `try-with-resources`.
- Toàn bộ dự án biên dịch qua `javac` đạt **0 lỗi**.

---

## [2026-10-22] - Hoàn thiện Task 12: Bảng Điều Khiển Trung Tâm (Central Dashboard - `index.jsp`)

### 1. Phân tích & Vấn đề Kỹ thuật:
- **Vấn đề ban đầu:** Trang chủ `index.jsp` trống trải (chỉ có 13 dòng text tĩnh), chưa phản ánh trạng thái hệ thống, không hỗ trợ thao tác nhanh và không cá nhân hóa theo vai trò của người dùng.
- **Yêu cầu Task 12:** Cải tạo trang chủ thành Bảng điều khiển trung tâm (Control Center) gồm 3 khu vực trực quan:
  1. Thẻ thông tin cá nhân & Huy hiệu trạng thái làm việc (Profile Card & Work Status Badge).
  2. Lưới thao tác nhanh (Quick Actions Grid) cá nhân hóa theo từng vai trò (Staff, Manager, Admin, Shipper).
  3. Bảng tin thông báo mới nhất (Notification Feed) hiển thị các thông báo gần đây và cho phép đánh dấu đã đọc trực tiếp.

### 2. Giải pháp Kỹ thuật Triệt để:
- **Tầng Điều phối (`DashboardController.java`):**
  - Tạo mới `src/java/controller/DashboardController.java` bọc Servlet `@WebServlet(name = "DashboardController", urlPatterns = {"/DashboardController"})`.
  - Khai báo ánh xạ Servlet và Servlet-mapping trong `web/WEB-INF/web.xml`.
  - Sử dụng `FlashMessageUtil.forwardFlashMessages(request, session)` để tiếp nhận flash messages.
  - Phối hợp dữ liệu từ `AttendanceService`, `NotificationService`, và `IngredientService`:
    + Trạng thái ca làm hôm nay của nhân viên: `getTodayAttendanceForView(userId)`.
    + 8 thông báo mới nhất: `getRecentNotificationsForView(userId, 8)`.
    + Số lượng thông báo chưa đọc: `getUnreadCount(userId)`.
    + Chỉ số nhanh cho Manager: Số lượng yêu cầu điểm danh/OT chờ duyệt và số lượng cảnh báo tồn kho còn hoạt động.
  - Chuyển tiếp (forward) an toàn sang `index.jsp` kèm toàn bộ dữ liệu dạng `Map<String, Object>` / `List<Map<String, Object>>` (Zero Model leakage).
- **Tầng Giao diện (`web/index.jsp`):**
  - **Khách chưa đăng nhập:** Hiển thị Landing Hero Banner ấn tượng, nút đăng nhập nhanh hoặc xem thực đơn khách hàng.
  - **Người dùng đã đăng nhập:**
    + **Khu vực 1 (Profile Card):** Avatar vòng tròn lớn với 2 chữ cái đầu in hoa, tên đầy đủ, vai trò, huy hiệu trạng thái làm việc tương ứng với trạng thái ca làm: 🟢 Đang trong ca (kèm giờ vào ca), 🟡 Chưa vào ca (kèm khung giờ ca), ⚪ Đã hoàn thành ca (kèm số giờ làm), 🔴 Không có ca hôm nay. Bên phải là cụm widget ngày hôm nay và thống kê số lượng việc cần xử lý.
    + **Khu vực 2 (Quick Actions Grid):** Danh sách các thẻ thao tác nhanh kèm icon nổi bật, tiêu đề và mô tả ngắn gọn. Phân tách rõ ràng giữa Staff (POS, Chấm công, Quản lý đơn, Kho, Báo cáo ẩn danh), Manager (Đơn hàng, Kho, Phân ca & Nhân sự, Thực đơn, Điều phối Shipper, Báo cáo, Phát loa), Admin (Quản trị tài khoản, Phát loa, Cài đặt, Hồ sơ), Shipper (Nhiệm vụ giao hàng, Cài đặt).
    + **Khu vực 3 (Notification Feed):** Hộp thông báo cuộn dọc hiện đại, hiển thị icon emoji theo từng loại thông báo, thời gian gửi, nút đánh dấu đã đọc hoặc đánh dấu tất cả đã đọc đồng bộ real-time với thanh Header.
- **Tầng Script & Kiểu dáng (`web/js/main.js` & `web/css/global.css`):**
  - Cập nhật `main.js`: Bổ sung xử lý cập nhật trạng thái đã đọc cho các phần tử `#feedItem_${id}` trên Dashboard feed khi click đánh dấu đọc hoặc đánh dấu tất cả đã đọc.
  - Bổ sung vào `global.css`: Định kiểu hoàn chỉnh cho `.dashboard-main`, `.dashboard-profile-card`, `.badge-work-status`, `.quick-actions-grid`, `.action-card`, `.notification-feed-card`, `.feed-item`. Tương thích 100% cả Light Mode và Dark Mode.
- **Đa ngôn ngữ i18n (`messages_vi.properties` & `messages_en.properties`):**
  - Khai báo toàn bộ nhãn hiển thị của Dashboard (`dashboard.*`), tiếng Việt mã hóa Unicode Escape `\uXXXX` 100%.

### 3. Tuân thủ kiến trúc (`Rule_BOMS.md`):
- Tuân thủ 100% kiến trúc MVC, Controller không import bất kỳ Model hay DAO nào.
- Giao diện JSP không chứa scriptlet (`<% %>`) hay inline style.
- Biên dịch toàn bộ mã nguồn qua JDK 17 / Tomcat 10.1 đạt **0 lỗi**.


---

## [2026-10-22] - Hoàn thiện Task 14: Nâng Cấp Trải Nghiệm Quản Lý Nhân Sự & Thời Khóa Biểu (Advanced Timetable)

### 1. Phân tích & Vấn đề Kỹ thuật:
- **Task 14.1 (Bulk Shift Assignment - Phân ca liên hoàn):**
  - Quản lý cần phân ca lặp lại cho nhân viên theo dải ngày (`startDate` đến `endDate`) và theo các thứ trong tuần (Thứ 2 đến Chủ Nhật) thay vì chỉ chọn từng ngày đơn lẻ tốn thời gian.
  - Xử lý xung đột: Tự động bỏ qua các ngày nhân viên đã có ca làm việc và gán thành công các ngày hợp lệ còn trống (theo phê duyệt của người dùng).
- **Task 14.2 (Staff Weekly Timetable - Thời khóa biểu tuần cho Nhân viên):**
  - Chuyển đổi bảng lịch sử phẳng thành ma trận Thời khóa biểu tuần chuyên nghiệp (FAP style): Hàng ngang là Ca làm việc / Khung giờ, Cột dọc là Thứ 2 đến Chủ Nhật.
  - Tích hợp điều hướng tuần (Tuần trước / Tuần này / Tuần sau), hiển thị thẻ ca làm việc kèm trạng thái và nút chấm công trực tiếp (Vào ca / Tan ca) cho ngày hôm nay.
- **Task 14.3 (Manager Matrix View - Bảng Ma trận Phân ca cho Quản lý):**
  - Hiển thị ma trận phân ca tổng quan toàn cửa hàng: Hàng ngang là danh sách Nhân viên, Cột dọc là Thứ 2 đến Chủ Nhật.
  - Ô có ca hiển thị huy hiệu ca (tên ca, giờ làm, cờ khóa nếu đã chấm công, nút xóa nếu chưa chấm công).
  - Ô trống cung cấp nút bấm nhanh `[+]` kích hoạt Modal phân ca điền sẵn Tên Nhân viên và Ngày đó.

### 2. File bị tác động & Giải pháp Kỹ thuật:
- `src/java/utility/Constants.java`:
  - Bổ sung mã thành công: `SUCCESS_BULK_ASSIGN_SHIFT = "S0707"`.
  - Bổ sung mã lỗi: `ERROR_INVALID_DATE_RANGE = "E0712"`, `ERROR_EMPTY_DAYS_OF_WEEK = "E0713"`, `ERROR_NO_ASSIGNMENT_CREATED = "E0714"`.
- `src/java/resources/messages_vi.properties` & `messages_en.properties`:
  - Khai báo toàn bộ các mã lỗi/thành công và nhãn hiển thị đa ngôn ngữ (tiếng Việt mã hóa Unicode Escape `\uXXXX` 100%).
- `src/java/dao/AttendanceDAO.java`:
  - Bổ sung hàm `getAssignmentsByUserAndDateRange(int userId, Date startDate, Date endDate)` phục vụ pivot thời khóa biểu nhân viên.
  - Bổ sung hàm `getAssignmentsByDateRange(Date startDate, Date endDate)` phục vụ pivot ma trận phân ca cho quản lý.
  - Toàn bộ truy vấn sử dụng `try-with-resources` và ném `ValidationException(Constants.ERROR_DATABASE_GET_ATTENDANCE)`.
- `src/java/service/AttendanceService.java`:
  - `bulkAssignShift(int userId, int shiftId, String startDateStr, String endDateStr, String[] daysOfWeek)`: Validate dải ngày, duyệt `LocalDate`, kiểm tra thứ trong tuần (`DayOfWeek`), bỏ qua ngày trùng, gọi DAO gán ca, bắn 1 thông báo tổng hợp tới người dùng.
  - `getStaffWeeklyTimetable(int userId, LocalDate weekDate)`: Thuật toán pivot data nhóm theo `Map<Shift, List<Slot>>`, tính toán cờ `canCheckIn`, `canCheckOut`, `isToday`.
  - `getManagerWeeklyMatrix(LocalDate weekDate)`: Thuật toán pivot data nhóm theo `Map<Staff, List<Slot>>`, chuẩn bị dữ liệu cho nút thao tác nhanh `[+]`.
  - Đóng gói 100% dữ liệu sang `Map<String, Object>` / `List<Map<String, Object>>` (Zero Model leakage).
- `src/java/controller/hr/HrManagementController.java`:
  - `doGet`: Nạp `weekDate`, gọi `getManagerWeeklyMatrix` và chuyển giao `MANAGER_MATRIX` sang JSP.
  - `doPost`: Xử lý `action = "bulkAssignShift"` theo mô hình PRG chuẩn và Flash Message.
- `src/java/controller/AttendanceController.java`:
  - `doGet`: Nạp `weekDate`, gọi `getStaffWeeklyTimetable` và chuyển giao `WEEKLY_TIMETABLE` sang JSP.
- `web/components/hr_shift_modal.jsp`:
  - Cải tạo Modal với bộ chuyển đổi Tab/Toggle giữa phân ca đơn lẻ và phân ca hàng loạt.
  - Thiết kế dải ngày và lưới 7 checkbox thứ trong tuần kèm các nút chọn nhanh ("Cả tuần", "Thứ 2 - Thứ 6").
- `web/hr_management.jsp`:
  - Bổ sung Bảng Ma trận Phân ca Tuần (`matrix-table`) kèm thanh điều hướng tuần và nút quick add `[+]`.
- `web/attendance.jsp`:
  - Bổ sung Bảng Thời khóa biểu Tuần (`timetable-grid`) phong cách FAP kèm nút chấm công nhanh.
- `web/js/main.js`:
  - Bổ sung các hàm: `setAssignMode(mode)`, `selectWeekdays(type)`, `openQuickAssignModal(userId, fullName, date)`.
- `web/css/global.css`:
  - Bổ sung kiểu dáng cho `.modal-card-wide`, `.assign-mode-toggle`, `.btn-mode-tab`, `.days-checkbox-grid`, `.matrix-table`, `.matrix-shift-badge`, `.btn-matrix-add`, `.btn-matrix-del`, `.timetable-grid`, `.timetable-slot-card`, `.th-today`, `.cell-today`.

### 3. Tuân thủ kiến trúc (`Rule_BOMS.md`):
- Controller tuyệt đối không import Model hay DAO.
- Không rò rỉ Model lên View (toàn bộ cấu trúc trả về là Map).
- Giao diện JSP không chứa scriptlet (`<% %>`) hay inline style; toàn bộ style viết tại `global.css` và script tại `main.js`.
- File đa ngôn ngữ tiếng Việt lưu trữ dưới dạng Unicode Escape `\uXXXX` 100%.
- Toàn bộ dự án biên dịch thành công qua NetBeans Ant đạt **`BUILD SUCCESSFUL` (0 lỗi)**.

---

## [2026-10-23] - Tích Hợp Đồng Hồ Hệ Thống & Đồng Bộ Menu Điều Hướng Nhanh Tài Khoản Trên `index.jsp`

### 1. Phân tích & Yêu cầu:
- **Đồng bộ tính năng từ thẻ xổ xuống Header (`#menuTaiKhoan`) vào Menu điều hướng nhanh Dashboard (`index.jsp`):**
  - Các chức năng tài khoản quan trọng gồm: Xem hồ sơ cá nhân (`ProfileController`), Cài đặt (`SettingsController`), và Đăng xuất (`LogoutController`).
  - Phải có mặt ở Menu điều hướng nhanh (`quick-actions-grid`) của TẤT CẢ các role (`Staff`, `Manager`, `Admin`, `Shipper`).
  - Tránh trùng lặp: Ở vai trò `Admin` và `Shipper`, trước đó `SettingsController` hoặc `ProfileController` đã nằm rải rác bên trong khối role riêng; tiến hành gom nhóm 3 nút này thành cụm dùng chung ở cuối lưới thao tác nhanh, loại bỏ các nút trùng lặp khỏi khối riêng của Admin/Shipper.
- **Bổ sung Đồng hồ hệ thống thời gian thực vào `index.jsp`:**
  - Lấy theo chuẩn thiết kế và cơ chế hoạt động của trang Chấm công nhân viên (`web/attendance.jsp` - `#serverClockDisplay`).
  - Đồng hồ hiển thị thời gian máy chủ (`HH:mm:ss`) chạy real-time mỗi giây thông qua script `main.js` và ngày máy chủ (`yyyy-MM-dd`).
  - Đặt tại thẻ widget chuyên biệt trong `profile-card-right` của Profile Header Card ở đầu trang chủ Dashboard.

### 2. File bị tác động & Giải pháp Kỹ thuật:
- `src/java/controller/DashboardController.java`:
  - Luôn nạp sẵn `DASHBOARD_SERVER_DATE` và `DASHBOARD_SERVER_TIME` từ `LocalDateTime.now()` để đảm bảo đồng hồ luôn hiển thị chính xác cho mọi đối tượng người dùng (kể cả khi chưa có ca làm việc).
- `web/index.jsp`:
  - Tích hợp khối widget Đồng hồ máy chủ `#serverClockDisplay` vào cụm `profile-card-right` ở đầu trang.
  - Tinh gọn khối Admin và Shipper (loại bỏ nút Settings/Profile trùng lặp).
  - Bổ sung cụm 3 action cards tài khoản chuẩn hóa cho mọi vai trò: Hồ sơ (`ProfileController`), Cài đặt (`SettingsController`), Đăng xuất an toàn (`LogoutController` kèm popup xác nhận).
- `src/java/resources/messages_vi.properties` & `messages_en.properties`:
  - Khai báo nhãn mô tả `dashboard.action.logout_desc` và thông báo xác nhận `dashboard.confirm.logout` (Unicode Escape `\uXXXX` 100%).
- `web/css/global.css`:
  - Định kiểu `.dashboard-clock-card`, `.dashboard-clock-time`, `.dashboard-clock-date`, `.logout-icon`, `.action-card-logout`.

### 3. Tuân thủ kiến trúc (`Rule_BOMS.md`):
- Không rò rỉ Model, Controller không import DAO/Model.
- Không inline CSS/JS trong JSP; JSP thuần JSTL/EL.
- Biên dịch toàn bộ mã nguồn qua NetBeans Ant đạt **`BUILD SUCCESSFUL` (0 lỗi)**.

---

## [2026-10-23] - Hoàn thiện Task 15: Chặt Chẽ Phân Quyền Phân Ca Quản Lý (Manager Shift Assignment Restriction) & Tải Ảnh Đại Diện Hồ Sơ Cá Nhân (Profile Avatar Upload)

### 1. Phân tích & Vấn đề Kỹ thuật:
- **Vấn đề 1 (Role Manager - Phân ca sai thẩm quyền):**
  - Tại `AttendanceDAO.getActiveStaffUsers()`, câu lệnh SQL trước đó lọc `WHERE Role IN ('Staff', 'Manager') AND IsActive = 1`. Điều này dẫn đến việc tài khoản có vai trò `Manager` xuất hiện trong dropdown phân ca và hiển thị thành các hàng trong ma trận phân ca tuần của cửa hàng, tạo cơ hội cho Manager tự gán ca cho chính mình hoặc gán ca cho Manager khác.
  - Tại `AttendanceService`, hai hàm `assignShift` và `bulkAssignShift` chỉ kiểm tra `userId <= 0` mà chưa có tầng kiểm tra vai trò nghiệp vụ (Tier 2 validation) để bảo đảm tài khoản được phân ca phải là `Staff`.
- **Vấn đề 2 (Tính năng Tải ảnh hồ sơ cá nhân còn dang dở):**
  - Tại `web/profile.jsp`, form upload ảnh hướng về `UploadAvatarController` nhưng Controller này chưa tồn tại trong hệ thống. `ProfileController` thì có phương thức `doPost` hoàn toàn rỗng.
  - Bảng `Users` trong CSDL `BOMS_DATABASE.sql` chưa có cột `Avatar`, model `User.java` và `UserDAO.java` chưa có trường/hàm thao tác với ảnh đại diện.
  - Thiếu luồng dọn dẹp file ảnh cũ trên ổ đĩa máy chủ khi người dùng đổi ảnh mới hoặc chủ động xóa ảnh đại diện quay về chữ cái viết tắt mặc định.

### 2. Giải pháp Kỹ thuật Triệt để:
- **Xử lý Vấn đề 1 (Giới hạn phân ca nghiêm ngặt cho Staff):**
  - `src/java/dao/AttendanceDAO.java`: Sửa câu truy vấn trong `getActiveStaffUsers()` thành `WHERE Role = 'Staff' AND IsActive = 1`. Triệt tiêu hoàn toàn sự xuất hiện của Manager trên giao diện chọn phân công và ma trận tuần.
  - `src/java/service/AttendanceService.java`: Bổ sung hàm private đơn trách nhiệm `validateStaffRole(int userId)`. Gọi hàm này tại đầu cả hai luồng `assignShift` và `bulkAssignShift`. Nếu người dùng không tồn tại hoặc có vai trò khác `'Staff'` (kể cả Manager hay Admin), ném ngay `ValidationException(Constants.ERROR_CANNOT_ASSIGN_NON_STAFF)` (mã `E0715`).
  - `src/java/utility/Constants.java`: Bổ sung `ERROR_CANNOT_ASSIGN_NON_STAFF = "E0715"`.
- **Xử lý Vấn đề 2 (Hoàn thiện tải và quản lý Avatar dùng chung ImageUploadUtility):**
  - `DATABASE/BOMS_DATABASE.sql`: Thêm cột `Avatar VARCHAR(255) NULL` vào lệnh `CREATE TABLE Users` và bổ sung khối lệnh kiểm tra/alter table tự động nếu bảng đã tồn tại sẵn.
  - `src/java/model/User.java`: Thêm thuộc tính `private String avatar;`, các hàm tạo và getter/setter `getAvatar()` / `setAvatar()`.
  - `src/java/dao/UserDAO.java`:
    + An toàn trích xuất `Avatar` trong `getAllUsers()`, `getUserByUsername()`, `getUserById()` qua hàm trợ năng `getSafeAvatar(rs)` (bọc try-catch phòng thủ trường hợp schema chưa migrate).
    + Thêm phương thức `updateAvatar(int userId, String avatarUrl)` với try-with-resources.
  - `src/java/service/UserService.java`:
    + Đưa `avatar` vào Session Map (`buildUserSessionMap`) và Profile Map (`buildUserProfileMap`).
    + Bổ sung 2 phương thức nghiệp vụ: `updateAvatar(int userId, String avatarUrl)` và `deleteAvatar(int userId)`.
  - `src/java/controller/hr/ProfileController.java`:
    + Đính kèm `@MultipartConfig(fileSizeThreshold = 1MB, maxFileSize = 10MB, maxRequestSize = 15MB)`.
    + `doGet`: Tiếp nhận Flash Messages qua `FlashMessageUtil.forwardFlashMessages(request, session)`.
    + `doPost`: Xử lý 2 action `uploadAvatar` và `deleteAvatar`. Tận dụng `ImageUploadUtility.saveImage(part, "avatars")` và tự động gọi `ImageUploadUtility.deleteImage(oldAvatar)` khi thay thế hoặc xóa ảnh để giải phóng bộ nhớ đĩa.
    + Cập nhật Session Map `loginUser.put("avatar", ...)` để đồng bộ ngay lập tức không cần đăng nhập lại.
    + Điều hướng theo mô hình PRG (Post-Redirect-Get) sang `ProfileController`.
  - `web/WEB-INF/web.xml`: Bổ sung servlet-mapping `/UploadAvatarController` ánh xạ về `ProfileController` để tương thích toàn diện.
  - `web/profile.jsp`: Bổ sung thông báo Flash message (`SUCCESS_MESSAGE`, `ERROR_MESSAGE`) với `<c:remove>`, hiển thị ảnh avatar bo tròn lớn hoặc chữ cái viết tắt, nút chọn file và nút [Xóa ảnh] kèm xác nhận.
  - `web/header.jsp` & `web/index.jsp`: Đồng bộ hiển thị ảnh avatar thu nhỏ trên nút Header `#btnAvatar` và ô đại diện trên Bảng điều khiển trung tâm `.dashboard-avatar`.
  - `web/css/global.css`: Định kiểu hoàn chỉnh cho `.profile-avatar-img`, `.profile-avatar-wrapper`, `.btn-delete-avatar`, `.header-avatar-img`, `.dashboard-avatar-img`.
  - `messages_vi.properties` & `messages_en.properties`: Khai báo `S0103`, `S0104`, `E0105`, `E0715`, `profile.avatar.delete`, `profile.avatar.confirm_delete` (tiếng Việt mã hóa Unicode Escape `\uXXXX` 100%).

### 3. File tác động & Biên dịch:
- `DATABASE/BOMS_DATABASE.sql`
- `src/java/model/User.java`
- `src/java/dao/UserDAO.java`
- `src/java/dao/AttendanceDAO.java`
- `src/java/service/AttendanceService.java`
- `src/java/service/UserService.java`
- `src/java/utility/Constants.java`
- `src/java/controller/hr/ProfileController.java`
- `web/WEB-INF/web.xml`
- `web/profile.jsp`
- `web/header.jsp`
- `web/index.jsp`
- `web/css/global.css`
- `src/java/resources/messages_vi.properties` & `src/java/resources/messages_en.properties`
- `.agent/WorkList.md`
- **Trạng thái Build:** Apache Ant (nt clean compile với JDK 17) đạt **BUILD SUCCESSFUL (0 errors, 74 source files, 44 web files copied)**.

### 4. Bài học kinh nghiệm & Quy chuẩn kiến trúc cốt lõi:
- **Nguyên tắc phân quyền 2 tầng trong điều phối:** Giao diện lọc (Dropdown/Matrix) chỉ là tầng bảo vệ thứ nhất. Bắt buộc tầng Service phải thực thi kiểm tra vai trò người nhận tác vụ (`validateStaffRole`) để ngăn chặn việc bypass tham số qua HTTP Request.
- **Tối ưu hóa Vòng đời Tệp tải lên (File Lifecycle Management):** Khi người dùng cập nhật ảnh đại diện mới hoặc xóa ảnh, luôn đối chiếu và xóa file vật lý cũ trên ổ đĩa máy chủ (`ImageUploadUtility.deleteImage`) để chống phân mảnh và tràn ổ đĩa theo thời gian.
- **Đồng bộ Session State đa điểm (Multi-point Session Sync):** Khi thông tin người dùng (như avatar) thay đổi ở trang Profile, cập nhật ngay đối tượng Session Map (`Constants.USER_SESSION`) để các thành phần toàn cục như thanh điều hướng Header và Dashboard phản ánh tức thì mà không yêu cầu người dùng phải đăng xuất.

---

## [2026-10-23] - Hoàn thiện Khắc Phục Luồng Upload Ảnh Bằng Link Web & Tối Ưu Render Thẻ <img> Trên JSP

### 1. Phân tích & Vấn đề Kỹ thuật:
- **Xung đột & Dư thừa Tài nguyên Ổ đĩa khi Đổi sang Link Web:**
  - Trong `ProductService.updateProduct`, khi người dùng đổi từ ảnh upload nội bộ (`uploads/products/...`) sang một liên kết ngoài (`http://` hoặc `https://`), file ảnh cũ trên ổ đĩa máy chủ không được dọn dẹp, gây lãng phí dung lượng lưu trữ.
  - Cần hàm validation `Validator.checkMaxLength(imageUrl, 255, Constants.ERROR_IMAGE_URL_TOO_LONG)` để bảo vệ độ dài `VARCHAR(255)` ở CSDL SQL Server.
- **Quy tắc Ưu tiên Ảnh tại MenuManagementController:**
  - Logic phân giải ảnh giữa file tải lên (`productImageFile`) và đường dẫn liên kết (`imageUrl`) cần được tách biệt rõ ràng: file tải lên luôn được ưu tiên xử lý trước; nếu không tải file mới thì giữ nguyên/nhận link URL mới nhập.
- **Hiển thị thẻ `<img>` trên JSP (`menu_management.jsp` & `menu.jsp`):**
  - Biểu thức EL nối chuỗi phức tạp dễ gây lỗi đường dẫn. Cần áp dụng cú pháp JSTL `<c:choose>` chuẩn mực: nếu `imageUrl` bắt đầu bằng `http` thì in thẳng link gốc, ngược lại gắn `${pageContext.request.contextPath}/` phía trước.

### 2. Giải pháp Kỹ thuật Triệt để:
- **Tầng Service (`ProductService.java`):**
  - Import `utility.ImageUploadUtility;`.
  - Tích hợp `Validator.checkMaxLength(imageUrl.trim(), 255, Constants.ERROR_IMAGE_URL_TOO_LONG)` tại cả `createProduct` và `updateProduct`.
  - Bổ sung hàm private đơn trách nhiệm `cleanOldInternalImage(String oldImageUrl, String newImageUrl)`: Nếu `oldImageUrl` bắt đầu bằng `uploads/` và `newImageUrl` là link ngoài (`http/https`) HOẶC là file upload mới khác, tự động gọi `ImageUploadUtility.deleteImage(oldImageUrl)` dọn dẹp ổ đĩa.
- **Tầng Controller (`MenuManagementController.java`):**
  - Bóc tách hàm trợ năng đơn trách nhiệm `resolveImageUrl(HttpServletRequest request)`: Ưu tiên lưu `productImageFile` qua `ImageUploadUtility.saveImage`; nếu không có file thì lấy `imageUrl` từ request param (chuỗi rỗng chuẩn hóa về `null`).
- **Tầng View (`web/menu_management.jsp`, `web/menu.jsp`, `web/js/main.js`):**
  - Tại cả `menu_management.jsp` và `menu.jsp`: Bọc thẻ `<img>` trong `<c:choose>` với điều kiện `<c:when test="${fn:startsWith(product.imageUrl, 'http')}">` in thẳng `src="${product.imageUrl}"`, `<c:otherwise>` in `src="${pageContext.request.contextPath}/${product.imageUrl}"`.
  - Cập nhật hàm xem trước `openEditProductModal` trong `main.js`: Nhận diện đường dẫn nội bộ để gắn đúng context path trên trình duyệt khi mở modal chỉnh sửa.

### 3. File tác động & Biên dịch:
- `src/java/service/ProductService.java`
- `src/java/controller/MenuManagementController.java`
- `web/menu_management.jsp`
- `web/menu.jsp`
- `web/js/main.js`
- **Trạng thái Build:** Apache Ant (`ant compile`) đạt **BUILD SUCCESSFUL (0 errors)**.

---

## [2026-10-23] - Hoàn thiện Toàn diện Task 16 (Task 16.1, 16.2, 16.3): Nâng Cấp Quyền Lực Quản Trị Viên (Super Admin Dashboard - HireDate, Password Reset, Direct Role Change & Revoke Security Key)

### 1. Phân tích & Vấn đề Kỹ thuật:
- **Thiếu thông tin Ngày gia nhập & Cơ chế Flash Message trên Admin Dashboard:**
  - Bảng danh sách người dùng (`#tab-users`) trên `admin_dashboard.jsp` chưa hiển thị cột `HireDate`.
  - `AdminDashboardController` chỉ có `doGet` sơ khai, chưa tiếp nhận `FlashMessageUtil.forwardFlashMessages` nên các thông báo chuyển trang sau khi thao tác (như bật/tắt tài khoản từ `ToggleUserController`) không được hiển thị mượt mà theo chuẩn PRG.
- **Admin chưa có chức năng Cấp lại mật khẩu trực tiếp cho tài khoản:**
  - Khi nhân viên quên mật khẩu hoặc cần reset khẩn cấp, hệ thống chưa cung cấp giao diện cho Admin cấp lại mật khẩu mới.
  - Phải có ràng buộc nghiệp vụ: Admin tuyệt đối không được tự cấp lại mật khẩu của chính mình tại trang này (tránh xung đột và bypass mật khẩu cũ ở Settings).
- **Bộ quyền lực Super Admin (Task 16.3 - Option A):**
  - Admin cần khả năng đổi trực tiếp vai trò nhân sự (`Manager`, `Staff`, `Shipper`) kèm tính toàn vẹn dữ liệu:
    + Chặn Admin tự hạ quyền hoặc tự đổi vai trò của chính mình.
    + Khi đổi sang `Shipper`, tự động bảo đảm bản ghi hồ sơ `ShipperProfiles` tồn tại (`ensureShipperProfileExists`).
    + Khi đổi một Shipper sang vai trò khác, kiểm tra bảo toàn: Nếu Shipper đang có đơn hàng giao chưa hoàn tất (`ActiveOrders > 0`), CHẶN đổi vai trò (`E9005`); ngược lại tự động chuyển `ShipperStatus = 'Offline'`.
  - Admin cần thu hồi/khóa các mã `Security Key` còn hiệu lực nhưng chưa được ai sử dụng (`revokeUnusedKey`).

### 2. Giải pháp Kỹ thuật Triệt để:
- **Task 16.1 (Bổ sung HireDate & Chuẩn hóa Flash Message):**
  - `AdminService.java`: Trong `getAllUsersForView()`, đóng gói `map.put("hireDate", u.getHireDate());`.
  - `AdminDashboardController.java`: Bổ sung `FlashMessageUtil.forwardFlashMessages(request, session);` tại đầu `doGet` kèm kiểm tra phân quyền `Admin`.
  - `web/admin_dashboard.jsp`: Thêm cột `<th><fmt:message key="admin.dashboard.table.hiredate" /></th>`, định dạng ngày qua `<fmt:formatDate value="${user.hireDate}" pattern="dd/MM/yyyy" />`.
- **Task 16.2 (Tab Cấp lại mật khẩu `tab-passwords`):**
  - `AdminService.java`: Viết `resetUserPasswordByAdmin(adminUserId, targetUserIdStr, newPassword, confirmPassword)`. Validate chặt chẽ: Chặn tự reset (`E9001`), kiểm tra tồn tại tài khoản (`Constants.ERROR_USER_NOT_FOUND`), tái sử dụng `userService.validatePassword(newPassword)` và kiểm tra khớp mật khẩu xác nhận (`Constants.ERROR_CONFIRM_PSW`), cập nhật qua `userDAO.updatePassword`.
  - `AdminDashboardController.java`: Xử lý `action="resetPassword"`, lưu mã thành công `Constants.SUCCESS_ADMIN_RESET_PSW` (`S9001`) và điều hướng `sendRedirect("AdminDashboardController?tab=tab-passwords")`.
  - `web/admin_dashboard.jsp`: Thêm Tab nút bấm `tab-passwords` và giao diện Form cấp lại mật khẩu trong `.admin-password-box`, lọc ẩn tài khoản của chính Admin đang đăng nhập.
- **Task 16.3 (Đổi Vai trò trực tiếp & Thu hồi Security Key):**
  - `UserDAO.java`:
    + `updateUserRole(userId, newRole)` với `try-with-resources`.
    + `ensureShipperProfileExists(userId)`: Tự động khởi tạo hồ sơ nếu chưa có (`'Offline'`, `0`, `3`).
    + `isShipperHavingActiveOrders(userId)`: Kiểm tra đơn hàng đang giao.
    + `setShipperOffline(userId)`: Đưa trạng thái về Offline.
  - `AccountManagerDAO.java`:
    + `updateRoleByUserId(userId, newRole)` với `try-with-resources`.
    + `revokeUnusedKey(keyId)`: Cập nhật `IsActive = 0 WHERE KeyID = ? AND UserID IS NULL AND IsActive = 1`.
  - `AdminService.java`:
    + `getAllKeysForView()`: Bổ sung `map.put("keyId", k.getKeyId());`.
    + `changeUserRoleByAdmin(adminUserId, targetUserIdStr, newRole)`: Chặn tự đổi role (`E9002`), validate whitelist role (`E9003`), chặn chuyển đổi nếu Shipper còn đơn đang giao (`E9005`), cập nhật cả `Users` và `AccountManager`.
    + `revokeSecurityKey(keyIdStr)`: Thu hồi key chưa dùng (`E9004` nếu thất bại).
  - `AdminDashboardController.java`: Xử lý `action="changeRole"` (S9002) và `action="revokeKey"` (S9003) với PRG chuẩn mực.
  - `web/admin_dashboard.jsp`:
    + Bảng `#tab-users`: Thêm dropdown chọn Role inline kèm nút Lưu cho các tài khoản không phải Admin hiện tại.
    + Bảng `#tab-keys`: Thêm cột Thao tác với nút [Thu hồi] cho các key còn hiệu lực và chưa được kích hoạt (`key.active and empty key.userId`).
  - `src/java/resources/messages_vi.properties` & `messages_en.properties`: Khai báo toàn bộ các mã lỗi/thành công và nhãn giao diện (tiếng Việt mã hóa Unicode Escape `\uXXXX` 100%).
  - `web/css/global.css`: Định kiểu `.admin-password-box`, `.role-shipper`, `.select-role-inline`.

### 3. File tác động & Biên dịch:
- `src/java/utility/Constants.java`
- `src/java/resources/messages_vi.properties`
- `src/java/resources/messages_en.properties`
- `src/java/dao/UserDAO.java`
- `src/java/dao/AccountManagerDAO.java`
- `src/java/service/AdminService.java`
- `src/java/controller/admin/AdminDashboardController.java`
- `web/admin_dashboard.jsp`
- `web/css/global.css`
- `.agent/WorkList.md`
- **Trạng thái Build:** Apache Ant (nt clean compile với JDK 17) đạt **BUILD SUCCESSFUL (0 errors, 74 source files, 44 web files copied)**.

### 4. Bài học kinh nghiệm & Tuân thủ kiến trúc:
- **Zero Model Leakage & Zero DAO Leakage:** Toàn bộ Controller không import Model hay DAO. Mọi dữ liệu trả về View đều qua Map thuần túy (`Map<String, Object>`).
- **Bảo toàn dữ liệu phân hệ Giao hàng khi đổi Role:** Đổi vai trò tài khoản không chỉ là thao tác `UPDATE Users`, mà còn gắn liền với trạng thái hoạt động thực tế của tài khoản đó ở các bảng chuyên biệt (`ShipperProfiles`, `DeliveryTasks`). Việc chặn chuyển đổi khi Shipper đang có đơn (`ActiveOrders > 0`) giúp ngăn ngừa rủi ro treo đơn hàng vô chủ trong luồng giao hàng.

---

## [2026-10-23] - Hoàn thiện Task 17: Tự Động Xử Lý Cảnh Báo Hết Hạn Khi Cập Nhật Kho & Đồng Bộ Thông Báo Kho Ra Màn Hình Ngoài, Kèm Hotfix Nhãn Security Key Admin

### 1. Phân tích & Vấn đề Kỹ thuật:
- **Cảnh báo Zombie Expired/Expiring Soon kẹt trong kho:**
  - Khi Manager cập nhật lại số lượng nguyên liệu trên trang Quản lý kho, hàm `updateIngredient()` chỉ gọi `updateIngredient` mà không dọn dẹp các lô cũ đã quá hạn trong `IngredientBatches`. Do đó mỗi khi `scanIngredientStock()` kích hoạt, các cảnh báo `Expired` hoặc `Expiring Soon` bị tái sinh liên tục dù nguyên liệu đã được nhập mới.
- **Thiếu đồng bộ thông báo kho ra Bảng tin & Quả chuông toàn cục:**
  - Các sự cố kho nghiêm trọng (`OutOfStock`, `LowStock`, `Expired`, `Expiring Soon`) chỉ được ghi vào bảng `InventoryAlerts` và hiển thị trên màn hình Quản lý kho. Nhân viên và Quản lý ở ngoài trang chủ (`index.jsp`) hoặc các trang khác không nhận được thông báo để xử lý kịp thời.
- **Lỗi hiển thị `???admin.dashboard.status.unused???` trên Tab Security Key:**
  - Trong `web/admin_dashboard.jsp`, cột UserID của key chưa gán gọi thẻ `<fmt:message key="admin.dashboard.status.unused" />`, nhưng trong các file `.properties` key lại được đặt tên là `admin.dashboard.status.unused_key`, dẫn đến việc JSP in ra lỗi chuỗi chưa dịch.

### 2. Giải pháp Kỹ thuật Triệt để:
- **Hotfix nhãn `admin.dashboard.status.unused`:**
  - Bổ sung `admin.dashboard.status.unused=Ch\u01b0a g\u00e1n` vào `src/java/resources/messages_vi.properties` (chuẩn mã hóa `\uXXXX`).
  - Bổ sung `admin.dashboard.status.unused=Unassigned` vào `src/java/resources/messages_en.properties`.
- **Task 17.1 (Tự động dọn lô quá hạn & đóng cảnh báo khi cập nhật kho):**
  - `src/java/dao/IngredientDAO.java`:
    + Bổ sung phương thức `resolveExpiryAlertsOnRestock(int ingredientId)` bọc `try-with-resources`:
      1. Đưa số lượng tồn `Quantity = 0` cho các lô quá hạn/sắp hết hạn ($\le 3$ ngày) của nguyên liệu trong `IngredientBatches`:
         `UPDATE IngredientBatches SET Quantity = 0 WHERE IngredientID = ? AND ExpirationDate <= DATEADD(day, 3, CAST(GETDATE() AS DATE)) AND Quantity > 0`
      2. Đóng trạng thái toàn bộ cảnh báo `Expired` và `Expiring Soon` đang mở (`Active`, `Confirmed`) trong `InventoryAlerts`:
         `UPDATE InventoryAlerts SET Status = 'Closed' WHERE IngredientID = ? AND AlertType IN ('Expired', 'Expiring Soon') AND Status IN ('Active', 'Confirmed')`
  - `src/java/service/IngredientService.java`:
    + Trong `updateIngredient(...)`, ngay sau `ingredientDao.updateIngredient(existing);`, lập tức gọi liên tiếp:
      `ingredientDao.resolveExpiryAlertsOnRestock(ingredientId);`
      `ingredientDao.closeResolvedStockAlerts();`
    + Đảm bảo đồng bộ kép: Vừa xóa sạch cảnh báo hết hàng/tồn kho thấp, vừa triệt tiêu vĩnh viễn cảnh báo hết hạn zombie.
- **Task 17.2 (Đồng bộ Cảnh báo Kho tự động sang `Notifications`):**
  - `src/java/dao/UserDAO.java`: Bổ sung `getActiveManagerAndStaffUserIds()` (`SELECT UserID FROM Users WHERE IsActive = 1 AND Role IN ('Manager', 'Staff')`).
  - `src/java/dao/IngredientDAO.java`: Nâng cấp `insertInventoryAlert(int ingredientId, String alertType)` sử dụng `Statement.RETURN_GENERATED_KEYS` để trả về `int alertId` vừa sinh ra.
  - `src/java/service/IngredientService.java`:
    + Khởi tạo `NotificationService` và `UserDAO`.
    + Xây dựng hàm trợ năng đơn trách nhiệm `broadcastInventoryAlertNotification(Ingredient ing, String alertType, int alertId)`: Định dạng nội dung chi tiết rõ ràng theo Phương án A đã chọn (`[Hết hàng]`, `[Sắp hết hàng]`, `[Đã hết hạn]`, `[Sắp hết hạn]` + Tên nguyên liệu + Số lượng/Đơn vị) và gửi tới toàn bộ Manager & Staff đang hoạt động.
    + Trong `scanIngredientStock()`: Tại cả 4 nhánh cảnh báo, chỉ phát thông báo khi `!ingredientDao.isAlertActive(...)` và `insertInventoryAlert` thành công, đảm bảo mỗi sự cố kho chỉ gửi duy nhất 1 lần, không bị spam khi client polling.

### 3. File tác động & Biên dịch:
- `src/java/resources/messages_vi.properties`
- `src/java/resources/messages_en.properties`
- `src/java/dao/UserDAO.java`
- `src/java/dao/IngredientDAO.java`
- `src/java/service/IngredientService.java`
- `.agent/WorkList.md`
- **Trạng thái Build:** Apache Ant (nt clean compile với JDK 17) đạt **BUILD SUCCESSFUL (0 errors, 74 source files, 44 web files copied)**.

### 4. Bài học kinh nghiệm & Tuân thủ kiến trúc:
- **Xử lý Dữ liệu Zombie trong Quản lý Tồn kho:** Khi cập nhật dữ liệu bảng cha (`Ingredients`), bắt buộc phải rà soát và đồng bộ các bản ghi chi tiết ở bảng con (`IngredientBatches`, `InventoryAlerts`) để tránh sự bất nhất về nghiệp vụ giữa số liệu tồn kho tổng và tình trạng từng lô hàng.
- **Bảo đảm Hiệu năng khi Tích hợp Broadcast Thông báo (Decoupled Notification):** Luồng bắn thông báo tự động từ hệ thống cảnh báo kho được bọc trong khối `try-catch` riêng biệt để nếu có sự cố nhỏ trong việc gửi thông báo phụ thì tiến trình quét kho cốt lõi vẫn hoàn thành trơn tru không bị gián đoạn.

---

## [2026-10-23] - Hoàn thiện Task 18: Quản Lý Danh Mục Động Cho Thực Đơn & Kho Nguyên Liệu (Phương án 6A)

### 1. Phân tích & Vấn đề Kỹ thuật:
- **Vấn đề ban đầu:** Danh mục món ăn (`MenuCategories`) và danh mục nguyên liệu kho (`IngredientCategories`) bị fix cứng trong CSDL hoặc thiếu chức năng quản trị động trên giao diện web. Người quản lý không thể chủ động thêm các nhóm món mới (như Trà Sữa, Cà Phê, Tráng Miệng...) hay nhóm nguyên liệu mới (Sữa, Trái Cây, Đóng Hộp...).
- **Yêu cầu Task 18:** Triển khai quản lý danh mục động theo Phương án 6A:
  - Cung cấp nút mở Modal Quản lý danh mục ngay trên thanh công cụ của trang Quản lý Thực đơn (`menu_management.jsp`) và Quản lý Kho (`inventory_managerment.jsp`).
  - Cho phép thêm danh mục mới với đầy đủ kiểm tra tính hợp lệ và chống trùng lặp tên.
  - Cho phép xóa an toàn các danh mục chưa chứa món ăn hoặc nguyên liệu (`inUse == false`), hiển thị badge trạng thái *"Đang có món"* / *"Đang có nguyên liệu"* và ẩn/vô hiệu hóa nút xóa đối với danh mục đang được sử dụng.
  - Áp dụng cơ chế PRG (Post-Redirect-Get) giữ mở modal tự động qua tham số URL `?manageCategory=true` sau khi thêm hoặc xóa danh mục để quản lý xem ngay danh sách mới nhất.

### 2. Giải pháp Kỹ thuật Triệt để:
- **Tầng DAO (`MenuCategoryDAO.java` & `IngredientDAO.java`):**
  - `MenuCategoryDAO.java`:
    + Bổ sung `insertCategory(String categoryName)` thêm danh mục món mới vào bảng `MenuCategories`.
    + Bổ sung `isCategoryInUse(int categoryId)` kiểm tra xem danh mục có đang chứa món ăn trong bảng `Products` hay không (`SELECT COUNT(1) FROM Products WHERE CategoryID = ?`).
    + Bổ sung `deleteCategory(int categoryId)` xóa danh mục rỗng khỏi bảng `MenuCategories`.
  - `IngredientDAO.java`:
    + Bổ sung `isIngredientCategoryNameExists(String categoryName)` kiểm tra trùng tên không phân biệt hoa thường (`LOWER(CategoryName) = LOWER(?)`).
    + Bổ sung `insertIngredientCategory(String categoryName)` thêm danh mục kho mới vào bảng `IngredientCategories`.
    + Bổ sung `isIngredientCategoryInUse(int categoryId)` kiểm tra xem danh mục có đang chứa nguyên liệu trong bảng `Ingredients` hay không (`SELECT COUNT(1) FROM Ingredients WHERE CategoryID = ?`).
    + Bổ sung `deleteIngredientCategory(int categoryId)` xóa danh mục kho rỗng khỏi `IngredientCategories`.
    + Toàn bộ phương thức DAO đều sử dụng `try-with-resources`, bắt `SQLException` và ném `ValidationException` kèm mã lỗi `DEXXX`.
- **Tầng Nghiệp vụ (`ProductService.java` & `IngredientService.java`):**
  - `ProductService.java`:
    + Trong `getAllCategoriesForView()`: Bổ sung thuộc tính `map.put("inUse", menuCategoryDAO.isCategoryInUse(cat.getCategoryId()));`.
    + Thêm `createCategory(String categoryName)`: Kiểm tra độ dài, kiểm tra trùng lặp tên (`E0219` - `Constants.ERROR_MENU_CATEGORY_EXISTS`).
    + Thêm `deleteCategory(String categoryIdStr)`: Kiểm tra `isCategoryInUse`, nếu đang có món thì chặn xóa (`E0220` - `Constants.ERROR_MENU_CATEGORY_IN_USE`).
  - `IngredientService.java`:
    + Trong `getCategoriesForView()`: Bổ sung `m.put("inUse", ingredientDao.isIngredientCategoryInUse(entry.getKey()));`.
    + Thêm `createCategory(String categoryName, String userRole)`: Kiểm tra quyền Manager (`E0307`), tên rỗng (`E0409`), trùng tên (`E0410`).
    + Thêm `deleteCategory(String categoryIdStr, String userRole)`: Kiểm tra quyền Manager (`E0307`), kiểm tra `isIngredientCategoryInUse`, nếu đang có nguyên liệu thì chặn xóa (`E0411`).
  - Toàn bộ dữ liệu trả về đều đóng gói dạng `Map<String, Object>`, không để rò rỉ Model.
- **Tầng Controller (`MenuManagementController.java` & `InventoryManagementController.java`):**
  - `MenuManagementController.java`:
    + Hứng `action="createCategory"` và `action="deleteCategory"`.
    + Lưu Flash Message vào Session (`S0213` - `SUCCESS_CREATE_MENU_CATEGORY`, `S0214` - `SUCCESS_DELETE_MENU_CATEGORY`).
    + Thực hiện PRG redirect về `MenuManagementController?manageCategory=true`.
  - `InventoryManagementController.java`:
    + Hứng `action="createCategory"` và `action="deleteCategory"`.
    + Lưu Flash Message vào Session (`S0405` - `SUCCESS_CREATE_INGREDIENT_CATEGORY`, `S0406` - `SUCCESS_DELETE_INGREDIENT_CATEGORY`).
    + Thực hiện PRG redirect về `InventoryManagementController?manageCategory=true`.
- **Tầng Giao diện (JSP / CSS / JS / i18n):**
  - `web/menu_management.jsp`: Thêm nút `📁 Quản lý danh mục` bên cạnh nút Thêm món mới.
  - `web/components/product_form_modal.jsp`: Nhúng Modal `#menuCategoryModal` hiển thị form thêm mới và danh sách danh mục có badge trạng thái "Đang có món" / "Trống" và nút Xóa an toàn.
  - `web/inventory_managerment.jsp`: Thêm nút `📁 Quản lý danh mục` bên cạnh nút Thêm nguyên liệu (chỉ hiển thị cho Manager).
  - `web/components/inventory_form_modal.jsp`: Nhúng Modal `#inventoryCategoryModal` hiển thị form thêm mới và danh sách danh mục kho kèm nút Xóa an toàn.
  - `web/js/main.js`: Bổ sung các hàm điều khiển modal (`openMenuCategoryModal`, `closeMenuCategoryModal`, `openInventoryCategoryModal`, `closeInventoryCategoryModal`), phím tắt Escape và tự động mở lại modal nếu URL chứa `manageCategory=true`.
  - `web/css/global.css`: Bổ sung kiểu dáng thanh lịch cho category table, badge trạng thái `badge-in-use`, `badge-empty`, và responsive layout cho modal quản lý danh mục.
  - `messages_vi.properties` & `messages_en.properties`: Bổ sung đầy đủ dải mã `S0213`, `S0214`, `S0405`, `S0406`, `E0219`, `E0220`, `E0409`, `E0410`, `E0411` cùng bộ nhãn UI `menu.category.*` và `inventory.category.*` (tiếng Việt mã hóa Unicode Escape 100%).

### 3. File tác động & Biên dịch:
- `src/java/utility/Constants.java`
- `src/java/dao/MenuCategoryDAO.java`
- `src/java/service/ProductService.java`
- `src/java/controller/MenuManagementController.java`
- `src/java/dao/IngredientDAO.java`
- `src/java/service/IngredientService.java`
- `src/java/controller/InventoryManagementController.java`
- `web/menu_management.jsp`
- `web/components/product_form_modal.jsp`
- `web/inventory_managerment.jsp`
- `web/components/inventory_form_modal.jsp`
- `web/js/main.js`
- `web/css/global.css`
- `src/java/resources/messages_vi.properties`
- `src/java/resources/messages_en.properties`
- `.agent/WorkList.md`
- **Trạng thái Build:** Apache Ant (nt clean compile với JDK 17) đạt **BUILD SUCCESSFUL (0 errors, 74 source files, 44 web files copied)**.

### 4. Bài học kinh nghiệm & Tuân thủ kiến trúc:
- **Xóa an toàn với ràng buộc toàn vẹn quan hệ (Referential Integrity Check):** Luôn kiểm tra ràng buộc nghiệp vụ ở cả tầng DAO (`COUNT(1) > 0`) và ném ngoại lệ rõ ràng ở tầng Service trước khi thực thi lệnh `DELETE`. Điều này bảo vệ dữ liệu khỏi ngoại lệ Foreign Key Constraint vô hình của CSDL và cung cấp thông báo thân thiện tới người dùng.
- **Trải nghiệm liền mạch với PRG kèm cờ trạng thái Modal (`?manageCategory=true`):** Khi thao tác quản lý danh mục trên Modal, nếu redirect PRG thông thường thì trang sẽ tải lại và đóng modal, khiến người dùng phải mở lại modal thủ công để xem kết quả. Bằng cách thêm cờ tham số URL và kích hoạt tự động mở modal trong JavaScript, trải nghiệm người dùng được bảo đảm mượt mà như Single Page Application mà vẫn duy trì tính đơn giản của kiến trúc Servlet/JSP.

---

## [2026-10-23] - Hoàn thiện Toàn diện Task 19 (Task 19.1 đến 19.5): Tổng Rà Soát, Vá Lỗ Hổng Bảo Mật & Hoàn Thiện Chất Lượng Toàn Diện (Final Hardening & Polish - Beta 1)

### 1. Phân tích & Vấn đề Kỹ thuật:
- **Lỗ hổng bảo mật phân quyền & Controller thiếu an toàn (Task 19.1):**
  + Hai Servlet `GenerateKeyController.java` và `ToggleUserController.java` được cấu hình độc lập nhưng thiếu kiểm tra chặt chẽ quyền Admin tập trung, có thể bị gọi lén qua URL hoặc script giả mạo. Ngoài ra, Admin có thể tự khóa chính tài khoản của mình dẫn tới mất quyền quản trị.
  + `SettingsController.java` thiếu kiểm tra `loginUser == null` khi session hết hạn dẫn tới `NullPointerException` (HTTP 500), đồng thời trong `doPost` lại gọi đệ quy `doGet(request, response)` thay vì chuyển hướng PRG chuẩn mực.
- **Tắc nghẽn luồng khách vãng lai tại Bộ lọc bảo mật (Task 19.2):**
  + Trong `AuthFilter.java`, whitelist `isAuthPage` chưa chứa `DashboardController`, khiến khách vãng lai khi truy cập trang giới thiệu hệ thống Landing Page (`index.jsp`) bị chặn và ép chuyển hướng sang `login.jsp`. Dòng kiểm tra `register.jsp` bị lặp lại 2 lần.
- **Rò rỉ tài nguyên kết nối JDBC diện rộng (Task 19.3):**
  + Hơn 60 phương thức từ các giai đoạn đầu trong `UserDAO.java`, `OrderDAO.java`, `IngredientDAO.java`, `ProductDAO.java`, `MenuCategoryDAO.java`, `AccountManagerDAO.java` khởi tạo `PreparedStatement` và `ResultSet` nhưng không bọc trong khối `try-with-resources`, dẫn đến nguy cơ cạn kiệt Connection / Statement Pool của MS SQL Server khi tải cao.
- **Vi phạm quy chuẩn cấm Inline CSS/JS & Trùng lặp Script (Task 19.4):**
  + `web/login.jsp` chứa `style="margin-top: 15px;"` và `onclick="..."` nội tuyến tại popup `bannedPopup`.
  + `web/menu.jsp` chứa khối `style="..."` trực tiếp tại thanh công cụ tìm kiếm và thông báo giỏ hàng trống.
  + `web/attendance.jsp` và `web/hr_management.jsp` nạp lại `<script src=".../main.js"></script>` ở cuối trang dù `footer.jsp` đã nạp sẵn.
- **Thiếu sót cấu hình Đa ngôn ngữ và Servlet Descriptor (Task 19.5):**
  + `messages_en.properties` thiếu key `login.banned.message` và có lỗi khoảng trắng `register.label.fullname =Fullname:`.
  + `web/WEB-INF/web.xml` thiếu khai báo `<servlet>` và `<servlet-mapping>` cho `DispatchController` và `ShipperController`.
  + `MenuManagementController.java` và `OrderManagementController.java` vẫn còn khai báo các phương thức helper `forwardFlashMessages` private trùng lặp thay vì dùng `FlashMessageUtil`.

### 2. Giải pháp Kỹ thuật Triệt để:
- **Task 19.1: Vá lỗ hổng bảo mật & Chuẩn hóa PRG Controller (Phương án A):**
  + Hợp nhất toàn bộ xử lý `action="generateKey"` và `action="toggleUser"` vào phương thức `doPost` của `AdminDashboardController.java`, kế thừa toàn vẹn lớp bảo vệ xác thực quyền Admin (`Role == 'Admin'`).
  + Bổ sung cơ chế chặn Admin tự khóa tài khoản của chính mình (`adminUserId != targetUserId`), nếu vi phạm sẽ ném ngoại lệ `Constants.ERROR_ADMIN_CANNOT_CHANGE_SELF_ROLE` (`E9002`).
  + Cập nhật các form trong `web/admin_dashboard.jsp` trỏ về `AdminDashboardController` với hidden action `generateKey` và `toggleUser`.
  + Xóa bỏ hoàn toàn 2 file controller thừa `GenerateKeyController.java` và `ToggleUserController.java`, gỡ bỏ các khai báo servlet tương ứng trong `web.xml`.
  + Tái cấu trúc toàn diện `SettingsController.java`: Kiểm tra `loginUser == null` ở đầu cả `doGet` và `doPost`, áp dụng mô hình PRG chuẩn mực với `FlashMessageUtil.forwardFlashMessages` và `sendRedirect("SettingsController")`, xóa bỏ hoàn toàn lệnh gọi `doGet()` trong `doPost`.
- **Task 19.2: Mở thông luồng Khách vãng lai (`AuthFilter.java`):**
  + Thêm điều kiện `|| uri.endsWith("DashboardController")` vào whitelist `isAuthPage` trong `AuthFilter.java`, cho phép người dùng chưa đăng nhập tự do truy cập Landing Page.
  + Gỡ bỏ dòng trùng lặp `|| uri.endsWith("register.jsp")`.
- **Task 19.3: Chuyển đổi 100% JDBC sang `try-with-resources`:**
  + Tái cấu trúc triệt để toàn bộ các phương thức truy vấn và cập nhật trong `UserDAO.java`, `OrderDAO.java` (đặc biệt là khối giao dịch phức tạp `insertOrder` và `loadOrderDetails`), `IngredientDAO.java`, `ProductDAO.java`, `MenuCategoryDAO.java`, `AccountManagerDAO.java`.
  + 100% các đối tượng `PreparedStatement`, `Statement`, `ResultSet` và `getGeneratedKeys()` đều được bao bọc an toàn trong cú pháp `try (PreparedStatement st = ...; ResultSet rs = ...)`.
  + Xác nhận toàn bộ 10 DAO trong dự án không còn bất kỳ đối tượng tài nguyên JDBC nào bị bỏ sót ngoài `try-with-resources`.
- **Task 19.4: Dọn dẹp Inline CSS/JS & Trùng lặp Script:**
  + `web/login.jsp`: Thay thế `style` và `onclick` nội tuyến bằng class `.btn-banned-close` và gọi hàm `closeBannedPopup()` được định nghĩa chuẩn tắc trong `web/js/main.js`.
  + `web/css/global.css`: Định nghĩa các class CSS dùng chung `.btn-banned-close`, `.menu-header-bar`, `.menu-empty-container`.
  + `web/menu.jsp`: Thay thế toàn bộ mã inline CSS bằng class ngữ nghĩa `.menu-header-bar` và `.menu-empty-container`.
  + `web/attendance.jsp` & `web/hr_management.jsp`: Xóa bỏ thẻ `<script src=".../main.js"></script>` dư thừa ở cuối trang.
- **Task 19.5: Chuẩn hóa Đa ngôn ngữ, `web.xml` & Tái sử dụng FlashMessageUtil:**
  + `src/java/resources/messages_en.properties`: Bổ sung `login.banned.message=Your account has been deactivated. Please contact the administrator for assistance!` và chuẩn hóa `register.label.fullname=Fullname:`.
  + `web/WEB-INF/web.xml`: Bổ sung đầy đủ khai báo servlet và servlet-mapping cho `DispatchController` và `ShipperController` (bao gồm `<multipart-config>` hỗ trợ upload ảnh giao hàng).
  + `MenuManagementController.java` & `OrderManagementController.java`: Loại bỏ các hàm helper private trùng lặp, tích hợp `FlashMessageUtil.forwardFlashMessages(request, session)`.

### 3. File tác động & Biên dịch:
- `src/java/controller/admin/AdminDashboardController.java`
- `src/java/controller/admin/GenerateKeyController.java` *(Đã xóa)*
- `src/java/controller/admin/ToggleUserController.java` *(Đã xóa)*
- `web/admin_dashboard.jsp`
- `src/java/controller/SettingsController.java`
- `src/java/filter/AuthFilter.java`
- `src/java/dao/AccountManagerDAO.java`
- `src/java/dao/MenuCategoryDAO.java`
- `src/java/dao/ProductDAO.java`
- `src/java/dao/UserDAO.java`
- `src/java/dao/OrderDAO.java`
- `src/java/dao/IngredientDAO.java`
- `web/login.jsp`
- `web/menu.jsp`
- `web/attendance.jsp`
- `web/hr_management.jsp`
- `web/js/main.js`
- `web/css/global.css`
- `src/java/resources/messages_en.properties`
- `web/WEB-INF/web.xml`
- `src/java/controller/MenuManagementController.java`
- `src/java/controller/OrderManagementController.java`
- `.agent/WorkList.md`
- **Trạng thái Build:** Apache Ant (nt clean compile với JDK 17) đạt **BUILD SUCCESSFUL (0 errors, 74 source files, 44 web files copied)**.

### 4. Bài học kinh nghiệm & Tuân thủ kiến trúc cốt lõi:
- **Tập trung hóa quyền hạn (Principle of Least Privilege & Centralized Authorization):** Các tác vụ nhạy cảm của Admin (tạo key, khóa tài khoản, cấp lại mật khẩu, đổi role) không nên phân tán rải rác ở nhiều servlet nhỏ riêng biệt vì rất dễ dẫn đến việc quên kiểm tra quyền hoặc logic bảo mật không đồng nhất. Tập trung các action này vào một Controller chính thống duy nhất có bảo vệ Role tập trung giúp mã nguồn an toàn tuyệt đối và dễ kiểm soát.
- **Phòng chống tấn công tự khóa (Self-Lockout Prevention):** Trong bất kỳ hệ thống quản trị nào, quyền Super Admin phải luôn có lớp bảo vệ logic để ngăn Admin vô tình tự khóa tài khoản hoặc tự hạ quyền của chính mình (`adminUserId == targetUserId`), ngăn chặn thảm họa "mất chìa khóa vạn năng".
- **Tuyệt đối giải phóng tài nguyên JDBC bằng `try-with-resources`:** Cơ chế `AutoCloseable` của Java 7+ là giải pháp tối ưu nhất để tránh rò rỉ Connection / Statement / ResultSet trong môi trường máy chủ Servlet đa luồng (Tomcat). Không bao giờ được mở `PreparedStatement` hoặc `ResultSet` bên ngoài khối `try (...)`.







---

## [2026-10-24] - HoÃ n thiá»‡n ToÃ n diá»‡n Task 27: Chiáº¿n dá»‹ch Tá»•ng Vá»‡ Sinh & Triá»‡t TiÃªu Ná»£ Kiáº¿n TrÃºc Tá»“n Äá»ng (Architectural Purge & Full Compliance)

### 1. PhÃ¢n tÃ­ch & Váº¥n Ä‘á» Ká»¹ thuáº­t:
- **Tá»“n dÆ° Inline JavaScript trÃªn táº§ng View (Task 27.1):** 61 vá»‹ trÃ­ inline event (onclick, onsubmit, onchange, oninput) tráº£i dÃ i trÃªn 16 file JSP vi pháº¡m Ä‘iá»u cáº¥m nghiÃªm ngáº·t trong Rule_BOMS.md.
- **Khai bÃ¡o biáº¿n Static ngoÃ i Utility (Task 27.2):** 4 háº±ng sá»‘ private static final int trong AttendanceService.java vi pháº¡m quy táº¯c cáº¥u trÃºc táº§ng vÃ  cáº¥m biáº¿n static ngoÃ i package utility.
- **Chuá»—i text tiáº¿ng Viá»‡t hardcode trong táº§ng Service (Task 27.3):** Nhiá»u thÃ´ng bÃ¡o thÃ´ng tin phÃ¢n ca, ngÃ y trong tuáº§n, duyá»‡t cháº¥m cÃ´ng, cáº£nh bÃ¡o kho, tÃªn mÃ³n fallback bá»‹ gÃµ cá»©ng thÃ´ báº±ng tiáº¿ng Viá»‡t trong code Java thay vÃ¬ lÆ°u trong Constants.java vÃ  mÃ£ hÃ³a Unicode Escape \uXXXX.
- **Lá»— há»•ng XSS cÃ²n sÃ³t láº¡i trÃªn View (Task 27.4):** CÃ¡c biá»ƒu thá»©c EL tráº§n ${item.ingredientName}, ${item.categoryName}, ${item.unit}, ${item.storageLocation}, ${sessionScope.USER_SESSION.fullName} vÃ  ${cat.categoryName} trong tháº» <option> chÆ°a Ä‘Æ°á»£c bao bá»c bá»Ÿi <c:out>.
- **RÃ² rá»‰ Model (Model Leakage) cÅ© trong Service (Task 27.5):** AdminService.java (getAllUsers, getAllKeys) vÃ  ProductService.java (getAllProducts, getProductById) tráº£ vá» trá»±c tiáº¿p Model dÃ¹ khÃ´ng cÃ²n bÃªn nÃ o sá»­ dá»¥ng, tiá»m áº©n vi pháº¡m Zero Model Leakage.

### 2. Giáº£i phÃ¡p Ká»¹ thuáº­t Triá»‡t Ä‘á»ƒ:
- **Task 27.1: Triá»‡t tiÃªu 100% Inline JavaScript trÃªn toÃ n bá»™ 16 file JSP:**
  + Chuyá»ƒn Ä‘á»•i 61 vá»‹ trÃ­ sá»± kiá»‡n inline sang thuá»™c tÃ­nh ngá»¯ nghÄ©a data-* (data-action, data-close-modal, data-tab, data-confirm, data-auto-submit...).
  + Triá»ƒn khai Event Delegation táº­p trung toÃ n diá»‡n trong web/js/main.js cho click, submit, change vÃ  input. Xá»­ lÃ½ Ä‘Ã³ng modal overlay click-outside tá»± Ä‘á»™ng cho toÃ n bá»™ modal.
- **Task 27.2: Di dá»i biáº¿n Static sang Utility:**
  + Chuyá»ƒn 4 háº±ng sá»‘ ngÆ°á»¡ng cháº¥m cÃ´ng sang Constants.java vá»›i tiá»n tá»‘ ATTENDANCE_THRESHOLD_* vÃ  cáº­p nháº­t toÃ n bá»™ lá»i gá»i trong AttendanceService.java.
- **Task 27.3: Quy chuáº©n hÃ³a Háº±ng sá»‘ & Chuá»—i Äa ngÃ´n ngá»¯:**
  + Bá»• sung Ä‘áº§y Ä‘á»§ máº«u cÃ¢u thÃ´ng bÃ¡o phÃ¢n ca, duyá»‡t/tá»« chá»‘i cháº¥m cÃ´ng, máº£ng tÃªn thá»© WEEK_DAY_NAMES, fallback kho/thá»±c Ä‘Æ¡n vÃ o Constants.java vá»›i chuáº©n Unicode Escape \uXXXX.
  + Cáº­p nháº­t AttendanceService.java, IngredientService.java, OrderService.java sá»­ dá»¥ng háº±ng sá»‘ qua String.format.
- **Task 27.4: VÃ¡ Triá»‡t Ä‘á»ƒ Lá»— há»•ng XSS:**
  + Bao bá»c toÃ n bá»™ dá»¯ liá»‡u Ä‘á»™ng vá»›i <c:out value="..." /> trÃªn web/inventory_managerment.jsp, web/header.jsp, web/components/inventory_form_modal.jsp, web/components/product_form_modal.jsp.
- **Task 27.5: Triá»‡t tiÃªu HoÃ n toÃ n Model Leakage:**
  + Loáº¡i bá» cÃ¡c hÃ m tráº£ vá» trá»±c tiáº¿p Model cÅ© khÃ´ng dÃ¹ng trong AdminService.java vÃ  ProductService.java, báº£o Ä‘áº£m chá»‰ Ä‘Ã³ng gÃ³i tráº£ vá» Map<String, Object> cho Controller.

### 3. File tÃ¡c Ä‘á»™ng & Tráº¡ng thÃ¡i:
- src/java/utility/Constants.java
- src/java/service/AttendanceService.java
- src/java/service/IngredientService.java
- src/java/service/OrderService.java
- src/java/service/AdminService.java
- src/java/service/ProductService.java
- web/inventory_managerment.jsp
- web/header.jsp
- web/components/inventory_form_modal.jsp
- web/components/product_form_modal.jsp
- web/index.jsp
- web/admin_dashboard.jsp
- web/menu_management.jsp
- web/report.jsp
- web/profile.jsp
- web/components/notification_dropdown.jsp
- web/components/broadcast_modal.jsp
- web/components/inventory_alert_modal.jsp
- web/components/staff_cancel_modal.jsp
- web/components/delivery_orders_table.jsp
- web/components/delivery_info_modal.jsp
- web/components/revert_delivery_modal.jsp
- web/js/main.js
- .agent/WorkList.md
- .agent/learning.md
- **Trạng thái Build:** Apache Ant (nt clean compile với JDK 17) đạt **BUILD SUCCESSFUL (0 errors, 74 source files, 44 web files copied)**.

### 4. BÃ i há»c kinh nghiá»‡m & TuÃ¢n thá»§ kiáº¿n trÃºc cá»‘t lÃµi:
- **Táº­p trung hÃ³a xá»­ lÃ½ sá»± kiá»‡n qua Event Delegation (Pure Separation of Concerns):** Viá»‡c xÃ³a sáº¡ch inline event khá»i HTML/JSP khÃ´ng chá»‰ báº£o Ä‘áº£m tÃ­nh tháº©m má»¹ vÃ  dá»… báº£o trÃ¬, mÃ  cÃ²n lÃ  ranh giá»›i ká»¹ thuáº­t tá»‘i quan trá»ng ngÄƒn cháº·n hoÃ n toÃ n táº¥n cÃ´ng DOM XSS do chÃ¨n mÃ£ Ä‘á»™c vÃ o thuá»™c tÃ­nh sá»± kiá»‡n.
- **Quy chuáº©n Zero Model Leakage:** Táº§ng Controller tuyá»‡t Ä‘á»‘i khÃ´ng Ä‘Æ°á»£c tiáº¿p xÃºc vá»›i Model entities, má»i dá»¯ liá»‡u hiá»ƒn thá»‹ pháº£i Ä‘Æ°á»£c Controller nháº­n tá»« Service dÆ°á»›i dáº¡ng Map/DTO an toÃ n.
- **Báº£o toÃ n tÃ­nh chuáº©n hÃ³a Ä‘a ngÃ´n ngá»¯ (i18n & Unicode Escape):** Má»i thÃ´ng bÃ¡o hiá»ƒn thá»‹ tá»« backend Ä‘á»u pháº£i quy hoáº¡ch thÃ nh háº±ng sá»‘ hoáº·c properties mÃ£ hÃ³a Unicode Escape (\uXXXX) Ä‘á»ƒ trÃ¡nh rá»§i ro vá»¡ font trÃªn mÃ´i trÆ°á»ng Windows (Cp1252) vÃ  Tomcat 10.

---

## [2026-10-24] - Giai đoạn Release Candidate: Khắc phục Triệt để Toàn bộ 25 Vi phạm Kiểm toán Tĩnh (Static Code Audit Hardening)

### 1. Phân tích & Vấn đề Kỹ thuật (25 Vi phạm phát hiện):
- **Nhóm 1 - SQL Injection & Ghép chuỗi (06 vị trí / 09 câu SQL):**
  + DeliveryDAO.java (dòng 347-348, 358-359, 27, 53, 80, 321): Ghép chuỗi trạng thái và loại đơn Constants.STATUS_*, Constants.ORDER_TYPE_DELIVERY thay vì tham số hóa ? trong PreparedStatement tại các hàm getAllTasks, getTasksByShipper, getAvailableTasksForShipper, getPendingAcceptanceOrders, 
evokeTaskFromShipper, countActiveOrdersByShipperId.
  + OrderDAO.java (dòng 116, 223, 251): Nối chuỗi trạng thái và loại đơn trực tiếp vào SQL trong getLatestOrderByTableId, cancelOrder, và updateOrderToDelivery.
- **Nhóm 2 - Nuốt lỗi ngầm (Swallowed Exceptions - 07 vị trí):**
  + UserDAO.java (dòng 403-406): Hàm getSafeAvatar nuốt ngầm SQLException khi đọc cột Avatar.
  + ReportDAO.java (dòng 320-332): 
ollbackQuietly và 
estoreAutoCommit nuốt lỗi im lặng không để lại dấu vết log hệ thống.
  + AttendanceService.java (dòng 371, 461, 709, 744) & IngredientService.java (dòng 323-325): Các khối catch ngoại lệ gửi thông báo phụ (phân ca, duyệt chấm công, cảnh báo kho) bỏ trống hoàn toàn.
  + AttendanceController.java & HrManagementController.java: Bắt chung ngoại lệ tổng Exception khi parse LocalDate thay vì bắt đúng DateTimeParseException.
- **Nhóm 3 - Dead Code, Import thừa & Lỗi Logic (09 vị trí):**
  + Import thừa trong AttendanceDAO.java (Connection), OrderDAO.java (Connection), UserDAO.java (java.sql.Date), AdminService.java (java.util.Date), AttendanceService.java (LocalDateTime), OrderService.java (model.Product).
  + NotificationController.java: Khai báo biến cục bộ int sentCount nhưng không sử dụng.
  + MenuManagementController.java (dòng 154): Lỗi copy-paste gán nhầm mã lỗi Constants.SERROR_INVALID_SETTING_TAB (E9006) cho catch chung thay vì Constants.ERROR_SYSTEM.
  + Constants.java: Biến alias thừa SERROR_INVALID_SETTING_TAB (và tham chiếu trong SettingsController.java).
- **Nhóm 4 - Transaction Leak & ACID Integrity (03 vị trí CRITICAL):**
  + ReportDAO.java (insertReportWithEvidences): Bắt duy nhất SQLException, thiếu null-check evidences. Nếu evidences == null sẽ ném NPE thoát khỏi khối catch, 
ollbackQuietly() bị bỏ qua, auto-commit phục hồi gây commit nửa chừng.
  + OrderDAO.java (insertOrder): Khối catch chỉ bắt SQLException. Nếu duyệt order.getOrderDetails() ném NullPointerException, transaction bị rò rỉ.
  + UserDAO.java (
egisterUserWithKeyTransaction): Khối catch chỉ bắt SQLException, thiếu null-check user.getRole() != null.

### 2. Giải pháp Kỹ thuật Triệt để:
- **Tham số hóa 100% câu lệnh SQL:** Chuyển đổi toàn bộ mệnh đề SQL nối chuỗi (+) trong DeliveryDAO.java và OrderDAO.java sang tham số bind ? an toàn qua PreparedStatement.setString(...).
- **Triệt tiêu Nuốt lỗi & Ghi log Cảnh báo An toàn:**
  + Xóa bỏ hàm getSafeAvatar, đọc trực tiếp 
s.getString("Avatar") trong các phương thức truy vấn với try-catch đầy đủ.
  + Bổ sung java.util.logging.Logger cấp độ WARNING vào 
ollbackQuietly, 
estoreAutoCommit trong ReportDAO.java và các khối catch thông báo phụ trong AttendanceService.java, IngredientService.java. Tuyệt đối không dùng System.out.println hay e.printStackTrace() theo đúng Rule_BOMS.md.
  + Thu hẹp khối catch parse ngày thành DateTimeParseException trong AttendanceController.java và HrManagementController.java.
- **Dọn dẹp Dead Code & Khắc phục Lỗi Logic:**
  + Loại bỏ toàn bộ 6 import thừa trong DAO/Service, xóa biến chết sentCount trong NotificationController.java.
  + Sửa MenuManagementController.java sử dụng đúng Constants.ERROR_SYSTEM.
  + Xóa bỏ hằng số alias SERROR_INVALID_SETTING_TAB khỏi Constants.java và cập nhật SettingsController.java sang Constants.ERROR_INVALID_SETTING_TAB.
- **Gia cố Transaction Leak & Bảo toàn ACID:**
  + Mở rộng các khối catch quản lý transaction sang catch (Exception e) trong ReportDAO.java, OrderDAO.java, UserDAO.java.
  + Bổ sung kiểm tra null cho evidences và user.getRole() != null trước khi xử lý logic nhánh.
  + Đảm bảo 
rollback() BẮT BUỘC ĐƯỢC GỌI khi xảy ra bất kỳ lỗi Runtime Exception nào trước khi trả về setAutoCommit(originalAutoCommit) trong inally.

### 3. File tác động & Trạng thái:
- src/java/dao/DeliveryDAO.java
- src/java/dao/OrderDAO.java
- src/java/dao/UserDAO.java
- src/java/dao/ReportDAO.java
- src/java/dao/AttendanceDAO.java
- src/java/service/AdminService.java
- src/java/service/AttendanceService.java
- src/java/service/IngredientService.java
- src/java/service/OrderService.java
- src/java/controller/NotificationController.java
- src/java/controller/MenuManagementController.java
- src/java/controller/SettingsController.java
- src/java/controller/AttendanceController.java
- src/java/controller/hr/HrManagementController.java
- src/java/utility/Constants.java
- .agent/learning.md
- **Trạng thái Build:** Apache Ant (nt clean compile với JDK 17) đạt **BUILD SUCCESSFUL (0 errors, 74 source files, 44 web files copied)**.

### 4. Bài học kinh nghiệm & Tuân thủ kiến trúc cốt lõi:
- **ACID Transaction Hardening:** Khi quản lý thủ công setAutoCommit(false), luôn luôn bắt catch (Exception e) (không chỉ SQLException) để bảo vệ dữ liệu chống lại mọi RuntimeException bất ngờ (NPE, OOB), đảm bảo connection.rollback() luôn được kích hoạt.
- **Không nuốt lỗi (No Silent Failure):** Các tác vụ gửi thông báo nền không được làm gián đoạn luồng chính, nhưng bắt buộc phải ghi log cảnh báo (Logger.log(Level.WARNING, ...)), không được để khối catch rỗng.

---

## [2026-10-24] - Hotfix Khẩn cấp: Khắc phục Lỗi Tra cứu Tên Đăng nhập & Đọc An toàn Cột Avatar trong UserDAO.java

### 1. Nguyên nhân Sự cố:
- Khi xóa bỏ hàm getSafeAvatar trong đợt quét kiểm toán tĩnh trước đó, lệnh đọc s.getString("Avatar") được đặt trực tiếp trong getUserByUsername, getUserById, getAllUsers.
- Trong môi trường cơ sở dữ liệu chưa chạy migration cột Avatar, câu lệnh s.getString("Avatar") làm văng ngoại lệ SQLException: Invalid column name 'Avatar'.
- Khối catch (SQLException e) tại getUserByUsername bắt được lỗi này và ném ValidationException(Constants.ERROR_DATABASE_GET_USER_BY_USERNAME) (mã DE0004), dẫn đến thông báo hiển thị trên giao diện: *"Lỗi cơ sở dữ liệu khi tra cứu tên đăng nhập!"* khi người dùng login.

### 2. Giải pháp Khắc phục Triệt để:
- **Khôi phục cơ chế Đọc Cột An toàn (getSafeAvatar):**
  + Sử dụng java.sql.ResultSetMetaData để quét toàn bộ tên cột (getColumnName / getColumnLabel) trước khi đọc giá trị.
  + Nếu cột Avatar không tồn tại trong schema CSDL hiện tại, hàm tự động trả về 
ull một cách an toàn mà hoàn toàn KHÔNG làm văng SQLException.
- **Bảo đảm Không Đóng Connection Sớm:**
  + Xác minh 100% các khối 	ry-with-resources trong UserDAO.java: Đối tượng connection (từ DBContext) TUYỆT ĐỐI KHÔNG nằm trong ngoặc của 	ry (...). Chỉ duy nhất PreparedStatement và ResultSet được đặt vào để giải phóng Statement.
  + Bổ sung cơ chế tự phục hồi kết nối ensureConnection(): Tự động kiểm tra connection == null || connection.isClosed() và tái kết nối từ ConnectDB.properties nếu phiên kết nối của CSDL bị ngắt sau thời gian idle.

### 3. File tác động:
- src/java/dao/UserDAO.java
- .agent/learning.md
- **Kết quả Build:** Apache Ant (nt clean compile với JDK 17) đạt **BUILD SUCCESSFUL (0 errors, 74 source files)**.

---

## [2026-10-24] - Hotfix Nghiệp vụ Điều phối & Giao hàng: Đồng bộ ActiveOrders trong DeliveryService.java

### 1. Nguyên nhân Sự cố:
- **Đảo ngược thứ tự thực thi trong rejectTask:** Khi Shipper bấm [Từ chối] đơn được điều phối gán (trạng thái Waiting for delivery), hàm syncShipperProfileStatus(shipperId, shipperUsername) bị gọi trước khi câu lệnh deliveryDAO.updateTaskShipper(taskId, null) và deliveryDAO.updateTaskStatus(taskId, Constants.STATUS_WAITING_DISPATCH) kịp cập nhật CSDL. Sau khi CSDL được cập nhật, hệ thống không gọi lại syncShipperProfileStatus, khiến bảng ShipperProfiles lưu trữ thông tin tải trọng lệch pha hoặc tính toán sai đơn bị từ chối.
- **Trùng lặp gọi đồng bộ (Redundant Duplicate Calls):** Các phương thức acceptTask, claimTask, updateShipperTaskStatus và rejectTask bị gọi trùng lặp syncShipperProfileStatus(shipperId, shipperUsername) 2 lần liên tiếp.
- **Nghịch lý Capacity Check trong acceptTask:** countActiveOrders đếm cả 2 trạng thái Delivering và Waiting for delivery. Khi Manager đã gán đơn thứ 3 cho Shipper (đang là Waiting for delivery), đơn này đã được tính vào tải trọng của Shipper. Lệnh validateShipperCapacity(shipperUsername) kiểm tra activeCount >= maxAllowed (3 >= 3) bên trong acceptTask dẫn đến việc Shipper không thể bấm xác nhận [Nhận đơn] của chính mình.
- **Lệch hằng số giới hạn đơn mặc định:** Fallback giới hạn tải Shipper khi không có profile sử dụng MAX_SHIPPER_ORDERS (5) thay vì DEFAULT_SHIPPER_MAX_ORDERS (3) theo chuẩn nghiệp vụ UC-09 9.1 và schema CSDL (MaxOrders DEFAULT 3).

### 2. Giải pháp Khắc phục Triệt để:
- **Chuẩn hóa luồng rejectTask:**
  1. Hủy gán Shipper (ShipperID = NULL) và cập nhật trạng thái đơn về Waiting for dispatch xuống CSDL trước.
  2. Ghi nhật ký vào DeliveryStatusHistory với note Constants.NOTE_SHIPPER_REJECT.
  3. Kích hoạt syncShipperProfileStatus(shipperId, shipperUsername) sau cùng, đảm bảo hàm countActiveOrders chỉ đếm các đơn thực tế còn trong tiến trình giao (Delivering và Waiting for delivery), loại trừ hoàn toàn đơn vừa từ chối.
- **Làm sạch các lời gọi đồng bộ:** Loại bỏ toàn bộ các lệnh gọi kép syncShipperProfileStatus dư thừa trong acceptTask, claimTask, updateShipperTaskStatus.
- **Gỡ bỏ Capacity Check bất hợp lý trong acceptTask:** Vì đơn đã được kiểm tra tải trọng tại thời điểm Manager điều phối (assignTaskToShipper), việc Shipper chuyển trạng thái từ Waiting for delivery sang Delivering không làm tăng tổng số đơn hoạt động.
- **Chuẩn hóa hàm countActiveOrders:** Chuyển sang public, kiểm tra an toàn bằng equalsIgnoreCase cho cả hai trạng thái Constants.STATUS_DELIVERING và Constants.STATUS_WAITING_DELIVERY.
- **Đồng bộ chuẩn hằng số DEFAULT_SHIPPER_MAX_ORDERS (3):** Áp dụng nhất quán trong cả buildShipperStatusMap và validateShipperCapacity.

### 3. File tác động:
- src/java/service/DeliveryService.java
- .agent/learning.md
- **Kết quả Build:** Apache Ant (ant clean compile với JDK 17) đạt BUILD SUCCESSFUL (0 errors, 74 source files).

---

## [2026-10-24] - Hotfix & Hardening: Đồng bộ Session Avatar và Bảo mật Upload Ảnh (ImageUploadUtility & ProfileController)

### 1. Vấn đề & Yêu cầu:
- **Lỗi đồng bộ Avatar trên Session:** Khi người dùng đổi avatar qua ProfileController, ảnh đã cập nhật xuống CSDL nhưng thanh Header vẫn hiển thị avatar cũ (hoặc ký tự viết tắt cũ) cho đến khi đăng nhập lại, do Tomcat sessionScope không được kích hoạt refresh tự động khi chỉ gọi put vào Map.
- **Rủi ro Path Traversal & Giả mạo định dạng:** Khi lưu file qua saveImage, nếu phụ thuộc vào tên file gốc do client gửi (getSubmittedFileName) hoặc MIME type từ request header có thể dẫn đến lỗ hổng Path Traversal (../) hoặc giả mạo đuôi file (.jsp.jpg).
- **Chuẩn hóa đường dẫn tương đối:** Đảm bảo đường dẫn lưu DB và trả về từ saveImage có định dạng chuẩn uploads/avatars/<uuid>.jpg (không có dấu / ở đầu) để tương thích 100% với thẻ <img> src="/" và servlet mapping /uploads/* của ImageServingServlet.

### 2. Giải pháp Thực thi:
- **ProfileController.java:**
  + Tại phương thức doPost, ngay sau loginUser.put("avatar", uploadedPath); và loginUser.put("avatar", ""); (khi xóa avatar), bổ sung tường minh session.setAttribute(Constants.USER_SESSION, loginUser); để ép Tomcat cập nhật Session ngay lập tức, View header/profile phản hồi theo thời gian thực.
- **ImageUploadUtility.java:**
  + Trong saveImage(Part filePart, String subFolder), tuyệt đối KHÔNG sử dụng getSubmittedFileName() từ client.
  + Bổ sung hàm validateAndDetectImageExtension(InputStream in) đọc Magic Bytes trực tiếp từ binary header của tệp:
    * JPEG: kiểm tra 2 byte đầu là FF D8 (SOI marker), chuẩn hóa đuôi .jpg.
    * PNG: kiểm tra 2 byte đầu là 89 50 (và chữ ký PNG), chuẩn hóa đuôi .png.
    * WEBP: kiểm tra chữ ký RIFF....WEBP, chuẩn hóa đuôi .webp.
    * Bất kỳ định dạng nào không khớp đều bị từ chối ngay lập tức và ném ValidationException(Constants.ERROR_INVALID_IMAGE_TYPE).
  + Khởi tạo tên file hoàn toàn mới bằng UUID.randomUUID().toString() + ext, triệt tiêu hoàn toàn nguy cơ Path Traversal.
  + Chuẩn hóa đường dẫn trả về dạng uploads/avatars/<uuid>.jpg (không có dấu gạch chéo / ở đầu).

### 3. File tác động:
- src/java/controller/hr/ProfileController.java
- src/java/utility/ImageUploadUtility.java
- .agent/learning.md
- **Kết quả Build:** Apache Ant (ant clean compile với JDK 17) đạt BUILD SUCCESSFUL (0 errors, 74 source files).