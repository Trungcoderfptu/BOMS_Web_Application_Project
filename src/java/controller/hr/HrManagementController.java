package controller.hr;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import service.AttendanceService;
import utility.Constants;
import utility.FlashMessageUtil;
import utility.ValidationException;

/**
 * Controller điều phối Phân hệ Quản lý Nhân sự & Ca làm việc (HR & Shift Management - Task 10)
 * Tuân thủ Rule_BOMS: Tuyệt đối không import Model hoặc DAO, bọc try-catch tập trung, PRG chuẩn.
 */
@WebServlet(name = "HrManagementController", urlPatterns = {"/HrManagementController"})
public class HrManagementController extends HttpServlet {

    private AttendanceService attendanceService;

    @Override
    public void init() throws ServletException {
        attendanceService = new AttendanceService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        HttpSession session = request.getSession();

        @SuppressWarnings("unchecked")
        Map<String, String> userSession = (Map<String, String>) session.getAttribute(Constants.USER_SESSION);
        if (userSession == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        String role = userSession.get("role");
        if (!"Manager".equalsIgnoreCase(role) && !"Admin".equalsIgnoreCase(role)) {
            session.setAttribute(Constants.ERROR_MESSAGE, Constants.ERROR_UNAUTHORIZED);
            response.sendRedirect(request.getContextPath() + "/index.jsp");
            return;
        }

        FlashMessageUtil.forwardFlashMessages(request, session);

        LocalDate today = LocalDate.now();
        String dateParam = request.getParameter("date");
        LocalDate filterDate = today;
        if (dateParam != null && !dateParam.trim().isEmpty()) {
            try {
                filterDate = LocalDate.parse(dateParam.trim());
            } catch (Exception ignored) {
                filterDate = today;
            }
        }

        String weekParam = request.getParameter("weekDate");
        LocalDate matrixWeekDate = today;
        if (weekParam != null && !weekParam.trim().isEmpty()) {
            try {
                matrixWeekDate = LocalDate.parse(weekParam.trim());
            } catch (Exception ignored) {
                matrixWeekDate = today;
            }
        }

        int month = parseQueryInt(request.getParameter("month"), today.getMonthValue());
        int year = parseQueryInt(request.getParameter("year"), today.getYear());
        String activeTab = request.getParameter("tab");
        if (activeTab == null || activeTab.trim().isEmpty()) {
            activeTab = "shifts";
        }

        try {
            List<Map<String, Object>> shifts = attendanceService.getShiftsForManagementView();
            List<Map<String, Object>> dailyAssignments = attendanceService.getDailyAssignmentsForView(filterDate);
            Map<String, Object> managerMatrix = attendanceService.getManagerWeeklyMatrix(matrixWeekDate);
            List<Map<String, Object>> monthlySummary = attendanceService.getMonthlyTimesheetSummaryForView(month, year);
            List<Map<String, Object>> pendingApprovals = attendanceService.getPendingApprovalsForView();
            List<Map<String, Object>> staffList = attendanceService.getStaffListForView();

            request.setAttribute("SHIFTS", shifts);
            request.setAttribute("DAILY_ASSIGNMENTS", dailyAssignments);
            request.setAttribute("MANAGER_MATRIX", managerMatrix);
            request.setAttribute("MONTHLY_SUMMARY", monthlySummary);
            request.setAttribute("PENDING_APPROVALS", pendingApprovals);
            request.setAttribute("STAFF_LIST", staffList);

            request.setAttribute("FILTER_DATE", filterDate.toString());
            request.setAttribute("MATRIX_WEEK_DATE", matrixWeekDate.toString());
            request.setAttribute("CURRENT_MONTH", month);
            request.setAttribute("CURRENT_YEAR", year);
            request.setAttribute(Constants.ACTIVE_TAB, activeTab);
        } catch (ValidationException e) {
            request.setAttribute(Constants.ERROR_MESSAGE, e.getMessage());
        } catch (Exception e) {
            request.setAttribute(Constants.ERROR_MESSAGE, Constants.ERROR_SYSTEM);
        }

        request.getRequestDispatcher("hr_management.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        HttpSession session = request.getSession();

        @SuppressWarnings("unchecked")
        Map<String, String> userSession = (Map<String, String>) session.getAttribute(Constants.USER_SESSION);
        if (userSession == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        String role = userSession.get("role");
        if (!"Manager".equalsIgnoreCase(role) && !"Admin".equalsIgnoreCase(role)) {
            session.setAttribute(Constants.ERROR_MESSAGE, Constants.ERROR_UNAUTHORIZED);
            response.sendRedirect(request.getContextPath() + "/index.jsp");
            return;
        }

        int managerUserId = Integer.parseInt(userSession.get("userId"));
        String action = request.getParameter("action");
        String activeTab = request.getParameter("tab");
        if (activeTab == null || activeTab.trim().isEmpty()) {
            activeTab = "shifts";
        }
        String filterDate = request.getParameter("filterDate");
        if (filterDate == null || filterDate.trim().isEmpty()) {
            filterDate = LocalDate.now().toString();
        }

        try {
            if ("createShift".equals(action)) {
                String shiftName = request.getParameter("shiftName");
                String startTime = request.getParameter("startTime");
                String endTime = request.getParameter("endTime");
                attendanceService.createShift(shiftName, startTime, endTime);
                session.setAttribute(Constants.SUCCESS_MESSAGE, Constants.SUCCESS_CREATE_SHIFT);
                activeTab = "shifts";
            } else if ("updateShift".equals(action)) {
                int shiftId = parseQueryInt(request.getParameter("shiftId"), 0);
                String shiftName = request.getParameter("shiftName");
                String startTime = request.getParameter("startTime");
                String endTime = request.getParameter("endTime");
                attendanceService.updateShift(shiftId, shiftName, startTime, endTime);
                session.setAttribute(Constants.SUCCESS_MESSAGE, Constants.SUCCESS_UPDATE_SHIFT);
                activeTab = "shifts";
            } else if ("assignShift".equals(action)) {
                int userId = parseQueryInt(request.getParameter("userId"), 0);
                int shiftId = parseQueryInt(request.getParameter("shiftId"), 0);
                String workDate = request.getParameter("workDate");
                attendanceService.assignShift(userId, shiftId, workDate);
                session.setAttribute(Constants.SUCCESS_MESSAGE, Constants.SUCCESS_ASSIGN_SHIFT);
                filterDate = workDate;
                activeTab = "shifts";
            } else if ("bulkAssignShift".equals(action)) {
                int userId = parseQueryInt(request.getParameter("userId"), 0);
                int shiftId = parseQueryInt(request.getParameter("shiftId"), 0);
                String startDate = request.getParameter("startDate");
                String endDate = request.getParameter("endDate");
                String[] daysOfWeek = request.getParameterValues("daysOfWeek");
                attendanceService.bulkAssignShift(userId, shiftId, startDate, endDate, daysOfWeek);
                session.setAttribute(Constants.SUCCESS_MESSAGE, Constants.SUCCESS_BULK_ASSIGN_SHIFT);
                if (startDate != null && !startDate.trim().isEmpty()) {
                    filterDate = startDate;
                }
                activeTab = "shifts";
            } else if ("deleteAssignment".equals(action)) {
                int assignmentId = parseQueryInt(request.getParameter("assignmentId"), 0);
                attendanceService.deleteAssignment(assignmentId);
                session.setAttribute(Constants.SUCCESS_MESSAGE, Constants.SUCCESS_REMOVE_ASSIGNMENT);
                activeTab = "shifts";
            } else if ("approveOt".equals(action)) {
                int attendanceId = parseQueryInt(request.getParameter("attendanceId"), 0);
                attendanceService.approveAttendance(attendanceId, managerUserId, "ot");
                session.setAttribute(Constants.SUCCESS_MESSAGE, Constants.SUCCESS_APPROVE_ATTENDANCE);
                activeTab = "tracking";
            } else if ("rejectOt".equals(action)) {
                int attendanceId = parseQueryInt(request.getParameter("attendanceId"), 0);
                attendanceService.rejectAttendance(attendanceId, managerUserId, "ot");
                session.setAttribute(Constants.SUCCESS_MESSAGE, Constants.SUCCESS_REJECT_ATTENDANCE);
                activeTab = "tracking";
            } else if ("approveEarly".equals(action)) {
                int attendanceId = parseQueryInt(request.getParameter("attendanceId"), 0);
                attendanceService.approveAttendance(attendanceId, managerUserId, "early");
                session.setAttribute(Constants.SUCCESS_MESSAGE, Constants.SUCCESS_APPROVE_ATTENDANCE);
                activeTab = "tracking";
            } else if ("rejectEarly".equals(action)) {
                int attendanceId = parseQueryInt(request.getParameter("attendanceId"), 0);
                attendanceService.rejectAttendance(attendanceId, managerUserId, "early");
                session.setAttribute(Constants.SUCCESS_MESSAGE, Constants.SUCCESS_REJECT_ATTENDANCE);
                activeTab = "tracking";
            } else {
                throw new ValidationException(Constants.ERROR_INVALID_ACTION);
            }
        } catch (ValidationException e) {
            session.setAttribute(Constants.ERROR_MESSAGE, e.getMessage());
        } catch (Exception e) {
            session.setAttribute(Constants.ERROR_MESSAGE, Constants.ERROR_SYSTEM);
        }

        response.sendRedirect(request.getContextPath() + "/HrManagementController?tab=" + activeTab + "&date=" + filterDate);
    }

    private int parseQueryInt(String paramValue, int defaultValue) {
        if (paramValue != null && !paramValue.trim().isEmpty()) {
            try {
                return Integer.parseInt(paramValue.trim());
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    @Override
    public String getServletInfo() {
        return "HR and Shift Management Controller for BOMS";
    }
}
