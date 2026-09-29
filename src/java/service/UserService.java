/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import dao.UserDAO;
import java.util.HashMap;
import java.util.Map;
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

    public UserService() {
        this.userDAO = new UserDAO();
    }

    public Map<String, String> login(String username, String password, String role) throws ValidationException {

        Validator.checkEmpty(username, Constants.ERROR_MESSAGE_USERNAME);
        Validator.checkValidUsername(username, Constants.ERROR_MESSAGE_USERNAME);

        Validator.checkEmpty(password, Constants.ERROR_MESSAGE_PASSWORD);
        Validator.checkMinLength(password, Constants.minLeng, Constants.ERROR_MESSAGE_PASSWORD);

        Validator.checkEmpty(role, Constants.ERROR_MESSAGE_ROLE);

        User user = userDAO.checkLogin(username, password);

        if (user == null) {
            throw new ValidationException(Constants.ERROR_MESSAGE_LOGIN_FALL);
        }

        if (!user.getRole().equalsIgnoreCase(role)) {
            throw new ValidationException(Constants.ERROR_MESSAGE_LOGIN_FAIL_ROLE);
        }

        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("username", user.getUsername());
        userInfo.put("fullName", user.getFullName());
        userInfo.put("role", user.getRole());

        return userInfo;
    }

    //Lấy THông tin user 
    public Map<String, String> getUserProfile(String username) throws ValidationException {
        User user = userDAO.getUserByUsername(username);

        if (user == null) {
            throw new ValidationException(Constants.ERROR_MESSAGE_PROFILE);
        }

        Map<String, String> profile = new HashMap<>();
        profile.put("username", user.getUsername());
        profile.put("fullName", user.getFullName());
        profile.put("email", user.getPrimaryEmail() != null ? user.getPrimaryEmail() : "Chưa cập nhật");
        profile.put("phone", user.getPrimaryPhone() != null ? user.getPrimaryPhone() : "Chưa cập nhật");
        profile.put("role", user.getRole());
        profile.put("hireDate", user.getHireDate() != null ? user.getHireDate().toString() : "");
        profile.put("address", user.getAddress() != null ? user.getAddress() : "Chưa cập nhật");

        return profile;
    }

    public void updateProfile(String username, String email, String phone, String address) throws ValidationException {
        // Tạm thời chưa check format quá gắt, chỉ cập nhật xuống DAO
        userDAO.updateProfile(username, email, phone, address);
    }

    public void changePassword(String username, String oldPassword, String newPassword, String confirmPassword) throws ValidationException {
        User user = userDAO.getUserByUsername(username);

        if (user == null || !user.getPassword().equals(oldPassword)) {
            throw new ValidationException(Constants.ERROR_MESSAGE_PASSWORD_CHANGE_FALL_OLD_PASS);
        }

        Validator.checkEmpty(newPassword, Constants.ERROR_MESSAGE_PASSWORD);
        Validator.checkMinLength(newPassword, Constants.minLeng, Constants.ERROR_MESSAGE_PASSWORD);

        if (!newPassword.equals(confirmPassword)) {
            throw new ValidationException(Constants.ERROR_MESSAGE_PASSWORD_CHANGE_FALL_NEW_PASS);
        }

        userDAO.changePassword(username, newPassword);
    }
}
