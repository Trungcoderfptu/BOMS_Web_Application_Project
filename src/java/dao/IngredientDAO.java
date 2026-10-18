package dao;

import dal.DBContext;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.AlertStatus;
import model.AlertType;
import model.Ingredient;
import model.IngredientAlert;
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

    public boolean isIngredientNameExists(String name, int excludeId) throws ValidationException {
        String sql = "SELECT COUNT(*) FROM Ingredients WHERE LOWER(IngredientName) = LOWER(?) AND IngredientID != ? AND IsActive = 1";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setString(1, name.trim());
            st.setInt(2, excludeId);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_GET_INGREDIENT);
        }
        return false;
    }
    
    public List<Ingredient> searchIngredient(String ingredientName) throws ValidationException {
        List<Ingredient> search = new ArrayList<>();
        String sql = "SELECT IngredientID, CategoryID, IngredientName, Unit, Quantity, Price, StorageLocation, WarningThreshold, IsActive "
                + "FROM Ingredients WHERE (IngredientName LIKE '%' + ? + '%' OR ? = '') AND IsActive = 1 ORDER BY IngredientID ASC";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            String term = (ingredientName != null) ? ingredientName.trim() : "";
            st.setString(1, term);
            st.setString(2, term);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                search.add(mapResultSetToIngredient(rs));
            }
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_GET_INGREDIENT);
        }
        return search;
    }

    public java.util.Map<Integer, String> getIngredientCategoryMap() throws ValidationException {
        java.util.Map<Integer, String> map = new java.util.HashMap<>();
        String sql = "SELECT CategoryID, CategoryName FROM IngredientCategories ORDER BY CategoryID ASC";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                map.put(rs.getInt("CategoryID"), rs.getString("CategoryName"));
            }
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_GET_INGREDIENT);
        }
        return map;
    }
    
    // =========================================================================
    // INVENTORY ALERTS (FE-04)
    // =========================================================================

    public boolean isAlertActive(int ingredientId, String alertType) throws ValidationException {
        String sql = "SELECT COUNT(*) FROM InventoryAlerts WHERE IngredientID = ? AND AlertType = ? AND Status IN ('Active', 'Confirmed')";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setInt(1, ingredientId);
            st.setString(2, alertType);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_GET_ALERT);
        }
        return false;
    }

    public void insertInventoryAlert(int ingredientId, String alertType) throws ValidationException {
        String sql = "INSERT INTO InventoryAlerts (IngredientID, AlertType, Status, CreatedAt) VALUES (?, ?, 'Active', GETDATE())";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setInt(1, ingredientId);
            st.setString(2, alertType);
            st.executeUpdate();
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_UPDATE_ALERT);
        }
    }

    public List<IngredientAlert> getActiveAlerts() throws ValidationException {
        List<IngredientAlert> list = new ArrayList<>();
        String sql = "SELECT a.AlertID, a.IngredientID, a.AlertType, a.Status, a.ConfirmedBy, a.CreatedAt, a.ConfirmedAt, "
                + "i.CategoryID, i.IngredientName, i.Unit, i.Quantity, i.Price, i.StorageLocation, i.WarningThreshold, i.IsActive, "
                + "u.FullName AS ConfirmedByName "
                + "FROM InventoryAlerts a "
                + "JOIN Ingredients i ON a.IngredientID = i.IngredientID "
                + "LEFT JOIN Users u ON a.ConfirmedBy = u.UserID "
                + "WHERE a.Status IN ('Active', 'Confirmed') AND i.IsActive = 1 "
                + "ORDER BY a.CreatedAt DESC";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                Ingredient ing = mapResultSetToIngredient(rs);
                IngredientAlert alert = new IngredientAlert();
                alert.setAlertId(rs.getInt("AlertID"));
                alert.setIngredient(ing);
                String rawAlertType = rs.getString("AlertType");
                if ("Expiring Soon".equalsIgnoreCase(rawAlertType) || "ExpiringSoon".equalsIgnoreCase(rawAlertType)) {
                    alert.setType(AlertType.ExpiringSoon);
                } else {
                    try {
                        alert.setType(AlertType.valueOf(rawAlertType));
                    } catch (IllegalArgumentException | NullPointerException ex) {
                        alert.setType(AlertType.LowStock);
                    }
                }
                try {
                    alert.setStatus(AlertStatus.valueOf(rs.getString("Status")));
                } catch (IllegalArgumentException | NullPointerException ex) {
                    alert.setStatus(AlertStatus.Active);
                }
                alert.setConfirmedBy(rs.getString("ConfirmedByName"));
                alert.setCreatedAt(rs.getTimestamp("CreatedAt"));
                alert.setConfirmedAt(rs.getTimestamp("ConfirmedAt"));
                list.add(alert);
            }
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_GET_ALERT);
        }
        return list;
    }

    public void confirmAlert(int alertId, int userId) throws ValidationException {
        String sql = "UPDATE InventoryAlerts SET Status = 'Confirmed', ConfirmedBy = ?, ConfirmedAt = GETDATE() WHERE AlertID = ?";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            if (userId > 0) {
                st.setInt(1, userId);
            } else {
                st.setNull(1, java.sql.Types.INTEGER);
            }
            st.setInt(2, alertId);
            st.executeUpdate();
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_UPDATE_ALERT);
        }
    }

    public void closeAlert(int alertId) throws ValidationException {
        String sql = "UPDATE InventoryAlerts SET Status = 'Closed' WHERE AlertID = ?";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setInt(1, alertId);
            st.executeUpdate();
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_UPDATE_ALERT);
        }
    }

    public void closeResolvedStockAlerts() throws ValidationException {
        String sql = "UPDATE a SET a.Status = 'Closed' "
                + "FROM InventoryAlerts a "
                + "JOIN Ingredients i ON a.IngredientID = i.IngredientID "
                + "WHERE a.Status IN ('Active', 'Confirmed') "
                + "  AND a.AlertType IN ('LowStock', 'OutOfStock') "
                + "  AND i.Quantity > i.WarningThreshold";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.executeUpdate();
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_UPDATE_ALERT);
        }
    }

    public List<Integer> getExpiredIngredientIds() throws ValidationException {
        List<Integer> list = new ArrayList<>();
        String sql = "SELECT DISTINCT b.IngredientID "
                + "FROM IngredientBatches b "
                + "JOIN Ingredients i ON b.IngredientID = i.IngredientID "
                + "WHERE b.ExpirationDate < CAST(GETDATE() AS DATE) "
                + "  AND b.Quantity > 0 AND i.IsActive = 1";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                list.add(rs.getInt("IngredientID"));
            }
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_GET_ALERT);
        }
        return list;
    }

    public List<Integer> getExpiringSoonIngredientIds(int withinDays) throws ValidationException {
        List<Integer> list = new ArrayList<>();
        String sql = "SELECT DISTINCT b.IngredientID "
                + "FROM IngredientBatches b "
                + "JOIN Ingredients i ON b.IngredientID = i.IngredientID "
                + "WHERE b.ExpirationDate >= CAST(GETDATE() AS DATE) "
                + "  AND b.ExpirationDate <= DATEADD(day, ?, CAST(GETDATE() AS DATE)) "
                + "  AND b.Quantity > 0 AND i.IsActive = 1";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setInt(1, withinDays);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                list.add(rs.getInt("IngredientID"));
            }
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_GET_ALERT);
        }
        return list;
    }
}
