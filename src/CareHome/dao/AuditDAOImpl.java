package CareHome.dao;

import CareHome.Model.ActionType;
import CareHome.Model.Audit.AuditEntry;
import CareHome.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AuditDAOImpl implements AuditDAO {

    // Persists a single audit entry row into audit_log
    @Override
    public void save(AuditEntry entry) throws Exception {
        String sql = "INSERT INTO audit_log (entry_id, staff_id, action_type, target_id, details, timestamp) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, entry.getEntryId());
            stmt.setString(2, entry.getStaffId());
            stmt.setString(3, entry.getActionType().name());
            stmt.setString(4, entry.getTargetId());
            stmt.setString(5, entry.getDetails());
            stmt.setString(6, entry.getTimestamp().toString());
            stmt.executeUpdate();
        }
    }

    // Retrieves all audit entries ordered by most recent first
    @Override
    public List<AuditEntry> findAll() throws Exception {
        List<AuditEntry> entries = new ArrayList<>();
        String sql = "SELECT * FROM audit_log ORDER BY timestamp DESC";
        try (Connection conn = DatabaseConfig.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                entries.add(createEntryFromResultSet(rs));
            }
        }
        return entries;
    }

    private AuditEntry createEntryFromResultSet(ResultSet rs) throws SQLException {
        AuditEntry entry = new AuditEntry(
                rs.getString("entry_id"),
                rs.getString("staff_id"),
                ActionType.valueOf(rs.getString("action_type")),
                rs.getString("details"),
                rs.getString("target_id")
        );
        entry.setTimestamp(LocalDateTime.parse(rs.getString("timestamp")));
        return entry;
    }
}
