package CareHome.Model.Location;

import CareHome.Model.Gender;
import CareHome.Model.Person.Patient;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Room {
    private String roomId;
    private List<Bed> beds;
    private int maxCapacity;

    public Room(String roomId, String wardId, int capacity) {
        this.roomId = roomId;
        this.maxCapacity = capacity;
        this.beds = new ArrayList<>();
        for (int i = 1; i <= capacity; i++) {
            beds.add(new Bed(roomId + "B" + i, wardId, Integer.parseInt(roomId.substring(3))));
        }
    }

    public boolean hasAvailableSpace() {
        return beds.stream().anyMatch(Bed::isAvailable);
    }

    public boolean canAccommodateGender(Gender gender) {
        for (Bed bed : beds) {
            if (bed.isOccupied() && bed.getCurrentPatient() != null && bed.getCurrentPatient().getGender() != gender) {
                return false;
            }
        }
        return true;
    }

    public List<Bed> getAvailableBeds() {
        List<Bed> available = new ArrayList<>();
        for (Bed bed : beds) {
            if (bed.isAvailable()) {
                available.add(bed);
            }
        }
        return available;
    }

    public String getRoomId() {
        return roomId;
    }

    public List<Bed> getAllBeds() {
        return beds;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }
}