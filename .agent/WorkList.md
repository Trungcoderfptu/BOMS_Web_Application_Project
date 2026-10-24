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
- [x] ~~**Task 2.4: Tích hợp Cổng thanh toán (Bước 6, 7):** Xây dựng `PaymentController` điều hướng giỏ hàng sang bên thứ 3 (VNPay/MoMo) khi khách bấm nút "Thanh toán Online".~~ **[HỦY / OUT OF SCOPE - MẶC ĐỊNH THANH TOÁN TIỀN MẶT THEO ĐẶC TẢ DỰ ÁN]**
- [x] ~~**Task 2.5: Xử lý ngoại lệ thanh toán (Exception 2.0.E2):** Hứng kết quả trả về từ Cổng thanh toán.~~ **[HỦY / OUT OF SCOPE - MẶC ĐỊNH THANH TOÁN TIỀN MẶT THEO ĐẶC TẢ DỰ ÁN]**
  - *~~Nếu lỗi/hủy: Trả về UI báo lỗi, giữ nguyên Session giỏ hàng.~~*
  - *~~Nếu thành công: Gọi logic chốt đơn (Task 1.4) và lưu DB với trạng thái Đã thanh toán / Chờ xác nhận.~~*
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

    ## 🧹 TASK 9: TỔNG VỆ SINH & QUY HOẠCH MÃ LỖI (Refactor Code & Properties)
**Actor:** System / Developer
**Mô tả:** Dọn dẹp các biến Hardcode trong Controller, quy hoạch lại hệ thống mã lỗi và đồng bộ cấu trúc file ngôn ngữ theo đúng chuẩn `Rule_BOMS.md`. Đảm bảo không làm gãy (crash) bất kỳ luồng hoạt động nào hiện tại.

- [x] **Task 9.1: Triệt tiêu Hardcode String trong Controller:** **[DONE]**
  - Quét toàn diện các Controller (`CartController.java`, `LanguageController.java`, `MenuController.java`, `ReportController.java`, `LoginController.java`). Gom toàn bộ các chuỗi session/attribute gõ chay (`"CURRENT_CART"`, `"CURRENT_TABLE"`, `"LAST_ORDER_ID"`, `"PLACED_ORDER_IDS"`, `"LANG"`, `"LAST_TICKET_ID"`) vào `Constants.java`.
  - Triệt tiêu 100% rủi ro gõ sai tên Session attribute và chống rò rỉ trạng thái.
- [x] **Task 9.2: Chuẩn hóa dải mã lỗi trong `Constants.java`:** **[DONE]**
  - Phân định rõ ràng quyền sở hữu dải mã thông báo thành công `SXXX`, mã lỗi nghiệp vụ `EXXX`, mã lỗi CSDL `DEXXX`.
  - Khắc phục triệt để các xung đột mã: chuyển Điểm danh / Chấm công từ `S0301`/`S0302` sang `S0708`/`S0709`, dọn dẹp trùng `E0301`-`E0306` sang `E0716`-`E0721`, chuyển mã lỗi DB chấm công sang `DE0056`-`DE0058`. Trả lại phân vùng `S030X`, `E030X`, `DE0030`-`DE0033` độc quyền cho Báo cáo ẩn danh; giữ `DE0018`-`DE0020` cho Cảnh báo kho nguyên liệu; tách `E9006` cho Tab cài đặt.
- [x] **Task 9.3: Đồng bộ & dọn dẹp `messages_vi.properties` và `messages_en.properties`:** **[DONE]**
  - Quy hoạch toàn bộ 207 mã backend (`SXXX`, `EXXX`, `DEXXX`) lên khối đầu trang theo đúng thứ tự logic trong `Constants.java`.
  - Loại bỏ sạch sẽ toàn bộ các khối UI và mã lỗi copy-paste trùng lặp. Cả 2 file `messages_vi.properties` và `messages_en.properties` đồng bộ chính xác 100% với 885 key duy nhất (0 duplicate keys).
  - Tuân thủ nghiêm ngặt 100% chuẩn mã hóa Unicode Escape (`\uXXXX`) cho tiếng Việt, không để lọt ký tự Unicode thô.
- [x] **Task 9.4: Hotfix khẩn cấp - Khắc phục lệch gán mã lỗi & khôi phục 41 keys Admin:** **[DONE]**
  - Khôi phục đầy đủ 41 keys giao diện Quản trị viên (`admin.dashboard.*`) đang bị thiếu trong cả 2 file `.properties` (mã hóa chuẩn Unicode Escape `\uXXXX` cho tiếng Việt).
  - Đối chiếu trực tiếp từng biến hằng số trong `Constants.java` và sửa lại chính xác 100% nội dung thông báo của toàn bộ các mã `SXXX`, `EXXX`, `DEXXX` bị hoán đổi dòng.
  - Xóa ký tự thừa `|` ở `DE0030`-`DE0033`, sửa lỗi mũi tên điều hướng ở `hr.matrix.prev_week` / `next_week`.
  - Điều chỉnh `minlength="3"` tại `web/admin_dashboard.jsp` khớp với `Constants.MIN_PASSWORD_LENG = 3`.
  - Cả 2 file đạt 930 keys đối xứng 1-1, 0 duplicate keys, 0 non-ASCII lines trong `messages_vi.properties`. Compile Ant đạt `BUILD SUCCESSFUL`.

  ## 👥 TASK 10: QUẢN LÝ NHÂN SỰ, CA LÀM VIỆC & DUYỆT CHẤM CÔNG (HR & Shift Management)
**Actor:** Quản lý cửa hàng (Store Managers) & Quản trị viên (Admin)
**Mô tả:** Loại bỏ cơ chế tự gán ca Demo phía nhân viên; xây dựng phân hệ cho phép Quản lý tạo ca mới, chỉnh sửa khung giờ ca (`Shifts`), phân ca làm việc cho nhân viên (`ShiftAssignments`), theo dõi trạng thái điểm danh tổng thể, phê duyệt OT / vào ca sớm (`ApprovedBy`) và xem bảng tổng kết công (`Timesheet Summary`).

- [x] **Task 10.1: Gỡ bỏ cơ chế tự gán/reset ca Demo ở trang Nhân viên (`attendance.jsp` & `AttendanceController`):** **[DONE]**
  - Xóa bỏ form `assignDemoShift` (nút "Gán ca mẫu") và form `resetDemoShift` (nút "Đổi / Chọn lại ca khác") trong `web/attendance.jsp`. Khi chưa được Manager phân ca, màn hình chỉ hiển thị thông báo `attendance.status.no_shift`.
  - Xóa bỏ 2 nhánh xử lý `assignDemoShift`, `resetDemoShift` trong `AttendanceController.java` và các hàm demo tương ứng trong `AttendanceService.java` / `AttendanceDAO.java`.

- [x] **Task 10.2: Mở rộng tầng DAO (`AttendanceDAO.java`) cho Quản lý Ca, Phân ca & Duyệt công:** **[DONE]**
  - **Quản lý khung giờ ca (`Shifts`):** Bổ sung các hàm `insertShift(Shift)`, `updateShift(Shift)`, `getShiftById(int)` và `isShiftNameExists(String, int)` để hỗ trợ tạo ca mới và chỉnh sửa giờ `StartTime`, `EndTime`.
  - **Quản lý phân ca (`ShiftAssignments`):** Bổ sung hàm gán ca cho nhân viên theo ngày (`insertShiftAssignment`), đổi ca/xóa phân ca khi nhân viên chưa Check-in (`deleteShiftAssignment`), kiểm tra trùng phân ca trong ngày và lấy danh sách phân ca kèm trạng thái điểm danh của toàn bộ nhân viên theo ngày/tháng.
  - **Duyệt OT / Vào ca sớm & Thống kê (`Attendances`):** Bổ sung hàm `updateAttendanceApproval(int attendanceId, String checkInStatus, String checkOutStatus, int approvedBy)` để cập nhật cột `ApprovedBy` và trạng thái sau khi Manager duyệt/từ chối; bổ sung hàm truy vấn dữ liệu tổng hợp công tháng của toàn bộ nhân viên.
  - Bắt buộc bọc `try-catch (SQLException)` và ném `ValidationException` kèm mã lỗi `DEXXX` trong `Constants.java`.

- [x] **Task 10.3: Xây dựng tầng Nghiệp vụ & Validation (`AttendanceService.java`, `Validator.java`, `Constants.java`):** **[DONE]**
  - Bổ sung Validation tầng 1 (kiểm tra tên ca không rỗng, định dạng giờ `HH:mm` hợp lệ, giờ kết thúc phải sau giờ bắt đầu) và Validation tầng 2 (kiểm tra quyền Manager/Admin, kiểm tra trùng tên ca, không cho xóa phân ca đã chấm công).
  - Xây dựng các hàm nghiệp vụ tạo/sửa ca, gán/xóa phân ca, xử lý Duyệt hoặc Từ chối đối với các bản ghi có trạng thái `Pending OT Approval` (UC-06 6.1) và `Pending Early Approval` (UC-05 5.1).
  - Đóng gói toàn bộ dữ liệu danh sách ca, lịch phân ca, danh sách chờ duyệt OT và bảng tổng hợp công toàn cửa hàng sang cấu trúc `Map<String, Object>` / `List<Map<String, Object>>` (tuyệt đối không để lọt Model lên Controller).

- [x] **Task 10.4: Xây dựng tầng Điều phối (`controller/hr/HrManagementController.java`):** **[DONE]**
  - Tạo Servlet điều phối trang Quản lý nhân sự & ca làm việc, kiểm tra chặt chẽ quyền truy cập trong `Session` (chỉ cho phép `Manager` và `Admin`).
  - `doGet`: Dùng `FlashMessageUtil.forwardFlashMessages` xử lý thông báo, đọc tham số bộ lọc (ngày, tháng, năm, nhân viên, tab đang mở) và đẩy các `Map` dữ liệu từ `AttendanceService` sang View.
  - `doPost`: Điều phối các `action` (`createShift`, `updateShift`, `assignShift`, `removeAssignment`, `approveOt`, `rejectOt`, `approveEarly`), lưu mã kết quả `SXXX` / `EXXX` vào `Session` và điều hướng bằng `sendRedirect` (chuẩn PRG).

- [x] **Task 10.5: Thiết kế Giao diện Quản lý Nhân sự & Ca làm (`web/hr_management.jsp` & `web/components/`):** **[DONE]**
  - Tạo trang `web/hr_management.jsp` (liên kết với mục `header.sidebar.hr` trên thanh menu) gồm 3 Tab dùng chung hàm `openTab`:
    1. **Tab Phân Ca & Khung Giờ (`tab-shifts`):** Bảng danh sách khung giờ ca (có nút Thêm ca / Sửa giờ ca mở Modal) và khu vực Phân ca cho nhân viên theo ngày.
    2. **Tab Theo Dõi Điểm Danh & Duyệt OT (`tab-tracking`):** Bảng theo dõi trạng thái vào/tan ca của toàn bộ nhân viên, hiển thị lý do OT (`otReason`) và cung cấp nút **[Duyệt OT]** / **[Từ chối]** ngay trên các dòng có trạng thái chờ duyệt.
    3. **Tab Tổng Hợp Công Tháng (`tab-summary`):** Bảng thống kê tổng số ca, tổng giờ làm, số lần đúng giờ/đi muộn/OT của từng nhân viên trong tháng.
  - Tách các Modal tạo/sửa ca và phân ca vào `web/components/hr_shift_modal.jsp`. Toàn bộ CSS viết trong `web/css/global.css`, JS viết trong `web/js/main.js`, và đa ngôn ngữ viết dưới dạng Unicode Escape (`\uXXXX`) trong `messages_vi.properties` & `messages_en.properties`.
