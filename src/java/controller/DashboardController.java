package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import service.AttendanceService;
import service.IngredientService;
import service.NotificationService;
import utility.Constants;
import utility.FlashMessageUtil;

/**
 * Controller điều phối Bảng điều khiển trung tâm (Central Dashboard - Task 12)
 * Tuân thủ nghiêm ngặt Rule_BOMS: Zero Model leakage, PRG chuẩn, dùng FlashMessageUtil.
 */
@WebServlet(name = "DashboardController", urlPatterns = {"/DashboardController"})
public class DashboardController extends HttpServlet {

    private AttendanceService attendanceService;
    private NotificationService notificationService;
    private IngredientService ingredientService;

    @Override
    public void init() throws ServletException {
        this.attendanceService = new AttendanceService();
        this.notificationService = new NotificationService();
        this.ingredientService = new IngredientService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        FlashMessageUtil.forwardFlashMessages(request, session);

        @SuppressWarnings("unchecked")
        Map<String, String> userSession = (Map<String, String>) session.getAttribute(Constants.USER_SESSION);
        if (userSession == null) {
            // Khách chưa đăng nhập: Hiển thị giao diện Landing page chào mừng
            request.getRequestDispatcher("index.jsp").forward(request, response);
            return;
        }

        int userId = 0;
        try {
            userId = Integer.parseInt(userSession.get("userId"));
        } catch (NumberFormatException ignored) {
        }

        String userRole = userSession.get("role");

        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.format.DateTimeFormatter dateFormatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd");
        java.time.format.DateTimeFormatter timeFormatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss");
        request.setAttribute("DASHBOARD_SERVER_DATE", now.format(dateFormatter));
        request.setAttribute("DASHBOARD_SERVER_TIME", now.format(timeFormatter));

        try {
            // 1. Trạng thái ca làm việc hiện tại của người dùng
            Map<String, Object> todayAttendance = null;
            if (userId > 0) {
                todayAttendance = attendanceService.getTodayAttendanceForView(userId);
            }
            request.setAttribute("DASHBOARD_ATTENDANCE", todayAttendance);

            // 2. Danh sách 8 thông báo mới nhất & số lượng chưa đọc
            List<Map<String, Object>> recentNotifications = Collections.emptyList();
            int unreadCount = 0;
            if (userId > 0) {
                recentNotifications = notificationService.getRecentNotificationsForView(userId, 8);
                unreadCount = notificationService.getUnreadCount(userId);
            }
            request.setAttribute("DASHBOARD_NOTIFICATIONS", recentNotifications);
            request.setAttribute("DASHBOARD_UNREAD_COUNT", unreadCount);

            // 3. Chỉ số thống kê nhanh dành cho Manager
            if ("Manager".equalsIgnoreCase(userRole)) {
                List<Map<String, Object>> pendingApprovals = attendanceService.getPendingApprovalsForView();
                int pendingApprovalsCount = pendingApprovals != null ? pendingApprovals.size() : 0;
                request.setAttribute("DASHBOARD_PENDING_APPROVALS", pendingApprovalsCount);

                List<Map<String, Object>> activeAlerts = ingredientService.getActiveAlertsForView();
                int activeAlertsCount = activeAlerts != null ? activeAlerts.size() : 0;
                request.setAttribute("DASHBOARD_ACTIVE_ALERTS", activeAlertsCount);
            }

        } catch (Exception e) {
            request.setAttribute("DASHBOARD_NOTIFICATIONS", Collections.emptyList());
            request.setAttribute("DASHBOARD_UNREAD_COUNT", 0);
        }

        request.getRequestDispatcher("index.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
