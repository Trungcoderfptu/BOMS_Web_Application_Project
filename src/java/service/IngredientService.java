package service;

import dao.IngredientDAO;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.Ingredient;
import model.IngredientAlert;
import utility.Constants;
import utility.ValidationException;
import utility.Validator;

public class IngredientService {
    private final IngredientDAO ingredientDao;

    public IngredientService() {
        this.ingredientDao = new IngredientDAO();
    }

    /**
     * Lấy danh sách nguyên liệu dạng Map DTO cho Controller / View
     * Hỗ trợ tìm kiếm theo tên và gắn nhãn trạng thái tồn kho
     */
    public List<Map<String, Object>> getAllIngredientsForView(String searchTerm) throws ValidationException {
        List<Ingredient> ingredients = ingredientDao.searchIngredient(searchTerm);
        Map<Integer, String> categoryMap = ingredientDao.getIngredientCategoryMap();
        List<Map<String, Object>> result = new ArrayList<>();
        if (ingredients != null) {
            for (Ingredient ing : ingredients) {
                result.add(convertIngredientToMap(ing, categoryMap));
            }
        }
        return result;
    }

    /**
     * Lấy chi tiết một nguyên liệu dạng Map DTO
     */
    public Map<String, Object> getIngredientDetailForView(String ingredientIdStr) throws ValidationException {
        int ingredientId = Validator.checkInteger(ingredientIdStr, Constants.ERROR_INGREDIENT_NOT_FOUND);
        Ingredient ing = ingredientDao.getIngredientById(ingredientId);
        if (ing == null || !ing.isActive()) {
            throw new ValidationException(Constants.ERROR_INGREDIENT_NOT_FOUND);
        }
        Map<Integer, String> categoryMap = ingredientDao.getIngredientCategoryMap();
        return convertIngredientToMap(ing, categoryMap);
    }

    /**
     * Lấy danh mục nguyên liệu phục vụ Form modal
     */
    public List<Map<String, Object>> getCategoriesForView() throws ValidationException {
        Map<Integer, String> catMap = ingredientDao.getIngredientCategoryMap();
        List<Map<String, Object>> list = new ArrayList<>();
        if (catMap != null) {
            for (Map.Entry<Integer, String> entry : catMap.entrySet()) {
                Map<String, Object> m = new HashMap<>();
                m.put("categoryId", entry.getKey());
                m.put("categoryName", entry.getValue());
                list.add(m);
            }
        }
        return list;
    }

    /**
     * Cập nhật ngưỡng cảnh báo nguyên liệu (Chỉ dành cho Manager / Admin)
     */
    public void updateWarningThreshold(String ingredientIdStr, String thresholdStr, String userRole)
            throws ValidationException {
        validateManagerPermission(userRole);
        int ingredientId = Validator.checkInteger(ingredientIdStr, Constants.ERROR_INGREDIENT_NOT_FOUND);
        double threshold = Validator.checkNonNegativeDouble(thresholdStr, Constants.ERROR_INVALID_THRESHOLD);

        Ingredient ing = ingredientDao.getIngredientById(ingredientId);
        if (ing == null || !ing.isActive()) {
            throw new ValidationException(Constants.ERROR_INGREDIENT_NOT_FOUND);
        }

        ingredientDao.updateThresholdIngredient(ingredientId, threshold);
    }

    /**
     * Thêm mới nguyên liệu vào kho (Chỉ dành cho Manager / Admin)
     */
    public void createIngredient(String categoryIdStr, String name, String unit, String quantityStr,
            String priceStr, String location, String thresholdStr, String userRole)
            throws ValidationException {
        validateManagerPermission(userRole);

        // Validation Tầng 1
        Validator.checkEmpty(name, Constants.ERROR_EMPTY_INGREDIENT_NAME);
        Validator.checkEmpty(unit, Constants.ERROR_EMPTY_INGREDIENT_NAME);
        int categoryId = Validator.checkInteger(categoryIdStr, Constants.ERROR_INVALID_INGREDIENT_DATA);
        double quantity = Validator.checkNonNegativeDouble(quantityStr, Constants.ERROR_INVALID_INGREDIENT_QTY);
        double price = Validator.checkNonNegativeDouble(priceStr, Constants.ERROR_INVALID_INGREDIENT_PRICE);
        double threshold = Validator.checkNonNegativeDouble(thresholdStr, Constants.ERROR_INVALID_THRESHOLD);

        // Validation Tầng 2
        if (ingredientDao.isIngredientNameExists(name.trim(), 0)) {
            throw new ValidationException(Constants.ERROR_INGREDIENT_NAME_EXISTS);
        }

        Ingredient ing = new Ingredient();
        ing.setCategoryId(categoryId);
        ing.setIngredientName(name.trim());
        ing.setUnit(unit.trim());
        ing.setQuantity(quantity);
        ing.setPrice(price);
        ing.setStorageLocation(location != null && !location.trim().isEmpty() ? location.trim() : null);
        ing.setWarningThreshold(threshold);
        ing.setIsActive(true);

        ingredientDao.insertIngredient(ing);
    }

