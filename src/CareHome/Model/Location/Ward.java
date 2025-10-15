package CareHome.Model.Location;

import CareHome.Model.Gender;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Ward {
    private String wardId; // Unique ward identifier (e.g., "W1", "W2")
    private List<Room> rooms; // All rooms in this ward
    private String wardName; // Descriptive name (e.g., "High Care Ward")

    // Constructor for new ward
    public Ward(String wardId, String wardName) {
        this.wardId = wardId;
        this.wardName = wardName;
        this.rooms = new ArrayList<>();
    }

    // Add a room to this ward
    public void addRoom(Room room) {
        rooms.add(room);
    }

    // Find all available beds in this ward
    public List<Bed> getAllAvailableBeds() {
        List<Bed> availableBeds = new ArrayList<>();
        for (Room room : rooms) {
            availableBeds.addAll(room.getAvailableBeds());
        }
        return availableBeds;
    }

    // Find available bed suitable for patient gender
    public Bed findAvailableBedForGender(Gender gender) {
        for (Room room : rooms) {
            if (room.canAccommodateGender(gender) && room.hasAvailableSpace()) {
                return room.getAvailableBeds().get(0); // Return first available bed
            }
        }
        return null; // No suitable bed found
    }

    // Getters
    public String getWardId() { return wardId; }
    public String getWardName() { return wardName; }
    public List<Room> getAllRooms() { return new ArrayList<>(rooms); }

    @Override
    public String toString() {
        return wardName + " (" + wardId + ") - " + getAllAvailableBeds().size() + " beds available";
    }
}

