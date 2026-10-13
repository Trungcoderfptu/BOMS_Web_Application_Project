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
import service.ProductService;
import utility.Constants;
import utility.ValidationException;

/**
 *
 * @author AD
 */
public class MenuController extends HttpServlet {

    private ProductService productService;

    @Override
    public void init() throws ServletException {
        productService = new ProductService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        jakarta.servlet.http.HttpSession session = request.getSession();
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
        if (session.getAttribute("ACTIVE_TAB") != null) {
            request.setAttribute("ACTIVE_TAB", session.getAttribute("ACTIVE_TAB"));
            session.removeAttribute("ACTIVE_TAB");
        }
        String activeTab = request.getParameter("activeTab");
        if (activeTab != null && !activeTab.trim().isEmpty()) {
            request.setAttribute("ACTIVE_TAB", activeTab);
        }
        try {
            List<Map<String, Object>> menuList = productService.getAllProductsForView();
            request.setAttribute("MENU_LIST", menuList);
            String tableId = request.getParameter("tableId");
            if (tableId != null && !tableId.trim().isEmpty()) {
                request.setAttribute("CURRENT_TABLE", tableId);
            }
        } catch (ValidationException e) {
            request.setAttribute(Constants.ERROR_MESSAGE, e.getMessage());
        }
        request.getRequestDispatcher("menu.jsp").forward(request, response);

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
