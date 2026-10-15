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
## [v0.0.3] - 2026-10-07.
### Added
* **Admin:**Đa thêm bảng điều khiển cho admin với chức năng khóa tìa khoản và cấp mã bảo mật cho nhân viên mới(Mã bảo mật bao gồm role của nhân viên đó).`(@Trung)`
 **Đăng ký tài khoản:** Đã thêm Luồng đăng ký tài khoản cần sử dụng mã bảo vệ do admin tạo để đăng ký tài khoản. `(@Trung)`
* **Giao diện:** Thêm 1 số cái popup lung tung. `(@Trung)`
* **Giao diện:** Giao diện cho bảng điều khiển admin. `(@Trung)`
* **Chức năng:** Đã thêm chức năng tạo mã nhân viên mới ở bảng điều khiển admin. `(@Trung)`


### Fixed 
* Các lỗi về trải nghiệm người dùng đã được khắc phục í dụ khi nhập đăng ký mà nhập sai thì dữ liệu vẫn còn không cần nhập lại.
* Cấu trúc code đã được băm nhỏ nhất có thể ở tầng service thể hiển tính đơn trách nhiệm của từng hàm.
* Các thông báo chuỗi lỗi do backend đẩy lên đã được đổi thành các mã lỗi để jsp dùng jstl(C:if) phân loại và gán vào hệ thống đa ngôn ngữ để đồng bộ với hệ thống.

### Notes
* Tài liệu doc dự án đã update hãy đọc để biết thêm rule dự án.
* Tại phiên bản này gần như bộ khung admin login rigister đã sẵn sàng chuẩn bị cho việc xóa fake database hardcode sẽ tiến hành ở phiên bản 0.0.4
---
## [v0.0.4] - 2026-10-08.
### Added
none


### Fixed 
* đã thêm database thay cho fake database của lồng user admin login register

### Notes
* Tài liệu doc dự án đã update hãy đọc để biết thêm rule dự án.
* đây chỉ là 1 bản nhỏ yêu cầu tất cả phải tự thiết lập microsoft sql management và đảm bảo đẫ sẵn sàng để cài jdbc đọc tài liệu dự án để nắm rõ
---
# [v0.0.5] - 2026-10-13.
### Added
* **Staff:**Đã thêm chức năng quản lí đơn hàng cho staff gồm xem đơn hủy đơn đặt đơn tại quầy và cập nhật trạng thái đơn. đồng thời trừ kho tự động với các thao tác liên quan đến nguyên liệu của nhân viên quầy.`(@Trung)`
 **Customer:** Đã có giao diện đặt hàng dùng chung với staft khác nhau về cách hiển thị để đặt hàng và chốt đơn sẽ gửi về chỗ staff. `(@Trung)`
* **Giao diện:** thêm menu, giỏ hàng và màn hình quản lí đơn. `(@Trung)`
* **database:** update cơ sở dữ liệu cho đăng nhập. `(@Trung)`


### Fixed 
* đã fix các hardcode cũ của jsp

### Notes
* Tài liệu doc dự án đã update hãy đọc để biết thêm rule dự án.
* đã thêm các tài liệu mới hãy xem .agent
---
# [v0.0.6] - 2026-10-15.
### Added
* **Báo cáo ẩn danh (UC-07):** Nhân viên gửi báo cáo ẩn danh kèm bằng chứng (JPG, PNG, MP4, PDF, DOCX; tối đa 5 file, 20 MB/file). Hệ thống không lưu danh tính người gửi, trả về Ticket ID để tra cứu phản hồi và gửi thông báo cho Manager phụ trách loại báo cáo. `(@Duong)`
* **Quản lý báo cáo (UC-08):** Manager/Admin xem danh sách (lọc theo Trạng thái, Ngày, Loại), xem chi tiết, tải bằng chứng, cập nhật trạng thái In Progress / Resolved / Rejected kèm phản hồi. Bắt buộc nhập lý do khi từ chối. `(@Duong)`

### Fixed
* None

### Notes
* File bằng chứng được lưu NGOÀI dự án tại thư mục khai báo trong `web/WEB-INF/Upload.properties` (mặc định `C:/BOMS_uploads/reports`). Ai muốn đổi ổ đĩa thì sửa file này, thư mục sẽ tự tạo khi gửi báo cáo đầu tiên.
* Không cần chạy lại SQL: chức năng dùng các bảng `ReportCategories`, `CategoryManagers`, `AnonymousReports`, `ReportEvidences`, `Notifications` đã có sẵn.
* Manager chỉ thấy báo cáo của loại được phân công trong bảng `CategoryManagers`; Admin thấy tất cả.
---
