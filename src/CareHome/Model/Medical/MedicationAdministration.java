package CareHome.Model.Medical;

import java.time.LocalDateTime;

public class MedicationAdministration {
    private String adminId;
    private String prescriptionId;
    private String nurseId;
    private LocalDateTime administeredDate;
    private String notes;
    private boolean completed;

    public MedicationAdministration(String adminId, String prescriptionId,
                                    String nurseId, String notes) {
        this.adminId = adminId;
        this.prescriptionId = prescriptionId;
        this.nurseId = nurseId;
        this.notes = notes;
        this.administeredDate = LocalDateTime.now();
        this.completed = true;
    }

}
