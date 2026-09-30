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
            Map<String, String> profile = userService.getUserProfile(loginUser.get("username"));
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
        String username = loginUser.get("username");

        String action = request.getParameter("action");
        request.setAttribute("ACTIVE_TAB", action);

        try {
            switch (action) {
                case "updateProfile":
                    String email = request.getParameter("txtEmail");
                    String phone = request.getParameter("txtPhone");
                    String address = request.getParameter("txtAddress");

                    userService.updateProfile(username, email, phone, address);
                    request.setAttribute(Constants.SUCCESS_MESSAGE, "Cập nhật thông tin thành công!");
                    break;

                case "changePassword":
                    String oldPass = request.getParameter("txtOldPassword");
                    String newPass = request.getParameter("txtNewPassword");
                    String confirmPass = request.getParameter("txtConfirmPassword");

                    userService.changePassword(username, oldPass, newPass, confirmPass);
                    request.setAttribute(Constants.SUCCESS_MESSAGE, "Đổi mật khẩu thành công!");
                    break;
                default:
                    throw new ValidationException("Hành động không hợp lệ!");
            }

            doGet(request, response);

        } catch (ValidationException e) {
            request.setAttribute(Constants.ERROR_MESSAGE, e.getMessage());
            doGet(request, response);
        }
    }
}
