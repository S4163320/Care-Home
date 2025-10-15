package CareHome.Model;

public enum ActionType {
    ADD_PATIENT,           // Manager adds new patient
    ADD_STAFF,            // Manager adds new staff member
    MODIFY_STAFF,         // Manager modifies staff details
    ADD_PRESCRIPTION,     // Doctor adds prescription
    ADMINISTER_MEDICATION,// Nurse administers medication
    MOVE_PATIENT,         // Manager/Nurse moves patient to different bed
    LOGIN,                // Staff member logs into system
    LOGOUT,                // Staff member logs out of system
    DISCHARGE_PATIENT,    // Manager discharges a patient
    ASSIGN_SHIFT,
    MODIFY_SHIFT
}