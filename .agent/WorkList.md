# LUỒNG TRIỂN KHAI VÀ PHÂN CHIA TASK: UC-01 & UC-02 (QUẢN LÝ BÁN HÀNG & KDS)

## 🛒 UC-01: BÁN HÀNG TẠI QUẦY (Create Order at Counter)
**Actor:** Nhân viên quầy (Floor & Counter Staff)
**Mô tả:** Khách chọn món tại quầy, nhân viên thao tác POS, thanh toán tiền mặt và chuyển trạng thái đơn.

- [x] **Task 1.1: Hiển thị Menu đồ uống** (Bước 2): Tải danh sách món từ DB lên giao diện `menu.jsp` bằng hệ thống Tab. **[DONE]**
- [x] **Task 1.2: Thêm món vào giỏ hàng** (Bước 3): Lưu tạm dữ liệu vào `CURRENT_CART` trong Session, hiển thị Badge đếm số lượng món. **[DONE]**
- [x] **Task 1.3: Xem chi tiết giỏ hàng** (Bước 5): Hiển thị UI dạng bảng, tính tổng tiền, tắt/bật nút Floating Cart động. **[DONE]**
- [x] **Task 1.4: Chốt đơn cơ bản** (Bước 7): Lưu thông tin vào bảng `Orders` và `OrderDetails`, dọn dẹp Session giỏ hàng, hiển thị Flash Message. **[DONE]**
- [x] **Task 1.5: Tinh chỉnh giỏ hàng (Alt Flow 1.1):** Thêm nút `[+]`, `[-]` và `[Xóa]` (Thùng rác) vào bảng trong tab giỏ hàng để cập nhật số lượng trực tiếp trong Session trước khi thanh toán.**[DONE]**
- [x] **Task 1.6: Trừ kho tự động (Postcondition 3):** Xây dựng logic gọi Service/DAO (hoặc DB Trigger) để tự động trừ số lượng nguyên liệu tương ứng ngay khi chốt đơn thành công.**[DONE]**
- [x] **Task 1.7: Trang Quản lý & Theo dõi Đơn (Dành cho Nhân viên / KDS):** Làm 1 trang riêng cho nhân viên hiển thị danh sách đơn hàng real-time. Có các nút thao tác chuyển trạng thái: `Chờ xác nhận` -> `Đã nhận/Đang làm` -> `Đã hoàn thành`.**[DONE]**

## 📱 UC-02: KHÁCH TỰ ĐẶT MÓN TẠI BÀN (Place Order at Table)
**Actor:** Khách hàng (Customers)
**Mô tả:** Khách quét mã QR tại bàn, tự chọn món qua trình duyệt web, thanh toán qua cổng điện tử(chưa cần cổng điện tử mặc định là trả tiền mặt) và theo dõi trạng thái.

- [x] **Task 2.1: Tái sử dụng giao diện chọn món** (Bước 2, 3, 5): Dùng chung cấu trúc `menu.jsp` và Tab Giỏ hàng đã hoàn thiện ở UC-01. **[DONE]**
- [x] **Task 2.2: Khởi tạo phiên qua QR (Bước 1):** Xây dựng Controller hứng URL có chứa tham số bàn (ví dụ: `/menu?tableId=5`). Lưu `tableId` này vào Session để gắn tự động vào đơn hàng khi lưu DB. **[DONE]**
- [x] **Task 2.3: Xử lý giao diện hết hàng (Exception 2.0.E1):** Làm mờ (gray out) hoặc ẩn nút "Thêm vào giỏ" đối với các sản phẩm có số lượng = 0 để khách không thể chọn. **[DONE]**
- [ ] **Task 2.4: Tích hợp Cổng thanh toán (Bước 6, 7):** Xây dựng `PaymentController` điều hướng giỏ hàng sang bên thứ 3 (VNPay/MoMo) khi khách bấm nút "Thanh toán Online".
- [ ] **Task 2.5: Xử lý ngoại lệ thanh toán (Exception 2.0.E2):** Hứng kết quả trả về từ Cổng thanh toán.
  - *Nếu lỗi/hủy:* Trả về UI báo lỗi, giữ nguyên Session giỏ hàng.
  - *Nếu thành công:* Gọi logic chốt đơn (Task 1.4) và lưu DB với trạng thái `Đã thanh toán / Chờ xác nhận`.
- [x] **Task 2.6: Modal Xác Nhận & Tab Theo Dõi Đơn Của Bàn (Alt Flow 2.1):** **[DONE]**
  - **Modal Xác nhận đơn hàng:** Thay vì chốt đơn ngay, hiển thị bảng nổi xác nhận (`order_confirm_modal.jsp`) tóm tắt giỏ hàng, số bàn và tổng tiền trước khi gửi yêu cầu.
  - **Tab Theo dõi đơn:** Khách theo dõi danh sách các đơn đã đặt của bàn với trạng thái real-time (`Chờ xác nhận`, `Đang làm`, `Đã hoàn thành`).
  - **Hủy đơn khi chưa nấu:** Cung cấp nút **[Hủy đơn]** khi đơn chưa vào bếp (`cookingStartedAt == null`). (Đã loại bỏ chức năng đổi món khỏi Task 2.6).
  - **Tự động ẩn đơn:** Tự động ẩn các đơn đã hoàn thành sau 10 phút để giữ sạch màn hình cho phiên khách tiếp theo.

  ## 🍽️ TASK 3: QUẢN LÝ & ĐIỀU CHỈNH THỰC ĐƠN (Menu Management)
