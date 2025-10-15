package CareHome.Exception;

public class ComplianceException extends CareHomeException {

    public ComplianceException(String message) {
        super("Compliance Violation: " + message);
    }

}