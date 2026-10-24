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
# [v0.1.0] - 2026-10-20.
### Added
* **Tích hợp toàn diện luồng Giao hàng & Điều phối Shipper (UC-09 & UC-10):**
  - **Nền tảng Dữ liệu & Model:** Bổ sung các entity model `ShipperProfile`, `DeliveryArea`, `DeliveryStatusHistory` khớp 100% CSDL SQL Server; chuẩn hóa các trạng thái vận chuyển (`Waiting for dispatch`, `Waiting for delivery`, `Picked up`, `Delivering`, `Delivered`, `Returned`) theo ràng buộc `CK_Order_Status`.
  - **Modal Thu thập Thông tin Giao hàng (`delivery_info_modal.jsp`):** Khi nhân viên chuyển đơn sang trạng thái giao hàng trên `order_management.jsp`, hệ thống hiển thị modal thu thập tên người nhận, số điện thoại, địa chỉ và chọn khu vực giao hàng (`DeliveryAreas`).
  - **Phân tách Tab Đơn hàng Giao cho Shipper:** Thiết kế tab chuyên biệt "Đơn giao Shipper" trên `order_management.jsp`, tách component bảng `standard_orders_table.jsp` và `delivery_orders_table.jsp` hiển thị đầy đủ thông tin khách nhận, Shipper phụ trách và trạng thái vận chuyển, tích hợp bộ đếm đơn thời gian thực (`ORDER_COUNTS`).
  - **Cơ chế Điều phối & Bể Đơn Chờ Nhận (Hybrid Dispatching & Claim Pool):** Quản lý chủ động gán Shipper từ `dispatch_dashboard.jsp`; đồng thời Shipper có thể tự xem và bấm nhận đơn (`claimTask`) theo khu vực phụ trách trên `shipper_dashboard.jsp` với câu lệnh cập nhật nguyên tử (atomic update) chống tranh chấp dữ liệu (race condition).
  - **Quy trình Shipper Nhận/Từ chối đơn:** Shipper bấm nhận đơn (`acceptTask`) chuyển sang `Delivering`, tự động tăng số đơn đang giao; Shipper từ chối đơn (`rejectTask`) chuyển về `Waiting for dispatch` (gỡ Shipper để điều phối lại) và ghi nhận lý do từ chối vào lịch sử.
  - **Bằng chứng Giao hàng & Lý do Thất bại:** Tích hợp tải ảnh bằng chứng giao hàng khi Shipper hoàn thành (`Delivered`) qua `shipper_complete_modal.jsp` (lưu tại `~/boms_data/uploads/delivery/`); bắt buộc nhập lý do thất bại qua `shipper_fail_modal.jsp` khi hoàn hàng (`Returned`); tự động giải phóng trạng thái Shipper về `'Ready'` khi hết đơn đang giao. `(@Trung)`
* **Chuẩn hóa phân quyền Menu Header (header.jsp):** Phân chia rõ ràng ranh giới truy cập giao diện theo 4 vai trò của dự án:
  - **Admin:** Chỉ duy nhất phụ trách Quản lý tài khoản (`AdminDashboardController`), tách hoàn toàn khỏi luồng kho bãi và báo cáo.
  - **Manager:** Quản lý đơn hàng, Quản lý thực đơn (thêm/sửa món), Quản lý kho (xem/thêm/sửa), Quản lý báo cáo ẩn danh, Quản lý điều phối giao hàng (`/DispatchController`) và Quản lý nhân sự.
  - **Staff:** Đặt món tại quầy POS, Quản lý đơn hàng, Tạo thực đơn, Xem kho, Chấm công và Gửi báo cáo ẩn danh.
  - **Shipper:** Bàn làm việc Shipper (`/ShipperController`) tiếp nhận và cập nhật đơn hàng giao. `(@Trung)`

### Fixed
* **Khắc phục triệt để lỗi khóa cứng thực đơn & sập luồng đặt món khi vượt tồn kho:**
  - Tách biệt hoàn toàn giữa quyền quản trị danh mục món (`Products.IsAvailable`) và kiểm tra tồn kho động (`Dynamic Stock Check` qua `hasSufficientStockForProduct`).
  - Trong `CartService`, khi khách đặt vượt tồn kho, chỉ ném thông báo lỗi để điều chỉnh số lượng giỏ hàng mà KHÔNG can thiệp ghi đè `IsAvailable = 0` vào CSDL.
  - Trong `OrderService`, khi hủy đơn hoàn trả kho nguyên liệu, KHÔNG tự ý ghi đè `IsAvailable = 1` làm mất trạng thái quản trị mà Quản lý đã cài đặt trước đó.
