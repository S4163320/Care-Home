package CareHome.Exception;

public class AuthorizationException extends CareHomeException {

    public AuthorizationException(String staffId, String action) {
        super("Authorization Failed: Staff " + staffId + " is not authorized to " + action);
    }

    public AuthorizationException(String message) {
        super("Authorization Failed: " + message);
    }
}
