package CareHome.controller;

import CareHome.Service.AuthenticationService;
import CareHome.Model.Person.Staff;
import CareHome.Model.Person.Manager;
import CareHome.Model.Audit.AuditLogger;
import CareHome.Model.ActionType;
import CareHome.dao.StaffDAO;
import CareHome.dao.StaffDAOImpl;
import CareHome.Exception.AuthorizationException;

import java.util.List;

public class StaffController extends BaseController {
    private StaffDAO staffDAO;

    // Sets up staff controller with authentication, audit, and staff DAO
    public StaffController(AuthenticationService authService, AuditLogger auditLogger) {
        super(authService, auditLogger);
        this.staffDAO = new StaffDAOImpl();
    }

    /* Authenticates a staff member and returns their staff object on success
    public Staff login(String username, String password) throws AuthorizationException {
        return authService.login(username, password);
    } */

    // Logs out the currently authenticated user if present
    public void logout() {
        Staff currentUser = authService.getCurrentUser();
        if (currentUser != null) {
            authService.logout(currentUser);
        }
    }

    // Persists a new staff member (manager-only) and records the action in the audit log
    public void addStaff(Staff staff) throws Exception {
        validateAuthorization("ADD_STAFF");
        Staff currentUser = getCurrentUser();

        staffDAO.save(staff);

        auditLogger.logAction(currentUser.getStaffId(),
                ActionType.ADD_STAFF,
                "Added staff member: " + staff.getName(),
                staff.getStaffId());
    }

    // Updates a staff member's password by username
    public void updateStaffPassword(String username, String newPassword) throws Exception {
        Staff staff = staffDAO.findByUsername(username);
        if (staff == null) {
            throw new Exception("Staff member not found with username: " + username);
        }
        staffDAO.updatePasswordByUsername(username, newPassword);
    }

    // Restricts staff-management actions to managers
    @Override
    protected boolean isAuthorizedForAction(Staff staff, String action) {
        switch (action) {
            case "ADD_STAFF":
            case "MODIFY_STAFF":
                return staff instanceof Manager;
            default:
                return false;
        }
    }
}
