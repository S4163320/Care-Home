package CareHome.Demo;

import CareHome.Model.*;
import CareHome.Model.Medical.Prescription;
import CareHome.Model.Person.*;
import CareHome.Model.Audit.AuditLogger;
import CareHome.Model.Audit.AuditEntry;
import CareHome.Model.Schedule.Shift;
import CareHome.Service.*;
import CareHome.controller.*;
import CareHome.dao.*;
import java.time.LocalDate;
import java.time.DayOfWeek;
import java.util.Scanner;
import java.util.List;

public class CareHomeDemoApplication {
    private AuditLogger auditLogger;
    private AuthenticationServiceImpl authService;
    private PatientController patientController;
    private StaffController staffController;
    private PrescriptionController prescriptionController;
    private ShiftController shiftController;
    private Scanner scanner;
    private Staff currentUser;
    private boolean systemRunning;

    // Database services - NO MORE MEMORY STORAGE
    private PatientServiceImpl patientService;
    private PrescriptionServiceImpl prescriptionService;
    private StaffDAOImpl staffDAO;
    private BedDAOImpl bedDAO;
    private PrescriptionDAOImpl prescriptionDAO;

    public static void main(String[] args) {
        new CareHomeDemoApplication().run();
    }

    public CareHomeDemoApplication() {
        // Initialize system with database-only persistence
        auditLogger = new AuditLogger();
        authService = new AuthenticationServiceImpl(auditLogger);
        scanner = new Scanner(System.in);
        systemRunning = true;

        // Initialize database services
        patientService = new PatientServiceImpl();
        prescriptionService = new PrescriptionServiceImpl();
        staffDAO = new StaffDAOImpl();
        bedDAO = new BedDAOImpl();
        prescriptionDAO = new PrescriptionDAOImpl();

        // Initialize controllers
        patientController = new PatientController(authService, patientService, auditLogger);
        staffController = new StaffController(authService, auditLogger);
        prescriptionController = new PrescriptionController(authService, auditLogger);
        shiftController = new ShiftController(authService, auditLogger);

        // Display initial system info
        displayInitialInfo();
    }

    private void displayInitialInfo() {
        try {
            System.out.println("=== RMIT Care Home Management System - Production Version ===");
            System.out.println("All data persisted to SQLite database: carehome.db");
            System.out.println("Total Beds Available: " + bedDAO.getTotalBeds());
            System.out.println("Available Beds: " + bedDAO.getAvailableBedCount());
            System.out.println("\nDefault Login Credentials:");
            System.out.println("Manager: username='manager', password='pass123'");
            System.out.println("Doctor: username='doctor', password='pass123'");
            System.out.println("Nurse: username='nurse', password='pass123'");
            System.out.println("===============================================\n");
        } catch (Exception e) {
            System.err.println("Error loading system info: " + e.getMessage());
        }
    }

    public void run() {
        // Main system loop - keeps running until explicit exit
        while (systemRunning) {
            // Login loop
            currentUser = null;
            while (currentUser == null && systemRunning) {
                if (!performLogin()) {
                    System.out.println("\nDo you want to try again? (y/n): ");
                    String response = scanner.nextLine().trim().toLowerCase();
                    if (!response.equals("y") && !response.equals("yes")) {
                        systemRunning = false;
                        break;
                    }
                }
            }

            // Main menu loop (only if successfully logged in)
            while (currentUser != null && systemRunning) {
                displayMainMenu();
                int choice = getIntInput("Enter your choice: ");

                try {
                    if (!handleMenuChoice(choice)) {
                        // User chose to logout (choice 0)
                        if (currentUser != null) {
                            staffController.logout();
                        }
                        currentUser = null;
                        System.out.println("\nLogged out successfully. Returning to login screen...\n");
                        break;
                    }
                } catch (Exception e) {
                    System.err.println("Error: " + e.getMessage());
                }

                if (currentUser != null) {
                    System.out.println("\nPress Enter to continue...");
                    scanner.nextLine();
                }
            }
        }

        System.out.println("Thank you for using RMIT Care Home System!");
        scanner.close();
    }

