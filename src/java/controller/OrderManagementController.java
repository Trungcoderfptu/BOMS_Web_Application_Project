/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import service.OrderService;
import utility.Constants;
import utility.FlashMessageUtil;
import utility.ValidationException;

/**
 * Controller điều phối chức năng Quản lý Đơn hàng
 * Tuyệt đối không import Model hoặc DAO (tuân thủ Rule_BOMS.md)
 *
 * @author AD
 */
public class OrderManagementController extends HttpServlet {

    private OrderService orderService;

    @Override
    public void init() throws ServletException {
        orderService = new OrderService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        HttpSession session = request.getSession();

        @SuppressWarnings("unchecked")
        Map<String, String> userSession = (Map<String, String>) session.getAttribute(Constants.USER_SESSION);
        if (userSession == null || !hasAccessPermission(userSession.get("role"))) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        FlashMessageUtil.forwardFlashMessages(request, session);

        String filterDate = request.getParameter("filterDate");
        if (filterDate == null || filterDate.trim().isEmpty()) {
            filterDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        }
        String filterStatus = request.getParameter("filterStatus");
        if (filterStatus == null || filterStatus.trim().isEmpty()) {
            filterStatus = "ALL";
        }

        try {
            List<Map<String, Object>> orderList = orderService.getOrdersForView(filterDate, filterStatus);
            List<Map<String, Object>> deliveryAreas = orderService.getAllDeliveryAreas();
            Map<String, Integer> orderCounts = orderService.calculateOrderCounts(orderList);
            request.setAttribute("ORDER_LIST", orderList);
            request.setAttribute("DELIVERY_AREAS", deliveryAreas);
            request.setAttribute("ORDER_COUNTS", orderCounts);
            request.setAttribute("CURRENT_FILTER_DATE", filterDate);
            request.setAttribute("CURRENT_FILTER_STATUS", filterStatus);
            String activeTab = request.getParameter("activeTab");
            if (activeTab != null && !activeTab.trim().isEmpty()) {
                request.setAttribute(Constants.ACTIVE_TAB, activeTab.trim());
            }
        } catch (ValidationException e) {
            request.setAttribute(Constants.ERROR_MESSAGE, e.getMessage());
        }

        request.getRequestDispatcher("order_management.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        HttpSession session = request.getSession();

        @SuppressWarnings("unchecked")
        Map<String, String> userSession = (Map<String, String>) session.getAttribute(Constants.USER_SESSION);
        if (userSession == null || !hasAccessPermission(userSession.get("role"))) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        String filterDate = request.getParameter("filterDate");
        String filterStatus = request.getParameter("filterStatus");
        String activeTab = request.getParameter("activeTab");

        try {
            String action = request.getParameter("action");
            if ("updateStatus".equals(action)) {
                int orderId = Integer.parseInt(request.getParameter("orderId"));
                String newStatus = request.getParameter("newStatus");
                orderService.updateOrderStatus(orderId, newStatus);
                session.setAttribute(Constants.SUCCESS_MESSAGE, Constants.SUCCESS_UPDATE_ORDER_STATUS);
            } else if ("cancel".equals(action)) {
                int orderId = Integer.parseInt(request.getParameter("orderId"));
                orderService.cancelOrder(orderId);
                session.setAttribute(Constants.SUCCESS_MESSAGE, Constants.SUCCESS_CANCEL_ORDER);
            } else if ("convertToDelivery".equals(action)) {
                int orderId = Integer.parseInt(request.getParameter("orderId"));
                String customerName = request.getParameter("customerName");
                String customerPhone = request.getParameter("customerPhone");
                String deliveryAddress = request.getParameter("deliveryAddress");
                int areaId = 0;
                try {
                    areaId = Integer.parseInt(request.getParameter("areaId"));
                } catch (NumberFormatException e) {
                    areaId = 0;
                }
                orderService.updateOrderToDelivery(orderId, customerName, customerPhone, deliveryAddress, areaId);
                session.setAttribute(Constants.SUCCESS_MESSAGE, Constants.SUCCESS_DISPATCH_ORDER);
                if (activeTab == null || activeTab.trim().isEmpty()) {
                    activeTab = "tab-delivery-orders";
                }
            }
        } catch (ValidationException e) {
            session.setAttribute(Constants.ERROR_MESSAGE, e.getMessage());
        } catch (Exception e) {
            session.setAttribute(Constants.ERROR_MESSAGE, Constants.ERROR_SYSTEM);
        }

        String redirectUrl = "OrderManagementController?filterDate=" + (filterDate != null ? filterDate : "")
                + "&filterStatus=" + (filterStatus != null ? filterStatus : "ALL")
                + (activeTab != null && !activeTab.trim().isEmpty() ? "&activeTab=" + activeTab.trim() : "");

        response.sendRedirect(request.getContextPath() + "/" + redirectUrl);
    }

    private boolean hasAccessPermission(String role) {
        if (role == null) {
            return false;
        }
        return Constants.ROLE_STAFF.equalsIgnoreCase(role) || Constants.ROLE_MANAGER.equalsIgnoreCase(role) || Constants.ROLE_ADMIN.equalsIgnoreCase(role);
    }

    @Override
    public String getServletInfo() {
        return "Order Management Controller";
    }
}