* **Sửa lỗi logic trạng thái đơn khi Shipper từ chối:** Chuyển trạng thái về `Waiting for dispatch` thay vì `Cancelled`, ngăn chặn triệt để xung đột kích hoạt nhầm logic hoàn kho của quầy/bếp.
* **Chuẩn hóa kiến trúc PRG (Post-Redirect-Get) & Session Flash Message:** Loại bỏ việc gọi trực tiếp `doGet` sau `doPost` trong `DispatchController`, `ShipperController`, `OrderManagementController`, ngăn ngừa lỗi gửi lại dữ liệu form khi người dùng nhấn F5.
* **Khắc phục lỗi cú pháp servlet trong `web.xml` (Fix dứt điểm lỗi phần Hoài):** Tách thẻ `<servlet>` và `<servlet-mapping>` bị chập gộp giữa `AttendanceController` (nhánh Hoài) và `InventoryManagementController` (nhánh Đạt) thành 2 định nghĩa servlet độc lập, đảm bảo hệ thống deploy an toàn trên Tomcat 10.
* **Dọn dẹp liên kết và tối ưu trải nghiệm (UX):**
  - Xóa bỏ hoàn toàn các thẻ duplicate và link rác (`href="#"`) của Báo cáo và Nhân sự.
  - Loại bỏ chuông cảnh báo kho cho Admin (chỉ Staff và Manager nhận thông báo).
  - Khóa liên kết Chấm công trong dropdown tài khoản cá nhân, chỉ hiển thị riêng cho Staff.
* **Đồng bộ hóa 100% Đa ngôn ngữ & Mã hóa Unicode Escape:** Toàn bộ nhãn mới và mã lỗi/thành công (`S0211`, `E0213`-`E0217`) trong `messages_vi.properties` được mã hóa chuẩn `\uXXXX` thuần ASCII.

### Notes
* File SQL `BOMS_DATABASE.sql` sử dụng các bảng `ShipperProfiles`, `DeliveryAreas`, `DeliveryStatusHistory` đã có sẵn trong cơ sở dữ liệu.
* Thư mục ảnh bằng chứng giao hàng lưu tại `~/boms_data/uploads/delivery/` nằm ngoài thư mục build, phục vụ qua ánh xạ `/uploads/*`.
* Dự án biên dịch thành công 0 lỗi qua NetBeans Ant (`BUILD SUCCESSFUL`).
* Nhớ trước khi nộp lên git thì chạy lệnh này ở terminal tại thư mục gốc dự án: `del /s *.class` để tránh lỗi khi merge nhánh.
---
# [v0.1.1] - 2026-10-20.
### Added
* **Cơ chế Hoàn tác An toàn Đơn Giao hàng (Safe Revert Delivery Order - Task 6.7):**
  - Bổ sung modal cảnh báo xác nhận hoàn tác (`revert_delivery_modal.jsp`) trên `order_management.jsp` khi nhân viên chuyển ngược trạng thái đơn từ Giao hàng về phục vụ nội bộ.
  - Tích hợp kiểm tra an toàn: Chặn hoàn tác nếu đơn hàng đang giao hoặc đã giao (`Delivering`, `Delivered`, `Returned`).
  - Tự động phục hồi `OrderType` gốc (`Table` nếu có mã bàn, `Counter` nếu tại quầy), giải phóng Shipper (`ShipperID = NULL`), ghi vết biến động vào `DeliveryStatusHistory` và đồng bộ lại số đơn hoạt động (`ActiveOrders`) của Shipper. `(@Trung)`
* **Quy hoạch Bàn giao & Chuẩn hóa Luồng Shipper (Task 7):**
  - Bổ sung ghi chú bàn giao nghiêm ngặt và checklist công việc vào `.agent/WorkList.md`.
  - Bổ sung **Task 7.7** để quy hoạch toàn bộ hệ thống mã thông báo của luồng Shipper về dải mã số chuẩn `S060X` (thành công) và `E060X` (lỗi nghiệp vụ) theo đúng quy chuẩn kiến trúc `Rule_BOMS.md`. `(@Trung)`

