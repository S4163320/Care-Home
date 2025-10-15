package CareHome.Model.Location;

import CareHome.Model.Person.Patient;
import java.io.Serializable;

public class Bed  {
    private String bedId;
    private String wardId;
    private int roomNumber;
    private Patient currentPatient;
    private boolean isOccupied;

    public Bed(String bedId, String wardId, int roomNumber) {
        this.bedId = bedId;
        this.wardId = wardId;
        this.roomNumber = roomNumber;
        this.currentPatient = null;
        this.isOccupied = false;
    }

    // Assign a patient to this bed
    public void assignPatient(Patient patient) {
        if (isOccupied) {
            throw new IllegalStateException("Bed " + bedId + " is already occupied");
        }
        this.currentPatient = patient;
        this.isOccupied = true;
    }

    // Remove patient from bed (discharge or move)
    public Patient removePatient() {
        if (!isOccupied) {
            throw new IllegalStateException("Bed " + bedId + " is already empty");
        }
        Patient patient = this.currentPatient;
        this.currentPatient = null;
        this.isOccupied = false;
        return patient;
    }

    // Check if bed is available for new patient
    public boolean isAvailable() {
        return !isOccupied;
    }

    // Getters
    public String getBedId() {
        return bedId;
    }

    public String getWardId() {
        return wardId;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public Patient getCurrentPatient() {
        return currentPatient;
    }

    public boolean isOccupied() {
        return isOccupied;
    }
    public String toString() {
        return "Bed " + bedId + (isOccupied ? " (Occupied by " + currentPatient.getName() + ")" : " (Available)");
    }
}