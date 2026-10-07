/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller.admin;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.AdminService;
import utility.Constants;

/**
 *
 * @author AD
 */
public class ToggleUserController extends HttpServlet {

    private AdminService adminService;

    @Override
    public void init() throws ServletException {
        adminService = new AdminService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        try {
            int userId = Integer.parseInt(request.getParameter("userId"));
            adminService.toggleUserStatus(userId);

            request.getSession().setAttribute(Constants.SUCCESS_MESSAGE, Constants.SUCCESS_ACCOUNT_STATE_CHANGE);
        } catch (Exception e) {
            request.getSession().setAttribute(Constants.ERROR_MESSAGE, Constants.SERROR_INVALID_SETTING_TAB);
        }
        response.sendRedirect("AdminDashboardController");
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }
}