### Fixed
* **Tiêu diệt Model ảo `DeliveryTask.java` & Chặn rò rỉ Model lên View (Task 7.2):** Xóa bỏ hoàn toàn file `src/java/model/DeliveryTask.java`. Tái cấu trúc 100% các hàm trong `DeliveryDAO` và `DeliveryService` sang dùng `Map<String, Object>` và `Map<String, String>`, loại bỏ nguy cơ ô nhiễm tầng Model và ngăn chặn rò rỉ Model trực tiếp lên giao diện JSP.
* **Xóa bỏ Reflection sai quy chuẩn kiến trúc (Task 7.1):** Xóa sạch 30 dòng mã Reflection trong `IngredientAlertController.java`, thay thế bằng phương pháp ép kiểu `Map<String, String>` chuẩn từ Session an toàn và đồng bộ với hệ thống.
* **Đồng bộ hóa Đa ngôn ngữ:** Cập nhật mã thành công `S0212` (thu hồi đơn giao hàng) vào `messages_vi.properties` (chuẩn mã hóa `\uXXXX`) và `messages_en.properties`.

### Notes
* Model ảo `DeliveryTask.java` đã bị xóa vĩnh viễn khỏi dự án. Thành viên tiếp nhận luồng Shipper tuyệt đối không tạo lại class này, mọi thao tác danh sách đơn giao bắt buộc sử dụng Map.
* Toàn bộ mã nguồn biên dịch thành công 0 lỗi (`BUILD SUCCESSFUL`).
* Nhớ trước khi nộp lên git thì chạy lệnh này ở terminal tại thư mục gốc dự án: `del /s *.class` để tránh lỗi khi merge nhánh.
---
# [v0.9.0] - 2026-10-23.
### Added
* **Bảng Điều Khiển Trung Tâm (Central Control Dashboard - Task 12):**
  - Cải tạo toàn diện trang chủ `index.jsp` từ trang tĩnh thành Trung tâm điều khiển hiện đại, phân quyền hiển thị theo 4 vai trò (Admin, Manager, Staff, Shipper).
  - Tích hợp thẻ thông tin cá nhân (Profile Card) và Huy hiệu trạng thái làm việc động (Work Status Badge) phản ánh chính xác tình trạng ca làm hôm nay (🟢 Đang trong ca, 🟡 Chưa vào ca, ⚪ Đã hoàn thành, 🔴 Không có ca).
  - Lưới thao tác nhanh (Quick Actions Grid) hỗ trợ điều hướng tức thì tới các phân hệ nghiệp vụ theo quyền hạn: POS, Quản lý đơn hàng, Quản lý kho, Quản lý thực đơn, Phân ca & Nhân sự, Điều phối giao hàng, Quản trị tài khoản, Phát loa toàn cục và Báo cáo ẩn danh.
  - Bảng tin thông báo mới nhất (Notification Feed) hiển thị các thông báo gần đây, hỗ trợ đánh dấu đã đọc trực tiếp. Landing Banner chuyên nghiệp dành riêng cho khách chưa đăng nhập. `(@Trung)`
* **Hệ thống Thông Báo Thời Gian Thực & Phát Loa Toàn Cục (Notification & Broadcast - Task 11 & Task 17.2):**
  - Tích hợp Quả chuông 🔔 trên thanh điều hướng Header kèm Badge đỏ đếm số lượng thông báo chưa đọc theo thời gian thực (tự động cập nhật ngầm qua AJAX mỗi 30 giây).
  - Hộp thông báo đa tầng (Notification Dropdown) hiển thị danh sách thông báo kèm biểu tượng trực quan (`📢`, `🗓️`, `⏱️`, `⚠️`, `🛵`, `🔔`), hỗ trợ đánh dấu đã đọc từng tin và đánh dấu tất cả đã đọc mượt mà không cần tải lại trang.
  - Chức năng Phát loa toàn cục 📢 (`broadcast_modal.jsp`) dành riêng cho Admin và Manager gửi thông báo khẩn cấp tức thời tới toàn bộ nhân sự đang hoạt động trong hệ thống.
  - Tự động phát sinh thông báo khi có lịch phân ca mới, khi Quản lý duyệt/từ chối công/OT, và tự động đồng bộ mọi cảnh báo kho mới (`InventoryAlert`: Hết hàng, Sắp hết hàng, Quá hạn, Sắp hết hạn) gửi thẳng tới toàn bộ Quản lý và Nhân viên. `(@Trung)`
* **Hệ thống Chấm Công & Quản Lý Ca Làm Việc Chặt Chẽ (Time Attendance & HR Shifts - Task 10 & Task 13):**
  - **Khống chế khung giờ chấm công chặt chẽ (Phương án A):** Khóa nút Check-in nếu trước giờ bắt đầu ca > 15 phút; vào ca sớm trong vòng 15 phút ghi nhận trạng thái `Pending Early Approval` chờ Quản lý duyệt; khóa nút Check-out khi chưa hết giờ ca; tan ca muộn > 15 phút bắt buộc nhập lý do làm thêm giờ (OT) và ghi nhận `Pending OT Approval`. Bảng tổng hợp công tháng (`Timesheet Summary`) tự động chỉ tính công các ca đúng giờ hoặc đã được duyệt.
  - **Bàn làm việc Phân ca & Quản lý Nhân sự (`hr_management.jsp`, `hr_shift_modal.jsp`):** Cho phép Quản lý theo dõi lịch ca của toàn bộ nhân viên theo tuần/tháng, phân ca làm việc mới, sửa giờ ca, và duyệt/từ chối nhanh chóng các yêu cầu vào sớm và yêu cầu OT. `(@Trung)`