- [x] **Task 10.6: Valid vào và ra ca chỉ có thể điểm danh khi đã vào giờ làm việc và tan ca khi đã hết giờ làm việc:** **[DONE]**
  - Chặn chấm công khi chưa tới giờ (khóa nút và từ chối nếu `currentTime < StartTime - 15m`). Vào ca trong khoảng 15 phút trước giờ bắt đầu được ghi nhận `Pending Early Approval` và cần Manager duyệt mới tính công.
  - Khóa nút tan ca khi chưa hết giờ làm việc (`currentTime < EndTime`). Sau ca làm việc > 15 phút yêu cầu nhập lý do OT và lưu trạng thái `Pending OT Approval`.
  - Quản lý duyệt ca (`ApprovedBy`) thì ca mới được ghi nhận vào bảng tổng kết công tháng (`Timesheet Summary`).
  ## 🔔 TASK 11: HỆ THỐNG THÔNG BÁO & PHÁT THANH TOÀN CỤC (Notifications & Broadcast)
**Actor:** Tất cả người dùng
**Mô tả:** Xây dựng hệ thống gửi thông báo tự động từ các tác vụ nghiệp vụ (Trigger) và gửi thông báo khẩn cấp toàn cục (Broadcast) từ cấp Quản lý.

- [x] **Task 11.1: Mở rộng `NotificationDAO` & Xây dựng `NotificationService`:** **[DONE]**
  - Bổ sung hàm truy vấn danh sách thông báo chưa đọc (Unread) của user, hàm đánh dấu "Đã đọc", đếm số thông báo chưa đọc. 
  - Xây dựng hàm nghiệp vụ `broadcastMessage` (Lấy danh sách tất cả User đang Active từ `UserDAO` và vòng lặp gọi `insertNotification` cho từng người).
- [x] **Task 11.2: Gắn Trigger tự động (Auto-Notification):** **[DONE]**
  - Tích hợp gọi `NotificationService` vào các Service khác: Bắn thông báo khi Manager phân ca mới cho Staff (`ShiftAssignment`), khi Manager duyệt/từ chối OT/vào sớm (`AttendanceApproval`).
- [x] **Task 11.3: Giao diện Quả Chuông (Header Notification):** **[DONE]**
  - Thêm icon Quả Chuông 🔔 trên `header.jsp` có gắn Badge đỏ hiển thị số lượng chưa đọc. 
  - Bấm vào sẽ xổ ra Dropdown list xem nhanh các thông báo. Tích hợp nút đánh dấu "Đã đọc" và "Đánh dấu tất cả đã đọc" qua AJAX.
- [x] **Task 11.4: Chức năng Phát loa toàn cục (Broadcast):** **[DONE]**
  - Thêm icon Megaphone 📢 trên Header (chỉ hiển thị với Admin/Manager). Bấm vào mở Modal nhập tin nhắn. 
  - Viết `NotificationController` hứng tin nhắn và gọi hàm `broadcastMessage` để rải thông báo đến toàn bộ nhân sự, tuân thủ PRG và hiển thị Flash message.

## 🎛️ TASK 12: BẢNG ĐIỀU KHIỂN TRUNG TÂM (Central Dashboard - `index.jsp`)
**Actor:** Tất cả người dùng
**Mô tả:** Cải tạo trang chủ `index.jsp` trống trải thành Dashboard động cá nhân hóa theo từng vai trò (Role-based), đóng vai trò như trung tâm điều khiển (Control Center) của hệ thống.

- [x] **Task 12.1: Xây dựng `DashboardController`:** **[DONE]**
  - Tạo Controller mới để fetch toàn bộ dữ liệu cần thiết trước khi load trang chủ: Thông tin cá nhân cơ bản, trạng thái ca làm việc hiện tại (từ `AttendanceService`), và danh sách thông báo mới nhất.
- [x] **Task 12.2: Thiết kế giao diện `index.jsp` cá nhân hóa:** **[DONE]**
  - **Khu vực 1 (Profile Card):** Hiển thị Avatar, "Xin chào Họ Tên", Role, kèm huy hiệu trạng thái làm việc (Vd: 🟢 Đang trong ca / 🔴 Chưa vào ca / ⚪ Đã hoàn ca).
  - **Khu vực 2 (Quick Actions Grid):** Lưới nút thao tác nhanh. 
    + Nếu là Staff: Nút [Mở POS Bán Hàng], [Chấm công], [Quản lý Đơn hàng], [Xem kho], [Báo cáo ẩn danh]. 
    + Nếu là Manager: Nút [Quản lý Đơn hàng], [Quản lý Kho], [Phân ca nhân sự], [Quản lý thực đơn], [Điều phối shipper], [Báo cáo doanh thu], [Phát loa toàn cục].
    + Nếu là Admin: Nút [Quản trị người dùng], [Phát loa toàn cục], [Cài đặt hệ thống], [Hồ sơ cá nhân].
    + Nếu là Shipper: Nút [Bảng nhiệm vụ giao hàng], [Cài đặt cá nhân].
  - **Khu vực 3 (Notification Feed):** Bảng tin dạng cuộn hiển thị 8 thông báo mới nhất một cách trực quan ngay trên trang chủ, kèm nút đánh dấu đã đọc đồng bộ real-time.

  ## 🐞 TASK 13: FIX BUG GIAO DIỆN & ĐỒNG BỘ THÔNG BÁO (UI/UX Hotfix)
**Actor:** System / Developer / AI Agent
**Mô tả:** Khắc phục các lỗi về hiển thị đa ngôn ngữ (chữ cứng) trên Dashboard và xử lý đồng bộ trạng thái giao diện (DOM Update) cho chức năng Thông báo bằng AJAX mà không cần tải lại trang. Cấm làm ảnh hưởng đến cấu trúc Controller hiện tại.

- [x] **Task 13.1: Triệt tiêu chữ cứng tiếng Việt trên `index.jsp` (Đa ngôn ngữ):** **[DONE]**
  - Quét file `web/index.jsp` tại khu vực render các nút thao tác của Admin và Shipper (Khu vực `Quick Actions Grid`).
  - Tìm và thay thế các chuỗi mô tả hardcode (như *"Cấu hình bảo mật và mật khẩu"*, *"Xem và cập nhật thông tin cá nhân"*, *"Cập nhật hồ sơ và mật khẩu"*) bằng cú pháp JSTL `<fmt:message key="..." />`.
  - Khai báo các Key tương ứng vào file `messages_vi.properties` (**Bắt buộc** mã hóa Unicode Escape `\uXXXX`) và `messages_en.properties`.
- [x] **Task 13.2: Đồng bộ UI thời gian thực cho Thông báo (`js/main.js`):** **[DONE]**
  - Viết lại hàm `markNotificationAsRead(id)` và `markAllNotificationsAsRead()` trong `web/js/main.js` để xử lý AJAX triệt để.
  - Khi API trả về `success: true`, Javascript thực hiện ngay 3 việc mà KHÔNG CẦN F5 trang:
    1. Cập nhật con số hiển thị trên Badge của icon Quả Chuông 🔔 (ẩn Badge nếu số = 0).
    2. Nếu đang ở trang chủ (`index.jsp`): Tìm `<li>` thông báo tương ứng trên Notification Feed, xóa class `unread` và thẻ `<span class="notification-unread-dot">`.
    3. Tìm và xóa hẳn thông báo đó khỏi danh sách DOM `<ul id="notificationItemsList">` bên trong Dropdown của thanh Header (để dropdown luôn chỉ chứa các thư chưa đọc).
- [x] **Task 13.3: Khắc phục triệt để lỗi hiển thị `???key???` tại phân hệ Quản lý Nhân sự & Điểm danh (`hr_management.jsp`):** **[DONE]**
  - Bổ sung 8 keys i18n bị thiếu vào `messages_vi.properties` (chuẩn mã hóa `\uXXXX`) và `messages_en.properties`:
    + `hr.btn.approve_ot`, `hr.btn.reject_ot` (Tooltip nút duyệt/từ chối OT).
    + `attendance.badge.ontime`, `attendance.badge.late`, `attendance.badge.left_early`, `attendance.badge.pending_ot` (Huy hiệu trạng thái điểm danh).
    + `attendance.filter.month`, `attendance.filter.year` (Nhãn bộ lọc tháng/năm ở tab tổng hợp công).

## 📅 TASK 14: NÂNG CẤP TRẢI NGHIỆM QUẢN LÝ NHÂN SỰ & THỜI KHÓA BIỂU (Advanced Timetable)
**Actor:** Quản lý cửa hàng (Store Managers) & Nhân viên (Staff)
**Mô tả:** Nâng cấp phân hệ phân ca và điểm danh. Bổ sung tính năng phân ca hàng loạt (Bulk Assignment) và áp dụng thuật toán ma trận (Matrix/Pivot Data) để hiển thị thời khóa biểu dạng lưới chuyên nghiệp cho cả nhân viên (giống FAP) và giao diện tổng quan cho quản lý. Không yêu cầu tạo thêm bảng mới trong CSDL.

- [x] **Task 14.1: Xây dựng logic Phân ca liên hoàn (Bulk Shift Assignment):** **[DONE]**
  - Cải tạo Modal phân ca trên `hr_management.jsp`. Thay vì chọn 1 ngày, cho phép chọn khoảng thời gian (`startDate`, `endDate`) và các checkbox ngày trong tuần (`Thứ 2`, `Thứ 3`... `Chủ Nhật`).
  - Tại `AttendanceService`, viết hàm xử lý vòng lặp ngày (dùng `LocalDate`). Duyệt từ `startDate` đến `endDate`, nếu ngày rơi vào thứ đã chọn trong checkbox và nhân viên đó chưa có ca, thực hiện gọi DAO gán ca tự động.
- [x] **Task 14.2: Giao diện Thời khóa biểu tuần cho Nhân viên (Staff Weekly Timetable):** **[DONE]**
  - **Xử lý Backend (Pivot Data):** Viết hàm `getStaffWeeklyTimetable` trong `AttendanceService` (nhận tham số `userId` và mốc ngày của Tuần). Nhóm dữ liệu trả về theo cấu trúc `Map<Ca Làm, Map<Ngày, Thông tin Ca>>`.
  - **Hiển thị View (`attendance.jsp`):** Thay thế danh sách chấm công cũ bằng Bảng lưới Thời khóa biểu. Hàng ngang là `Slot/Ca làm`, Cột dọc là `Thứ 2 - Chủ Nhật`. Ô nào có ca in nền xanh kèm nút Chấm công, ô nào trống in gạch ngang `-`. Bổ sung nút chuyển đổi "Tuần trước / Tuần sau / Tuần này".
