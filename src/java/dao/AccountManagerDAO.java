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
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            ResultSet rs = st.executeQuery();
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
            throw new ValidationException(Constants.ERROR_DATABASE_GETALL_KEY + e.getMessage());
        }
        return list;
    }

    public void insertKey(AccountManager am) throws ValidationException {
        String sql = "INSERT INTO AccountManager (SecurityKey, Role, UserID, IsActive, CreatedAt) VALUES (?, ?, ?, ?, ?)";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
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
            throw new ValidationException(Constants.ERROR_DATABASE_INSERT_KEY + e.getMessage());
        }
    }
}