    private boolean performLogin() {
        System.out.println("=== LOGIN ===");
        for (int attempts = 0; attempts < 3; attempts++) {
            System.out.print("Username: ");
            String username = scanner.nextLine().trim();

            if (username.equalsIgnoreCase("exit")) {
                systemRunning = false;
                return false;
            }

            System.out.print("Password: ");
            String password = scanner.nextLine().trim();

            try {
                // Authenticate user using database only
                currentUser = authService.login(username, password);
                System.out.println("Login successful! Welcome " + currentUser.getName() + " (" + currentUser.getRole() + ")");
                return true;

            } catch (Exception e) {
                System.err.println("Login failed: " + e.getMessage());
                if (attempts < 2) {
                    System.out.println("Please try again... (or type 'exit' as username to quit)");
                }
            }
        }
        System.out.println("Maximum login attempts exceeded.");
        return false;
    }

    private void displayMainMenu() {
        System.out.println("\n=== MAIN MENU ===");
        System.out.println("Current user: " + currentUser.getName() + " (" + currentUser.getRole() + ")");
        System.out.println("1. Add New Patient");
        System.out.println("2. View Patient Details");
        System.out.println("3. Move Patient to Different Bed");
        System.out.println("4. Discharge Patient (Manager only)");
        System.out.println("5. Add Staff Member");
        System.out.println("6. Modify Staff Details");
        System.out.println("7. Assign/Modify Staff Shifts");
        System.out.println("8. Add Prescription (Doctor only)");
        System.out.println("9. Administer Medication (Nurse only)");
        System.out.println("10. View Audit Log");
        System.out.println("11. Display System Status");
        System.out.println("12. Exit System Completely");
        System.out.println("0. Logout (Return to Login Screen)");
    }

