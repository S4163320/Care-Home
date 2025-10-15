package CareHome.TestCases;

import CareHome.Model.Audit.AuditEntry;
import CareHome.Model.Audit.AuditLogger;
import CareHome.Model.ActionType;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AuditLoggerTest {

    private AuditLogger auditLogger;
    private String RUN_TAG; // simple marker to find only entries from this test run

    @BeforeMethod
    public void setUp() {
        auditLogger = new AuditLogger();
        RUN_TAG = "[TEST-" + System.nanoTime() + "]";   // unique tag per run
    }

    private List<AuditEntry> all() throws Exception {
        return auditLogger.getAllAuditEntries();
    }

    private List<AuditEntry> entriesForThisRun(List<AuditEntry> all) {
        List<AuditEntry> picked = new ArrayList<>();
        for (AuditEntry e : all) {
            String d = e.getDetails();
            if (d != null && d.contains(RUN_TAG)) {
                picked.add(e);
            }
        }
        return picked;
    }

    private int countForStaff(List<AuditEntry> list, String staffId) {
        int c = 0;
        for (AuditEntry e : list) {
            if (staffId.equals(e.getStaffId())) c++;
        }
        return c;
    }

    @Test
    public void testCreationIncreasesCount_forThisRunOnly() throws Exception {
        int before = entriesForThisRun(all()).size();

        auditLogger.logAction("MGR001", ActionType.ADD_PATIENT,
                "Added patient Alice " + RUN_TAG, "P001");

        int after = entriesForThisRun(all()).size();
        Assert.assertEquals(after, before + 1, "Count for THIS test run should increase by 1");
    }

    @Test
    public void testEntryDetailsSaved() throws Exception {
        auditLogger.logAction("DOC001", ActionType.ADD_PRESCRIPTION,
                "Added aspirin " + RUN_TAG, "P001");

        List<AuditEntry> runEntries = entriesForThisRun(all());
        Assert.assertTrue(runEntries.size() > 0, "Should have at least one entry for this run");

        AuditEntry last = runEntries.get(runEntries.size() - 1);
        Assert.assertEquals(last.getStaffId(), "DOC001");
        Assert.assertEquals(last.getActionType(), ActionType.ADD_PRESCRIPTION);
        Assert.assertEquals(last.getTargetId(), "P001");
        Assert.assertNotNull(last.getTimestamp());
    }

    @Test
    public void testFilterByStaff_doneInTest() throws Exception {
        auditLogger.logAction("MGR001", ActionType.ADD_PATIENT, "A1 " + RUN_TAG, "P001");
        auditLogger.logAction("DOC001", ActionType.ADD_PRESCRIPTION, "A2 " + RUN_TAG, "P001");
        auditLogger.logAction("MGR001", ActionType.ADD_STAFF, "A3 " + RUN_TAG, "S001");

        List<AuditEntry> runEntries = entriesForThisRun(all());

        int mgr = countForStaff(runEntries, "MGR001");
        int doc = countForStaff(runEntries, "DOC001");

        Assert.assertEquals(mgr, 2, "Two entries expected for MGR001 in this run");
        Assert.assertEquals(doc, 1, "One entry expected for DOC001 in this run");
    }

    @Test
    public void testDateRange_todayVsTomorrow_doneInTest() throws Exception {
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        auditLogger.logAction("MGR001", ActionType.ADD_PATIENT, "Today " + RUN_TAG, "P001");

        List<AuditEntry> runEntries = entriesForThisRun(all());

        int todayCount = 0;
        int tomorrowCount = 0;
        for (AuditEntry e : runEntries) {
            if (e.getTimestamp() == null) continue;
            LocalDate d = e.getTimestamp().toLocalDate();
            if (!d.isBefore(today) && !d.isAfter(today)) todayCount++;
            if (d.isEqual(tomorrow)) tomorrowCount++;
        }

        Assert.assertTrue(todayCount > 0, "Should find entries for today (this run)");
        Assert.assertEquals(tomorrowCount, 0, "Should find no entries for tomorrow (this run)");
    }

    @Test
    public void testDataIntegrity_simpleReread() throws Exception {
        auditLogger.logAction("NUR001", ActionType.ADMINISTER_MEDICATION,
                "Given medication " + RUN_TAG, "P001");

        List<AuditEntry> firstRead = entriesForThisRun(all());
        Assert.assertTrue(firstRead.size() > 0);
        AuditEntry last1 = firstRead.get(firstRead.size() - 1);

        String details1 = last1.getDetails();
        String staff1 = last1.getStaffId();

        List<AuditEntry> secondRead = entriesForThisRun(all());
        AuditEntry last2 = secondRead.get(secondRead.size() - 1);

        Assert.assertEquals(last2.getDetails(), details1, "Details should persist after re-read");
        Assert.assertEquals(last2.getStaffId(), staff1, "Staff ID should persist after re-read");
    }
}
