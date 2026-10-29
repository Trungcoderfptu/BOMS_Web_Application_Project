package controller;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import utility.Constants;

/**
 * Điều phối chuyển đổi ngôn ngữ hiển thị (i18n: vi / en / ja).
 * Tuân thủ Rule_BOMS.md.
 * 
 * @author AD
 */
@WebServlet(name = "LanguageController", urlPatterns = {"/LanguageController"})
public class LanguageController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String lang = request.getParameter("lang");
        if (isValidLanguage(lang)) {
            HttpSession session = request.getSession();
            session.setAttribute(Constants.LANG, lang);
            Cookie cookie = new Cookie(Constants.LANG, lang);
            cookie.setMaxAge(30 * 24 * 60 * 60);
            cookie.setPath("/");
            response.addCookie(cookie);
        }
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isEmpty()) {
            response.sendRedirect(referer);
        } else {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
        }
    }

    /**
     * Kiểm tra tính hợp lệ của mã ngôn ngữ theo danh sách cho phép (SRP).
     *
     * @param lang Mã ngôn ngữ đầu vào
     * @return true nếu thuộc danh sách hỗ trợ (vi, en, ja)
     */
    private boolean isValidLanguage(String lang) {
        return Constants.LANG_VI.equals(lang)
                || Constants.LANG_EN.equals(lang)
                || Constants.LANG_JA.equals(lang);
    }
}
