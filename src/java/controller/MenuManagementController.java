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
import service.ProductService;
import utility.Constants;
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

    @Override
    public void init() throws ServletException {
        productService = new ProductService();
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

        forwardFlashMessages(request, session);

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
                String imageUrl = request.getParameter("imageUrl");

                Part imagePart = getPartSafely(request, "productImageFile");
                if (imagePart != null && imagePart.getSize() > 0) {
                    String uploadedPath = ImageUploadUtility.saveImage(imagePart, "products");
                    if (uploadedPath != null) {
                        imageUrl = uploadedPath;
                    }
                }

                String[] recipeIngredientIds = request.getParameterValues("recipeIngredientId");
                String[] recipeQuantities = request.getParameterValues("recipeQuantity");

                productService.createProduct(categoryId, productName, price, imageUrl, recipeIngredientIds, recipeQuantities);
                session.setAttribute(Constants.SUCCESS_MESSAGE, Constants.SUCCESS_CREATE_PRODUCT);
                redirectUrl = "MenuManagementController?filterCategory=ALL";
            } else if ("updateProduct".equals(action)) {
                String productId = request.getParameter("productId");
                String productName = request.getParameter("productName");
                String categoryId = request.getParameter("categoryId");
                String price = request.getParameter("price");
                String imageUrl = request.getParameter("imageUrl");
                String isAvailable = request.getParameter("isAvailable");

                Part imagePart = getPartSafely(request, "productImageFile");
                if (imagePart != null && imagePart.getSize() > 0) {
                    String uploadedPath = ImageUploadUtility.saveImage(imagePart, "products");
                    if (uploadedPath != null) {
                        imageUrl = uploadedPath;
                    }
                }

                String[] recipeIngredientIds = request.getParameterValues("recipeIngredientId");
                String[] recipeQuantities = request.getParameterValues("recipeQuantity");

                productService.updateProduct(productId, categoryId, productName, price, imageUrl, isAvailable, recipeIngredientIds, recipeQuantities);
                session.setAttribute(Constants.SUCCESS_MESSAGE, Constants.SUCCESS_UPDATE_PRODUCT);
            } else if ("toggleStatus".equals(action)) {
                String productId = request.getParameter("productId");
                productService.toggleProductStatus(productId);
                session.setAttribute(Constants.SUCCESS_MESSAGE, Constants.SUCCESS_CHANGE_PRODUCT_STATUS);
            }
        } catch (ValidationException e) {
            session.setAttribute(Constants.ERROR_MESSAGE, e.getMessage());
        } catch (IllegalStateException e) {
            // Bắt lỗi kích thước file multipart vượt quá trần cấu hình, bảo vệ socket không bị reset
            session.setAttribute(Constants.ERROR_MESSAGE, Constants.ERROR_IMAGE_SIZE_EXCEEDED);
        } catch (Exception e) {
            session.setAttribute(Constants.ERROR_MESSAGE, Constants.SERROR_INVALID_SETTING_TAB);
        }

        response.sendRedirect(request.getContextPath() + "/" + redirectUrl);
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

    private void forwardFlashMessages(HttpServletRequest request, HttpSession session) {
        if (session.getAttribute(Constants.SUCCESS_MESSAGE) != null) {
            request.setAttribute(Constants.SUCCESS_MESSAGE, session.getAttribute(Constants.SUCCESS_MESSAGE));
            session.removeAttribute(Constants.SUCCESS_MESSAGE);
        }
        if (session.getAttribute(Constants.ERROR_MESSAGE) != null) {
            request.setAttribute(Constants.ERROR_MESSAGE, session.getAttribute(Constants.ERROR_MESSAGE));
            session.removeAttribute(Constants.ERROR_MESSAGE);
        }
    }

    @Override
    public String getServletInfo() {
        return "Menu Management Controller";
    }
}