* **Nâng Cấp Quyền Lực Quản Trị Viên Toàn Diện (Super Admin Dashboard - Task 16):**
  - Bổ sung hiển thị cột "Ngày gia nhập" (`HireDate`) trên bảng danh sách người dùng.
  - Bổ sung Tab chuyên biệt "Cấp lại mật khẩu" (`tab-passwords`) cho phép Super Admin trực tiếp thiết lập lại mật khẩu cho bất kỳ tài khoản nào trong hệ thống (chặn Admin tự reset mật khẩu của chính mình tại đây).
  - Bổ sung tính năng Thay đổi vai trò nhân sự trực tiếp (`Change Role`) qua Dropdown trên bảng User; tự động khởi tạo hồ sơ vận chuyển `ShipperProfiles` nếu chuyển vai trò sang `Shipper`; bảo vệ an toàn luồng giao hàng bằng cơ chế chặn đổi vai trò nếu Shipper đang có đơn hàng dở dang (`ActiveOrders > 0`).
  - Bổ sung chức năng Thu hồi / Khóa mã `Security Key` chưa sử dụng (`Revoke Key`) trên bảng quản lý mã bảo mật. `(@Trung)`
* **Quản Lý Danh Mục Động Cho Thực Đơn & Kho Nguyên Liệu (Phương án 6A - Task 18):**
  - Giải phóng việc fix cứng danh mục trong cơ sở dữ liệu ở cả hai phân hệ Thực đơn (`MenuCategories`) và Kho nguyên liệu (`IngredientCategories`).
  - Tích hợp nút "Quản lý danh mục" trực tiếp trên thanh công cụ của `menu_management.jsp` và `inventory_managerment.jsp`.
  - Hộp thoại Modal quản lý danh mục (`menuCategoryModal`, `inventoryCategoryModal`) hỗ trợ: Thêm danh mục mới (kiểm tra rỗng, độ dài tối đa 100 ký tự và chống trùng tên); Hiển thị danh sách danh mục hiện có kèm huy hiệu trạng thái ("Đang có món" / "Đang có nguyên liệu"); Cơ chế Xóa an toàn chỉ cho phép xóa các danh mục rỗng (chưa chứa món ăn hoặc nguyên liệu nào).
  - Tích hợp điều hướng PRG kèm tham số `?manageCategory=true` tự động giữ mở modal sau khi thêm hoặc xóa danh mục để Quản lý nắm bắt danh sách cập nhật tức thì. `(@Trung)`
* **Tự Động Dọn Dẹp Lô Hết Hạn & Đóng Cảnh Báo Kho (Task 17.1):**
  - Tích hợp cơ chế tự động đưa số lượng tồn của các lô quá hạn cũ trong `IngredientBatches` về 0 và đóng (`Closed`) toàn bộ cảnh báo `Expired` / `Expiring Soon` khi Quản lý lưu cập nhật thông tin nguyên liệu.
  - Khắc phục triệt để hiện tượng cảnh báo zombie hết hạn tái phát liên tục khi nguyên liệu đã được nhập mới. Đồng thời tự động đóng các cảnh báo thiếu hụt kho khi số lượng tồn đạt ngưỡng an toàn. `(@Trung)`

### Fixed
* **Khắc phục lỗi hiển thị nhãn Security Key:** Sửa lỗi chuỗi chưa dịch `???admin.dashboard.status.unused???` trên Tab Security Key trang Quản trị viên, chuẩn hóa thành `Chưa gán` (VI) và `Unassigned` (EN).
* **Khắc phục lỗi cú pháp đóng khối tại Controllers:** Sửa triệt để lỗi dư thừa dấu ngoặc đóng khối trong `MenuManagementController.java` và `InventoryManagementController.java`.
* **Tiêu diệt hiện tượng rò rỉ Model (Zero Model Leakage):** Toàn bộ dữ liệu hiển thị từ tầng DAO và Service chuyển giao lên Controllers và View đều được chuẩn hóa 100% qua `Map<String, Object>`, tuyệt đối không để lộ thực thể Model Entity lên tầng giao diện người dùng.
* **Đồng bộ hóa 100% Đa ngôn ngữ & Mã hóa Unicode Escape:** Bổ sung toàn bộ dải mã trạng thái thành công (`S0213`-`S0214`, `S0405`-`S0406`, `S0801`-`S0803`, `S9000`-`S9003`) và mã lỗi chuẩn (`E0219`-`E0220`, `E0409`-`E0411`, `E0801`-`E0802`, `E9000`-`E9005`, `DE0061`-`DE0062`) vào `messages_vi.properties` (mã hóa chuẩn `\uXXXX` thuần ASCII) và `messages_en.properties`.

