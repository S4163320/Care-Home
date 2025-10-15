package CareHome.Model.Audit;

import CareHome.Model.ActionType;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import CareHome.dao.AuditDAO;
import CareHome.dao.AuditDAOImpl;

public class AuditLogger implements Serializable {
    private AuditDAO auditDAO;
    private List<AuditEntry> auditTrail; // All audit entries


    public AuditLogger() {
        this.auditDAO = new AuditDAOImpl();
    }

    public void logAction(String staffId, ActionType actionType, String details, String targetId) {
        String entryId = "AUDIT" + String.format("%06d", System.currentTimeMillis() % 1000000);
        AuditEntry entry = new AuditEntry(entryId, staffId, actionType, details, targetId);
        try {
            auditDAO.save(entry);
            System.out.println("AUDIT: " + entry.getFormattedDescription()); // For debugging
        } catch (Exception e) {
            System.err.println("Failed to save audit log to database: " + e.getMessage());
            e.printStackTrace();
        }
    }
    // Get all audit entries within a date range
    public List<AuditEntry> getAuditTrail(LocalDate fromDate, LocalDate toDate) {
        List<AuditEntry> filteredEntries = new ArrayList<>();
        for (AuditEntry entry : auditTrail) {
            LocalDate entryDate = entry.getTimestamp().toLocalDate();
            if (!entryDate.isBefore(fromDate) && !entryDate.isAfter(toDate)) {
                filteredEntries.add(entry);
            }
        }
        return filteredEntries;
    }

    // Get all audit entries for a specific staff member
    public List<AuditEntry> getAuditTrailForStaff(String staffId) {
        List<AuditEntry> staffEntries = new ArrayList<>();
        for (AuditEntry entry : auditTrail) {
            if (entry.getStaffId().equals(staffId)) {
                staffEntries.add(entry);
            }
        }
        return staffEntries;
    }

    public List<AuditEntry> getAllAuditEntries() throws Exception {
        return auditDAO.findAll();
    }

    public int getTotalEntries() {
        return auditTrail.size();
    }
}
