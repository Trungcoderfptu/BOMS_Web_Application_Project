package controller.admin;

import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import service.AdminService;
import utility.Constants;
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
}