**Actor:** Nhân viên (Floor & Counter Staff) & Quản lý cửa hàng (Store Managers)
**Mô tả:** Cho phép nhân viên và quản lý tạo món mới, cập nhật thông tin thực đơn (giá cả, mô tả, danh mục, trạng thái còn hàng/hết hàng...) dựa trên cấu trúc bảng trong `BOMS_DATABASE.sql`. **Lưu ý cho AI: Thực hiện tuần tự từng sub-task một, hoàn thành và được duyệt mới chuyển sang bước tiếp theo.**

- [x] **Task 3.1: Khảo sát CSDL & Xây dựng tầng DAO cho Thực đơn:** **[DONE]**
  - Đọc `BOMS_DATABASE.sql` và Model/DAO hiện tại của thực đơn để nắm chính xác các cột dữ liệu (giá, tên, trạng thái, phân loại...).
  - Bổ sung các hàm CRUD trong DAO: Thêm món mới (`insert`), Cập nhật thông tin/giá cả (`update`), và Đổi trạng thái hiển thị/tồn kho của món. Bắt buộc bọc `try-catch` và ném `ValidationException` kèm mã lỗi `DEXXX`.
- [x] **Task 3.2: Xây dựng tầng Nghiệp vụ & Validation (Service & Utility):** **[DONE]**
  - Bổ sung hàm Validation tầng 1 (kiểm tra tên món rỗng, định dạng giá tiền > 0) tại `Validator`.
  - Xây dựng Service xử lý nghiệp vụ thêm/sửa món, kiểm tra trùng tên món (Validation tầng 2) và đóng gói dữ liệu trả về dạng `Map` (không để lọt Model lên Controller).
- [x] **Task 3.3: Thiết kế Giao diện Quản lý Thực đơn (View & Components):** **[DONE]**
  - Tạo giao diện bảng quản lý thực đơn và Form/Modal phục vụ "Thêm món mới" & "Chỉnh sửa món" (điều chỉnh giá, thông tin, trạng thái).
  - Tách riêng các thành phần phức tạp vào `web/components/`, toàn bộ style đưa vào `global.css` và script xử lý đóng/mở modal đưa vào `main.js`.
- [x] **Task 3.4: Điều phối chức năng Tạo món mới (Create Menu Item Controller):** **[DONE]**
  - Xây dựng Controller hứng dữ liệu tạo món mới từ Form, kiểm tra quyền truy cập (Staff/Manager), gọi Service xử lý và điều hướng bằng `sendRedirect` (chuẩn PRG) kèm thông báo Flash Message qua `Session`.
- [x] **Task 3.5: Điều phối chức năng Chỉnh sửa & Cập nhật giá/trạng thái (Update Menu Item Controller):** **[DONE]**
  - Xây dựng Controller xử lý việc cập nhật giá cả, thông tin chi tiết hoặc bật/tắt trạng thái món ăn trên thực đơn, trả kết quả qua Flash Message và đồng bộ ngay lập tức với giao diện POS bán hàng.

## 🌐 TASK 4: CHUẨN HÓA & ĐỒNG BỘ MÃ HÓA UNICODE ESCAPE TRONG FILE ĐA NGÔN NGỮ
**Actor:** System / Developer
**Mô tả:** Để đảm bảo tính tương thích tuyệt đối 100% với môi trường phát triển của toàn bộ team (NetBeans 17, JDK 17, Tomcat 10 trên Windows), toàn bộ file `.properties` bắt buộc phải lưu dưới chuẩn Unicode Escape (`\uXXXX`), tuyệt đối không lưu UTF-8 thô để tránh nguy cơ vỡ font (Mojibake) và corrupt tệp khi thành viên trong team mở và sửa trong NetBeans.

- [x] **Task 4.1: Rà soát và đồng bộ 100% `messages_vi.properties` sang chuẩn Unicode Escape (`\uXXXX`):** **[DONE]**
  - Quét toàn bộ file `messages_vi.properties`.
  - Chuẩn hóa văn phong, ngữ pháp và chính tả hiển thị cho toàn bộ nhãn hệ thống và thông báo lỗi/thành công.
  - Mã hóa đồng bộ 100% các ký tự tiếng Việt có dấu sang định dạng Unicode Escape (`\uXXXX`) thuần ASCII trên đĩa, bảo toàn nguyên vẹn 100% Key và tương thích hoàn hảo với NetBeans 17 và Tomcat 10.

