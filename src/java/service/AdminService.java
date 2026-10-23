package service;

import dao.AccountManagerDAO;
import dao.UserDAO;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import model.AccountManager;
import model.User;
import utility.Constants;
import utility.ValidationException;
import utility.Validator;

public class AdminService {

    private UserDAO userDAO;
    private AccountManagerDAO accountDAO;
    private UserService userService;

    public AdminService() {
        this.userDAO = new UserDAO();
        this.accountDAO = new AccountManagerDAO();
        this.userService = new UserService();
    }

    public List<User> getAllUsers() throws ValidationException {
        return userDAO.getAllUsers();
    }

    public List<AccountManager> getAllKeys() throws ValidationException {
        return accountDAO.getAllKeys();
    }

    public void generateSecurityKey(String role) throws ValidationException {
        String randomPart = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        String securityKey = "BOMS-" + role.toUpperCase().substring(0, 3) + "-" + randomPart;
        AccountManager newKey = new AccountManager(0, securityKey, role, null, true, new java.sql.Date(System.currentTimeMillis()));
        accountDAO.insertKey(newKey);
    }

    public void toggleUserStatus(int userId) throws ValidationException {
        userDAO.updateUserStatus(userId);
    }

    //Ép kiểu obj ném lên tầng trên để bảo toàn nguyên tắc dự án
    public List<Map<String, Object>> getAllUsersForView() throws ValidationException {
        List<User> users = userDAO.getAllUsers();
        List<Map<String, Object>> result = new ArrayList<>();
        for (User u : users) {
            Map<String, Object> map = new HashMap<>();
            map.put("userID", u.getUserID());
            map.put("username", u.getUsername());
            map.put("fullName", u.getFullName());
            map.put("role", u.getRole());
            map.put("active", u.getActive()); // JSTL sẽ đọc được key này
            map.put("hireDate", u.getHireDate());
            result.add(map);
        }
        return result;
    }

    public List<Map<String, Object>> getAllKeysForView() throws ValidationException {
        List<AccountManager> keys = accountDAO.getAllKeys();
        List<Map<String, Object>> result = new ArrayList<>();
        for (AccountManager k : keys) {
            Map<String, Object> map = new HashMap<>();
            map.put("keyId", k.getKeyId());
            map.put("securityKey", k.getSecurityKey());
            map.put("role", k.getRole());
            map.put("userId", k.getUserId());
            map.put("active", k.getActive());
            map.put("createdAt", k.getCreatedAt());
            result.add(map);
        }
        return result;
    }

    public void resetUserPasswordByAdmin(int adminUserId, String targetUserIdStr, String newPassword, String confirmPassword) throws ValidationException {
        Validator.checkEmpty(targetUserIdStr, Constants.ERROR_USER_NOT_FOUND);
        int targetUserId;
        try {
            targetUserId = Integer.parseInt(targetUserIdStr.trim());
        } catch (NumberFormatException e) {
            throw new ValidationException(Constants.ERROR_USER_NOT_FOUND);
        }

        if (adminUserId == targetUserId) {
            throw new ValidationException(Constants.ERROR_ADMIN_CANNOT_RESET_SELF_PSW);
        }

        User targetUser = userDAO.getUserById(targetUserId);
        if (targetUser == null) {
            throw new ValidationException(Constants.ERROR_USER_NOT_FOUND);
        }

        userService.validatePassword(newPassword);
        if (confirmPassword == null || !newPassword.equals(confirmPassword)) {
            throw new ValidationException(Constants.ERROR_CONFIRM_PSW);
        }

        String hashedPassword = utility.PasswordUtil.hashPassword(newPassword);
        userDAO.updatePassword(targetUserId, hashedPassword);
    }

    public void changeUserRoleByAdmin(int adminUserId, String targetUserIdStr, String newRole) throws ValidationException {
        Validator.checkEmpty(targetUserIdStr, Constants.ERROR_USER_NOT_FOUND);
        Validator.checkEmpty(newRole, Constants.ERROR_INVALID_ROLE_SELECTION);
        int targetUserId;
        try {
            targetUserId = Integer.parseInt(targetUserIdStr.trim());
        } catch (NumberFormatException e) {
            throw new ValidationException(Constants.ERROR_USER_NOT_FOUND);
        }

        if (adminUserId == targetUserId) {
            throw new ValidationException(Constants.ERROR_ADMIN_CANNOT_CHANGE_SELF_ROLE);
        }

        String trimmedRole = newRole.trim();
        if (!"Manager".equalsIgnoreCase(trimmedRole) && !"Staff".equalsIgnoreCase(trimmedRole) && !"Shipper".equalsIgnoreCase(trimmedRole)) {
            throw new ValidationException(Constants.ERROR_INVALID_ROLE_SELECTION);
        }

        User targetUser = userDAO.getUserById(targetUserId);
        if (targetUser == null) {
            throw new ValidationException(Constants.ERROR_USER_NOT_FOUND);
        }

        if ("Shipper".equalsIgnoreCase(targetUser.getRole()) && !"Shipper".equalsIgnoreCase(trimmedRole)) {
            if (userDAO.isShipperHavingActiveOrders(targetUserId)) {
                throw new ValidationException(Constants.ERROR_SHIPPER_HAS_ACTIVE_ORDERS);
            }
            userDAO.setShipperOffline(targetUserId);
        }

        userDAO.updateUserRole(targetUserId, trimmedRole);
        accountDAO.updateRoleByUserId(targetUserId, trimmedRole);

        if ("Shipper".equalsIgnoreCase(trimmedRole)) {
            userDAO.ensureShipperProfileExists(targetUserId);
        }
    }

    public void revokeSecurityKey(String keyIdStr) throws ValidationException {
        Validator.checkEmpty(keyIdStr, Constants.ERROR_KEY_CANNOT_REVOKE);
        int keyId;
        try {
            keyId = Integer.parseInt(keyIdStr.trim());
        } catch (NumberFormatException e) {
            throw new ValidationException(Constants.ERROR_KEY_CANNOT_REVOKE);
        }

        int affected = accountDAO.revokeUnusedKey(keyId);
        if (affected <= 0) {
            throw new ValidationException(Constants.ERROR_KEY_CANNOT_REVOKE);
        }
    }
}