### Notes
* CSDL hệ thống sử dụng các bảng `Notifications`, `ShiftAssignments`, `TimeAttendance`, `InventoryAlerts`, `IngredientBatches`, `AccountManager`, `ShipperProfiles`. Hãy chạy file `BOMS_DATABASE.sql` trong thư mục `DATABASE` nếu cài đặt môi trường mới.
* Mọi thao tác JDBC đều tuân thủ cơ chế `try-with-resources` đóng an toàn kết nối, không sử dụng `System.out.println` trong tầng DAO.
* Toàn bộ mã nguồn dự án biên dịch thành công tuyệt đối 0 lỗi qua NetBeans Ant (`BUILD SUCCESSFUL`, 75 source files).
* Nhớ trước khi nộp lên git thì chạy lệnh này ở terminal tại thư mục gốc dự án: `del /s *.class` để tránh lỗi khi merge nhánh.
---
## [v1.0.0-beta1] - 2026-10-23.
### Added
* **Bảo vệ Phân quyền Quản trị Tập trung & Phòng Chống Tự Khóa (Task 19.1 - Phương án A):**
  - Tích hợp toàn bộ nghiệp vụ tạo mã bảo mật (`generateKey`) và khóa/mở khóa tài khoản (`toggleUser`) vào phương thức `doPost` của `AdminDashboardController.java`, bảo vệ tập trung dưới phân quyền `Admin`.
  - Cơ chế tự vệ logic: Ngăn chặn tuyệt đối việc Admin tự khóa tài khoản của chính mình (`adminUserId == targetUserId`), chống nguy cơ vô tình làm tê liệt quyền quản trị tối cao của hệ thống. `(@Trung)`
* **Mở Thông Luồng Khách Vãng Lai Trải Nghiệm Hệ Thống (Task 19.2):**
  - Bổ sung `DashboardController` vào danh sách ngoại lệ bảo mật (`isAuthPage`) trong `AuthFilter.java`, cho phép khách vãng lai (chưa đăng nhập) truy cập xem Landing Page giới thiệu giải pháp BOMS trên `index.jsp` mà không bị ép chuyển hướng tới `login.jsp`. `(@Trung)`

### Fixed
* **Quy Hoạch Giải Phóng Tài Nguyên JDBC 100% Qua Try-With-Resources (Task 19.3):**
  - Tái cấu trúc triệt để hơn 60 phương thức từ các giai đoạn đầu trong toàn bộ các lớp DAO (`UserDAO.java`, `OrderDAO.java`, `IngredientDAO.java`, `ProductDAO.java`, `MenuCategoryDAO.java`, `AccountManagerDAO.java`) sang cú pháp chuẩn `try-with-resources`.
  - Đảm bảo 100% các đối tượng `PreparedStatement`, `Statement`, `ResultSet` và `getGeneratedKeys()` được tự động giải phóng an toàn, triệt tiêu hoàn toàn nguy cơ rò rỉ kết nối và cạn kiệt Connection Pool của MS SQL Server.
* **Chuẩn Hóa PRG & Triệt Tiêu Lỗi HTTP 500 NPE Tại Cài Đặt (Task 19.1):**
  - Bổ sung cơ chế kiểm tra `loginUser == null` tại `SettingsController.java`, tự động chuyển hướng về trang đăng nhập nếu phiên làm việc hết hạn.
  - Chuẩn hóa toàn bộ `doPost` sang mô hình Post-Redirect-Get (PRG) kết hợp `FlashMessageUtil`, xóa bỏ hoàn toàn lệnh gọi đệ quy `doGet(request, response)` sai kiến trúc.
