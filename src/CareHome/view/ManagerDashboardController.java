package CareHome.view;

import CareHome.Service.AuthenticationService;
import CareHome.Service.AuthenticationServiceImpl;
import CareHome.Model.Audit.AuditLogger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ManagerDashboardController {

    // Opens the ward overview in a new window
    @FXML
    private void handleViewWards(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/CareHome/view/WardView.fxml"));
            Scene scene = new Scene(loader.load(),1000,800);
            Stage stage = new Stage();
            stage.setTitle("Ward Overview");
            stage.setScene(scene);
            //stage.setWidth(2000);
            //stage.setHeight(1800);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Opens the Add Patient dialog as a modal window
    @FXML
    private void handleAddNewPatient(ActionEvent event) {
        try {
            CareHomeApp.openModalPercent("/CareHome/view/AddPatientView.fxml",
                    "Add New Patient", 0.5, 0.5, false);
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

    // Opens the Patient Management view as a modal window
    @FXML
    private void handleManagePatients(ActionEvent event) {
        try {
            CareHomeApp.openModalPercent("/CareHome/view/PatientView.fxml",
                    "Patient Management", 0.5, 0.5, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Opens the Staff Management view as a modal window
    @FXML
    private void handleViewStaff(ActionEvent event) {
        try {
            CareHomeApp.openModalPercent("/CareHome/view/StaffView.fxml",
                    "Staff Management", 0.6, 0.7, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Opens the Shift Management view as a modal window
    @FXML
    private void handleAssignShifts(ActionEvent event) {
        try {
            CareHomeApp.openModalPercent("/CareHome/view/ShiftManagementView.fxml",
                    "Shift Management", 0.95, 0.7, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Opens the Audit Log/Reports view as a modal window
    @FXML
    private void handleLogReports(ActionEvent event) {
        try {
            CareHomeApp.openModalPercent("/CareHome/view/ReportView.fxml",
                    "System Audit Log", 0.9, 0.8, true);
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