    private boolean handleMenuChoice(int choice) throws Exception {
        switch (choice) {
            case 1: addNewPatient(); break;
            case 2: viewPatientDetails(); break;
            case 3: movePatient(); break;
            case 4: dischargePatient(); break;
            case 5: addStaffMember(); break;
            case 6: modifyStaffDetails(); break;
            case 7: manageShifts(); break;
            case 8: addPrescription(); break;
            case 9: administerMedication(); break;
          //  case 10: viewAuditLog(); break;
            case 11: displaySystemStatus(); break;
            case 12:
                systemRunning = false;
                return false;
            case 0:
                return false;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
        return true;
    }

    // Add new patient if manager
    private void addNewPatient() throws Exception {
        if (!(currentUser instanceof Manager)) {
            throw new Exception("Only managers can add patients");
        }

        System.out.println("\n=== ADD NEW PATIENT ===");
        System.out.print("First Name: ");
        String firstName = scanner.nextLine().trim();
        System.out.print("Last Name: ");
        String lastName = scanner.nextLine().trim();
        System.out.print("Age: ");
        int age = getIntInput("");
        System.out.print("Gender (MALE/FEMALE): ");
        Gender gender = Gender.valueOf(scanner.nextLine().toUpperCase().trim());
        System.out.print("Patient ID: ");
        String patientId = scanner.nextLine().trim();
        System.out.print("Needs Isolation? (y/n): ");
        boolean needsIsolation = scanner.nextLine().toLowerCase().trim().startsWith("y");

        Patient patient = new Patient("P" + System.currentTimeMillis(), firstName, lastName,
                gender, age, patientId, LocalDate.now());
        patient.setNeedsIsolation(needsIsolation);

        patientController.addPatient(patient);

        // Find and display assigned bed from database
        String assignedBed = patientService.getPatientBed(patient.getId());
        System.out.println("Patient added successfully!");
        if (assignedBed != null) {
            System.out.println("Assigned to bed: " + assignedBed);
        } else {
            System.out.println("Warning: No bed assignment found");
        }
    }

    // View selected patient's details
    private void viewPatientDetails() throws Exception {
        System.out.println("\n=== VIEW PATIENT DETAILS ===");
        displayAllPatients();

        List<Patient> allPatients = patientService.getAllPatients();
        if (allPatients.isEmpty()) {
            System.out.println("No patients to view.");
            return;
        }

        System.out.print("Enter Patient ID to view details: ");
        String patientId = scanner.nextLine().trim();

        Patient patient = allPatients.stream()
                .filter(p -> p.getPatientId().equals(patientId))
                .findFirst()
                .orElse(null);

        if (patient == null) {
            System.out.println("Patient not found!");
            return;
        }

        System.out.println("\n--- Patient Details ---");
        System.out.println("Name: " + patient.getName());
        System.out.println("Age: " + patient.getAge());
        System.out.println("Gender: " + patient.getGender());
        System.out.println("Patient ID: " + patient.getPatientId());
        System.out.println("Admission Date: " + patient.getAdmissionDate());
        System.out.println("Needs Isolation: " + (patient.needsIsolation() ? "Yes" : "No"));
        System.out.println("Days in Care: " + patient.getDaysInCare());

        String bedId = patientService.getPatientBed(patient.getId());
        System.out.println("Current Bed: " + (bedId != null ? bedId : "Not assigned"));

        displayPatientPrescriptions(patientId);
    }

    // Move patient to another bed if permitted
    private void movePatient() throws Exception {
        if (!currentUser.canMovePatients()) {
            throw new Exception("You are not authorized to move patients");
        }

        System.out.println("\n=== MOVE PATIENT ===");
        displayAllPatients();

        List<Patient> allPatients = patientService.getAllPatients();
        if (allPatients.isEmpty()) {
            System.out.println("No patients to move.");
            return;
        }

        System.out.print("Enter Patient ID to move: ");
        String patientId = scanner.nextLine().trim();

        displayAvailableBeds();
        System.out.print("Enter target Bed ID: ");
        String bedId = scanner.nextLine().trim();

        Patient patient = allPatients.stream()
                .filter(p -> p.getPatientId().equals(patientId))
                .findFirst()
                .orElse(null);

        if (patient == null) {
            throw new Exception("Patient not found");
        }

        String oldBed = patientService.getPatientBed(patient.getId());
        patientController.movePatient(patient.getId(), bedId);

        System.out.println("Patient moved successfully!");
        System.out.println("Moved from: " + (oldBed != null ? oldBed : "Unknown") + " to: " + bedId);
    }

    // Discharge patient (Manager only) and archive data
    private void dischargePatient() throws Exception {
        if (!(currentUser instanceof Manager)) {
            throw new Exception("Only managers can discharge patients");
        }

        System.out.println("\n=== DISCHARGE PATIENT ===");
        displayAllPatients();

        List<Patient> allPatients = patientService.getAllPatients();
        if (allPatients.isEmpty()) {
            System.out.println("No patients to discharge.");
            return;
        }

        System.out.print("Enter Patient ID to discharge: ");
        String patientId = scanner.nextLine().trim();

        Patient patient = allPatients.stream()
                .filter(p -> p.getPatientId().equals(patientId))
                .findFirst()
                .orElse(null);

        if (patient == null) {
            throw new Exception("Patient not found");
        }

        String currentBed = patientService.getPatientBed(patient.getId());
        System.out.println("\nDischarging patient: " + patient.getName());
        System.out.println("Current bed: " + (currentBed != null ? currentBed : "Not assigned"));
        System.out.println("Days in care: " + patient.getDaysInCare());

        System.out.print("Confirm discharge? (y/n): ");
        if (scanner.nextLine().toLowerCase().trim().startsWith("y")) {
            patientController.dischargePatient(patient.getId());
            System.out.println("Patient discharged successfully!");
            System.out.println("Bed " + (currentBed != null ? currentBed : "") + " is now available.");
        } else {
            System.out.println("Discharge cancelled.");
        }
    }

    // Add new staff (Manager only)
    private void addStaffMember() throws Exception {
        if (!(currentUser instanceof Manager)) {
            throw new Exception("Only managers can add staff");
        }

        System.out.println("\n=== ADD STAFF MEMBER ===");
        System.out.print("First Name: ");
        String firstName = scanner.nextLine().trim();
        System.out.print("Last Name: ");
        String lastName = scanner.nextLine().trim();
        System.out.print("Age: ");
        int age = getIntInput("");
        System.out.print("Gender (MALE/FEMALE): ");
        Gender gender = Gender.valueOf(scanner.nextLine().toUpperCase().trim());
        System.out.print("Staff ID: ");
        String staffId = scanner.nextLine().trim();
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();
        System.out.print("Role (DOCTOR/NURSE/MANAGER): ");
        String role = scanner.nextLine().toUpperCase().trim();

        Staff staff;
        String id = "S" + System.currentTimeMillis();

        switch (role) {
            case "DOCTOR":
                System.out.print("Medical License Number: ");
                String medLicense = scanner.nextLine().trim();
                staff = new Doctor(id, firstName, lastName, gender, age, staffId, username, password, medLicense);
                break;
            case "NURSE":
                System.out.print("Nursing License Number: ");
                String nurLicense = scanner.nextLine().trim();
                staff = new Nurse(id, firstName, lastName, gender, age, staffId, username, password, nurLicense);
                break;
            case "MANAGER":
                staff = new Manager(id, firstName, lastName, gender, age, staffId, username, password);
                break;
            default:
                throw new Exception("Invalid role");
        }

        staffController.addStaff(staff);
        System.out.println("Staff member added successfully!");
        System.out.println("New staff can now login with username: '" + username + "' and password: '" + password + "'");
    }

    // Modify staff password (Manager only)
    private void modifyStaffDetails() throws Exception {
        if (!(currentUser instanceof Manager)) {
            throw new Exception("Only managers can modify staff details");
        }

        System.out.println("\n=== MODIFY STAFF DETAILS ===");
        displayAllStaff();
        System.out.print("Enter Staff Username to modify: ");
        String staffId = scanner.nextLine().trim();
        System.out.print("Enter new password: ");
        String newPassword = scanner.nextLine().trim();

        staffController.updateStaffPassword(staffId, newPassword);
        System.out.println("Staff password updated successfully in database!");
    }

    // Manage shifts
    private void manageShifts() throws Exception {
        System.out.println("\n=== MANAGE SHIFTS ===");
        System.out.println("1. Assign new shift");
        System.out.println("2. View staff shifts");
        System.out.print("Choice: ");
        int choice = getIntInput("");

        if (choice == 1) {
            assignShift();
        } else if (choice == 2) {
            viewStaffShifts();
        }
    }

    // Assign shift to staff (Manager only)
    private void assignShift() throws Exception {
        if (!(currentUser instanceof Manager)) {
            throw new Exception("Only managers can assign shifts");
        }

        displayAllStaff();
        System.out.print("Enter Staff ID: ");
        String staffId = scanner.nextLine().trim();

        System.out.println("Available days: MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY");
        System.out.print("Enter day: ");
        String dayInput = scanner.nextLine().toUpperCase().trim();
        DayOfWeek day;
        try {
            day = DayOfWeek.valueOf(dayInput);
        } catch (IllegalArgumentException e) {
            throw new Exception("Invalid day. Please use format like MONDAY, TUESDAY, etc.");
        }

        System.out.println("Available shift types:");
        System.out.println("- MORNING_NURSE (8am-4pm, 8 hours)");
        System.out.println("- AFTERNOON_NURSE (2pm-10pm, 8 hours)");
        System.out.println("- DOCTOR_ROUND (9am-10am, 1 hour)");
        System.out.print("Enter shift type: ");
        String shiftTypeInput = scanner.nextLine().toUpperCase();

        shiftController.assignShift(staffId, day, shiftTypeInput);
        System.out.println("Shift assigned successfully and saved to database!");
    }

    // View shifts for a staff member
    private void viewStaffShifts() throws Exception {
        System.out.print("Enter Staff ID: ");
        String staffId = scanner.nextLine().trim();

        List<Shift> shifts = shiftController.getStaffShifts(staffId);

        if (shifts.isEmpty()) {
            System.out.println("No shifts assigned to staff member: " + staffId);
        } else {
            System.out.println("\n--- Shifts for Staff " + staffId + " ---");
            shifts.forEach(shift -> System.out.println(shift.toString()));
        }
    }

    // Add a prescription (Doctors only)
    private void addPrescription() throws Exception {
        if (!(currentUser instanceof Doctor)) {
            throw new Exception("Only doctors can add prescriptions");
        }

        System.out.println("\n=== ADD PRESCRIPTION ===");
        displayAllPatients();

        List<Patient> allPatients = patientService.getAllPatients();
        if (allPatients.isEmpty()) {
            System.out.println("No patients available for prescription.");
            return;
        }

        System.out.print("Enter Patient ID: ");
        String patientId = scanner.nextLine().trim();

        boolean patientExists = allPatients.stream()
                .anyMatch(p -> p.getPatientId().equals(patientId));
        if (!patientExists) {
            System.out.println("Patient not found with ID: " + patientId);
            return;
        }

        System.out.print("Medication Name: ");
        String medication = scanner.nextLine().trim();
        System.out.print("Dosage: ");
        String dosage = scanner.nextLine().trim();
        System.out.print("Frequency: ");
        String frequency = scanner.nextLine().trim();
        System.out.print("Duration in days: ");
        int duration = getIntInput("");

        String prescId = "PRESC" + System.currentTimeMillis();
        Prescription prescription = new Prescription(prescId, patientId, currentUser.getId(),
                medication, dosage, frequency,
                LocalDate.now(), LocalDate.now().plusDays(duration));

        prescriptionController.addPrescription(prescription);
        prescriptionDAO.save(prescription);

        System.out.println("Prescription added successfully to database!");
        System.out.println("Prescription ID: " + prescId);
    }

    // Administer medication (Nurses only)
    private void administerMedication() throws Exception {
        if (!(currentUser instanceof Nurse)) {
            throw new Exception("Only nurses can administer medication");
        }

        System.out.println("\n=== ADMINISTER MEDICATION ===");
        displayPatientsWithPrescriptions();

        System.out.print("Enter Patient ID: ");
        String patientId = scanner.nextLine().trim();

        List<Prescription> patientPrescriptions = prescriptionDAO.findByPatientId(patientId);

        if (patientPrescriptions.isEmpty()) {
            System.out.println("No active prescriptions found for patient: " + patientId);
            return;
        }

        System.out.println("\n--- Available Prescriptions for Patient " + patientId + " ---");
        for (int i = 0; i < patientPrescriptions.size(); i++) {
            Prescription p = patientPrescriptions.get(i);
            System.out.println((i + 1) + ". " + p.getMedicationName() + " - " +
                    p.getDosage() + " (" + p.getFrequency() + ")");
        }

        System.out.print("Select prescription number to administer (1-" + patientPrescriptions.size() + "): ");
        int choice = getIntInput("") - 1;

        if (choice < 0 || choice >= patientPrescriptions.size()) {
            System.out.println("Invalid selection.");
            return;
        }

        Prescription selectedPrescription = patientPrescriptions.get(choice);

        System.out.print("Enter administration notes: ");
        String notes = scanner.nextLine().trim();

        prescriptionController.administerMedication(selectedPrescription.getPrescriptionId(), notes);
        System.out.println("Medication administered and logged to database successfully!");
        System.out.println("Administered: " + selectedPrescription.getMedicationName() +
                " (" + selectedPrescription.getDosage() + ") to patient " + patientId);
    }

    /* View Audit log entries
    private void viewAuditLog() {
        System.out.println("\n=== AUDIT LOG ===");
        List<AuditEntry> entries = auditLogger.getAllAuditEntries();

        if (entries.isEmpty()) {
            System.out.println("No audit entries found.");
        } else {
            entries.forEach(entry -> System.out.println(entry.toString()));
        }
    } */

    // Display system status info
    private void displaySystemStatus() {
        System.out.println("\n=== SYSTEM STATUS ===");
        try {
            System.out.println("Total Patients: " + patientService.getAllPatients().size());
            System.out.println("Total Staff: " + staffDAO.findAll().size());
       //     System.out.println("Total Audit Entries: " + auditLogger.getTotalEntries());
            System.out.println("Total Beds: " + bedDAO.getTotalBeds());
            System.out.println("Available Beds: " + bedDAO.getAvailableBedCount());
            System.out.println("Occupied Beds: " + (bedDAO.getTotalBeds() - bedDAO.getAvailableBedCount()));
            System.out.println("Active Prescriptions: " + prescriptionDAO.findActivePrescriptions().size());
            System.out.println("\nAll data persisted to database - survives system restarts!");
        } catch (Exception e) {
            System.err.println("Error retrieving system status: " + e.getMessage());
        }
    }

    // Helper methods for displaying lists and input
    private void displayPatientPrescriptions(String patientId) {
        try {
            List<Prescription> patientPrescriptions = prescriptionDAO.findByPatientId(patientId);

            if (!patientPrescriptions.isEmpty()) {
                System.out.println("\n--- Current Prescriptions ---");
                for (Prescription prescription : patientPrescriptions) {
                    System.out.println("ID: " + prescription.getPrescriptionId() +
                            " | Medication: " + prescription.getMedicationName() +
                            " | Dosage: " + prescription.getDosage() +
                            " | Frequency: " + prescription.getFrequency());
                }
            }
        } catch (Exception e) {
            System.err.println("Error retrieving prescriptions: " + e.getMessage());
        }
    }

    private void displayPatientsWithPrescriptions() {
        System.out.println("\n--- Patients with Active Prescriptions ---");
        try {
            List<Patient> allPatients = patientService.getAllPatients();
            boolean hasPatients = false;

            for (Patient patient : allPatients) {
                List<Prescription> prescriptions = prescriptionDAO.findByPatientId(patient.getPatientId());
                if (!prescriptions.isEmpty()) {
                    hasPatients = true;
                    System.out.println("ID: " + patient.getPatientId() + " - " + patient.getName() +
                            " (" + prescriptions.size() + " prescriptions)");
                }
            }

            if (!hasPatients) {
                System.out.println("No patients have active prescriptions.");
            }
        } catch (Exception e) {
            System.err.println("Error retrieving patients with prescriptions: " + e.getMessage());
        }
    }

    private void displayAllPatients() {
        System.out.println("\n--- Current Patients ---");
        try {
            List<Patient> patients = patientService.getAllPatients();
            if (patients.isEmpty()) {
                System.out.println("No patients registered");
            } else {
                for (Patient patient : patients) {
                    String bedId = patientService.getPatientBed(patient.getId());
                    System.out.println("ID: " + patient.getPatientId() + " - " + patient.getName() +
                            " (Bed: " + (bedId != null ? bedId : "Not assigned") + ")");
                }
            }
        } catch (Exception e) {
            System.err.println("Error retrieving patients: " + e.getMessage());
        }
    }

    private void displayAllStaff() {
        System.out.println("\n--- Current Staff ---");
        try {
            List<Staff> allStaff = staffDAO.findAll();
            allStaff.forEach(staff ->
                    System.out.println("ID: " + staff.getStaffId() + " - " + staff.getName() +
                            " (" + staff.getRole() + ") [" + staff.getUsername() + "]"));
        } catch (Exception e) {
            System.err.println("Error retrieving staff: " + e.getMessage());
        }
    }

    private void displayAvailableBeds() {
        System.out.println("\n--- Available Beds ---");
        try {
            List<String> availableBeds = bedDAO.getAvailableBeds();

            if (availableBeds.isEmpty()) {
                System.out.println("No beds available");
            } else {
                availableBeds.forEach(bedId -> System.out.println("Bed ID: " + bedId));
            }
        } catch (Exception e) {
            System.err.println("Error retrieving available beds: " + e.getMessage());
        }
    }

    private int getIntInput(String prompt) {
        System.out.print(prompt);
        while (!scanner.hasNextInt()) {
            System.out.print("Please enter a valid number: ");
            scanner.next();
        }
        int result = scanner.nextInt();
        scanner.nextLine(); // consume newline
        return result;
    }
}
