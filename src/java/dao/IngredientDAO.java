/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import java.util.ArrayList;
import java.util.List;
import model.Ingredient;
import model.ProductIngredient;
import utility.ValidationException;

/**
 * Data Access Object quản lý kho nguyên liệu và công thức định lượng (In-Memory Mock)
 * @author AD
 */
public class IngredientDAO {

    private static List<Ingredient> mockIngredients = new ArrayList<>();
    private static List<ProductIngredient> mockProductIngredients = new ArrayList<>();

    static {
        // Mock nguyên liệu kho (Ingredients)
        mockIngredients.add(new Ingredient(1, 1, "Sữa tươi & Sữa đặc", "Lít", 20.0, 5.0, true));
        mockIngredients.add(new Ingredient(2, 1, "Trân châu đen", "Kg", 10.0, 2.0, true));
        mockIngredients.add(new Ingredient(3, 1, "Vỏ kem ốc quế", "Cái", 30.0, 5.0, true));
        mockIngredients.add(new Ingredient(4, 2, "Trà xanh & Rau má", "Kg", 15.0, 3.0, true));
        mockIngredients.add(new Ingredient(5, 2, "Đường nước & Mật mía", "Lít", 25.0, 5.0, true));
        mockIngredients.add(new Ingredient(6, 2, "Vải ngâm đóng hộp", "Hộp", 8.0, 2.0, true));

        // Mock công thức sản phẩm (ProductIngredients)
        // Món 1: Bá mía đè tem (Kem trân châu)
        mockProductIngredients.add(new ProductIngredient(1, 1, 0.1)); // 0.1 Lít sữa
        mockProductIngredients.add(new ProductIngredient(1, 2, 0.05)); // 0.05 Kg trân châu

        // Món 2: Khô gà loại 2 (Kem ốc quế)
        mockProductIngredients.add(new ProductIngredient(2, 1, 0.1)); // 0.1 Lít sữa
        mockProductIngredients.add(new ProductIngredient(2, 3, 1.0)); // 1 Cái ốc quế

        // Món 3: Nước rau má (Trà sữa đường đen)
        mockProductIngredients.add(new ProductIngredient(3, 4, 0.05)); // 0.05 Kg rau má
        mockProductIngredients.add(new ProductIngredient(3, 5, 0.03)); // 0.03 Lít đường

        // Món 4: Độ MIXI (Trà ô long vải)
        mockProductIngredients.add(new ProductIngredient(4, 4, 0.04)); // 0.04 Kg trà
        mockProductIngredients.add(new ProductIngredient(4, 5, 0.02)); // 0.02 Lít đường
        mockProductIngredients.add(new ProductIngredient(4, 6, 0.1));  // 0.1 Hộp vải
    }

    public List<Ingredient> getAllIngredients() throws ValidationException {
        return mockIngredients;
    }

    public Ingredient getIngredientById(int ingredientId) {
        for (Ingredient ing : mockIngredients) {
            if (ing.getIngredientId() == ingredientId) {
                return ing;
            }
        }
        return null;
    }

    public List<ProductIngredient> getIngredientsByProductId(int productId) {
        List<ProductIngredient> result = new ArrayList<>();
        for (ProductIngredient pi : mockProductIngredients) {
            if (pi.getProductId() == productId) {
                result.add(pi);
            }
        }
        return result;
    }

    public List<Integer> getProductIdsUsingIngredient(int ingredientId) {
        List<Integer> productIds = new ArrayList<>();
        for (ProductIngredient pi : mockProductIngredients) {
            if (pi.getIngredientId() == ingredientId && !productIds.contains(pi.getProductId())) {
                productIds.add(pi.getProductId());
            }
        }
        return productIds;
    }

    public synchronized void deductStock(int ingredientId, double amount) {
        Ingredient ing = getIngredientById(ingredientId);
        if (ing != null) {
            double currentStock = ing.getQuantity();
            ing.setQuantity(Math.max(0, currentStock - amount));
        }
    }

    public synchronized void updateIngredientQuantity(int ingredientId, double newQuantity) {
        Ingredient ing = getIngredientById(ingredientId);
        if (ing != null) {
            ing.setQuantity(newQuantity);
        }
    }

    public synchronized void restoreStock(int ingredientId, double amount) {
        Ingredient ing = getIngredientById(ingredientId);
        if (ing != null) {
            ing.setQuantity(ing.getQuantity() + amount);
        }
    }

    public boolean hasSufficientStockForProduct(int productId, int quantity) {
        List<ProductIngredient> recipes = getIngredientsByProductId(productId);
        for (ProductIngredient pi : recipes) {
            Ingredient ing = getIngredientById(pi.getIngredientId());
            if (ing == null || ing.getQuantity() < (pi.getQuantityUsed() * quantity)) {
                return false;
            }
        }
        return true;
    }
}
