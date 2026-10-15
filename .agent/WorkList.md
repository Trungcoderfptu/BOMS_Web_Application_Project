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