package CareHome.controller;

import CareHome.Service.AuthenticationService;
import CareHome.Model.Audit.AuditLogger;
import CareHome.Model.ActionType;
import CareHome.Model.Person.Staff;
import CareHome.Model.Person.Manager;
import CareHome.Model.Person.Doctor;
import CareHome.Model.Person.Nurse;
import CareHome.Model.Schedule.Shift;
import CareHome.Model.ShiftType;
import CareHome.Exception.SchedulingException;
import CareHome.dao.StaffDAO;
import CareHome.dao.StaffDAOImpl;
import CareHome.dao.ShiftDAO;
import CareHome.dao.ShiftDAOImpl;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class ShiftController extends BaseController {
    private List<Shift> allShifts;
    private ShiftDAO shiftDAO;
    private StaffDAO staffDAO;
    private Map<String, List<Shift>> staffSchedules; // StaffId -> List of assigned shifts

    // Initializes shift management with DAOs, in-memory lists, and schedules map
    public ShiftController(AuthenticationService authService, AuditLogger auditLogger) {
        super(authService, auditLogger);
        this.allShifts = new ArrayList<>();
        this.shiftDAO = new ShiftDAOImpl();
        this.staffDAO = new StaffDAOImpl();
        this.staffSchedules = new HashMap<>();
    }

    //Assign shift to staff member with comprehensive validation
    public void assignShift(String staffId, DayOfWeek day, String shiftTypeStr) throws Exception {
        Staff staff = staffDAO.findByStaffId(staffId);
        validateAuthorization("ASSIGN_SHIFT");
        Staff currentUser = getCurrentUser();

        if (staffId == null || staffId.trim().isEmpty()) {
            throw new SchedulingException("Staff ID cannot be null or empty");
        }

        ShiftType shiftType;
        try {
            shiftType = ShiftType.valueOf(shiftTypeStr);
        } catch (IllegalArgumentException e) {
            throw new SchedulingException("Invalid shift type: " + shiftTypeStr);
        }

        if (staff == null) {
            throw new SchedulingException("Staff member not found: " + staffId);
        }

        validateShiftAssignment(staff, day, shiftType);

        String shiftId = generateShiftId(staffId, day, shiftType);
        Shift newShift = new Shift(shiftId, day, shiftType);
        newShift.assignStaff(staffId);

        shiftDAO.saveShift(newShift); // Save to database
        allShifts.add(newShift); // Also add to in-memory list

        staffSchedules.computeIfAbsent(staffId, k -> new ArrayList<>()).add(newShift);

        auditLogger.logAction(currentUser.getStaffId(),
                ActionType.ASSIGN_SHIFT,
                "Assigned " + shiftType + " shift on " + day,
                staffId);

        System.out.println("Shift assigned: " + shiftId + " to " + staff.getName());
    }



    //Get all shifts assigned to a staff member
    public List<Shift> getStaffShifts(String staffId) throws Exception {
        validateAuthentication();
        return new ArrayList<>(staffSchedules.getOrDefault(staffId, new ArrayList<>()));
    }

    //Validate shift assignment against business rules
    private void validateShiftAssignment(Staff staff, DayOfWeek day, ShiftType shiftType) throws Exception {
        // Rule 1: Check if shift type matches staff role
        if (shiftType == ShiftType.DOCTOR_ROUND && !(staff instanceof Doctor)) {
            throw new SchedulingException("Only doctors can be assigned to DOCTOR_ROUND shifts");
        }

        if ((shiftType == ShiftType.MORNING_NURSE || shiftType == ShiftType.AFTERNOON_NURSE)
                && !(staff instanceof Nurse)) {
            throw new SchedulingException("Only nurses can be assigned to nurse shifts");
        }

        // Rule 2: Check maximum hours per day
        List<Shift> existingShifts = staffSchedules.getOrDefault(staff.getStaffId(), new ArrayList<>());
        int hoursOnDay = existingShifts.stream()
                .filter(shift -> shift.getDayOfWeek().equals(day))
                .mapToInt(Shift::getDurationHours)
                .sum();

        if (hoursOnDay + shiftType.getDurationHours() > staff.getMaxHoursPerDay()) {
            throw new SchedulingException(String.format(
                    "Shift assignment would exceed maximum hours per day. Current: %d, Adding: %d, Max: %d",
                    hoursOnDay, shiftType.getDurationHours(), staff.getMaxHoursPerDay()));
        }

        // Rule 3: Check for overlapping shifts on same day
        for (Shift existingShift : existingShifts) {
            if (existingShift.getDayOfWeek().equals(day)) {
                if (shiftsOverlap(existingShift.getShiftType(), shiftType)) {
                    throw new SchedulingException("Shift overlaps with existing assignment on " + day);
                }
            }
        }

        // Rule 4: Doctors can only work 1 hour per day (DOCTOR_ROUND only)
        if (staff instanceof Doctor) {
            boolean hasExistingShiftOnDay = existingShifts.stream()
                    .anyMatch(shift -> shift.getDayOfWeek().equals(day));

            if (hasExistingShiftOnDay) {
                throw new SchedulingException("Doctor already has shift assigned on " + day);
            }

            if (shiftType != ShiftType.DOCTOR_ROUND) {
                throw new SchedulingException("Doctors can only be assigned to DOCTOR_ROUND shifts");
            }
        }

        // Rule 5: Weekend restrictions for managers
        if (staff instanceof Manager && (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY)) {
            throw new SchedulingException("Managers cannot work on weekends");
        }
    }

    //Check if two shift types overlap in time
    private boolean shiftsOverlap(ShiftType shift1, ShiftType shift2) {
        return shift1.getStartHour() < shift2.getEndHour() &&
                shift2.getStartHour() < shift1.getEndHour();
    }

    //Generate unique shift ID
    private String generateShiftId(String staffId, DayOfWeek day, ShiftType shiftType) {
        return String.format("SH_%s_%s_%s_%d",
                staffId, day.name(), shiftType.name(), System.currentTimeMillis());
    }

    //Find shift by ID
    private Shift findShiftById(String shiftId) {
        return allShifts.stream()
                .filter(shift -> shift.getShiftId().equals(shiftId))
                .findFirst()
                .orElse(null);
    }

    // Declares which roles can assign/modify shifts and who may view schedules
    @Override
    protected boolean isAuthorizedForAction(Staff staff, String action) {
        switch (action) {
            case "ASSIGN_SHIFT":
            case "MODIFY_SHIFT":
                return staff instanceof Manager;
            case "VIEW_SHIFTS":
                return true; // All authenticated staff can view schedules
            default:
                return false;
        }
    }
}
