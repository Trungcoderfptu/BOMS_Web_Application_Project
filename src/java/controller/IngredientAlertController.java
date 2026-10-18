package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
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
            response.getWriter().write("{\"success\":false,\"count\":0,\"alerts\":[]}");
            return;
        }

        try {
            List<Map<String, Object>> alerts = ingredientService.getActiveAlertsForView();
            String json = buildAlertsJson(alerts);
            response.getWriter().write(json);
        } catch (ValidationException e) {
            response.getWriter().write("{\"success\":false,\"count\":0,\"alerts\":[],\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        } catch (Exception e) {
            response.getWriter().write("{\"success\":false,\"count\":0,\"alerts\":[]}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(Constants.USER_SESSION) == null) {
            response.getWriter().write("{\"success\":false,\"error\":\"E0004\"}");
            return;
        }

        String action = request.getParameter("action");
        String alertId = request.getParameter("alertId");

        // Lấy UserID an toàn từ session (hỗ trợ cả String từ UserService.buildUserSessionMap và Number)
        int userId = 0;
        Object userObj = session.getAttribute(Constants.USER_SESSION);
        if (userObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> userMap = (Map<String, Object>) userObj;
            Object idVal = userMap.get("userId");
            if (idVal instanceof Number) {
                userId = ((Number) idVal).intValue();
            } else if (idVal != null) {
                try {
                    userId = Integer.parseInt(idVal.toString().trim());
                } catch (NumberFormatException ignored) {
                }
            }
        } else if (userObj != null) {
            try {
                java.lang.reflect.Method m = userObj.getClass().getMethod("getUserID");
                Object val = m.invoke(userObj);
                if (val instanceof Number) {
                    userId = ((Number) val).intValue();
                }
            } catch (Exception ignored) {
                try {
                    java.lang.reflect.Method m = userObj.getClass().getMethod("getUserId");
                    Object val = m.invoke(userObj);
                    if (val instanceof Number) {
                        userId = ((Number) val).intValue();
                    }
                } catch (Exception ignored2) {
                }
            }
        }

        try {
            if ("confirm".equalsIgnoreCase(action)) {
                ingredientService.confirmAlert(alertId, userId);
                response.getWriter().write("{\"success\":true,\"message\":\"" + Constants.SUCCESS_CONFIRM_ALERT + "\"}");
            } else if ("close".equalsIgnoreCase(action)) {
                ingredientService.closeAlert(alertId);
                response.getWriter().write("{\"success\":true,\"message\":\"" + Constants.SUCCESS_CLOSE_ALERT + "\"}");
            } else {
                response.getWriter().write("{\"success\":false,\"error\":\"E9000\"}");
            }
        } catch (ValidationException e) {
            response.getWriter().write("{\"success\":false,\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        } catch (Exception e) {
            response.getWriter().write("{\"success\":false,\"error\":\"DE0020\"}");
        }
    }

    // =========================================================================
    // JSON BUILDER HELPERS (SRP)
    // =========================================================================

    private String buildAlertsJson(List<Map<String, Object>> alerts) {
        StringBuilder sb = new StringBuilder();
        int count = alerts != null ? alerts.size() : 0;
        sb.append("{\"success\":true,\"count\":").append(count).append(",\"alerts\":[");

        if (alerts != null && !alerts.isEmpty()) {
            for (int i = 0; i < alerts.size(); i++) {
                if (i > 0) {
                    sb.append(",");
                }
                Map<String, Object> a = alerts.get(i);
                sb.append("{");
                sb.append("\"alertId\":").append(a.get("alertId")).append(",");
                sb.append("\"ingredientId\":").append(a.get("ingredientId")).append(",");
                sb.append("\"ingredientName\":\"").append(escapeJson(String.valueOf(a.get("ingredientName")))).append("\",");
                sb.append("\"unit\":\"").append(escapeJson(String.valueOf(a.get("unit")))).append("\",");
                sb.append("\"quantity\":").append(a.get("quantity")).append(",");
                sb.append("\"warningThreshold\":").append(a.get("warningThreshold")).append(",");
                sb.append("\"alertType\":\"").append(escapeJson(String.valueOf(a.get("alertType")))).append("\",");
                sb.append("\"status\":\"").append(escapeJson(String.valueOf(a.get("status")))).append("\",");
                sb.append("\"confirmedBy\":\"").append(escapeJson(String.valueOf(a.get("confirmedBy")))).append("\",");
                sb.append("\"createdAt\":\"").append(escapeJson(String.valueOf(a.get("createdAt")))).append("\",");
                sb.append("\"confirmedAt\":\"").append(escapeJson(String.valueOf(a.get("confirmedAt")))).append("\"");
                sb.append("}");
            }
        }
        sb.append("]}");
        return sb.toString();
    }

    private String escapeJson(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\b", "\\b")
                    .replace("\f", "\\f")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
    }
}
