package utility;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Tiện ích băm và xác thực mật khẩu chuẩn SHA-256 tuân thủ Rule_BOMS.md.
 * Cung cấp cơ chế đối chiếu tương thích ngược (Backward Compatibility)
 * cho các tài khoản kiểm thử ban đầu chưa băm.
 * 
 * @author AD
 */
public final class PasswordUtil {

    private PasswordUtil() {
        // Utility class không cho phép khởi tạo
    }

    /**
     * Băm mật khẩu văn bản thô sang chuỗi Hex SHA-256 (64 ký tự).
     *
     * @param plainPassword Mật khẩu thô cần băm
     * @return Chuỗi băm hex SHA-256, hoặc null nếu đầu vào null
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null) {
            return null;
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(plainPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm is not supported on this JVM", e);
        }
    }

    /**
     * Đối chiếu mật khẩu người dùng nhập vào với mật khẩu trong cơ sở dữ liệu.
     * Hỗ trợ cả mật khẩu đã băm SHA-256 lẫn mật khẩu văn bản thô (backward compatibility).
     *
     * @param plainInput Mật khẩu người dùng nhập vào
     * @param storedPassword Mật khẩu lưu trữ trong CSDL
     * @return true nếu khớp, ngược lại false
     */
    public static boolean checkPassword(String plainInput, String storedPassword) {
        if (plainInput == null || storedPassword == null) {
            return false;
        }
        String hashedInput = hashPassword(plainInput);
        return storedPassword.equals(hashedInput) || storedPassword.equals(plainInput);
    }
}
