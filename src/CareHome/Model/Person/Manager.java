package CareHome.Model.Person;

import CareHome.Model.Gender;
import java.time.LocalDate;

public class Manager extends Staff {

    // Constructor for manager
    public Manager(String id, String firstName, String lastName, Gender gender,
                   int age, String staffId, String username, String password) {
        super(id, firstName, lastName, gender, age, staffId, username, password);
    }

    @Override
    public String getRole() {
        return "Manager";
    }

    @Override
    public boolean canPrescribeMedication() {
        return false; // Managers are not medical staff
    }

    @Override
    public boolean canAdministerMedication() {
        return false; // Managers are not medical staff
    }

    @Override
    public int getMaxHoursPerDay() {
        return 12; // Managers can work longer hours if needed
    }

    @Override
    public boolean canMovePatients() {
        return true; // Managers can move patients
    }

    // Manager-specific methods
}
