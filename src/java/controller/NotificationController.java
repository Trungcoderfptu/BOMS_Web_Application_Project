package controller;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import service.NotificationService;
import utility.Constants;
import utility.ValidationException;

/**
 * Controller điều phối Hệ thống Thông báo & Phát thanh toàn cục (Task 11)
 * Tuân thủ Rule_BOMS: Không import Model/DAO, PRG chuẩn cho form submit, JSON cho AJAX.
 */
@WebServlet(name = "NotificationController", urlPatterns = {"/NotificationController"})
public class NotificationController extends HttpServlet {

    private NotificationService notificationService;
    private Gson gson;

    @Override
    public void init() throws ServletException {
        notificationService = new NotificationService();
        gson = new Gson();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        HttpSession session = request.getSession();

        @SuppressWarnings("unchecked")
        Map<String, String> userSession = (Map<String, String>) session.getAttribute(Constants.USER_SESSION);
        if (userSession == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("error", Constants.ERROR_UNAUTHORIZED);
            response.getWriter().write(gson.toJson(err));
            return;
        }

        int userId = Integer.parseInt(userSession.get("userId"));
        String action = request.getParameter("action");
        if (action == null) {
            action = "getUnread";
        }

        Map<String, Object> jsonResponse = new HashMap<>();
        try {
            if ("getUnread".equals(action)) {
                int unreadCount = notificationService.getUnreadCount(userId);
                List<Map<String, Object>> list = notificationService.getUnreadNotificationsForView(userId);
                jsonResponse.put("success", true);
                jsonResponse.put("unreadCount", unreadCount);
                jsonResponse.put("notifications", list);
            } else if ("getRecent".equals(action)) {
                int limit = 10;
                String limitParam = request.getParameter("limit");
                if (limitParam != null && !limitParam.trim().isEmpty()) {
                    try {
                        limit = Integer.parseInt(limitParam.trim());
                    } catch (NumberFormatException ignored) {
                    }
                }
                int unreadCount = notificationService.getUnreadCount(userId);
                List<Map<String, Object>> list = notificationService.getRecentNotificationsForView(userId, limit);
                jsonResponse.put("success", true);
                jsonResponse.put("unreadCount", unreadCount);
                jsonResponse.put("notifications", list);
            } else {
                jsonResponse.put("success", false);
                jsonResponse.put("error", Constants.ERROR_INVALID_ACTION);
            }
        } catch (ValidationException e) {
            jsonResponse.put("success", false);
            jsonResponse.put("error", e.getMessage());
        } catch (Exception e) {
            jsonResponse.put("success", false);
            jsonResponse.put("error", Constants.ERROR_SYSTEM);
        }

        response.getWriter().write(gson.toJson(jsonResponse));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession();

        @SuppressWarnings("unchecked")
        Map<String, String> userSession = (Map<String, String>) session.getAttribute(Constants.USER_SESSION);
        if (userSession == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        int userId = Integer.parseInt(userSession.get("userId"));
        String userRole = userSession.get("role");
        String action = request.getParameter("action");

        // Action broadcast phát thanh toàn cục (form POST từ Modal)
        if ("broadcast".equals(action)) {
            if (!"Admin".equalsIgnoreCase(userRole) && !"Manager".equalsIgnoreCase(userRole)) {
                session.setAttribute(Constants.ERROR_MESSAGE, Constants.ERROR_UNAUTHORIZED);
                redirectToReferer(request, response);
                return;
            }

            String broadcastMessage = request.getParameter("broadcastMessage");
            try {
                int sentCount = notificationService.broadcastMessage(broadcastMessage, userId);
                session.setAttribute(Constants.SUCCESS_MESSAGE, Constants.SUCCESS_BROADCAST_MESSAGE);
            } catch (ValidationException e) {
                session.setAttribute(Constants.ERROR_MESSAGE, e.getMessage());
            } catch (Exception e) {
                session.setAttribute(Constants.ERROR_MESSAGE, Constants.ERROR_SYSTEM);
            }
            redirectToReferer(request, response);
            return;
        }

        // Các action AJAX (markAsRead, markAllAsRead)
        response.setContentType("application/json;charset=UTF-8");
        Map<String, Object> jsonResponse = new HashMap<>();

        try {
            if ("markAsRead".equals(action)) {
                int notificationId = 0;
                String notifIdStr = request.getParameter("notificationId");
                if (notifIdStr != null && !notifIdStr.trim().isEmpty()) {
                    try {
                        notificationId = Integer.parseInt(notifIdStr.trim());
                    } catch (NumberFormatException ignored) {
                    }
                }
                boolean success = notificationService.markAsRead(notificationId, userId);
                jsonResponse.put("success", success);
                jsonResponse.put("unreadCount", notificationService.getUnreadCount(userId));
            } else if ("markAllAsRead".equals(action)) {
                boolean success = notificationService.markAllAsRead(userId);
                jsonResponse.put("success", success);
                jsonResponse.put("unreadCount", 0);
            } else {
                jsonResponse.put("success", false);
                jsonResponse.put("error", Constants.ERROR_INVALID_ACTION);
            }
        } catch (ValidationException e) {
            jsonResponse.put("success", false);
            jsonResponse.put("error", e.getMessage());
        } catch (Exception e) {
            jsonResponse.put("success", false);
            jsonResponse.put("error", Constants.ERROR_SYSTEM);
        }

        response.getWriter().write(gson.toJson(jsonResponse));
    }

    private void redirectToReferer(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.trim().isEmpty()) {
            response.sendRedirect(referer);
        } else {
            response.sendRedirect(request.getContextPath() + "/index.jsp");
        }
    }

    @Override
    public String getServletInfo() {
        return "Notification and Global Broadcast Controller for BOMS";
    }
}
