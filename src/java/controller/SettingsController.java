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
        try {
            HttpSession session = request.getSession();
            Map<String, String> loginUser = (Map<String, String>) session.getAttribute(Constants.USER_SESSION);
            int userId = Integer.parseInt(loginUser.get("userId"));
            Map<String, String> profile = userService.getUserProfile(userId);
            request.setAttribute(Constants.USER_PROFILE, profile);
            request.getRequestDispatcher("settings.jsp").forward(request, response);
        } catch (ValidationException e) {
            response.sendRedirect("index.jsp");
        }

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        HttpSession session = request.getSession();
        Map<String, String> loginUser = (Map<String, String>) session.getAttribute(Constants.USER_SESSION);

        String action = request.getParameter("action");
        request.setAttribute(Constants.ACTIVE_TAB, action);

        try {
            int userId = Integer.parseInt(loginUser.get("userId"));
            switch (action) {
                case "updateProfile":
                    String email = request.getParameter("txtEmail");
                    String phone = request.getParameter("txtPhone");
                    String address = request.getParameter("txtAddress");

                    userService.updateProfile(userId, email, phone, address);
                    request.setAttribute(Constants.SUCCESS_MESSAGE, Constants.SUCCESS_UPDATE_PROFILE);
                    break;

                case "changePassword":
                    String oldPass = request.getParameter("txtOldPassword");
                    String newPass = request.getParameter("txtNewPassword");
                    String confirmPass = request.getParameter("txtConfirmPassword");

                    userService.changePassword(userId, oldPass, newPass, confirmPass);
                    request.setAttribute(Constants.SUCCESS_MESSAGE, Constants.SUCCESS_PSW_CHANGE);
                    break;
                default:
                    throw new ValidationException(Constants.SERROR_INVALID_SETTING_TAB);
            }

            doGet(request, response);

        } catch (ValidationException e) {
            request.setAttribute(Constants.ERROR_MESSAGE, e.getMessage());
            doGet(request, response);
        }
    }
}
