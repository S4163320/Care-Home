package CareHome.Model.Person;
import CareHome.Model.Gender;
import java.time.LocalDate;

public class Doctor extends Staff {
    private String medicalLicenseNumber; // Doctor's medical license

    // Constructor for doctor
    public Doctor(String id, String firstName, String lastName, Gender gender,
                  int age, String staffId, String username, String password,
                  String medicalLicenseNumber) {
        super(id, firstName, lastName, gender, age, staffId, username, password);
        this.medicalLicenseNumber = medicalLicenseNumber;
    }

    @Override
    public String getRole() {
        return "Doctor";
    }

    @Override
    public boolean canPrescribeMedication() {
        return true; // ONLY doctors can write prescriptions
    }

    @Override
    public boolean canAdministerMedication() {
        return false; // Doctors don't give medications directly
    }

    @Override
    public int getMaxHoursPerDay() {
        return 8; // Standard work day for doctors
    }

    @Override
    public boolean canMovePatients() {
        return false; // Doctors can't move patients
    }

    public String getMedicalLicenseNumber() { return medicalLicenseNumber; }
}