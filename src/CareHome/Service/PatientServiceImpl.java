package CareHome.Service;

import CareHome.Model.Location.Ward;
import CareHome.Service.PatientService;
import CareHome.Model.Person.Patient;
import CareHome.dao.PatientDAO;
import CareHome.dao.PatientDAOImpl;
import CareHome.dao.BedDAO;
import CareHome.dao.BedDAOImpl;
import CareHome.Exception.ComplianceException;
import CareHome.Exception.CareHomeException;
import CareHome.Model.ActionType;
import CareHome.Model.Audit.AuditLogger;
import CareHome.Model.Person.Staff;

import java.util.List;

public class PatientServiceImpl implements PatientService {
    private PatientDAO patientDAO;
    private BedDAO bedDAO;

    // Creates the service with DAO implementations for patients and beds
    public PatientServiceImpl() {
        this.patientDAO = new PatientDAOImpl();
        this.bedDAO = new BedDAOImpl();
    }

    // Validates input, ensures unique ID, finds suitable bed, persists patient and assignments, and audits
    @Override
    public void addPatient(Patient patient) throws Exception {
        // Validate patient data
        if (patient == null) {
            throw new CareHomeException("Patient cannot be null");
        }

        if (patient.getFirstName() == null || patient.getFirstName().trim().isEmpty()) {
            throw new CareHomeException("Patient first name is required");
        }

        if (patient.getLastName() == null || patient.getLastName().trim().isEmpty()) {
            throw new CareHomeException("Patient last name is required");
        }

        if (patient.getPatientId() == null || patient.getPatientId().trim().isEmpty()) {
            throw new CareHomeException("Patient ID is required");
        }

        // Check if patient ID already exists in database
        Patient existing = patientDAO.findById(patient.getId());
        if (existing != null) {
            throw new ComplianceException("Patient with ID " + patient.getId() + " already exists");
        }

        // Find suitable bed in database
        String suitableBedId = bedDAO.findSuitableBed(
                patient.getGender().toString(),
                patient.needsIsolation()
        );

        if (suitableBedId == null) {
            throw new ComplianceException("No suitable bed available for patient");
        }

        // Save patient to database first
        patientDAO.save(patient);

        // Then assign bed in database, which marks it as occupied
        bedDAO.assignPatientToBed(suitableBedId, patient.getId());

        // Finally, log the assignment in the history table
        patientDAO.assignBedToPatient(patient.getId(), suitableBedId);

        // Log the action using the shared logger
        new AuthenticationServiceImpl(new AuditLogger()).getAuditLogger().logAction(getCurrentUserId(), ActionType.ADD_PATIENT, "Added new patient", patient.getPatientId());

        System.out.println("Patient " + patient.getName() + " assigned to bed " + suitableBedId);
    }

    // Fetches a patient by ID after validating input
    @Override
    public Patient findPatientById(String patientId) throws Exception {
        if (patientId == null || patientId.trim().isEmpty()) {
            throw new CareHomeException("Patient ID cannot be null or empty");
        }
        return patientDAO.findById(patientId);
    }

    // Returns all patients via DAO
    @Override
    public List<Patient> getAllPatients() throws Exception {
        return patientDAO.findAll();
    }

    // Performs cross-table move (free old bed, end assignment, assign new bed, start assignment) and audits
    @Override
    public void movePatient(String patientId, String newBedId) throws Exception {
        // Validate inputs
        if (patientId == null || patientId.trim().isEmpty()) {
            throw new CareHomeException("Patient ID cannot be null or empty");
        }

        if (newBedId == null || newBedId.trim().isEmpty()) {
            throw new CareHomeException("Bed ID cannot be null or empty");
        }

        // Find the patient
        Patient patient = patientDAO.findById(patientId);
        if (patient == null) {
            throw new CareHomeException("Patient not found with ID: " + patientId);
        }

        // Check if target bed is available
        if (!bedDAO.isBedAvailable(newBedId)) {
            throw new ComplianceException("Target bed is already occupied");
        }

        // Check for gender compatibility
        if (!bedDAO.isRoomGenderCompatible(newBedId, patient.getGender().toString())) {
            throw new ComplianceException("Gender incompatibility in the target room.");
        }

        // Find patient's current bed
        String currentBedId = bedDAO.findPatientBed(patientId);
        if (currentBedId == null) {
            throw new CareHomeException("Patient not found in any bed");
        }

        // End the old bed assignment in both tables
        bedDAO.freeBed(currentBedId);
        patientDAO.endBedAssignment(patientId);

        // Start the new bed assignment in both tables
        bedDAO.assignPatientToBed(newBedId, patientId);
        patientDAO.assignBedToPatient(patientId, newBedId);

        new AuditLogger().logAction(getCurrentUserId(), ActionType.MOVE_PATIENT, "Moved patient to bed: " + newBedId, patient.getPatientId());

        System.out.println("Patient moved from " + currentBedId + " to " + newBedId);
    }

    // Frees bed if occupied, archives patient data as needed, discharges patient, and audits
    @Override
    public void dischargePatient(String patientId) throws Exception {
        if (patientId == null || patientId.trim().isEmpty()) {
            throw new CareHomeException("Patient ID cannot be null or empty");
        }

        Patient patient = patientDAO.findById(patientId);
        if (patient == null) {
            throw new CareHomeException("Patient not found with ID: " + patientId);
        }

        // Find and free patient's bed
        String bedId = bedDAO.findPatientBed(patientId);
        if (bedId != null) {
            bedDAO.freeBed(bedId);
        }

        // Discharge from database (soft delete)
        patientDAO.discharge(patientId);

        new AuditLogger().logAction(getCurrentUserId(), ActionType.DISCHARGE_PATIENT, "Patient discharged", patient.getPatientId());

        System.out.println("Patient discharged, bed " + bedId + " is now available");
    }

    // Additional methods for database-based bed management
    public String getPatientBed(String patientId) throws Exception {
        return bedDAO.findPatientBed(patientId);
    }

    // Helper to fetch current user ID for audit entries
    private String getCurrentUserId() {
        Staff currentUser = new AuthenticationServiceImpl(new AuditLogger()).getCurrentUser();
        return currentUser != null ? currentUser.getStaffId() : "SYSTEM";
    }

}
