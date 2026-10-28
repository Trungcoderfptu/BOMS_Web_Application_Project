package service;

import dao.MenuOptionDAO;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.MenuSize;
import model.MenuTopping;
import utility.Constants;
import utility.ValidationException;
import utility.Validator;

/**
 * Nghiệp vụ tuỳ chọn món dùng chung (size ly, topping).
 * - Đọc danh sách cho màn đặt món và form quản lý.
 * - Thêm / sửa / bật-tắt: chỉ Manager và Admin.
 */
public class MenuOptionService {

    private MenuOptionDAO menuOptionDAO;

    public MenuOptionService() {
        this.menuOptionDAO = new MenuOptionDAO();
    }

    // ==========================================
    // ========== 1. ĐỌC CHO VIEW ===============
    // ==========================================
    public List<Map<String, Object>> getSizesForView(boolean activeOnly) throws ValidationException {
        List<Map<String, Object>> result = new ArrayList<>();
        for (MenuSize s : menuOptionDAO.getSizes(activeOnly)) {
            Map<String, Object> map = new HashMap<>();
            map.put("sizeId", String.valueOf(s.getSizeId()));
            map.put("sizeName", s.getSizeName());
            map.put("extraPrice", s.getExtraPrice());
            map.put("sortOrder", s.getSortOrder());
            map.put("active", s.isActive());
            result.add(map);
        }
        return result;
    }

    public List<Map<String, Object>> getToppingsForView(boolean activeOnly) throws ValidationException {
        List<Map<String, Object>> result = new ArrayList<>();
        for (MenuTopping t : menuOptionDAO.getToppings(activeOnly)) {
            Map<String, Object> map = new HashMap<>();
            map.put("toppingId", String.valueOf(t.getToppingId()));
            map.put("toppingName", t.getToppingName());
            map.put("price", t.getPrice());
            map.put("active", t.isActive());
            result.add(map);
        }
        return result;
    }

    public boolean canManageOptions(String role) {
        return Constants.ROLE_MANAGER.equalsIgnoreCase(role) || Constants.ROLE_ADMIN.equalsIgnoreCase(role);
    }

    // ==========================================
    // ========== 2. SIZE =======================
    // ==========================================
    public void createSize(String sizeName, String extraPriceStr, String sortOrderStr, String role) throws ValidationException {
        validateManagePermission(role);
        String name = validateOptionName(sizeName);
        if (menuOptionDAO.isSizeNameExists(name, 0)) {
            throw new ValidationException(Constants.ERROR_MENU_OPTION_EXISTS);
        }
        MenuSize size = new MenuSize(0, name, parsePrice(extraPriceStr), parseSortOrder(sortOrderStr), true);
        menuOptionDAO.insertSize(size);
    }

    public void updateSize(String sizeIdStr, String sizeName, String extraPriceStr, String sortOrderStr, String role) throws ValidationException {
        validateManagePermission(role);
        MenuSize existing = getExistingSize(sizeIdStr);
        String name = validateOptionName(sizeName);
        if (menuOptionDAO.isSizeNameExists(name, existing.getSizeId())) {
            throw new ValidationException(Constants.ERROR_MENU_OPTION_EXISTS);
        }
        existing.setSizeName(name);
        existing.setExtraPrice(parsePrice(extraPriceStr));
        existing.setSortOrder(parseSortOrder(sortOrderStr));
        menuOptionDAO.updateSize(existing);
    }

    public void toggleSize(String sizeIdStr, String role) throws ValidationException {
        validateManagePermission(role);
        menuOptionDAO.toggleSizeActive(getExistingSize(sizeIdStr).getSizeId());
    }

    // ==========================================
    // ========== 3. TOPPING ====================
    // ==========================================
    public void createTopping(String toppingName, String priceStr, String role) throws ValidationException {
        validateManagePermission(role);
        String name = validateOptionName(toppingName);
        if (menuOptionDAO.isToppingNameExists(name, 0)) {
            throw new ValidationException(Constants.ERROR_MENU_OPTION_EXISTS);
        }
        menuOptionDAO.insertTopping(new MenuTopping(0, name, parsePrice(priceStr), true));
    }

    public void updateTopping(String toppingIdStr, String toppingName, String priceStr, String role) throws ValidationException {
        validateManagePermission(role);
        MenuTopping existing = getExistingTopping(toppingIdStr);
        String name = validateOptionName(toppingName);
        if (menuOptionDAO.isToppingNameExists(name, existing.getToppingId())) {
            throw new ValidationException(Constants.ERROR_MENU_OPTION_EXISTS);
        }
        existing.setToppingName(name);
        existing.setPrice(parsePrice(priceStr));
        menuOptionDAO.updateTopping(existing);
    }

    public void toggleTopping(String toppingIdStr, String role) throws ValidationException {
        validateManagePermission(role);
        menuOptionDAO.toggleToppingActive(getExistingTopping(toppingIdStr).getToppingId());
    }

    // ==========================================
    // ========== 4. VALIDATION =================
    // ==========================================
    private void validateManagePermission(String role) throws ValidationException {
        if (!canManageOptions(role)) {
            throw new ValidationException(Constants.ERROR_MENU_OPTION_FORBIDDEN);
        }
    }

    private String validateOptionName(String name) throws ValidationException {
        Validator.checkEmpty(name, Constants.ERROR_MENU_OPTION_NAME);
        Validator.checkMaxLength(name, Constants.MAX_MENU_OPTION_NAME_LENG, Constants.ERROR_MENU_OPTION_NAME);
        return name.trim();
    }

    private double parsePrice(String priceStr) throws ValidationException {
        if (priceStr == null || priceStr.trim().isEmpty()) {
            return 0;
        }
        return Validator.checkNonNegativeDouble(priceStr, Constants.ERROR_MENU_OPTION_PRICE);
    }

    private int parseSortOrder(String sortOrderStr) {
        try {
            return (sortOrderStr == null || sortOrderStr.trim().isEmpty()) ? 0 : Integer.parseInt(sortOrderStr.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private MenuSize getExistingSize(String sizeIdStr) throws ValidationException {
        int sizeId = Validator.checkInteger(sizeIdStr, Constants.ERROR_INVALID_MENU_SIZE);
        MenuSize size = menuOptionDAO.getSizeById(sizeId);
        if (size == null) {
            throw new ValidationException(Constants.ERROR_INVALID_MENU_SIZE);
        }
        return size;
    }

    private MenuTopping getExistingTopping(String toppingIdStr) throws ValidationException {
        int toppingId = Validator.checkInteger(toppingIdStr, Constants.ERROR_INVALID_MENU_TOPPING);
        MenuTopping topping = menuOptionDAO.getToppingById(toppingId);
        if (topping == null) {
            throw new ValidationException(Constants.ERROR_INVALID_MENU_TOPPING);
        }
        return topping;
    }
}
