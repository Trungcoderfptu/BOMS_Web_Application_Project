package dao;

import dal.DBContext;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.Ingredient;
import model.ProductIngredient;
import utility.Constants;
import utility.ValidationException;

/**
 * Data Access Object quản lý kho nguyên liệu và công thức định lượng kết nối MS SQL Server
 * @author AD
 */
public class IngredientDAO extends DBContext {

    public List<Ingredient> getAllIngredients() throws ValidationException {
        List<Ingredient> list = new ArrayList<>();
        String sql = "SELECT IngredientID, CategoryID, IngredientName, Unit, Quantity, Price, StorageLocation, WarningThreshold, IsActive "
                + "FROM Ingredients ORDER BY IngredientID ASC";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                list.add(mapResultSetToIngredient(rs));
            }
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_GET_INGREDIENT);
        }
        return list;
    }

    public Ingredient getIngredientById(int ingredientId) throws ValidationException {
        String sql = "SELECT IngredientID, CategoryID, IngredientName, Unit, Quantity, Price, StorageLocation, WarningThreshold, IsActive "
                + "FROM Ingredients WHERE IngredientID = ?";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setInt(1, ingredientId);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return mapResultSetToIngredient(rs);
            }
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_GET_INGREDIENT);
        }
        return null;
    }

    public List<ProductIngredient> getIngredientsByProductId(int productId) throws ValidationException {
        List<ProductIngredient> result = new ArrayList<>();
        String sql = "SELECT ProductID, IngredientID, QuantityUsed FROM ProductIngredients WHERE ProductID = ?";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setInt(1, productId);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                ProductIngredient pi = new ProductIngredient(
                        rs.getInt("ProductID"),
                        rs.getInt("IngredientID"),
                        rs.getDouble("QuantityUsed")
                );
                result.add(pi);
            }
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_GET_INGREDIENT);
        }
        return result;
    }

    public List<Integer> getProductIdsUsingIngredient(int ingredientId) throws ValidationException {
        List<Integer> productIds = new ArrayList<>();
        String sql = "SELECT DISTINCT ProductID FROM ProductIngredients WHERE IngredientID = ?";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setInt(1, ingredientId);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                productIds.add(rs.getInt("ProductID"));
            }
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_GET_INGREDIENT);
        }
        return productIds;
    }

    public void deductStock(int ingredientId, double amount) throws ValidationException {
        String sql = "UPDATE Ingredients SET Quantity = CASE WHEN Quantity - ? < 0 THEN 0 ELSE Quantity - ? END WHERE IngredientID = ?";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setDouble(1, amount);
            st.setDouble(2, amount);
            st.setInt(3, ingredientId);
            st.executeUpdate();
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_UPDATE_INGREDIENT);
        }
    }

    public void updateIngredientQuantity(int ingredientId, double newQuantity) throws ValidationException {
        String sql = "UPDATE Ingredients SET Quantity = ? WHERE IngredientID = ?";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setDouble(1, newQuantity);
            st.setInt(2, ingredientId);
            st.executeUpdate();
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_UPDATE_INGREDIENT);
        }
    }

    public void restoreStock(int ingredientId, double amount) throws ValidationException {
        String sql = "UPDATE Ingredients SET Quantity = Quantity + ? WHERE IngredientID = ?";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setDouble(1, amount);
            st.setInt(2, ingredientId);
            st.executeUpdate();
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_UPDATE_INGREDIENT);
        }
    }

    public boolean hasSufficientStockForProduct(int productId, int quantity) throws ValidationException {
        // Kiểm tra xem có bất kỳ nguyên liệu nào trong công thức của sản phẩm bị thiếu không
        String sql = "SELECT COUNT(*) AS LackingCount "
                + "FROM ProductIngredients pi "
                + "JOIN Ingredients i ON pi.IngredientID = i.IngredientID "
                + "WHERE pi.ProductID = ? AND i.Quantity < (pi.QuantityUsed * ?)";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setInt(1, productId);
            st.setInt(2, quantity);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return rs.getInt("LackingCount") == 0;
            }
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_GET_INGREDIENT);
        }
        return false;
    }

    public void insertIngredient(Ingredient ing) throws ValidationException {
        String sql = "INSERT INTO Ingredients (CategoryID, IngredientName, Unit, Quantity, Price, StorageLocation, WarningThreshold, IsActive) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement st = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            st.setInt(1, ing.getCategoryId());
            st.setString(2, ing.getIngredientName());
            st.setString(3, ing.getUnit());
            st.setDouble(4, ing.getQuantity());
            st.setDouble(5, ing.getPrice());
            st.setString(6, ing.getStorageLocation());
            st.setDouble(7, ing.getWarningThreshold());
            st.setBoolean(8, ing.isActive());
            st.executeUpdate();
            ResultSet rs = st.getGeneratedKeys();
            if (rs.next()) {
                ing.setIngredientId(rs.getInt(1));
            }
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_UPDATE_INGREDIENT);
        }
    }

    public void updateIngredient(Ingredient ing) throws ValidationException {
        String sql = "UPDATE Ingredients SET CategoryID = ?, IngredientName = ?, Unit = ?, Quantity = ?, Price = ?, "
                + "StorageLocation = ?, WarningThreshold = ?, IsActive = ? WHERE IngredientID = ?";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setInt(1, ing.getCategoryId());
            st.setString(2, ing.getIngredientName());
            st.setString(3, ing.getUnit());
            st.setDouble(4, ing.getQuantity());
            st.setDouble(5, ing.getPrice());
            st.setString(6, ing.getStorageLocation());
            st.setDouble(7, ing.getWarningThreshold());
            st.setBoolean(8, ing.isActive());
            st.setInt(9, ing.getIngredientId());
            st.executeUpdate();
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_UPDATE_INGREDIENT);
        }
    }

    public void deleteIngredient(int ingredientId) throws ValidationException {
        // Xóa mềm: Chuyển IsActive = false
        String sql = "UPDATE Ingredients SET IsActive = 0 WHERE IngredientID = ?";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setInt(1, ingredientId);
            st.executeUpdate();
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_UPDATE_INGREDIENT);
        }
    }

    public void insertProductIngredient(ProductIngredient pi) throws ValidationException {
        String sql = "INSERT INTO ProductIngredients (ProductID, IngredientID, QuantityUsed) VALUES (?, ?, ?)";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setInt(1, pi.getProductId());
            st.setInt(2, pi.getIngredientId());
            st.setDouble(3, pi.getQuantityUsed());
            st.executeUpdate();
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_UPDATE_INGREDIENT);
        }
    }

    public void deleteProductIngredientsByProductId(int productId) throws ValidationException {
        String sql = "DELETE FROM ProductIngredients WHERE ProductID = ?";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setInt(1, productId);
            st.executeUpdate();
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_UPDATE_INGREDIENT);
        }
    }

    private Ingredient mapResultSetToIngredient(ResultSet rs) throws SQLException {
        Ingredient ing = new Ingredient();
        ing.setIngredientId(rs.getInt("IngredientID"));
        ing.setCategoryId(rs.getInt("CategoryID"));
        ing.setIngredientName(rs.getString("IngredientName"));
        ing.setUnit(rs.getString("Unit"));
        ing.setQuantity(rs.getDouble("Quantity"));
        ing.setPrice(rs.getDouble("Price"));
        ing.setStorageLocation(rs.getString("StorageLocation"));
        ing.setWarningThreshold(rs.getDouble("WarningThreshold"));
        ing.setIsActive(rs.getBoolean("IsActive"));
        return ing;
    }
    
    public boolean updateThresholdIngredient(int ingredientId, double threshold) throws ValidationException {
        String sql = "UPDATE Ingredients SET WarningThreshold = ? WHERE IngredientID = ?";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setDouble(1, threshold);
            st.setInt(2, ingredientId);
            st.executeUpdate();
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_UPDATE_THRESHOLD);
        }
        return true;
    }
    
    public List<Ingredient> searchIngredient(String ingredientName) throws ValidationException {
        List<Ingredient> search = new ArrayList<>();
        String sql = "SELECT * FROM Ingredients WHERE IngredientName LIKE '%' + ? + '%'";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setString(1, ingredientName);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                search.add(mapResultSetToIngredient(rs));
            }
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_GET_INGREDIENT);
        }
        return search;
    }
    
    public boolean addInventoryAlert() {
        return true;
    }
}
