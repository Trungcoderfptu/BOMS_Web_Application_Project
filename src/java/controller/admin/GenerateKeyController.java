package controller.admin;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.AdminService;
import utility.Constants;
import utility.ValidationException;

public class GenerateKeyController extends HttpServlet {

    private AdminService adminService;

    @Override
    public void init() throws ServletException {
        adminService = new AdminService();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String role = request.getParameter("ddlRole");

        if (role != null && !role.trim().isEmpty()) {
            try {
                adminService.generateSecurityKey(role);
                request.getSession().setAttribute(Constants.SUCCESS_MESSAGE, Constants.SUCCESS_GEN_KEY);
            } catch (ValidationException e) {
                request.getSession().setAttribute(Constants.ERROR_MESSAGE, e.getMessage());
            }
        }

        response.sendRedirect("AdminDashboardController?tab=tab-keys");
    }
}
