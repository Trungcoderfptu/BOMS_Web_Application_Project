/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import dao.MenuCategoryDAO;
import dao.ProductDAO;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import model.Ingredient;
import model.MenuCategory;
import model.Product;
import model.ProductIngredient;
import utility.Constants;
import utility.ImageUploadUtility;
import utility.ValidationException;
import utility.Validator;

/**
 *
 * @author AD
 */
public class ProductService {

    private ProductDAO productDAO;
    private dao.IngredientDAO ingredientDAO;
    private MenuCategoryDAO menuCategoryDAO;

    public ProductService() {
        this.productDAO = new ProductDAO();
        this.ingredientDAO = new dao.IngredientDAO();
        this.menuCategoryDAO = new MenuCategoryDAO();
    }

    public List<Product> getAllProducts() throws ValidationException {
        return productDAO.getAllProducts();
    }

    public Product getProductById(int productId) {
        if (productId <= 0) {
            return null;
        }
        try {
            return productDAO.getProductById(productId);
        } catch (ValidationException e) {
            return null;
        }
    }

    public List<Map<String, Object>> getAllProductsForView() throws ValidationException {
        List<Product> products = productDAO.getAllProducts();
        return buildProductListMap(products);
    }

    /**
     * Lấy toàn bộ danh sách danh mục thực đơn phục vụ View (dropdown lọc, form thêm/sửa món)
     */
    public List<Map<String, Object>> getAllCategoriesForView() throws ValidationException {
        List<MenuCategory> categories = menuCategoryDAO.getAllCategories();
        List<Map<String, Object>> result = new ArrayList<>();
        if (categories != null) {
            for (MenuCategory cat : categories) {
                Map<String, Object> map = new HashMap<>();
                map.put("categoryId", String.valueOf(cat.getCategoryId()));
                map.put("categoryName", cat.getCategoryName());
                map.put("inUse", menuCategoryDAO.isCategoryInUse(cat.getCategoryId()));
                result.add(map);
            }
        }
        return result;
    }

    public void createCategory(String categoryName) throws ValidationException {
        Validator.checkEmpty(categoryName, Constants.ERROR_EMPTY_PRODUCT_CATEGORY);
        Validator.checkMaxLength(categoryName.trim(), 100, Constants.ERROR_EMPTY_PRODUCT_CATEGORY);

        if (menuCategoryDAO.getCategoryByName(categoryName.trim()) != null) {
            throw new ValidationException(Constants.ERROR_MENU_CATEGORY_EXISTS);
        }

        menuCategoryDAO.insertCategory(categoryName.trim());
    }

    public void deleteCategory(String categoryIdStr) throws ValidationException {
        int categoryId = Validator.checkInteger(categoryIdStr, Constants.ERROR_PRODUCT_NOT_FOUND);

        if (menuCategoryDAO.isCategoryInUse(categoryId)) {
            throw new ValidationException(Constants.ERROR_MENU_CATEGORY_IN_USE);
        }

        menuCategoryDAO.deleteCategory(categoryId);
    }

    /**
     * Lấy toàn bộ danh sách nguyên liệu đang hoạt động phục vụ dropdown công thức món
     */
    public List<Map<String, Object>> getAllActiveIngredientsForView() throws ValidationException {
        List<Ingredient> ingredients = ingredientDAO.getAllIngredients();
        List<Map<String, Object>> result = new ArrayList<>();
        if (ingredients != null) {
            for (Ingredient ing : ingredients) {
                if (ing.isActive()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("ingredientId", String.valueOf(ing.getIngredientId()));
                    map.put("ingredientName", ing.getIngredientName());
                    map.put("unit", ing.getUnit());
                    result.add(map);
                }
            }
        }
        return result;
    }

