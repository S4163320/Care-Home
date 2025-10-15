package CareHome.Model.Person;

import CareHome.Model.Gender;
import java.time.LocalDate;

public abstract class Staff extends Person {
    protected String staffId; // Unique staff identification number
    protected String username; // Login username
    protected String password; // Encrypted password (simplified as String for now)
    protected boolean isActive; // Whether staff member is currently employed

    // Constructor for all staff members
    public Staff(String id, String firstName, String lastName, Gender gender,
                 int age, String staffId, String username, String password) {
        super(id, firstName, lastName, gender, age);
        this.staffId = staffId;
        this.username = username;
        this.password = password; // Simple hash simulation
        this.isActive = true; // New staff members are active by default
    }

    // Check if provided password matches stored password
    public boolean checkPassword(String password) {
        return this.password.equals(password);
    }

    // Abstract methods that each staff type must implement
    public abstract boolean canPrescribeMedication(); // Can this staff type write prescriptions?
    public abstract boolean canAdministerMedication(); // Can this staff type give medications?
    public abstract int getMaxHoursPerDay(); // Maximum work hours allowed per day
    public abstract boolean canMovePatients();

    // Getters and setters
    public String getStaffId() { return staffId; }
    public String getUsername() { return username; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public String getPassword() {
        return password;
    }

    // Change password (only manager should call this)
    public void changePassword(String newPassword) {
        this.password = newPassword;
    }


}

