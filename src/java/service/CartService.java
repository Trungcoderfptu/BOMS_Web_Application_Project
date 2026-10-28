/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import dao.IngredientDAO;
import dao.MenuOptionDAO;
import dao.OrderDAO;
import dao.ProductDAO;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import model.Ingredient;
import model.MenuSize;
import model.MenuTopping;
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
    private MenuOptionDAO menuOptionDAO;

    public CartService() {
        this.productDAO = new ProductDAO();
        this.orderDAO = new OrderDAO();
        this.ingredientDAO = new IngredientDAO();
        this.menuOptionDAO = new MenuOptionDAO();
    }

    // ==========================================
    // ========== 1. MAIN SERVICE (FACADE) ======
    // ==========================================
    /**
     * Thêm nhanh 1 phần với tuỳ chọn mặc định (size đầu tiên, 100% đá, 100% đường, không topping).
     */
    public Object addProductToCart(Object sessionCart, int productId) throws ValidationException {
        return addProductToCart(sessionCart, productId, "1", null, null, null, null, null);
    }

    /**
     * Thêm món kèm tuỳ chọn từ popup chi tiết món (size, đá, đường, topping, ghi chú, số lượng).
     * Cùng món + cùng tuỳ chọn thì cộng dồn số lượng; khác tuỳ chọn thì tách thành dòng mới.
     */
    public Object addProductToCart(Object sessionCart, int productId, String quantityStr, String sizeIdStr,
            String iceLevel, String sugarLevel, String[] toppingIdStrs, String note) throws ValidationException {
        Product product = validateAndGetProduct(productId);
        int quantity = validateQuantity(quantityStr);
        OrderDetail newLine = buildCartLine(product, quantity, sizeIdStr, iceLevel, sugarLevel, toppingIdStrs, note);
        Order cart = getOrCreateCart(sessionCart);
        mergeOrAddLine(cart, newLine);
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

    /**
     * Cập nhật số lượng 1 dòng giỏ hàng (xác định bằng lineKey). Số lượng <= 0 thì xóa dòng.
     */
    public Object updateItemQuantity(Object sessionCart, String lineKey, int newQuantity) throws ValidationException {
        Order cart = validateAndGetCart(sessionCart);
        if (newQuantity > Constants.MAX_ITEM_QUANTITY) {
            throw new ValidationException(Constants.ERROR_INVALID_ITEM_QUANTITY);
        }
        updateQuantityOrRemove(cart, lineKey, newQuantity);
        recalculateTotal(cart);
        return cart;
    }

    public Object removeItemFromCart(Object sessionCart, String lineKey) throws ValidationException {
        Order cart = validateAndGetCart(sessionCart);
        removeItemByLineKey(cart, lineKey);
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

    private void updateQuantityOrRemove(Order cart, String lineKey, int newQuantity) {
        if (newQuantity <= 0) {
            removeItemByLineKey(cart, lineKey);
        } else {
            setItemQuantity(cart, lineKey, newQuantity);
        }
    }

    private void setItemQuantity(Order cart, String lineKey, int newQuantity) {
        OrderDetail item = findItemByLineKey(cart, lineKey);
        if (item != null) {
            item.setQuantity(newQuantity);
        }
    }

    private void removeItemByLineKey(Order cart, String lineKey) {
        cart.getOrderDetails().removeIf(item -> item.getLineKey() != null && item.getLineKey().equals(lineKey));
    }

    private void clearCartDetails(Order cart) {
        cart.getOrderDetails().clear();
        cart.setTotalAmount(0);
    }

    private void mergeOrAddLine(Order cart, OrderDetail newLine) throws ValidationException {
        OrderDetail existing = findItemByLineKey(cart, newLine.getLineKey());
        if (existing == null) {
            cart.getOrderDetails().add(newLine);
            return;
        }
        int merged = existing.getQuantity() + newLine.getQuantity();
        if (merged > Constants.MAX_ITEM_QUANTITY) {
            throw new ValidationException(Constants.ERROR_INVALID_ITEM_QUANTITY);
        }
        existing.setQuantity(merged);
    }

    private OrderDetail findItemByLineKey(Order cart, String lineKey) {
        if (lineKey == null) {
            return null;
        }
        for (OrderDetail item : cart.getOrderDetails()) {
            if (lineKey.equals(item.getLineKey())) {
                return item;
            }
        }
        return null;
    }

    // ==========================================
    // ========== 2b. TUỲ CHỌN MÓN (SIZE/ĐÁ/ĐƯỜNG/TOPPING/GHI CHÚ)
    // ==========================================
    /**
     * Dựng 1 dòng giỏ hàng: đơn giá = giá món + tiền size + tổng giá topping.
     * Món không có tuỳ chọn (HasOptions = 0, ví dụ kem) chỉ nhận ghi chú.
     */
    private OrderDetail buildCartLine(Product product, int quantity, String sizeIdStr, String iceLevel,
            String sugarLevel, String[] toppingIdStrs, String note) throws ValidationException {
        OrderDetail line = new OrderDetail(0, product.getProductId(), quantity, product.getPrice());
        line.setProduct(product);
        String sizeKey = "";
        String toppingKey = "";
        if (product.isHasOptions()) {
            MenuSize size = resolveSize(sizeIdStr);
            if (size != null) {
                line.setSizeName(size.getSizeName());
                line.setUnitPrice(line.getUnitPrice() + size.getExtraPrice());
                sizeKey = String.valueOf(size.getSizeId());
            }
            line.setIceLevel(resolveLevel(iceLevel, Constants.ICE_LEVELS, Constants.DEFAULT_ICE_LEVEL, Constants.ERROR_INVALID_ICE_LEVEL));
            line.setSugarLevel(resolveLevel(sugarLevel, Constants.SUGAR_LEVELS, Constants.DEFAULT_SUGAR_LEVEL, Constants.ERROR_INVALID_SUGAR_LEVEL));
            List<MenuTopping> toppings = resolveToppings(toppingIdStrs);
            applyToppings(line, toppings);
            toppingKey = buildToppingKey(toppings);
        }
        line.setNote(validateNote(note));
        line.setLineKey(product.getProductId() + "|" + sizeKey + "|" + nullToEmpty(line.getIceLevel()) + "|"
                + nullToEmpty(line.getSugarLevel()) + "|" + toppingKey + "|" + nullToEmpty(line.getNote()));
        return line;
    }

    /**
     * Size bắt buộc chọn nếu cửa hàng đang bán theo size. Không gửi size thì lấy size đầu tiên (thêm nhanh).
     */
    private MenuSize resolveSize(String sizeIdStr) throws ValidationException {
        List<MenuSize> activeSizes = menuOptionDAO.getSizes(true);
        if (activeSizes.isEmpty()) {
            return null;
        }
        if (sizeIdStr == null || sizeIdStr.trim().isEmpty()) {
            return activeSizes.get(0);
        }
        int sizeId = Validator.checkInteger(sizeIdStr, Constants.ERROR_INVALID_MENU_SIZE);
        for (MenuSize s : activeSizes) {
            if (s.getSizeId() == sizeId) {
                return s;
            }
        }
        throw new ValidationException(Constants.ERROR_INVALID_MENU_SIZE);
    }

    private String resolveLevel(String level, String[] allowed, String defaultLevel, String errorCode) throws ValidationException {
        if (level == null || level.trim().isEmpty()) {
            return defaultLevel;
        }
        for (String a : allowed) {
            if (a.equals(level.trim())) {
                return a;
            }
        }
        throw new ValidationException(errorCode);
    }

    private List<MenuTopping> resolveToppings(String[] toppingIdStrs) throws ValidationException {
        List<MenuTopping> result = new ArrayList<>();
        if (toppingIdStrs == null) {
            return result;
        }
        Set<Integer> seen = new TreeSet<>();
        for (String idStr : toppingIdStrs) {
            seen.add(Validator.checkInteger(idStr, Constants.ERROR_INVALID_MENU_TOPPING));
        }
        for (Integer toppingId : seen) {
            MenuTopping topping = menuOptionDAO.getToppingById(toppingId);
            if (topping == null || !topping.isActive()) {
                throw new ValidationException(Constants.ERROR_INVALID_MENU_TOPPING);
            }
            result.add(topping);
        }
        return result;
    }

    private void applyToppings(OrderDetail line, List<MenuTopping> toppings) {
        if (toppings.isEmpty()) {
            return;
        }
        List<String> names = new ArrayList<>();
        double toppingTotal = 0;
        for (MenuTopping t : toppings) {
            names.add(t.getToppingName());
            toppingTotal += t.getPrice();
        }
        line.setToppings(String.join(Constants.TOPPING_SEPARATOR, names));
        line.setUnitPrice(line.getUnitPrice() + toppingTotal);
    }

    private String buildToppingKey(List<MenuTopping> toppings) {
        List<String> ids = new ArrayList<>();
        for (MenuTopping t : toppings) {
            ids.add(String.valueOf(t.getToppingId()));
        }
        return String.join(",", ids);
    }

    private String validateNote(String note) throws ValidationException {
        if (note == null || note.trim().isEmpty()) {
            return null;
        }
        Validator.checkMaxLength(note, Constants.MAX_ITEM_NOTE_LENG, Constants.ERROR_ITEM_NOTE_TOO_LONG);
        return note.trim();
    }

    private int validateQuantity(String quantityStr) throws ValidationException {
        if (quantityStr == null || quantityStr.trim().isEmpty()) {
            return 1;
        }
        int quantity = Validator.checkInteger(quantityStr, Constants.ERROR_INVALID_ITEM_QUANTITY);
        if (quantity < 1 || quantity > Constants.MAX_ITEM_QUANTITY) {
            throw new ValidationException(Constants.ERROR_INVALID_ITEM_QUANTITY);
        }
        return quantity;
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
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
        if (product == null || !product.isAvailable() || !ingredientDAO.hasSufficientStockForProduct(productId, 1)) {
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
                throw new ValidationException(Constants.ERROR_INSUFFICIENT_INGREDIENTS);
            }
        }
    }

    private Map<Integer, Double> calculateTotalIngredientsNeeded(Order cart) throws ValidationException {
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

    private void deductIngredientsStock(Order cart) throws ValidationException {
        Map<Integer, Double> neededMap = calculateTotalIngredientsNeeded(cart);
        for (Map.Entry<Integer, Double> entry : neededMap.entrySet()) {
            int ingredientId = entry.getKey();
            double amount = entry.getValue();
            ingredientDAO.deductStock(ingredientId, amount);
        }
    }
}
