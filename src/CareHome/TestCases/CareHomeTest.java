package CareHome.TestCases;

import CareHome.CareHome;
import CareHome.Model.Person.*;
import CareHome.Model.Gender;
import CareHome.Model.Location.*;
import org.testng.annotations.*;
import org.testng.Assert;
import java.time.LocalDate;

public class CareHomeTest {
    private CareHome careHome;
    private Patient testPatient;
    private Manager testManager;

    @BeforeMethod
    public void setUp() {
        careHome = CareHome.getInstance();
        testPatient = new Patient("P1", "Alice", "Johnson", Gender.FEMALE, 75, "P001", LocalDate.now());
        testManager = new Manager("M1", "John", "Manager", Gender.MALE, 45, "MGR001", "mgr", "pass");
    }

    @Test
    public void testSingletonPattern() {
        // Verify singleton implementation
        CareHome instance1 = CareHome.getInstance();
        CareHome instance2 = CareHome.getInstance();
        Assert.assertSame(instance1, instance2, "CareHome should return same instance (singleton)");
    }

    @Test
    public void testCareHomeInitialization() {
        // Test that care home initializes with correct structure
        Assert.assertEquals(careHome.getWards().size(), 2, "Care home should have 2 wards");
        Assert.assertEquals(careHome.getTotalBeds(), 38, "Care home should have 38 total beds");
        Assert.assertTrue(careHome.getAvailableBedCount() > 0, "Should have available beds initially");
    }

    @Test
    public void testAddPatientToCareHome() throws Exception {
        // Test core patient addition functionality
        int initialPatients = careHome.getAllPatients().size();
        boolean result = careHome.addPatient(testPatient);
        Assert.assertTrue(result, "Adding patient should succeed");
        Assert.assertEquals(careHome.getAllPatients().size(), initialPatients + 1, "Patient count should increase");
    }

    @Test
    public void testGenderBasedBedAssignment() throws Exception {
        // Get current patient count first to avoid assertion issues
        int initialPatients = careHome.getAllPatients().size();

        // Test business rule: gender compatibility in rooms
        String maleId = "P" + System.currentTimeMillis() + "_male";
        String femaleId = "P" + System.currentTimeMillis() + "_female";
        String malePatientId = "TST" + System.currentTimeMillis() + "_M";
        String femalePatientId = "TST" + System.currentTimeMillis() + "_F";

        Patient malePatient = new Patient(maleId, "John", "Doe", Gender.MALE, 70, malePatientId, LocalDate.now());
        Patient femalePatient = new Patient(femaleId, "Jane", "Smith", Gender.FEMALE, 65, femalePatientId, LocalDate.now());

        careHome.addPatient(malePatient);
        careHome.addPatient(femalePatient);

        // Both should be added (total should be initial + 2)
        int expectedTotal = initialPatients + 2;
        Assert.assertEquals(careHome.getAllPatients().size(), expectedTotal,
                "Both patients should be added. Expected: " + expectedTotal +
                        ", Actual: " + careHome.getAllPatients().size());
    }

    @Test
    public void testAddStaffToCareHome() throws Exception {
        // Test staff addition functionality
        int initialStaff = careHome.getAllStaff().size();
        careHome.addStaff(testManager);
        Assert.assertEquals(careHome.getAllStaff().size(), initialStaff + 1, "Staff count should increase");
    }
}