* **Triệt Tiêu Hoàn Toàn Inline CSS / JS & Loại Bỏ Trùng Lặp Script (Task 19.4):**
  - `web/login.jsp`: Loại bỏ style nội tuyến và `onclick` tại popup tài khoản bị khóa, thay thế bằng class `.btn-banned-close` và hàm `closeBannedPopup()` trong `main.js`.
  - `web/menu.jsp`: Thay thế khối style inline bằng class ngữ nghĩa `.menu-header-bar` và `.menu-empty-container` trong `global.css`.
  - `web/attendance.jsp` & `web/hr_management.jsp`: Xóa bỏ thẻ `<script src=".../main.js"></script>` bị nạp thừa 2 lần.
* **Đồng Bộ Hoàn Thiện Cấu Hình Dự Án & Đa Ngôn Ngữ (Task 19.5):**
  - Khai báo đầy đủ servlet và servlet-mapping cho `DispatchController` và `ShipperController` (kèm `<multipart-config>`) trong `web/WEB-INF/web.xml`.
  - Bổ sung thông điệp `login.banned.message` và chuẩn hóa khoảng trắng nhãn `register.label.fullname` trong `messages_en.properties`.
  - Tái cấu trúc `MenuManagementController.java` và `OrderManagementController.java` sử dụng `FlashMessageUtil.forwardFlashMessages(request, session)`, loại bỏ các hàm private trùng lặp (DRY).

### Notes
* Xóa bỏ vĩnh viễn 2 servlet đơn lẻ không an toàn: `GenerateKeyController.java` và `ToggleUserController.java`.
* Toàn bộ 73 tệp mã nguồn Java của dự án biên dịch thành công tuyệt đối 0 lỗi qua NetBeans Ant (`BUILD SUCCESSFUL`).
* Dự án BOMS chính thức hoàn thành toàn bộ 19 Task, đạt mốc bàn giao **Bản Beta 1 (v1.0.0-beta1)**, sẵn sàng nghiệm thu và triển khai thực tế.
---
## [v1.0.0-final] - 2026-10-23.
### Added
* **Quản Lý Giao Dịch Nguyên Tử Khi Đăng Ký Tài Khoản (Atomic Registration Transaction - Task 20.1):**
  - Xây dựng phương thức `registerUserWithKeyTransaction(User user, String securityKey)` trong `UserDAO.java` tích hợp kết nối JDBC nguyên tử (`connection.setAutoCommit(false)`, `commit()`, `rollback()`).
  - Đảm bảo 3 tác vụ: Thêm tài khoản vào bảng `Users`, Khóa mã bảo mật `AccountManager` (`IsUsed = 1`, `UserID = ?`), và Tự động tạo hồ sơ vận chuyển `ShipperProfiles` (đối với vai trò Shipper) diễn ra đồng thời trong một giao dịch duy nhất. Nếu bất kỳ bước nào phát sinh ngoại lệ, toàn bộ dữ liệu tự động hoàn tác (Rollback), triệt tiêu hoàn toàn rủi ro tạo tài khoản mồ côi hoặc treo mã Security Key. `(@Trung)`
* **Mã Hóa Mật Khẩu Bảo Mật SHA-256 & Cơ Chế Tương Thích Ngược Thông Minh (Password Hashing - Task 20.2):**
  - Xây dựng tiện ích băm mật khẩu `utility/PasswordUtil.java` sử dụng thuật toán chuẩn `SHA-256` với chuỗi đầu ra Hexadecimal 64 ký tự.
  - Tích hợp toàn diện tại các điểm tiếp xúc mật khẩu: `UserService.register` (băm mật khẩu khi đăng ký mới), `UserService.changePassword` (băm mật khẩu khi người dùng đổi mật khẩu), và `AdminService.resetUserPasswordByAdmin` (băm mật khẩu khi Admin cấp lại mật khẩu).
  - Tích hợp cơ chế tự động nâng cấp mã hóa (Auto-upgrade password hashing): Tại `UserService.login`, hệ thống hỗ trợ đăng nhập bình thường đối với các tài khoản kiểm thử cũ đang lưu mật khẩu thô (như `123456`), sau đó tự động băm mật khẩu đó sang chuẩn SHA-256 và cập nhật ngay vào CSDL trong lần đăng nhập thành công đầu tiên, vừa đảm bảo an toàn tuyệt đối vừa không làm gián đoạn việc kiểm thử. `(@Trung)`

### Fixed
* **Triệt Tiêu 100% Chuỗi Session & Request Hardcode Trong Controllers (Task 9.1):**
  - Khai báo tập trung toàn bộ các hằng số Session trong `Constants.java`: `CURRENT_CART`, `CURRENT_TABLE`, `LAST_ORDER_ID`, `PLACED_ORDER_IDS`, `LANG`, `LAST_TICKET_ID`.
  - Quét và thay thế sạch sẽ toàn bộ các chuỗi session gõ chay trong `CartController.java`, `LanguageController.java`, `MenuController.java`, `ReportController.java`, và `LoginController.java`, triệt tiêu hoàn toàn rủi ro gõ sai tên Session attribute và chống rò rỉ trạng thái bộ nhớ.