## 🗂️ TASK 5: TÁI CẤU TRÚC & PHÂN NHÓM MÃ TRẠNG THÁI (ERROR & SUCCESS CODES)
**Actor:** System / Developer
**Mô tả:** Sắp xếp và quy hoạch lại hệ thống mã thông báo trong `utility/Constants.java` và các file `.properties` theo từng phân mục rõ ràng để dễ tra cứu. **Chỉ áp dụng cho các chuỗi mã lỗi số và mã thành công số (như `EXXX`, `DEXXX`, `SXXX`). Thực hiện từng bước để không làm gãy code.**

- [x] **Task 5.1: Quy hoạch và phân mục lại mã trong `Constants.java`:** **[DONE]**
  - Gom nhóm và sắp xếp lại toàn bộ các hằng số chứa mã lỗi (`EXXX`), mã lỗi CSDL (`DEXXX`), và mã thành công (`SXXX`) trong `utility/Constants.java` theo từng cụm chức năng rõ ràng (ví dụ: Nhóm Hệ thống/Chung, Nhóm Xác thực/Auth, Nhóm Quản trị/Admin, Nhóm Thực đơn & Giỏ hàng/POS, Nhóm Cơ sở dữ liệu/Database) có comment phân vùng ngắn gọn.
  - Giữ nguyên các hằng số cấu hình khác (đường dẫn JSP, tên biến Session/Attribute...).
- [x] **Task 5.2: Rà soát và cập nhật đồng bộ toàn bộ mã nguồn Java & JSP:** **[DONE]**
  - Quét toàn bộ các tầng `dao/`, `service/`, `controller/` và `web/` để kiểm tra và cập nhật lại các vị trí gọi hằng số hoặc mã lỗi bị ảnh hưởng bởi đợt sắp xếp ở Task 5.1, đảm bảo dự án biên dịch thành công 100% không phát sinh lỗi.
- [x] **Task 5.3: Sắp xếp và đồng bộ hóa các file `.properties` (`messages_vi.properties` & `messages_en.properties`):** **[DONE]**
  - Xếp lại thứ tự các cặp `Key=Value` của các mã `EXXX`, `DEXXX`, `SXXX` trong cả 2 file ngôn ngữ Việt - Anh sao cho khớp 100% với thứ tự phân mục mới trong `Constants.java`.
  - Kiểm tra chéo để đảm bảo không bị thiếu sót bất kỳ mã nào giữa `Constants.java` và 2 file `.properties`.

## 🚚 TASK 6: TÍCH HỢP & NÂNG CẤP LUỒNG GIAO HÀNG (UC-09 & UC-10: SHIPPER & ORDER MANAGEMENT INTEGRATION)
**Actor:** Nhân viên quầy (Floor & Counter Staff), Quản lý cửa hàng (Store Managers), Shipper
**Mô tả:** Kết nối 2 luồng Quản lý đơn hàng (Order Management) và Giao hàng (Delivery/Shipper); sửa lỗi lệch chuẩn Model/CSDL, sửa lỗi trạng thái đơn hàng và bổ sung giao diện pop-up nhập thông tin giao hàng khi chuyển trạng thái đơn.
**Quy tắc:** Thực hiện tuần tự từng task theo độ ưu tiên từ cao xuống thấp. Hoàn thành task trước mới chuyển sang task tiếp theo.

### 🔴 MỨC ƯU TIÊN 1: NỀN TẢNG DỮ LIỆU & SỬA LỖI TRẠNG THÁI (DATA CONSISTENCY & BUG FIX)
- [x] **Task 6.1: Chuẩn hóa Model & Khắc phục Lỗi Trạng Thái Đơn Hàng:** **[DONE]**
  - **Tạo Entity Model chuẩn khớp với `BOMS_DATABASE.sql`:**
    + `ShipperProfile.java` đại diện bảng `ShipperProfiles` (`userId`, `vehiclePlate`, `shipperStatus`, `activeOrders`, `maxOrders`).
    + `DeliveryArea.java` đại diện bảng `DeliveryAreas` (`areaId`, `areaName`).
    + `DeliveryStatusHistory.java` đại diện bảng `DeliveryStatusHistory` (`historyId`, `orderId`, `shipperId`, `status`, `failReason`, `proofImage`, `updatedAt`).
    + Tái cấu trúc `DeliveryTask.java` về đúng vai trò DTO/View Object, không coi là Entity đại diện bảng.
  - **Sửa lỗi nghiêm trọng về trạng thái trong `Constants.java` & `DeliveryService.java`:**
    + Sửa lỗi Shipper từ chối đơn (`rejectTask`): Chuyển trạng thái về `'Waiting for dispatch'` (Chờ điều phối lại), **TUYỆT ĐỐI KHÔNG** dùng `'Cancelled'` (tránh gây nhầm lẫn và xung đột với nghiệp vụ hủy đơn hoàn kho của hệ thống).
    + Ghi nhận bản ghi vào `DeliveryStatusHistory` khi Shipper từ chối đơn phục vụ quản lý hiệu suất theo quyết định phê duyệt.
    + Chuẩn hóa các hằng số trạng thái khớp với ràng buộc `CK_Order_Status` của CSDL: `'Waiting for dispatch'`, `'Waiting for delivery'`, `'Picked up'`, `'Delivering'`, `'Delivered'`, `'Returned'`.
  - **Sửa giới hạn gom đơn:** Đọc giới hạn `MaxOrders` từ `ShipperProfiles` (mặc định 3 theo UC-09 9.1), loại bỏ việc hardcode `activeCount >= 5` trong `DeliveryService`.

