# BINGXUE OPERATING MANAGER SYSTEM (BOMS)
---
## [v0.0.1] - 2026-09-29.
### Added
* **Cài đặt:**Hoàn thiện luồng đăng nhập (Login) và đăng xuất (Logout). `(@Trung)`
* ** Cài đặt:** Thêm khung thiết lập cơ bản bao gồm đổi mật khẩu và thay đổi ngôn ngữ. `(@Trung)`
* **Giao diện:** Tích hợp giao diện chế độ tối (Dark Mode). `(@Trung)`

### Fixed 
* None

### Notes
* None

---
## [v0.0.2] - 2026-09-30.
### Added
* **Cài đặt:**Đa loại bỏ tab thay đổi nogno ngữ khỏi cài đặt và đưa ra ngoài màn hình chính.`(@Trung)`
 **Filter:** Đã thêm filter cho việc kiểm soát phiên đăng nhập, filter cho việc encoding toàn bộ dự án về UTF 8 `(@Trung)`
* **Giao diện:** Đã xóa daskboard.jsp thay vào đó là index.jsp 1 trang chủ cho tát cả có thể vào mà không cần đăng nhập. có thê vào bằng cách bấm vào mục home trên menu thả xuống của header. `(@Trung)`
* **Giao diện:** Đã thêm giao diện đăng ký đã được thiết lập nhưng chưa có backend. `(@Trung)`


### Fixed 
* Lỗi mã hóa phông cho biểu tượng mặt trang mặt trời của nút darkmode đã được khắc phục trong main.js(các icon phải sử dụng mã hóa chuẩn utf 8 để tránh lỗi hiển thị).
* đã khắc phục lỗi filter khi truy cập vào trang web bằng link mặc định và bị điều hướng vè logic.jsp. giờ đây link mặc định sẽ dẫn vào index.jsp

### Notes
* Tài liệu doc dự án đã update hãy đọc để biết thêm rule dự án.
---