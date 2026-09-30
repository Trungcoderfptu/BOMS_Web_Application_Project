package controller;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet; // <-- Bổ sung import này
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "LanguageController", urlPatterns = {"/LanguageController"})
public class LanguageController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String lang = request.getParameter("lang");
        if (lang != null && (lang.equals("vi") || lang.equals("en"))) {
            HttpSession session = request.getSession();
            session.setAttribute("LANG", lang);
            Cookie cookie = new Cookie("LANG", lang);
            cookie.setMaxAge(30 * 24 * 60 * 60);
            cookie.setPath("/"); // <-- Ép Cookie áp dụng cho toàn bộ dự án
            response.addCookie(cookie);
        }
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isEmpty()) {
            response.sendRedirect(referer);
        } else {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
        }
    }
}
