package CareHome.Model;

public enum ShiftType {
    MORNING_NURSE(8, 16),    // 8am to 4pm (8 hours)
    AFTERNOON_NURSE(14, 22), // 2pm to 10pm (8 hours)
    DOCTOR_ROUND(9, 10);     // 9am to 10am (1 hour)

    private final int startHour; // Start time in 24-hour format
    private final int endHour;   // End time in 24-hour format

    ShiftType(int startHour, int endHour) {
        this.startHour = startHour;
        this.endHour = endHour;
    }

    public int getStartHour() { return startHour; }
    public int getEndHour() { return endHour; }
    public int getDurationHours() { return endHour - startHour; }
}
