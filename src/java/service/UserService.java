/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import dao.AccountManagerDAO;
import dao.UserDAO;
import java.util.HashMap;
import java.util.Map;
import model.AccountManager;
import model.User;
import utility.Constants;
import utility.ValidationException;
import utility.Validator;

/**
 *
 * @author AD
 */
public class UserService {

    private UserDAO userDAO;
    private AccountManagerDAO accountDAO;

    public UserService() {
        this.userDAO = new UserDAO();
        this.accountDAO = new AccountManagerDAO();
    }

    // ========== Main service ==========
    public Map<String, String> login(String username, String password, String role) throws ValidationException {
        validateUserName(username);
        validatePassword(password);
        validateRole(role);
        User user = userDAO.getUserByUsername(username);
        if (user == null) {
            throw new ValidationException(Constants.ERROR_LOGIN_FALL); // Sai username
        }
        if (!user.getActive()) {
            throw new ValidationException(Constants.ERROR_LOCK_ACC); // Tài khoản bị khóa
        }
        if (!user.getPassword().equals(password)) {
            throw new ValidationException(Constants.ERROR_LOGIN_FALL); // Sai password
        }
        if (!user.getRole().equalsIgnoreCase(role)) {
            throw new ValidationException(Constants.ERROR_LOGIN_FAIL_ROLE); // Sai quyền
        }
        return buildUserSessionMap(user);
    }

    public Map<String, String> getUserProfile(int userId) throws ValidationException {
        validateUserId(userId);
        User user = userDAO.getUserById(userId);
        if (user == null) {
            throw new ValidationException(Constants.ERROR_PROFILE);
        }
        if (!user.isActive()) {
            throw new ValidationException(Constants.ERROR_LOGIN_FALL); // Hoặc tạo 1 mã E mới cho "Tài khoản bị khóa"
        }
        return buildUserProfileMap(user);
    }

    public void updateProfile(int userId, String email, String phone, String address) throws ValidationException {
        validateUserId(userId);
        validateEmail(email);
        validatePhoneNumber(phone);
        validateAddress(address);
        User user = userDAO.getUserById(userId);
        if (user == null || !user.isActive()) {
            throw new ValidationException(Constants.ERROR_PROFILE);
        }
        userDAO.updateEmail(userId, email);
        userDAO.updatePhone(userId, phone);
        userDAO.updateAddress(userId, address);
    }

    public void changePassword(int userId, String oldPassword, String newPassword, String confirmPassword) throws ValidationException {
        validateUserId(userId);
        validatePassword(newPassword); // Chỉ gọi 1 lần là đủ bao xài (rỗng, độ dài, format)
        if (!newPassword.equals(confirmPassword)) {
            throw new ValidationException(Constants.ERROR_CONFIRM_PSW);
        }
        User user = userDAO.getUserById(userId);
        if (user == null || !user.isActive() || !user.getPassword().equals(oldPassword)) {
            throw new ValidationException(Constants.ERROR_WORONG_OPW);
        }

        if (newPassword.equals(user.getPassword())) {
            throw new ValidationException(Constants.ERROR_NEW_PSW_MATCH_OLD);
        }
        userDAO.updatePassword(userId, newPassword);
    }

    public void register(String username, String fullName, String password, String confirmPassword, String email, String phone, String address, String securityId) throws ValidationException {
        validateUserName(username);
        Validator.checkEmpty(fullName, Constants.ERROR_EMPTY_FULLNAME);
        validatePassword(password);
        validateEmail(email);
        validatePhoneNumber(phone);
        validateAddress(address);
        Validator.checkEmpty(securityId, Constants.ERROR_INVALID_SECURITY_CODE); // Check rỗng trước khi chạm vào DB
        if (!password.equals(confirmPassword)) {
            throw new ValidationException(Constants.ERROR_CONFIRM_PSW);
        }
        User existingUser = userDAO.getUserByUsername(username);
        if (existingUser != null) {
            throw new ValidationException(Constants.ERROR_USERNAME_EXIST);
        }
        User existingEmail = userDAO.getUserByEmail(email);
        if (existingEmail != null) {
            throw new ValidationException(Constants.ERROR_EMAIL_EXIST);
        }
        AccountManager validKey = findSecurityKey(securityId);
        validateSecurityKey(validKey);
        String assignedRole = validKey.getRole();
        User newUser = new User(0, username, password, fullName, email, phone, assignedRole, new java.sql.Date(System.currentTimeMillis()), address, true);
        userDAO.insertUser(newUser);
        validKey.setUserId(newUser.getUserID());
    }

