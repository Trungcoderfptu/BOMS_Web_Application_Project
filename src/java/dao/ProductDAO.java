package dao;

import dal.DBContext;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.Product;
import utility.Constants;
import utility.ValidationException;

/**
 * Data Access Object quản lý dữ liệu bảng Products kết nối MS SQL Server
 * @author AD
 */
public class ProductDAO extends DBContext {

    public List<Product> getAllProducts() throws ValidationException {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT ProductID, CategoryID, ProductName, Price, ImageUrl, IsAvailable FROM Products ORDER BY ProductID ASC";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                list.add(mapResultSetToProduct(rs));
            }
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_GET_PRODUCT);
        }
        return list;
    }

    public Product getProductById(int productId) throws ValidationException {
        String sql = "SELECT ProductID, CategoryID, ProductName, Price, ImageUrl, IsAvailable FROM Products WHERE ProductID = ?";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setInt(1, productId);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return mapResultSetToProduct(rs);
            }
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_GET_PRODUCT);
        }
        return null;
    }

    public Product getProductByName(String productName) throws ValidationException {
        String sql = "SELECT ProductID, CategoryID, ProductName, Price, ImageUrl, IsAvailable FROM Products WHERE LOWER(ProductName) = LOWER(?)";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setString(1, productName.trim());
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return mapResultSetToProduct(rs);
            }
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_GET_PRODUCT);
        }
        return null;
    }

    public boolean isProductNameExists(String productName, int excludeProductId) throws ValidationException {
        String sql = "SELECT COUNT(*) AS Cnt FROM Products WHERE LOWER(ProductName) = LOWER(?) AND ProductID != ?";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setString(1, productName.trim());
            st.setInt(2, excludeProductId);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return rs.getInt("Cnt") > 0;
            }
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_GET_PRODUCT);
        }
        return false;
    }

    public List<Product> getProductsByCategoryId(int categoryId) throws ValidationException {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT ProductID, CategoryID, ProductName, Price, ImageUrl, IsAvailable FROM Products WHERE CategoryID = ? ORDER BY ProductID ASC";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setInt(1, categoryId);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                list.add(mapResultSetToProduct(rs));
            }
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_GET_PRODUCT);
        }
        return list;
    }

    public void updateProductAvailability(int productId, boolean isAvailable) throws ValidationException {
        String sql = "UPDATE Products SET IsAvailable = ? WHERE ProductID = ?";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setBoolean(1, isAvailable);
            st.setInt(2, productId);
            st.executeUpdate();
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_UPDATE_PRODUCT);
        }
    }

    public void insertProduct(Product product) throws ValidationException {
        String sql = "INSERT INTO Products (CategoryID, ProductName, Price, ImageUrl, IsAvailable) VALUES (?, ?, ?, ?, ?)";
        try {
            PreparedStatement st = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            st.setInt(1, product.getCategoryId());
            st.setString(2, product.getProductName());
            st.setDouble(3, product.getPrice());
            st.setString(4, product.getImageUrl());
            st.setBoolean(5, product.isAvailable());
            st.executeUpdate();
            ResultSet rs = st.getGeneratedKeys();
            if (rs.next()) {
                product.setProductId(rs.getInt(1));
            }
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_UPDATE_PRODUCT);
        }
    }

    public void updateProduct(Product product) throws ValidationException {
        String sql = "UPDATE Products SET CategoryID = ?, ProductName = ?, Price = ?, ImageUrl = ?, IsAvailable = ? WHERE ProductID = ?";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setInt(1, product.getCategoryId());
            st.setString(2, product.getProductName());
            st.setDouble(3, product.getPrice());
            st.setString(4, product.getImageUrl());
            st.setBoolean(5, product.isAvailable());
            st.setInt(6, product.getProductId());
            st.executeUpdate();
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_UPDATE_PRODUCT);
        }
    }

    public void deleteProduct(int productId) throws ValidationException {
        // Xóa mềm: Chuyển IsAvailable = false để bảo toàn khóa ngoại với OrderDetails
        updateProductAvailability(productId, false);
    }

    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setProductId(rs.getInt("ProductID"));
        p.setCategoryId(rs.getInt("CategoryID"));
        p.setProductName(rs.getString("ProductName"));
        p.setPrice(rs.getDouble("Price"));
        p.setImageUrl(rs.getString("ImageUrl"));
        p.setAvailable(rs.getBoolean("IsAvailable"));
        return p;
    }
}
