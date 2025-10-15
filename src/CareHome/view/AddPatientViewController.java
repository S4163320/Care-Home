package CareHome.view;

import CareHome.Model.Gender;
import CareHome.Model.Person.Patient;
import CareHome.Service.PatientService;
import CareHome.Service.PatientServiceImpl;
import CareHome.util.IdGenerator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;

public class AddPatientViewController {

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField ageField;
    @FXML private ComboBox<Gender> genderComboBox;
    @FXML private TextField patientIdField;
    @FXML private DatePicker admissionDatePicker;
    @FXML private Label errorLabel;
    @FXML private CheckBox isolationCheckBox;

    private PatientService patientService = new PatientServiceImpl();

    // Initializes gender choices and defaults the admission date to today
    @FXML
    public void initialize() {
        genderComboBox.getItems().setAll(Gender.values());
        admissionDatePicker.setValue(LocalDate.now());
        if (isolationCheckBox != null) {
            isolationCheckBox.setSelected(false); // default off
        }
    }

    // Builds a Patient from form fields, calls service to save, and closes the dialog
    @FXML
    private void handleSave(ActionEvent event) {
        try {
            Patient patient = new Patient(
                    IdGenerator.getNextPatientId(),
                    firstNameField.getText(),
                    lastNameField.getText(),
                    genderComboBox.getValue(),
                    Integer.parseInt(ageField.getText()),
                    patientIdField.getText(),
                    admissionDatePicker.getValue()
            );
            if (isolationCheckBox != null) {
                patient.setNeedsIsolation(isolationCheckBox.isSelected());
            }

            patientService.addPatient(patient);

            Stage stage = (Stage) firstNameField.getScene().getWindow();
            stage.close();

        } catch (NumberFormatException e) {
            errorLabel.setText("Age must be a number.");
        } catch (Exception e) {
            errorLabel.setText("Error saving patient: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