    public void toggleUserStatus(int userId) {
        userDAO.updateUserStatus(userId);
    }
    //========== create respon pack to other service==========

    private Map<String, String> buildUserSessionMap(User user) {
        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("userId", String.valueOf(user.getUserID()));
        userInfo.put("username", user.getUsername());
        userInfo.put("fullName", user.getFullName());
        userInfo.put("role", user.getRole());
        return userInfo;
    }

    private Map<String, String> buildUserProfileMap(User user) {
        Map<String, String> profile = new HashMap<>();
        profile.put("username", user.getUsername());
        profile.put("fullName", user.getFullName());
        profile.put("email", user.getEmail() != null ? user.getEmail() : Constants.ERROR_NOT_UPDATED);
        profile.put("phone", user.getPrimaryPhone() != null ? user.getPrimaryPhone() : Constants.ERROR_NOT_UPDATED);
        profile.put("role", user.getRole());
        profile.put("hireDate", user.getHireDate() != null ? user.getHireDate().toString() : "");
        profile.put("address", user.getAddress() != null ? user.getAddress() : Constants.ERROR_NOT_UPDATED);
        return profile;
    }

    // orther support method
    private AccountManager findSecurityKey(String securityId) {
        for (AccountManager key : accountDAO.getAllKeys()) {
            if (key.getSecurityKey().equals(securityId)) {
                return key;
            }
        }
        return null; // Không thấy thì trả về null
    }

    //==========validation service==========
    private void validateUserName(String userName) throws ValidationException {
        Validator.checkEmpty(userName, Constants.ERROR_USERNAME);
        Validator.checkValidUsername(userName, Constants.ERROR_USERNAME);
        Validator.checkMinLength(userName, Constants.MIN_USER_LENG, Constants.ERROR_USER_LENG);
    }

    public void validatePassword(String password) throws ValidationException {
        Validator.checkEmpty(password, Constants.ERROR_PASSWORD);
        Validator.checkMinLength(password, Constants.MIN_PASSWORD_LENG, Constants.ERROR_PASSWORD);
        Validator.checkValidPassword(password, Constants.ERROR_INVALID_PSW);
    }

    private void validateRole(String role) throws ValidationException {
        Validator.checkEmpty(role, Constants.ERROR_ROLE);
    }

    private void validateEmail(String email) throws ValidationException {
        Validator.checkEmpty(email, Constants.ERROR_EMPTY_EMAIL);
        Validator.checkEmailFormat(email, Constants.ERROR_INVALID_EMAIL_FORMAT);
    }

    private void validatePhoneNumber(String phone) throws ValidationException {
        Validator.checkEmpty(phone, Constants.ERROR_EMPTY_PHONE);
        Validator.checkPhoneFormat(phone, Constants.ERROR_INVALID_PHONE_FORMAT);
    }

    private void validateAddress(String address) throws ValidationException {
        Validator.checkEmpty(address, Constants.ERROR_EMPTY_ADDRESS);
    }

    private void validateUserId(int userId) throws ValidationException {
        if (userId <= 0) {
            throw new ValidationException(Constants.ERROR_PROFILE);
        }
    }

    private void validateSecurityKey(AccountManager key) throws ValidationException {
        if (key == null || !key.getActive() || key.getUserId() != null) {
            throw new ValidationException(Constants.ERROR_INVALID_SECURITY_CODE);
        }
    }
}
