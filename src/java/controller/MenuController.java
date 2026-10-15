/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import service.OrderService;
import service.ProductService;
import utility.Constants;
import utility.ValidationException;

/**
 *
 * @author AD
 */
public class MenuController extends HttpServlet {

    private ProductService productService;
    private OrderService orderService;

    @Override
    public void init() throws ServletException {
        productService = new ProductService();
        orderService = new OrderService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        jakarta.servlet.http.HttpSession session = request.getSession();
        forwardFlashMessages(session, request);
        resolveActiveTab(request, session);
        handleTableSession(request, session);
        loadTableOrders(request, session);

        try {
            List<Map<String, Object>> menuList = productService.getAllProductsForView();
            request.setAttribute("MENU_LIST", menuList);
        } catch (ValidationException e) {
            request.setAttribute(Constants.ERROR_MESSAGE, e.getMessage());
        }
        request.getRequestDispatcher("menu.jsp").forward(request, response);
    }

    private void loadTableOrders(HttpServletRequest request, jakarta.servlet.http.HttpSession session) {
        String currentTable = (String) request.getAttribute("CURRENT_TABLE");
        if (currentTable == null || currentTable.trim().isEmpty()) {
            currentTable = (String) session.getAttribute("CURRENT_TABLE");
        }
        if (currentTable != null && !currentTable.trim().isEmpty()) {
            try {
                @SuppressWarnings("unchecked")
                List<Integer> placedOrderIds = (List<Integer>) session.getAttribute("PLACED_ORDER_IDS");
                List<Map<String, Object>> tableOrders = orderService.getOrdersForTableView(currentTable, placedOrderIds);
                request.setAttribute("TABLE_ORDERS", tableOrders);
            } catch (ValidationException e) {
                // Ignore non-fatal error
            }
        }
    }

    private void forwardFlashMessages(jakarta.servlet.http.HttpSession session, HttpServletRequest request) {
        if (session.getAttribute(Constants.SUCCESS_MESSAGE) != null) {
            request.setAttribute(Constants.SUCCESS_MESSAGE, session.getAttribute(Constants.SUCCESS_MESSAGE));
            session.removeAttribute(Constants.SUCCESS_MESSAGE);
        }
        if (session.getAttribute(Constants.ERROR_MESSAGE) != null) {
            request.setAttribute(Constants.ERROR_MESSAGE, session.getAttribute(Constants.ERROR_MESSAGE));
            session.removeAttribute(Constants.ERROR_MESSAGE);
        }
        if (session.getAttribute("LAST_ORDER_ID") != null) {
            request.setAttribute("LAST_ORDER_ID", session.getAttribute("LAST_ORDER_ID"));
            session.removeAttribute("LAST_ORDER_ID");
        }
    }

    private void resolveActiveTab(HttpServletRequest request, jakarta.servlet.http.HttpSession session) {
        if (session.getAttribute("ACTIVE_TAB") != null) {
            request.setAttribute("ACTIVE_TAB", session.getAttribute("ACTIVE_TAB"));
            session.removeAttribute("ACTIVE_TAB");
        }
        String activeTab = request.getParameter("activeTab");
        if (activeTab != null && !activeTab.trim().isEmpty()) {
            request.setAttribute("ACTIVE_TAB", activeTab.trim());
        }
    }

    private void handleTableSession(HttpServletRequest request, jakarta.servlet.http.HttpSession session) {
        String paramTableId = request.getParameter("tableId");
        boolean isStaffLoggedIn = session.getAttribute(Constants.USER_SESSION) != null;

        if (paramTableId != null && !paramTableId.trim().isEmpty()) {
            String newTableId = paramTableId.trim();
            String currentTable = (String) session.getAttribute("CURRENT_TABLE");
            if (currentTable != null && !currentTable.equals(newTableId)) {
                // Quét sang bàn mới khác bàn đang lưu trong Session -> Tự động làm rỗng giỏ hàng và danh sách đơn của bàn cũ
                session.removeAttribute("CURRENT_CART");
                session.removeAttribute("PLACED_ORDER_IDS");
            }
            session.setAttribute("CURRENT_TABLE", newTableId);
            request.setAttribute("CURRENT_TABLE", newTableId);
        } else {
            if (isStaffLoggedIn) {
                // Nhân viên vào Menu quầy thu ngân (không truyền tableId) -> Đảm bảo không dính bàn khách cũ
                session.removeAttribute("CURRENT_TABLE");
                session.removeAttribute("PLACED_ORDER_IDS");
            } else {
                String sessionTableId = (String) session.getAttribute("CURRENT_TABLE");
                if (sessionTableId != null && !sessionTableId.trim().isEmpty()) {
                    request.setAttribute("CURRENT_TABLE", sessionTableId);
                }
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }
}
