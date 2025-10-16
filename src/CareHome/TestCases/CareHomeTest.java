package CareHome.TestCases;

import CareHome.CareHome;
import CareHome.Model.Person.*;
import CareHome.Model.Gender;
import CareHome.Model.Location.*;
import CareHome.Exception.ComplianceException;
import org.testng.annotations.*;
import org.testng.Assert;
import java.time.LocalDate;

/**
 * Core test suite for CareHome demonstrating main business rules:
 * - Adding patient to vacant bed succeeds
 * - Adding patient to occupied bed fails with diagnostic message
 * - Gender compatibility enforcement
 * - Patient discharge frees bed
 */
public class CareHomeTest {
    private CareHome careHome;

    @BeforeMethod
    public void setUp() {
        careHome = CareHome.getInstance();
    }

    // ========== CORE BUSINESS RULE TESTS ==========

    @Test
    public void testAddPatientToVacantBed_ShouldSucceed() throws Exception {
        // BUSINESS RULE: Adding a new resident to a vacant bed should succeed
        int initialAvailableBeds = careHome.getAvailableBedCount();
        int initialPatients = careHome.getAllPatients().size();
        
        String uniqueId = "VACANT_" + System.nanoTime();
        Patient patient = new Patient(uniqueId, "Emily", "Brown", Gender.FEMALE, 68, uniqueId + "_PID", LocalDate.now());
        
        boolean result = careHome.addPatient(patient);
        
        Assert.assertTrue(result, "Adding patient to vacant bed should succeed");
        Assert.assertEquals(careHome.getAllPatients().size(), initialPatients + 1, 
            "Patient count should increase by 1 after successful addition");
        Assert.assertEquals(careHome.getAvailableBedCount(), initialAvailableBeds - 1, 
            "Available bed count should decrease by 1 after patient assignment");
        
        System.out.println("✓ PASSED: Patient successfully added to vacant bed");
    }

    @Test
    public void testBedOccupancyPreventsDoubleAssignment() throws Exception {
        // BUSINESS RULE: Attempting to assign a patient to an already occupied bed should fail
        String uniqueId = "OCCUPIED_" + System.nanoTime();
        Patient patient1 = new Patient(uniqueId + "_1", "First", "Patient", Gender.MALE, 70, uniqueId + "_PID1", LocalDate.now());
        Patient patient2 = new Patient(uniqueId + "_2", "Second", "Patient", Gender.MALE, 65, uniqueId + "_PID2", LocalDate.now());
        
        // Add first patient successfully
        careHome.addPatient(patient1);
        
        // Find the bed assigned to patient1
        Bed occupiedBed = findPatientBed(patient1);
        Assert.assertNotNull(occupiedBed, "First patient should be assigned to a bed");
        Assert.assertTrue(occupiedBed.isOccupied(), "Bed should be marked as occupied");
        
        // Try to assign second patient to the same bed directly (should fail)
        try {
            occupiedBed.assignPatient(patient2);
            Assert.fail("Should not allow assigning patient to already occupied bed - Expected IllegalStateException");
        } catch (IllegalStateException e) {
            Assert.assertTrue(e.getMessage().contains("already occupied"), 
                "Error message should indicate bed is already occupied. Got: " + e.getMessage());
            System.out.println("✓ PASSED: Occupied bed correctly rejected second patient with message: " + e.getMessage());
        }
    }