- [x] **Task 14.3: Bảng Ma trận Phân ca cho Quản lý (Manager Matrix View):** **[DONE]**
  - **Xử lý Backend:** Viết hàm `getManagerWeeklyMatrix` trong `AttendanceService`. Nhóm dữ liệu trả về theo cấu trúc `Map<Nhân viên, Map<Ngày, Danh sách Ca>>`.
  - **Hiển thị View (`hr_management.jsp`):** Cải tạo Tab Phân Ca thành Bảng Ma trận. Hàng ngang là danh sách Nhân viên, Cột dọc là `Thứ 2 - Chủ Nhật`. Tại giao điểm của ô, hiển thị Badge tên ca được gán.
  - **Thao tác nhanh (Quick Action):** Nếu ô giao điểm chưa có ca, biến ô đó thành nút bấm `[+]`. Khi Manager bấm vào, lập tức bật Modal gán ca điền sẵn Tên Nhân viên và Ngày đó.

## 🎯 TASK 15: Chặt Chẽ Phân Quyền Phân Ca Quản Lý (Manager Restriction) & Tải Ảnh Hồ Sơ Cá Nhân (Profile Avatar Upload)
**Actor:** Tất cả người dùng (All Roles) & Quản lý (Manager)
**Mô tả:** Khắc phục triệt để logic phân ca nhân sự để Manager không thể phân ca cho chính mình hoặc cho Manager khác (chỉ phân ca cho Staff). Hoàn thiện trọn vẹn luồng tải ảnh đại diện và xóa ảnh hồ sơ cá nhân qua tiện ích dùng chung `ImageUploadUtility`.

- [x] **Task 15.1: Chặn Manager phân ca cho Manager (Only Staff is Assignable):** **[DONE]**
  - **Tầng DAO (`AttendanceDAO.java`):** Sửa truy vấn trong `getActiveStaffUsers()` thành `WHERE Role = 'Staff' AND IsActive = 1`, loại bỏ hoàn toàn Manager khỏi danh sách nhân viên phân ca và ma trận tuần.
  - **Tầng Service (`AttendanceService.java`):** Bổ sung hàm kiểm tra `validateStaffRole(userId)` trong cả `assignShift` và `bulkAssignShift`. Ném `ValidationException(Constants.ERROR_CANNOT_ASSIGN_NON_STAFF)` (mã lỗi `E0715`) nếu tài khoản được gán không phải là Staff.
  - **Đa ngôn ngữ:** Khai báo mã `E0715` chuẩn Unicode Escape trong `messages_vi.properties` và `messages_en.properties`.

- [x] **Task 15.2: Hoàn thiện tính năng Tải ảnh & Quản lý Avatar Cá nhân (`profile.jsp`):** **[DONE]**
  - **Cơ sở dữ liệu (`BOMS_DATABASE.sql`):** Bổ sung cột `Avatar VARCHAR(255) NULL` vào bảng `Users` kèm đoạn mã kiểm tra an toàn `IF NOT EXISTS ... ALTER TABLE Users ADD Avatar`.
  - **Tầng Model & DAO (`User.java` & `UserDAO.java`):** Bổ sung thuộc tính `avatar`, getter/setter, an toàn trích xuất `Avatar` trong `getAllUsers()`, `getUserByUsername()`, `getUserById()` và phương thức cập nhật `updateAvatar(int userId, String avatarUrl)`.
  - **Tầng Tiện ích & Nghiệp vụ (`UserService.java` & `ImageUploadUtility.java`):** Tái sử dụng `ImageUploadUtility.saveImage(part, "avatars")` và `deleteImage(oldAvatar)`. Bổ sung `avatar` vào Session Map và Profile Map. Thêm các hàm `updateAvatar(userId, avatarUrl)` và `deleteAvatar(userId)`.
  - **Tầng Điều phối (`ProfileController.java`):** Cấu hình `@MultipartConfig`. Trong `doGet` chuyển tiếp Flash message. Trong `doPost` xử lý 2 action `uploadAvatar` và `deleteAvatar`, tự động dọn dẹp ảnh cũ trên máy chủ, cập nhật tức thì Session Map và chuyển hướng PRG. Bổ sung ánh xạ servlet-mapping `/UploadAvatarController` trong `web.xml`.
  - **Giao diện & Trải nghiệm (`profile.jsp`, `header.jsp`, `index.jsp`, `global.css`):** Hiển thị ảnh đại diện bo tròn mượt mà trên Profile cá nhân (kèm nút [Xóa ảnh]), nút Header `#btnAvatar`, và Thẻ thông tin Dashboard. Toàn bộ style chuẩn hóa trong `global.css`.


  ## 🛡️ TASK 16: NÂNG CẤP TOÀN DIỆN QUYỀN LỰC QUẢN TRỊ VIÊN (SUPER ADMIN DASHBOARD - USER, ROLE, KEY & PASSWORD RESET)
**Actor:** Quản trị viên hệ thống (Admin)
**Mô tả:** Nâng cấp trang Quản trị viên (`admin_dashboard.jsp`) để hiển thị đầy đủ ngày gia nhập (`HireDate`), bổ sung Tab cấp lại mật khẩu cho các tài khoản trong hệ thống (chặn Admin tự đổi mật khẩu của chính mình tại đây), và bổ sung bộ quyền lực Super Admin (Option A): Thay đổi trực tiếp vai trò nhân sự (`Role`) kèm đồng bộ hồ sơ `ShipperProfiles`/`AccountManager`, và thu hồi/khóa các mã `Security Key` chưa sử dụng. **Lưu ý cho AI: Thực hiện tuần tự từng sub-task một, tuân thủ nghiêm ngặt `Rule_BOMS.md` và `boms_agent_workflow.md`.**

- [x] **Task 16.1: Bổ sung cột Ngày gia nhập (`HireDate`) vào bảng Quản lý Người dùng & Chuẩn hóa Flash Message (`AdminService.java`, `AdminDashboardController.java`, `admin_dashboard.jsp`):** **[DONE]**
  - **Tầng Service (`AdminService.java`):** Trong phương thức `getAllUsersForView()`, bổ sung đóng gói thuộc tính `map.put("hireDate", u.getHireDate());` vào `Map<String, Object>` của từng người dùng.
  - **Tầng Controller (`AdminDashboardController.java`):** Bổ sung lời gọi `FlashMessageUtil.forwardFlashMessages(request, request.getSession());` ở đầu phương thức `doGet` để tiếp nhận và dọn dẹp thông báo một lần từ `Session` chuẩn PRG.
  - **Tầng View & Đa ngôn ngữ (`web/admin_dashboard.jsp`, `messages_vi.properties`, `messages_en.properties`):**
    + Thêm cột tiêu đề `<th><fmt:message key="admin.dashboard.table.hiredate" /></th>` vào bảng danh sách người dùng tại `#tab-users`.
    + Hiển thị giá trị ngày gia nhập bằng `<fmt:formatDate value="${user.hireDate}" pattern="dd/MM/yyyy" />` (nếu `empty user.hireDate` thì hiển thị `-`). Cập nhật `colspan` của dòng trống từ `6` lên `7`.
    + Khai báo key `admin.dashboard.table.hiredate` vào 2 file `.properties` (bắt buộc mã hóa Unicode Escape `\uXXXX` trong `messages_vi.properties`).

- [x] **Task 16.2: Xây dựng Tab "Cấp lại mật khẩu" (`tab-passwords`) dành cho Admin (Chặn tự đổi mật khẩu của chính mình):** **[DONE]**
  - **Tầng Service (`AdminService.java` & `UserService.java`):**
    + Xây dựng phương thức `resetUserPasswordByAdmin(int adminUserId, String targetUserIdStr, String newPassword, String confirmPassword)` trong `AdminService`.
    + Kiểm tra nếu `adminUserId == targetUserId` thì ném `ValidationException(Constants.ERROR_ADMIN_CANNOT_RESET_SELF_PSW)` (mã lỗi `E9001`).
    + Kiểm tra sự tồn tại của tài khoản đích qua `userDAO.getUserById(targetUserId)` (nếu không tồn tại ném `Constants.ERROR_USER_NOT_FOUND`).
    + Tái sử dụng hàm `userService.validatePassword(newPassword)` để kiểm tra mật khẩu mới không rỗng, đủ độ dài tối thiểu (`MIN_PASSWORD_LENG`) và đúng định dạng; kiểm tra `newPassword.equals(confirmPassword)` (nếu sai ném `Constants.ERROR_CONFIRM_PSW`).
    + Gọi `userDAO.updatePassword(targetUserId, newPassword)` để cập nhật mật khẩu.
  - **Tầng Controller (`AdminDashboardController.java`):**
    + Bổ sung phương thức `doPost` trong `AdminDashboardController` (kiểm tra chặt chẽ quyền `Admin` từ `USER_SESSION`).
    + Khi nhận `action="resetPassword"`, lấy `adminUserId` từ `USER_SESSION`, gọi `adminService.resetUserPasswordByAdmin(...)`, lưu mã thành công `Constants.SUCCESS_ADMIN_RESET_PSW` (`S9001`) vào `Session` và điều hướng `sendRedirect("AdminDashboardController?tab=tab-passwords")`.
  - **Tầng View (`web/admin_dashboard.jsp`):**
    + Thêm nút chuyển Tab thứ 3: `tab-passwords` (`<fmt:message key="admin.dashboard.tab.passwords" />`).
    + Thiết kế Form cấp lại mật khẩu gồm: Dropdown chọn tài khoản từ `${USER_LIST}` (dùng `<c:if test="${user.username != sessionScope.USER_SESSION.username}">` để ẩn tài khoản của chính Admin đang đăng nhập), ô nhập Mật khẩu mới, ô Xác nhận mật khẩu mới và nút Submit.

