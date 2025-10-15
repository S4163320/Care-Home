package CareHome;

import CareHome.Model.Location.*;
import CareHome.Model.Person.*;
import CareHome.Model.Gender;
import CareHome.Exception.ComplianceException;
import CareHome.Exception.CareHomeException;

import java.util.ArrayList;
import java.util.List;

//Main CareHome class - Singleton pattern

public class CareHome {
    private static CareHome instance;
    private List<Ward> wards;
    private List<Patient> allPatients;
    private List<Staff> allStaff;

    // Private constructor for singleton
    private CareHome() {
        this.wards = new ArrayList<>();
        this.allPatients = new ArrayList<>();
        this.allStaff = new ArrayList<>();
        initializeWards();
    }

    // Singleton getInstance method
    public static synchronized CareHome getInstance() {
        if (instance == null) {
            instance = new CareHome();
        }
        return instance;
    }

    /**
     * Initialize the care home structure: 2 wards, 6 rooms each, 1-4 beds per room
     */
    private void initializeWards() {
        // Ward 1 - High Care Ward
        Ward ward1 = new Ward("W1", "High Care Ward");
        ward1.addRoom(new Room("W1R1", "W1", 1)); // Room 1: 1 bed
        ward1.addRoom(new Room("W1R2", "W1", 2)); // Room 2: 2 beds
        ward1.addRoom(new Room("W1R3", "W1", 4)); // Room 3: 4 beds
        ward1.addRoom(new Room("W1R4", "W1", 4)); // Room 4: 4 beds
        ward1.addRoom(new Room("W1R5", "W1", 4)); // Room 5: 4 beds
        ward1.addRoom(new Room("W1R6", "W1", 4)); // Room 6: 4 beds

        // Ward 2 - Standard Care Ward
        Ward ward2 = new Ward("W2", "Standard Care Ward");
        ward2.addRoom(new Room("W2R1", "W2", 1)); // Room 1: 1 bed
        ward2.addRoom(new Room("W2R2", "W2", 2)); // Room 2: 2 beds
        ward2.addRoom(new Room("W2R3", "W2", 4)); // Room 3: 4 beds
        ward2.addRoom(new Room("W2R4", "W2", 4)); // Room 4: 4 beds
        ward2.addRoom(new Room("W2R5", "W2", 4)); // Room 5: 4 beds
        ward2.addRoom(new Room("W2R6", "W2", 4)); // Room 6: 4 beds

        wards.add(ward1);
        wards.add(ward2);

        System.out.println("CareHome initialized with 2 wards, 38 total beds");
    }

    //Add a new patient to the care home - finds suitable bed automatically

    public boolean addPatient(Patient patient) throws Exception {
        Bed suitableBed = findSuitableBed(patient);
        if (suitableBed == null) {
            return false; // No suitable bed available
        }

        // Assign patient to bed
        suitableBed.assignPatient(patient);
        allPatients.add(patient);

        System.out.println("Patient " + patient.getName() + " assigned to bed " + suitableBed.getBedId());
        return true;
    }

    //Find suitable bed for patient based on gender and isolation needs

    private Bed findSuitableBed(Patient patient) {
        for (Ward ward : wards) {
            // If patient needs isolation, look for single-bed rooms first
            if (patient.needsIsolation()) {
                for (Room room : ward.getAllRooms()) {
                    if (room.getMaxCapacity() == 1 && room.hasAvailableSpace()) {
                        return room.getAvailableBeds().get(0);
                    }
                }
            }

            // Look for regular beds with gender compatibility
            Bed bed = ward.findAvailableBedForGender(patient.getGender());
            if (bed != null) {
                return bed;
            }
        }
        return null;
    }

    //Move patient to different bed with validation

    public void movePatientToBed(Patient patient, String newBedId) throws Exception {
        // Find current bed
        Bed currentBed = findPatientCurrentBed(patient);
        if (currentBed == null) {
            throw new CareHomeException("Patient not found in any bed");
        }

        // Find target bed
        Bed targetBed = findBedById(newBedId);
        if (targetBed == null) {
            throw new CareHomeException("Target bed not found: " + newBedId);
        }

        if (targetBed.isOccupied()) {
            throw new ComplianceException("Target bed is already occupied");
        }

        // Validate gender compatibility
        Room targetRoom = findRoomContainingBed(targetBed);
        if (!targetRoom.canAccommodateGender(patient.getGender())) {
            throw new ComplianceException("Gender incompatibility - cannot move to target bed");
        }

        // Perform the move
        currentBed.removePatient();
        targetBed.assignPatient(patient);

        System.out.println("Patient " + patient.getName() + " moved to bed " + newBedId);
    }

    //Check if patient can move to specific bed

    public boolean canPatientMoveToBed(Patient patient, String bedId) {
        try {
            Bed targetBed = findBedById(bedId);
            if (targetBed == null || targetBed.isOccupied()) {
                return false;
            }

            Room targetRoom = findRoomContainingBed(targetBed);
            return targetRoom.canAccommodateGender(patient.getGender());
        } catch (Exception e) {
            return false;
        }
    }


    // Discharge patient from care home

    public void dischargePatient(String patientId) throws Exception {
        Patient patient = findPatientById(patientId);
        if (patient == null) {
            throw new CareHomeException("Patient not found: " + patientId);
        }

        // Remove from bed
        Bed currentBed = findPatientCurrentBed(patient);
        if (currentBed != null) {
            currentBed.removePatient();
        }

        // Remove from patient list
        allPatients.remove(patient);

        System.out.println("Patient " + patient.getName() + " discharged from care home");
    }

    //Add staff member to care home

    public void addStaff(Staff staff) throws Exception {
        // Check if staff ID already exists
        for (Staff existing : allStaff) {
            if (existing.getStaffId().equals(staff.getStaffId())) {
                throw new ComplianceException("Staff ID already exists: " + staff.getStaffId());
            }
        }

        allStaff.add(staff);
        System.out.println("Staff " + staff.getName() + " (" + staff.getRole() + ") added to care home");
    }

    // Helper methods for finding objects
    private Patient findPatientById(String patientId) {
        return allPatients.stream()
                .filter(p -> p.getId().equals(patientId))
                .findFirst()
                .orElse(null);
    }

    private Bed findPatientCurrentBed(Patient patient) {
        for (Ward ward : wards) {
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

    private Bed findBedById(String bedId) {
        for (Ward ward : wards) {
            for (Room room : ward.getAllRooms()) {
                for (Bed bed : room.getAllBeds()) {
                    if (bed.getBedId().equals(bedId)) {
                        return bed;
                    }
                }
            }
        }
        return null;
    }

    private Room findRoomContainingBed(Bed bed) {
        for (Ward ward : wards) {
            for (Room room : ward.getAllRooms()) {
                if (room.getAllBeds().contains(bed)) {
                    return room;
                }
            }
        }
        return null;
    }

    // Getter methods for accessing care home data
    public List<Ward> getWards() { return new ArrayList<>(wards); }
    public List<Patient> getAllPatients() { return new ArrayList<>(allPatients); }
    public List<Staff> getAllStaff() { return new ArrayList<>(allStaff); }

    // Get total bed count
    public int getTotalBeds() {
        return wards.stream()
                .mapToInt(ward -> ward.getAllRooms().stream()
                        .mapToInt(Room::getMaxCapacity)
                        .sum())
                .sum();
    }

    // Get available bed count
    public int getAvailableBedCount() {
        return wards.stream()
                .mapToInt(ward -> ward.getAllAvailableBeds().size())
                .sum();
    }
}