### 🟠 MỨC ƯU TIÊN 2: CẦU NỐI ĐIỀU PHỐI TỪ QUẢN LÝ ĐƠN HÀNG (STAFF ORDER DISPATCHING)
- [x] **Task 6.2 [DONE]: Modal Thu Thập Thông Tin Giao Hàng Khi Đổi Sang Trạng Thái Giao (`delivery_info_modal.jsp`):**
  - **Tạo Component Modal:** Tạo file `web/components/delivery_info_modal.jsp` và nhúng vào `order_management.jsp`.
  - **Kịch bản Client:** Khi Staff/Manager chọn cập nhật trạng thái đơn sang "Chờ giao hàng" (`Waiting for dispatch`):
    + Không submit form ngay mà kích hoạt modal popup nổi.
    + Modal hiển thị form nhập: Tên khách hàng (`customerName`), Số điện thoại (`customerPhone`), Địa chỉ nhận hàng (`deliveryAddress`), và Dropdown chọn Khu vực giao hàng (`areaId` nạp từ `DeliveryAreas`).
  - **Xử lý Controller & Service:**
    + `OrderManagementController` tiếp nhận action chuyển đơn sang giao hàng.
    + Validation tầng 1 (kiểm tra rỗng địa chỉ, số điện thoại hợp lệ).
    + `OrderService` cập nhật bản ghi `Orders`: Gán `OrderType = 'Delivery'`, nạp thông tin khách hàng/địa chỉ/khu vực và gán `Status = 'Waiting for dispatch'`.

### 🟡 MỨC ƯU TIÊN 3: GIAO DIỆN QUẢN LÝ ĐƠN HÀNG GIAO CHO SHIPPER
- [x] **Task 6.3 [DONE]: Tab "Đơn Hàng Giao Cho Shipper" Trên `order_management.jsp`:**
  - **Thiết kế Tab chuyên biệt:** Bổ sung Tab "Đơn giao Shipper" (kế bên các tab Tất cả / Tại quầy / Tại bàn) trên `order_management.jsp`.
  - **Hiển thị danh sách đơn vận chuyển:** Lọc các đơn có `OrderType = 'Delivery'` với các trạng thái thời gian thực: `Chờ điều phối`, `Chờ shipper nhận`, `Đang giao`, `Đã giao`, `Giao thất bại/Hoàn hàng`.
  - **Thông tin chi tiết:** Hiển thị tên Shipper được gán (nếu có), khu vực giao hàng, số điện thoại khách, địa chỉ và mã đơn hàng.

### 🟢 MỨC ƯU TIÊN 4: ĐỒNG BỘ HIỂN THỊ ĐƠN CHO SHIPPER & TÁC VỤ GIAO HÀNG
- [x] **Task 6.4: Đồng Bộ Hiển Thị Đơn Chờ Giao & Nhận/Từ Chối Đơn Phía Shipper:** **[DONE]**
  - **Đồng bộ hàng chờ:** Ngay khi Staff xác nhận chuyển đơn sang giao hàng (`Waiting for dispatch`), đơn hàng tự động xuất hiện trên `dispatch_dashboard.jsp` để điều phối gán Shipper, đồng thời xuất hiện trong bể đơn chờ tiếp nhận theo khu vực `ShipperAreas` trên `shipper_dashboard.jsp`.
  - **Tác vụ Shipper (`shipper_dashboard.jsp`):**
    + Shipper thấy đơn hàng mới chờ nhận trong khu vực và có thể bấm [Nhận giao đơn này] (`claimTask`).
    + Shipper thấy đơn được điều phối gán trong bảng "Đơn chờ xác nhận": [Nhận đơn] (`acceptTask`) chuyển sang `Delivering`, tự động cập nhật `ActiveOrders` và `ShipperStatus`; [Từ chối] (`rejectTask`) gỡ Shipper, trả về `Waiting for dispatch` và ghi nhật ký `DeliveryStatusHistory`.
    + Chuẩn hóa kiến trúc PRG và Session Flash Messages cho cả `DispatchController` và `ShipperController`.

### 🔵 MỨC ƯU TIÊN 5: LỊCH SỬ GIAO HÀNG, BẰNG CHỨNG & HOÀN THIỆN ĐIỀU HƯỚNG
- [x] **Task 6.5: Bằng Chứng Giao Hàng & Lý Do Giao Thất Bại (UC-10):** **[DONE]**
  - **Upload ảnh bằng chứng (`ProofImage`):** Tận dụng `ImageUploadUtility.saveImage(part, "delivery")` khi Shipper chọn hoàn thành (`Delivered`), lưu ảnh xác nhận giao hàng vào thư mục công khai.
  - **Nhập lý do thất bại (`FailReason`):** Khi Shipper chọn hoàn hàng (`Returned`), hiển thị modal bắt buộc nhập lý do (gợi ý sẵn các lý do phổ biến) và validate chặt chẽ ở tầng Service/DAO.
  - **Ghi nhật ký:** Tự động lưu bản ghi vào bảng `DeliveryStatusHistory` ghi nhận mốc thời gian, người giao, trạng thái, ảnh bằng chứng hoặc lý do hoàn hàng.