- [x] **Task 16.3: Nâng cấp Quyền lực Super Admin (Option A) — Đổi Vai trò nhân sự trực tiếp (`Change Role`) & Thu hồi `Security Key` chưa sử dụng (`Revoke Key`):** **[DONE]**
  - **Tầng DAO (`UserDAO.java` & `AccountManagerDAO.java`):**
    + Tại `UserDAO`: Bổ sung hàm `updateUserRole(int userId, String newRole)` (`UPDATE Users SET Role = ? WHERE UserID = ?`) và hàm `ensureShipperProfileExists(int userId)` (kiểm tra nếu chưa có bản ghi trong `ShipperProfiles` cho `userId` thì `INSERT INTO ShipperProfiles (UserID, ShipperStatus, ActiveOrders, MaxOrders) VALUES (?, 'Offline', 0, 3)`).
    + Tại `AccountManagerDAO`: Bổ sung hàm `updateRoleByUserId(int userId, String newRole)` (`UPDATE AccountManager SET Role = ? WHERE UserID = ?`) và hàm `revokeUnusedKey(int keyId)` (`UPDATE AccountManager SET IsActive = 0 WHERE KeyID = ? AND UserID IS NULL AND IsActive = 1`).
  - **Tầng Service (`AdminService.java`):**
    + Trong `getAllKeysForView()`: Bổ sung `map.put("keyId", k.getKeyId());`.
    + Xây dựng hàm `changeUserRoleByAdmin(int adminUserId, String targetUserIdStr, String newRole)`: Chặn Admin tự đổi Role của chính mình (ném `E9002` - `Constants.ERROR_ADMIN_CANNOT_CHANGE_SELF_ROLE`), kiểm tra `newRole` hợp lệ thuộc `Manager`, `Staff`, `Shipper` (ném `E9003` - `Constants.ERROR_INVALID_ROLE_SELECTION`), gọi `userDAO.updateUserRole`, `accountDAO.updateRoleByUserId`, và nếu `newRole` là `'Shipper'` thì gọi `userDAO.ensureShipperProfileExists`.
    + Xây dựng hàm `revokeSecurityKey(String keyIdStr)`: Kiểm tra `keyId` hợp lệ và gọi `accountDAO.revokeUnusedKey(keyId)` (nếu không cập nhật được dòng nào do mã đã dùng hoặc đã khóa thì ném `E9004` - `Constants.ERROR_KEY_CANNOT_REVOKE`).
  - **Tầng Controller & Giao diện (`AdminDashboardController.java`, `web/admin_dashboard.jsp`):**
    + Mở rộng `AdminDashboardController.doPost` xử lý `action="changeRole"` (mã thành công `S9002` - `Constants.SUCCESS_ADMIN_CHANGE_ROLE`, redirect về `tab-users`) và `action="revokeKey"` (mã thành công `S9003` - `Constants.SUCCESS_REVOKE_KEY`, redirect về `tab-keys`).
    + Trên bảng `#tab-users`: Với các tài khoản khác Admin hiện tại, hiển thị Dropdown chọn Role (`Manager`, `Staff`, `Shipper`) kèm nút Lưu Role ngay bên cạnh nút Khóa/Mở khóa.
    + Trên bảng `#tab-keys`: Thêm cột Thao tác hiển thị nút **[Thu hồi / Khóa Key]** đối với các mã có `key.active and empty key.userId`.

## ⚠️ TASK 17: TỰ ĐỘNG XỬ LÝ CẢNH BÁO HẾT HẠN KHI CẬP NHẬT KHO & ĐỒNG BỘ THÔNG BÁO KHO RA MÀN HÌNH NGOÀI
**Actor:** Quản lý cửa hàng (Store Managers) & Nhân viên (Staff)
**Mô tả:** Giải quyết triệt để hiện tượng cảnh báo `Expired` / `Expiring Soon` không tự biến mất khi cập nhật lại nguyên liệu trên trang Quản lý kho (do bị kẹt dữ liệu lô cũ trong `IngredientBatches`), đồng thời đồng bộ tự động mọi cảnh báo kho mới sang bảng `Notifications` để hiển thị trên Quả chuông 🔔 và Bảng tin màn hình ngoài (`index.jsp`) cho cả `Manager` và `Staff`.

- [x] **Task 17.1: Tự động dọn lô hàng quá hạn và đóng cảnh báo `Expired` / `Expiring Soon` khi bấm Cập nhật nguyên liệu (`IngredientDAO.java` & `IngredientService.java`):** **[DONE]**
  - **Tầng DAO (`IngredientDAO.java`):** Bổ sung phương thức `resolveExpiryAlertsOnRestock(int ingredientId)` thực hiện:
    1. Đưa số lượng tồn của các lô đã quá hạn hoặc sắp hết hạn cũ của nguyên liệu đó trong `IngredientBatches` về `0`:
       `UPDATE IngredientBatches SET Quantity = 0 WHERE IngredientID = ? AND ExpirationDate <= DATEADD(day, 3, CAST(GETDATE() AS DATE)) AND Quantity > 0` (ngăn chặn `scanIngredientStock()` tái sinh cảnh báo Zombie).
    2. Đóng toàn bộ cảnh báo hạn sử dụng đang mở của nguyên liệu đó trong `InventoryAlerts`:
       `UPDATE InventoryAlerts SET Status = 'Closed' WHERE IngredientID = ? AND AlertType IN ('Expired', 'Expiring Soon') AND Status IN ('Active', 'Confirmed')`.
  - **Tầng Service (`IngredientService.java`):**
    + Trong hàm `updateIngredient(...)`, ngay sau lệnh `ingredientDao.updateIngredient(existing);`, gọi liên tiếp `ingredientDao.resolveExpiryAlertsOnRestock(ingredientId);` và `ingredientDao.closeResolvedStockAlerts();`.
    + Đảm bảo ngay khi Manager bấm Lưu cập nhật nguyên liệu trên trang Quản lý kho, cả cảnh báo Hết hàng lẫn cảnh báo Hết hạn của nguyên liệu đó đều tự động biến mất.

- [x] **Task 17.2: Đồng bộ Cảnh báo Kho (`InventoryAlert`) sang Bảng thông báo hệ thống (`Notifications`) và Bảng tin `index.jsp` cho cả `Manager` và `Staff`:** **[DONE]**
  - **Tầng DAO (`UserDAO.java` & `IngredientDAO.java`):**
    + Bổ sung phương thức `getActiveManagerAndStaffUserIds()` trong `UserDAO.java` (`SELECT UserID FROM Users WHERE IsActive = 1 AND Role IN ('Manager', 'Staff')`).
    + Nâng cấp `insertInventoryAlert(int ingredientId, String alertType)` trong `IngredientDAO.java` sử dụng `Statement.RETURN_GENERATED_KEYS` để trả về `int alertId` vừa tạo.
  - **Tầng Service (`IngredientService.java`):**
    + Khởi tạo `NotificationService` và `UserDAO` trong `IngredientService`.
    + Viết hàm trợ năng đơn trách nhiệm `broadcastInventoryAlertNotification(Ingredient ing, String alertType, int alertId)` gửi thông báo loại `"InventoryAlert"` kèm nội dung cảnh báo rõ ràng tới toàn bộ `UserID` lấy từ `userDAO.getActiveManagerAndStaffUserIds()`.
    + Trong `scanIngredientStock()`, tại cả 4 nhánh phát sinh cảnh báo mới (`OutOfStock`, `LowStock`, `Expired`, `Expiring Soon`), chỉ khi `!ingredientDao.isAlertActive(...)` và vừa `insertInventoryAlert` thành công mới gọi `broadcastInventoryAlertNotification(...)` (đảm bảo mỗi sự cố kho chỉ bắn thông báo 1 lần duy nhất ra màn hình ngoài `index.jsp` và Quả chuông 🔔, không gây spam khi polling).

## 🗂️ TASK 18: QUẢN LÝ DANH MỤC ĐỘNG CHO THỰC ĐƠN & KHO NGUYÊN LIỆU (THÊM MỚI & XÓA AN TOÀN - PHƯƠNG ÁN 6A)
**Actor:** Quản lý cửa hàng (Store Managers) & Nhân viên (Staff)
**Mô tả:** Giải phóng việc fix cứng danh mục ở cả Quản lý Thực đơn (`MenuCategories`) và Quản lý Kho (`IngredientCategories`). Cung cấp nút mở Modal Quản lý Danh mục ngay trên thanh công cụ của 2 trang (Phương án 6A), cho phép Thêm danh mục mới và Xóa an toàn các danh mục chưa chứa món ăn/nguyên liệu.

- [x] **Task 18.1: Quản lý Danh mục Thực đơn (`MenuCategories`) — Thêm mới & Xóa danh mục rỗng trên `menu_management.jsp`:** **[DONE]**
  - **Tầng DAO (`MenuCategoryDAO.java`):**
    + Bổ sung các hàm `insertCategory(String categoryName)`, `isCategoryInUse(int categoryId)` (`SELECT COUNT(1) FROM Products WHERE CategoryID = ?`), và `deleteCategory(int categoryId)` (`DELETE FROM MenuCategories WHERE CategoryID = ?`). Bọc `try-with-resources` và ném `ValidationException` kèm mã lỗi `Constants.ERROR_DATABASE_UPDATE_PRODUCT` / `ERROR_DATABASE_GET_PRODUCT`.
  - **Tầng Service (`ProductService.java`):**
    + Trong `getAllCategoriesForView()`: Bổ sung thuộc tính `map.put("inUse", menuCategoryDAO.isCategoryInUse(cat.getCategoryId()));` cho từng danh mục.
    + Xây dựng hàm `createCategory(String categoryName)`: Kiểm tra không rỗng & độ dài tối đa 100 ký tự (`Constants.ERROR_EMPTY_PRODUCT_CATEGORY`), kiểm tra trùng tên qua `menuCategoryDAO.getCategoryByName(categoryName)` (nếu trùng ném `E0219` - `Constants.ERROR_MENU_CATEGORY_EXISTS`), và gọi `menuCategoryDAO.insertCategory`.
    + Xây dựng hàm `deleteCategory(String categoryIdStr)`: Kiểm tra `categoryId` hợp lệ, nếu `menuCategoryDAO.isCategoryInUse(categoryId)` trả về `true` thì chặn xóa và ném `E0220` (`Constants.ERROR_MENU_CATEGORY_IN_USE`), ngược lại gọi `menuCategoryDAO.deleteCategory(categoryId)`.
  - **Tầng Controller & Giao diện (`MenuManagementController.java`, `menu_management.jsp`, `product_form_modal.jsp`, `main.js`):**
    + Trong `MenuManagementController.doPost`: Bổ sung nhánh xử lý `action="createCategory"` (mã thành công `S0213` - `Constants.SUCCESS_CREATE_MENU_CATEGORY`) và `action="deleteCategory"` (mã thành công `S0214` - `Constants.SUCCESS_DELETE_MENU_CATEGORY`), chuyển hướng PRG.
    + Trên `web/menu_management.jsp`: Thêm nút **`📁 <fmt:message key="menu.category.btn.manage" />`** bên cạnh nút `+ Thêm Món Mới` để gọi `openMenuCategoryModal()`.
    + Trong `web/components/product_form_modal.jsp`: Bổ sung Modal `#menuCategoryModal` hiển thị Form nhập tên danh mục món mới và Bảng danh sách các danh mục hiện có kèm nút **[Xóa]** (chỉ hiển thị nút Xóa khi `${not cat.inUse}`, nếu `${cat.inUse}` hiển thị badge *"Đang có món"*).

