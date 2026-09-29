package filter;

import java.io.IOException;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import utility.Constants;

@WebFilter(filterName = "AuthFilter", urlPatterns = {"/*"})
public class AuthFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        
        String uri = req.getRequestURI();
        
        //Whitelist
        boolean isStaticResource = uri.endsWith(".css") || uri.endsWith(".js") || uri.endsWith(".svg") || uri.endsWith(".png");
        boolean isAuthPage = uri.endsWith("login.jsp") || uri.endsWith("register.jsp") || uri.endsWith("LoginController")||uri.endsWith("dashboard.jsp");
        
        if (isStaticResource || isAuthPage) {
            chain.doFilter(request, response);
            return;
        }
        
        // Check Session
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute(Constants.USER_SESSION) != null) {
            chain.doFilter(request, response);
        } else {
            res.sendRedirect(req.getContextPath() + "/login.jsp");
        }
    }

    @Override
    public void destroy() {}
}