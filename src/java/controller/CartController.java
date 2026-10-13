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
import java.util.Map;
import service.CartService;
import utility.Constants;
import utility.ValidationException;

/**
 *
 * @author AD
 */
public class CartController extends HttpServlet {

    private CartService cartService;

    @Override
    public void init() throws ServletException {
        cartService = new CartService();
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
        String url = "MenuController";
        HttpSession session = request.getSession();
        try {
            String action = request.getParameter("action");
            String tableId = request.getParameter("tableId");
            if (tableId != null && !tableId.trim().isEmpty()) {
                url = "MenuController?tableId=" + tableId;
            }

            if ("add".equals(action)) {
                int productId = Integer.parseInt(request.getParameter("productId"));
                Object currentCart = session.getAttribute("CURRENT_CART");
                currentCart = cartService.addProductToCart(currentCart, productId);
                session.setAttribute("CURRENT_CART", currentCart);
                session.setAttribute(Constants.SUCCESS_MESSAGE, Constants.SUCCESS_ADD_TO_CART);
            } else if ("update".equals(action)) {
                int productId = Integer.parseInt(request.getParameter("productId"));
                int quantity = Integer.parseInt(request.getParameter("quantity"));
                Object currentCart = session.getAttribute("CURRENT_CART");
                currentCart = cartService.updateItemQuantity(currentCart, productId, quantity);
                session.setAttribute("CURRENT_CART", currentCart);
                session.setAttribute("ACTIVE_TAB", "cart");
                session.setAttribute(Constants.SUCCESS_MESSAGE, Constants.SUCCESS_UPDATE_CART);
            } else if ("remove".equals(action)) {
                int productId = Integer.parseInt(request.getParameter("productId"));
                Object currentCart = session.getAttribute("CURRENT_CART");
                currentCart = cartService.removeItemFromCart(currentCart, productId);
                session.setAttribute("CURRENT_CART", currentCart);
                session.setAttribute("ACTIVE_TAB", "cart");
                session.setAttribute(Constants.SUCCESS_MESSAGE, Constants.SUCCESS_REMOVE_FROM_CART);
            } else if ("clear".equals(action)) {
                Object currentCart = session.getAttribute("CURRENT_CART");
                cartService.clearCart(currentCart);
                session.removeAttribute("CURRENT_CART");
                session.setAttribute("ACTIVE_TAB", "cart");
                session.setAttribute(Constants.SUCCESS_MESSAGE, Constants.SUCCESS_CLEAR_CART);
            } else if ("checkout".equals(action)) {
                Object currentCart = session.getAttribute("CURRENT_CART");
                @SuppressWarnings("unchecked")
                Map<String, String> userSession = (Map<String, String>) session.getAttribute(Constants.USER_SESSION);
                int orderId = cartService.checkout(currentCart, tableId, userSession);
                session.removeAttribute("CURRENT_CART");
                session.setAttribute("LAST_ORDER_ID", orderId);
                session.setAttribute(Constants.SUCCESS_MESSAGE, Constants.SUCCESS_CHECKOUT);
            }

        } catch (ValidationException e) {
            session.setAttribute("ACTIVE_TAB", "cart");
            session.setAttribute(Constants.ERROR_MESSAGE, e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/" + url);
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }
}