    /**
     * Lấy danh sách toàn bộ món ăn cho trang Quản lý Thực đơn (Admin & Staff)
     * Đóng gói đầy đủ tên danh mục, trạng thái kinh doanh, công thức nguyên liệu và tình trạng tồn kho nguyên liệu
     */
    public List<Map<String, Object>> getAllProductsForManagementView() throws ValidationException {
        List<Product> products = productDAO.getAllProducts();
        List<MenuCategory> categories = menuCategoryDAO.getAllCategories();
        Map<Integer, String> categoryNameMap = new HashMap<>();
        if (categories != null) {
            for (MenuCategory cat : categories) {
                categoryNameMap.put(cat.getCategoryId(), cat.getCategoryName());
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        if (products != null) {
            for (Product p : products) {
                Map<String, Object> map = new HashMap<>();
                map.put("productId", p.getProductId());
                map.put("categoryId", String.valueOf(p.getCategoryId()));
                map.put("categoryName", categoryNameMap.getOrDefault(p.getCategoryId(), "---"));
                map.put("productName", p.getProductName());
                map.put("price", p.getPrice());
                map.put("imageUrl", p.getImageUrl());
                map.put("isAvailable", p.isAvailable());
                map.put("availableInStock", checkProductAvailability(p));
                map.put("recipeData", buildRecipeString(p.getProductId()));
                result.add(map);
            }
        }
        return result;
    }

    /**
     * Lấy chi tiết món ăn đóng gói dạng Map DTO phục vụ form Chỉnh sửa món
     */
    public Map<String, Object> getProductDetailForView(String productIdStr) throws ValidationException {
        int productId = Validator.checkInteger(productIdStr, Constants.ERROR_PRODUCT_NOT_FOUND);
        Product p = productDAO.getProductById(productId);
        if (p == null) {
            throw new ValidationException(Constants.ERROR_PRODUCT_NOT_FOUND);
        }
        MenuCategory cat = menuCategoryDAO.getCategoryById(p.getCategoryId());

        Map<String, Object> map = new HashMap<>();
        map.put("productId", p.getProductId());
        map.put("categoryId", String.valueOf(p.getCategoryId()));
        map.put("categoryName", cat != null ? cat.getCategoryName() : "---");
        map.put("productName", p.getProductName());
        map.put("price", p.getPrice());
        map.put("imageUrl", p.getImageUrl());
        map.put("isAvailable", p.isAvailable());
        map.put("availableInStock", checkProductAvailability(p));
        return map;
    }

    /**
     * Nghiệp vụ Tạo món mới vào thực đơn (Overload tương thích ngược)
     */
    public void createProduct(String categoryIdStr, String productName, String priceStr, String imageUrl)
            throws ValidationException {
        createProduct(categoryIdStr, productName, priceStr, imageUrl, null, null);
    }

    /**
     * Nghiệp vụ Tạo món mới vào thực đơn kèm công thức nguyên liệu và 2 tầng Validation
     */
    public void createProduct(String categoryIdStr, String productName, String priceStr, String imageUrl,
            String[] recipeIngredientIds, String[] recipeQuantities) throws ValidationException {
        // Validation Tầng 1: Kiểm tra dữ liệu thô đầu vào
        Validator.checkEmpty(productName, Constants.ERROR_EMPTY_PRODUCT_NAME);
        int categoryId = Validator.checkInteger(categoryIdStr, Constants.ERROR_EMPTY_PRODUCT_CATEGORY);
        double price = Validator.checkPositiveDouble(priceStr, Constants.ERROR_INVALID_PRODUCT_PRICE);
        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            Validator.checkMaxLength(imageUrl.trim(), 255, Constants.ERROR_IMAGE_URL_TOO_LONG);
        }

        // Validation Tầng 2: Kiểm tra nghiệp vụ hệ thống
        MenuCategory cat = menuCategoryDAO.getCategoryById(categoryId);
        if (cat == null) {
            throw new ValidationException(Constants.ERROR_EMPTY_PRODUCT_CATEGORY);
        }
        Product existing = productDAO.getProductByName(productName);
        if (existing != null) {
            throw new ValidationException(Constants.ERROR_PRODUCT_NAME_EXISTS);
        }

        Map<Integer, Double> recipeMap = parseAndValidateRecipes(recipeIngredientIds, recipeQuantities);

        // Tạo thực thể và lưu xuống CSDL
        Product newProduct = new Product();
        newProduct.setCategoryId(categoryId);
        newProduct.setProductName(productName.trim());
        newProduct.setPrice(price);
        newProduct.setImageUrl(imageUrl != null && !imageUrl.trim().isEmpty() ? imageUrl.trim() : null);
        newProduct.setAvailable(true); // Mặc định món mới được phép kinh doanh

        productDAO.insertProduct(newProduct);

        // Lưu công thức định lượng vào ProductIngredients
        saveProductRecipes(newProduct.getProductId(), recipeMap);
    }

    /**
     * Nghiệp vụ Cập nhật thông tin món ăn (Overload tương thích ngược)
     */
    public void updateProduct(String productIdStr, String categoryIdStr, String productName,
            String priceStr, String imageUrl, String isAvailableStr) throws ValidationException {
        updateProduct(productIdStr, categoryIdStr, productName, priceStr, imageUrl, isAvailableStr, null, null);
    }

    /**
     * Nghiệp vụ Cập nhật thông tin món ăn kèm cập nhật công thức nguyên liệu và 2 tầng Validation
     */
    public void updateProduct(String productIdStr, String categoryIdStr, String productName,
            String priceStr, String imageUrl, String isAvailableStr,
            String[] recipeIngredientIds, String[] recipeQuantities) throws ValidationException {
        // Validation Tầng 1: Kiểm tra dữ liệu thô đầu vào
        int productId = Validator.checkInteger(productIdStr, Constants.ERROR_PRODUCT_NOT_FOUND);
        Validator.checkEmpty(productName, Constants.ERROR_EMPTY_PRODUCT_NAME);
        int categoryId = Validator.checkInteger(categoryIdStr, Constants.ERROR_EMPTY_PRODUCT_CATEGORY);
        double price = Validator.checkPositiveDouble(priceStr, Constants.ERROR_INVALID_PRODUCT_PRICE);
        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            Validator.checkMaxLength(imageUrl.trim(), 255, Constants.ERROR_IMAGE_URL_TOO_LONG);
        }

        // Validation Tầng 2: Kiểm tra tính nghiệp vụ
        Product existing = productDAO.getProductById(productId);
        if (existing == null) {
            throw new ValidationException(Constants.ERROR_PRODUCT_NOT_FOUND);
        }
        MenuCategory cat = menuCategoryDAO.getCategoryById(categoryId);
        if (cat == null) {
            throw new ValidationException(Constants.ERROR_EMPTY_PRODUCT_CATEGORY);
        }
        if (productDAO.isProductNameExists(productName, productId)) {
            throw new ValidationException(Constants.ERROR_PRODUCT_NAME_EXISTS);
        }

        Map<Integer, Double> recipeMap = parseAndValidateRecipes(recipeIngredientIds, recipeQuantities);

        // Dọn dẹp ảnh nội bộ cũ nếu đổi sang link web ngoài hoặc đổi file ảnh mới
        cleanOldInternalImage(existing.getImageUrl(), imageUrl);

        // Cập nhật thuộc tính
        boolean isAvailable = isAvailableStr != null ? Boolean.parseBoolean(isAvailableStr) : existing.isAvailable();
        existing.setCategoryId(categoryId);
        existing.setProductName(productName.trim());
        existing.setPrice(price);
        existing.setImageUrl(imageUrl != null && !imageUrl.trim().isEmpty() ? imageUrl.trim() : null);
        existing.setAvailable(isAvailable);

        productDAO.updateProduct(existing);

        // Đồng bộ lại công thức: Xóa cũ và nạp mới
        ingredientDAO.deleteProductIngredientsByProductId(productId);
        saveProductRecipes(productId, recipeMap);
    }

