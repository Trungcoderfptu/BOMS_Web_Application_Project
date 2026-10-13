/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import dao.IngredientDAO;
import dao.OrderDAO;
import dao.ProductDAO;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.Ingredient;
import model.Order;
import model.OrderDetail;
import model.Product;
import model.ProductIngredient;
import utility.Constants;
import utility.ValidationException;
import utility.Validator;

public class CartService {

    private ProductDAO productDAO;
    private OrderDAO orderDAO;
    private IngredientDAO ingredientDAO;

    public CartService() {
        this.productDAO = new ProductDAO();
        this.orderDAO = new OrderDAO();
        this.ingredientDAO = new IngredientDAO();
    }

    // ==========================================
    // ========== 1. MAIN SERVICE (FACADE) ======
    // ==========================================
    public Object addProductToCart(Object sessionCart, int productId) throws ValidationException {
        Product product = validateAndGetProduct(productId);
        Order cart = getOrCreateCart(sessionCart);
        addItemToCart(cart, product);
        recalculateTotal(cart);
        return cart;
    }

    public int checkout(Object sessionCart, String tableIdStr, Map<String, String> userSession) throws ValidationException {
        Order cart = validateAndGetCart(sessionCart);
        validateOrderSource(tableIdStr, userSession);
        validateCartItemsAvailability(cart);
        validateInventoryStock(cart);
        setupOrderDetails(cart, tableIdStr, userSession);
        int generatedOrderId = orderDAO.insertOrder(cart);
        deductIngredientsStock(cart);
        return generatedOrderId;
    }

    public Object updateItemQuantity(Object sessionCart, int productId, int newQuantity) throws ValidationException {
        Order cart = validateAndGetCart(sessionCart);
        updateQuantityOrRemove(cart, productId, newQuantity);
        recalculateTotal(cart);
        return cart;
    }

    public Object removeItemFromCart(Object sessionCart, int productId) throws ValidationException {
        Order cart = validateAndGetCart(sessionCart);
        removeItemByProductId(cart, productId);
        recalculateTotal(cart);
        return cart;
    }

    public void clearCart(Object sessionCart) throws ValidationException {
        Order cart = validateAndGetCart(sessionCart);
        clearCartDetails(cart);
    }

    // ==========================================
    // ========== 2. CART MANIPULATION ==========
    // ==========================================
    private Order getOrCreateCart(Object sessionCart) {
        if (sessionCart instanceof Order) {
            return (Order) sessionCart;
        }
        return new Order();
    }

    private void updateQuantityOrRemove(Order cart, int productId, int newQuantity) {
        if (newQuantity <= 0) {
            removeItemByProductId(cart, productId);
        } else {
            setItemQuantity(cart, productId, newQuantity);
        }
    }

    private void setItemQuantity(Order cart, int productId, int newQuantity) {
        OrderDetail item = findItemInCart(cart, productId);
        if (item != null) {
            item.setQuantity(newQuantity);
        }
    }

    private void removeItemByProductId(Order cart, int productId) {
        cart.getOrderDetails().removeIf(item -> item.getProductId() == productId);
    }

    private void clearCartDetails(Order cart) {
        cart.getOrderDetails().clear();
        cart.setTotalAmount(0);
    }

