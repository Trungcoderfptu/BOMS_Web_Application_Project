/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import java.util.ArrayList;
import java.sql.Date;
import java.util.List;
import model.User;

/**
 *
 * @author AD
 */
public class UserDAO {

    private static List<User> userList = new ArrayList<>();

    static {
        userList.add(new User(1, "admin", "123", "Quản Trị Hệ Thống", "admin@bx.com", "090123", "Admin", Date.valueOf("2026-01-01"), "Hà Nội", true));
        userList.add(new User(2, "staff1", "123", "Nhân Viên Chạy Bàn", "staff@bx.com", "090456", "Staff", Date.valueOf("2026-06-15"), "Hòa Bình", true));
    }

    public List<User> getAllUsers() {
        return userList;
    }

    public User getUserByUsername(String username) {
        for (User u : userList) {
            if (u.getUsername().equals(username)) {
                return u;
            }
        }
        return null;
    }

    public User getUserById(int userId) {
        for (User u : userList) {
            if (u.getUserID() == userId) {
                return u;
            }
        }
        return null;
    }

    public User getUserByEmail(String email) {
        for (User user : userList) {
            if (user.getEmail().equalsIgnoreCase(email)) {
                return user;
            }
        }
        return null;
    }

    public void updateEmail(int userId, String email) {
        User u = getUserById(userId);
        if (u != null) {
            u.setEmail(email);
        }
    }

    public void updatePhone(int userId, String phone) {
        User u = getUserById(userId);
        if (u != null) {
            u.setPrimaryPhone(phone);
        }
    }

    public void updateAddress(int userId, String address) {
        User u = getUserById(userId);
        if (u != null) {
            u.setAddress(address);
        }
    }

    public void updatePassword(int userId, String newPassword) {
        User u = getUserById(userId);
        if (u != null) {
            u.setPassword(newPassword);
        }
    }

    public void updateUserStatus(int userId) {
        for (User user : userList) {
            if (user.getUserID() == userId) {
                user.setIsActive(!user.getActive());
                break;
            }
        }
    }

    public void insertUser(User user) {
        int newId = userList.isEmpty() ? 1 : userList.get(userList.size() - 1).getUserID() + 1;
        user.setUserID(newId);
        userList.add(user);
    }
}