- [x] **Task 6.6: Hoàn Thiện Điều Hướng Header & Phân Quyền URL:** **[DONE]**
  - Cập nhật `web/header.jsp`:
    + Bổ sung liên kết "Điều phối giao hàng" trỏ tới `${pageContext.request.contextPath}/DispatchController` cho vai trò `Manager`.
    + Kích hoạt liên kết "Nhiệm vụ giao hàng" trỏ tới `${pageContext.request.contextPath}/ShipperController` cho vai trò `Shipper`.


### 🛡️ MỨC ƯU TIÊN 6: ĐỒNG BỘ HAI CHIỀU & BẢO TOÀN DỮ LIỆU ĐƠN HÀNG (BI-DIRECTIONAL SYNC & SAFETY REVERT)
- [x] **Task 6.7: Khắc Phục Triệt Để Lỗi Đồng Bộ Khi Chuyển Ngược Trạng Thái Đơn Giao Hàng Về Phục Vụ Nội Bộ:** **[DONE]**
  - **Phân tích lỗi:** Khi đơn chuyển sang `Delivery` rồi sau đó đổi ngược về `Accepted`/`Prepared` từ tab "Tất cả đơn", `OrderType` cũ vẫn giữ nguyên là `Delivery`, khiến đơn tiếp tục xuất hiện sai lệch ở tab "Đơn giao Shipper" và treo `ShipperID`.
  - **Cơ chế Hoàn tác An toàn (Safe Revert):**
    + Chặn hoàn tác nếu đơn đang hoặc đã giao (`Delivering`, `Delivered`, `Returned`).
    + Phục hồi `OrderType` chuẩn xác: `'Table'` (nếu có `TableID`) hoặc `'Counter'`.
    + Giải phóng `ShipperID = NULL`, ghi vết vào `DeliveryStatusHistory`, đồng bộ lại `ActiveOrders` và `ShipperStatus` của Shipper.
    + Bổ sung modal cảnh báo xác nhận nguy cơ (`revert_delivery_modal.jsp`) trước khi thực hiện chuyển đổi.

## TASK 7: CHIẾN DỊCH TÁI CẤU TRÚC VÀ DỌN DẸP MÃ NGUỒN (REFACTORING & CLEANUP)
**Actor:** AI Agent / Developer
**Mô tả:** Dự án đang tồn tại 6 điểm vi phạm nghiêm trọng quy tắc Rule_BOMS.md (rò rỉ Model, trùng lặp code, code rác hoang tưởng, hardcode chuỗi, sai chuẩn mã lỗi backend). Yêu cầu AI Agent thực hiện TUẦN TỰ từng task dưới đây theo thứ tự ưu tiên. Bắt buộc hoàn thành và báo cáo từng Sub-task trước khi chuyển sang Sub-task tiếp theo. Tuyệt đối không làm gộp.


### MỨC ƯU TIÊN 1: XÓA CODE HOANG TƯỞNG VÀ SỬA LỖI RÒ RỈ MODEL (CRITICAL)
- [x] **Task 7.1: Xóa đoạn code dùng Reflection sai chuẩn trong IngredientAlertController.java** **[DONE]**
  - Vị trí: Trong hàm doPost của IngredientAlertController.
  - Hành động: Xóa sạch khối code dài 30 dòng cố gắng check `userObj instanceof Map` và dùng `java.lang.reflect.Method` để ép kiểu Session. 
  - Giải pháp: Thay thế bằng 2 dòng code chuẩn hóa của dự án:
    `Map<String, String> userSession = (Map<String, String>) session.getAttribute(Constants.USER_SESSION);`
    `int userId = Integer.parseInt(userSession.get("userId"));`

- [x] **Task 7.2: Tiêu diệt Model DeliveryTask.java và chặn rò rỉ Model lên View** **[DONE]**
  - Hành động 1: Xóa hoàn toàn file src/java/model/DeliveryTask.java.
  - Hành động 2: Mở DeliveryDAO.java và DeliveryService.java, sửa đổi tất cả các hàm đang trả về kiểu List<DeliveryTask> (hoặc Object này) thành List<Map<String, Object>>. 
  - Hành động 3: Bên trong các hàm DAO/Service, khởi tạo `Map<String, Object> map = new HashMap<>();` và nạp các key ("orderCode", "customerName", "customerAddress", "totalAmount", "status", "taskId") vào Map thay vì dùng Constructor của DeliveryTask. Controller sẽ tự động truyền List Map này ra View. Tuyệt đối không cần sửa file .jsp.

