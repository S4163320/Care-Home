package CareHome.Model.Audit;

import CareHome.Model.ActionType;
import java.io.Serializable;
import java.time.LocalDateTime;

public class AuditEntry implements Serializable {
    private String entryId;         // Unique identifier for this audit entry
    private String staffId;         // ID of staff who performed the action
    private ActionType actionType;  // Type of action performed
    private LocalDateTime timestamp; // When the action was performed
    private String details;         // Additional details about the action
    private String targetId;        // ID of patient/staff/bed affected by action

    // Constructor for new audit entry
    public AuditEntry(String entryId, String staffId, ActionType actionType,
                      String details, String targetId) {
        this.entryId = entryId;
        this.staffId = staffId;
        this.actionType = actionType;
        this.details = details;
        this.targetId = targetId;
        this.timestamp = LocalDateTime.now(); // Record current time
    }

    // Create a formatted description of the action
    public String getFormattedDescription() {
        return String.format("[%s] Staff %s performed %s on %s: %s",
                timestamp.toString(), staffId, actionType, targetId, details);
    }

    // Getters
    public String getEntryId() { return entryId; }
    public String getStaffId() { return staffId; }
    public ActionType getActionType() { return actionType; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getDetails() { return details; }
    public String getTargetId() { return targetId; }

    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return getFormattedDescription();
    }
}