    private void addItemToCart(Order cart, Product product) {
        OrderDetail existingItem = findItemInCart(cart, product.getProductId());
        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + 1);
        } else {
            cart.getOrderDetails().add(createNewOrderDetail(product));
        }
    }

    private OrderDetail findItemInCart(Order cart, int productId) {
        for (OrderDetail item : cart.getOrderDetails()) {
            if (item.getProductId() == productId) {
                return item;
            }
        }
        return null;
    }

    private OrderDetail createNewOrderDetail(Product product) {
        OrderDetail newItem = new OrderDetail(0, product.getProductId(), 1, product.getPrice());
        newItem.setProduct(product);
        return newItem;
    }

    // ==========================================
    // ========== 3. CHECKOUT PREPARATION =======
    // ==========================================
    private void setupOrderDetails(Order cart, String tableIdStr, Map<String, String> userSession) {
        assignOrderSource(cart, tableIdStr, userSession);
        setupOrderMetadata(cart);
        recalculateTotal(cart);
    }

    private void assignOrderSource(Order cart, String tableIdStr, Map<String, String> userSession) {
        if (isTableOrder(tableIdStr)) {
            cart.setOrderType("Table");
            cart.setTableId(parseTableId(tableIdStr));
            cart.setStaffId(null);
        } else {
            cart.setOrderType("Counter");
            cart.setStaffId(parseStaffId(userSession));
            cart.setTableId(null);
        }
    }

    private void validateOrderSource(String tableIdStr, Map<String, String> userSession) throws ValidationException {
        if (!isTableOrder(tableIdStr) && parseStaffId(userSession) == null) {
            throw new ValidationException(Constants.ERROR_INVALID_TABLE);
        }
    }

    private boolean isTableOrder(String tableIdStr) {
        return tableIdStr != null && !tableIdStr.trim().isEmpty();
    }

    private int parseTableId(String tableIdStr) {
        try {
            return Integer.parseInt(tableIdStr.replaceAll("\\D+", ""));
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private Integer parseStaffId(Map<String, String> userSession) {
        if (userSession == null || userSession.get("userId") == null) {
            return null;
        }
        try {
            return Integer.parseInt(userSession.get("userId"));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void setupOrderMetadata(Order cart) {
        cart.setStatus("Accepted");
        cart.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        cart.setCookingStartedAt(null);
    }

    private void recalculateTotal(Order cart) {
        double total = 0;
        for (OrderDetail item : cart.getOrderDetails()) {
            total += (item.getUnitPrice() * item.getQuantity());
        }
        cart.setTotalAmount(total);
    }

    // ==========================================
    // ========== 4. VALIDATION SERVICE (TẦNG 2) =
    // ==========================================
    private Product validateAndGetProduct(int productId) throws ValidationException {
        if (productId <= 0) {
            throw new ValidationException(Constants.ERROR_PRODUCT_NOT_AVAILABLE);
        }
        Product product = productDAO.getProductById(productId);
        if (product == null || !product.isAvailable()) {
            throw new ValidationException(Constants.ERROR_PRODUCT_NOT_AVAILABLE);
        }
        return product;
    }

    private Order validateAndGetCart(Object sessionCart) throws ValidationException {
        Validator.checkNotNull(sessionCart, Constants.ERROR_EMPTY_CART);

        if (!(sessionCart instanceof Order)) {
            throw new ValidationException(Constants.ERROR_EMPTY_CART);
        }
        Order cart = (Order) sessionCart;
        List<OrderDetail> items = cart.getOrderDetails();
        if (items == null || items.isEmpty()) {
            throw new ValidationException(Constants.ERROR_EMPTY_CART);
        }
        return cart;
    }

    private void validateCartItemsAvailability(Order cart) throws ValidationException {
        for (OrderDetail item : cart.getOrderDetails()) {
            validateItemAvailability(item);
        }
    }

    private void validateItemAvailability(OrderDetail item) throws ValidationException {
        Product product = productDAO.getProductById(item.getProductId());
        if (product == null || !product.isAvailable()) {
            throw new ValidationException(Constants.ERROR_PRODUCT_NOT_AVAILABLE);
        }
    }

    // ==========================================
    // ========== 5. INVENTORY LOGIC (UC-01 POST-3)
    // ==========================================
    private void validateInventoryStock(Order cart) throws ValidationException {
        Map<Integer, Double> neededMap = calculateTotalIngredientsNeeded(cart);
        for (Map.Entry<Integer, Double> entry : neededMap.entrySet()) {
            int ingredientId = entry.getKey();
            double neededAmount = entry.getValue();
            Ingredient ingredient = ingredientDAO.getIngredientById(ingredientId);

            if (ingredient == null || ingredient.getQuantity() < neededAmount) {
                disableProductsUsingIngredient(ingredientId);
                throw new ValidationException(Constants.ERROR_INSUFFICIENT_INGREDIENTS);
            }
        }
    }

    private Map<Integer, Double> calculateTotalIngredientsNeeded(Order cart) {
        Map<Integer, Double> neededMap = new HashMap<>();
        for (OrderDetail item : cart.getOrderDetails()) {
            List<ProductIngredient> recipes = ingredientDAO.getIngredientsByProductId(item.getProductId());
            for (ProductIngredient pi : recipes) {
                double totalItemNeeded = pi.getQuantityUsed() * item.getQuantity();
                neededMap.put(pi.getIngredientId(), neededMap.getOrDefault(pi.getIngredientId(), 0.0) + totalItemNeeded);
            }
        }
        return neededMap;
    }

    private void disableProductsUsingIngredient(int ingredientId) {
        List<Integer> productIds = ingredientDAO.getProductIdsUsingIngredient(ingredientId);
        for (Integer pid : productIds) {
            productDAO.updateProductAvailability(pid, false);
        }
    }

    private void deductIngredientsStock(Order cart) {
        Map<Integer, Double> neededMap = calculateTotalIngredientsNeeded(cart);
        for (Map.Entry<Integer, Double> entry : neededMap.entrySet()) {
            int ingredientId = entry.getKey();
            double amount = entry.getValue();
            ingredientDAO.deductStock(ingredientId, amount);
            checkAndDisableIfDepleted(ingredientId);
        }
    }

    private void checkAndDisableIfDepleted(int ingredientId) {
        Ingredient ing = ingredientDAO.getIngredientById(ingredientId);
        if (ing != null && ing.getQuantity() <= 0) {
            disableProductsUsingIngredient(ingredientId);
        }
    }
}
