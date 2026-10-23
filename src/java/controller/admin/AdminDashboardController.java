package controller.admin;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import service.AdminService;
import utility.Constants;
import utility.FlashMessageUtil;
import utility.ValidationException;

public class AdminDashboardController extends HttpServlet {

    private AdminService adminService;

    @Override
    public void init() throws ServletException {
        adminService = new AdminService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        FlashMessageUtil.forwardFlashMessages(request, session);

        Map<String, String> userSession = (Map<String, String>) session.getAttribute(Constants.USER_SESSION);
        if (userSession == null || !"Admin".equalsIgnoreCase(userSession.get("role"))) {
            response.sendRedirect("login.jsp");
            return;
        }

        try {
            List<Map<String, Object>> userList = adminService.getAllUsersForView();
            List<Map<String, Object>> keyList = adminService.getAllKeysForView();
            request.setAttribute("USER_LIST", userList);
            request.setAttribute("KEY_LIST", keyList);
        } catch (ValidationException e) {
            request.setAttribute(Constants.ERROR_MESSAGE, e.getMessage());
        }
        request.getRequestDispatcher("admin_dashboard.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        Map<String, String> userSession = (Map<String, String>) session.getAttribute(Constants.USER_SESSION);
        if (userSession == null || !"Admin".equalsIgnoreCase(userSession.get("role"))) {
            response.sendRedirect("login.jsp");
            return;
        }

        String action = request.getParameter("action");
        if (action == null) {
            action = "";
        }

        int adminUserId = Integer.parseInt(userSession.get("userId"));

        try {
            if ("resetPassword".equals(action)) {
                String targetUserId = request.getParameter("targetUserId");
                String newPassword = request.getParameter("newPassword");
                String confirmPassword = request.getParameter("confirmPassword");
                adminService.resetUserPasswordByAdmin(adminUserId, targetUserId, newPassword, confirmPassword);
                session.setAttribute(Constants.SUCCESS_MESSAGE, Constants.SUCCESS_ADMIN_RESET_PSW);
                response.sendRedirect("AdminDashboardController?tab=tab-passwords");
                return;
            } else if ("changeRole".equals(action)) {
                String targetUserId = request.getParameter("targetUserId");
                String newRole = request.getParameter("newRole");
                adminService.changeUserRoleByAdmin(adminUserId, targetUserId, newRole);
                session.setAttribute(Constants.SUCCESS_MESSAGE, Constants.SUCCESS_ADMIN_CHANGE_ROLE);
                response.sendRedirect("AdminDashboardController?tab=tab-users");
                return;
            } else if ("revokeKey".equals(action)) {
                String keyId = request.getParameter("keyId");
                adminService.revokeSecurityKey(keyId);
                session.setAttribute(Constants.SUCCESS_MESSAGE, Constants.SUCCESS_REVOKE_KEY);
                response.sendRedirect("AdminDashboardController?tab=tab-keys");
                return;
            } else if ("generateKey".equals(action)) {
                String role = request.getParameter("ddlRole");
                if (role == null || role.trim().isEmpty()) {
                    throw new ValidationException(Constants.ERROR_INVALID_ROLE_SELECTION);
                }
                adminService.generateSecurityKey(role.trim());
                session.setAttribute(Constants.SUCCESS_MESSAGE, Constants.SUCCESS_GEN_KEY);
                response.sendRedirect("AdminDashboardController?tab=tab-keys");
                return;
            } else if ("toggleUser".equals(action)) {
                String userIdStr = request.getParameter("userId");
                int targetUserId;
                try {
                    targetUserId = Integer.parseInt(userIdStr);
                } catch (Exception e) {
                    throw new ValidationException(Constants.ERROR_USER_NOT_FOUND);
                }
                if (adminUserId == targetUserId) {
                    throw new ValidationException(Constants.ERROR_ADMIN_CANNOT_CHANGE_SELF_ROLE);
                }
                adminService.toggleUserStatus(targetUserId);
                session.setAttribute(Constants.SUCCESS_MESSAGE, Constants.SUCCESS_ACCOUNT_STATE_CHANGE);
                response.sendRedirect("AdminDashboardController?tab=tab-users");
                return;
            } else {
                session.setAttribute(Constants.ERROR_MESSAGE, Constants.ERROR_INVALID_ACTION);
                response.sendRedirect("AdminDashboardController");
                return;
            }
        } catch (ValidationException e) {
            session.setAttribute(Constants.ERROR_MESSAGE, e.getMessage());
            if ("resetPassword".equals(action)) {
                response.sendRedirect("AdminDashboardController?tab=tab-passwords");
            } else if ("revokeKey".equals(action) || "generateKey".equals(action)) {
                response.sendRedirect("AdminDashboardController?tab=tab-keys");
            } else {
                response.sendRedirect("AdminDashboardController?tab=tab-users");
            }
        }
    }
}
