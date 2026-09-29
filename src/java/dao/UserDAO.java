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

    public User checkLogin(String username, String password) {
        for (User u : userList) {
            if (u.getUsername().equals(username) && u.getPassword().equals(password) && u.isActive()) {
                return u;
            }
        }
        return null; // Sai tài khoản hoặc đã bị khóa (isActive = false)
    }

    public User getUserByUsername(String username) {
        for (User u : userList) {
            if (u.getUsername().equals(username) && u.isActive()) {
                return u;
            }
        }
        return null;
    }

    public void updateProfile(String username, String email, String phone, String address) {
        User u = getUserByUsername(username);
        if (u != null) {
            u.setPrimaryEmail(email);
            u.setPrimaryPhone(phone);
            u.setAddress(address);
        }
    }

    public void changePassword(String username, String newPassword) {
        User u = getUserByUsername(username);
        if (u != null) {
            u.setPassword(newPassword);
        }
    }
}