### MỨC ƯU TIÊN 2: SỬA LỖI UI VÀ CHUẨN HÓA HẰNG SỐ (HIGH)
- [x] **Task 7.3: Sửa lỗi hiển thị ???status.Waiting for dispatch??? trên dispatch_dashboard.jsp** **[DONE]**
  - Nguyên nhân: File JSP đang dùng `<fmt:message key="status.${task.status}" />` nhưng thiếu key trong file properties.
  - Hành động: Thay thẻ in trạng thái tại cột status-cell bằng cấu trúc `<c:choose>`. Bắt cứng trường hợp task.status == 'Waiting for dispatch' để in ra thẻ `<span class="order-badge order-badge-dispatch">` gọi đúng key chuẩn `<fmt:message key="order.status.waiting_dispatch" />` (đồng bộ hoàn toàn với trang delivery_orders_table.jsp). Các trạng thái khác đưa vào `<c:otherwise>`.

- [x] **Task 7.4: Gỡ bỏ chuỗi Hardcode, tuân thủ Constants.java (Hoàn tất phần DispatchController)** **[DONE]**
  - Vị trí 1: DispatchController.java đang hardcode chuỗi "dispatch.msg.assign_success" và "error.unauthorized".
  - Vị trí 2: IngredientAlertController.java đang hardcode các chuỗi lỗi JSON "E0004", "E9000", "DE0020".
  - Hành động: Bổ sung các chuỗi này thành biến `public static final String` tại file utility/Constants.java với comment phân vùng rõ ràng. Sau đó cập nhật lại các Controller để gọi biến hằng số.

- [x] **Task 7.7: Chuẩn hóa toàn bộ hệ thống mã thông báo & mã lỗi phân hệ Shipper/Delivery theo chuẩn `Rule_BOMS.md`** **[DONE]**
  - Vấn đề: `DeliveryService.java` và `DispatchController.java` đang dùng các chuỗi văn bản tự do dạng key (`error.delivery.*`, `dispatch.msg.*`), vi phạm quy chuẩn mã thông báo backend `EXXX` và `SXXX` trong `Rule_BOMS.md`.
  - Hành động 1: Khai báo dải mã thành công mới `S060X` trong `Constants.java`: ví dụ `SUCCESS_ASSIGN_SHIPPER = "S0601"`.
  - Hành động 2: Khai báo dải mã lỗi nghiệp vụ mới `E060X` trong `Constants.java` thay thế cho 7 chuỗi `error.delivery.*` (`E0601` đến `E0607`). Chuẩn hóa lỗi phân quyền về `E0004` (`ERROR_LOGIN_FAIL_ROLE`).
  - Hành động 3: Khai báo nội dung đa ngôn ngữ tương ứng của các mã `S060X` và `E060X` trong `messages_vi.properties` (bắt buộc mã hóa Unicode Escape `\uXXXX`) và `messages_en.properties`.
  - Hành động 4: Cập nhật đồng bộ trong `DeliveryService.java`, `DispatchController.java`, `ShipperController.java` để ném và hứng mã hằng số chuẩn từ `Constants.java`.

### MỨC ƯU TIÊN 3: LOẠI BỎ TRÙNG LẶP (DRY) VÀ NỢ KỸ THUẬT (MEDIUM)
- [x] **Task 7.5: Gom hàm Flash Messages đang bị copy-paste 6 lần** **[DONE]**
  - Vị trí vi phạm: Cả 6 file AttendanceController, DispatchController, InventoryManagementController, ReportController, ReportManagementController, MenuController đều đang tự định nghĩa một hàm private void forwardFlashMessages(...).
  - Hành động 1: Tạo một class tiện ích mới src/java/utility/FlashMessageUtil.java.
  - Hành động 2: Cắt nội dung hàm lặp này đưa vào class Utility dưới dạng `public static void forwardFlashMessages(HttpServletRequest request, HttpSession session)`.
  - Hành động 3: Xóa bỏ hàm private dư thừa ở 6 Controller và cập nhật lời gọi hàm ở đầu các phương thức doGet/doPost trỏ về FlashMessageUtil.forwardFlashMessages(...).

- [x] **Task 7.6: Tái cấu trúc logic tạo chuỗi JSON (Chống Bad Practice)** **[DONE]**
  - Vị trí: IngredientAlertController.java (hàm buildAlertsJson và escapeJson).
  - Hành động: Thông báo cho người dùng biết cần tải thư viện Gson (gson-2.10.1.jar) vào thư mục WEB-INF/lib/ (nếu người dùng đồng ý). Sau đó, xóa bỏ hoàn toàn 2 hàm nối chuỗi JSON thủ công rườm rà, thay bằng cách khởi tạo `Gson gson = new Gson();` và dùng `gson.toJson(alerts)` để trả về Response. (Nếu người dùng chưa muốn thêm thư viện, bỏ qua Task này).

