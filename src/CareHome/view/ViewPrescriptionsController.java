package CareHome.view;

import CareHome.Model.Medical.Prescription;
import CareHome.Service.PrescriptionServiceImpl;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class ViewPrescriptionsController {

    @FXML
    private TableView<Prescription> prescriptionTable;

    private final PrescriptionServiceImpl prescriptionService = new PrescriptionServiceImpl();

    // Creates columns programmatically and loads all prescriptions into the table
    @FXML
    public void initialize() {
        try {
            List<Prescription> allPrescriptions = prescriptionService.getAllPrescriptions();

            TableColumn<Prescription, String> patientCol = new TableColumn<>("Patient ID");
            patientCol.setCellValueFactory(new PropertyValueFactory<>("patientId"));

            TableColumn<Prescription, String> doctorCol = new TableColumn<>("Doctor ID");
            doctorCol.setCellValueFactory(new PropertyValueFactory<>("doctorId"));

            TableColumn<Prescription, String> medCol = new TableColumn<>("Medication");
            medCol.setCellValueFactory(new PropertyValueFactory<>("medicationName"));

            TableColumn<Prescription, String> dosageCol = new TableColumn<>("Dosage");
            dosageCol.setCellValueFactory(new PropertyValueFactory<>("dosage"));

            TableColumn<Prescription, String> freqCol = new TableColumn<>("Frequency");
            freqCol.setCellValueFactory(new PropertyValueFactory<>("frequency"));

            prescriptionTable.getColumns().addAll(patientCol, doctorCol, medCol, dosageCol, freqCol);
            prescriptionTable.setItems(FXCollections.observableArrayList(allPrescriptions));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
