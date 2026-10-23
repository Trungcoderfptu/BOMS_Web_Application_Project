package service;

import dao.AttendanceDAO;
import dao.UserDAO;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.Attendance;
import model.Shift;
import model.ShiftAssignment;
import model.User;
import utility.Constants;
import utility.ValidationException;
import utility.Validator;

/**
 * Service xử lý nghiệp vụ Quản lý Chấm công & Ca làm việc (UC-05 & UC-06)
 * Tuân thủ Rule_BOMS: 100% logic tại Service, đóng gói Map trả về Controller, cấm để lọt Model.
 */
public class AttendanceService {

    private static final int EARLY_CHECKIN_THRESHOLD_MINUTES = 15;
    private static final int LATE_CHECKIN_THRESHOLD_MINUTES = 15;
    private static final int EARLY_CHECKOUT_THRESHOLD_MINUTES = 15;
    private static final int OVERTIME_THRESHOLD_MINUTES = 15;

    private AttendanceDAO attendanceDAO;
    private UserDAO userDAO;
    private NotificationService notificationService;
    private SimpleDateFormat timeFormat;
    private SimpleDateFormat dateFormat;
    private SimpleDateFormat dateTimeFormat;

    public AttendanceService() {
        this.attendanceDAO = new AttendanceDAO();
        this.userDAO = new UserDAO();
        this.notificationService = new NotificationService();
        this.timeFormat = new SimpleDateFormat("HH:mm:ss");
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        this.dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    // =========================================================================
    // 1. NGHIỆP VỤ CHẤM CÔNG (FACADE)
    // =========================================================================

    /**
     * UC-05: Chấm công vào ca (Check-in)
     */
    public void checkIn(int userId) throws ValidationException {
        ShiftAssignment assignment = validateAndGetTodayAssignment(userId);
        validateNotYetCheckedIn(assignment.getAssignmentId());

        Timestamp now = new Timestamp(System.currentTimeMillis());
        LocalTime currentTime = now.toLocalDateTime().toLocalTime();
        LocalTime startTime = assignment.getShift().getStartTime().toLocalTime();

        long diffMinutes = java.time.Duration.between(startTime, currentTime).toMinutes();
        if (diffMinutes < -EARLY_CHECKIN_THRESHOLD_MINUTES) {
            throw new ValidationException(Constants.ERROR_ATTENDANCE_TOO_EARLY);
        }

        String checkInStatus = determineCheckInStatus(now, assignment.getShift().getStartTime());

        Attendance attendance = new Attendance(0, assignment.getAssignmentId(), now, checkInStatus);
        attendanceDAO.insertCheckIn(attendance);
    }

    /**
     * UC-06: Chấm công tan ca (Check-out)
     */
    public void checkOut(int userId, String otReason) throws ValidationException {
        ShiftAssignment assignment = validateAndGetTodayAssignment(userId);
        Attendance attendance = validateAndGetActiveAttendance(assignment.getAssignmentId());

        validateNoPendingCounterOrders(userId);

        Timestamp now = new Timestamp(System.currentTimeMillis());
        LocalTime currentTime = now.toLocalDateTime().toLocalTime();
        LocalTime endTime = assignment.getShift().getEndTime().toLocalTime();

        if (currentTime.isBefore(endTime)) {
            throw new ValidationException(Constants.ERROR_ATTENDANCE_TOO_EARLY_CHECKOUT);
        }

        boolean isOvertime = isCurrentTimeExceedingOvertime(now, assignment.getShift().getEndTime());

        if (isOvertime && (otReason == null || otReason.trim().isEmpty())) {
            throw new ValidationException(Constants.ERROR_ATTENDANCE_EMPTY_OT_REASON);
        }

        String checkOutStatus = determineCheckOutStatus(now, assignment.getShift().getEndTime(), isOvertime);
        double workingHours = calculateWorkingHours(attendance.getCheckInTime(), now);

        attendance.setCheckOutTime(now);
        attendance.setCheckOutStatus(checkOutStatus);
        attendance.setWorkingHours(workingHours);
        attendance.setOtReason(otReason != null && !otReason.trim().isEmpty() ? otReason.trim() : null);

        attendanceDAO.updateCheckOut(attendance);
    }

    public List<Map<String, Object>> getAllAvailableShiftsForView() throws ValidationException {
        List<Shift> shifts = attendanceDAO.getAllShifts();
        List<Map<String, Object>> list = new ArrayList<>();
        if (shifts != null) {
            for (Shift s : shifts) {
                Map<String, Object> map = new HashMap<>();
                map.put("shiftId", s.getShiftId());
                map.put("shiftName", s.getShiftName());
                map.put("startTime", timeFormat.format(s.getStartTime()));
                map.put("endTime", timeFormat.format(s.getEndTime()));
                map.put("timeRange", timeFormat.format(s.getStartTime()) + " - " + timeFormat.format(s.getEndTime()));
                list.add(map);
            }
        }
        return list;
    }

    // =========================================================================
    // 2. DỮ LIỆU ĐÓNG GÓI CHO VIEW (MAP / DTO)
    // =========================================================================

    public Map<String, Object> getTodayAttendanceForView(int userId) throws ValidationException {
        Map<String, Object> viewData = new HashMap<>();
        Timestamp now = new Timestamp(System.currentTimeMillis());

        viewData.put("serverCurrentDate", dateFormat.format(now));
        viewData.put("serverCurrentTime", timeFormat.format(now));

        ShiftAssignment assignment = attendanceDAO.getTodayShiftAssignment(userId);
        if (assignment == null) {
            viewData.put("hasShift", false);
            viewData.put("systemStatus", "No Shift Today");
            return viewData;
        }

        viewData.put("hasShift", true);
        viewData.put("assignmentId", assignment.getAssignmentId());
        viewData.put("shiftId", assignment.getShiftId());
        viewData.put("shiftName", assignment.getShift().getShiftName());
        viewData.put("startTime", timeFormat.format(assignment.getShift().getStartTime()));
        viewData.put("endTime", timeFormat.format(assignment.getShift().getEndTime()));

        LocalTime currentTime = now.toLocalDateTime().toLocalTime();
        LocalTime shiftStart = assignment.getShift().getStartTime().toLocalTime();
        LocalTime shiftEnd = assignment.getShift().getEndTime().toLocalTime();
        long diffMinutes = java.time.Duration.between(shiftStart, currentTime).toMinutes();

        boolean canCheckIn = diffMinutes >= -EARLY_CHECKIN_THRESHOLD_MINUTES;
        boolean canCheckOut = !currentTime.isBefore(shiftEnd);
        viewData.put("canCheckIn", canCheckIn);
        viewData.put("canCheckOut", canCheckOut);

        Attendance attendance = attendanceDAO.getAttendanceByAssignmentId(assignment.getAssignmentId());
        if (attendance == null) {
            viewData.put("hasCheckedIn", false);
            viewData.put("hasCheckedOut", false);
            viewData.put("systemStatus", "Not Started");
            return viewData;
        }

        viewData.put("hasCheckedIn", true);
        viewData.put("checkInTime", timeFormat.format(attendance.getCheckInTime()));
        viewData.put("checkInDateTime", dateTimeFormat.format(attendance.getCheckInTime()));
        viewData.put("checkInStatus", attendance.getCheckInStatus());

        if (attendance.getCheckOutTime() != null) {
            viewData.put("hasCheckedOut", true);
            viewData.put("checkOutTime", timeFormat.format(attendance.getCheckOutTime()));
            viewData.put("checkOutDateTime", dateTimeFormat.format(attendance.getCheckOutTime()));
            viewData.put("checkOutStatus", attendance.getCheckOutStatus());
            viewData.put("workingHours", attendance.getWorkingHours());
            viewData.put("otReason", attendance.getOtReason());
            viewData.put("systemStatus", "Shift Ended / Off Shift");
        } else {
            viewData.put("hasCheckedOut", false);
            viewData.put("systemStatus", "On Shift / Working");
            viewData.put("isOvertimeEligible", isCurrentTimeExceedingOvertime(now, assignment.getShift().getEndTime()));
        }

        return viewData;
    }

    public List<Map<String, Object>> getAttendanceHistoryForView(int userId, int month, int year) throws ValidationException {
        List<Attendance> attendances = attendanceDAO.getAttendanceHistoryByUserId(userId, month, year);
        List<Map<String, Object>> result = new ArrayList<>();

        if (attendances != null) {
            for (Attendance a : attendances) {
                Map<String, Object> map = new HashMap<>();
                map.put("attendanceId", a.getAttendanceId());
                map.put("workDate", a.getShiftAssignment().getWorkDate().toString());
                map.put("shiftName", a.getShiftAssignment().getShift().getShiftName());
                map.put("shiftTimeRange", timeFormat.format(a.getShiftAssignment().getShift().getStartTime())
                        + " - " + timeFormat.format(a.getShiftAssignment().getShift().getEndTime()));
                map.put("checkInTime", timeFormat.format(a.getCheckInTime()));
                map.put("checkInStatus", a.getCheckInStatus());

                if (a.getCheckOutTime() != null) {
                    map.put("checkOutTime", timeFormat.format(a.getCheckOutTime()));
                    map.put("checkOutStatus", a.getCheckOutStatus());
                    map.put("workingHours", a.getWorkingHours());
                    map.put("otReason", a.getOtReason());
                } else {
                    map.put("checkOutTime", "---");
                    map.put("checkOutStatus", "Working");
                    map.put("workingHours", "---");
                }
                result.add(map);
            }
        }
        return result;
    }

    public Map<String, Object> getMonthlySummaryForView(int userId, int month, int year) throws ValidationException {
        List<Attendance> attendances = attendanceDAO.getAttendanceHistoryByUserId(userId, month, year);
        Map<String, Object> summary = new HashMap<>();

        int totalShifts = 0;
        double totalHours = 0.0;
        int onTimeCount = 0;
        int lateCount = 0;
        int otCount = 0;

        if (attendances != null) {
            for (Attendance a : attendances) {
                totalShifts++;
                if (a.getWorkingHours() != null) {
                    totalHours += a.getWorkingHours();
                }
                if ("On Time".equalsIgnoreCase(a.getCheckInStatus())) {
                    onTimeCount++;
                } else if ("Late".equalsIgnoreCase(a.getCheckInStatus())) {
                    lateCount++;
                }
                if ("Pending OT Approval".equalsIgnoreCase(a.getCheckOutStatus())
                        || "Overtime".equalsIgnoreCase(a.getCheckOutStatus())) {
                    otCount++;
                }
            }
        }

        summary.put("totalShifts", totalShifts);
        summary.put("totalHours", Math.round(totalHours * 100.0) / 100.0);
        summary.put("onTimeCount", onTimeCount);
        summary.put("lateCount", lateCount);
        summary.put("otCount", otCount);
        return summary;
    }

    // =========================================================================
    // 3. LOGIC SO SÁNH THỜI GIAN & TÍNH TOÁN NGHIỆP VỤ (SRP)
    // =========================================================================

    private ShiftAssignment validateAndGetTodayAssignment(int userId) throws ValidationException {
        ShiftAssignment sa = attendanceDAO.getTodayShiftAssignment(userId);
        if (sa == null) {
            throw new ValidationException(Constants.ERROR_ATTENDANCE_NO_SHIFT);
        }
        return sa;
    }

    // =========================================================================
    // HR & SHIFT MANAGEMENT (TASK 10)
    // =========================================================================

    public int createShift(String shiftName, String startTimeStr, String endTimeStr) throws ValidationException {
        Validator.validateShiftName(shiftName);
        Validator.validateShiftTime(startTimeStr, endTimeStr);

        if (attendanceDAO.isShiftNameExists(shiftName, 0)) {
            throw new ValidationException(Constants.ERROR_SHIFT_EXISTS);
        }

        LocalTime startLt = LocalTime.parse(startTimeStr.trim());
        LocalTime endLt = LocalTime.parse(endTimeStr.trim());
        Shift shift = new Shift(0, shiftName.trim(), Time.valueOf(startLt), Time.valueOf(endLt));
        return attendanceDAO.insertShift(shift);
    }

    public boolean updateShift(int shiftId, String shiftName, String startTimeStr, String endTimeStr) throws ValidationException {
        if (shiftId <= 0) {
            throw new ValidationException(Constants.ERROR_ATTENDANCE_NO_SHIFT);
        }
        Validator.validateShiftName(shiftName);
        Validator.validateShiftTime(startTimeStr, endTimeStr);

        Shift existing = attendanceDAO.getShiftById(shiftId);
        if (existing == null) {
            throw new ValidationException(Constants.ERROR_ATTENDANCE_NO_SHIFT);
        }

        if (attendanceDAO.isShiftNameExists(shiftName, shiftId)) {
            throw new ValidationException(Constants.ERROR_SHIFT_EXISTS);
        }

        LocalTime startLt = LocalTime.parse(startTimeStr.trim());
        LocalTime endLt = LocalTime.parse(endTimeStr.trim());
        Shift shift = new Shift(shiftId, shiftName.trim(), Time.valueOf(startLt), Time.valueOf(endLt));
        return attendanceDAO.updateShift(shift);
    }

    private void validateStaffRole(int userId) throws ValidationException {
        User user = userDAO.getUserById(userId);
        if (user == null || !user.isActive()) {
            throw new ValidationException(Constants.ERROR_USER_NOT_FOUND);
        }
        if (!"Staff".equalsIgnoreCase(user.getRole())) {
            throw new ValidationException(Constants.ERROR_CANNOT_ASSIGN_NON_STAFF);
        }
    }

    public int assignShift(int userId, int shiftId, String workDateStr) throws ValidationException {
        if (userId <= 0) {
            throw new ValidationException(Constants.ERROR_USER_NOT_FOUND);
        }
        validateStaffRole(userId);
        if (shiftId <= 0) {
            throw new ValidationException(Constants.ERROR_ATTENDANCE_NO_SHIFT);
        }
        Validator.checkEmpty(workDateStr, Constants.ERROR_INVALID_DATE_FORMAT);

        LocalDate workDate;
        try {
            workDate = LocalDate.parse(workDateStr.trim());
        } catch (Exception e) {
            throw new ValidationException(Constants.ERROR_INVALID_DATE_FORMAT);
        }

        Shift shift = attendanceDAO.getShiftById(shiftId);
        if (shift == null) {
            throw new ValidationException(Constants.ERROR_ATTENDANCE_NO_SHIFT);
        }

        Date sqlDate = Date.valueOf(workDate);
        if (attendanceDAO.hasAssignmentOnDate(userId, sqlDate)) {
            throw new ValidationException(Constants.ERROR_ASSIGNMENT_EXISTS);
        }

        ShiftAssignment sa = new ShiftAssignment(0, userId, shiftId, sqlDate);
        int newAssignmentId = attendanceDAO.insertShiftAssignment(sa);
        try {
            notificationService.createNotification(
                    userId,
                    "ShiftAssignment",
                    newAssignmentId,
                    "Bạn vừa được phân công ca: " + shift.getShiftName() + " (" + workDateStr + ")"
            );
        } catch (Exception ignored) {
        }
        return newAssignmentId;
    }

    public Map<String, Object> bulkAssignShift(int userId, int shiftId, String startDateStr, String endDateStr, String[] daysOfWeek) throws ValidationException {
        if (userId <= 0) {
            throw new ValidationException(Constants.ERROR_USER_NOT_FOUND);
        }
        validateStaffRole(userId);
        if (shiftId <= 0) {
            throw new ValidationException(Constants.ERROR_ATTENDANCE_NO_SHIFT);
        }
        Validator.checkEmpty(startDateStr, Constants.ERROR_INVALID_DATE_FORMAT);
        Validator.checkEmpty(endDateStr, Constants.ERROR_INVALID_DATE_FORMAT);

        if (daysOfWeek == null || daysOfWeek.length == 0) {
            throw new ValidationException(Constants.ERROR_EMPTY_DAYS_OF_WEEK);
        }

        LocalDate startDate;
        LocalDate endDate;
        try {
            startDate = LocalDate.parse(startDateStr.trim());
            endDate = LocalDate.parse(endDateStr.trim());
        } catch (Exception e) {
            throw new ValidationException(Constants.ERROR_INVALID_DATE_FORMAT);
        }

        if (endDate.isBefore(startDate)) {
            throw new ValidationException(Constants.ERROR_INVALID_DATE_RANGE);
        }

        Shift shift = attendanceDAO.getShiftById(shiftId);
        if (shift == null) {
            throw new ValidationException(Constants.ERROR_ATTENDANCE_NO_SHIFT);
        }

        java.util.Set<Integer> selectedDays = new java.util.HashSet<>();
        for (String dow : daysOfWeek) {
            if (dow != null && !dow.trim().isEmpty()) {
                try {
                    selectedDays.add(Integer.parseInt(dow.trim()));
                } catch (NumberFormatException ignored) {
                }
            }
        }

        if (selectedDays.isEmpty()) {
            throw new ValidationException(Constants.ERROR_EMPTY_DAYS_OF_WEEK);
        }

        int assignedCount = 0;
        int skippedCount = 0;
        int lastAssignmentId = 0;

        LocalDate curr = startDate;
        while (!curr.isAfter(endDate)) {
            int dayVal = curr.getDayOfWeek().getValue(); // 1 = Mon ... 7 = Sun
            if (selectedDays.contains(dayVal)) {
                Date sqlDate = Date.valueOf(curr);
                if (attendanceDAO.hasAssignmentOnDate(userId, sqlDate)) {
                    skippedCount++;
                } else {
                    ShiftAssignment sa = new ShiftAssignment(0, userId, shiftId, sqlDate);
                    int newId = attendanceDAO.insertShiftAssignment(sa);
                    if (newId > 0) {
                        assignedCount++;
                        lastAssignmentId = newId;
                    }
                }
            }
            curr = curr.plusDays(1);
        }

        if (assignedCount == 0) {
            if (skippedCount > 0) {
                throw new ValidationException(Constants.ERROR_NO_ASSIGNMENT_CREATED);
            } else {
                throw new ValidationException(Constants.ERROR_EMPTY_DAYS_OF_WEEK);
            }
        }

        try {
            notificationService.createNotification(
                    userId,
                    "ShiftAssignment",
                    lastAssignmentId,
                    "Bạn vừa được phân công " + assignedCount + " ca làm việc (" + shift.getShiftName() + ") từ " + startDateStr + " đến " + endDateStr + "."
            );
        } catch (Exception ignored) {
        }

        Map<String, Object> result = new HashMap<>();
        result.put("assignedCount", assignedCount);
        result.put("skippedCount", skippedCount);
        return result;
    }

    public Map<String, Object> getStaffWeeklyTimetable(int userId, LocalDate weekDate) throws ValidationException {
        if (weekDate == null) {
            weekDate = LocalDate.now();
        }
        LocalDate monday = weekDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate sunday = monday.plusDays(6);
        LocalDate today = LocalDate.now();
        LocalTime nowTime = LocalTime.now();

        List<Map<String, Object>> daysList = new ArrayList<>();
        String[] dayNames = {"Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ Nhật"};
        for (int i = 0; i < 7; i++) {
            LocalDate d = monday.plusDays(i);
            Map<String, Object> dayInfo = new HashMap<>();
            dayInfo.put("date", d.toString());
            dayInfo.put("dayOfWeek", i + 1);
            dayInfo.put("dayName", dayNames[i]);
            dayInfo.put("shortDate", d.getDayOfMonth() + "/" + d.getMonthValue());
            dayInfo.put("isToday", d.equals(today));
            daysList.add(dayInfo);
        }

        List<Shift> shifts = attendanceDAO.getAllShifts();
        List<Map<String, Object>> assignments = attendanceDAO.getAssignmentsByUserAndDateRange(
                userId, Date.valueOf(monday), Date.valueOf(sunday)
        );

        Map<String, Map<String, Object>> lookup = new HashMap<>();
        if (assignments != null) {
            for (Map<String, Object> a : assignments) {
                Date wd = (Date) a.get("workDate");
                int sId = (int) a.get("shiftId");
                if (wd != null) {
                    lookup.put(wd.toString() + "_" + sId, a);
                }
            }
        }

        List<Map<String, Object>> matrix = new ArrayList<>();
        if (shifts != null) {
            for (Shift s : shifts) {
                Map<String, Object> row = new HashMap<>();
                row.put("shiftId", s.getShiftId());
                row.put("shiftName", s.getShiftName());
                String startStr = s.getStartTime() != null ? timeFormat.format(s.getStartTime()).substring(0, 5) : "--:--";
                String endStr = s.getEndTime() != null ? timeFormat.format(s.getEndTime()).substring(0, 5) : "--:--";
                row.put("timeRange", startStr + " - " + endStr);

                List<Map<String, Object>> slots = new ArrayList<>();
                for (int i = 0; i < 7; i++) {
                    LocalDate d = monday.plusDays(i);
                    String key = d.toString() + "_" + s.getShiftId();
                    Map<String, Object> cell = new HashMap<>();
                    cell.put("date", d.toString());
                    cell.put("isToday", d.equals(today));

                    if (lookup.containsKey(key)) {
                        Map<String, Object> a = lookup.get(key);
                        cell.put("hasShift", true);
                        cell.put("assignmentId", a.get("assignmentId"));
                        cell.put("shiftName", s.getShiftName());
                        cell.put("timeRange", startStr + " - " + endStr);
                        cell.put("hasAttendance", a.get("hasAttendance"));

                        Timestamp inTime = (Timestamp) a.get("checkInTime");
                        Timestamp outTime = (Timestamp) a.get("checkOutTime");
                        cell.put("checkInTime", inTime != null ? timeFormat.format(inTime).substring(0, 5) : null);
                        cell.put("checkOutTime", outTime != null ? timeFormat.format(outTime).substring(0, 5) : null);
                        cell.put("checkInStatus", a.get("checkInStatus"));
                        cell.put("checkOutStatus", a.get("checkOutStatus"));
                        cell.put("workingHours", a.get("workingHours"));

                        boolean hasIn = inTime != null;
                        boolean hasOut = outTime != null;
                        cell.put("hasCheckedIn", hasIn);
                        cell.put("hasCheckedOut", hasOut);

                        if (d.equals(today)) {
                            LocalTime sStart = s.getStartTime().toLocalTime();
                            LocalTime sEnd = s.getEndTime().toLocalTime();
                            long diffMin = java.time.Duration.between(sStart, nowTime).toMinutes();
                            cell.put("canCheckIn", diffMin >= -EARLY_CHECKIN_THRESHOLD_MINUTES && !hasIn);
                            cell.put("canCheckOut", hasIn && !hasOut && !nowTime.isBefore(sEnd));
                        } else {
                            cell.put("canCheckIn", false);
                            cell.put("canCheckOut", false);
                        }
                    } else {
                        cell.put("hasShift", false);
                    }
                    slots.add(cell);
                }
                row.put("slots", slots);
                matrix.add(row);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("monday", monday.toString());
        result.put("sunday", sunday.toString());
        result.put("weekLabel", monday.getDayOfMonth() + "/" + monday.getMonthValue() + " - " + sunday.getDayOfMonth() + "/" + sunday.getMonthValue() + "/" + sunday.getYear());
        result.put("prevWeekDate", monday.minusWeeks(1).toString());
        result.put("nextWeekDate", monday.plusWeeks(1).toString());
        result.put("currentWeekDate", today.toString());
        result.put("days", daysList);
        result.put("matrix", matrix);
        return result;
    }

    public Map<String, Object> getManagerWeeklyMatrix(LocalDate weekDate) throws ValidationException {
        if (weekDate == null) {
            weekDate = LocalDate.now();
        }
        LocalDate monday = weekDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate sunday = monday.plusDays(6);
        LocalDate today = LocalDate.now();

        List<Map<String, Object>> daysList = new ArrayList<>();
        String[] dayNames = {"Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ Nhật"};
        for (int i = 0; i < 7; i++) {
            LocalDate d = monday.plusDays(i);
            Map<String, Object> dayInfo = new HashMap<>();
            dayInfo.put("date", d.toString());
            dayInfo.put("dayOfWeek", i + 1);
            dayInfo.put("dayName", dayNames[i]);
            dayInfo.put("shortDate", d.getDayOfMonth() + "/" + d.getMonthValue());
            dayInfo.put("isToday", d.equals(today));
            daysList.add(dayInfo);
        }

        List<Map<String, Object>> staffList = attendanceDAO.getActiveStaffUsers();
        List<Map<String, Object>> assignments = attendanceDAO.getAssignmentsByDateRange(
                Date.valueOf(monday), Date.valueOf(sunday)
        );

        Map<String, Map<String, Object>> lookup = new HashMap<>();
        if (assignments != null) {
            for (Map<String, Object> a : assignments) {
                int uId = (int) a.get("userId");
                Date wd = (Date) a.get("workDate");
                if (wd != null) {
                    lookup.put(uId + "_" + wd.toString(), a);
                }
            }
        }

        List<Map<String, Object>> matrix = new ArrayList<>();
        if (staffList != null) {
            for (Map<String, Object> staff : staffList) {
                int uId = (int) staff.get("userId");
                Map<String, Object> row = new HashMap<>(staff);

                List<Map<String, Object>> slots = new ArrayList<>();
                for (int i = 0; i < 7; i++) {
                    LocalDate d = monday.plusDays(i);
                    String key = uId + "_" + d.toString();
                    Map<String, Object> cell = new HashMap<>();
                    cell.put("date", d.toString());
                    cell.put("userId", uId);
                    cell.put("fullName", staff.get("fullName"));
                    cell.put("isToday", d.equals(today));

                    if (lookup.containsKey(key)) {
                        Map<String, Object> a = lookup.get(key);
                        cell.put("hasAssignment", true);
                        cell.put("assignmentId", a.get("assignmentId"));
                        cell.put("shiftId", a.get("shiftId"));
                        cell.put("shiftName", a.get("shiftName"));
                        Time st = (Time) a.get("startTime");
                        Time et = (Time) a.get("endTime");
                        String timeRange = (st != null ? timeFormat.format(st).substring(0, 5) : "--:--")
                                + " - " + (et != null ? timeFormat.format(et).substring(0, 5) : "--:--");
                        cell.put("timeRange", timeRange);
                        cell.put("hasAttendance", a.get("hasAttendance"));
                        cell.put("checkInStatus", a.get("checkInStatus"));
                        cell.put("checkOutStatus", a.get("checkOutStatus"));
                        cell.put("workingHours", a.get("workingHours"));
                        cell.put("isLocked", a.get("hasAttendance") != null && (boolean) a.get("hasAttendance"));
                    } else {
                        cell.put("hasAssignment", false);
                    }
                    slots.add(cell);
                }
                row.put("slots", slots);
                matrix.add(row);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("monday", monday.toString());
        result.put("sunday", sunday.toString());
        result.put("weekLabel", monday.getDayOfMonth() + "/" + monday.getMonthValue() + " - " + sunday.getDayOfMonth() + "/" + sunday.getMonthValue() + "/" + sunday.getYear());
        result.put("prevWeekDate", monday.minusWeeks(1).toString());
        result.put("nextWeekDate", monday.plusWeeks(1).toString());
        result.put("currentWeekDate", today.toString());
        result.put("days", daysList);
        result.put("matrix", matrix);
        return result;
    }

    public boolean deleteAssignment(int assignmentId) throws ValidationException {
        if (assignmentId <= 0) {
            throw new ValidationException(Constants.ERROR_CANNOT_DELETE_ASSIGNMENT);
        }
        if (attendanceDAO.isAssignmentCheckedIn(assignmentId)) {
            throw new ValidationException(Constants.ERROR_CANNOT_DELETE_ASSIGNMENT);
        }
        return attendanceDAO.deleteShiftAssignment(assignmentId);
    }

    public boolean approveAttendance(int attendanceId, int managerUserId, String type) throws ValidationException {
        if (attendanceId <= 0) {
            throw new ValidationException(Constants.ERROR_ATTENDANCE_NOT_FOUND);
        }
        Attendance att = attendanceDAO.getAttendanceById(attendanceId);
        if (att == null) {
            throw new ValidationException(Constants.ERROR_ATTENDANCE_NOT_FOUND);
        }

        String checkInStatus = att.getCheckInStatus();
        String checkOutStatus = att.getCheckOutStatus();

        if ("ot".equalsIgnoreCase(type)) {
            checkOutStatus = Constants.ATTENDANCE_STATUS_APPROVED;
        } else if ("early".equalsIgnoreCase(type)) {
            checkInStatus = Constants.ATTENDANCE_STATUS_APPROVED;
        }

        boolean updated = attendanceDAO.updateAttendanceApproval(attendanceId, checkInStatus, checkOutStatus, managerUserId);
        if (updated && att.getShiftAssignment() != null) {
            try {
                String typeLabel = "ot".equalsIgnoreCase(type) ? "tăng ca (OT)" : "vào ca sớm";
                notificationService.createNotification(
                        att.getShiftAssignment().getUserId(),
                        "AttendanceApproval",
                        attendanceId,
                        "Quản lý đã phê duyệt yêu cầu " + typeLabel + " của bạn."
                );
            } catch (Exception ignored) {
            }
        }
        return updated;
    }

    public boolean rejectAttendance(int attendanceId, int managerUserId, String type) throws ValidationException {
        if (attendanceId <= 0) {
            throw new ValidationException(Constants.ERROR_ATTENDANCE_NOT_FOUND);
        }
        Attendance att = attendanceDAO.getAttendanceById(attendanceId);
        if (att == null) {
            throw new ValidationException(Constants.ERROR_ATTENDANCE_NOT_FOUND);
        }

        String checkInStatus = att.getCheckInStatus();
        String checkOutStatus = att.getCheckOutStatus();

        if ("ot".equalsIgnoreCase(type)) {
            checkOutStatus = Constants.ATTENDANCE_STATUS_REJECTED;
        } else if ("early".equalsIgnoreCase(type)) {
            checkInStatus = Constants.ATTENDANCE_STATUS_REJECTED;
        }

        boolean updated = attendanceDAO.updateAttendanceApproval(attendanceId, checkInStatus, checkOutStatus, managerUserId);
        if (updated && att.getShiftAssignment() != null) {
            try {
                String typeLabel = "ot".equalsIgnoreCase(type) ? "tăng ca (OT)" : "vào ca sớm";
                notificationService.createNotification(
                        att.getShiftAssignment().getUserId(),
                        "AttendanceApproval",
                        attendanceId,
                        "Quản lý đã từ chối yêu cầu " + typeLabel + " của bạn."
                );
            } catch (Exception ignored) {
            }
        }
        return updated;
    }

    public List<Map<String, Object>> getShiftsForManagementView() throws ValidationException {
        List<Shift> shifts = attendanceDAO.getAllShifts();
        List<Map<String, Object>> list = new ArrayList<>();
        if (shifts != null) {
            for (Shift s : shifts) {
                Map<String, Object> map = new HashMap<>();
                map.put("shiftId", s.getShiftId());
                map.put("shiftName", s.getShiftName());
                String startStr = s.getStartTime() != null ? timeFormat.format(s.getStartTime()).substring(0, 5) : "--:--";
                String endStr = s.getEndTime() != null ? timeFormat.format(s.getEndTime()).substring(0, 5) : "--:--";
                map.put("startTime", startStr);
                map.put("endTime", endStr);
                map.put("timeRange", startStr + " - " + endStr);
                map.put("isAssigned", attendanceDAO.isShiftAssigned(s.getShiftId()));
                list.add(map);
            }
        }
        return list;
    }

    public List<Map<String, Object>> getDailyAssignmentsForView(LocalDate date) throws ValidationException {
        if (date == null) {
            date = LocalDate.now();
        }
        List<Map<String, Object>> rawList = attendanceDAO.getAssignmentsWithAttendance(Date.valueOf(date));
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map<String, Object> raw : rawList) {
            Map<String, Object> map = new HashMap<>(raw);
            Time st = (Time) raw.get("startTime");
            Time et = (Time) raw.get("endTime");
            String startStr = st != null ? timeFormat.format(st).substring(0, 5) : "--:--";
            String endStr = et != null ? timeFormat.format(et).substring(0, 5) : "--:--";
            map.put("timeRange", startStr + " - " + endStr);

            Timestamp inTime = (Timestamp) raw.get("checkInTime");
            Timestamp outTime = (Timestamp) raw.get("checkOutTime");
            map.put("checkInTimeStr", inTime != null ? timeFormat.format(inTime) : "--:--:--");
            map.put("checkOutTimeStr", outTime != null ? timeFormat.format(outTime) : "--:--:--");
            list.add(map);
        }
        return list;
    }

    public List<Map<String, Object>> getMonthlyTimesheetSummaryForView(int month, int year) throws ValidationException {
        return attendanceDAO.getMonthlyTimesheetSummary(month, year);
    }

    public List<Map<String, Object>> getPendingApprovalsForView() throws ValidationException {
        List<Map<String, Object>> rawList = attendanceDAO.getPendingApprovals();
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map<String, Object> raw : rawList) {
            Map<String, Object> map = new HashMap<>(raw);
            Timestamp inTime = (Timestamp) raw.get("checkInTime");
            Timestamp outTime = (Timestamp) raw.get("checkOutTime");
            map.put("checkInTimeStr", inTime != null ? timeFormat.format(inTime) : "--:--:--");
            map.put("checkOutTimeStr", outTime != null ? timeFormat.format(outTime) : "--:--:--");
            list.add(map);
        }
        return list;
    }

    public List<Map<String, Object>> getStaffListForView() throws ValidationException {
        return attendanceDAO.getActiveStaffUsers();
    }

    private void validateNotYetCheckedIn(int assignmentId) throws ValidationException {
        Attendance existing = attendanceDAO.getAttendanceByAssignmentId(assignmentId);
        if (existing != null) {
            throw new ValidationException(Constants.ERROR_ATTENDANCE_ALREADY_CHECKED_IN);
        }
    }

    private Attendance validateAndGetActiveAttendance(int assignmentId) throws ValidationException {
        Attendance existing = attendanceDAO.getAttendanceByAssignmentId(assignmentId);
        if (existing == null) {
            throw new ValidationException(Constants.ERROR_ATTENDANCE_NOT_CHECKED_IN);
        }
        if (existing.getCheckOutTime() != null) {
            throw new ValidationException(Constants.ERROR_ATTENDANCE_ALREADY_CHECKED_OUT);
        }
        return existing;
    }

    private void validateNoPendingCounterOrders(int staffId) throws ValidationException {
        if (attendanceDAO.hasPendingCounterOrders(staffId)) {
            throw new ValidationException(Constants.ERROR_ATTENDANCE_PENDING_ORDERS);
        }
    }

    private String determineCheckInStatus(Timestamp now, Time shiftStartTime) {
        LocalTime currentTime = now.toLocalDateTime().toLocalTime();
        LocalTime startTime = shiftStartTime.toLocalTime();

        long diffMinutes = java.time.Duration.between(startTime, currentTime).toMinutes();

        if (diffMinutes < -EARLY_CHECKIN_THRESHOLD_MINUTES) {
            return "Pending Early Approval";
        } else if (diffMinutes > LATE_CHECKIN_THRESHOLD_MINUTES) {
            return "Late";
        } else {
            return "On Time";
        }
    }

    private boolean isCurrentTimeExceedingOvertime(Timestamp now, Time shiftEndTime) {
        LocalTime currentTime = now.toLocalDateTime().toLocalTime();
        LocalTime endTime = shiftEndTime.toLocalTime();
        long diffMinutes = java.time.Duration.between(endTime, currentTime).toMinutes();
        return diffMinutes > OVERTIME_THRESHOLD_MINUTES;
    }

    private String determineCheckOutStatus(Timestamp now, Time shiftEndTime, boolean isOvertime) {
        if (isOvertime) {
            return "Pending OT Approval";
        }

        LocalTime currentTime = now.toLocalDateTime().toLocalTime();
        LocalTime endTime = shiftEndTime.toLocalTime();
        long diffMinutes = java.time.Duration.between(endTime, currentTime).toMinutes();

        if (diffMinutes < -EARLY_CHECKOUT_THRESHOLD_MINUTES) {
            return "Left Early";
        }
        return "On Time";
    }

    private double calculateWorkingHours(Timestamp checkInTime, Timestamp checkOutTime) {
        long diffMillis = checkOutTime.getTime() - checkInTime.getTime();
        double hours = diffMillis / (1000.0 * 60.0 * 60.0);
        return Math.round(hours * 100.0) / 100.0;
    }
}
