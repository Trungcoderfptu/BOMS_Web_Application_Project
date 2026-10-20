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
