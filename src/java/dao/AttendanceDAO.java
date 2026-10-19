package dao;

import dal.DBContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
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

    public void assignShiftIfNone(int userId, int shiftId) throws ValidationException {
        String checkSql = "SELECT COUNT(1) FROM ShiftAssignments WHERE UserID = ? AND WorkDate = CAST(GETDATE() AS DATE)";
        String insertSql = "INSERT INTO ShiftAssignments (UserID, ShiftID, WorkDate) VALUES (?, ?, CAST(GETDATE() AS DATE))";
        try (PreparedStatement checkPs = connection.prepareStatement(checkSql)) {
            checkPs.setInt(1, userId);
            try (ResultSet rs = checkPs.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) {
                    try (PreparedStatement insertPs = connection.prepareStatement(insertSql)) {
                        insertPs.setInt(1, userId);
                        insertPs.setInt(2, shiftId);
                        insertPs.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            throw new ValidationException(Constants.ERROR_DATABASE_INSERT_ATTENDANCE);
        }
    }

    public void resetTodayShiftForTest(int userId) throws ValidationException {
        ShiftAssignment sa = getTodayShiftAssignment(userId);
        if (sa != null) {
            String delAttSql = "DELETE FROM Attendances WHERE AssignmentID = ?";
            String delSaSql = "DELETE FROM ShiftAssignments WHERE AssignmentID = ?";
            try (PreparedStatement ps1 = connection.prepareStatement(delAttSql);
                 PreparedStatement ps2 = connection.prepareStatement(delSaSql)) {
                ps1.setInt(1, sa.getAssignmentId());
                ps1.executeUpdate();
                ps2.setInt(1, sa.getAssignmentId());
                ps2.executeUpdate();
            } catch (SQLException e) {
                throw new ValidationException(Constants.ERROR_DATABASE_UPDATE_ATTENDANCE);
            }
        }
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
