package CareHome.Exception;

public class SchedulingException extends CareHomeException {

    public SchedulingException(String message) {
        super("Scheduling Error: " + message);
    }


}
