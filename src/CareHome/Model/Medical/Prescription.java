package CareHome.Model.Medical;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Prescription {
    private String prescriptionId;
    private String patientId;
    private String doctorId;
    private String medicationName;
    private String dosage;
    private String frequency;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime createdDate;
    private boolean isActive;

    public Prescription(String prescriptionId, String patientId, String doctorId,
                        String medicationName, String dosage, String frequency,
                        LocalDate startDate, LocalDate endDate) {
        this.prescriptionId = prescriptionId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.medicationName = medicationName;
        this.dosage = dosage;
        this.frequency = frequency;
        this.startDate = startDate;
        this.endDate = endDate;
        this.createdDate = LocalDateTime.now();
        this.isActive = true;
    }

    // Getters
    public String getPrescriptionId() { return prescriptionId; }
    public String getPatientId() { return patientId; }
    public String getDoctorId() { return doctorId; }
    public String getMedicationName() { return medicationName; }
    public String getDosage() { return dosage; }
    public String getFrequency() { return frequency; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public boolean isActive() { return isActive; }

    public void setActive(boolean active) { this.isActive = active; }


}
