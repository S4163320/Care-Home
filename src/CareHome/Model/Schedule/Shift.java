package CareHome.Model.Schedule;

import CareHome.Model.ShiftType;
import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class Shift {
    private String shiftId;        // Unique identifier for this shift
    private DayOfWeek dayOfWeek;   // Which day of the week (MONDAY, TUESDAY, etc.)
    private ShiftType shiftType;   // Type of shift (MORNING_NURSE, AFTERNOON_NURSE, DOCTOR_ROUND)
    private String assignedStaffId; // ID of staff member assigned to this shift
    private boolean isAssigned;    // Whether someone is assigned to this shift

    // Constructor for new shift
    public Shift(String shiftId, DayOfWeek dayOfWeek, ShiftType shiftType) {
        this.shiftId = shiftId;
        this.dayOfWeek = dayOfWeek;
        this.shiftType = shiftType;
        this.assignedStaffId = null;
        this.isAssigned = false;
    }

    // Assign staff member to this shift
    public void assignStaff(String staffId) {
        this.assignedStaffId = staffId;
        this.isAssigned = true;
    }

    // Remove staff assignment from this shift
    public void unassignStaff() {
        this.assignedStaffId = null;
        this.isAssigned = false;
    }

    // Check if this shift overlaps with another shift (same day, overlapping times)
    public boolean overlapsWith(Shift otherShift) {
        if (!this.dayOfWeek.equals(otherShift.dayOfWeek)) {
            return false; // Different days don't overlap
        }

        // Check time overlap
        int thisStart = this.shiftType.getStartHour();
        int thisEnd = this.shiftType.getEndHour();
        int otherStart = otherShift.shiftType.getStartHour();
        int otherEnd = otherShift.shiftType.getEndHour();

        return thisStart < otherEnd && otherStart < thisEnd;
    }

    // Check if this shift is currently active
    public boolean isCurrentlyActive() {
        LocalDateTime now = LocalDateTime.now();
        DayOfWeek today = now.getDayOfWeek();
        int currentHour = now.getHour();

        return this.dayOfWeek.equals(today) &&
                currentHour >= shiftType.getStartHour() &&
                currentHour < shiftType.getEndHour();
    }

    // Getters
    public String getShiftId() { return shiftId; }
    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public ShiftType getShiftType() { return shiftType; }
    public String getAssignedStaffId() {
        return assignedStaffId;
    }

    public String getStaffId() { // Add this getter for the TableView
        return assignedStaffId;
    }

    public boolean isAssigned() { return isAssigned; }
    public int getDurationHours() { return shiftType.getDurationHours(); }

    @Override
    public String toString() {
        return shiftType + " on " + dayOfWeek +
                (isAssigned ? " (Assigned to: " + assignedStaffId + ")" : " (Unassigned)");
    }
}