- [x] **Task 18.2: Quản lý Danh mục Kho nguyên liệu (`IngredientCategories`) — Thêm mới & Xóa danh mục rỗng trên `inventory_managerment.jsp`:** **[DONE]**
  - **Tầng DAO (`IngredientDAO.java`):**
    + Bổ sung các hàm `isIngredientCategoryNameExists(String categoryName)` (`SELECT COUNT(1) FROM IngredientCategories WHERE LOWER(CategoryName) = LOWER(?)`), `insertIngredientCategory(String categoryName)`, `isIngredientCategoryInUse(int categoryId)` (`SELECT COUNT(1) FROM Ingredients WHERE CategoryID = ?`), và `deleteIngredientCategory(int categoryId)` (`DELETE FROM IngredientCategories WHERE CategoryID = ?`).
  - **Tầng Service (`IngredientService.java`):**
    + Trong `getCategoriesForView()`: Bổ sung `m.put("inUse", ingredientDao.isIngredientCategoryInUse(entry.getKey()));`.
    + Xây dựng hàm `createCategory(String categoryName, String userRole)` và `deleteCategory(String categoryIdStr, String userRole)`: Bắt buộc gọi `validateManagerPermission(userRole)`, kiểm tra tên danh mục không rỗng (`E0409` - `Constants.ERROR_EMPTY_INGREDIENT_CATEGORY`), kiểm tra trùng tên (`E0410` - `Constants.ERROR_INGREDIENT_CATEGORY_EXISTS`), kiểm tra danh mục chưa chứa nguyên liệu trước khi xóa (`E0411` - `Constants.ERROR_INGREDIENT_CATEGORY_IN_USE`). Khai báo mã thành công `S0405` (`Constants.SUCCESS_CREATE_INGREDIENT_CATEGORY`) và `S0406` (`Constants.SUCCESS_DELETE_INGREDIENT_CATEGORY`).
  - **Tầng Controller & Giao diện (`InventoryManagementController.java`, `inventory_managerment.jsp`, `inventory_form_modal.jsp`, `main.js`):**
    + Trong `InventoryManagementController.doPost`: Bổ sung nhánh `action="createCategory"` và `action="deleteCategory"`, lưu Flash Message vào `Session` và redirect PRG.
    + Trên `web/inventory_managerment.jsp`: Trong khối `<c:if test="${IS_MANAGER}">`, thêm nút **`📁 <fmt:message key="inventory.category.btn.manage" />`** cạnh nút `+ Thêm nguyên liệu` gọi `openInventoryCategoryModal()`.
    + Trong `web/components/inventory_form_modal.jsp`: Bổ sung Modal `#inventoryCategoryModal` gồm Form thêm danh mục nguyên liệu mới và Bảng danh sách danh mục hiện có kèm nút **[Xóa]** cho các danh mục `${not cat.inUse}`.

## 🔧 TASK 19: TỔNG RÀ SOÁT, VÁ LỖ HỔNG BẢO MẬT & HOÀN THIỆN CHẤT LƯỢNG TOÀN DIỆN (FINAL HARDENING & POLISH)
**Actor:** Toàn bộ hệ thống / Developer
**Mô tả:** Khắc phục toàn bộ các lỗ hổng bảo mật truy cập trái phép, dọn dẹp rò rỉ kết nối JDBC, triệt tiêu inline CSS/JS, mở thông whitelist khách vãng lai và đồng bộ cấu hình servlet để đạt trạng thái xuất xưởng hoàn hảo nhất.

- [x] **Task 19.1: Vá lỗ hổng bảo mật phân quyền & Chuẩn hóa PRG Controller (`AdminDashboardController`, `SettingsController` - Phương án A):** **[DONE]**
  - **Tầng Controller (`AdminDashboardController.java`):**
    + Hợp nhất toàn bộ luồng tạo mã Security Key (`action="generateKey"`) và luồng Khóa/Mở khóa tài khoản (`action="toggleUser"`) vào `doPost` của `AdminDashboardController` (tận dụng khối xác thực quyền Admin tập trung `Role == 'Admin'`).
    + Chặn Admin tự khóa tài khoản của chính mình (ném lỗi `Constants.ERROR_ADMIN_CANNOT_CHANGE_SELF_ROLE` / `E9002`).
    + Cập nhật các form tương ứng trong `web/admin_dashboard.jsp` trỏ về `action="AdminDashboardController"` kèm `input hidden name="action" value="generateKey"` và `value="toggleUser"`.
    + Xóa bỏ 2 servlet thừa và không an toàn: `GenerateKeyController.java` và `ToggleUserController.java`, gỡ bỏ khai báo tương ứng trong `web.xml`.
  - **Tầng Controller (`SettingsController.java`):**
    + Trong `doGet` và `doPost`: Bổ sung kiểm tra `loginUser == null` (nếu null lập tức redirect về `/login.jsp`, triệt tiêu hoàn toàn lỗi `NullPointerException` HTTP 500).
    + Chuyển đổi toàn bộ `doPost` sang chuẩn PRG (`sendRedirect("SettingsController")` kèm lưu `SUCCESS_MESSAGE`/`ERROR_MESSAGE` vào `Session`), dọn dẹp lệnh gọi `doGet(request, response)` sai quy chuẩn kiến trúc.

- [x] **Task 19.2: Mở thông luồng Khách vãng lai tại Bộ lọc bảo mật (`AuthFilter.java`):** **[DONE]**
  - Trong `AuthFilter.java`: Thêm điều kiện `|| uri.endsWith("DashboardController")` vào whitelist `isAuthPage` để khách vãng lai (chưa đăng nhập) truy cập được vào Bảng điều khiển trung tâm xem giao diện Landing Page giới thiệu hệ thống trên `index.jsp`.
  - Xóa bỏ dòng `|| uri.endsWith("register.jsp")` bị khai báo trùng lặp (dòng 39 và dòng 42).

- [x] **Task 19.3: Quy hoạch dọn dẹp rò rỉ kết nối JDBC sang `try-with-resources` toàn diện các DAO:** **[DONE]**
  - Tái cấu trúc toàn bộ ~60 phương thức còn sót lại từ giai đoạn đầu trong `UserDAO.java`, `OrderDAO.java` (đặc biệt là `insertOrder` và `loadOrderDetails`), `IngredientDAO.java`, `ProductDAO.java`, `MenuCategoryDAO.java`, `AccountManagerDAO.java` sang cú pháp `try-with-resources` (`try (PreparedStatement st = ...; ResultSet rs = ...)`).
  - Đảm bảo 100% tài nguyên kết nối cơ sở dữ liệu được đóng tự động và an toàn trong mọi trường hợp (kể cả khi xảy ra lỗi rollback).

- [x] **Task 19.4: Triệt tiêu Inline CSS / JS & Loại bỏ trùng lặp Script (`login.jsp`, `menu.jsp`, `attendance.jsp`, `hr_management.jsp`):** **[DONE]**
  - **Tầng Giao diện & Script (`web/login.jsp`, `web/js/main.js`, `web/css/global.css`):**
    + Xóa bỏ `style="margin-top: 15px;"` và `onclick="..."` tại nút đóng popup `bannedPopup` trong `login.jsp`. Thêm hàm `closeBannedPopup()` vào `main.js` và bổ sung class CSS tương ứng vào `global.css`.
  - **Tầng Giao diện (`web/menu.jsp`, `web/css/global.css`):**
    + Xóa bỏ style nội tuyến tại dòng 29 và 38 của `menu.jsp`, chuyển đổi thành các class ngữ nghĩa `.menu-header-bar` và `.menu-empty-container` trong `global.css`.
  - **Tầng Giao diện (`web/attendance.jsp`, `web/hr_management.jsp`):**
    + Xóa bỏ thẻ `<script src=".../js/main.js"></script>` thừa ở cuối trang `attendance.jsp` và `hr_management.jsp` để tránh việc nạp script 2 lần (đã có trong `footer.jsp`).

- [x] **Task 19.5: Hoàn thiện Đa ngôn ngữ (i18n) & Khai báo Servlet trong `web.xml`:** **[DONE]**
  - Bổ sung key `login.banned.message=Your account has been deactivated. Please contact the administrator for assistance!` vào `messages_en.properties`.
  - Sửa lỗi khoảng trắng trước dấu bằng ở key `register.label.fullname` trong `messages_en.properties`.
  - Khai báo bổ sung servlet và servlet-mapping cho `DispatchController` và `ShipperController` trong `web/WEB-INF/web.xml` để đảm bảo 100% servlet dự án được cấu hình đồng bộ.
  - Tái sử dụng `FlashMessageUtil.forwardFlashMessages(request, session)` trong `MenuManagementController.java` và `OrderManagementController.java`, xóa bỏ các hàm helper private trùng lặp.


## 🔒 FINAL TASK 20: XỬ LÝ NỢ KỸ THUẬT CUỐI CÙNG (TRANSACTION ĐĂNG KÝ & MÃ HÓA MẬT KHẨU SHA-256)
**Actor:** System / Developer
**Mô tả:** Đóng toàn bộ các hạng mục nợ kỹ thuật cuối cùng được thống nhất làm sau cùng trước khi bàn giao sản phẩm: Quản lý giao dịch nguyên tử (Atomic Transaction) khi Đăng ký tài khoản và Mã hóa mật khẩu bảo mật chuẩn SHA-256 kèm cơ chế tương thích ngược cho dữ liệu kiểm thử.

- [x] **Task 20.1: Quản lý giao dịch (Transaction) nguyên tử khi Đăng ký:** **[DONE]**
  - **Tầng DAO (`UserDAO.java`):** Bổ sung hàm `registerUserWithKeyTransaction(User user, String securityKey)` quản lý kết nối JDBC nguyên tử với `connection.setAutoCommit(false)`, `commit()` và `rollback()`. Đảm bảo việc thêm bản ghi vào `Users`, cập nhật trạng thái `AccountManager` (`IsUsed = 1`, `UserID = ?`), và tự động khởi tạo hồ sơ `ShipperProfiles` (nếu vai trò là Shipper) diễn ra đồng thời. Nếu bất kỳ bước nào thất bại, tự động rollback 100% dữ liệu.
  - **Tầng Service (`UserService.java`):** Tích hợp gọi `userDAO.registerUserWithKeyTransaction(user, securityKey)` trong quy trình đăng ký tài khoản.

- [x] **Task 20.2: Mã hóa mật khẩu bảo mật (Password Hashing) chuẩn SHA-256:** **[DONE]**
  - **Tầng Utility (`PasswordUtil.java`):** Xây dựng tiện ích băm mật khẩu `hashPassword(String)` bằng thuật toán SHA-256 với định dạng chuỗi Hex 64 ký tự; hàm `checkPassword(plain, stored)` kiểm tra so khớp mật khẩu và tự động hỗ trợ tương thích ngược (backward compatibility) cho các tài khoản kiểm thử cũ đang lưu mật khẩu thô (plain-text).
  - **Tầng Service (`UserService.java` & `AdminService.java`):**
    + Trong `UserService.login()`: Kiểm tra mật khẩu qua `PasswordUtil.checkPassword`; tự động nâng cấp mã hóa (Auto-upgrade password hashing) mật khẩu thô lên SHA-256 ngay trong lần đăng nhập thành công đầu tiên.
    + Trong `UserService.register()`: Băm mật khẩu bằng `PasswordUtil.hashPassword` trước khi lưu vào CSDL.
    + Trong `UserService.changePassword()`: Kiểm tra mật khẩu cũ bằng `checkPassword` và băm mật khẩu mới bằng `hashPassword` trước khi gọi `updatePassword`.
    + Trong `AdminService.resetUserPasswordByAdmin()`: Tự động băm mật khẩu mới bằng `PasswordUtil.hashPassword` trước khi ghi xuống CSDL.



## 🔐 TASK 21: NÂNG CẤP, TỐI ƯU & KHẮC PHỤC TRIỆT ĐỂ LỖI HỆ THỐNG ĐĂNG NHẬP (LOGIN SYSTEM HARDENING & BUG FIX)
**Actor:** Tất cả người dùng & Developer
**Mô tả:** Khắc phục toàn bộ các lỗi hiển thị, vỡ cấu trúc DOM HTML, xung đột trạng thái đăng nhập, lệch chuẩn thông báo độ dài username, tích hợp Flash Message dùng chung và triệt tiêu inline JS theo đúng quy chuẩn `Rule_BOMS.md`.

