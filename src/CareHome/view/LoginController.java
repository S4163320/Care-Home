package CareHome.view;

import CareHome.Service.AuthenticationService;
import CareHome.Service.AuthenticationServiceImpl;
import CareHome.Model.Audit.AuditLogger;
import CareHome.Model.Person.Staff;
import CareHome.Model.Person.Manager;
import CareHome.Model.Person.Doctor;
import CareHome.Model.Person.Nurse;
import CareHome.Exception.AuthorizationException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;


    private AuthenticationService authenticationService = new AuthenticationServiceImpl(new AuditLogger());

    // Handles login button: authenticates and routes user to role-specific dashboard
    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        try {
            Staff user = authenticationService.login(username, password);

            if (user == null) {
                errorLabel.setText("Invalid credentials. Try again.");
                return;
            }

            if (user instanceof Manager) {
                CareHomeApp.switchScene("/CareHome/view/ManagerDashboardController.fxml",
                        "Dashboard - Manager", CareHomeApp.DASHBOARD_W, CareHomeApp.DASHBOARD_H, true);
            } else if (user instanceof Doctor) {
                CareHomeApp.switchScene("/CareHome/view/DoctorDashboardController.fxml",
                        "Dashboard - Doctor", CareHomeApp.DASHBOARD_W, CareHomeApp.DASHBOARD_H, true);
            } else if (user instanceof Nurse) {
                CareHomeApp.switchScene("/CareHome/view/NurseDashboardController.fxml",
                        "Dashboard - Nurse", CareHomeApp.DASHBOARD_W, CareHomeApp.DASHBOARD_H, true);
            } else {
                errorLabel.setText("Unknown role.");
            }

        } catch (AuthorizationException e) {
            errorLabel.setText("Login failed: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Error loading dashboard.");
        }
    }
}
