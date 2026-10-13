# TÀI LIỆU QUY CHUẨN KIẾN TRÚC DỰ ÁN BOMS (Bingxue Operation System)

Tài liệu này quy định chuẩn mực lập trình và cấu trúc hệ thống. Yêu cầu toàn bộ thành viên dự án và AI Agent tuân thủ nghiêm ngặt để đảm bảo tính nhất quán, dễ bảo trì và mở rộng.

## 1. QUY CHUẨN KIẾN TRÚC MVC VÀ VAI TRÒ CÁC TẦNG (PACKAGES)

### VIEW (Giao diện)
- Chứa các file HTML và JSP.
- Chỉ xử lý giao diện hiển thị, nghiêm cấm sử dụng scriptlet (`<% code java %>`).
- Logic giao diện ưu tiên sử dụng JavaScript hoạc jstl-EL.
- `js/`: Chứa toàn bộ file JavaScript. View gọi đến pack này để nhúng code. Tuyệt đối không viết JavaScript trực tiếp vào JSP/HTML.
- `css/`: Chứa toàn bộ file CSS. Các thẻ trong View phải dùng ID hoặc Class để đối chiếu. Tuyệt đối không viết CSS trực tiếp vào JSP/JavaScript.

### CONTROLLER (Tầng điều phối)
- Chứa các Servlet làm nhiệm vụ điều phối luồng chương trình.
- Chỉ tiếp nhận Request, kiểm tra Session, quản lý Try-Catch tổng và điều hướng (Forward/Redirect).
- Tuyệt đối không chứa logic nghiệp vụ tính toán.

### SERVICE (Tầng nghiệp vụ)
- Chứa toàn bộ logic nghiệp vụ phức tạp (tính toán, kiểm tra đăng nhập...).
- Được phép gọi trực tiếp DAO và Model.
- Mọi ngoại lệ (Exception) phải được ném (throws) lên tầng Controller xử lý.

### DAO (Data Access Object - Tầng truy xuất dữ liệu)
- Chứa toàn bộ các hàm CRUD và câu lệnh truy vấn SQL.
- Được phép import và thao tác với Model.

### MODEL (Tầng thực thể)
- Là vỏ rỗng chứa cấu trúc dữ liệu, các thuộc tính tương ứng với các cột trong Database.
- Chỉ chứa thuộc tính private, Constructor, Getter và Setter.
- Tuyệt đối không chứa logic xử lý.

### UTILITY (Tầng tiện ích)
- Chứa các công cụ dùng chung: kiểm tra rỗng, kiểm tra định dạng, mã hóa, khai báo hằng số.
- Các phương thức được phép đặt ở dạng `public static`.
- Không xử lý Try-Catch tại đây, phát hiện lỗi phải ném Exception.

## 2. NGUYÊN TẮC LẬP TRÌNH CỐT LÕI

- **Controller:** Cấm import DAO và Model. Cấm khai báo hoặc gọi `PrintWriter` để in ra màn hình. Mọi luồng dữ liệu phải đẩy qua View bằng `request.setAttribute` hoặc `sendRedirect`.
- **Service và DAO:** Là 2 tầng duy nhất được phép import Model.
- **Biến Static:** Nghiêm cấm sử dụng khai báo static ngoài package Utility.
- **Text và Đa ngôn ngữ:** Không hardcode chuỗi ký tự cứng vào dự án. Mọi chuỗi thông báo hoặc mã lỗi phải đặt trong class Constants hoặc thẻ properties. ĐẶC BIỆT Riêng các thông báo từ backEnd thì phải khai báo thành 1 mã lỗi ví dụ "E000" chuỗi E000 này sẽ được đặt vào biến #ERROR_MESAGE ở tầng View và sẽ được in ra nội dung chi tiết được khai báo trong properties ví dụ E000=Lỗi hệ thống(Bản việt) hoạc E000=System ERROR(Bản Tiếng anh)
- **Tối ưu View:** Các giao diện phức tạp phải được tách nhỏ thành các JSP riêng đặt trong thư mục `components` và dùng lệnh include để nhúng vào trang chính.
- **Quản lý phiên bản (Git):** Không copy/paste toàn bộ mã nguồn để đẩy lên Github. Chỉ commit các file đã thay đổi nằm trong `src/`, `web/` và `nbproject/`.
- **Nguyên tắc đơn trách nhiệm (SRP):** Các hàm phải được bóc tách nhỏ nhất có thể. Mỗi hàm chỉ làm một nhiệm vụ duy nhất để tối ưu hóa module.
- **Cơ chế Flash Message (Thông báo một lần):** Khi thực hiện các thao tác thay đổi dữ liệu (Create/Update/Delete) và chuyển trang bằng `sendRedirect`, mọi thông báo trạng thái (Thành công/Thất bại) BẮT BUỘC phải được lưu tạm vào `Session` (Ví dụ: `session.setAttribute("SUCCESS_MESSAGE", "Mã thông báo...");`). Phía giao diện View (`.jsp`) sau khi hiển thị xong phải lập tức dùng thẻ `<c:remove var="SUCCESS_MESSAGE" scope="session"/>` để dọn dẹp, đảm bảo thông báo không bị lặp lại khi tải lại trang (F5) hoạc nếu servlet có gọi theo phong cách post get thì phải sử lí bên servlet còn lại đảm bảo không lưu rác trong seasion.
## 3. QUY CHUẨN VALIDATION (2 TẦNG)

