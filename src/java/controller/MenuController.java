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
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import service.MenuOptionService;
import service.OrderService;
import service.ProductService;
import utility.Constants;
import utility.FlashMessageUtil;
import utility.ValidationException;

/**
 * Điều phối hiển thị thực đơn POS và đặt món tại bàn QR.
 * Tuân thủ Rule_BOMS.md.
 * 
 * @author AD
 */
public class MenuController extends HttpServlet {

    private ProductService productService;
    private OrderService orderService;
    private MenuOptionService menuOptionService;

    @Override
    public void init() throws ServletException {
        productService = new ProductService();
        orderService = new OrderService();
        menuOptionService = new MenuOptionService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        HttpSession session = request.getSession();
        FlashMessageUtil.forwardFlashMessages(request, session);
        if (session.getAttribute(Constants.LAST_ORDER_ID) != null) {
            request.setAttribute(Constants.LAST_ORDER_ID, session.getAttribute(Constants.LAST_ORDER_ID));
            session.removeAttribute(Constants.LAST_ORDER_ID);
        }
        // Cờ "Thanh toán ngay" (flash 1 lần): View tự mở hộp xác nhận đặt món
        if (session.getAttribute(Constants.OPEN_CHECKOUT) != null) {
            request.setAttribute(Constants.OPEN_CHECKOUT, Boolean.TRUE);
            session.removeAttribute(Constants.OPEN_CHECKOUT);
        }
        resolveActiveTab(request, session);
        handleTableSession(request, session);
        loadTableOrders(request, session);

        try {
            List<Map<String, Object>> menuList = productService.getAllProductsForView();
            request.setAttribute("MENU_LIST", menuList);
            request.setAttribute("MENU_CATEGORIES", productService.getAllCategoriesForView());
            request.setAttribute("BEST_SELLER_IDS", productService.getBestSellerIdsForView());
            request.setAttribute("MENU_SIZES", menuOptionService.getSizesForView(true));
            request.setAttribute("MENU_TOPPINGS", menuOptionService.getToppingsForView(true));
            request.setAttribute("ICE_LEVELS", Constants.ICE_LEVELS);
            request.setAttribute("SUGAR_LEVELS", Constants.SUGAR_LEVELS);
        } catch (ValidationException e) {
            request.setAttribute(Constants.ERROR_MESSAGE, e.getMessage());
        }
        request.getRequestDispatcher("menu.jsp").forward(request, response);
    }

    private void loadTableOrders(HttpServletRequest request, HttpSession session) {
        String currentTable = (String) request.getAttribute(Constants.CURRENT_TABLE);
        if (currentTable == null || currentTable.trim().isEmpty()) {
            currentTable = (String) session.getAttribute(Constants.CURRENT_TABLE);
        }
        if (currentTable != null && !currentTable.trim().isEmpty()) {
            try {
                @SuppressWarnings("unchecked")
                List<Integer> placedOrderIds = (List<Integer>) session.getAttribute(Constants.PLACED_ORDER_IDS);
                List<Map<String, Object>> tableOrders = orderService.getOrdersForTableView(currentTable, placedOrderIds);
                request.setAttribute("TABLE_ORDERS", tableOrders);
            } catch (ValidationException e) {
                // Ignore non-fatal error
            }
        }
    }

    private void resolveActiveTab(HttpServletRequest request, HttpSession session) {
        if (session.getAttribute(Constants.ACTIVE_TAB) != null) {
            request.setAttribute(Constants.ACTIVE_TAB, session.getAttribute(Constants.ACTIVE_TAB));
            session.removeAttribute(Constants.ACTIVE_TAB);
        }
        String activeTab = request.getParameter("activeTab");
        if (activeTab != null && !activeTab.trim().isEmpty()) {
            request.setAttribute(Constants.ACTIVE_TAB, activeTab.trim());
        }
    }

    private void handleTableSession(HttpServletRequest request, HttpSession session) {
        String paramTableId = request.getParameter("tableId");
        boolean isStaffLoggedIn = session.getAttribute(Constants.USER_SESSION) != null;

        if (paramTableId != null && !paramTableId.trim().isEmpty()) {
            String newTableId = paramTableId.trim();
            String currentTable = (String) session.getAttribute(Constants.CURRENT_TABLE);
            if (currentTable != null && !currentTable.equals(newTableId)) {
                // Quét sang bàn mới khác bàn đang lưu trong Session -> Tự động làm rỗng giỏ hàng và danh sách đơn của bàn cũ
                session.removeAttribute(Constants.CURRENT_CART);
                session.removeAttribute(Constants.PLACED_ORDER_IDS);
            }
            session.setAttribute(Constants.CURRENT_TABLE, newTableId);
            request.setAttribute(Constants.CURRENT_TABLE, newTableId);
        } else {
            if (isStaffLoggedIn) {
                // Nhân viên vào Menu quầy thu ngân (không truyền tableId) -> Đảm bảo không dính bàn khách cũ
                session.removeAttribute(Constants.CURRENT_TABLE);
                session.removeAttribute(Constants.PLACED_ORDER_IDS);
            } else {
                String sessionTableId = (String) session.getAttribute(Constants.CURRENT_TABLE);
                if (sessionTableId != null && !sessionTableId.trim().isEmpty()) {
                    request.setAttribute(Constants.CURRENT_TABLE, sessionTableId);
                }
            }
        }
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }
}