    /**
     * Cập nhật thông tin nguyên liệu (Chỉ dành cho Manager / Admin)
     */
    public void updateIngredient(String ingredientIdStr, String categoryIdStr, String name, String unit,
            String quantityStr, String priceStr, String location, String thresholdStr, String userRole)
            throws ValidationException {
        validateManagerPermission(userRole);

        // Validation Tầng 1
        int ingredientId = Validator.checkInteger(ingredientIdStr, Constants.ERROR_INGREDIENT_NOT_FOUND);
        Validator.checkEmpty(name, Constants.ERROR_EMPTY_INGREDIENT_NAME);
        Validator.checkEmpty(unit, Constants.ERROR_EMPTY_INGREDIENT_NAME);
        int categoryId = Validator.checkInteger(categoryIdStr, Constants.ERROR_INVALID_INGREDIENT_DATA);
        double quantity = Validator.checkNonNegativeDouble(quantityStr, Constants.ERROR_INVALID_INGREDIENT_QTY);
        double price = Validator.checkNonNegativeDouble(priceStr, Constants.ERROR_INVALID_INGREDIENT_PRICE);
        double threshold = Validator.checkNonNegativeDouble(thresholdStr, Constants.ERROR_INVALID_THRESHOLD);

        // Validation Tầng 2
        Ingredient existing = ingredientDao.getIngredientById(ingredientId);
        if (existing == null || !existing.isActive()) {
            throw new ValidationException(Constants.ERROR_INGREDIENT_NOT_FOUND);
        }
        if (ingredientDao.isIngredientNameExists(name.trim(), ingredientId)) {
            throw new ValidationException(Constants.ERROR_INGREDIENT_NAME_EXISTS);
        }

        existing.setCategoryId(categoryId);
        existing.setIngredientName(name.trim());
        existing.setUnit(unit.trim());
        existing.setQuantity(quantity);
        existing.setPrice(price);
        existing.setStorageLocation(location != null && !location.trim().isEmpty() ? location.trim() : null);
        existing.setWarningThreshold(threshold);

        ingredientDao.updateIngredient(existing);
    }

    /**
     * Xóa mềm nguyên liệu khỏi kho (Chỉ dành cho Manager / Admin)
     */
    public void deleteIngredient(String ingredientIdStr, String userRole) throws ValidationException {
        validateManagerPermission(userRole);
        int ingredientId = Validator.checkInteger(ingredientIdStr, Constants.ERROR_INGREDIENT_NOT_FOUND);
        Ingredient ing = ingredientDao.getIngredientById(ingredientId);
        if (ing == null || !ing.isActive()) {
            throw new ValidationException(Constants.ERROR_INGREDIENT_NOT_FOUND);
        }
        ingredientDao.deleteIngredient(ingredientId);
    }

    // =========================================================================
    // HELPER METHODS (SRP)
    // =========================================================================

    private void validateManagerPermission(String role) throws ValidationException {
        if (!"Manager".equalsIgnoreCase(role) && !"Admin".equalsIgnoreCase(role)) {
            throw new ValidationException(Constants.ERROR_INSUFFICIENT_PERMISSION);
        }
    }

    private Map<String, Object> convertIngredientToMap(Ingredient ing, Map<Integer, String> categoryMap) {
        Map<String, Object> map = new HashMap<>();
        map.put("ingredientId", ing.getIngredientId());
        map.put("categoryId", ing.getCategoryId());
        String catName = (categoryMap != null && categoryMap.containsKey(ing.getCategoryId()))
                ? categoryMap.get(ing.getCategoryId()) : "Khác";
        map.put("categoryName", catName);
        map.put("ingredientName", ing.getIngredientName());
        map.put("unit", ing.getUnit());
        map.put("quantity", ing.getQuantity());
        map.put("price", ing.getPrice());
        map.put("storageLocation", ing.getStorageLocation() != null ? ing.getStorageLocation() : "-");
        map.put("warningThreshold", ing.getWarningThreshold());
        map.put("isActive", ing.isActive());

        // Phân loại trạng thái kho theo màu sắc (Đỏ: Hết hàng, Vàng: Sắp hết, Xanh: Đầy đủ)
        String stockStatus;
        if (ing.getQuantity() <= 0) {
            stockStatus = "OUT_OF_STOCK";
        } else if (ing.getQuantity() <= ing.getWarningThreshold()) {
            stockStatus = "LOW_STOCK";
        } else {
            stockStatus = "IN_STOCK";
        }
        map.put("stockStatus", stockStatus);

        return map;
    }

    // =========================================================================
    // ALERT OPERATIONS (FE-04)
    // =========================================================================

