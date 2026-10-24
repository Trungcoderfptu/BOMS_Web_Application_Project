/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utility;

import jakarta.servlet.http.Part;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Tiện ích xử lý tải lên và quản lý tệp tin dùng chung cho BOMS (ảnh, video, tài liệu).
 * Lưu trữ độc lập trên ổ đĩa máy chủ (ngoài thư mục deploy) để tránh mất file khi Clean & Build.
 *
 * Có 2 vùng lưu trữ:
 * - {@link #UPLOAD_ROOT_DIR} (công khai): ImageServingServlet phát qua URL /uploads/* cho mọi người (ảnh món ăn...).
 * - {@link #PRIVATE_ROOT_DIR} (riêng tư): KHÔNG có URL công khai, chỉ Controller có kiểm tra quyền
 *   mới được đọc qua {@link #getPrivateFile(String)} (ví dụ: bằng chứng báo cáo ẩn danh).
 *
 * Mỗi loại file khai báo 1 {@link UploadRule} (đuôi file, dung lượng, mã lỗi, vùng lưu trữ).
 *
 * @author AD
 */
public class ImageUploadUtility {

    public static final String DATA_ROOT_DIR = System.getProperty("user.home")
            + File.separator + "boms_data";

    public static final String UPLOAD_ROOT_DIR = DATA_ROOT_DIR + File.separator + "uploads";

    public static final String PRIVATE_ROOT_DIR = DATA_ROOT_DIR + File.separator + "private";

    public static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB

    private static final String PUBLIC_PREFIX = "uploads/";
    private static final String PRIVATE_PREFIX = "private/";

    // =========================================================================
    // QUY TẮC TẢI LÊN CHO TỪNG LOẠI FILE
    // =========================================================================
    /**
     * Quy tắc kiểm tra và nơi lưu cho 1 loại file.
     */
    public static final class UploadRule {

        private final Set<String> allowedExtensions;
        private final Set<String> allowedMimeTypes;
        private final long maxSize;
        private final String typeErrorCode;
        private final String sizeErrorCode;
        private final String uploadErrorCode;
        private final boolean privateStorage;

        /**
         * @param allowedExtensions đuôi file hợp lệ, có dấu chấm (".jpg")
         * @param allowedMimeTypes MIME hợp lệ; null = chỉ kiểm tra đuôi file
         *        (dùng cho tài liệu vì trình duyệt gửi MIME của .docx không thống nhất)
         * @param privateStorage true = lưu vào vùng riêng tư, không có URL công khai
         */
        public UploadRule(Set<String> allowedExtensions, Set<String> allowedMimeTypes, long maxSize,
                String typeErrorCode, String sizeErrorCode, String uploadErrorCode, boolean privateStorage) {
            this.allowedExtensions = Collections.unmodifiableSet(allowedExtensions);
            this.allowedMimeTypes = allowedMimeTypes == null ? null : Collections.unmodifiableSet(allowedMimeTypes);
            this.maxSize = maxSize;
            this.typeErrorCode = typeErrorCode;
            this.sizeErrorCode = sizeErrorCode;
            this.uploadErrorCode = uploadErrorCode;
            this.privateStorage = privateStorage;
        }

        public long getMaxSize() {
            return maxSize;
        }

        public boolean isPrivateStorage() {
            return privateStorage;
        }
    }

    /**
     * Ảnh món ăn, avatar...: công khai, tối đa 10MB.
     */
    public static final UploadRule IMAGE_RULE = new UploadRule(
            new HashSet<>(Arrays.asList(".jpg", ".jpeg", ".png", ".webp")),
            new HashSet<>(Arrays.asList("image/jpeg", "image/png", "image/webp")),
            MAX_FILE_SIZE,
            Constants.ERROR_INVALID_IMAGE_TYPE,
            Constants.ERROR_IMAGE_SIZE_EXCEEDED,
            Constants.ERROR_IMAGE_UPLOAD_FAILED,
            false);

    /**
     * Bằng chứng báo cáo ẩn danh (UC-07): ảnh, video, tài liệu; riêng tư, tối đa 20MB.
     * Đuôi file khớp ràng buộc CK_Evidence_Type, dung lượng khớp CK_Evidence_Size trong DB.
     */
    public static final UploadRule REPORT_EVIDENCE_RULE = new UploadRule(
            new HashSet<>(Arrays.asList(".jpg", ".jpeg", ".png", ".mp4", ".pdf", ".docx")),
            null,
            Constants.REPORT_MAX_EVIDENCE_SIZE,
            Constants.ERROR_REPORT_INVALID_FILE_TYPE,
            Constants.ERROR_REPORT_FILE_TOO_LARGE,
            Constants.ERROR_REPORT_UPLOAD_FAILED,
            true);

    // =========================================================================
    // LƯU FILE
    // =========================================================================
    /**
     * Lưu file ảnh được tải lên vào thư mục con chỉ định (vùng công khai).
     * Tuyệt đối KHÔNG sử dụng tên file gốc do client gửi (phòng chống Path Traversal).
     * Khởi tạo tên file hoàn toàn mới bằng UUID.randomUUID().toString() ghép với đuôi mở rộng chuẩn (.jpg, .png,...).
     * Xác thực Magic Bytes từ InputStream để đảm bảo file thực sự là hình ảnh.
     *
     * @param filePart đối tượng Part từ multipart request
     * @param subFolder thư mục con (ví dụ: "products", "avatars")
     * @return đường dẫn tương đối dạng uploads/avatars/<uuid>.jpg (không có dấu / ở đầu), hoặc null nếu không có file
     * @throws ValidationException nếu file vượt dung lượng, sai định dạng (magic bytes) hoặc lỗi ghi file
     */
    public static String saveImage(Part filePart, String subFolder) throws ValidationException {
        if (filePart == null || filePart.getSize() <= 0) {
            return null;
        }

        // 1. Kiểm tra dung lượng tối đa
        if (filePart.getSize() > MAX_FILE_SIZE) {
            throw new ValidationException(Constants.ERROR_IMAGE_SIZE_EXCEEDED);
        }

        // 2. Bổ sung cơ chế đọc byte (Magic Byte) từ luồng InputStream để xác thực file thực sự là hình ảnh
        String ext;
        try (InputStream in = filePart.getInputStream()) {
            ext = validateAndDetectImageExtension(in);
        } catch (IOException e) {
            throw new ValidationException(Constants.ERROR_INVALID_IMAGE_TYPE);
        }

        // 3. Khởi tạo tên file hoàn toàn mới bằng UUID.randomUUID().toString() ghép với đuôi chuẩn (ví dụ .jpg, .png)
        String uniqueFileName = UUID.randomUUID().toString() + ext;
        String sanitizedSubFolder = sanitizeSubFolder(subFolder);

        // 4. Lưu vào thư mục đích trên đĩa máy chủ (UPLOAD_ROOT_DIR)
        File targetDir = new File(UPLOAD_ROOT_DIR, sanitizedSubFolder);
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }

        File targetFile = new File(targetDir, uniqueFileName);
        try (InputStream in = filePart.getInputStream();
                OutputStream out = new FileOutputStream(targetFile)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            targetFile.delete();
            throw new ValidationException(Constants.ERROR_IMAGE_UPLOAD_FAILED);
        }

        // 5. Chuẩn hóa đường dẫn trả về: uploads/subFolder/<uuid>.ext (không có dấu / ở đầu)
        return PUBLIC_PREFIX + sanitizedSubFolder + "/" + uniqueFileName;
    }

    /**
     * Xác thực tệp tin ảnh dựa trên Magic Bytes từ luồng InputStream.
     * Ném ValidationException ngay nếu sai định dạng.
     *
     * @param in luồng dữ liệu InputStream
     * @return đuôi mở rộng chuẩn (ví dụ: ".jpg", ".png", ".webp")
     * @throws IOException khi gặp lỗi I/O
     * @throws ValidationException khi magic bytes không khớp bất kỳ định dạng ảnh hợp lệ nào
     */
    public static String validateAndDetectImageExtension(InputStream in) throws IOException, ValidationException {
        if (in == null) {
            throw new ValidationException(Constants.ERROR_INVALID_IMAGE_TYPE);
        }

        byte[] header = new byte[12];
        int bytesRead = in.read(header);
        if (bytesRead < 4) {
            throw new ValidationException(Constants.ERROR_INVALID_IMAGE_TYPE);
        }

        // JPEG: byte đầu là FF D8 (SOI marker: 0xFF, 0xD8)
        if ((header[0] & 0xFF) == 0xFF && (header[1] & 0xFF) == 0xD8) {
            return ".jpg";
        }

        // PNG: byte đầu là 89 50 (89 50 4E 47 0D 0A 1A 0A)
        if ((header[0] & 0xFF) == 0x89 && (header[1] & 0xFF) == 0x50) {
            if (bytesRead >= 4 && header[2] == 'N' && header[3] == 'G') {
                return ".png";
            }
        }

        // WEBP: RIFF (bytes 0..3) ... WEBP (bytes 8..11)
        if (bytesRead >= 12
                && header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F'
                && header[8] == 'W' && header[9] == 'E' && header[10] == 'B' && header[11] == 'P') {
            return ".webp";
        }

        throw new ValidationException(Constants.ERROR_INVALID_IMAGE_TYPE);
    }

    /**
     * Lưu file bất kỳ theo quy tắc chỉ định.
     *
     * @return "uploads/{subFolder}/{tên}" (công khai) hoặc "private/{subFolder}/{tên}" (riêng tư),
     *         hoặc null nếu không có file
     * @throws ValidationException nếu file sai định dạng, vượt dung lượng hoặc lỗi ghi file
     */
    public static String saveFile(Part filePart, String subFolder, UploadRule rule) throws ValidationException {
        if (filePart == null || filePart.getSize() <= 0) {
            return null;
        }
        String submittedFileName = filePart.getSubmittedFileName();
        if (submittedFileName == null || submittedFileName.trim().isEmpty()) {
            return null;
        }
        validateFile(filePart, rule);

        String ext = extractExtension(submittedFileName).toLowerCase();
        String sanitizedSubFolder = sanitizeSubFolder(subFolder);
        String uniqueFileName = generateUniqueFileName(sanitizedSubFolder, ext);

        File targetDir = new File(rule.privateStorage ? PRIVATE_ROOT_DIR : UPLOAD_ROOT_DIR, sanitizedSubFolder);
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }

        File targetFile = new File(targetDir, uniqueFileName);
        try (InputStream in = filePart.getInputStream();
                OutputStream out = new FileOutputStream(targetFile)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            targetFile.delete();
            throw new ValidationException(rule.uploadErrorCode);
        }

        String prefix = rule.privateStorage ? PRIVATE_PREFIX : PUBLIC_PREFIX;
        return prefix + sanitizedSubFolder + "/" + uniqueFileName;
    }

    /**
     * Kiểm tra dung lượng, đuôi file và MIME mà KHÔNG ghi file.
     * Dùng khi cần kiểm tra toàn bộ danh sách file trước rồi mới lưu (tránh lưu dở dang).
     *
     * @throws ValidationException nếu file rỗng, vượt dung lượng hoặc sai định dạng
     */
    public static void validateFile(Part filePart, UploadRule rule) throws ValidationException {
        if (filePart == null || filePart.getSize() <= 0 || filePart.getSize() > rule.maxSize) {
            throw new ValidationException(rule.sizeErrorCode);
        }
        String submittedFileName = filePart.getSubmittedFileName();
        String ext = submittedFileName == null ? "" : extractExtension(submittedFileName).toLowerCase();
        if (!rule.allowedExtensions.contains(ext)) {
            throw new ValidationException(rule.typeErrorCode);
        }
        String mimeType = filePart.getContentType();
        if (rule.allowedMimeTypes != null && mimeType != null
                && !rule.allowedMimeTypes.contains(mimeType.toLowerCase())) {
            throw new ValidationException(rule.typeErrorCode);
        }

        // Kiểm tra chữ ký tệp (Magic Bytes) chống ngụy trang định dạng
        try (InputStream in = filePart.getInputStream()) {
            if (!isValidMagicBytes(in, ext)) {
                throw new ValidationException(rule.typeErrorCode);
            }
        } catch (IOException e) {
            throw new ValidationException(rule.typeErrorCode);
        }
    }

    /**
     * Xác thực tệp tin dựa trên File Signatures (Magic Bytes) từ luồng nhị phân thực tế,
     * ngăn chặn triệt để lỗ hổng giả mạo MIME type hoặc đổi đuôi file mã độc (.jsp, .exe,...).
     *
     * @param in luồng dữ liệu của file
     * @param ext đuôi mở rộng chữ thường (ví dụ: ".jpg", ".png")
     * @return true nếu chữ ký nhị phân khớp định dạng, false nếu phát hiện sai lệch
     * @throws IOException khi lỗi đọc luồng dữ liệu
     */
    public static boolean isValidMagicBytes(InputStream in, String ext) throws IOException {
        if (in == null || ext == null) {
            return false;
        }
        byte[] header = new byte[12];
        int bytesRead = in.read(header);
        if (bytesRead < 4) {
            return false;
        }

        switch (ext) {
            case ".jpg":
            case ".jpeg":
                // JPEG magic bytes: FF D8
                return (header[0] & 0xFF) == 0xFF
                        && (header[1] & 0xFF) == 0xD8;

            case ".png":
                // PNG magic bytes: 89 50 4E 47 0D 0A 1A 0A
                if (bytesRead < 8) {
                    return false;
                }
                return (header[0] & 0xFF) == 0x89
                        && header[1] == 'P'
                        && header[2] == 'N'
                        && header[3] == 'G'
                        && header[4] == 0x0D
                        && header[5] == 0x0A
                        && header[6] == 0x1A
                        && header[7] == 0x0A;

            case ".webp":
                // WEBP: RIFF....WEBP (header[0..3] == 'RIFF' and header[8..11] == 'WEBP')
                if (bytesRead < 12) {
                    return false;
                }
                return header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F'
                        && header[8] == 'W' && header[9] == 'E' && header[10] == 'B' && header[11] == 'P';

            case ".pdf":
                // PDF magic bytes: %PDF- (25 50 44 46)
                return header[0] == 0x25 && header[1] == 0x50 && header[2] == 0x44 && header[3] == 0x46;

            case ".mp4":
                // MP4: Offset 4 chứa ftyp (bytes 4..7: 66 74 79 70)
                if (bytesRead < 8) {
                    return false;
                }
                return header[4] == 'f' && header[5] == 't' && header[6] == 'y' && header[7] == 'p';

            case ".docx":
                // DOCX (ZIP container): PK.. (50 4B 03 04)
                return header[0] == 0x50 && header[1] == 0x4B
                        && (header[2] == 0x03 || header[2] == 0x05 || header[2] == 0x07)
                        && (header[3] == 0x04 || header[3] == 0x06 || header[3] == 0x08);

            default:
                return true;
        }
    }

    // =========================================================================
    // ĐỌC & XÓA FILE
    // =========================================================================
    /**
     * Lấy đối tượng File trên đĩa vật lý từ đường dẫn tương đối (CHỈ vùng công khai).
     * Có kiểm tra chống Directory Traversal để bảo mật hệ thống.
     *
     * @param relativePath đường dẫn (ví dụ: "uploads/products/cafe.jpg" hoặc "/products/cafe.jpg")
     * @return File đối ứng trên đĩa hoặc null nếu không an toàn
     */
    public static File getImageFile(String relativePath) {
        if (relativePath == null || relativePath.trim().isEmpty()) {
            return null;
        }

        String cleanedPath = relativePath.trim().replace("\\", "/");
        if (cleanedPath.startsWith(PUBLIC_PREFIX)) {
            cleanedPath = cleanedPath.substring(PUBLIC_PREFIX.length());
        } else if (cleanedPath.startsWith("/" + PUBLIC_PREFIX)) {
            cleanedPath = cleanedPath.substring(PUBLIC_PREFIX.length() + 1);
        } else if (cleanedPath.startsWith("/")) {
            cleanedPath = cleanedPath.substring(1);
        }
        return resolveInside(UPLOAD_ROOT_DIR, cleanedPath);
    }

    /**
     * Lấy File trong vùng riêng tư. Chỉ gọi sau khi đã kiểm tra quyền của người dùng.
     *
     * @param relativePath đường dẫn đã lưu DB (ví dụ: "private/reports/repo_123.pdf")
     * @return File đối ứng trên đĩa hoặc null nếu đường dẫn không hợp lệ / không an toàn
     */
    public static File getPrivateFile(String relativePath) {
        if (relativePath == null) {
            return null;
        }
        String cleanedPath = relativePath.trim().replace("\\", "/");
        if (!cleanedPath.startsWith(PRIVATE_PREFIX)) {
            return null;
        }
        return resolveInside(PRIVATE_ROOT_DIR, cleanedPath.substring(PRIVATE_PREFIX.length()));
    }

    /**
     * Xóa file ảnh cũ trên đĩa khi người dùng đổi ảnh hoặc xóa món ăn.
     *
     * @param relativePath đường dẫn tương đối đã lưu trong DB
     * @return true nếu xóa thành công
     */
    public static boolean deleteImage(String relativePath) {
        return deleteFile(relativePath);
    }

    /**
     * Xóa file ở cả vùng công khai lẫn riêng tư (tự nhận biết theo tiền tố đường dẫn).
     *
     * @return true nếu xóa thành công
     */
    public static boolean deleteFile(String relativePath) {
        boolean isPrivate = relativePath != null && relativePath.trim().replace("\\", "/").startsWith(PRIVATE_PREFIX);
        File file = isPrivate ? getPrivateFile(relativePath) : getImageFile(relativePath);
        if (file != null && file.exists() && file.isFile()) {
            return file.delete();
        }
        return false;
    }

    /**
     * MIME type theo đuôi file, dùng khi trả file về trình duyệt.
     */
    public static String getContentType(String fileName) {
        String ext = fileName == null ? "" : extractExtension(fileName).toLowerCase();
        switch (ext) {
            case ".jpg":
            case ".jpeg":
                return "image/jpeg";
            case ".png":
                return "image/png";
            case ".webp":
                return "image/webp";
            case ".mp4":
                return "video/mp4";
            case ".pdf":
                return "application/pdf";
            case ".docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            default:
                return "application/octet-stream";
        }
    }

    // =========================================================================
    // HÀM HỖ TRỢ
    // =========================================================================
    /**
     * Ghép đường dẫn vào thư mục gốc và chặn Directory Traversal ("../").
     */
    private static File resolveInside(String baseDir, String cleanedPath) {
        File file = new File(baseDir, cleanedPath.replace("/", File.separator));
        try {
            String canonicalBase = new File(baseDir).getCanonicalPath() + File.separator;
            String canonicalFile = file.getCanonicalPath();
            if (!canonicalFile.startsWith(canonicalBase)) {
                return null; // Chặn directory traversal attack
            }
        } catch (IOException e) {
            return null;
        }
        return file;
    }

    private static String extractExtension(String filename) {
        int dotIndex = filename.lastIndexOf(".");
        if (dotIndex >= 0 && dotIndex < filename.length() - 1) {
            return filename.substring(dotIndex);
        }
        return "";
    }

    private static String sanitizeSubFolder(String subFolder) {
        if (subFolder == null || subFolder.trim().isEmpty()) {
            return "general";
        }
        return subFolder.replaceAll("[^a-zA-Z0-9_-]", "");
    }

    private static String generateUniqueFileName(String prefix, String ext) {
        return UUID.randomUUID().toString() + ext.toLowerCase();
    }
}
