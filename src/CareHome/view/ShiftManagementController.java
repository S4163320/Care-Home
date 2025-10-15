package CareHome.view;

import CareHome.Model.Schedule.Shift;
import CareHome.Model.Person.Staff;
import CareHome.Model.ShiftType;
import CareHome.controller.ShiftController;
import CareHome.dao.ShiftDAO;
import CareHome.dao.ShiftDAOImpl;
import CareHome.dao.StaffDAO;
import CareHome.dao.StaffDAOImpl;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.DayOfWeek;
import java.util.List;

import CareHome.Service.AuthenticationService;
import CareHome.Service.AuthenticationServiceImpl;
import CareHome.Model.Audit.AuditLogger;
import javafx.scene.control.Alert;

public class ShiftManagementController {

    @FXML private TableView<Shift> shiftTable;
    @FXML private TableColumn<Shift, String> staffIdColumn;
    @FXML private TableColumn<Shift, DayOfWeek> dayColumn;
    @FXML private TableColumn<Shift, String> shiftTypeColumn;

    @FXML private ComboBox<Staff> staffComboBox;
    @FXML private ComboBox<DayOfWeek> dayComboBox;
    @FXML private ComboBox<ShiftType> shiftTypeComboBox;

    private ShiftDAO shiftDAO = new ShiftDAOImpl();
    private StaffDAO staffDAO = new StaffDAOImpl();
    private ShiftController shiftController;

    // Initializes controller dependencies, populates dropdowns, and loads existing shifts
    @FXML
    public void initialize() {
        AuthenticationService authService = new AuthenticationServiceImpl(new AuditLogger());
        this.shiftController = new ShiftController(authService, new AuditLogger());

        staffIdColumn.setCellValueFactory(new PropertyValueFactory<>("staffId"));
        dayColumn.setCellValueFactory(new PropertyValueFactory<>("dayOfWeek"));
        shiftTypeColumn.setCellValueFactory(new PropertyValueFactory<>("shiftType"));

        try {
            staffComboBox.setItems(FXCollections.observableArrayList(staffDAO.findAll()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        dayComboBox.getItems().setAll(DayOfWeek.values());
        shiftTypeComboBox.getItems().setAll(ShiftType.values());

        refreshShiftList();
    }

    // Assigns a shift to the selected staff for the selected day/type, with error/confirmation prompts
    @FXML
    private void handleAssignShift(ActionEvent event) {
        try {
            Staff selectedStaff = staffComboBox.getValue();
            DayOfWeek selectedDay = dayComboBox.getValue();
            ShiftType selectedShiftType = shiftTypeComboBox.getValue();

            if (selectedStaff != null && selectedDay != null && selectedShiftType != null) {
                shiftController.assignShift(selectedStaff.getStaffId(), selectedDay, selectedShiftType.name());
                refreshShiftList();
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Shift assigned successfully.");
                alert.showAndWait();
            }
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to assign shift: " + e.getMessage());
            alert.showAndWait();
            e.printStackTrace();
        }
    }

    // Reloads the table of assigned shifts
    private void refreshShiftList() {
        try {
            List<Shift> shifts = shiftDAO.getAllShifts();
            shiftTable.setItems(FXCollections.observableArrayList(shifts));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