    private void cleanOldInternalImage(String oldImageUrl, String newImageUrl) {
        if (oldImageUrl != null && oldImageUrl.trim().startsWith("uploads/")) {
            String trimmedOld = oldImageUrl.trim();
            String trimmedNew = (newImageUrl != null) ? newImageUrl.trim() : "";
            boolean isNewWebLink = trimmedNew.startsWith("http://") || trimmedNew.startsWith("https://");
            boolean isDifferentUpload = trimmedNew.startsWith("uploads/") && !trimmedNew.equals(trimmedOld);
            if (isNewWebLink || isDifferentUpload) {
                ImageUploadUtility.deleteImage(trimmedOld);
            }
        }
    }

    /**
     * Nghiệp vụ Đổi trạng thái món / Xóa mềm (chuyển đổi giữa Đang kinh doanh <-> Ngừng kinh doanh)
     */
    public void toggleProductStatus(String productIdStr) throws ValidationException {
        int productId = Validator.checkInteger(productIdStr, Constants.ERROR_PRODUCT_NOT_FOUND);
        Product p = productDAO.getProductById(productId);
        if (p == null) {
            throw new ValidationException(Constants.ERROR_PRODUCT_NOT_FOUND);
        }
        productDAO.updateProductAvailability(productId, !p.isAvailable());
    }

