package controller;

import java.io.IOException;
import java.util.Map;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import service.UserService;
import utility.Constants;
import utility.FlashMessageUtil;
import utility.ValidationException;

public class SettingsController extends HttpServlet {

    private UserService userService;

    @Override
    public void init() throws ServletException {
        userService = new UserService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        HttpSession session = request.getSession();

        @SuppressWarnings("unchecked")
        Map<String, String> loginUser = (Map<String, String>) session.getAttribute(Constants.USER_SESSION);
        if (loginUser == null || loginUser.get("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        FlashMessageUtil.forwardFlashMessages(request, session);

        try {
            int userId = Integer.parseInt(loginUser.get("userId"));
            Map<String, String> profile = userService.getUserProfile(userId);
            request.setAttribute(Constants.USER_PROFILE, profile);
            request.getRequestDispatcher("settings.jsp").forward(request, response);
        } catch (ValidationException e) {
            session.setAttribute(Constants.ERROR_MESSAGE, e.getMessage());
            response.sendRedirect(request.getContextPath() + "/index.jsp");
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/index.jsp");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        HttpSession session = request.getSession();
        @SuppressWarnings("unchecked")
        Map<String, String> loginUser = (Map<String, String>) session.getAttribute(Constants.USER_SESSION);
        if (loginUser == null || loginUser.get("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        String action = request.getParameter("action");
        if (action != null && !action.trim().isEmpty()) {
            session.setAttribute(Constants.ACTIVE_TAB, action.trim());
        }

        try {
            int userId = Integer.parseInt(loginUser.get("userId"));
            switch (action != null ? action : "") {
                case "updateProfile":
                    String email = request.getParameter("txtEmail");
                    String phone = request.getParameter("txtPhone");
                    String address = request.getParameter("txtAddress");

                    userService.updateProfile(userId, email, phone, address);
                    session.setAttribute(Constants.SUCCESS_MESSAGE, Constants.SUCCESS_UPDATE_PROFILE);
                    break;

                case "changePassword":
                    String oldPass = request.getParameter("txtOldPassword");
                    String newPass = request.getParameter("txtNewPassword");
                    String confirmPass = request.getParameter("txtConfirmPassword");

                    userService.changePassword(userId, oldPass, newPass, confirmPass);
                    session.setAttribute(Constants.SUCCESS_MESSAGE, Constants.SUCCESS_PSW_CHANGE);
                    break;

                default:
                    throw new ValidationException(Constants.SERROR_INVALID_SETTING_TAB);
            }
        } catch (ValidationException e) {
            session.setAttribute(Constants.ERROR_MESSAGE, e.getMessage());
        } catch (Exception e) {
            session.setAttribute(Constants.ERROR_MESSAGE, Constants.ERROR_SYSTEM);
        }

        response.sendRedirect(request.getContextPath() + "/SettingsController");
    }
}
