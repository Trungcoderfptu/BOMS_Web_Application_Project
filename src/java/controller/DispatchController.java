package controller;

import java.io.IOException;
import java.util.Map;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import service.DeliveryService;
import utility.Constants;
import utility.ValidationException;

@WebServlet(name = "DispatchController", urlPatterns = {"/DispatchController"})
public class DispatchController extends HttpServlet {

    private DeliveryService deliveryService;

    @Override
    public void init() throws ServletException {
        deliveryService = new DeliveryService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!validateSessionAndRole(request, response)) {
            return; 
        }

        HttpSession session = request.getSession(false);
        forwardFlashMessages(request, session);

        loadDashboardData(request);
        request.getRequestDispatcher("dispatch_dashboard.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        if (!validateSessionAndRole(request, response)) {
            return;
        }

        processAssignShipper(request);
        response.sendRedirect(request.getContextPath() + "/DispatchController");
    }

    private boolean validateSessionAndRole(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(Constants.USER_SESSION) == null) {
            response.sendRedirect("login.jsp");
            return false;
        }

        Map<String, String> loginUser = (Map<String, String>) session.getAttribute(Constants.USER_SESSION);
        String currentRole = loginUser.get("role");

        if ("Shipper".equalsIgnoreCase(currentRole)) {
            response.sendRedirect("ShipperController");
            return false;
        } 
        else if (!"Manager".equalsIgnoreCase(currentRole) && !"Staff".equalsIgnoreCase(currentRole)) {
            request.setAttribute(Constants.ERROR_MESSAGE, "error.unauthorized");
            request.getRequestDispatcher("delivery_unauthorized.jsp").forward(request, response);
            return false;
        }
        
        return true;
    }

    private void loadDashboardData(HttpServletRequest request) {
        try {
            request.setAttribute("SHIPPER_LIST", deliveryService.getAvailableShippers());
            request.setAttribute("UNASSIGNED_TASKS", deliveryService.getUnassignedTasks());
        } catch (ValidationException e) {
            request.setAttribute(Constants.ERROR_MESSAGE, e.getMessage());
        }
    }

    private void processAssignShipper(HttpServletRequest request) {
        String shipperUsername = request.getParameter("shipperUsername");
        String taskIdStr = request.getParameter("taskId");
        HttpSession session = request.getSession();

        try {
            deliveryService.assignTaskToShipper(taskIdStr, shipperUsername);
            session.setAttribute(Constants.SUCCESS_MESSAGE, "dispatch.msg.assign_success");
        } catch (ValidationException e) {
            session.setAttribute(Constants.ERROR_MESSAGE, e.getMessage());
        }
    }

    private void forwardFlashMessages(HttpServletRequest request, HttpSession session) {
        if (session == null) {
            return;
        }
        if (session.getAttribute(Constants.SUCCESS_MESSAGE) != null) {
            request.setAttribute(Constants.SUCCESS_MESSAGE, session.getAttribute(Constants.SUCCESS_MESSAGE));
            session.removeAttribute(Constants.SUCCESS_MESSAGE);
        }
        if (session.getAttribute(Constants.ERROR_MESSAGE) != null) {
            request.setAttribute(Constants.ERROR_MESSAGE, session.getAttribute(Constants.ERROR_MESSAGE));
            session.removeAttribute(Constants.ERROR_MESSAGE);
        }
    }
}