## 🛵 TASK 8: HOÀN THIỆN ĐA NGÔN NGỮ, TỐI ƯU GIAO DIỆN & TIÊU DIỆT NỢ KỸ THUẬT PHÂN HỆ SHIPPER / DELIVERY
**Actor:** Điều phối viên (Floor & Counter Staff / Store Managers), Shipper & Developer
**Mô tả:** Tiếp tục củng cố và hoàn thiện toàn diện phân hệ Giao hàng (Shipper & Dispatch) sau Task 7. Xử lý triệt để cấu hình Locale đa ngôn ngữ JSTL động 3 tầng, chuẩn hóa hiển thị giao diện, xóa bỏ inline CSS, và loại bỏ toàn bộ các nợ kỹ thuật tồn đọng ở tầng Backend (triệt tiêu chuỗi/số hardcode, đóng an toàn tài nguyên JDBC với `try-with-resources`, áp dụng tiện ích dùng chung `FlashMessageUtil`) tuân thủ nghiêm ngặt 100% nguyên tắc `Rule_BOMS.md`.

### 🟢 MỨC ƯU TIÊN 1: ĐA NGÔN NGỮ & GIAO DIỆN (UI / VIEW) - [ĐÃ HOÀN THÀNH]
- [x] **Task 8.1: Bổ sung cấu hình JSTL Locale động 3 tầng cho các trang và Modal luồng Shipper** **[DONE]**
  - **Vấn đề:** Các trang và modal JSP của luồng giao hàng không nhận diện được biến đổi ngôn ngữ khi người dùng chuyển đổi ngôn ngữ/cờ. Lệnh `<jsp:include>` tạo `PageContext` độc lập khiến các modal con mất cấu hình Locale nếu không được khai báo riêng.
  - **Hành động:**
    + Thiết lập cấu hình JSTL Locale động 3 tầng ưu tiên (`Session` -> `Cookie` -> Mặc định `'vi'`):
      `<c:set var="currentLang" value="${not empty sessionScope.LANG ? sessionScope.LANG : (not empty cookie.LANG.value ? cookie.LANG.value : 'vi')}" />`
      `<fmt:setLocale value="${currentLang}" />`
      ngay phía trên dòng `<fmt:setBundle basename="resources.messages" />` tại các file:
      * `web/dispatch_dashboard.jsp`
      * `web/shipper_dashboard.jsp`
      * `web/delivery_unauthorized.jsp`
      * `web/components/shipper_complete_modal.jsp`
      * `web/components/shipper_fail_modal.jsp`
    + Đảm bảo cơ chế hiển thị đa ngôn ngữ i18n chính xác và đồng bộ 100% toàn hệ thống.

- [x] **Task 8.2: Chuẩn hóa nhãn hiển thị và loại bỏ CSS inline trên `dispatch_dashboard.jsp`** **[DONE]**
  - **Vấn đề:** Vi phạm `Rule_BOMS.md` về việc sử dụng inline CSS (`style="margin: 30px 0;"` trên thẻ `<hr class="section-divider" />`) và hardcode chuỗi tiếng Việt `"đơn"` cạnh chỉ số gom đơn của Shipper.
  - **Hành động:**
    + Xóa bỏ hoàn toàn thuộc tính inline CSS trên thẻ `<hr class="section-divider" />` và tận dụng class CSS chung `section-divider`.
    + Thay thế chuỗi hardcode `"đơn"` bằng thẻ đa ngôn ngữ `<fmt:message key="dispatch.label.orders" />`.
    + Bổ sung key `dispatch.label.orders` vào `messages_vi.properties` (chuẩn mã hóa Unicode Escape `\uXXXX`) và `messages_en.properties`.

- [x] **Task 8.3: Chuẩn hóa mã lỗi phân quyền trang `delivery_unauthorized.jsp`** **[DONE]**
  - **Vấn đề:** Trang `delivery_unauthorized.jsp` hardcode fallback string `"error.unauthorized"`, vi phạm quy chuẩn định danh mã lỗi thống nhất toàn hệ thống.
  - **Hành động:**
    + Thay thế chuỗi hardcode `"error.unauthorized"` bằng mã lỗi phân quyền chuẩn toàn hệ thống `E0004` (`Constants.ERROR_LOGIN_FAIL_ROLE`).

- [x] **Task 8.4: Đa ngôn ngữ hóa 100% Badge trạng thái đơn Delivery trên `delivery_orders_table.jsp`** **[DONE]**
  - **Vấn đề:** Trong bảng đơn hàng `web/components/delivery_orders_table.jsp`, các badge trạng thái đang hiển thị chuỗi tiếng Anh tĩnh (`Waiting for delivery`, `Picked up`, `Delivering`, `Delivered`, `Returned`, `Paid`, `Cancelled`), gây lệch ngôn ngữ khi người dùng chọn tiếng Việt.
  - **Hành động:**
    + Chuyển đổi toàn bộ nội dung hiển thị text cứng trong các thẻ `span.order-badge` thành cú pháp động `<fmt:message key="status.${order.status}" />`.
    + Khai báo bổ sung các key tương ứng còn thiếu trong `messages_vi.properties` (Unicode Escape `\uXXXX`) và `messages_en.properties` (`status.Waiting\ for\ dispatch`, `status.Picked\ up`, `status.Paid`).

