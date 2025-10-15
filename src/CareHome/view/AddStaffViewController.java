package CareHome.view;

import CareHome.Model.Gender;
import CareHome.Model.Person.Doctor;
import CareHome.Model.Person.Manager;
import CareHome.Model.Person.Nurse;
import CareHome.Model.Person.Staff;
import CareHome.Model.Audit.AuditLogger;
import CareHome.Model.ActionType;
import CareHome.dao.StaffDAO;
import CareHome.dao.StaffDAOImpl;
import CareHome.util.IdGenerator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddStaffViewController {

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField ageField;
    @FXML private ComboBox<Gender> genderComboBox;
    @FXML private TextField staffIdField;
    @FXML private TextField usernameField;
    @FXML private TextField passwordField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private Label errorLabel;

    private StaffDAO staffDAO = new StaffDAOImpl();
    private AuditLogger auditLogger = new AuditLogger();

    // Initializes gender and role options for the form
    @FXML
    public void initialize() {
        genderComboBox.getItems().setAll(Gender.values());
        roleComboBox.getItems().setAll("Manager", "Doctor", "Nurse");
    }

    // Creates a Staff subtype based on role, saves it, audits, and closes the dialog
    @FXML
    private void handleSave(ActionEvent event) {
        try {
            String role = roleComboBox.getValue();
            String firstName = firstNameField.getText();
            String lastName = lastNameField.getText();
            String ageText = ageField.getText();
            Gender gender = genderComboBox.getValue();
            String username = usernameField.getText();
            String password = passwordField.getText();

            if (role == null || firstName.trim().isEmpty() || lastName.trim().isEmpty() ||
                ageText.trim().isEmpty() || gender == null || username.trim().isEmpty() ||
                password.trim().isEmpty()) {
                errorLabel.setText("All fields are required.");
                return;
            }

            // Generate next ID using the new IdGenerator
            String staffId = IdGenerator.getNextStaffId(role);

            int age = Integer.parseInt(ageText);
            Staff staff;

            switch (role) {
                case "Doctor":
                    staff = new Doctor(
                            "DOC" + System.currentTimeMillis(), // internal DB id
                            firstName, lastName,
                            gender, age,
                            staffId, username,
                            password, "DefaultLicense");
                    break;

                case "Nurse":
                    staff = new Nurse(
                            "NUR" + System.currentTimeMillis(),
                            firstName, lastName,
                            gender, age,
                            staffId, username,
                            password, "DefaultLicense");
                    break;

                case "Manager":
                    staff = new Manager(
                            "MGR" + System.currentTimeMillis(),
                            firstName, lastName,
                            gender, age,
                            staffId, username,
                            password);
                    break;

                default:
                    throw new IllegalArgumentException("Invalid role selected");
            }

            staffDAO.save(staff);
            auditLogger.logAction(staff.getId(), ActionType.ADD_STAFF,
                    "New staff member added", staff.getStaffId());
            System.out.println("SUCCESS: Staff " + staff.getName() +
                    " added with ID " + staff.getStaffId());

            Stage stage = (Stage) firstNameField.getScene().getWindow();
            stage.close();

        } catch (NumberFormatException e) {
            errorLabel.setText("Age must be a number.");
        } catch (Exception e) {
            errorLabel.setText("Error saving staff: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
