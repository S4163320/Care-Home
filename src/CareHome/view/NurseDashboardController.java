package CareHome.view;

import CareHome.Service.AuthenticationService;
import CareHome.Service.AuthenticationServiceImpl;
import CareHome.Model.Audit.AuditLogger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class NurseDashboardController {

    // Opens the ward overview in a new window
    @FXML
    private void handleViewWards(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/CareHome/view/WardView.fxml"));
            Scene scene = new Scene(loader.load(), 1000, 800);
            Stage stage = new Stage();
            stage.setTitle("Ward Overview");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Opens the Patient Information view as a modal window
    @FXML
    private void handleInfoPatients(ActionEvent event) {
        try {
            CareHomeApp.openModalPercent("/CareHome/view/PatientInfo.fxml",
                    "Patients Information", 0.85, 0.8, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Opens the Patient Management view (move) as a modal window
    @FXML
    private void handleManagePatients(ActionEvent event) {
        try {
            CareHomeApp.openModalPercent("/CareHome/view/PatientView.fxml",
                    "Patient Management", 0.5, 0.5, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Opens the All Prescriptions view as a modal window
    @FXML
    private void handleViewPrescriptions(ActionEvent event) {
        try {
            CareHomeApp.openModalPercent("/CareHome/view/ViewPrescriptionsView.fxml",
                    "All Prescriptions", 0.6, 0.65, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Logs out by switching the primary stage back to the Login view
    @FXML
    private void handleLogout(ActionEvent event) throws Exception {
        CareHomeApp.switchScene("/CareHome/view/LoginView.fxml",
                "Care Home - Login", CareHomeApp.LOGIN_W, CareHomeApp.LOGIN_H, false);
    }
}
