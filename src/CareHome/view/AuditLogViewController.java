package CareHome.view;

import CareHome.Model.Audit.AuditEntry;
import CareHome.Model.Audit.AuditLogger;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class AuditLogViewController {

    @FXML private TableView<AuditEntry> auditTable;
    @FXML private TableColumn<AuditEntry, String> entryIdCol;
    @FXML private TableColumn<AuditEntry, String> staffIdCol;
    @FXML private TableColumn<AuditEntry, String> actionTypeCol;
    @FXML private TableColumn<AuditEntry, String> targetIdCol;
    @FXML private TableColumn<AuditEntry, String> detailsCol;
    @FXML private TableColumn<AuditEntry, String> timestampCol;

    private AuditLogger auditLogger;

    // Injects the logger from outside and triggers initial table load
    public void setAuditLogger(AuditLogger logger) throws Exception {
        this.auditLogger = logger;
        loadData(); //  Load data once set
    }

    // Configures cell value factories for table columns
    @FXML
    public void initialize() {
        entryIdCol.setCellValueFactory(new PropertyValueFactory<>("entryId"));
        staffIdCol.setCellValueFactory(new PropertyValueFactory<>("staffId"));
        actionTypeCol.setCellValueFactory(new PropertyValueFactory<>("actionType"));
        targetIdCol.setCellValueFactory(new PropertyValueFactory<>("targetId"));
        detailsCol.setCellValueFactory(new PropertyValueFactory<>("details"));
        timestampCol.setCellValueFactory(new PropertyValueFactory<>("timestampFormatted"));
    }

    // Reads audit entries from the logger and populates the table
    private void loadData() throws Exception {
        if (auditLogger != null) {
            auditTable.setItems(FXCollections.observableArrayList(auditLogger.getAllAuditEntries()));
        }
    }
}
