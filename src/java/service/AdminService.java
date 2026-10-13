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
import utility.ValidationException;

public class AdminService {

    private UserDAO userDAO;
    private AccountManagerDAO accountDAO;

    public AdminService() {
        this.userDAO = new UserDAO();
        this.accountDAO = new AccountManagerDAO();
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
            result.add(map);
        }
        return result;
    }

    public List<Map<String, Object>> getAllKeysForView() throws ValidationException {
        List<AccountManager> keys = accountDAO.getAllKeys();
        List<Map<String, Object>> result = new ArrayList<>();
        for (AccountManager k : keys) {
            Map<String, Object> map = new HashMap<>();
            map.put("securityKey", k.getSecurityKey());
            map.put("role", k.getRole());
            map.put("userId", k.getUserId());
            map.put("active", k.getActive());
            map.put("createdAt", k.getCreatedAt());
            result.add(map);
        }
        return result;
    }
}