    /**
     * Cập nhật trạng thái kinh doanh của món ăn theo chỉ định
     */
    public void setProductStatus(String productIdStr, boolean isAvailable) throws ValidationException {
        int productId = Validator.checkInteger(productIdStr, Constants.ERROR_PRODUCT_NOT_FOUND);
        Product p = productDAO.getProductById(productId);
        if (p == null) {
            throw new ValidationException(Constants.ERROR_PRODUCT_NOT_FOUND);
        }
        productDAO.updateProductAvailability(productId, isAvailable);
    }

    //========== create respon pack to other service ==========
    private List<Map<String, Object>> buildProductListMap(List<Product> products) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (products != null) {
            for (Product p : products) {
                result.add(buildSingleProductMap(p)); // Tách tiếp việc ép 1 Product ra hàm nhỏ hơn
            }
        }
        return result;
    }

    private boolean checkProductAvailability(Product p) {
        if (p == null || !p.isAvailable()) {
            return false;
        }
        try {
            return ingredientDAO.hasSufficientStockForProduct(p.getProductId(), 1);
        } catch (ValidationException e) {
            return false;
        }
    }

    private Map<String, Object> buildSingleProductMap(Product p) {
        Map<String, Object> map = new HashMap<>();
        map.put("productId", p.getProductId());
        map.put("productName", p.getProductName());
        map.put("price", p.getPrice());
        map.put("imageUrl", p.getImageUrl());
        map.put("available", checkProductAvailability(p));
        return map;
    }

    private String buildRecipeString(int productId) throws ValidationException {
        List<ProductIngredient> list = ingredientDAO.getIngredientsByProductId(productId);
        if (list == null || list.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            ProductIngredient pi = list.get(i);
            if (i > 0) {
                sb.append(",");
            }
            sb.append(pi.getIngredientId()).append(":").append(pi.getQuantityUsed());
        }
        return sb.toString();
    }

    private Map<Integer, Double> parseAndValidateRecipes(String[] recipeIngredientIds, String[] recipeQuantities)
            throws ValidationException {
        Map<Integer, Double> recipeMap = new LinkedHashMap<>();
        if (recipeIngredientIds == null || recipeQuantities == null) {
            return recipeMap;
        }
        int len = Math.min(recipeIngredientIds.length, recipeQuantities.length);
        for (int i = 0; i < len; i++) {
            String ingIdStr = recipeIngredientIds[i];
            String qtyStr = recipeQuantities[i];
            if (ingIdStr == null || ingIdStr.trim().isEmpty()) {
                continue;
            }
            int ingId;
            try {
                ingId = Integer.parseInt(ingIdStr.trim());
            } catch (NumberFormatException e) {
                continue;
            }
            if (ingId <= 0) {
                continue;
            }
            double qty = Validator.checkPositiveDouble(qtyStr, Constants.ERROR_INVALID_RECIPE_QUANTITY);
            Ingredient ing = ingredientDAO.getIngredientById(ingId);
            if (ing == null || !ing.isActive()) {
                throw new ValidationException(Constants.ERROR_INGREDIENT_NOT_FOUND);
            }
            recipeMap.put(ingId, recipeMap.getOrDefault(ingId, 0.0) + qty);
        }
        return recipeMap;
    }

    private void saveProductRecipes(int productId, Map<Integer, Double> recipeMap) throws ValidationException {
        if (recipeMap != null) {
            for (Map.Entry<Integer, Double> entry : recipeMap.entrySet()) {
                ProductIngredient pi = new ProductIngredient(productId, entry.getKey(), entry.getValue());
                ingredientDAO.insertProductIngredient(pi);
            }
        }
    }
}
