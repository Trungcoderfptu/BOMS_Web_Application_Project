package model;

import java.sql.Timestamp;

/**
 * Entity đại diện cho bảng Attendances trong CSDL
 */
public class Attendance {

    private int attendanceId;
    private int assignmentId;
    private Timestamp checkInTime;
    private String checkInStatus;
    private Timestamp checkOutTime;
    private String checkOutStatus;
    private Double workingHours;
    private String otReason;
    private Integer approvedBy;
    private ShiftAssignment shiftAssignment;

    public Attendance() {
    }

    public Attendance(int attendanceId, int assignmentId, Timestamp checkInTime, String checkInStatus) {
        this.attendanceId = attendanceId;
        this.assignmentId = assignmentId;
        this.checkInTime = checkInTime;
        this.checkInStatus = checkInStatus;
    }

    public int getAttendanceId() {
        return attendanceId;
    }

    public void setAttendanceId(int attendanceId) {
        this.attendanceId = attendanceId;
    }

    public int getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(int assignmentId) {
        this.assignmentId = assignmentId;
    }

    public Timestamp getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(Timestamp checkInTime) {
        this.checkInTime = checkInTime;
    }

    public String getCheckInStatus() {
        return checkInStatus;
    }

    public void setCheckInStatus(String checkInStatus) {
        this.checkInStatus = checkInStatus;
    }

    public Timestamp getCheckOutTime() {
        return checkOutTime;
    }

    public void setCheckOutTime(Timestamp checkOutTime) {
        this.checkOutTime = checkOutTime;
    }

    public String getCheckOutStatus() {
        return checkOutStatus;
    }

    public void setCheckOutStatus(String checkOutStatus) {
        this.checkOutStatus = checkOutStatus;
    }

    public Double getWorkingHours() {
        return workingHours;
    }

    public void setWorkingHours(Double workingHours) {
        this.workingHours = workingHours;
    }

    public String getOtReason() {
        return otReason;
    }

    public void setOtReason(String otReason) {
        this.otReason = otReason;
    }

    public Integer getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(Integer approvedBy) {
        this.approvedBy = approvedBy;
    }

    public ShiftAssignment getShiftAssignment() {
        return shiftAssignment;
    }

    public void setShiftAssignment(ShiftAssignment shiftAssignment) {
        this.shiftAssignment = shiftAssignment;
    }
}
