package CareHome.view;

import CareHome.Model.Person.Patient;
import CareHome.Service.AuthenticationServiceImpl;
import CareHome.Service.PatientServiceImpl;
import CareHome.Model.Audit.AuditLogger;
import CareHome.controller.PatientController;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class PatientInfoController {

    @FXML private TableView<Patient> patientTable;

    @FXML private TableColumn<Patient, String> patientIdColumn;
    @FXML private TableColumn<Patient, String> firstNameColumn;
    @FXML private TableColumn<Patient, String> lastNameColumn;
    @FXML private TableColumn<Patient, String> genderColumn;
    @FXML private TableColumn<Patient, Integer> ageColumn;
    @FXML private TableColumn<Patient, String> admissionDateColumn;
    @FXML private TableColumn<Patient, String> isolationColumn;
    @FXML private TableColumn<Patient, String> bedIdColumn;

    private final PatientController patientController;

    // Wires a PatientController with fresh auth/service/logger for this view
    public PatientInfoController() {
        this.patientController = new PatientController(
                new AuthenticationServiceImpl(new AuditLogger()),
                new PatientServiceImpl(),
                new AuditLogger()
        );
    }

    // Configures table columns and loads patient rows on view init
    @FXML
    public void initialize() {
        setupTableColumns();
        loadPatients();
    }

    // Sets property-value factories for each patient column
    private void setupTableColumns() {
        patientIdColumn.setCellValueFactory(new PropertyValueFactory<>("patientId"));
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        genderColumn.setCellValueFactory(new PropertyValueFactory<>("gender"));
        ageColumn.setCellValueFactory(new PropertyValueFactory<>("age"));
        admissionDateColumn.setCellValueFactory(new PropertyValueFactory<>("admissionDate"));
        isolationColumn.setCellValueFactory(new PropertyValueFactory<>("isolationDisplay"));
        bedIdColumn.setCellValueFactory(new PropertyValueFactory<>("bedIdDisplay"));
    }

    // Retrieves patients, enriches rows with isolation/bed display, and populates the table
    private void loadPatients() {
        try {
            List<Patient> patients = patientController.getAllPatients();
            PatientServiceImpl patientService = new PatientServiceImpl();

            for (Patient p : patients) {
                p.setIsolationDisplay(p.needsIsolation() ? "Yes" : "No");
                String bedId = patientService.getPatientBed(p.getId());
                p.setBedIdDisplay(bedId != null ? bedId : "Unassigned");
            }

            patientTable.setItems(FXCollections.observableArrayList(patients));

        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Failed to load patients: " + e.getMessage()).showAndWait();
        }
    }

    // Refreshes the table data from the backing services
    @FXML
    private void handleRefresh() {
        loadPatients();
    }

    // Closes the patient info window
    @FXML
    private void handleClose() {
        Stage stage = (Stage) patientTable.getScene().getWindow();
        stage.close();
    }
}