* **Quy Hoạch & Tách Biệt Độc Quyền Dải Mã Lỗi Hệ Thống Trong `Constants.java` (Task 9.2):**
  - Tách bạch và giải quyết triệt để mọi xung đột mã backend: Dải mã Báo cáo ẩn danh giữ độc quyền sở hữu `S0300`-`S0301`, `E0300`-`E0316`, `DE0030`-`DE0033`.
  - Dải mã Điểm danh / Chấm công được chuyển dời hoàn toàn sang phân vùng riêng biệt `S0708`-`S0709`, `E0716`-`E0721`, `DE0056`-`DE0058`.
  - Dải mã Cảnh báo kho nguyên liệu giữ độc quyền `DE0018`-`DE0020`. Tách riêng mã lỗi Tab Cài đặt sang `E9006` (`ERROR_INVALID_SETTING_TAB`), bảo toàn `E9000` độc quyền cho lỗi hành động không hợp lệ.
* **Tái Cấu Trúc, Xóa Trùng Lặp & Đồng Bộ 100% Đa Ngôn Ngữ (Task 9.3):**
  - Quy hoạch toàn bộ 207 mã backend chuẩn (`SXXX`, `EXXX`, `DEXXX`, `WARXXX`) lên khối đầu trang của cả 2 tệp `messages_vi.properties` và `messages_en.properties` theo đúng thứ tự logic trong `Constants.java`.
  - Bổ sung 10 khóa giao diện còn thiếu ở phía tiếng Việt và mã hóa 100% nội dung tiếng Việt sang định dạng Unicode Escape (`\uXXXX`) thuần ASCII theo nghiêm ngặt `Rule_BOMS.md`.
  - Loại bỏ hoàn toàn các khối copy-paste trùng lặp cũ: Cả hai tệp `messages_vi.properties` và `messages_en.properties` đạt trạng thái đồng bộ hoàn hảo với 885 khóa duy nhất đối xứng 1-1 (0 duplicate keys).

### Notes
* Hoàn thành 100% toàn bộ 20 Tasks và các Sub-task trong lộ trình phát triển `WorkList.md`. Toàn bộ nợ kỹ thuật tồn đọng đã được thanh toán triệt để.
* Toàn bộ 74 tệp mã nguồn Java của dự án biên dịch thành công tuyệt đối 0 lỗi qua NetBeans Ant (`BUILD SUCCESSFUL`).
* Dự án BOMS chính thức hoàn tất phiên bản phát hành cuối cùng **(v1.0.0-final)**, sẵn sàng nghiệm thu và đưa vào vận hành thực tế.
---
## [v1.0.0 - Release 1.0 Notes] - 2026-10-24.

### 🛡️ 1. Bảo Mật & Upload Avatar
* **Vá lỗ hổng Path Traversal & Tên file độc hại (`ImageUploadUtility.java`):**
  - Loại bỏ hoàn toàn việc sử dụng tên file gốc do client gửi lên (`getSubmittedFileName`).
  - Khởi tạo tên file hoàn toàn ngẫu nhiên và an toàn bằng `UUID.randomUUID().toString()` kết hợp với phần mở rộng chuẩn (`.jpg`, `.png`).
  - Bổ sung cơ chế đọc byte chữ ký nhị phân (**Magic Bytes**) trực tiếp từ luồng dữ liệu `InputStream` để xác thực định dạng file thực sự (`FF D8 FF` cho JPEG, `89 50 4E 47` cho PNG, `RIFF...WEBP` cho WebP). Tự động ném `ValidationException` và từ chối lưu ra đĩa nếu sai định dạng.
* **Đồng bộ HttpSession & Phản hồi Tức thì (`ProfileController.java`):**
  - Bổ sung lệnh làm mới `session.setAttribute(Constants.USER_SESSION, loginUser)` ngay sau khi cập nhật avatar mới hoặc xóa avatar, giúp giao diện người dùng thay đổi ảnh đại diện ngay lập tức mà không cần đăng nhập lại.
* **Lưu ý triển khai cho Team (Database Migration):**
  - Môi trường mới hoặc các cơ sở dữ liệu hiện hữu bắt buộc chạy kịch bản `DATABASE/BOMS_DATABASE_2.sql` để khởi tạo cột `Avatar VARCHAR(255) NULL` trong bảng `Users`.

