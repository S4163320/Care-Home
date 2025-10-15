package CareHome.Model.Person;

import CareHome.Model.Gender;
import java.time.LocalDate;

public class Patient extends Person {
    private String patientId; // Unique patient identification number
    private LocalDate admissionDate; // Date when patient was admitted
    private boolean needsIsolation; // Whether patient needs private room
    private String isolationDisplay;
    private String bedIdDisplay;

    // Constructor for new patient
    public Patient(String id, String firstName, String lastName, Gender gender,
                   int age, String patientId, LocalDate admissionDate) {
        super(id, firstName, lastName, gender, age);
        this.patientId = patientId;
        this.admissionDate = admissionDate;
        this.needsIsolation = false; // Default to no isolation needed
    }

    @Override
    public String getRole() {
        return "Patient";
    }

    // Getters and setters
    public String getPatientId() { return patientId; }
    public LocalDate getAdmissionDate() { return admissionDate; }
    public boolean needsIsolation() { return needsIsolation; }
    public void setNeedsIsolation(boolean needsIsolation) { this.needsIsolation = needsIsolation; }

    // Calculate days since admission
    public long getDaysInCare() {
        return admissionDate.until(LocalDate.now()).getDays();
    }

    @Override
    public String toString() {
        return getFirstName() + " " + getLastName() + " (" + getPatientId() + ")";
    }

    public String getIsolationDisplay() {
        return isolationDisplay;
    }

    public void setIsolationDisplay(String isolationDisplay) {
        this.isolationDisplay = isolationDisplay;
    }

    public String getBedIdDisplay() {
        return bedIdDisplay;
    }

    public void setBedIdDisplay(String bedIdDisplay) {
        this.bedIdDisplay = bedIdDisplay;
    }

}

