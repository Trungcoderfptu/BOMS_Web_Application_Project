# BOMS LEARNING & PROGRESS LOG

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
- `web/WEB-INF/Upload.properties` (Tạo mới): Khai báo `upload.dir` (mặc định `C:/BOMS_uploads/reports`), nằm NGOÀI dự án để không bị xóa khi Clean and Build.
- `web/WEB-INF/web.xml`: Đăng ký 3 Servlet; `ReportController` có `<multipart-config>` giới hạn 20MB/file.
- `web/header.jsp`: Link "Báo cáo" (Admin/Manager) trỏ `ReportManagementController`; thêm link "Báo cáo ẩn danh" cho mọi tài khoản đã đăng nhập.
- `src/java/utility/Constants.java`: Mã S0300-S0301, E0300-E0316, DE0012-DE0015 và hằng số nghiệp vụ báo cáo.
- `messages_vi.properties` & `messages_en.properties`: Bản dịch đầy đủ cho các khóa `report.*` và mã lỗi mới.
- `web/css/global.css`: Class `.report-*`, `.input-error`, `.ticket-box`, `.report-badge-*`.
- `web/js/main.js`: `initEvidenceFileCheck` kiểm tra số lượng / định dạng / dung lượng file ngay khi chọn (server vẫn kiểm tra lại).

### 2. Bài học kinh nghiệm:
- **Không dùng `Paths.get` với tên file người dùng gửi lên:** Tên chứa ký tự Windows cấm (`?`, `*`, `:`) làm ném `InvalidPathException` -> lỗi 500. Chỉ cắt chuỗi sau dấu `/` cuối cùng và lưu file trên ổ đĩa bằng tên UUID.
- **Không khai báo `static` ngoài package utility** (kể cả hằng số chuỗi SQL trong DAO): dùng `private final`.
- **Upload file trong Tomcat:** Khi file vượt `max-file-size`, `request.getParts()` ném `IllegalStateException` -> Controller phải bắt và đổi thành mã lỗi (E0306). Trình duyệt không cho điền lại file khi forward về form, cần nhắc người dùng chọn lại.
- **Kiểm thử tiếng Việt bằng curl trên Git Bash:** Tham số dòng lệnh không phải UTF-8 nên dữ liệu bị lỗi font; phải đọc nội dung từ file UTF-8 (`-F "field=<file.txt"`) mới phản ánh đúng trình duyệt.

