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
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Tiện ích xử lý tải lên và quản lý tệp tin hình ảnh dùng chung cho BOMS.
 * Lưu trữ độc lập trên ổ đĩa máy chủ (ngoài thư mục deploy) để tránh mất file khi Clean & Build.
 *
 * @author AD
 */
public class ImageUploadUtility {

    public static final String UPLOAD_ROOT_DIR = System.getProperty("user.home")
            + File.separator + "boms_data" + File.separator + "uploads";

    public static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB

    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(
            Arrays.asList(".jpg", ".jpeg", ".png", ".webp")
    );

    private static final Set<String> ALLOWED_MIME_TYPES = new HashSet<>(
            Arrays.asList("image/jpeg", "image/png", "image/webp")
    );

    /**
     * Lưu file ảnh được tải lên vào thư mục con chỉ định.
     *
     * @param filePart đối tượng Part từ multipart request
     * @param subFolder thư mục con (ví dụ: "products", "avatars")
     * @return đường dẫn tương đối để lưu DB (ví dụ: "uploads/products/prod_123.jpg"), hoặc null nếu không có file
     * @throws ValidationException nếu file vượt dung lượng, sai định dạng hoặc lỗi ghi file
     */
    public static String saveImage(Part filePart, String subFolder) throws ValidationException {
        if (filePart == null || filePart.getSize() <= 0) {
            return null;
        }

        if (filePart.getSize() > MAX_FILE_SIZE) {
            throw new ValidationException(Constants.ERROR_IMAGE_SIZE_EXCEEDED);
        }

        String submittedFileName = filePart.getSubmittedFileName();
        if (submittedFileName == null || submittedFileName.trim().isEmpty()) {
            return null;
        }

        String ext = extractExtension(submittedFileName);
        if (!ALLOWED_EXTENSIONS.contains(ext.toLowerCase())) {
            throw new ValidationException(Constants.ERROR_INVALID_IMAGE_TYPE);
        }

        String mimeType = filePart.getContentType();
        if (mimeType != null && !ALLOWED_MIME_TYPES.contains(mimeType.toLowerCase())) {
            throw new ValidationException(Constants.ERROR_INVALID_IMAGE_TYPE);
        }

        String sanitizedSubFolder = sanitizeSubFolder(subFolder);
        String uniqueFileName = generateUniqueFileName(sanitizedSubFolder, ext);

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
            throw new ValidationException(Constants.ERROR_IMAGE_UPLOAD_FAILED);
        }

        return "uploads/" + sanitizedSubFolder + "/" + uniqueFileName;
    }

    /**
     * Lấy đối tượng File trên đĩa vật lý từ đường dẫn tương đối.
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
        if (cleanedPath.startsWith("uploads/")) {
            cleanedPath = cleanedPath.substring("uploads/".length());
        } else if (cleanedPath.startsWith("/uploads/")) {
            cleanedPath = cleanedPath.substring("/uploads/".length());
        } else if (cleanedPath.startsWith("/")) {
            cleanedPath = cleanedPath.substring(1);
        }

        File file = new File(UPLOAD_ROOT_DIR, cleanedPath.replace("/", File.separator));
        try {
            String canonicalBase = new File(UPLOAD_ROOT_DIR).getCanonicalPath();
            String canonicalFile = file.getCanonicalPath();
            if (!canonicalFile.startsWith(canonicalBase)) {
                return null; // Chặn directory traversal attack
            }
        } catch (IOException e) {
            return null;
        }

        return file;
    }

    /**
     * Xóa file ảnh cũ trên đĩa khi người dùng đổi ảnh hoặc xóa món ăn.
     *
     * @param relativePath đường dẫn tương đối đã lưu trong DB
     * @return true nếu xóa thành công
     */
    public static boolean deleteImage(String relativePath) {
        File file = getImageFile(relativePath);
        if (file != null && file.exists() && file.isFile()) {
            return file.delete();
        }
        return false;
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
