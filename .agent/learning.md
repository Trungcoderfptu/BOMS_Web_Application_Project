# BOMS LEARNING & PROGRESS LOG

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
- src/java/dao/OrderDAO.java: Thêm hàm revertOrderFromDelivery(int orderId, String newOrderType, String newStatus).
- src/java/dao/DeliveryDAO.java: Khôi phục toàn bộ các hàm nghiệp vụ Task 6 và bổ sung countActiveOrdersByShipperId(int shipperId).
- src/java/service/OrderService.java: Bổ sung revertDeliveryOrder và tích hợp kiểm tra trong updateOrderStatus và cancelOrder.
- web/components/rrevert_delivery_modal.jsp (Tạo mới): Component modal cảnh báo trước khi hoàn tác đơn giao hàng.
- web/order_management.jsp: Include revert_delivery_modal.jsp.
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
