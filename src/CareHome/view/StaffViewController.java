package CareHome.view;

import CareHome.Model.Person.Staff;
import CareHome.Model.Audit.AuditLogger;
import CareHome.Model.ActionType;
import CareHome.Service.AuthenticationServiceImpl;
import CareHome.dao.StaffDAO;
import CareHome.dao.StaffDAOImpl;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;

public class StaffViewController {

    @FXML
    private TableView<Staff> staffTable;
    @FXML
    private TableColumn<Staff, String> idColumn;
    @FXML
    private TableColumn<Staff, String> nameColumn;
    @FXML
    private TableColumn<Staff, String> roleColumn;
    @FXML
    private TableColumn<Staff, String> usernameColumn;

    private StaffDAO staffDAO = new StaffDAOImpl();

    // Maps columns to staff properties and loads the initial staff list
    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("staffId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));

        refreshStaffList();
    }

    // Opens the Add Staff dialog and refreshes the table on close
    @FXML
    private void handleAddStaff() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/CareHome/view/AddStaffView.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setTitle("Add New Staff");
            stage.setScene(scene);
            stage.setWidth(1000);
            stage.setHeight(800);

            // Refresh the table when the add dialog is closed
            stage.setOnHidden(e -> refreshStaffList());

            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Prompts for a new password and updates the selected staff member
    @FXML
    private void handleChangePassword() {
        Staff selectedStaff = staffTable.getSelectionModel().getSelectedItem();
        if (selectedStaff == null) {
            System.out.println("No staff member selected.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Change Password");
        dialog.setHeaderText("Change password for " + selectedStaff.getName());
        dialog.setContentText("Please enter new password:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newPassword -> {
            try {
                staffDAO.updatePassword(selectedStaff.getUsername(), newPassword);
                new AuditLogger().logAction(new AuthenticationServiceImpl(new AuditLogger()).getCurrentUser().getStaffId(), ActionType.MODIFY_STAFF, "Changed password for user: " + selectedStaff.getUsername(), selectedStaff.getStaffId());
                System.out.println("Password updated successfully for " + selectedStaff.getUsername());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    // Reloads staff from the DAO and binds to the table
    private void refreshStaffList() {
        System.out.println("DEBUG: Refreshing staff list...");
        try {
            List<Staff> staffList = staffDAO.findAll();
            staffTable.setItems(FXCollections.observableArrayList(staffList));
        } catch (Exception e) {
            e.printStackTrace();

        }
    }
}
