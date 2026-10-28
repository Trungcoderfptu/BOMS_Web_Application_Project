package dao;

import dal.DBContext;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.MenuSize;
import model.MenuTopping;
import utility.Constants;
import utility.ValidationException;

/**
 * Data Access Object cho tuỳ chọn món dùng chung: bảng MenuSizes và MenuToppings.
 * Không xóa cứng (đơn cũ đã lưu bản sao tên size/topping), chỉ bật/tắt IsActive.
 */
public class MenuOptionDAO extends DBContext {

    // ==========================================
    // ========== 1. SIZE =======================
    // ==========================================
    public List<MenuSize> getSizes(boolean activeOnly) throws ValidationException {
        List<MenuSize> list = new ArrayList<>();
        String sql = "SELECT SizeID, SizeName, ExtraPrice, SortOrder, IsActive FROM MenuSizes "
                + (activeOnly ? "WHERE IsActive = 1 " : "")
                + "ORDER BY SortOrder ASC, ExtraPrice ASC, SizeID ASC";
        try (PreparedStatement st = connection.prepareStatement(sql);
                ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                list.add(mapSize(rs));
            }
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_MENU_OPTION);
        }
        return list;
    }

    public MenuSize getSizeById(int sizeId) throws ValidationException {
        String sql = "SELECT SizeID, SizeName, ExtraPrice, SortOrder, IsActive FROM MenuSizes WHERE SizeID = ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, sizeId);
            try (ResultSet rs = st.executeQuery()) {
                return rs.next() ? mapSize(rs) : null;
            }
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_MENU_OPTION);
        }
    }

    public boolean isSizeNameExists(String sizeName, int excludeSizeId) throws ValidationException {
        String sql = "SELECT 1 FROM MenuSizes WHERE LOWER(SizeName) = LOWER(?) AND SizeID != ?";
        return existsByName(sql, sizeName, excludeSizeId);
    }

    public void insertSize(MenuSize size) throws ValidationException {
        String sql = "INSERT INTO MenuSizes (SizeName, ExtraPrice, SortOrder, IsActive) VALUES (?, ?, ?, 1)";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setNString(1, size.getSizeName());
            st.setDouble(2, size.getExtraPrice());
            st.setInt(3, size.getSortOrder());
            st.executeUpdate();
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_MENU_OPTION);
        }
    }

    public void updateSize(MenuSize size) throws ValidationException {
        String sql = "UPDATE MenuSizes SET SizeName = ?, ExtraPrice = ?, SortOrder = ? WHERE SizeID = ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setNString(1, size.getSizeName());
            st.setDouble(2, size.getExtraPrice());
            st.setInt(3, size.getSortOrder());
            st.setInt(4, size.getSizeId());
            st.executeUpdate();
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_MENU_OPTION);
        }
    }

    public void toggleSizeActive(int sizeId) throws ValidationException {
        String sql = "UPDATE MenuSizes SET IsActive = CASE WHEN IsActive = 1 THEN 0 ELSE 1 END WHERE SizeID = ?";
        executeToggle(sql, sizeId);
    }

    // ==========================================
    // ========== 2. TOPPING ====================
    // ==========================================
    public List<MenuTopping> getToppings(boolean activeOnly) throws ValidationException {
        List<MenuTopping> list = new ArrayList<>();
        String sql = "SELECT ToppingID, ToppingName, Price, IsActive FROM MenuToppings "
                + (activeOnly ? "WHERE IsActive = 1 " : "")
                + "ORDER BY ToppingName ASC";
        try (PreparedStatement st = connection.prepareStatement(sql);
                ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                list.add(mapTopping(rs));
            }
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_MENU_OPTION);
        }
        return list;
    }

    public MenuTopping getToppingById(int toppingId) throws ValidationException {
        String sql = "SELECT ToppingID, ToppingName, Price, IsActive FROM MenuToppings WHERE ToppingID = ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, toppingId);
            try (ResultSet rs = st.executeQuery()) {
                return rs.next() ? mapTopping(rs) : null;
            }
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_MENU_OPTION);
        }
    }

    public boolean isToppingNameExists(String toppingName, int excludeToppingId) throws ValidationException {
        String sql = "SELECT 1 FROM MenuToppings WHERE LOWER(ToppingName) = LOWER(?) AND ToppingID != ?";
        return existsByName(sql, toppingName, excludeToppingId);
    }

    public void insertTopping(MenuTopping topping) throws ValidationException {
        String sql = "INSERT INTO MenuToppings (ToppingName, Price, IsActive) VALUES (?, ?, 1)";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setNString(1, topping.getToppingName());
            st.setDouble(2, topping.getPrice());
            st.executeUpdate();
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_MENU_OPTION);
        }
    }

    public void updateTopping(MenuTopping topping) throws ValidationException {
        String sql = "UPDATE MenuToppings SET ToppingName = ?, Price = ? WHERE ToppingID = ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setNString(1, topping.getToppingName());
            st.setDouble(2, topping.getPrice());
            st.setInt(3, topping.getToppingId());
            st.executeUpdate();
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_MENU_OPTION);
        }
    }

    public void toggleToppingActive(int toppingId) throws ValidationException {
        String sql = "UPDATE MenuToppings SET IsActive = CASE WHEN IsActive = 1 THEN 0 ELSE 1 END WHERE ToppingID = ?";
        executeToggle(sql, toppingId);
    }

    // ==========================================
    // ========== 3. HELPERS ====================
    // ==========================================
    private boolean existsByName(String sql, String name, int excludeId) throws ValidationException {
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setNString(1, name.trim());
            st.setInt(2, excludeId);
            try (ResultSet rs = st.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_MENU_OPTION);
        }
    }

    private void executeToggle(String sql, int id) throws ValidationException {
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, id);
            st.executeUpdate();
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_MENU_OPTION);
        }
    }

    private MenuSize mapSize(ResultSet rs) throws SQLException {
        return new MenuSize(rs.getInt("SizeID"), rs.getNString("SizeName"), rs.getDouble("ExtraPrice"),
                rs.getInt("SortOrder"), rs.getBoolean("IsActive"));
    }

    private MenuTopping mapTopping(ResultSet rs) throws SQLException {
        return new MenuTopping(rs.getInt("ToppingID"), rs.getNString("ToppingName"), rs.getDouble("Price"),
                rs.getBoolean("IsActive"));
    }
}
