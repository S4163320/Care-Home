package CareHome.Service;

import CareHome.Model.Person.Staff;
import CareHome.Model.Audit.AuditLogger;
import CareHome.Exception.AuthorizationException;

public interface AuthenticationService {
    Staff login(String username, String password) throws AuthorizationException;
    void logout(Staff staff);
    Staff getCurrentUser();
    AuditLogger getAuditLogger();
    void validateWorkingHours(Staff staff) throws AuthorizationException;
}
