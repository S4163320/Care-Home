package CareHome.Service;

import CareHome.Model.Person.Patient;
import CareHome.Model.Location.Bed;
import java.util.List;

public interface PatientService {
    void addPatient(Patient patient) throws Exception;
    Patient findPatientById(String patientId) throws Exception;
    List<Patient> getAllPatients() throws Exception;
    void movePatient(String patientId, String newBedId) throws Exception;
    void dischargePatient(String patientId) throws Exception;

}
