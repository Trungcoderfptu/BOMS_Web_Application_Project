/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import dal.DBContext;
import java.util.ArrayList;
import java.sql.Date;
import java.util.List;
import model.User;
import utility.Constants;
import utility.ValidationException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author AD
 */
public class UserDAO extends DBContext {

    private static List<User> userList = new ArrayList<>();

    static {
    }

    public List<User> getAllUsers() throws ValidationException {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM Users ORDER BY UserID DESC";

        try {
            PreparedStatement st = connection.prepareStatement(sql);
            ResultSet rs = st.executeQuery();

            while (rs.next()) {
                User user = new User();
                user.setUserID(rs.getInt("UserID"));
                user.setUsername(rs.getString("Username"));
                user.setPassword(rs.getString("Password"));
                user.setFullName(rs.getString("FullName"));
                user.setEmail(rs.getString("PrimaryEmail"));
                user.setPrimaryPhone(rs.getString("PrimaryPhone"));
                user.setRole(rs.getString("Role"));

                java.sql.Date hireDate = rs.getDate("HireDate");
                if (rs.wasNull()) {
                    user.setHireDate(null);
                } else {
                    user.setHireDate(hireDate);
                }

                user.setAddress(rs.getString("Address"));
                user.setIsActive(rs.getBoolean("IsActive"));

                list.add(user);
            }
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_GETALL_USER);
        }
        return list;
    }

    public User getUserByUsername(String username) throws ValidationException {
        String sql = "SELECT * FROM Users WHERE Username = ?";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setString(1, username);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setUserID(rs.getInt("UserID"));
                user.setUsername(rs.getString("Username"));
                user.setPassword(rs.getString("Password"));
                user.setFullName(rs.getString("FullName"));
                user.setEmail(rs.getString("PrimaryEmail"));
                user.setPrimaryPhone(rs.getString("PrimaryPhone"));
                user.setRole(rs.getString("Role"));
                java.sql.Date hireDate = rs.getDate("HireDate");
                if (rs.wasNull()) {
                    user.setHireDate(null);
                } else {
                    user.setHireDate(hireDate);
                }

                user.setAddress(rs.getString("Address"));
                user.setIsActive(rs.getBoolean("IsActive"));

                return user; // Tìm thấy thì trả về User
            }
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_GET_USER_BY_USERNAME);
        }
        return null;
    }

    public User getUserById(int userId) throws ValidationException {
        String sql = "SELECT * FROM Users WHERE UserID = ?";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setInt(1, userId);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setUserID(rs.getInt("UserID"));
                user.setUsername(rs.getString("Username"));
                user.setPassword(rs.getString("Password"));
                user.setFullName(rs.getString("FullName"));
                user.setEmail(rs.getString("PrimaryEmail"));
                user.setPrimaryPhone(rs.getString("PrimaryPhone"));
                user.setRole(rs.getString("Role"));
                java.sql.Date hireDate = rs.getDate("HireDate");
                if (rs.wasNull()) {
                    user.setHireDate(null);
                } else {
                    user.setHireDate(hireDate);
                }

                user.setAddress(rs.getString("Address"));
                user.setIsActive(rs.getBoolean("IsActive"));

                return user; // Tìm thấy thì trả về
            }
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_GET_USER_BY_ID);
        }
        return null;
    }

    public User getUserByEmail(String email) throws ValidationException {
        String sql = "SELECT * FROM Users WHERE PrimaryEmail = ?";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setString(1, email);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setUserID(rs.getInt("UserID"));
                user.setEmail(rs.getString("PrimaryEmail"));
                return user;
            }
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_GET_USER_BY_EMAIL);
        }
        return null;
    }

    public void updateEmail(int userId, String email) throws ValidationException {
        String sql = "UPDATE Users SET PrimaryEmail = ? WHERE UserID = ?";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setString(1, email);
            st.setInt(2, userId);
            st.executeUpdate();
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_UPDATE_INFO);
        }
    }

    public void updatePhone(int userId, String phone) throws ValidationException {
        String sql = "UPDATE Users SET PrimaryPhone = ? WHERE UserID = ?";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setString(1, phone);
            st.setInt(2, userId);
            st.executeUpdate();
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_UPDATE_INFO);
        }
    }

    public void updateAddress(int userId, String address) throws ValidationException {
        String sql = "UPDATE Users SET Address = ? WHERE UserID = ?";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setString(1, address);
            st.setInt(2, userId);
            st.executeUpdate();
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_UPDATE_INFO);
        }
    }

    public void updatePassword(int userId, String newPassword) throws ValidationException {
        String sql = "UPDATE Users SET Password = ? WHERE UserID = ?";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setString(1, newPassword);
            st.setInt(2, userId);
            st.executeUpdate();
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_UPDATE_INFO);
        }
    }

    public void updateUserStatus(int userId) throws ValidationException {
        String sql = "UPDATE Users SET IsActive = ~IsActive WHERE UserID = ?";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setInt(1, userId);
            st.executeUpdate();
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_UPDATE_STATUS);
        }
    }

    public void insertUser(User user) throws ValidationException {
        String sql = "INSERT INTO Users (Username, Password, FullName, PrimaryEmail, PrimaryPhone, Role, HireDate, Address, IsActive) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement st = connection.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS);
            st.setString(1, user.getUsername());
            st.setString(2, user.getPassword());
            st.setString(3, user.getFullName());
            if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                st.setString(4, user.getEmail());
            } else {
                st.setNull(4, java.sql.Types.VARCHAR);
            }
            st.setString(5, user.getPrimaryPhone());
            st.setString(6, user.getRole());
            st.setDate(7, user.getHireDate());
            st.setString(8, user.getAddress());
            st.setBoolean(9, user.getActive());
            st.executeUpdate();
            ResultSet rs = st.getGeneratedKeys();
            if (rs.next()) {
                user.setUserID(rs.getInt(1));
            }
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_INSERT_USER);
        }
    }
}
