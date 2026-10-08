package service;

import dao.AccountManagerDAO;
import dao.UserDAO;
import java.util.Date;
import java.util.List;
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

    public List<User> getAllUsers() {
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

    public void toggleUserStatus(int userId) {
        userDAO.updateUserStatus(userId);
    }
}
