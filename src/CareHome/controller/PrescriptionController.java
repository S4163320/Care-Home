package CareHome.controller;

import CareHome.Service.AuthenticationService;
import CareHome.Model.Audit.AuditLogger;
import CareHome.Model.Medical.Prescription;
import CareHome.Model.Medical.MedicationAdministration;
import CareHome.Model.ActionType;
import CareHome.Model.Person.Staff;
import CareHome.Model.Person.Doctor;
import CareHome.Model.Person.Nurse;

public class PrescriptionController extends BaseController {

    // Creates a prescription controller with auth and audit capabilities
    public PrescriptionController(AuthenticationService authService, AuditLogger auditLogger) {
        super(authService, auditLogger);
    }

    // Adds a prescription (doctor only) and records the action in the audit log
    public void addPrescription(Prescription prescription) throws Exception {
        validateAuthorization("ADD_PRESCRIPTION");
        Staff currentUser = getCurrentUser();

        System.out.println("Prescription added: " + prescription.getMedicationName());

        auditLogger.logAction(currentUser.getStaffId(),
                ActionType.ADD_PRESCRIPTION,
                "Added prescription: " + prescription.getMedicationName(),
                prescription.getPatientId());
    }

    // Records that a medication was administered (nurse-only) and audits the event
    public void administerMedication(String prescriptionId, String notes) throws Exception {
        validateAuthorization("ADMINISTER_MEDICATION");
        Staff currentUser = getCurrentUser();

        System.out.println("Medication administered for prescription: " + prescriptionId);

        auditLogger.logAction(currentUser.getStaffId(),
                ActionType.ADMINISTER_MEDICATION,
                "Administered medication: " + notes,
                prescriptionId);
    }

    // Specifies which roles can add prescriptions or administer medications
    @Override
    protected boolean isAuthorizedForAction(Staff staff, String action) {
        switch (action) {
            case "ADD_PRESCRIPTION":
                return staff instanceof Doctor;
            case "ADMINISTER_MEDICATION":
                return staff instanceof Nurse;
            default:
                return false;
        }
    }
}
