/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import service.MenuOptionService;
import service.ProductService;
import utility.Constants;
import utility.FlashMessageUtil;
import utility.ImageUploadUtility;
import utility.ValidationException;

/**
 * Controller điều phối nghiệp vụ Quản lý Thực đơn (Menu Management)
 * Tuyệt đối không import Model hoặc DAO (tuân thủ Rule_BOMS.md)
 *
 * @author AD
 */
@MultipartConfig(
        fileSizeThreshold = 2 * 1024 * 1024, // 2 MB
        maxFileSize = 10 * 1024 * 1024, // 10 MB
        maxRequestSize = 30 * 1024 * 1024 // 30 MB
)
public class MenuManagementController extends HttpServlet {

    private ProductService productService;
    private MenuOptionService menuOptionService;

    @Override
    public void init() throws ServletException {
        productService = new ProductService();
        menuOptionService = new MenuOptionService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        HttpSession session = request.getSession();

        @SuppressWarnings("unchecked")
        Map<String, String> userSession = (Map<String, String>) session.getAttribute(Constants.USER_SESSION);
        if (userSession == null || !hasAccessPermission(userSession.get("role"))) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        FlashMessageUtil.forwardFlashMessages(request, session);

        String filterCategory = request.getParameter("filterCategory");
        if (filterCategory == null || filterCategory.trim().isEmpty()) {
            filterCategory = "ALL";
        }

        try {
            List<Map<String, Object>> categoryList = productService.getAllCategoriesForView();
            List<Map<String, Object>> ingredientList = productService.getAllActiveIngredientsForView();
            List<Map<String, Object>> allProducts = productService.getAllProductsForManagementView();
            List<Map<String, Object>> filteredProducts = filterProductsByCategory(allProducts, filterCategory);

            request.setAttribute("PRODUCT_LIST", filteredProducts);
            request.setAttribute("CATEGORY_LIST", categoryList);
            request.setAttribute("INGREDIENT_LIST", ingredientList);
            request.setAttribute("SIZE_LIST", menuOptionService.getSizesForView(false));
            request.setAttribute("TOPPING_LIST", menuOptionService.getToppingsForView(false));
            request.setAttribute("CAN_MANAGE_OPTIONS", menuOptionService.canManageOptions(userSession.get("role")));
            request.setAttribute("CURRENT_FILTER_CATEGORY", filterCategory);
        } catch (ValidationException e) {
            request.setAttribute(Constants.ERROR_MESSAGE, e.getMessage());
        }

        request.getRequestDispatcher("menu_management.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        HttpSession session = request.getSession();

        @SuppressWarnings("unchecked")
        Map<String, String> userSession = (Map<String, String>) session.getAttribute(Constants.USER_SESSION);
        if (userSession == null || !hasAccessPermission(userSession.get("role"))) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        String redirectUrl = "MenuManagementController?filterCategory=ALL";

        try {
            String filterCategory = request.getParameter("filterCategory");
            if (filterCategory != null && !filterCategory.trim().isEmpty()) {
                redirectUrl = "MenuManagementController?filterCategory=" + filterCategory.trim();
            }
            String action = request.getParameter("action");

            if ("createProduct".equals(action)) {
                String productName = request.getParameter("productName");
                String categoryId = request.getParameter("categoryId");
                String price = request.getParameter("price");
                String imageUrl = resolveImageUrl(request);

                String[] recipeIngredientIds = request.getParameterValues("recipeIngredientId");
                String[] recipeQuantities = request.getParameterValues("recipeQuantity");

                productService.createProduct(categoryId, productName, price, imageUrl, recipeIngredientIds, recipeQuantities,
                        request.getParameter("description"), request.getParameter("videoUrl"),
                        lastValue(request, "isFeatured"), lastValue(request, "hasOptions"));
                session.setAttribute(Constants.SUCCESS_MESSAGE, Constants.SUCCESS_CREATE_PRODUCT);
                redirectUrl = "MenuManagementController?filterCategory=ALL";
            } else if ("updateProduct".equals(action)) {
                String productId = request.getParameter("productId");
                String productName = request.getParameter("productName");
                String categoryId = request.getParameter("categoryId");
                String price = request.getParameter("price");
                String imageUrl = resolveImageUrl(request);
                String isAvailable = request.getParameter("isAvailable");

                String[] recipeIngredientIds = request.getParameterValues("recipeIngredientId");
                String[] recipeQuantities = request.getParameterValues("recipeQuantity");

                productService.updateProduct(productId, categoryId, productName, price, imageUrl, isAvailable, recipeIngredientIds, recipeQuantities,
                        request.getParameter("description"), request.getParameter("videoUrl"),
                        lastValue(request, "isFeatured"), lastValue(request, "hasOptions"));
                session.setAttribute(Constants.SUCCESS_MESSAGE, Constants.SUCCESS_UPDATE_PRODUCT);
            } else if ("toggleStatus".equals(action)) {
                String productId = request.getParameter("productId");
                productService.toggleProductStatus(productId);
                session.setAttribute(Constants.SUCCESS_MESSAGE, Constants.SUCCESS_CHANGE_PRODUCT_STATUS);
            } else if ("createCategory".equals(action)) {
                String categoryName = request.getParameter("categoryName");
                productService.createCategory(categoryName);
                session.setAttribute(Constants.SUCCESS_MESSAGE, Constants.SUCCESS_CREATE_MENU_CATEGORY);
                redirectUrl = "MenuManagementController?manageCategory=true";
            } else if ("deleteCategory".equals(action)) {
                String categoryId = request.getParameter("categoryId");
                productService.deleteCategory(categoryId);
                session.setAttribute(Constants.SUCCESS_MESSAGE, Constants.SUCCESS_DELETE_MENU_CATEGORY);
                redirectUrl = "MenuManagementController?manageCategory=true";
            } else if (isMenuOptionAction(action)) {
                handleMenuOptionAction(request, session, action, userSession.get("role"));
                redirectUrl = "MenuManagementController?manageOptions=true";
            }
        } catch (ValidationException e) {
            session.setAttribute(Constants.ERROR_MESSAGE, e.getMessage());
            String action = request.getParameter("action");
            if ("createCategory".equals(action) || "deleteCategory".equals(action)) {
                redirectUrl = "MenuManagementController?manageCategory=true";
            } else if (isMenuOptionAction(action)) {
                redirectUrl = "MenuManagementController?manageOptions=true";
            }
        } catch (IllegalStateException e) {
            // Bắt lỗi kích thước file multipart vượt quá trần cấu hình, bảo vệ socket không bị reset
            session.setAttribute(Constants.ERROR_MESSAGE, Constants.ERROR_IMAGE_SIZE_EXCEEDED);
        } catch (Exception e) {
            session.setAttribute(Constants.ERROR_MESSAGE, Constants.ERROR_SYSTEM);
        }

        response.sendRedirect(request.getContextPath() + "/" + redirectUrl);
    }

    // ===== Size / Topping dùng chung (quyền Manager/Admin được kiểm tra trong MenuOptionService) =====
    private boolean isMenuOptionAction(String action) {
        return "createSize".equals(action) || "updateSize".equals(action) || "toggleSize".equals(action)
                || "createTopping".equals(action) || "updateTopping".equals(action) || "toggleTopping".equals(action);
    }

    private void handleMenuOptionAction(HttpServletRequest request, HttpSession session, String action, String role)
            throws ValidationException {
        switch (action) {
            case "createSize":
                menuOptionService.createSize(request.getParameter("sizeName"), request.getParameter("extraPrice"),
                        request.getParameter("sortOrder"), role);
                session.setAttribute(Constants.SUCCESS_MESSAGE, Constants.SUCCESS_CREATE_MENU_SIZE);
                break;
            case "updateSize":
                menuOptionService.updateSize(request.getParameter("sizeId"), request.getParameter("sizeName"),
                        request.getParameter("extraPrice"), request.getParameter("sortOrder"), role);
                session.setAttribute(Constants.SUCCESS_MESSAGE, Constants.SUCCESS_UPDATE_MENU_SIZE);
                break;
            case "toggleSize":
                menuOptionService.toggleSize(request.getParameter("sizeId"), role);
                session.setAttribute(Constants.SUCCESS_MESSAGE, Constants.SUCCESS_TOGGLE_MENU_SIZE);
                break;
            case "createTopping":
                menuOptionService.createTopping(request.getParameter("toppingName"), request.getParameter("toppingPrice"), role);
                session.setAttribute(Constants.SUCCESS_MESSAGE, Constants.SUCCESS_CREATE_MENU_TOPPING);
                break;
            case "updateTopping":
                menuOptionService.updateTopping(request.getParameter("toppingId"), request.getParameter("toppingName"),
                        request.getParameter("toppingPrice"), role);
                session.setAttribute(Constants.SUCCESS_MESSAGE, Constants.SUCCESS_UPDATE_MENU_TOPPING);
                break;
            default:
                menuOptionService.toggleTopping(request.getParameter("toppingId"), role);
                session.setAttribute(Constants.SUCCESS_MESSAGE, Constants.SUCCESS_TOGGLE_MENU_TOPPING);
                break;
        }
    }

    /**
     * Checkbox gửi kèm 1 input hidden "false" đứng trước: không tick -> chỉ có "false", tick -> lấy giá trị cuối "true".
     */
    private String lastValue(HttpServletRequest request, String name) {
        String[] values = request.getParameterValues(name);
        return (values == null || values.length == 0) ? null : values[values.length - 1];
    }

    private String resolveImageUrl(HttpServletRequest request) throws ValidationException {
        Part imagePart = getPartSafely(request, "productImageFile");
        if (imagePart != null && imagePart.getSize() > 0) {
            String uploadedPath = ImageUploadUtility.saveImage(imagePart, "products");
            if (uploadedPath != null) {
                return uploadedPath;
            }
        }
        String imageUrl = request.getParameter("imageUrl");
        return (imageUrl != null && !imageUrl.trim().isEmpty()) ? imageUrl.trim() : null;
    }

    private Part getPartSafely(HttpServletRequest request, String partName) {
        try {
            return request.getPart(partName);
        } catch (Exception e) {
            return null;
        }
    }

    private List<Map<String, Object>> filterProductsByCategory(List<Map<String, Object>> products, String filterCategory) {
        if (products == null || "ALL".equalsIgnoreCase(filterCategory)) {
            return products;
        }
        try {
            int targetCatId = Integer.parseInt(filterCategory);
            List<Map<String, Object>> filtered = new ArrayList<>();
            for (Map<String, Object> p : products) {
                Object catIdObj = p.get("categoryId");
                if (catIdObj != null && Integer.parseInt(catIdObj.toString()) == targetCatId) {
                    filtered.add(p);
                }
            }
            return filtered;
        } catch (NumberFormatException e) {
            return products;
        }
    }

    private boolean hasAccessPermission(String role) {
        if (role == null) {
            return false;
        }
        return "Staff".equalsIgnoreCase(role) || "Manager".equalsIgnoreCase(role) || "Admin".equalsIgnoreCase(role);
    }

    @Override
    public String getServletInfo() {
        return "Menu Management Controller";
    }
}
