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
# [v0.0.6] - 2026-10-13.
### Added
* **Staff and manager:**Đã thêm chức năng quản lí thực đơn giúp thêm món, cập nhật món, xóa món.`(@Trung)`
 **Chức năng:** Đã bổ xung upload ảnh vào dự án, ảnh lưu ở C/User/Tên máy bạn/. `(@Trung)`
* **Giao diện:** Thêm màn hình điều khiển quản lí thực đơn cho staff và manager. `(@Trung)`
* **DATABASE:** update cơ sở dữ liệu JSTL cho toàn bộ luồng use case 1 và 2 cùng với tự động trừ kho và các hàm hỗ trợ cho tác vụ nghiệp vụ của đơn hàng phải tương tác với dữ liệu nguyên liệu. `(@Trung)
`


### Fixed 
* đã fix các hardcode cũ của jsp
* các chuỗi mã còn thiếu và lỗi mã hóa đã được fix

### Notes
Phải chạy lại file .sql trong thư mục DATABASE mỗi khi update dự án
Các chuỗ trong file đa ngôn ngữ .prototies đặc biệt bản việt phải dùng bộ mã hóa encode unicode utf8 từ jdk 9 trở lên
xem file .agent/learning.md để nắm rõ thay đổi chi tiết
thêm file QR code.md để lưu các link QR truy cập nhanh
---
# [v0.0.7] - 2026-10-16.
### Added
* **Báo cáo ẩn danh (UC-07):** Nhân viên gửi báo cáo ẩn danh kèm bằng chứng (JPG, PNG, MP4, PDF, DOCX; tối đa 5 file, 20 MB/file). Hệ thống không lưu danh tính người gửi, trả về Ticket ID để tra cứu phản hồi và gửi thông báo cho Manager phụ trách loại báo cáo. `(@Duong)`
* **Quản lý báo cáo (UC-08):** Manager/Admin xem danh sách (lọc theo Trạng thái, Ngày, Loại), xem chi tiết, tải bằng chứng, cập nhật trạng thái In Progress / Resolved / Rejected kèm phản hồi. Bắt buộc nhập lý do khi từ chối. `(@Duong)`

### Fixed
* None

### Notes
* File bằng chứng lưu chung thư mục dữ liệu `~/boms_data` với ảnh món (qua `ImageUploadUtility`), nhưng ở vùng riêng tư `boms_data/private/reports/`: KHÔNG mở được qua link `/uploads/`, chỉ Manager/Admin tải được qua `EvidenceController`. Không cần cấu hình gì thêm.
* Không cần chạy lại SQL: chức năng dùng các bảng `ReportCategories`, `CategoryManagers`, `AnonymousReports`, `ReportEvidences`, `Notifications` đã có sẵn.
* Manager chỉ thấy báo cáo của loại được phân công trong bảng `CategoryManagers`; Admin thấy tất cả.
* nhớ trước khi nộp lên git thì chạy lệnh này ở terminal tại thư mục gốc dự án: del /s *.class để tránh lỗi khi merge nhánh
---
# [v0.0.8] - 2026-10-19.
### Added
* **CẬp nhật UC-03 và UC-04** các chức năng liên quan đến kho bãi. `(@Đạt)`
* **Gộp nhóm đạt thành công:**Đã kiểm thử và gộp thành công nhánh đạt `(@Trung)`

### Fixed
* đã fix cơ bản tính năng đặt món khi đơn hàng vượt quá giới hạn hàng có trông kho(mức tạm chấp nhận)
* đã xóa bỏ addmin ra khỏi luồng làm việc của kho

### Notes
* File SQL đã thêm 1 tí thay đổi hãy chạy lại SQL mỗi khi nâng cấp phần mềm
* nhớ trước khi nộp lên git thì chạy lệnh này ở terminal tại thư mục gốc dự án: del /s *.class để tránh lỗi khi merge nhánh
---
# [v0.0.9] - 2026-10-19.
### Added
* **Gộp nhóm hoài thành công:** Đã kiểm thử và gộp thành công nhánh hoài `(@Trung)`

### Fixed
* Chưa fix còn lỗi của phần hoài
---
# [v0.10] - 2026-10-19.
### Added
* **Chuẩn hóa phân quyền Menu Header (header.jsp):** Phân chia rõ ràng ranh giới truy cập giao diện theo 4 vai trò của dự án:
  - **Admin:** Chỉ duy nhất phụ trách Quản lý tài khoản (`AdminDashboardController`), tách hoàn toàn khỏi luồng kho bãi và báo cáo.
  - **Manager:** Quản lý đơn hàng, Quản lý thực đơn (thêm/sửa món), Quản lý kho (xem/thêm/sửa), Quản lý báo cáo ẩn danh và Quản lý nhân sự.
  - **Staff:** Đặt món tại quầy POS, Quản lý đơn hàng, Tạo thực đơn, Xem kho, Chấm công và Gửi báo cáo ẩn danh.
  - **Shipper:** Chỉ thực hiện luồng giao hàng của Shipper. `(@Trung)`

### Fixed
* **Khắc phục lỗi cú pháp servlet trong `web.xml` (Fix dứt điểm lỗi phần Hoài):** Tách thẻ `<servlet>` và `<servlet-mapping>` bị chập gộp giữa `AttendanceController` (nhánh Hoài) và `InventoryManagementController` (nhánh Đạt) thành 2 định nghĩa servlet độc lập, đảm bảo hệ thống deploy an toàn trên Tomcat 10.
* **Dọn dẹp liên kết và tối ưu trải nghiệm (UX):**
  - Xóa bỏ hoàn toàn các thẻ duplicate và link rác (`href="#"`) của Báo cáo và Nhân sự.
  - Loại bỏ chuông cảnh báo kho cho Admin (chỉ Staff và Manager nhận thông báo).
  - Khóa liên kết Chấm công trong dropdown tài khoản cá nhân, chỉ hiển thị riêng cho Staff.

### Notes
* Dự án biên dịch thành công 0 lỗi qua NetBeans Ant (`BUILD SUCCESSFUL`).
* Nhớ trước khi nộp lên git thì chạy lệnh này ở terminal tại thư mục gốc dự án: `del /s *.class` để tránh lỗi khi merge nhánh.
---