package CareHome.view;

import CareHome.Model.Person.Patient;
import CareHome.Service.PatientService;
import CareHome.Service.PatientServiceImpl;
import CareHome.Service.AuthenticationService;
import CareHome.Service.AuthenticationServiceImpl;
import CareHome.Model.Audit.AuditLogger;
import CareHome.Model.Person.Staff;
import CareHome.Model.Person.Manager;
import CareHome.Model.Person.Nurse;
import CareHome.dao.BedDAO;
import CareHome.dao.BedDAOImpl;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.util.List;
import java.util.Optional;

public class PatientViewController {

    @FXML private ListView<Patient> patientListView;
    @FXML private Label emptyLabel;
    @FXML private Button dischargeButton;
    @FXML private Button moveButton;

    private PatientService patientService = new PatientServiceImpl();
    private AuthenticationService authService = new AuthenticationServiceImpl(new AuditLogger());

    // Configures role-based buttons, list cell renderer, and loads the initial patient list
    @FXML
    public void initialize() {
        try {
            patientListView.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(Patient patient, boolean empty) {
                    super.updateItem(patient, empty);
                    setText(empty || patient == null ? null : "ID: " + patient.getPatientId() + ", Name: " + patient.getName());
                }
            });

            dischargeButton.setVisible(false);
            moveButton.setVisible(false);

            Staff currentUser = authService.getCurrentUser();
            if (currentUser instanceof Manager) {
                dischargeButton.setVisible(true);
                moveButton.setVisible(true);
            } else if (currentUser instanceof Nurse) {
                moveButton.setVisible(true);
            }

            refreshPatientList();
        } catch (Exception e) {
            emptyLabel.setText("Error initializing view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Discharges the selected patient after confirmation and refreshes the list
    @FXML
    private void handleDischarge() {
        Patient selectedPatient = patientListView.getSelectionModel().getSelectedItem();
        if (selectedPatient == null) {
            new Alert(Alert.AlertType.WARNING, "Please select a patient to discharge.").showAndWait();
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to discharge " + selectedPatient.getName() + "?");
        confirmAlert.showAndWait().filter(response -> response == ButtonType.OK).ifPresent(response -> {
            try {
                patientService.dischargePatient(selectedPatient.getId());
                refreshPatientList();
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, "Failed to discharge patient: " + e.getMessage()).showAndWait();
                e.printStackTrace();
            }
        });
    }

    // Moves the selected patient to a chosen available bed and refreshes the list
    @FXML
    private void handleMovePatient() {
        Patient selectedPatient = patientListView.getSelectionModel().getSelectedItem();
        if (selectedPatient == null) {
            new Alert(Alert.AlertType.WARNING, "Please select a patient to move.").showAndWait();
            return;
        }

        try {
            List<String> availableBeds = new BedDAOImpl().getAvailableBeds();
            if (availableBeds.isEmpty()) {
                new Alert(Alert.AlertType.INFORMATION, "No beds are currently available.").showAndWait();
                return;
            }

            ChoiceDialog<String> dialog = new ChoiceDialog<>(availableBeds.get(0), availableBeds);
            dialog.setTitle("Move Patient");
            dialog.setHeaderText("Move " + selectedPatient.getName() + " to a new bed.");
            dialog.setContentText("Select a new bed:");

            dialog.showAndWait().ifPresent(newBedId -> {
                try {
                    patientService.movePatient(selectedPatient.getId(), newBedId);
                    refreshPatientList();
                    new Alert(Alert.AlertType.INFORMATION, "Patient moved successfully.").showAndWait();
                } catch (Exception e) {
                    new Alert(Alert.AlertType.ERROR, "Failed to move patient: " + e.getMessage()).showAndWait();
                    e.printStackTrace();
                }
            });

        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Failed to load available beds: " + e.getMessage()).showAndWait();
            e.printStackTrace();
        }
    }

    // Reloads patients into the list and toggles the empty-state label
    private void refreshPatientList() {
        try {
            List<Patient> patients = patientService.getAllPatients();
            patientListView.setItems(FXCollections.observableArrayList(patients));
            patientListView.setVisible(!patients.isEmpty());
            emptyLabel.setVisible(patients.isEmpty());
            if (patients.isEmpty()) {
                emptyLabel.setText("There are no patients in the system.");
            }
        } catch (Exception e) {
            emptyLabel.setText("Error loading patients: " + e.getMessage());
            e.printStackTrace();
        }
    }
}