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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import service.CartService;
import utility.Constants;
import utility.ValidationException;

/**
 * Điều phối các thao tác giỏ hàng và chốt đơn tại quầy / tại bàn.
 * Tuân thủ Rule_BOMS.md: không hardcode Session keys.
 * 
 * @author AD
 */
public class CartController extends HttpServlet {

    private CartService cartService;
    private service.OrderService orderService;

    @Override
    public void init() throws ServletException {
        cartService = new CartService();
        orderService = new service.OrderService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        response.sendRedirect(request.getContextPath() + "/MenuController");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        HttpSession session = request.getSession();

        String tableId = resolveTableId(request, session);
        String url = buildRedirectUrl(tableId);

        try {
            String action = request.getParameter("action");
            if ("add".equals(action)) {
                handleAddAction(request, session);
            } else if ("update".equals(action)) {
                handleUpdateAction(request, session);
            } else if ("remove".equals(action)) {
                handleRemoveAction(request, session);
            } else if ("clear".equals(action)) {
                handleClearAction(session);
            } else if ("checkout".equals(action)) {
                handleCheckoutAction(session, tableId);
            } else if ("cancelTableOrder".equals(action)) {
                handleCancelTableOrderAction(request, session);
            }
        } catch (ValidationException e) {
            session.setAttribute(Constants.ACTIVE_TAB, "cart");
            session.setAttribute(Constants.ERROR_MESSAGE, e.getMessage());
        } catch (Exception e) {
            session.setAttribute(Constants.ACTIVE_TAB, "cart");
            session.setAttribute(Constants.ERROR_MESSAGE, Constants.ERROR_SYSTEM);
        }

        response.sendRedirect(request.getContextPath() + "/" + url);
    }

    private String resolveTableId(HttpServletRequest request, HttpSession session) {
        String tableId = request.getParameter("tableId");
        if (tableId != null && !tableId.trim().isEmpty()) {
            return tableId.trim();
        }
        return (String) session.getAttribute(Constants.CURRENT_TABLE);
    }

    private String buildRedirectUrl(String tableId) {
        if (tableId != null && !tableId.trim().isEmpty()) {
            return "MenuController?tableId=" + tableId;
        }
        return "MenuController";
    }

    private void handleAddAction(HttpServletRequest request, HttpSession session) throws ValidationException {
        int productId = Integer.parseInt(request.getParameter("productId"));
        Object currentCart = session.getAttribute(Constants.CURRENT_CART);
        currentCart = cartService.addProductToCart(currentCart, productId,
                request.getParameter("quantity"),
                request.getParameter("sizeId"),
                request.getParameter("iceLevel"),
                request.getParameter("sugarLevel"),
                request.getParameterValues("toppingId"),
                request.getParameter("itemNote"));
        session.setAttribute(Constants.CURRENT_CART, currentCart);
        session.setAttribute(Constants.SUCCESS_MESSAGE, Constants.SUCCESS_ADD_TO_CART);
        // Nút "Thanh toán ngay" trong popup món: thêm vào giỏ rồi mở luôn hộp xác nhận đặt món
        if ("true".equals(request.getParameter("buyNow"))) {
            session.setAttribute(Constants.ACTIVE_TAB, "cart");
            session.setAttribute(Constants.OPEN_CHECKOUT, Boolean.TRUE);
        }
    }

    private void handleUpdateAction(HttpServletRequest request, HttpSession session) throws ValidationException {
        String lineKey = request.getParameter("lineKey");
        int quantity = Integer.parseInt(request.getParameter("quantity"));
        Object currentCart = session.getAttribute(Constants.CURRENT_CART);
        currentCart = cartService.updateItemQuantity(currentCart, lineKey, quantity);
        session.setAttribute(Constants.CURRENT_CART, currentCart);
        session.setAttribute(Constants.ACTIVE_TAB, "cart");
        session.setAttribute(Constants.SUCCESS_MESSAGE, Constants.SUCCESS_UPDATE_CART);
    }

    private void handleRemoveAction(HttpServletRequest request, HttpSession session) throws ValidationException {
        String lineKey = request.getParameter("lineKey");
        Object currentCart = session.getAttribute(Constants.CURRENT_CART);
        currentCart = cartService.removeItemFromCart(currentCart, lineKey);
        session.setAttribute(Constants.CURRENT_CART, currentCart);
        session.setAttribute(Constants.ACTIVE_TAB, "cart");
        session.setAttribute(Constants.SUCCESS_MESSAGE, Constants.SUCCESS_REMOVE_FROM_CART);
    }

    private void handleClearAction(HttpSession session) throws ValidationException {
        Object currentCart = session.getAttribute(Constants.CURRENT_CART);
        cartService.clearCart(currentCart);
        session.removeAttribute(Constants.CURRENT_CART);
        session.setAttribute(Constants.ACTIVE_TAB, "cart");
        session.setAttribute(Constants.SUCCESS_MESSAGE, Constants.SUCCESS_CLEAR_CART);
    }

    private void handleCheckoutAction(HttpSession session, String tableId) throws ValidationException {
        Object currentCart = session.getAttribute(Constants.CURRENT_CART);
        @SuppressWarnings("unchecked")
        Map<String, String> userSession = (Map<String, String>) session.getAttribute(Constants.USER_SESSION);
        int orderId = cartService.checkout(currentCart, tableId, userSession);
        session.removeAttribute(Constants.CURRENT_CART);
        session.setAttribute(Constants.LAST_ORDER_ID, orderId);
        session.setAttribute(Constants.SUCCESS_MESSAGE, Constants.SUCCESS_CHECKOUT);
        if (tableId != null && !tableId.trim().isEmpty()) {
            recordPlacedOrderForTableSession(session, orderId);
            session.setAttribute(Constants.ACTIVE_TAB, "orders");
        }
    }

    @SuppressWarnings("unchecked")
    private void recordPlacedOrderForTableSession(HttpSession session, int orderId) {
        List<Integer> placedOrderIds = (List<Integer>) session.getAttribute(Constants.PLACED_ORDER_IDS);
        if (placedOrderIds == null) {
            placedOrderIds = new ArrayList<>();
        }
        if (!placedOrderIds.contains(orderId)) {
            placedOrderIds.add(orderId);
        }
        session.setAttribute(Constants.PLACED_ORDER_IDS, placedOrderIds);
    }

    private void handleCancelTableOrderAction(HttpServletRequest request, HttpSession session) throws ValidationException {
        int orderId = Integer.parseInt(request.getParameter("orderId"));
        orderService.cancelOrder(orderId);
        session.setAttribute(Constants.SUCCESS_MESSAGE, Constants.SUCCESS_CANCEL_ORDER);
        session.setAttribute(Constants.ACTIVE_TAB, "orders");
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }
}
