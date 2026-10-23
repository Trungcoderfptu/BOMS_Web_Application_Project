package dao;

import dal.DBContext;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.User;
import utility.Constants;
import utility.ValidationException;

/**
 * Data Access Object quản lý bảng Users kết nối MS SQL Server
 * @author AD
 */
public class UserDAO extends DBContext {

    public List<User> getAllUsers() throws ValidationException {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM Users ORDER BY UserID DESC";

        try (PreparedStatement st = connection.prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {
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
                user.setAvatar(getSafeAvatar(rs));
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
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setString(1, username);
            try (ResultSet rs = st.executeQuery()) {
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
                    user.setAvatar(getSafeAvatar(rs));
                    user.setIsActive(rs.getBoolean("IsActive"));

                    return user;
                }
            }
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_GET_USER_BY_USERNAME);
        }
        return null;
    }

    public User getUserById(int userId) throws ValidationException {
        String sql = "SELECT * FROM Users WHERE UserID = ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, userId);
            try (ResultSet rs = st.executeQuery()) {
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
                    user.setAvatar(getSafeAvatar(rs));
                    user.setIsActive(rs.getBoolean("IsActive"));

                    return user;
                }
            }
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_GET_USER_BY_ID);
        }
        return null;
    }

    public User getUserByEmail(String email) throws ValidationException {
        String sql = "SELECT * FROM Users WHERE PrimaryEmail = ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setString(1, email);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setUserID(rs.getInt("UserID"));
                    user.setEmail(rs.getString("PrimaryEmail"));
                    return user;
                }
            }
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_GET_USER_BY_EMAIL);
        }
        return null;
    }

    public void updateEmail(int userId, String email) throws ValidationException {
        String sql = "UPDATE Users SET PrimaryEmail = ? WHERE UserID = ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setString(1, email);
            st.setInt(2, userId);
            st.executeUpdate();
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_UPDATE_INFO);
        }
    }

    public void updatePhone(int userId, String phone) throws ValidationException {
        String sql = "UPDATE Users SET PrimaryPhone = ? WHERE UserID = ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setString(1, phone);
            st.setInt(2, userId);
            st.executeUpdate();
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_UPDATE_INFO);
        }
    }

    public void updateAddress(int userId, String address) throws ValidationException {
        String sql = "UPDATE Users SET Address = ? WHERE UserID = ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setString(1, address);
            st.setInt(2, userId);
            st.executeUpdate();
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_UPDATE_INFO);
        }
    }

    public void updatePassword(int userId, String newPassword) throws ValidationException {
        String sql = "UPDATE Users SET Password = ? WHERE UserID = ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setString(1, newPassword);
            st.setInt(2, userId);
            st.executeUpdate();
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_UPDATE_INFO);
        }
    }

    public void updateUserStatus(int userId) throws ValidationException {
        String sql = "UPDATE Users SET IsActive = ~IsActive WHERE UserID = ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, userId);
            st.executeUpdate();
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_UPDATE_STATUS);
        }
    }

    public void insertUser(User user) throws ValidationException {
        String sql = "INSERT INTO Users (Username, Password, FullName, PrimaryEmail, PrimaryPhone, Role, HireDate, Address, IsActive) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement st = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
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
            try (ResultSet rs = st.getGeneratedKeys()) {
                if (rs.next()) {
                    user.setUserID(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_INSERT_USER);
        }
    }

    public void registerUserWithKeyTransaction(User user, String securityKey) throws ValidationException {
        String insertUserSql = "INSERT INTO Users (Username, Password, FullName, PrimaryEmail, PrimaryPhone, Role, HireDate, Address, IsActive) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String updateKeySql = "UPDATE AccountManager SET UserID = ? WHERE SecurityKey = ? AND UserID IS NULL";

        boolean originalAutoCommit = true;
        try {
            originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            int newUserId = 0;
            try (PreparedStatement stUser = connection.prepareStatement(insertUserSql, Statement.RETURN_GENERATED_KEYS)) {
                stUser.setString(1, user.getUsername());
                stUser.setString(2, user.getPassword());
                stUser.setString(3, user.getFullName());
                if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                    stUser.setString(4, user.getEmail());
                } else {
                    stUser.setNull(4, java.sql.Types.VARCHAR);
                }
                stUser.setString(5, user.getPrimaryPhone());
                stUser.setString(6, user.getRole());
                stUser.setDate(7, user.getHireDate());
                stUser.setString(8, user.getAddress());
                stUser.setBoolean(9, user.getActive());
                stUser.executeUpdate();
                try (ResultSet rs = stUser.getGeneratedKeys()) {
                    if (rs.next()) {
                        newUserId = rs.getInt(1);
                        user.setUserID(newUserId);
                    }
                }
            }

            if (newUserId <= 0) {
                connection.rollback();
                throw new ValidationException(Constants.ERROR_DATABASE_INSERT_USER);
            }

            int rowsUpdated = 0;
            try (PreparedStatement stKey = connection.prepareStatement(updateKeySql)) {
                stKey.setInt(1, newUserId);
                stKey.setString(2, securityKey);
                rowsUpdated = stKey.executeUpdate();
            }

            if (rowsUpdated == 0) {
                connection.rollback();
                throw new ValidationException(Constants.ERROR_DATABASE_UPDATE_UID_FOR_KEY);
            }

            if ("Shipper".equalsIgnoreCase(user.getRole())) {
                String insertShipperSql = "IF NOT EXISTS (SELECT 1 FROM ShipperProfiles WHERE UserID = ?) "
                        + "INSERT INTO ShipperProfiles (UserID, ShipperStatus, ActiveOrders, MaxOrders) VALUES (?, 'Offline', 0, 3)";
                try (PreparedStatement stShipper = connection.prepareStatement(insertShipperSql)) {
                    stShipper.setInt(1, newUserId);
                    stShipper.setInt(2, newUserId);
                    stShipper.executeUpdate();
                }
            }

            connection.commit();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
            }
            throw new ValidationException(Constants.ERROR_DATABASE_INSERT_USER);
        } finally {
            try {
                connection.setAutoCommit(originalAutoCommit);
            } catch (SQLException ex) {
            }
        }
    }

    public List<Integer> getAllActiveUserIds() throws ValidationException {
        List<Integer> list = new ArrayList<>();
        String sql = "SELECT UserID FROM Users WHERE IsActive = 1";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(rs.getInt("UserID"));
            }
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_GETALL_USER);
        }
        return list;
    }

    public List<Integer> getActiveManagerAndStaffUserIds() throws ValidationException {
        List<Integer> list = new ArrayList<>();
        String sql = "SELECT UserID FROM Users WHERE IsActive = 1 AND Role IN ('Manager', 'Staff')";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(rs.getInt("UserID"));
            }
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_GETALL_USER);
        }
        return list;
    }

    public void updateAvatar(int userId, String avatarUrl) throws ValidationException {
        String sql = "UPDATE Users SET Avatar = ? WHERE UserID = ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            if (avatarUrl != null && !avatarUrl.trim().isEmpty()) {
                st.setString(1, avatarUrl.trim());
            } else {
                st.setNull(1, java.sql.Types.VARCHAR);
            }
            st.setInt(2, userId);
            st.executeUpdate();
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_UPDATE_INFO);
        }
    }

    public void updateUserRole(int userId, String newRole) throws ValidationException {
        String sql = "UPDATE Users SET Role = ? WHERE UserID = ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setString(1, newRole);
            st.setInt(2, userId);
            st.executeUpdate();
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_UPDATE_STATUS);
        }
    }

    public void ensureShipperProfileExists(int userId) throws ValidationException {
        String checkSql = "SELECT COUNT(1) FROM ShipperProfiles WHERE UserID = ?";
        String insertSql = "INSERT INTO ShipperProfiles (UserID, ShipperStatus, ActiveOrders, MaxOrders) VALUES (?, 'Offline', 0, 3)";
        try {
            try (PreparedStatement st = connection.prepareStatement(checkSql)) {
                st.setInt(1, userId);
                try (ResultSet rs = st.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        return;
                    }
                }
            }
            try (PreparedStatement st = connection.prepareStatement(insertSql)) {
                st.setInt(1, userId);
                st.executeUpdate();
            }
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_UPDATE_STATUS);
        }
    }

    public boolean isShipperHavingActiveOrders(int userId) throws ValidationException {
        String sql = "SELECT ActiveOrders FROM ShipperProfiles WHERE UserID = ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, userId);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ActiveOrders") > 0;
                }
            }
            return false;
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_UPDATE_STATUS);
        }
    }

    public void setShipperOffline(int userId) throws ValidationException {
        String sql = "UPDATE ShipperProfiles SET ShipperStatus = 'Offline' WHERE UserID = ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, userId);
            st.executeUpdate();
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_UPDATE_STATUS);
        }
    }

    private String getSafeAvatar(ResultSet rs) {
        try {
            return rs.getString("Avatar");
        } catch (SQLException ignored) {
            return null;
        }
    }
}