    @Test
    public void testGenderCompatibilityInSharedRooms() throws Exception {
        // BUSINESS RULE: Gender compatibility must be enforced in multi-bed rooms
        String uniqueId = "GENDER_" + System.nanoTime();
        
        Patient malePatient = new Patient(uniqueId + "_M", "Michael", "Smith", Gender.MALE, 72, uniqueId + "_MPID", LocalDate.now());
        careHome.addPatient(malePatient);
        
        Bed maleBed = findPatientBed(malePatient);
        Room maleRoom = findRoomContainingBed(maleBed);
        
        if (maleRoom.getMaxCapacity() > 1) {
            Patient femalePatient = new Patient(uniqueId + "_F", "Fiona", "Jones", Gender.FEMALE, 68, uniqueId + "_FPID", LocalDate.now());
            careHome.addPatient(femalePatient);
            
            Bed femaleBed = findPatientBed(femalePatient);
            Room femaleRoom = findRoomContainingBed(femaleBed);
            
            Assert.assertNotEquals(maleRoom.getRoomId(), femaleRoom.getRoomId(), 
                "Female patient should not be assigned to same shared room as male patient");
            
            System.out.println("✓ PASSED: Gender compatibility enforced - patients in separate rooms");
        } else {
            System.out.println("⊘ SKIPPED: Male patient in single-bed room");
        }
    }

    @Test
    public void testMovePatientToOccupiedBed_ShouldFail() throws Exception {
        // BUSINESS RULE: Moving a patient to an occupied bed should fail with diagnostic message
        String uniqueId = "MOVE_" + System.nanoTime();
        
        Patient patient1 = new Patient(uniqueId + "_1", "First", "Resident", Gender.FEMALE, 75, uniqueId + "_PID1", LocalDate.now());
        Patient patient2 = new Patient(uniqueId + "_2", "Second", "Resident", Gender.FEMALE, 77, uniqueId + "_PID2", LocalDate.now());
        
        careHome.addPatient(patient1);
        careHome.addPatient(patient2);
        
        Bed bed2 = findPatientBed(patient2);
        
        try {
            careHome.movePatientToBed(patient1, bed2.getBedId());
            Assert.fail("Should not allow moving patient to occupied bed");
        } catch (ComplianceException e) {
            Assert.assertTrue(e.getMessage().toLowerCase().contains("occupied"), 
                "Error message should indicate bed is occupied. Got: " + e.getMessage());
            System.out.println("✓ PASSED: Move to occupied bed rejected with message: " + e.getMessage());
        }
    }

    @Test
    public void testDischargePatientFreesBed() throws Exception {
        // BUSINESS RULE: Discharging a patient should free their bed for new occupants
        String uniqueId = "DISCHARGE_" + System.nanoTime();
        
        Patient patient = new Patient(uniqueId, "Temporary", "Patient", Gender.MALE, 65, uniqueId + "_PID", LocalDate.now());
        careHome.addPatient(patient);
        
        Bed assignedBed = findPatientBed(patient);
        int bedsBeforeDischarge = careHome.getAvailableBedCount();
        
        careHome.dischargePatient(patient.getId());
        
        Assert.assertFalse(assignedBed.isOccupied(), "Bed should be vacant after discharge");
        Assert.assertEquals(careHome.getAvailableBedCount(), bedsBeforeDischarge + 1, 
            "Available bed count should increase by 1 after discharge");
        
        System.out.println("✓ PASSED: Patient discharged and bed " + assignedBed.getBedId() + " is now available");
    }

    @Test
    public void testCareHomeInitialization() {
        // Verify care home structure
        Assert.assertEquals(careHome.getWards().size(), 2, "Care home should have 2 wards");
        Assert.assertEquals(careHome.getTotalBeds(), 38, "Care home should have 38 total beds");
        
        System.out.println("✓ PASSED: CareHome initialized correctly - 2 wards, 38 beds");
    }

    //  HELPER METHODS

    private Bed findPatientBed(Patient patient) {
        for (Ward ward : careHome.getWards()) {
            for (Room room : ward.getAllRooms()) {
                for (Bed bed : room.getAllBeds()) {
                    if (bed.isOccupied() && bed.getCurrentPatient().getId().equals(patient.getId())) {
                        return bed;
                    }
                }
            }
        }
        return null;
    }

    private Room findRoomContainingBed(Bed bed) {
        for (Ward ward : careHome.getWards()) {
            for (Room room : ward.getAllRooms()) {
                if (room.getAllBeds().contains(bed)) {
                    return room;
                }
            }
        }
        return null;
    }
}
