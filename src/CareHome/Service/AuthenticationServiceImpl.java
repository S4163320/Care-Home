package CareHome.Service;

import CareHome.Model.Person.Staff;
import CareHome.dao.StaffDAO;
import CareHome.dao.StaffDAOImpl;
import CareHome.Exception.AuthorizationException;
import CareHome.Model.Audit.AuditLogger;
import CareHome.Model.ActionType;

import java.util.Set;

import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.util.HashSet;
import CareHome.controller.ShiftController;

public class AuthenticationServiceImpl implements AuthenticationService {
    private StaffDAO staffDAO;
    private AuditLogger auditLogger;
    private static Staff currentUser;
    private Set<String> loggedInUsers;
    private ShiftController shiftController;

    // Builds the auth service with DAO, audit logger, session set, and shift controller
    public AuthenticationServiceImpl(AuditLogger auditLogger) {
        this.staffDAO = new StaffDAOImpl();
        this.auditLogger = auditLogger;
        this.loggedInUsers = new HashSet<>();
        this.shiftController = new ShiftController(this, auditLogger);
    }

    // Verifies credentials/eligibility, sets currentUser, records audit, and returns Staff
    @Override
    public Staff login(String username, String password) throws AuthorizationException {
        try {

            Staff staff = staffDAO.findByUsername(username);

            if (staff == null) {
                throw new AuthorizationException("Invalid username or password");
            }

            if (!staff.checkPassword(password)) {
                throw new AuthorizationException("Invalid username or password");
            }

            if (!staff.isActive()) {
                throw new AuthorizationException("Staff account is deactivated");
            }

            validateWorkingHours(staff);

            this.currentUser = staff;
            loggedInUsers.add(staff.getStaffId());

            auditLogger.logAction(staff.getStaffId(), ActionType.LOGIN,
                    "User logged in", staff.getStaffId());

            return staff;

        } catch (Exception e) {
            throw new AuthorizationException("Login failed: " + e.getMessage());
        }
    }

    // Clears session tracking for the given staff, records audit, and resets currentUser if needed
    @Override
    public void logout(Staff staff) {
        if (staff != null) {
            loggedInUsers.remove(staff.getStaffId());
            auditLogger.logAction(staff.getStaffId(), ActionType.LOGOUT,
                    "User logged out", staff.getStaffId());

            if (currentUser != null && currentUser.getStaffId().equals(staff.getStaffId())) {
                currentUser = null;
            }
        }
    }


    @Override
    public Staff getCurrentUser() {
        return currentUser;
    }

    @Override
    public AuditLogger getAuditLogger() {
        return this.auditLogger;
    }

    @Override
    public void validateWorkingHours(Staff staff) throws AuthorizationException {
        // Managers are exempt from working hour checks
        if (staff instanceof CareHome.Model.Person.Manager) {
            return;
        }

        // For other staff, check if they are currently on the schedule
      //  if (!shiftController.isStaffScheduledNow(staff.getStaffId())) {
      //      throw new AuthorizationException("You are not scheduled to work at this time.");
       // }
    }
}

