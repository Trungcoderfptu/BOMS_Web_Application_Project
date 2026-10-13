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
- [ ] **Task 2.2: Khởi tạo phiên qua QR (Bước 1):** Xây dựng Controller hứng URL có chứa tham số bàn (ví dụ: `/menu?tableId=5`). Lưu `tableId` này vào Session để gắn tự động vào đơn hàng khi lưu DB.
- [ ] **Task 2.3: Xử lý giao diện hết hàng (Exception 2.0.E1):** Làm mờ (gray out) hoặc ẩn nút "Thêm vào giỏ" đối với các sản phẩm có số lượng = 0 để khách không thể chọn.
- [ ] **Task 2.4: Tích hợp Cổng thanh toán (Bước 6, 7):** Xây dựng `PaymentController` điều hướng giỏ hàng sang bên thứ 3 (VNPay/MoMo) khi khách bấm nút "Thanh toán Online".
- [ ] **Task 2.5: Xử lý ngoại lệ thanh toán (Exception 2.0.E2):** Hứng kết quả trả về từ Cổng thanh toán.
  - *Nếu lỗi/hủy:* Trả về UI báo lỗi, giữ nguyên Session giỏ hàng.
  - *Nếu thành công:* Gọi logic chốt đơn (Task 1.4) và lưu DB với trạng thái `Đã thanh toán / Chờ xác nhận`.
- [ ] **Task 2.6: Tab Theo Dõi Lịch Sử Đơn Hàng (Alt Flow 2.1):** 
  - Tạo một Tab mới hiển thị danh sách đơn mà khách vừa chốt, fetch real-time các trạng thái: `Chờ xác nhận`, `Xác nhận`, `Đang làm`, `Đã hoàn thành`.
  - Cung cấp nút **[Hủy đơn / Sửa đơn]** chỉ hiển thị khi đơn ở trạng thái `Chờ xác nhận` (Bếp chưa làm).
  - Tự động reset/ẩn các đơn `Đã hoàn thành` khỏi màn hình của khách sau 10 phút để dọn dẹp phiên cho khách mới ngồi vào bàn.