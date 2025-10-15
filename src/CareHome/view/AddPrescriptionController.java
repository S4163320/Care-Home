package CareHome.view;

import CareHome.Model.ActionType;
import CareHome.Model.Audit.AuditLogger;
import CareHome.Model.Medical.Prescription;
import CareHome.Model.Person.Doctor;
import CareHome.Model.Person.Patient;
import CareHome.Service.AuthenticationService;
import CareHome.Service.AuthenticationServiceImpl;
import CareHome.Service.PatientService;
import CareHome.Service.PatientServiceImpl;
import CareHome.Service.PrescriptionServiceImpl;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.List;

public class AddPrescriptionController {

    @FXML private ComboBox<Patient> patientComboBox;
    @FXML private DatePicker prescriptionDatePicker;
    @FXML private TextField medicationField;
    @FXML private TextField dosageField;
    @FXML private TextField frequencyField;
    @FXML private TextArea instructionsArea;
    @FXML private Label errorLabel;

    private final PatientService patientService = new PatientServiceImpl();
    private final PrescriptionServiceImpl prescriptionService = new PrescriptionServiceImpl();
    private final AuthenticationService authService = new AuthenticationServiceImpl(new AuditLogger());
    private final AuditLogger auditLogger = new AuditLogger();

    // Loads patients into the combo box on dialog open
    @FXML
    public void initialize() {
        try {
            List<Patient> all = patientService.getAllPatients();
            patientComboBox.setItems(FXCollections.observableArrayList(all));
        } catch (Exception ex) {
            showError("Failed to load patients: " + ex.getMessage());
        }
    }

    // Validates inputs and role, persists the prescription, audits, and closes with confirmation
    @FXML
    private void handleAddPrescription() {
        try {
            Patient patient = patientComboBox.getValue();
            LocalDate date = prescriptionDatePicker.getValue();
            String medication = medicationField.getText().trim();
            String dosage = dosageField.getText().trim();
            String frequency = frequencyField.getText().trim();
            String instructions = instructionsArea.getText().trim();

            if (patient == null || date == null || medication.isEmpty() || dosage.isEmpty() || frequency.isEmpty()) {
                showError("Please fill all required fields.");
                return;
            }

            if (!(authService.getCurrentUser() instanceof Doctor doctor)) {
                showError("Only doctors can add prescriptions.");
                return;
            }

            String id = "PRESC" + System.currentTimeMillis();

            Prescription prescription = new Prescription(
                    id,
                    patient.getPatientId(),
                    doctor.getStaffId(),
                    medication,
                    dosage,
                    frequency,
                    LocalDate.now(),
                    null // endDate optional
            );

            // Save the prescription
            prescriptionService.addPrescription(prescription, doctor.getStaffId());

            // Log the action
            auditLogger.logAction(
                    doctor.getStaffId(),
                    ActionType.ADD_PRESCRIPTION,
                    "Added prescription for patient " + patient.getPatientId() +
                            " (" + medication + ", " + dosage + ", " + frequency + ")",
                    prescription.getPrescriptionId()
            );

            // Show confirmation
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Prescription added successfully.");
            alert.showAndWait();
            closeWindow();

        } catch (Exception ex) {
            showError("Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // Closes the dialog window without saving
    @FXML
    private void handleClose() {
        closeWindow();
    }

    // Helper to close the current stage
    private void closeWindow() {
        Stage stage = (Stage) medicationField.getScene().getWindow();
        stage.close();
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
    }
}