- [x] **Task 21.1: Khắc phục Lỗi Cấu trúc HTML & Vỡ DOM Hiển thị (`web/login.jsp`):** **[DONE]**
  - Xóa bỏ việc khai báo lồng thẻ `<!DOCTYPE html><html><head><body>` dư thừa trong `login.jsp`.
  - Di chuyển `<jsp:include page="header.jsp" />` ra khỏi thẻ `<head>` và chuyển đổi cấu trúc sang chuẩn chung của dự án (include `header.jsp` ở đầu và include `footer.jsp` ở cuối) để loại bỏ 100% tình trạng trùng lặp thẻ HTML, vỡ DOM và lỗi phân giải giao diện.

- [x] **Task 21.2: Đồng Bộ Hóa Logic Kiểm Tra & Thông Báo Độ Dài Username (`Constants.java`, `UserService.java`, `.properties`):** **[DONE]**
  - Khắc phục sự lệch pha giữa code kiểm tra độ dài tối thiểu (`Constants.MIN_USER_LENG = 5`) và nội dung thông báo mã lỗi `E1000` (*"Tên đăng nhập phải từ 3 đến 20 ký tự!"*).
  - Thống nhất ngưỡng kiểm tra và cập nhật đồng bộ giữa hằng số Java và file đa ngôn ngữ (`messages_vi.properties` & `messages_en.properties`) để thông báo hiển thị khớp chính xác 100% với logic thực tế.

- [x] **Task 21.3: Ngăn Chặn Xung Đột Trạng Thái Đã Đăng Nhập (`LoginController.java`):** **[DONE]**
  - Trong phương thức `doGet` của `LoginController`: Kiểm tra `session.getAttribute(Constants.USER_SESSION) != null`. Nếu người dùng đã đăng nhập thành công từ trước, tự động chuyển hướng (`sendRedirect`) về `DashboardController`, ngăn chặn xung đột UX (vừa hiển thị Avatar người dùng trên Header vừa hiện Form đăng nhập ở View).

- [x] **Task 21.4: Tích Hợp `FlashMessageUtil` & Bổ Sung Hiển Thị Thông Báo Thành Công (`LoginController.java` & `web/login.jsp`):** **[DONE]**
  - Gọi `FlashMessageUtil.forwardFlashMessages(request, session)` ở đầu hàm `doGet` trong `LoginController.java`.
  - Bổ sung khối `<c:if test="${not empty SUCCESS_MESSAGE}">` trên `login.jsp` để sẵn sàng hiển thị các thông điệp thành công (đổi mật khẩu, đăng xuất, chuyển hướng...) gửi về trang đăng nhập.

- [x] **Task 21.5: Chuẩn Hóa Cấu Hình JSTL Locale 3 Tầng & Triệt Tiêu Inline JS (`web/login.jsp`, `web/js/main.js`):** **[DONE]**
  - Cập nhật cấu hình đa ngôn ngữ trên đầu trang `login.jsp` sang chuẩn ưu tiên 3 tầng (`Session` -> `Cookie` -> Mặc định `'vi'`).
  - Xóa bỏ thuộc tính inline `onclick="closeBannedPopup()"` trên nút `#btnCloseBannedPopup` tại `login.jsp`, chuyển việc đăng ký sự kiện click sang `web/js/main.js` tuân thủ nghiêm ngặt `Rule_BOMS.md`.

- [x] **Task 21.6: Bổ Sung Khối Try-Catch Tổng Phòng Ngừa Ngoại Lệ Hệ Thống (`LoginController.java`):** **[DONE]**
  - Bổ sung khối `catch (Exception e)` trong `doPost` của `LoginController` để bắt toàn bộ các ngoại lệ bất thường (lỗi rớt kết nối CSDL, runtime exception...), gán mã lỗi chuẩn `Constants.ERROR_SYSTEM` (`E0000`) và forward an toàn về `login.jsp`, triệt tiêu rủi ro lộ trang lỗi HTTP 500 của máy chủ.


## 🚚 TASK 22: KIỂM TOÁN VÀ REFACTOR TOÀN DIỆN PHÂN HỆ PHÂN QUYỀN & GIAO HÀNG/ĐIỀU PHỐI (AUTH & DELIVERY REFACTORING)
**Actor:** Tất cả người dùng & Developer
**Mô tả:** Triển khai trọn vẹn 3 giai đoạn kiểm toán bảo mật, điều phối và giao diện cho phân hệ Giao hàng & Phân quyền, tuân thủ nghiêm ngặt 100% nguyên tắc Rule_BOMS.md.

- [x] **Task 22.1: Bảo Mật Luồng (AuthFilter & DAO) - [GIAI ĐOẠN 1]:** **[DONE]**
  - Khai báo hằng số phân quyền `ROLE_ADMIN`, `ROLE_MANAGER`, `ROLE_STAFF`, `ROLE_SHIPPER` và loại đơn `ORDER_TYPE_DELIVERY`, `ORDER_TYPE_COUNTER`, `ORDER_TYPE_TABLE` trong `Constants.java`.
  - Thiết lập cơ chế Phân quyền tập trung (RBAC) tại `AuthFilter.java`: bảo vệ tuyến `/ShipperController` (chỉ `Shipper`/`Admin`) và `/DispatchController` (chỉ `Manager`/`Admin`/`Staff`, cấm tuyệt đối `Shipper`).
  - Triệt tiêu 100% chuỗi literal thô trong các câu truy vấn SQL của `DeliveryDAO.java` sang sử dụng hằng số từ `Constants.java`.

- [x] **Task 22.2: Điều Hướng & Trạng Thái (ShipperController & DispatchController) - [GIAI ĐOẠN 2]:** **[DONE]**
  - Thay thế toàn bộ chuỗi hardcode role trong `validateSessionAndRole` bằng `Constants.ROLE_*`.
  - Bổ sung khối `catch (Exception e)` tổng dự phòng tại `doPost` cho cả `ShipperController` và `DispatchController`, bảo đảm không văng lỗi 500 khi đứt kết nối CSDL hoặc lỗi runtime.
  - Chuẩn hóa trạng thái chờ nhận đơn duy nhất `Constants.STATUS_WAITING_DELIVERY`, triệt tiêu alias dư thừa.

- [x] **Task 22.3: Giao Diện Song Ngữ & Triệt Tiêu Inline JS/CSS (Shipper Views) - [GIAI ĐOẠN 3]:** **[DONE]**
  - Chuẩn hóa cấu hình JSTL Locale 3 tầng (`Session` -> `Cookie` -> `'vi'`) cho `web/components/delivery_orders_table.jsp`.
  - Triệt tiêu 100% inline JavaScript (`onclick`, `onchange`, `onsubmit`) trong `web/shipper_dashboard.jsp`, `web/components/shipper_complete_modal.jsp`, `web/components/shipper_fail_modal.jsp` và `web/delivery_unauthorized.jsp`.
  - Chuyển toàn bộ cơ chế mở/đóng modal, xác nhận từ chối đơn (`confirm`), chọn lý do thất bại mẫu và nút quay lại sang mô hình Event Delegation chuyên nghiệp trong `web/js/main.js`.


## 🛒 TASK 23: KIỂM TOÁN VÀ REFACTOR TOÀN DIỆN PHÂN HỆ BÁN HÀNG & XỬ LÝ ĐƠN (CART, CHECKOUT & ORDER PROCESSING REFACTORING)
**Actor:** Khách hàng tại bàn/tại quầy, Thu ngân, Bếp/Pha chế, Quản lý cửa hàng & Developer
**Mô tả:** Triển khai trọn vẹn 3 giai đoạn kiểm toán toàn vẹn transaction, bảo vệ phiên giỏ hàng chống tràn bộ nhớ, chuẩn hóa MVC Controller và triệt tiêu inline JS / thiếu sót i18n theo đúng quy chuẩn Rule_BOMS.md.

- [x] **Task 23.1: Toàn vẹn Transaction & Chuẩn Hóa Tầng Dữ liệu (`OrderDAO.java`, `Constants.java`) - [GIAI ĐOẠN 1]:** **[DONE]**
  - Bổ sung 4 hằng số trạng thái đơn hàng chuẩn (`STATUS_ACCEPTED`, `STATUS_PAID`, `STATUS_PREPARED`, `STATUS_CANCELLED`) vào `Constants.java`.
  - Trong `OrderDAO.insertOrder`: Thiết lập chốt chặn kiểm tra Generated Key (`if (newOrderId <= 0) throw new SQLException(...)`) để kích hoạt rollback tức thì, ngăn chặn triệt để rủi ro mất chi tiết đơn hàng hoặc chèn dữ liệu mồ côi `OrderID = 0`.
  - Thay thế 100% Magic Strings trạng thái và loại đơn hàng trong `OrderDAO.java` sang sử dụng hằng số từ `Constants.*`.

- [x] **Task 23.2: Điều Hướng, Quản Lý Giỏ Hàng & Chống Tràn Bộ Nhớ (`CartController.java`, `OrderManagementController.java`, `OrderService.java`) - [GIAI ĐOẠN 2]:** **[DONE]**
  - Kiểm tra và xác nhận cơ chế giải phóng giỏ hàng khỏi Session (`session.removeAttribute(Constants.CURRENT_CART)`) sau khi Checkout thành công và khi làm trống giỏ.
  - Bổ sung khối `catch (Exception e)` tổng dự phòng cho `CartController.doPost` gán `Constants.ERROR_SYSTEM` (`E0000`), triệt tiêu hoàn toàn rủi ro crash HTTP 500 khi client gửi tham số sai định dạng (`NumberFormatException`).
  - Sửa lỗi copy-paste mã lỗi `Constants.SERROR_INVALID_SETTING_TAB` trong `OrderManagementController.doPost` về `Constants.ERROR_SYSTEM` (`E0000`).
  - Chuẩn hóa kiểm tra quyền truy cập `OrderManagementController` bằng các hằng số `Constants.ROLE_*`.
  - Thay thế 100% chuỗi literal trạng thái đơn hàng trong `OrderService.java` sang sử dụng `Constants.*`.

- [x] **Task 23.3: Chuẩn Hóa Locale 3 Tầng, Đa Ngôn Ngữ & Triệt Tiêu Inline JS (`menu.jsp`, `floating_cart.jsp`, `order_confirm_modal.jsp`, `order_management.jsp`, `standard_orders_table.jsp`, `main.js`) - [GIAI ĐOẠN 3]:** **[DONE]**
  - Chuẩn hóa cấu hình JSTL Locale 3 tầng (`Session` -> `Cookie` -> `'vi'`) cho cả 5 file JSP liên quan đến giỏ hàng và đơn hàng.
  - Xóa bỏ 100% inline JavaScript (`onclick`, `onsubmit`) trong `menu.jsp`, `floating_cart.jsp`, `order_confirm_modal.jsp`, `order_management.jsp`, `standard_orders_table.jsp`.
  - Bọc 100% text Tiếng Việt trần và các nhãn trạng thái đơn trong `standard_orders_table.jsp` và `order_management.jsp` vào `<fmt:message key="...">` đa ngôn ngữ.
  - Bổ sung cơ chế Event Delegation tập trung trong `web/js/main.js` để xử lý mượt mà việc chuyển tab giỏ hàng, đóng/mở modal xác nhận đặt món, confirm hủy đơn và xử lý thay đổi trạng thái đơn của nhân viên.


