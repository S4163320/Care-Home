package CareHome.view;

import CareHome.Model.Audit.AuditEntry;
import CareHome.dao.AuditDAO;
import CareHome.dao.AuditDAOImpl;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDateTime;
import java.util.List;

public class ReportViewController {

    @FXML private TableView<AuditEntry> reportTable;
    @FXML private TableColumn<AuditEntry, String> entryIdColumn;
    @FXML private TableColumn<AuditEntry, String> staffIdColumn;
    @FXML private TableColumn<AuditEntry, String> actionColumn;
    @FXML private TableColumn<AuditEntry, String> targetIdColumn;
    @FXML private TableColumn<AuditEntry, String> detailsColumn;
    @FXML private TableColumn<AuditEntry, LocalDateTime> timestampColumn;

    private AuditDAO auditDAO = new AuditDAOImpl();

    // Wires table columns to properties and loads all audit entries
    @FXML
    public void initialize() {
        entryIdColumn.setCellValueFactory(new PropertyValueFactory<>("entryId"));
        staffIdColumn.setCellValueFactory(new PropertyValueFactory<>("staffId"));
        actionColumn.setCellValueFactory(new PropertyValueFactory<>("actionType"));
        targetIdColumn.setCellValueFactory(new PropertyValueFactory<>("targetId"));
        detailsColumn.setCellValueFactory(new PropertyValueFactory<>("details"));
        timestampColumn.setCellValueFactory(new PropertyValueFactory<>("timestamp"));

        try {
            List<AuditEntry> entries = auditDAO.findAll();
            reportTable.setItems(FXCollections.observableArrayList(entries));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