- **Tầng 1 (Utility):** Kiểm tra dữ liệu thô đầu vào (chuỗi rỗng, độ dài mật khẩu, định dạng cơ bản).
- **Tầng 2 (Service):** Kiểm tra tính nghiệp vụ (kiểm tra trùng lặp tài khoản, tồn tại email). Hàm Validation Tầng 2 được phép gọi tới hàm Validation Tầng 1.

## 4. QUY CHUẨN CƠ SỞ DỮ LIỆU (JDBC)

- **Triển khai:** Mọi thành viên bắt buộc phải chạy file `BOMS_DATABASE.sql` trong SQL Server Management Studio trước khi lập trình. Cần tự thiết lập phía Microsoft sql management với bắt buộc phải có tài khoản với tên đăng nhập như sau: Tên đăng nhập: SA; MẬt khẩu: 123
- **Sửa đổi:** Cấm tự ý sửa file SQL mà không thông báo. Hạn chế tối đa việc xóa bảng/cột, ưu tiên phương án thêm mới (Additive changes).
- **Xử lý lỗi:** Mọi truy vấn database phải có Try-Catch và throws `ValidationException` lên tầng trên, kèm theo mã lỗi cấu hình sẵn trong Constants.
- **Console:** Nghiêm cấm dùng lệnh `System.out.println` trong package DAO.

## 5. CẤU TRÚC THƯ MỤC DỰ ÁN

```text
BOMS_Project/
├── DATABASE/                 // Lưu trữ kịch bản cơ sở dữ liệu (SQL)
├── src/                      // Mã nguồn Java Backend và tài nguyên hệ thống
│   ├── java/
│   │   ├── controller/       // Tầng điều phối ứng dụng (Servlet), tiếp nhận Request và điều hướng
│   │   ├── service/          // Tầng xử lý nghiệp vụ, tính toán logic và validation tầng 2
│   │   ├── dao/              // Tầng truy xuất dữ liệu, thực thi CRUD và câu lệnh SQL
│   │   ├── dal/              // Tầng nạp JDBC driver và thiết lập kết nối CSDL(Không cần can thiệp)
│   │   ├── model/            // Tầng thực thể đại diện cho cấu trúc bảng trong CSDL
│   │   ├── filter/           // Tầng bộ lọc tiền xử lý request/response, phân quyền và chuẩn hóa
│   │   └── utility/          // Chứa các lớp tiện ích dùng chung (hằng số, cấu hình, validation tầng 1)
│   └── resources/            // Chứa các tập tin tài nguyên i18n phục vụ đa ngôn ngữ
├── web/                      // Tầng giao diện (Web Root) và cấu hình ứng dụng Web
│   ├── components/           // Chứa các file JSP thành phần để include
│   ├── css/                  // Chứa các file style định dạng giao diện
│   ├── js/                   // Chứa các file JavaScript xử lý client
│   ├── WEB-INF/              // Chứa cấu hình bảo mật máy chủ (web.xml, properties)
│   └── META-INF/             // Chứa cấu hình ngữ cảnh ứng dụng trên Web Server
├── lib/                      // Chứa các thư viện phụ thuộc (JAR) phục vụ biên dịch
├── dist/                     // Chứa gói ứng dụng thành phẩm (.war)
├── nbproject/                // Chứa các tệp cấu hình dự án của IDE
├── README.md                 // Nhật ký cập nhật phiên bản (Changelog)
└── Tài liệu PRJ301.docx      // Tài liệu quy chuẩn kiến trúc dự án nay được thay bằng readme này
```