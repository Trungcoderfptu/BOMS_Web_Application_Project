package controller;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import service.IngredientService;
import utility.Constants;
import utility.ValidationException;

/**
 * Servlet điều phối Cảnh báo Tồn kho & Hết hạn (FE-04)
 * Cung cấp AJAX Polling Endpoint và xử lý xác nhận cảnh báo
 */
@WebServlet(name = "IngredientAlertController", urlPatterns = {"/IngredientAlertController"})
public class IngredientAlertController extends HttpServlet {

    private final IngredientService ingredientService;
    private final Gson gson = new Gson();

    public IngredientAlertController() {
        this.ingredientService = new IngredientService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(Constants.USER_SESSION) == null) {
            Map<String, Object> res = new LinkedHashMap<>();
            res.put("success", false);
            res.put("count", 0);
            res.put("alerts", Collections.emptyList());
            response.getWriter().write(gson.toJson(res));
            return;
        }

        try {
            List<Map<String, Object>> alerts = ingredientService.getActiveAlertsForView();
            Map<String, Object> res = new LinkedHashMap<>();
            res.put("success", true);
            res.put("count", alerts != null ? alerts.size() : 0);
            res.put("alerts", alerts != null ? alerts : Collections.emptyList());
            response.getWriter().write(gson.toJson(res));
        } catch (ValidationException e) {
            Map<String, Object> res = new LinkedHashMap<>();
            res.put("success", false);
            res.put("count", 0);
            res.put("alerts", Collections.emptyList());
            res.put("error", e.getMessage());
            response.getWriter().write(gson.toJson(res));
        } catch (Exception e) {
            Map<String, Object> res = new LinkedHashMap<>();
            res.put("success", false);
            res.put("count", 0);
            res.put("alerts", Collections.emptyList());
            res.put("error", Constants.ERROR_DATABASE_UPDATE_ALERT);
            response.getWriter().write(gson.toJson(res));
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(Constants.USER_SESSION) == null) {
            Map<String, Object> res = new LinkedHashMap<>();
            res.put("success", false);
            res.put("error", Constants.ERROR_LOGIN_FAIL_ROLE);
            response.getWriter().write(gson.toJson(res));
            return;
        }

        String action = request.getParameter("action");
        String alertId = request.getParameter("alertId");

        @SuppressWarnings("unchecked")
        Map<String, String> userSession = (Map<String, String>) session.getAttribute(Constants.USER_SESSION);

        try {
            int userId = Integer.parseInt(userSession.get("userId"));
            if ("confirm".equalsIgnoreCase(action)) {
                ingredientService.confirmAlert(alertId, userId);
                Map<String, Object> res = new LinkedHashMap<>();
                res.put("success", true);
                res.put("message", Constants.SUCCESS_CONFIRM_ALERT);
                response.getWriter().write(gson.toJson(res));
            } else if ("close".equalsIgnoreCase(action)) {
                ingredientService.closeAlert(alertId);
                Map<String, Object> res = new LinkedHashMap<>();
                res.put("success", true);
                res.put("message", Constants.SUCCESS_CLOSE_ALERT);
                response.getWriter().write(gson.toJson(res));
            } else {
                Map<String, Object> res = new LinkedHashMap<>();
                res.put("success", false);
                res.put("error", Constants.ERROR_INVALID_ACTION);
                response.getWriter().write(gson.toJson(res));
            }
        } catch (ValidationException e) {
            Map<String, Object> res = new LinkedHashMap<>();
            res.put("success", false);
            res.put("error", e.getMessage());
            response.getWriter().write(gson.toJson(res));
        } catch (Exception e) {
            Map<String, Object> res = new LinkedHashMap<>();
            res.put("success", false);
            res.put("error", Constants.ERROR_DATABASE_UPDATE_ALERT);
            response.getWriter().write(gson.toJson(res));
        }
    }
}