### 🟡 MỨC ƯU TIÊN 2: TIÊU DIỆT HARDCODE VÀ NỢ KỸ THUẬT BACKEND (CẦN LÀM)
- [x] **Task 8.5: Triệt tiêu toàn bộ chuỗi & số Magic Hardcode trong `DeliveryService.java`** **[DONE]**
  - **Vấn đề tồn đọng:**
    + Hardcode chuỗi tiếng Việt không dấu trong nhật ký lịch sử (`DeliveryStatusHistory`): `"Dieu phoi gan shipper"`, `"Shipper chu dong tiep nhan don"`, `"Shipper xac nhan nhan don"`, `"Shipper tu choi nhan don"`.
    + Hardcode các chuỗi trạng thái hoạt động của Shipper: `"READY"`, `"DELIVERING"`, `"Ready"`, `"Delivering"`, và chuỗi kiểm tra vai trò `"Shipper"`.
    + Hardcode magic number `3` làm giới hạn số đơn gom tối đa khi ShipperProfile chưa có cấu hình (`maxOrders = 3`).
    + Tồn tại điều kiện kiểm tra logic dư thừa: `"Returned".equalsIgnoreCase(newStatus)` tại dòng 102 (đã được bao hàm bởi `Constants.STATUS_FAILED`).
  - **Hành động dự kiến:**
    + Bổ sung vào `Constants.java`:
      * Hằng số ghi chú nhật ký: `NOTE_DISPATCH_ASSIGN`, `NOTE_SHIPPER_CLAIM`, `NOTE_SHIPPER_ACCEPT`, `NOTE_SHIPPER_REJECT`.
      * Hằng số trạng thái Shipper & phân quyền: `ROLE_SHIPPER = "Shipper"`, `SHIPPER_STATUS_AVAILABLE = "Ready"`, `SHIPPER_STATUS_BUSY = "Delivering"`, `SHIPPER_WORK_READY = "READY"`, `SHIPPER_WORK_DELIVERING = "DELIVERING"`.
      * Hằng số giới hạn gom đơn mặc định: `DEFAULT_SHIPPER_MAX_ORDERS = 3`.
    + Tái cấu trúc `DeliveryService.java` sử dụng toàn bộ các hằng số chuẩn từ `Constants.java`, triệt tiêu 100% chuỗi/số hardcode và dọn sạch điều kiện dư thừa.

- [x] **Task 8.6: Áp dụng `FlashMessageUtil` và thắt chặt routing action trong `ShipperController.java`** **[DONE]**
  - **Vấn đề tồn đọng:**
    + `ShipperController.java` vẫn còn giữ một hàm private `forwardFlashMessages(...)` tự viết (dài 13 dòng), vi phạm nguyên tắc DRY (Don't Repeat Yourself) và chưa áp dụng tiện ích dùng chung đã tạo ở Task 7.5.
    + Nhánh `else` trong phương thức `doPost` đang mặc định gọi `processUpdateStatus(...)`, gây rủi ro bảo mật và sai luồng nghiệp vụ khi nhận một `action` bất thường hoặc chuỗi rỗng.
  - **Hành động dự kiến:**
    + Xóa bỏ hàm private `forwardFlashMessages(...)` trong `ShipperController.java`, thay thế lời gọi tại `doGet` bằng `FlashMessageUtil.forwardFlashMessages(request, session)`.
    + Thắt chặt cấu trúc rẽ nhánh `doPost`: Bắt rõ `else if ("updateStatus".equals(action))`, trường hợp `else` (không khớp action hợp lệ) sẽ ghi log cảnh báo hoặc redirect an toàn về dashboard, tránh gọi nhầm nghiệp vụ cập nhật trạng thái.

- [x] **Task 8.7: Chuẩn hóa mã lỗi và đóng an toàn tài nguyên JDBC (try-with-resources) trong `DeliveryDAO.java`** **[DONE]**
  - **Vấn đề tồn đọng:**
    + `DeliveryDAO.java` đang hardcode chuỗi mã lỗi CSDL `"DE0016"` và `"DE0017"` tại 7 vị trí khác nhau thay vì dùng các hằng số chuẩn `Constants.ERROR_DATABASE_GET_ORDER` và `Constants.ERROR_DATABASE_UPDATE_ORDER`.
    + Các phương thức `getAllTasks()`, `getTasksByShipper()`, `getAvailableTasksForShipper()`, `getTaskById()`, `updateTaskStatus()`, `updateTaskShipper()` đang mở `PreparedStatement` và `ResultSet` nhưng không bọc trong cú pháp `try-with-resources`, tiềm ẩn rủi ro rò rỉ kết nối (Connection Leak) khi xảy ra ngoại lệ JDBC.
  - **Hành động dự kiến:**
    + Thay thế toàn bộ 7 chuỗi hardcode `"DE0016"` và `"DE0017"` bằng hằng số chuẩn tương ứng từ `Constants.java`.
    + Tái cấu trúc toàn bộ các khối lệnh JDBC trong các phương thức trên sang cú pháp `try-with-resources` (`try (PreparedStatement st = ...; ResultSet rs = ...)`) theo đúng quy chuẩn `Rule_BOMS.md`.