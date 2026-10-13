# BOMS AGENT WORKFLOW

Bắt buộc AI Agent tuân thủ nghiêm ngặt các bước dưới đây khi nhận yêu cầu lập trình mới. Tuyệt đối không nhảy bước.

## BƯỚC 1: NẠP NGỮ CẢNH (CHỈ ĐỌC 1 LẦN)
1. Đọc nội dung file `Rule_BOMS.md` và `learning.md` (nếu chưa đọc trong phiên hiện tại). Không quét lại ở các lượt chat tiếp theo trừ khi có yêu cầu.
2. Yêu cầu người dùng cung cấp đường dẫn tĩnh hoặc tên các file liên quan trực tiếp đến tác vụ (Controller, Service, JSP...).
3. Dùng công cụ đọc file để lấy nội dung đúng 1 lần. Lưu vào bộ nhớ tạm, không quét lại nếu nội dung không đổi.

## BƯỚC 2: PHÂN TÍCH, ĐỀ XUẤT VÀ LÀM RÕ (CẤM SINH CODE)
Sau khi nạp ngữ cảnh, đưa ra bản thiết kế gồm:
1. Danh sách file tạo mới và file cần chỉnh sửa.
2. Giải thích ngắn gọn (dưới 3 dòng) cách phân bổ logic (Controller - Service - View) để chứng minh tính tuân thủ kiến trúc.
3. Đặt **đúng 01 câu hỏi trắc nghiệm (Multiple Choice)** cho người dùng để làm rõ UI/UX, logic nghiệp vụ, hoặc các trường hợp ngoại lệ (edge cases).
4. Dừng tiến trình. Chờ người dùng duyệt phương án và trả lời câu hỏi.

## BƯỚC 3: TRIỂN KHAI MÃ NGUỒN (ĐIỂM XẠ)
Chỉ sinh code sau khi người dùng phê duyệt ở Bước 2.
1. Cung cấp mã nguồn dạng khối cập nhật (Điểm xạ). Ghi rõ thay thế từ dòng nào đến dòng nào, hoặc chèn vào vị trí cụ thể. Không in lại toàn bộ file.
2. Kiểm tra chéo với `Rule_BOMS.md`: Đảm bảo dùng mã lỗi chuẩn (E000...), không CSS/JS inline, không truyền Model trực tiếp lên Controller.
3. Bóc tách logic thành các hàm nhỏ (Single Responsibility Principle).

## BƯỚC 4: TỔNG KẾT VÀ CẬP NHẬT LỊCH SỬ (`learning.md`)
1. Tóm tắt ngắn gọn các thay đổi vừa thực hiện (hàm mới tạo, file bị tác động) sau khi mã nguồn được người dùng xác nhận.
2. Ghi chú phần tóm tắt tiến độ này vào file `learning.md`.
3. Nếu code sinh ra bị lỗi hoặc vi phạm kiến trúc bị nhắc nhở, bắt buộc ghi chú lỗi sai và định hướng khắc phục vào `learning.md` để ghi nhớ cho các phiên sau.