    /**
     * Tự động quét kiểm tra kho và cập nhật cảnh báo (BR-4, BR-5, BR-18, BR-19)
     */
    public void scanIngredientStock() throws ValidationException {
        // 1. Tự động đóng các cảnh báo LowStock/OutOfStock nếu kho đã được bổ sung
        ingredientDao.closeResolvedStockAlerts();

        // 2. Quét số lượng tồn kho so với ngưỡng cảnh báo
        List<Ingredient> ingredients = ingredientDao.getAllIngredients();
        if (ingredients != null) {
            for (Ingredient ing : ingredients) {
                if (ing.isActive()) {
                    if (ing.getQuantity() <= 0) {
                        if (!ingredientDao.isAlertActive(ing.getIngredientId(), "OutOfStock")) {
                            ingredientDao.insertInventoryAlert(ing.getIngredientId(), "OutOfStock");
                        }
                    } else if (ing.getQuantity() <= ing.getWarningThreshold()) {
                        if (!ingredientDao.isAlertActive(ing.getIngredientId(), "LowStock")) {
                            ingredientDao.insertInventoryAlert(ing.getIngredientId(), "LowStock");
                        }
                    }
                }
            }
        }

        // 3. Quét các lô hàng đã quá hạn sử dụng (ExpirationDate < hôm nay) -> 'Expired'
        List<Integer> expiredIds = ingredientDao.getExpiredIngredientIds();
        if (expiredIds != null) {
            for (Integer ingId : expiredIds) {
                if (!ingredientDao.isAlertActive(ingId, "Expired")) {
                    ingredientDao.insertInventoryAlert(ingId, "Expired");
                }
            }
        }

        // 4. Quét các lô hàng sắp hết hạn trong vòng 3 ngày tới (hôm nay <= ExpirationDate <= hôm nay + 3 ngày) -> 'Expiring Soon'
        List<Integer> expiringSoonIds = ingredientDao.getExpiringSoonIngredientIds(3);
        if (expiringSoonIds != null) {
            for (Integer ingId : expiringSoonIds) {
                if (!ingredientDao.isAlertActive(ingId, "Expiring Soon")) {
                    ingredientDao.insertInventoryAlert(ingId, "Expiring Soon");
                }
            }
        }
    }

    /**
     * Lấy danh sách cảnh báo đang Active/Confirmed dạng Map DTO
     */
    public List<Map<String, Object>> getActiveAlertsForView() throws ValidationException {
        // Kích hoạt quét tự động trước khi lấy dữ liệu
        scanIngredientStock();

        List<IngredientAlert> alerts = ingredientDao.getActiveAlerts();
        List<Map<String, Object>> result = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        if (alerts != null) {
            for (IngredientAlert a : alerts) {
                result.add(convertAlertToMap(a, sdf));
            }
        }
        return result;
    }

    /**
     * Xác nhận đã biết cảnh báo kho
     */
    public void confirmAlert(String alertIdStr, int userId) throws ValidationException {
        int alertId = Validator.checkInteger(alertIdStr, Constants.ERROR_ALERT_NOT_FOUND);
        ingredientDao.confirmAlert(alertId, userId);
    }

    /**
     * Đóng cảnh báo kho
     */
    public void closeAlert(String alertIdStr) throws ValidationException {
        int alertId = Validator.checkInteger(alertIdStr, Constants.ERROR_ALERT_NOT_FOUND);
        ingredientDao.closeAlert(alertId);
    }

    private Map<String, Object> convertAlertToMap(IngredientAlert alert, SimpleDateFormat sdf) {
        Map<String, Object> map = new HashMap<>();
        map.put("alertId", alert.getAlertId());
        
        Ingredient ing = alert.getIngredient();
        if (ing != null) {
            map.put("ingredientId", ing.getIngredientId());
            map.put("ingredientName", ing.getIngredientName());
            map.put("unit", ing.getUnit());
            map.put("quantity", ing.getQuantity());
            map.put("warningThreshold", ing.getWarningThreshold());
        } else {
            map.put("ingredientId", 0);
            map.put("ingredientName", "-");
            map.put("unit", "");
            map.put("quantity", 0.0);
            map.put("warningThreshold", 0.0);
        }

        String typeStr = alert.getType() != null ? alert.getType().name() : "LowStock";
        map.put("alertType", typeStr);
        String statusStr = alert.getStatus() != null ? alert.getStatus().name() : "Active";
        map.put("status", statusStr);
        map.put("confirmedBy", alert.getConfirmedBy() != null ? alert.getConfirmedBy() : "");
        map.put("createdAt", alert.getCreatedAt() != null ? sdf.format(alert.getCreatedAt()) : "");
        map.put("confirmedAt", alert.getConfirmedAt() != null ? sdf.format(alert.getConfirmedAt()) : "");

        return map;
    }
}
