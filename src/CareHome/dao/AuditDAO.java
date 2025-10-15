package CareHome.dao;

import CareHome.Model.Audit.AuditEntry;
import java.util.List;

public interface AuditDAO {
    void save(AuditEntry entry) throws Exception;
    List<AuditEntry> findAll() throws Exception;
}
