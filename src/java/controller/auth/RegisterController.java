package controller.auth;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import service.UserService;
import utility.Constants;
import utility.FlashMessageUtil;
import utility.ValidationException;

public class RegisterController extends HttpServlet {

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
        FlashMessageUtil.forwardFlashMessages(request, session);

        request.getRequestDispatcher("register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        String username = request.getParameter("txtUsername");
        String fullName = request.getParameter("txtFullName");
        String pass = request.getParameter("txtPassword");
        String confirmPass = request.getParameter("txtConfirmPassword");
        String email = request.getParameter("txtEmail");
        String phone = request.getParameter("txtPhone");
        String address = request.getParameter("txtAddress");
        String securityId = request.getParameter("txtSecurityId");

        try {
            userService.register(username, fullName, pass, confirmPass, email, phone, address, securityId);

            request.getSession().setAttribute(Constants.SUCCESS_MESSAGE, Constants.SUCCESS_REGISTER);
            response.sendRedirect("RegisterController");

        } catch (ValidationException e) {
            request.setAttribute(Constants.ERROR_MESSAGE, e.getMessage());
            request.getRequestDispatcher("register.jsp").forward(request, response);
        }
    }

    @Override
    public String getServletInfo() {
        return "Register Controller for Bingxue ERP";
    }
}
