package CareHome.controller;

import CareHome.Service.AuthenticationService;
import CareHome.Model.Person.Staff;
import CareHome.Exception.AuthorizationException;
import CareHome.Model.Audit.AuditLogger;

public abstract class BaseController {
    protected AuthenticationService authService;
    protected AuditLogger auditLogger;

    // Initializes the base controller with authentication and auditing dependencies
    public BaseController(AuthenticationService authService, AuditLogger auditLogger) {
        this.authService = authService;
        this.auditLogger = auditLogger;
    }

    // Returns the currently authenticated staff or throws if none is logged in
    protected Staff getCurrentUser() throws AuthorizationException {
        Staff currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            throw new AuthorizationException("User not authenticated");
        }
        return currentUser;
    }

    // Ensures a user is authenticated by invoking getCurrentUser
    protected void validateAuthentication() throws AuthorizationException {
        getCurrentUser();
    }

    // Verifies the current user is authorized for a specific action
    protected void validateAuthorization(String action) throws AuthorizationException {
        Staff currentUser = getCurrentUser();
        if (!isAuthorizedForAction(currentUser, action)) {
            throw new AuthorizationException(currentUser.getStaffId(), action);
        }
    }

    // Implemented by subclasses to decide if a staff member may perform an action
    protected abstract boolean isAuthorizedForAction(Staff staff, String action);
}

