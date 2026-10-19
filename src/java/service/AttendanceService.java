package service;

import dao.AttendanceDAO;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.Attendance;
import model.Shift;
import model.ShiftAssignment;
import utility.Constants;
import utility.ValidationException;

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
    private SimpleDateFormat timeFormat;
    private SimpleDateFormat dateFormat;
    private SimpleDateFormat dateTimeFormat;

    public AttendanceService() {
        this.attendanceDAO = new AttendanceDAO();
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

    /**
     * Tự động gán ca mẫu hôm nay cho nhân viên nếu chưa có ca (hỗ trợ kiểm thử trực tiếp)
     */
    public void assignDefaultShiftIfNone(int userId) throws ValidationException {
        assignDefaultShiftIfNone(userId, 0);
    }

    public void assignDefaultShiftIfNone(int userId, int shiftId) throws ValidationException {
        List<Shift> shifts = attendanceDAO.getAllShifts();
        if (shifts != null && !shifts.isEmpty()) {
            int selectedShiftId = shifts.get(0).getShiftId();
            if (shiftId > 0) {
                for (Shift s : shifts) {
                    if (s.getShiftId() == shiftId) {
                        selectedShiftId = shiftId;
                        break;
                    }
                }
            }
            attendanceDAO.assignShiftIfNone(userId, selectedShiftId);
        }
    }

    public void resetTodayShiftForTest(int userId) throws ValidationException {
        attendanceDAO.resetTodayShiftForTest(userId);
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