### 🚚 2. Chuẩn Hóa Luồng Điều Phối Shipper
* **Khắc phục lỗi đếm sai ActiveOrders khi Shipper từ chối đơn (`DeliveryService.java`):**
  - Điều chỉnh thứ tự thực thi trong `rejectTask`: Đảo lệnh cập nhật CSDL gỡ `ShipperID = NULL` và đưa trạng thái về `Waiting for dispatch` lên trước, sau đó mới gọi hàm đồng bộ `syncShipperProfileStatus`.
  - Đảm bảo hàm đếm `countActiveOrdersByShipperId` đếm chính xác số đơn thực sự trong tiến trình giao (`Delivering`, `Waiting for delivery`), không còn tình trạng tụt đơn hoạt động (từ 3/3 tụt nhầm về 2/3) khi từ chối đơn mới đẩy đến.
* **Chuẩn hóa dung lượng nhận đơn của Shipper:**
  - Dọn dẹp logic kiểm tra dung lượng thừa tại `acceptTask` và áp dụng hằng số chuẩn `Constants.MAX_SHIPPER_ORDERS = 3` trên toàn hệ thống.

### 🔒 3. Khóa State Machine (Vòng Đời Đơn Hàng)
* **Chốt chặn State Machine ở Backend (`OrderService.java`):**
  - Bắt buộc kiểm tra đơn hàng hiện tại qua `orderDAO.getOrderById(orderId)` trước khi thực hiện cập nhật.
  - Chặn tuyệt đối mọi thao tác thay đổi trạng thái đối với các đơn `Delivery` đang do Shipper kiểm soát (`Waiting for delivery`, `Picked up`, `Delivering`), ném `ValidationException(Constants.ERROR_DELIVERY_CONTROLLED_BY_SHIPPER)` (mã `E0224`). Muốn thay đổi trạng thái, nhân viên điều phối phải sử dụng chức năng Thu hồi (Revoke) tại Trung tâm điều phối.
  - Chặn tuyệt đối mọi cập nhật đối với các đơn đã ở trạng thái kết thúc (`Delivered`, `Paid`, `Returned`, `Cancelled`), ném `ValidationException(Constants.ERROR_ORDER_CLOSED_STATUS)` (mã `E0225`).
* **Khóa Giao diện UI ở Frontend (`order_management.jsp` & Table Components):**
  - Tại cột Thao tác (Action) của `standard_orders_table.jsp` và `delivery_orders_table.jsp`, bọc thẻ `<c:choose>` để khóa và ẩn form dropdown đối với các đơn thuộc Shipper (hiển thị `🔒 Đang do Shipper xử lý`) và các đơn đã kết thúc (hiển thị `🔒 Đã hoàn tất`), loại bỏ hoàn toàn khả năng người dùng gửi request sai luồng.

### 🧹 4. Tổng Kiểm Toán Mã Nguồn (Static Code Audit & Full Compliance)
* **Triệt tiêu 100% Khối Catch Rỗng (Zero Empty Catch):**
  - Rà soát và xử lý dứt điểm các khối catch rỗng tại `OrderDAO`, `UserDAO`, `DashboardController`, `NotificationController`, `AttendanceService`, bổ sung ném ngoại lệ hoặc ghi log chi tiết qua `java.util.logging.Logger`.
* **Vá Lỗ hổng Rò rỉ Transaction (Transaction Leak Prevention):**
  - Cập nhật các khối giao dịch JDBC (`OrderDAO`, `UserDAO`, `ReportDAO`) bắt `catch (Exception e)` chung (thay vì chỉ `SQLException`) để đảm bảo lệnh `connection.rollback()` bắt buộc được gọi khi phát sinh bất kỳ lỗi Runtime nào trước khi khôi phục `setAutoCommit(true)`.
* **Dọn dẹp Biến Static Cục bộ & Gom Chuỗi Hardcode về `Constants.java`:**
  - Di chuyển toàn bộ các hằng số tĩnh nội bộ trong `AttendanceService.java` sang `Constants.java`. Đạt chuẩn **0 biến static ngoài package utility**.
  - Gom toàn bộ chuỗi thông báo phân ca, duyệt điểm danh, cảnh báo kho về `Constants.java` dưới dạng template `String.format(Constants.NOTIF_..., ...)`.
  - Bổ sung `dayKey` i18n (`hr.assign.day.mon`..`sun`) trên giao diện Thời khóa biểu và Ma trận phân ca tuần, tuân thủ 100% quy chuẩn i18n đa ngôn ngữ và chuẩn mã hóa Unicode Escape (`\uXXXX`).
---
