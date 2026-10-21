package controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import service.IngredientService;
import utility.Constants;
import utility.FlashMessageUtil;
import utility.ValidationException;

@WebServlet(name = "InventoryManagementController", urlPatterns = {"/InventoryManagementController"})
public class InventoryManagementController extends HttpServlet {

    private final IngredientService ingredientService = new IngredientService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (!isUserAuthorized(session)) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        try {
            FlashMessageUtil.forwardFlashMessages(request, session);
            String searchTerm = request.getParameter("search");
            List<Map<String, Object>> ingredients = ingredientService.getAllIngredientsForView(searchTerm);
            List<Map<String, Object>> categories = ingredientService.getCategoriesForView();

            request.setAttribute("INGREDIENT_LIST", ingredients);
            request.setAttribute("CATEGORY_LIST", categories);
            request.setAttribute("SEARCH_TERM", searchTerm != null ? searchTerm.trim() : "");

            Map<String, String> userSession = getUserSession(session);
            String userRole = userSession != null ? userSession.get("role") : "";
            boolean isManager = "Manager".equalsIgnoreCase(userRole) || "Admin".equalsIgnoreCase(userRole);
            request.setAttribute("IS_MANAGER", isManager);

            request.getRequestDispatcher("/inventory_managerment.jsp").forward(request, response);
        } catch (ValidationException e) {
            request.setAttribute(Constants.ERROR_MESSAGE, e.getMessage());
            request.getRequestDispatcher("/inventory_managerment.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (!isUserAuthorized(session)) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        Map<String, String> userSession = getUserSession(session);
        String userRole = userSession != null ? userSession.get("role") : "";
        String action = request.getParameter("action");

        try {
            if ("updateThreshold".equals(action)) {
                handleUpdateThreshold(request, session, userRole);
            } else if ("create".equals(action)) {
                handleCreateIngredient(request, session, userRole);
            } else if ("update".equals(action)) {
                handleUpdateIngredient(request, session, userRole);
            } else if ("delete".equals(action)) {
                handleDeleteIngredient(request, session, userRole);
            }
        } catch (ValidationException e) {
            session.setAttribute(Constants.ERROR_MESSAGE, e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/InventoryManagementController");
    }

    // =========================================================================
    // HELPER METHODS (SRP)
    // =========================================================================

    private boolean isUserAuthorized(HttpSession session) {
        if (session == null || session.getAttribute(Constants.USER_SESSION) == null) {
            return false;
        }
        Map<String, String> user = getUserSession(session);
        if (user == null) {
            return false;
        }
        String role = user.get("role");
        return "Staff".equalsIgnoreCase(role) || "Manager".equalsIgnoreCase(role) || "Admin".equalsIgnoreCase(role);
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> getUserSession(HttpSession session) {
        if (session == null) {
            return null;
        }
        return (Map<String, String>) session.getAttribute(Constants.USER_SESSION);
    }

    private void handleUpdateThreshold(HttpServletRequest request, HttpSession session, String userRole)
            throws ValidationException {
        String ingredientId = request.getParameter("ingredientId");
        String threshold = request.getParameter("threshold");
        ingredientService.updateWarningThreshold(ingredientId, threshold, userRole);
        session.setAttribute(Constants.SUCCESS_MESSAGE, Constants.SUCCESS_UPDATE_THRESHOLD);
    }

    private void handleCreateIngredient(HttpServletRequest request, HttpSession session, String userRole)
            throws ValidationException {
        String categoryId = request.getParameter("categoryId");
        String name = request.getParameter("ingredientName");
        String unit = request.getParameter("unit");
        String quantity = request.getParameter("quantity");
        String price = request.getParameter("price");
        String location = request.getParameter("storageLocation");
        String threshold = request.getParameter("warningThreshold");

        ingredientService.createIngredient(categoryId, name, unit, quantity, price, location, threshold, userRole);
        session.setAttribute(Constants.SUCCESS_MESSAGE, Constants.SUCCESS_CREATE_INGREDIENT);
    }

    private void handleUpdateIngredient(HttpServletRequest request, HttpSession session, String userRole)
            throws ValidationException {
        String ingredientId = request.getParameter("ingredientId");
        String categoryId = request.getParameter("categoryId");
        String name = request.getParameter("ingredientName");
        String unit = request.getParameter("unit");
        String quantity = request.getParameter("quantity");
        String price = request.getParameter("price");
        String location = request.getParameter("storageLocation");
        String threshold = request.getParameter("warningThreshold");

        ingredientService.updateIngredient(ingredientId, categoryId, name, unit, quantity, price, location, threshold, userRole);
        session.setAttribute(Constants.SUCCESS_MESSAGE, Constants.SUCCESS_UPDATE_INGREDIENT);
    }

    private void handleDeleteIngredient(HttpServletRequest request, HttpSession session, String userRole)
            throws ValidationException {
        String ingredientId = request.getParameter("ingredientId");
        ingredientService.deleteIngredient(ingredientId, userRole);
        session.setAttribute(Constants.SUCCESS_MESSAGE, Constants.SUCCESS_DELETE_INGREDIENT);
    }
}
