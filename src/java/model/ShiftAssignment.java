package model;

import java.sql.Date;

/**
 * Entity đại diện cho bảng ShiftAssignments trong CSDL
 */
public class ShiftAssignment {

    private int assignmentId;
    private int userId;
    private int shiftId;
    private Date workDate;
    private Shift shift;

    public ShiftAssignment() {
    }

    public ShiftAssignment(int assignmentId, int userId, int shiftId, Date workDate) {
        this.assignmentId = assignmentId;
        this.userId = userId;
        this.shiftId = shiftId;
        this.workDate = workDate;
    }

    public int getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(int assignmentId) {
        this.assignmentId = assignmentId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getShiftId() {
        return shiftId;
    }

    public void setShiftId(int shiftId) {
        this.shiftId = shiftId;
    }

    public Date getWorkDate() {
        return workDate;
    }

    public void setWorkDate(Date workDate) {
        this.workDate = workDate;
    }

    public Shift getShift() {
        return shift;
    }

    public void setShift(Shift shift) {
        this.shift = shift;
    }
}
