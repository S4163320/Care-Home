package CareHome.Model.Person;


import CareHome.Model.Gender;
import java.time.LocalDate;

public class Nurse extends Staff {
    private String nursingLicenseNumber; // Nurse's license number

    // Constructor for nurse
    public Nurse(String id, String firstName, String lastName, Gender gender,
                 int age, String staffId, String username, String password,
                 String nursingLicenseNumber) {
        super(id, firstName, lastName, gender, age, staffId, username, password);
        this.nursingLicenseNumber = nursingLicenseNumber;
    }

    @Override
    public String getRole() {
        return "Nurse";
    }

    @Override
    public boolean canPrescribeMedication() {
        return false; // Nurses CANNOT write prescriptions
    }

    @Override
    public boolean canAdministerMedication() {
        return true; // ONLY nurses can give medications
    }

    @Override
    public int getMaxHoursPerDay() {
        return 8; // Maximum 8 hours per day per requirements
    }

    // Check if nurse can move patients between beds
    @Override
    public boolean canMovePatients() {
        return true; // Nurses can move patients
    }

    public String getNursingLicenseNumber() { return nursingLicenseNumber; }
}

