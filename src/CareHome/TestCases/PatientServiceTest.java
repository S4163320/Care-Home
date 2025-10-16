package CareHome.TestCases;

import CareHome.Service.PatientServiceImpl;
import CareHome.Model.Person.Patient;
import CareHome.Model.Gender;
import org.testng.annotations.*;
import org.testng.Assert;
import java.time.LocalDate;

/**
 * Core test suite for PatientService demonstrating main business rules
 */
public class PatientServiceTest {
    private PatientServiceImpl patientService;

    @BeforeMethod
    public void setUp() {
        patientService = new PatientServiceImpl();
    }

    @Test
    public void testFindNonExistentPatient_ShouldReturnNull() throws Exception {
        // BUSINESS RULE: Searching for non-existent patient should return null
        Patient notFound = patientService.findPatientById("NON_EXISTENT_ID_12345");
        Assert.assertNull(notFound, "Non-existent patient should return null");
        System.out.println("✓ PASSED: Non-existent patient correctly returns null");
    }
}