## 🚨 TASK 24: VÁ LỖ HỔNG NGHIỆP VỤ ĐIỀU PHỐI GIAO HÀNG (TRÀN TẢI SHIPPER & CƠ CHẾ THU HỒI ĐƠN TREO)
**Actor:** Quản lý cửa hàng (Store Manager), Shipper, QA Lead & Developer
**Mô tả:** Khắc phục triệt để 2 lỗ hổng logic nghiệp vụ nghiêm trọng: tính toán thiếu đơn chờ tiếp nhận dẫn đến tràn tải Shipper, và thiếu cơ chế thu hồi đơn bị treo khi Shipper không bấm nhận/từ chối.

- [x] **Task 24.1: Khắc phục Lỗ hổng "Tràn tải" (Validation tải trọng Shipper):** **[DONE]**
  - Khai báo hằng số `MAX_SHIPPER_ORDERS = 5` trong `Constants.java`.
  - Trong `DeliveryDAO.countActiveOrdersByShipperId`: Sửa câu lệnh SQL đếm tổng cả 2 trạng thái `Waiting for delivery` và `Delivering`.
  - Trong `DeliveryService.countActiveOrders`: Cập nhật logic đếm bao gồm cả đơn đang giao (`Delivering`) lẫn đơn đang chờ xác nhận (`Waiting for delivery`).
  - Trong `DeliveryService.validateShipperCapacity`: Kiểm tra tải thực tế dựa trên ngưỡng `MAX_SHIPPER_ORDERS` (hoặc cấu hình `maxOrders` riêng của Shipper), chặn điều phối và ném ngoại lệ `Constants.ERROR_SHIPPER_FULL` nếu chạm ngưỡng.

- [x] **Task 24.2: Xây dựng Cơ chế "Thu hồi đơn treo" (Order Revocation Mechanism):** **[DONE]**
  - Khai báo `NOTE_DISPATCH_REVOKE` và `SUCCESS_REVOKE_SHIPPER_TASK = "S0608"` trong `Constants.java` và bổ sung đa ngôn ngữ trong `.properties`.
  - Trong `DeliveryDAO.java`: Thêm hàm `revokeTaskFromShipper(orderId)` gỡ `ShipperID = NULL`, đưa `Status` về `'Waiting for dispatch'`, và hàm `getAssignedPendingTasks()` truy vấn danh sách đơn đang chờ tiếp nhận.
  - Trong `DeliveryService.java`: Thêm hàm nghiệp vụ `revokeTask(taskIdStr)` kiểm tra trạng thái hợp lệ, cập nhật CSDL, ghi nhật ký lịch sử điều phối và đồng bộ lại tải Shipper; thêm hàm `getAssignedPendingTasks()`.
  - Trong `DispatchController.java`: Nạp `ASSIGNED_PENDING_TASKS` lên Dashboard và xử lý action `revoke` trong `doPost` bảo đảm luồng PRG và thông báo qua `FlashMessageUtil`.
  - Trong `web/dispatch_dashboard.jsp`: Bổ sung Bảng "Đơn Đang Chờ Shipper Tiếp Nhận" hiển thị rõ thông tin Shipper được gán và nút "Thu Hồi Đơn" (`form-revoke-task`).
  - Trong `web/js/main.js`: Bổ sung Event Delegation xác nhận trước khi thu hồi đơn, tuân thủ 100% không dùng inline JS.

## 👥 TASK 25: KIỂM TOÁN VÀ KHẮC PHỤC LỖ HỔNG PHÂN HỆ NHÂN SỰ & CA LÀM VIỆC (HR & SHIFT MANAGEMENT AUDIT & HARDENING)
**Actor:** Nhân viên (Staff), Shipper, Quản lý cửa hàng (Manager), Super Admin, QA Lead & Developer
**Mô tả:** Triển khai thuật toán kiểm tra giao thoa thời gian (Time Overlap) trong xếp ca, cơ chế phòng thủ 3 lớp (Defense-in-Depth) bảo vệ API duyệt OT, chuẩn hóa hằng số Role, dọn sạch 100% inline JS/CSS và chuẩn hóa Locale 3 tầng theo quy chuẩn Rule_BOMS.md.

- [x] **Task 25.1: Vá Lỗ hổng Logic Xếp ca (Shift Overlap) & Interval Algorithm (`AttendanceDAO.java`, `AttendanceService.java`):** **[DONE]**
  - Thêm phương thức `hasOverlappingShiftAssignment(userId, shiftId, workDate)` vào `AttendanceDAO.java` sử dụng thuật toán kiểm tra giao thoa khoảng thời gian (`s_existing.StartTime < s_new.EndTime AND s_existing.EndTime > s_new.StartTime`) với 1 câu SQL `JOIN` tối ưu.
  - Tích hợp `hasOverlappingShiftAssignment` vào cả `AttendanceService.assignShift` (xếp ca đơn) và `AttendanceService.bulkAssignShift` (xếp ca hàng loạt theo tuần/thứ).
  - Khắc phục triệt để tình trạng chặn oan các ca làm việc khác giờ trong cùng một ngày, đồng thời ngăn chặn 100% việc xếp trùng giờ làm của nhân viên.

- [x] **Task 25.2: Thiết lập Cơ chế Bảo mật 3 lớp cho Phân quyền Duyệt OT (`AuthFilter.java`, `HrManagementController.java`, `AttendanceService.java`):** **[DONE]**
  - **Lớp 1 (Filter Routing):** Bổ sung quy tắc RBAC chặn đứng truy cập trái phép vào `/HrManagementController` và `/hr_management.jsp` trong `AuthFilter.java`, chỉ cho phép `Constants.ROLE_MANAGER` và `Constants.ROLE_ADMIN`.
  - **Lớp 2 (Controller Validation):** Thay thế toàn bộ chuỗi hardcode role (`"Manager"`, `"Admin"`) trong `HrManagementController.java` thành các hằng số quy chuẩn `Constants.ROLE_MANAGER` và `Constants.ROLE_ADMIN`.
  - **Lớp 3 (Service-Level Enforcement):** Bổ sung hàm phòng vệ `validateManagerRole(int managerUserId)` trong `AttendanceService.java` và tích hợp tại đầu hàm `approveAttendance` lẫn `rejectAttendance`, đảm bảo không ai ngoài Manager/Admin có thể phê duyệt OT ngay cả khi gọi trực tiếp từ tầng Service.
  - Bổ sung khối `catch (Exception e)` dự phòng trong cả `doGet` và `doPost` của `AttendanceController.java` để bắt lỗi runtime và gán `Constants.ERROR_SYSTEM` (`E0000`).

- [x] **Task 25.3: Chuẩn Hóa MVC, Triệt Tiêu 100% Inline JS & Chuẩn Hóa Locale 3 Tầng (`hr_management.jsp`, `hr_shift_modal.jsp`, `attendance.jsp`, `ot_reason_modal.jsp`, `main.js`):** **[DONE]**
  - Triệt tiêu 100% inline JavaScript (`onclick`, `onsubmit`, `onchange`) khỏi `web/hr_management.jsp` và `web/components/hr_shift_modal.jsp`.
  - Triệt tiêu inline JavaScript và chuẩn hóa Locale 3 tầng (`Session` -> `Cookie` -> `'vi'`) cho `web/attendance.jsp` và `web/components/ot_reason_modal.jsp`.
  - Xây dựng hệ thống Event Delegation chuyên nghiệp trong `web/js/main.js` xử lý: chuyển tab quản lý nhân sự (`data-hr-tab`), mở/đóng modal xếp ca & chấm công (`data-close-modal`, backdrop click), chuyển chế độ xếp ca đơn/hàng loạt (`data-assign-mode`), chọn nhanh các thứ trong tuần (`data-quick-select`), xác nhận xóa phân ca (`data-confirm`) và tự động submit bộ lọc ngày/tháng (`data-auto-submit`).
  - Kiểm thử biên dịch toàn bộ 74 source files bằng Apache Ant với JDK 17 đạt `BUILD SUCCESSFUL` 0 lỗi.

## 🛡️ TASK 26: KIỂM TOÁN VÀ KHẮC PHỤC LỖ HỔNG BẢO MẬT CHUYÊN SÂU (SECURITY AUDIT: XSS & FILE UPLOAD)
**Actor:** Senior Security Auditor, QA Lead & Developer
**Mô tả:** Triệt tiêu triệt để 2 lỗ hổng kinh điển XSS và ngụy trang File Upload bằng việc thoát mã dữ liệu an toàn trên 100% View và kiểm tra nhị phân Magic Bytes kết hợp HTTP Header phòng vệ.

- [x] **Task 26.1: Khắc phục Triệt để Lỗ hổng Cross-Site Scripting (XSS) trên toàn bộ Giao diện JSP (`web/`):** **[DONE]**
  - Rà soát toàn bộ các template JSP và bao bọc toàn bộ dữ liệu do người dùng nhập hoặc truy xuất từ CSDL bằng thẻ `<c:out value="..." />` hoặc `<fn:escapeXml(...)>`.
  - Chuẩn hóa trên 11 file JSP: `web/attendance.jsp` (lý do OT), `web/hr_management.jsp` (lý do OT, họ tên nhân sự), `web/dispatch_dashboard.jsp` (thông tin khách hàng, số điện thoại, địa chỉ, shipper), `web/shipper_dashboard.jsp` (thông tin khách hàng, số điện thoại, địa chỉ), `web/components/delivery_orders_table.jsp` (khách hàng, shipper, tên sản phẩm), `web/components/standard_orders_table.jsp` (khách hàng, tên sản phẩm), `web/menu.jsp` (tên sản phẩm trong alt, thẻ h3, giỏ hàng), `web/menu_management.jsp` (danh mục, tên món ăn trong alt và bảng), `web/components/order_confirm_modal.jsp` (tên thu ngân, tên sản phẩm trong modal), `web/admin_dashboard.jsp` (tài khoản, họ tên nhân sự) và `web/profile.jsp` (hồ sơ người dùng).
  - Vô hiệu hóa hoàn toàn nguy cơ Stored XSS và Reflected XSS trên toàn hệ thống.

- [x] **Task 26.2: Khắc phục Lỗ hổng Tải Tệp & Ngụy trang Định dạng (Magic Bytes & MIME Protection):** **[DONE]**
  - Bổ sung hàm kiểm tra chữ ký nhị phân `isValidMagicBytes(InputStream in, String ext)` trong `src/java/utility/ImageUploadUtility.java` kiểm tra trực tiếp byte header của file tải lên: JPEG (`FF D8 FF`), PNG (`89 50 4E 47`), WEBP (`RIFF....WEBP`), PDF (`%PDF-`), MP4 (`ftyp`), DOCX (`PK..`).
  - Tích hợp kiểm tra Magic Bytes vào quy trình `validateFile()` trước khi lưu file ra đĩa, ngăn chặn triệt để lỗ hổng kẻ tấn công đổi đuôi file webshell hoặc malware (.jsp, .exe) kết hợp giả mạo client header `Content-Type`.
  - Bổ sung header an toàn `X-Content-Type-Options: nosniff` trong `src/java/controller/ImageServingServlet.java` nhằm chặn đứng hành vi MIME-sniffing của trình duyệt đối với các tệp polyglot độc hại.
  - Kiểm thử biên dịch toàn bộ hệ thống bằng Apache Ant với JDK 17 đạt `BUILD SUCCESSFUL` 0 lỗi.

