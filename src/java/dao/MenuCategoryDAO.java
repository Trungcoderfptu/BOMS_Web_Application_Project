package dao;

import dal.DBContext;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.MenuCategory;
import utility.Constants;
import utility.ValidationException;

/**
 * Data Access Object quản lý dữ liệu bảng MenuCategories kết nối MS SQL Server
 * @author AD
 */
public class MenuCategoryDAO extends DBContext {

    public List<MenuCategory> getAllCategories() throws ValidationException {
        List<MenuCategory> list = new ArrayList<>();
        String sql = "SELECT CategoryID, CategoryName FROM MenuCategories ORDER BY CategoryID ASC";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                list.add(mapResultSetToMenuCategory(rs));
            }
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_GET_PRODUCT);
        }
        return list;
    }

    public MenuCategory getCategoryById(int categoryId) throws ValidationException {
        String sql = "SELECT CategoryID, CategoryName FROM MenuCategories WHERE CategoryID = ?";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setInt(1, categoryId);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return mapResultSetToMenuCategory(rs);
            }
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_GET_PRODUCT);
        }
        return null;
    }

    public MenuCategory getCategoryByName(String categoryName) throws ValidationException {
        String sql = "SELECT CategoryID, CategoryName FROM MenuCategories WHERE LOWER(CategoryName) = LOWER(?)";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setString(1, categoryName.trim());
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return mapResultSetToMenuCategory(rs);
            }
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_GET_PRODUCT);
        }
        return null;
    }

    private MenuCategory mapResultSetToMenuCategory(ResultSet rs) throws SQLException {
        MenuCategory category = new MenuCategory();
        category.setCategoryId(rs.getInt("CategoryID"));
        category.setCategoryName(rs.getString("CategoryName"));
        return category;
    }
}
