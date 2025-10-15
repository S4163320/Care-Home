package CareHome.controller;

import CareHome.Service.AuthenticationService;
import CareHome.Service.PatientService;
import CareHome.Model.Person.Patient;
import CareHome.Model.Person.Staff;
import CareHome.Model.Person.Manager;
import CareHome.Model.Person.Nurse;
import CareHome.Model.Audit.AuditLogger;
import CareHome.Model.ActionType;
import CareHome.Exception.AuthorizationException;

import java.util.List;

public class PatientController extends BaseController {
    private PatientService patientService;

    // Wires authentication, patient service, and audit logger for patient operations
    public PatientController(AuthenticationService authService,
                             PatientService patientService,
                             AuditLogger auditLogger) {
        super(authService, auditLogger);
        this.patientService = patientService;
    }

    // Adds a new patient via service and records the action in the audit log
    public void addPatient(Patient patient) throws Exception {
        validateAuthorization("ADD_PATIENT");
        Staff currentUser = getCurrentUser();

        patientService.addPatient(patient);

        auditLogger.logAction(currentUser.getStaffId(),
                ActionType.ADD_PATIENT,
                "Added patient: " + patient.getName(),
                patient.getId());
    }

    // Returns all patients after ensuring a user is authenticated
    public List<Patient> getAllPatients() throws Exception {
        validateAuthentication();
        return patientService.getAllPatients();
    }

    // Moves a patient to a new bed via service and writes an audit entry
    public void movePatient(String patientId, String newBedId) throws Exception {
        validateAuthorization("MOVE_PATIENT");
        Staff currentUser = getCurrentUser();

        patientService.movePatient(patientId, newBedId);

        auditLogger.logAction(currentUser.getStaffId(),
                ActionType.MOVE_PATIENT,
                "Moved patient to bed: " + newBedId,
                patientId);
    }

    // Discharges a patient via service and writes an audit entry
    public void dischargePatient(String patientId) throws Exception {
        validateAuthorization("DISCHARGE_PATIENT");
        Staff currentUser = getCurrentUser();

        patientService.dischargePatient(patientId);

        auditLogger.logAction(currentUser.getStaffId(),
                ActionType.ADD_PATIENT, // Reusing enum value
                "Discharged patient",
                patientId);
    }

    // Specifies which staff roles can perform which patient actions
    @Override
    protected boolean isAuthorizedForAction(Staff staff, String action) {
        switch (action) {
            case "ADD_PATIENT":
            case "DISCHARGE_PATIENT":
                return staff instanceof Manager;
            case "MOVE_PATIENT":
                return staff instanceof Manager || staff instanceof Nurse;
            case "VIEW_PATIENT":
                return true; // All authenticated staff can view patient details
            default:
                return false;
        }
    }
}
