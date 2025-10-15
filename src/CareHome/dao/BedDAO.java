package CareHome.dao;

import CareHome.Model.Location.Bed;
import java.util.List;

public interface BedDAO {
    void assignPatientToBed(String bedId, String patientId) throws Exception;
    void freeBed(String bedId) throws Exception;
    String findPatientBed(String patientId) throws Exception;
    List<String> getAvailableBeds() throws Exception;
    List<Bed> getAllBeds() throws Exception;
    boolean isBedAvailable(String bedId) throws Exception;
    String findSuitableBed(String patientGender, boolean needsIsolation) throws Exception;
    boolean isRoomGenderCompatible(String bedId, String patientGender) throws Exception;
    int getTotalBeds() throws Exception;
    int getAvailableBedCount() throws Exception;
}