## 🧹 TASK 27: CHIẾN DỊCH TỔNG VỆ SINH & TRIỆT TIÊU NỢ KIẾN TRÚC TỒN ĐỌNG (ARCHITECTURAL PURGE & FULL COMPLIANCE)
**Actor:** AI Agent, Senior Developer & QA Lead
**Mô tả:** Khắc phục triệt để 5 nhóm vi phạm quy chuẩn `Rule_BOMS.md` vừa được kiểm toán phát hiện: chuyển 100% Inline JavaScript (61 vị trí) sang Event Delegation trong `main.js`, di chuyển hằng số static trong `AttendanceService` sang `Constants.java`, triệt tiêu hardcode chuỗi thông báo tiếng Việt trong tầng Service, vá nốt các vị trí XSS còn sót lại trên giao diện kho, và dọn dẹp các phương thức Model thừa trong Service nhằm bảo đảm nguyên tắc Zero Model Leakage.

- [x] **Task 27.1: [DONE] Triệt tiêu 100% Inline JavaScript trên toàn bộ 16 file JSP còn lại:**
  - **Mục tiêu:** Xóa sạch toàn bộ thuộc tính inline event (`onclick`, `onsubmit`, `onchange`, `oninput`) khỏi 16 file JSP và components:
    + `web/components/product_form_modal.jsp` (12 vị trí)
    + `web/components/inventory_form_modal.jsp` (10 vị trí)
    + `web/inventory_managerment.jsp` (5 vị trí)
    + `web/index.jsp` (5 vị trí)
    + `web/admin_dashboard.jsp` (4 vị trí)
    + `web/menu_management.jsp` (4 vị trí)
    + `web/header.jsp` (3 vị trí)
    + `web/components/delivery_info_modal.jsp` (3 vị trí)
    + `web/components/revert_delivery_modal.jsp` (3 vị trí)
    + `web/report.jsp` (2 vị trí)
    + `web/components/broadcast_modal.jsp` (2 vị trí)
    + `web/components/delivery_orders_table.jsp` (2 vị trí)
    + `web/components/inventory_alert_modal.jsp` (2 vị trí)
    + `web/components/staff_cancel_modal.jsp` (2 vị trí)
    + `web/profile.jsp` (1 vị trí)
    + `web/components/notification_dropdown.jsp` (1 vị trí)
  - **Giải pháp:** Chuyển đổi toàn bộ sang các thuộc tính ngữ nghĩa `data-*` (như `data-open-modal`, `data-close-modal`, `data-tab`, `data-confirm-msg`, `data-action`) và bổ sung Event Delegation tập trung trong `web/js/main.js`.

- [x] **Task 27.2: [DONE] Di dời Khai báo Biến Static ngoài Utility (`AttendanceService.java`):**
  - **Vấn đề:** 4 hằng số `private static final int` (`EARLY_CHECKIN_THRESHOLD_MINUTES`, `LATE_CHECKIN_THRESHOLD_MINUTES`, `EARLY_CHECKOUT_THRESHOLD_MINUTES`, `OVERTIME_THRESHOLD_MINUTES`) đang vi phạm điều cấm khai báo static ngoài package `utility`.
  - **Giải pháp:** Chuyển 4 hằng số này vào `src/java/utility/Constants.java` với tiền tố `ATTENDANCE_THRESHOLD_*` và cập nhật lời gọi trong `AttendanceService.java`.

- [x] **Task 27.3: [DONE] Triệt tiêu Chuỗi Text Hardcode Tiếng Việt trong Tầng Service:**
  - **Vị trí vi phạm:**
    + `AttendanceService.java`: Thông báo phân ca (dòng 369, 459), mảng thứ tiếng Việt `dayNames` (dòng 480, 588), nhãn duyệt/từ chối OT/vào ca sớm (dòng 702, 707, 737, 742).
    + `IngredientService.java`: Tên fallback danh mục `"Khác"` (dòng 215), nội dung cảnh báo kho `"Hết hàng"`, `"Sắp hết hàng"`, `"Đã hết hạn"` (dòng 305–313).
    + `OrderService.java`: Chuỗi fallback tên món `"Món #"` (dòng 353).
  - **Giải pháp:** Quy hoạch các mẫu câu thông báo thành hằng số trong `Constants.java` hoặc cơ chế đa ngôn ngữ (mã hóa Unicode Escape `\uXXXX` trong `.properties`), không để sót text tiếng Việt gõ cứng trong code Java.

- [x] **Task 27.4: [DONE] Vá Triệt để Lỗ hổng XSS Còn Sót Lại trên View (`web/`):**
  - **Vị trí còn sót:**
    + `web/inventory_managerment.jsp`: Bao bọc `<c:out value="..." />` cho `${item.ingredientName}`, `${item.categoryName}`, `${item.unit}`, `${item.storageLocation}` tại dòng 87–96.
    + `web/header.jsp`: Bao bọc `<c:out value="${sessionScope.USER_SESSION.fullName}" />` tại dòng 73.
    + `web/components/inventory_form_modal.jsp` & `product_form_modal.jsp`: Bao bọc `<c:out value="${cat.categoryName}" />` trong các thẻ `<option>`.

- [x] **Task 27.5: [DONE] Dọn dẹp Phương thức Model Cũ Chưa Dùng trong Service (Zero Model Leakage):**
  - **Vấn đề:** `AdminService.java` còn `getAllUsers()`, `getAllKeys()` và `ProductService.java` còn `getAllProducts()`, `getProductById()` trả về trực tiếp Model.
  - **Giải pháp:** Chuyển các hàm này sang phạm vi `private` hoặc xóa bỏ nếu không còn sử dụng, bảo đảm Controller chỉ có thể truy xuất qua các hàm `*ForView()` đóng gói dạng `Map<String, Object>`.

## 🚀 RELEASE 1.0: TỔNG KẾT BẢN PHÁT HÀNH & CÁC BẢN VÁ QUAN TRỌNG (HOTFIXES & HARDENING)
**Actor:** AI Agent, Security Auditor & Lead Developer  
**Mục tiêu:** Hoàn thiện đóng gói Release 1.0, tổng kết 4 phân hệ bản vá trọng yếu: Bảo mật Avatar, Điều phối Shipper, State Machine Đơn hàng, và Kiểm toán Tĩnh (Static Code Audit).

- [x] **Task 28.1: [DONE] Bảo Mật & Quản Lý Upload Avatar (`ImageUploadUtility` & `ProfileController`):**
  - Vá lỗ hổng Path Traversal: Khởi tạo tên tệp ngẫu nhiên bằng `UUID.randomUUID().toString() + ext`, loại bỏ hoàn toàn tên file từ client.
  - Xác thực chữ ký nhị phân (Magic Bytes): Kiểm tra byte đầu luồng (`FF D8 FF` cho JPEG, `89 50 4E 47` cho PNG, `RIFF...WEBP` cho WebP) qua `InputStream`, ném `ValidationException` nếu sai định dạng.
  - Đồng bộ `HttpSession`: Bổ sung `session.setAttribute(Constants.USER_SESSION, loginUser)` để ép cập nhật giao diện ngay lập tức mà không cần đăng nhập lại.
  - Migration Script: Tạo `DATABASE/BOMS_DATABASE_2.sql` bổ sung cột `Avatar VARCHAR(255) NULL` an toàn cho các môi trường DB cũ.

- [x] **Task 28.2: [DONE] Chuẩn Hóa Nghiệp Vụ Điều Phối Shipper (`DeliveryService`):**
  - Khắc phục lỗi đếm sai `ActiveOrders`: Đảo thứ tự trong `rejectTask`, thực thi cập nhật CSDL gỡ `ShipperID = NULL` và đưa trạng thái về `Waiting for dispatch` trước khi gọi `syncShipperProfileStatus`.
  - Hàm `countActiveOrdersByShipperId` đếm chính xác các đơn thực sự trong tiến trình giao (`Delivering`, `Waiting for delivery`), triệt tiêu lỗi tụt số đơn hoạt động (3/3 tụt về 2/3) khi shipper từ chối đơn mới.
  - Dọn dẹp kiểm tra dung lượng thừa ở `acceptTask`, chuẩn hóa `Constants.MAX_SHIPPER_ORDERS = 3`.

- [x] **Task 28.3: [DONE] Khóa State Machine Vòng Đời Đơn Hàng (`OrderService` & View):**
  - Chốt chặn Backend: `OrderService.updateOrderStatus` kiểm tra đơn qua `orderDAO.getOrderById(orderId)`. Chặn tuyệt đối cập nhật trên đơn `Delivery` đang do Shipper xử lý (`Waiting for delivery`, `Picked up`, `Delivering`) với mã lỗi `E0224` (`ERROR_DELIVERY_CONTROLLED_BY_SHIPPER`).
  - Chặn tuyệt đối cập nhật trên đơn đã ở trạng thái kết thúc (`Delivered`, `Paid`, `Returned`, `Cancelled`) với mã lỗi `E0225` (`ERROR_ORDER_CLOSED_STATUS`).
  - Khóa UI View: Cột Action của `standard_orders_table.jsp` và `delivery_orders_table.jsp` sử dụng `<c:choose>` để khóa và ẩn form dropdown đối với đơn thuộc Shipper (`🔒 Đang do Shipper xử lý`) và đơn đã kết thúc (`🔒 Đã hoàn tất`).

- [x] **Task 28.4: [DONE] Tổng Kiểm Toán Tĩnh Mã Nguồn & Tuân Thủ 100% Rule_BOMS (Static Audit):**
  - Triệt tiêu 100% khối catch rỗng (Zero Empty Catch): Rà soát và xử lý toàn bộ các khối catch rỗng tại `OrderDAO`, `UserDAO`, `DashboardController`, `NotificationController`, `AttendanceService`.
  - Vá lỗ hổng rò rỉ Transaction (Transaction Leak Prevention): Bổ sung `catch (Exception e)` trong `OrderDAO` đảm bảo bắt buộc gọi `connection.rollback()` cho mọi lỗi Runtime; tích hợp `Logger` khi rollback/restore auto-commit.
  - Dọn dẹp biến static: Di dời toàn bộ hằng số static trong `AttendanceService` sang `Constants.java`. Đạt chuẩn **0 static fields** ngoài `utility`.
  - Gom chuỗi hardcode: Sử dụng `String.format(Constants.NOTIF_..., ...)` và `String.format(Constants.INVENTORY_ALERT_..., ...)`, tích hợp `dayKey` i18n cho thời khóa biểu tuần. Toàn bộ dự án đạt chuẩn i18n 100%.



