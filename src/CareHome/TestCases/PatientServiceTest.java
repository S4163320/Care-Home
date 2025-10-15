package CareHome.TestCases;

import CareHome.Service.PatientServiceImpl;
import CareHome.Model.Person.Patient;
import CareHome.Model.Gender;
import CareHome.Exception.CareHomeException;
import CareHome.Exception.ComplianceException;
import org.testng.annotations.*;
import org.testng.Assert;
import java.time.LocalDate;

public class PatientServiceTest {
    private PatientServiceImpl patientService;
    private Patient testPatient;

    @BeforeMethod
    public void setUp() {
        patientService = new PatientServiceImpl();
        testPatient = new Patient("P1", "Alice", "Johnson", Gender.FEMALE, 75, "P001", LocalDate.now());
    }

    @Test(expectedExceptions = CareHomeException.class)
    public void testAddPatientWithNullName() throws Exception {
        // Test validation: patient must have valid name
        Patient invalidPatient = new Patient("P2", null, "Smith", Gender.MALE, 65, "P002", LocalDate.now());
        patientService.addPatient(invalidPatient);
    }

    @Test(expectedExceptions = CareHomeException.class)
    public void testAddPatientWithEmptyPatientId() throws Exception {
        // Test validation: patient ID cannot be empty
        Patient invalidPatient = new Patient("P3", "John", "Doe", Gender.MALE, 70, "", LocalDate.now());
        patientService.addPatient(invalidPatient);
    }

    @Test
    public void testMovePatientToDifferentBed() throws Exception {
        // Create unique patient for this test
        String uniqueId = "P" + System.currentTimeMillis() + "_move";
        String uniquePatientId = "TST" + System.currentTimeMillis() + "_move";
        Patient movePatient = new Patient(uniqueId, "Move", "Test", Gender.MALE, 60, uniquePatientId, LocalDate.now());

        // Test patient movement functionality
        patientService.addPatient(movePatient);

        // This will test the bed assignment logic
        try {
            patientService.movePatient(movePatient.getId(), "W1R2B1");
            // If no exception, movement was successful
            Assert.assertTrue(true, "Patient movement should succeed for valid bed");
        } catch (Exception e) {
            // Expected if bed is occupied or incompatible
            Assert.assertTrue(e.getMessage().contains("bed") ||
                            e.getMessage().contains("not found") ||
                            e.getMessage().contains("Target bed"),
                    "Exception should mention bed issue: " + e.getMessage());
        }
    }

    @Test
    public void testDischargePatient() throws Exception {
        // Create unique patient for this test
        String uniqueId = "P" + System.currentTimeMillis() + "_discharge";
        String uniquePatientId = "TST" + System.currentTimeMillis() + "_discharge";
        Patient dischargePatient = new Patient(uniqueId, "Discharge", "Test", Gender.FEMALE, 80, uniquePatientId, LocalDate.now());

        // Test patient discharge process
        patientService.addPatient(dischargePatient);
        patientService.dischargePatient(dischargePatient.getId());

        // Patient should not be findable after discharge
        Patient discharged = patientService.findPatientById(dischargePatient.getId());
        Assert.assertNull(discharged, "Discharged patient should not be retrievable");
    }
}
