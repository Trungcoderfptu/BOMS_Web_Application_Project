# BOMS LEARNING & PROGRESS LOG

## [2026-10-15] - Hoàn thiện Hệ thống Tải ảnh & Streaming Độc lập, Khắc phục Lỗi Kết nối Đặt lại và Lỗi Cơ sở dữ liệu khi dán Link Web
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

