package controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import service.AttendanceService;
import utility.Constants;
import utility.FlashMessageUtil;
import utility.ValidationException;

/**
 * Controller điều phối phân hệ Chấm công (Time Attendance UC-05 & UC-06)
 * Tuyệt đối không import Model hoặc DAO (tuân thủ Rule_BOMS.md)
 */
public class AttendanceController extends HttpServlet {

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

        FlashMessageUtil.forwardFlashMessages(request, session);

        int userId = Integer.parseInt(userSession.get("userId"));
        LocalDate today = LocalDate.now();

        String weekParam = request.getParameter("weekDate");
        LocalDate timetableWeekDate = today;
        if (weekParam != null && !weekParam.trim().isEmpty()) {
            try {
                timetableWeekDate = LocalDate.parse(weekParam.trim());
            } catch (DateTimeParseException ignored) {
                timetableWeekDate = today;
            }
        }

        int month = parseQueryInt(request.getParameter("month"), today.getMonthValue());
        int year = parseQueryInt(request.getParameter("year"), today.getYear());

        try {
            Map<String, Object> todayAttendance = attendanceService.getTodayAttendanceForView(userId);
            Map<String, Object> weeklyTimetable = attendanceService.getStaffWeeklyTimetable(userId, timetableWeekDate);
            List<Map<String, Object>> historyList = attendanceService.getAttendanceHistoryForView(userId, month, year);
            Map<String, Object> summary = attendanceService.getMonthlySummaryForView(userId, month, year);

            request.setAttribute("TODAY_ATTENDANCE", todayAttendance);
            request.setAttribute("WEEKLY_TIMETABLE", weeklyTimetable);
            request.setAttribute("TIMETABLE_WEEK_DATE", timetableWeekDate.toString());
            request.setAttribute("ATTENDANCE_HISTORY", historyList);
            request.setAttribute("ATTENDANCE_SUMMARY", summary);
            request.setAttribute("CURRENT_MONTH", month);
            request.setAttribute("CURRENT_YEAR", year);
        } catch (ValidationException e) {
            request.setAttribute(Constants.ERROR_MESSAGE, e.getMessage());
        } catch (Exception e) {
            request.setAttribute(Constants.ERROR_MESSAGE, Constants.ERROR_SYSTEM);
        }

        request.getRequestDispatcher("attendance.jsp").forward(request, response);
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

        int userId = Integer.parseInt(userSession.get("userId"));
        String action = request.getParameter("action");

        try {
            if ("checkIn".equals(action)) {
                attendanceService.checkIn(userId);
                session.setAttribute(Constants.SUCCESS_MESSAGE, Constants.SUCCESS_ATTENDANCE_CHECK_IN);
            } else if ("checkOut".equals(action)) {
                String otReason = request.getParameter("otReason");
                attendanceService.checkOut(userId, otReason);
                session.setAttribute(Constants.SUCCESS_MESSAGE, Constants.SUCCESS_ATTENDANCE_CHECK_OUT);
            } else {
                throw new ValidationException(Constants.ERROR_INVALID_ACTION);
            }
        } catch (ValidationException e) {
            session.setAttribute(Constants.ERROR_MESSAGE, e.getMessage());
        } catch (Exception e) {
            session.setAttribute(Constants.ERROR_MESSAGE, Constants.ERROR_SYSTEM);
        }

        response.sendRedirect(request.getContextPath() + "/AttendanceController");
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
        return "Time Attendance Controller for BOMS";
    }
}
