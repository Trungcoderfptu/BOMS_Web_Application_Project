package filter;

import java.io.IOException;
import java.util.Map;
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
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String uri = req.getRequestURI();
        String contextPath = req.getContextPath();
        // Whitelist
        boolean isStaticResource = uri.endsWith(".css")
                || uri.endsWith(".js") || uri.endsWith(".svg")
                || uri.endsWith(".png") || uri.endsWith(".jpg")
                || uri.endsWith(".jpeg") || uri.endsWith(".webp")
                || uri.contains("/uploads/");
        boolean isAuthPage = uri.endsWith("login.jsp")
                || uri.endsWith("register.jsp")
                || uri.endsWith("LoginController")
                || uri.endsWith("index.jsp")
                || uri.endsWith("DashboardController")
                || uri.endsWith("menu.jsp")
                || uri.endsWith("RegisterController")
                || uri.endsWith("LanguageController")
                || uri.endsWith("MenuController")
                || uri.endsWith("/menu")
                || uri.endsWith("CartController")
                || uri.equals(contextPath)
                || uri.equals(contextPath + "/");

        if (isStaticResource || isAuthPage) {
            chain.doFilter(request, response);
            return;
        }

        // Check Session & Role-Based Access Control (RBAC)
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute(Constants.USER_SESSION) == null) {
            res.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }

        @SuppressWarnings("unchecked")
        Map<String, String> userSession = (Map<String, String>) session.getAttribute(Constants.USER_SESSION);
        String userRole = userSession != null ? userSession.get("role") : null;

        // Phân quyền phân hệ Shipper (Chỉ Shipper & Admin)
        if (uri.endsWith("ShipperController") || uri.endsWith("shipper_dashboard.jsp")) {
            if (!Constants.ROLE_SHIPPER.equalsIgnoreCase(userRole) && !Constants.ROLE_ADMIN.equalsIgnoreCase(userRole)) {
                res.sendRedirect(req.getContextPath() + "/delivery_unauthorized.jsp");
                return;
            }
        }

        // Phân quyền Điều phối Giao hàng (Manager, Admin, Staff quầy - CẤM Shipper)
        if (uri.endsWith("DispatchController") || uri.endsWith("dispatch_dashboard.jsp")) {
            if (Constants.ROLE_SHIPPER.equalsIgnoreCase(userRole)) {
                res.sendRedirect(req.getContextPath() + "/delivery_unauthorized.jsp");
                return;
            }
        }

        // Phân quyền Quản lý Nhân sự & Ca làm việc (Chỉ Manager & Admin)
        if (uri.endsWith("HrManagementController") || uri.endsWith("hr_management.jsp")) {
            if (!Constants.ROLE_MANAGER.equalsIgnoreCase(userRole) && !Constants.ROLE_ADMIN.equalsIgnoreCase(userRole)) {
                res.sendRedirect(req.getContextPath() + "/DashboardController");
                return;
            }
        }

        // Phân quyền Quản trị tối cao Super Admin
        if (uri.endsWith("AdminDashboardController") || uri.endsWith("admin_dashboard.jsp")) {
            if (!Constants.ROLE_ADMIN.equalsIgnoreCase(userRole)) {
                res.sendRedirect(req.getContextPath() + "/DashboardController");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
