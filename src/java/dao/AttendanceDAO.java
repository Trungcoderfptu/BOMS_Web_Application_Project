package dao;

import dal.DBContext;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
 * DAO xử lý truy xuất CSDL cho Phân hệ Chấm công (Shifts, ShiftAssignments, Attendances)
 */
public class AttendanceDAO extends DBContext {

    public ShiftAssignment getTodayShiftAssignment(int userId) throws ValidationException {
        String sql = "SELECT sa.AssignmentID, sa.UserID, sa.ShiftID, sa.WorkDate, "
                + "s.ShiftName, s.StartTime, s.EndTime "
                + "FROM ShiftAssignments sa "
                + "JOIN Shifts s ON sa.ShiftID = s.ShiftID "
                + "WHERE sa.UserID = ? AND sa.WorkDate = CAST(GETDATE() AS DATE)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToShiftAssignment(rs);
                }
            }
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_GET_ATTENDANCE);
        }
        return null;
    }

    public Attendance getAttendanceByAssignmentId(int assignmentId) throws ValidationException {
        String sql = "SELECT a.AttendanceID, a.AssignmentID, a.CheckInTime, a.CheckInStatus, "
                + "a.CheckOutTime, a.CheckOutStatus, a.WorkingHours, a.OTReason, a.ApprovedBy, "
                + "sa.UserID, sa.ShiftID, sa.WorkDate, "
                + "s.ShiftName, s.StartTime, s.EndTime "
                + "FROM Attendances a "
                + "JOIN ShiftAssignments sa ON a.AssignmentID = sa.AssignmentID "
                + "JOIN Shifts s ON sa.ShiftID = s.ShiftID "
                + "WHERE a.AssignmentID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, assignmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAttendance(rs);
                }
            }
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_GET_ATTENDANCE);
        }
        return null;
    }

    public int insertCheckIn(Attendance attendance) throws ValidationException {
        String sql = "INSERT INTO Attendances (AssignmentID, CheckInTime, CheckInStatus) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, attendance.getAssignmentId());
            ps.setTimestamp(2, attendance.getCheckInTime());
            ps.setString(3, attendance.getCheckInStatus());

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
            throw new ValidationException(Constants.ERROR_DATABASE_INSERT_ATTENDANCE);
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_INSERT_ATTENDANCE);
        }
    }

    public boolean updateCheckOut(Attendance attendance) throws ValidationException {
        String sql = "UPDATE Attendances "
                + "SET CheckOutTime = ?, CheckOutStatus = ?, WorkingHours = ?, OTReason = ? "
                + "WHERE AttendanceID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setTimestamp(1, attendance.getCheckOutTime());
            ps.setString(2, attendance.getCheckOutStatus());
            if (attendance.getWorkingHours() != null) {
                ps.setDouble(3, attendance.getWorkingHours());
            } else {
                ps.setNull(3, java.sql.Types.DECIMAL);
            }
            ps.setString(4, attendance.getOtReason());
            ps.setInt(5, attendance.getAttendanceId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_UPDATE_ATTENDANCE);
        }
    }

    public List<Attendance> getAttendanceHistoryByUserId(int userId, int month, int year) throws ValidationException {
        List<Attendance> list = new ArrayList<>();
        String sql = "SELECT a.AttendanceID, a.AssignmentID, a.CheckInTime, a.CheckInStatus, "
                + "a.CheckOutTime, a.CheckOutStatus, a.WorkingHours, a.OTReason, a.ApprovedBy, "
                + "sa.UserID, sa.ShiftID, sa.WorkDate, "
                + "s.ShiftName, s.StartTime, s.EndTime "
                + "FROM Attendances a "
                + "JOIN ShiftAssignments sa ON a.AssignmentID = sa.AssignmentID "
                + "JOIN Shifts s ON sa.ShiftID = s.ShiftID "
                + "WHERE sa.UserID = ? AND MONTH(sa.WorkDate) = ? AND YEAR(sa.WorkDate) = ? "
                + "ORDER BY sa.WorkDate DESC, a.CheckInTime DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, month);
            ps.setInt(3, year);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAttendance(rs));
                }
            }
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_GET_ATTENDANCE);
        }
        return list;
    }

    public boolean hasPendingCounterOrders(int staffId) throws ValidationException {
        String sql = "SELECT COUNT(1) FROM Orders "
                + "WHERE StaffID = ? AND OrderType = 'Counter' "
                + "AND Status IN ('Accepted', 'Prepared') "
                + "AND CAST(CreatedAt AS DATE) = CAST(GETDATE() AS DATE)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, staffId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_GET_ORDER);
        }
        return false;
    }

    public List<Shift> getAllShifts() throws ValidationException {
        List<Shift> list = new ArrayList<>();
        String sql = "SELECT ShiftID, ShiftName, StartTime, EndTime FROM Shifts ORDER BY StartTime";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Shift(
                        rs.getInt("ShiftID"),
                        rs.getNString("ShiftName"),
                        rs.getTime("StartTime"),
                        rs.getTime("EndTime")
                ));
            }
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_GET_ATTENDANCE);
        }
        return list;
    }

    public Shift getShiftById(int shiftId) throws ValidationException {
        String sql = "SELECT ShiftID, ShiftName, StartTime, EndTime FROM Shifts WHERE ShiftID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, shiftId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Shift(
                            rs.getInt("ShiftID"),
                            rs.getNString("ShiftName"),
                            rs.getTime("StartTime"),
                            rs.getTime("EndTime")
                    );
                }
            }
            return null;
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_GET_ATTENDANCE);
        }
    }

    public boolean isShiftNameExists(String shiftName, int excludeShiftId) throws ValidationException {
        String sql = "SELECT COUNT(1) FROM Shifts WHERE LOWER(ShiftName) = LOWER(?) AND ShiftID != ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setNString(1, shiftName.trim());
            ps.setInt(2, excludeShiftId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            return false;
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_GET_ATTENDANCE);
        }
    }

    public int insertShift(Shift shift) throws ValidationException {
        String sql = "INSERT INTO Shifts (ShiftName, StartTime, EndTime) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setNString(1, shift.getShiftName());
            ps.setTime(2, shift.getStartTime());
            ps.setTime(3, shift.getEndTime());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return 0;
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_INSERT_SHIFT);
        }
    }

    public boolean updateShift(Shift shift) throws ValidationException {
        String sql = "UPDATE Shifts SET ShiftName = ?, StartTime = ?, EndTime = ? WHERE ShiftID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setNString(1, shift.getShiftName());
            ps.setTime(2, shift.getStartTime());
            ps.setTime(3, shift.getEndTime());
            ps.setInt(4, shift.getShiftId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_UPDATE_SHIFT);
        }
    }

    public boolean isShiftAssigned(int shiftId) throws ValidationException {
        String sql = "SELECT COUNT(1) FROM ShiftAssignments WHERE ShiftID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, shiftId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            return false;
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_GET_ATTENDANCE);
        }
    }

    public int insertShiftAssignment(ShiftAssignment sa) throws ValidationException {
        String sql = "INSERT INTO ShiftAssignments (UserID, ShiftID, WorkDate) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, sa.getUserId());
            ps.setInt(2, sa.getShiftId());
            ps.setDate(3, sa.getWorkDate());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return 0;
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_ASSIGN_SHIFT);
        }
    }

    public boolean hasAssignmentOnDate(int userId, Date workDate) throws ValidationException {
        String sql = "SELECT COUNT(1) FROM ShiftAssignments WHERE UserID = ? AND WorkDate = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setDate(2, workDate);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            return false;
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_GET_ATTENDANCE);
        }
    }

    /**
     * Kiểm tra xem việc gán ca mới (shiftId) cho nhân viên (userId) vào ngày (workDate)
     * có bị trùng/giao thoa thời gian (Time Overlap) với bất kỳ ca nào đã phân công hay không.
     * Thuật toán: s_existing.StartTime < s_new.EndTime AND s_existing.EndTime > s_new.StartTime
     */
    public boolean hasOverlappingShiftAssignment(int userId, int shiftId, Date workDate) throws ValidationException {
        String sql = "SELECT COUNT(1) "
                + "FROM ShiftAssignments sa "
                + "JOIN Shifts s_existing ON sa.ShiftID = s_existing.ShiftID "
                + "JOIN Shifts s_new ON s_new.ShiftID = ? "
                + "WHERE sa.UserID = ? "
                + "  AND sa.WorkDate = ? "
                + "  AND s_existing.StartTime < s_new.EndTime "
                + "  AND s_existing.EndTime > s_new.StartTime";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, shiftId);
            ps.setInt(2, userId);
            ps.setDate(3, workDate);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            return false;
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_GET_ATTENDANCE);
        }
    }

    public boolean isAssignmentCheckedIn(int assignmentId) throws ValidationException {
        String sql = "SELECT COUNT(1) FROM Attendances WHERE AssignmentID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, assignmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            return false;
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_GET_ATTENDANCE);
        }
    }

    public boolean deleteShiftAssignment(int assignmentId) throws ValidationException {
        String sql = "DELETE FROM ShiftAssignments WHERE AssignmentID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, assignmentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_DELETE_ASSIGNMENT);
        }
    }

    public boolean updateAttendanceApproval(int attendanceId, String checkInStatus, String checkOutStatus, int approvedBy) throws ValidationException {
        String sql = "UPDATE Attendances SET CheckInStatus = ?, CheckOutStatus = ?, ApprovedBy = ? WHERE AttendanceID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, checkInStatus);
            ps.setString(2, checkOutStatus);
            ps.setInt(3, approvedBy);
            ps.setInt(4, attendanceId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_APPROVE_ATTENDANCE);
        }
    }

    public Attendance getAttendanceById(int attendanceId) throws ValidationException {
        String sql = "SELECT a.AttendanceID, a.AssignmentID, a.CheckInTime, a.CheckInStatus, "
                + "a.CheckOutTime, a.CheckOutStatus, a.WorkingHours, a.OTReason, a.ApprovedBy, "
                + "sa.UserID, sa.ShiftID, sa.WorkDate, s.ShiftName, s.StartTime, s.EndTime "
                + "FROM Attendances a "
                + "JOIN ShiftAssignments sa ON a.AssignmentID = sa.AssignmentID "
                + "JOIN Shifts s ON sa.ShiftID = s.ShiftID "
                + "WHERE a.AttendanceID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, attendanceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAttendance(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_GET_ATTENDANCE);
        }
    }

    public List<Map<String, Object>> getAssignmentsWithAttendance(Date workDate) throws ValidationException {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT sa.AssignmentID, sa.UserID, u.FullName, u.Role, "
                + "sa.ShiftID, s.ShiftName, s.StartTime, s.EndTime, sa.WorkDate, "
                + "a.AttendanceID, a.CheckInTime, a.CheckInStatus, a.CheckOutTime, a.CheckOutStatus, "
                + "a.WorkingHours, a.OTReason, a.ApprovedBy, approver.FullName AS ApproverName "
                + "FROM ShiftAssignments sa "
                + "JOIN Users u ON sa.UserID = u.UserID "
                + "JOIN Shifts s ON sa.ShiftID = s.ShiftID "
                + "LEFT JOIN Attendances a ON sa.AssignmentID = a.AssignmentID "
                + "LEFT JOIN Users approver ON a.ApprovedBy = approver.UserID "
                + "WHERE sa.WorkDate = ? "
                + "ORDER BY s.StartTime ASC, u.FullName ASC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDate(1, workDate);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("assignmentId", rs.getInt("AssignmentID"));
                    map.put("userId", rs.getInt("UserID"));
                    map.put("fullName", rs.getNString("FullName"));
                    map.put("role", rs.getString("Role"));
                    map.put("shiftId", rs.getInt("ShiftID"));
                    map.put("shiftName", rs.getNString("ShiftName"));
                    map.put("startTime", rs.getTime("StartTime"));
                    map.put("endTime", rs.getTime("EndTime"));
                    map.put("workDate", rs.getDate("WorkDate"));
                    int attId = rs.getInt("AttendanceID");
                    boolean hasAtt = !rs.wasNull() && attId > 0;
                    map.put("attendanceId", hasAtt ? attId : null);
                    map.put("hasAttendance", hasAtt);
                    map.put("checkInTime", rs.getTimestamp("CheckInTime"));
                    map.put("checkInStatus", rs.getString("CheckInStatus"));
                    map.put("checkOutTime", rs.getTimestamp("CheckOutTime"));
                    map.put("checkOutStatus", rs.getString("CheckOutStatus"));
                    double hours = rs.getDouble("WorkingHours");
                    map.put("workingHours", rs.wasNull() ? null : hours);
                    map.put("otReason", rs.getNString("OTReason"));
                    int approvedBy = rs.getInt("ApprovedBy");
                    map.put("approvedBy", rs.wasNull() ? null : approvedBy);
                    map.put("approverName", rs.getNString("ApproverName"));
                    list.add(map);
                }
            }
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_GET_ATTENDANCE);
        }
        return list;
    }

    public List<Map<String, Object>> getMonthlyTimesheetSummary(int month, int year) throws ValidationException {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT u.UserID, u.FullName, u.Role, "
                + "COUNT(sa.AssignmentID) AS TotalAssignedShifts, "
                + "COUNT(CASE WHEN (a.AttendanceID IS NOT NULL AND a.CheckInStatus != 'Pending Early Approval' AND a.CheckOutStatus != 'Pending OT Approval') OR a.ApprovedBy IS NOT NULL THEN a.AttendanceID END) AS TotalAttendedShifts, "
                + "ISNULL(SUM(CASE WHEN (a.AttendanceID IS NOT NULL AND a.CheckInStatus != 'Pending Early Approval' AND a.CheckOutStatus != 'Pending OT Approval') OR a.ApprovedBy IS NOT NULL THEN a.WorkingHours ELSE 0 END), 0) AS TotalWorkingHours, "
                + "SUM(CASE WHEN a.CheckInStatus = 'On Time' OR (a.CheckInStatus = 'Approved' AND a.ApprovedBy IS NOT NULL) THEN 1 ELSE 0 END) AS OnTimeCount, "
                + "SUM(CASE WHEN a.CheckInStatus = 'Late' THEN 1 ELSE 0 END) AS LateCount, "
                + "SUM(CASE WHEN (a.CheckOutStatus = 'Approved' AND a.ApprovedBy IS NOT NULL) OR a.CheckOutStatus = 'Pending OT Approval' THEN 1 ELSE 0 END) AS OtCount "
                + "FROM Users u "
                + "JOIN ShiftAssignments sa ON u.UserID = sa.UserID "
                + "LEFT JOIN Attendances a ON sa.AssignmentID = a.AssignmentID "
                + "WHERE MONTH(sa.WorkDate) = ? AND YEAR(sa.WorkDate) = ? "
                + "GROUP BY u.UserID, u.FullName, u.Role "
                + "ORDER BY u.FullName ASC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, month);
            ps.setInt(2, year);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("userId", rs.getInt("UserID"));
                    map.put("fullName", rs.getNString("FullName"));
                    map.put("role", rs.getString("Role"));
                    map.put("totalAssignedShifts", rs.getInt("TotalAssignedShifts"));
                    map.put("totalAttendedShifts", rs.getInt("TotalAttendedShifts"));
                    map.put("totalWorkingHours", rs.getDouble("TotalWorkingHours"));
                    map.put("onTimeCount", rs.getInt("OnTimeCount"));
                    map.put("lateCount", rs.getInt("LateCount"));
                    map.put("otCount", rs.getInt("OtCount"));
                    list.add(map);
                }
            }
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_GET_ATTENDANCE);
        }
        return list;
    }

    public List<Map<String, Object>> getPendingApprovals() throws ValidationException {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT a.AttendanceID, sa.AssignmentID, u.FullName, u.Role, s.ShiftName, sa.WorkDate, "
                + "a.CheckInTime, a.CheckInStatus, a.CheckOutTime, a.CheckOutStatus, a.WorkingHours, a.OTReason "
                + "FROM Attendances a "
                + "JOIN ShiftAssignments sa ON a.AssignmentID = sa.AssignmentID "
                + "JOIN Users u ON sa.UserID = u.UserID "
                + "JOIN Shifts s ON sa.ShiftID = s.ShiftID "
                + "WHERE a.CheckInStatus = 'Pending Early Approval' OR a.CheckOutStatus = 'Pending OT Approval' "
                + "ORDER BY sa.WorkDate DESC, a.CheckInTime DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("attendanceId", rs.getInt("AttendanceID"));
                map.put("assignmentId", rs.getInt("AssignmentID"));
                map.put("fullName", rs.getNString("FullName"));
                map.put("role", rs.getString("Role"));
                map.put("shiftName", rs.getNString("ShiftName"));
                map.put("workDate", rs.getDate("WorkDate"));
                map.put("checkInTime", rs.getTimestamp("CheckInTime"));
                map.put("checkInStatus", rs.getString("CheckInStatus"));
                map.put("checkOutTime", rs.getTimestamp("CheckOutTime"));
                map.put("checkOutStatus", rs.getString("CheckOutStatus"));
                double hours = rs.getDouble("WorkingHours");
                map.put("workingHours", rs.wasNull() ? null : hours);
                map.put("otReason", rs.getNString("OTReason"));
                list.add(map);
            }
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_GET_ATTENDANCE);
        }
        return list;
    }

    public List<Map<String, Object>> getActiveStaffUsers() throws ValidationException {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT UserID, FullName, Role, Username FROM Users "
                + "WHERE Role = 'Staff' AND IsActive = 1 "
                + "ORDER BY FullName ASC";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("userId", rs.getInt("UserID"));
                map.put("fullName", rs.getNString("FullName"));
                map.put("role", rs.getString("Role"));
                map.put("username", rs.getString("Username"));
                list.add(map);
            }
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_GET_ATTENDANCE);
        }
        return list;
    }

    public List<Map<String, Object>> getAssignmentsByUserAndDateRange(int userId, Date startDate, Date endDate) throws ValidationException {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT sa.AssignmentID, sa.UserID, sa.ShiftID, s.ShiftName, s.StartTime, s.EndTime, sa.WorkDate, "
                + "a.AttendanceID, a.CheckInTime, a.CheckInStatus, a.CheckOutTime, a.CheckOutStatus, a.WorkingHours, a.OTReason, a.ApprovedBy "
                + "FROM ShiftAssignments sa "
                + "JOIN Shifts s ON sa.ShiftID = s.ShiftID "
                + "LEFT JOIN Attendances a ON sa.AssignmentID = a.AssignmentID "
                + "WHERE sa.UserID = ? AND sa.WorkDate BETWEEN ? AND ? "
                + "ORDER BY sa.WorkDate ASC, s.StartTime ASC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setDate(2, startDate);
            ps.setDate(3, endDate);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("assignmentId", rs.getInt("AssignmentID"));
                    map.put("userId", rs.getInt("UserID"));
                    map.put("shiftId", rs.getInt("ShiftID"));
                    map.put("shiftName", rs.getNString("ShiftName"));
                    map.put("startTime", rs.getTime("StartTime"));
                    map.put("endTime", rs.getTime("EndTime"));
                    map.put("workDate", rs.getDate("WorkDate"));
                    int attId = rs.getInt("AttendanceID");
                    boolean hasAtt = !rs.wasNull() && attId > 0;
                    map.put("attendanceId", hasAtt ? attId : null);
                    map.put("hasAttendance", hasAtt);
                    map.put("checkInTime", rs.getTimestamp("CheckInTime"));
                    map.put("checkInStatus", rs.getString("CheckInStatus"));
                    map.put("checkOutTime", rs.getTimestamp("CheckOutTime"));
                    map.put("checkOutStatus", rs.getString("CheckOutStatus"));
                    double hours = rs.getDouble("WorkingHours");
                    map.put("workingHours", rs.wasNull() ? null : hours);
                    map.put("otReason", rs.getNString("OTReason"));
                    int approvedBy = rs.getInt("ApprovedBy");
                    map.put("approvedBy", rs.wasNull() ? null : approvedBy);
                    list.add(map);
                }
            }
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_GET_ATTENDANCE);
        }
        return list;
    }

    public List<Map<String, Object>> getAssignmentsByDateRange(Date startDate, Date endDate) throws ValidationException {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT sa.AssignmentID, sa.UserID, u.FullName, u.Role, "
                + "sa.ShiftID, s.ShiftName, s.StartTime, s.EndTime, sa.WorkDate, "
                + "a.AttendanceID, a.CheckInTime, a.CheckInStatus, a.CheckOutTime, a.CheckOutStatus, "
                + "a.WorkingHours, a.OTReason, a.ApprovedBy "
                + "FROM ShiftAssignments sa "
                + "JOIN Users u ON sa.UserID = u.UserID "
                + "JOIN Shifts s ON sa.ShiftID = s.ShiftID "
                + "LEFT JOIN Attendances a ON sa.AssignmentID = a.AssignmentID "
                + "WHERE sa.WorkDate BETWEEN ? AND ? "
                + "ORDER BY u.FullName ASC, sa.WorkDate ASC, s.StartTime ASC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDate(1, startDate);
            ps.setDate(2, endDate);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("assignmentId", rs.getInt("AssignmentID"));
                    map.put("userId", rs.getInt("UserID"));
                    map.put("fullName", rs.getNString("FullName"));
                    map.put("role", rs.getString("Role"));
                    map.put("shiftId", rs.getInt("ShiftID"));
                    map.put("shiftName", rs.getNString("ShiftName"));
                    map.put("startTime", rs.getTime("StartTime"));
                    map.put("endTime", rs.getTime("EndTime"));
                    map.put("workDate", rs.getDate("WorkDate"));
                    int attId = rs.getInt("AttendanceID");
                    boolean hasAtt = !rs.wasNull() && attId > 0;
                    map.put("attendanceId", hasAtt ? attId : null);
                    map.put("hasAttendance", hasAtt);
                    map.put("checkInTime", rs.getTimestamp("CheckInTime"));
                    map.put("checkInStatus", rs.getString("CheckInStatus"));
                    map.put("checkOutTime", rs.getTimestamp("CheckOutTime"));
                    map.put("checkOutStatus", rs.getString("CheckOutStatus"));
                    double hours = rs.getDouble("WorkingHours");
                    map.put("workingHours", rs.wasNull() ? null : hours);
                    map.put("otReason", rs.getNString("OTReason"));
                    int approvedBy = rs.getInt("ApprovedBy");
                    map.put("approvedBy", rs.wasNull() ? null : approvedBy);
                    list.add(map);
                }
            }
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_GET_ATTENDANCE);
        }
        return list;
    }

    private ShiftAssignment mapResultSetToShiftAssignment(ResultSet rs) throws SQLException {
        ShiftAssignment sa = new ShiftAssignment(
                rs.getInt("AssignmentID"),
                rs.getInt("UserID"),
                rs.getInt("ShiftID"),
                rs.getDate("WorkDate")
        );
        Shift s = new Shift(
                rs.getInt("ShiftID"),
                rs.getNString("ShiftName"),
                rs.getTime("StartTime"),
                rs.getTime("EndTime")
        );
        sa.setShift(s);
        return sa;
    }

    private Attendance mapResultSetToAttendance(ResultSet rs) throws SQLException {
        Attendance a = new Attendance();
        a.setAttendanceId(rs.getInt("AttendanceID"));
        a.setAssignmentId(rs.getInt("AssignmentID"));
        a.setCheckInTime(rs.getTimestamp("CheckInTime"));
        a.setCheckInStatus(rs.getString("CheckInStatus"));
        a.setCheckOutTime(rs.getTimestamp("CheckOutTime"));
        a.setCheckOutStatus(rs.getString("CheckOutStatus"));

        double hours = rs.getDouble("WorkingHours");
        if (!rs.wasNull()) {
            a.setWorkingHours(hours);
        }

        a.setOtReason(rs.getNString("OTReason"));

        int approvedBy = rs.getInt("ApprovedBy");
        if (!rs.wasNull()) {
            a.setApprovedBy(approvedBy);
        }

        ShiftAssignment sa = mapResultSetToShiftAssignment(rs);
        a.setShiftAssignment(sa);
        return a;
    }
}
