package dao;

import dal.DBContext;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.AccountManager;
import utility.Constants;
import utility.ValidationException;

public class AccountManagerDAO extends DBContext {

    public List<AccountManager> getAllKeys() throws ValidationException {
        List<AccountManager> list = new ArrayList<>();
        String sql = "SELECT * FROM AccountManager ORDER BY CreatedAt DESC";
        try (PreparedStatement st = connection.prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                AccountManager key = new AccountManager();
                key.setKeyId(rs.getInt("KeyID"));
                key.setSecurityKey(rs.getString("SecurityKey"));
                key.setRole(rs.getString("Role"));

                int uId = rs.getInt("UserID");
                if (rs.wasNull()) {
                    key.setUserId(null);
                } else {
                    key.setUserId(uId);
                }

                key.setIsActive(rs.getBoolean("IsActive"));
                key.setCreatedAt(new java.util.Date(rs.getTimestamp("CreatedAt").getTime()));
                list.add(key);
            }
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_GETALL_KEY);
        }
        return list;
    }

    public AccountManager getKeyBySecurityKey(String securityKey) throws ValidationException {
        String sql = "SELECT * FROM AccountManager WHERE SecurityKey = ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setString(1, securityKey);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    AccountManager key = new AccountManager();
                    key.setKeyId(rs.getInt("KeyID"));
                    key.setSecurityKey(rs.getString("SecurityKey"));
                    key.setRole(rs.getString("Role"));

                    int uId = rs.getInt("UserID");
                    if (rs.wasNull()) {
                        key.setUserId(null);
                    } else {
                        key.setUserId(uId);
                    }

                    key.setIsActive(rs.getBoolean("IsActive"));
                    key.setCreatedAt(new java.util.Date(rs.getTimestamp("CreatedAt").getTime()));
                    return key;
                }
            }
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_GETALL_KEY);
        }
        return null;
    }

    public void insertKey(AccountManager am) throws ValidationException {
        String sql = "INSERT INTO AccountManager (SecurityKey, Role, UserID, IsActive, CreatedAt) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setString(1, am.getSecurityKey());
            st.setString(2, am.getRole());

            if (am.getUserId() == null) {
                st.setNull(3, java.sql.Types.INTEGER);
            } else {
                st.setInt(3, am.getUserId());
            }

            st.setBoolean(4, am.getActive());
            st.setTimestamp(5, new java.sql.Timestamp(am.getCreatedAt().getTime()));
            st.executeUpdate();
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_INSERT_KEY);
        }
    }

    public void updateUserIdForKey(String securityKey, int userId) throws ValidationException {
        String sql = "UPDATE AccountManager SET UserID = ? WHERE SecurityKey = ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, userId);
            st.setString(2, securityKey);
            st.executeUpdate();
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_UPDATE_UID_FOR_KEY);
        }
    }

    public void updateRoleByUserId(int userId, String newRole) throws ValidationException {
        String sql = "UPDATE AccountManager SET Role = ? WHERE UserID = ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setString(1, newRole);
            st.setInt(2, userId);
            st.executeUpdate();
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_UPDATE_UID_FOR_KEY);
        }
    }

    public int revokeUnusedKey(int keyId) throws ValidationException {
        String sql = "UPDATE AccountManager SET IsActive = 0 WHERE KeyID = ? AND UserID IS NULL AND IsActive = 1";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, keyId);
            return st.executeUpdate();
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_UPDATE_UID_FOR_KEY);
        }
    }
}
