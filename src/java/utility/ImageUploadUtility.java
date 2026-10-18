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
     *
     * @param filePart đối tượng Part từ multipart request
     * @param subFolder thư mục con (ví dụ: "products", "avatars")
     * @return đường dẫn tương đối để lưu DB (ví dụ: "uploads/products/prod_123.jpg"), hoặc null nếu không có file
     * @throws ValidationException nếu file vượt dung lượng, sai định dạng hoặc lỗi ghi file
     */
    public static String saveImage(Part filePart, String subFolder) throws ValidationException {
        return saveFile(filePart, subFolder, IMAGE_RULE);
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
        String shortPrefix = prefix.length() > 4 ? prefix.substring(0, 4) : prefix;
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        return shortPrefix + "_" + System.currentTimeMillis() + "_" + uniqueId + ext.toLowerCase();
    }
}
