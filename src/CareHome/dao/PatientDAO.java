package CareHome.dao;

import CareHome.Model.Person.Patient;
import java.util.List;

public interface PatientDAO extends BaseDAO<Patient, String> {
    List<Patient> findByWardId(String wardId) throws Exception;
    Patient findByBedId(String bedId) throws Exception;
    void updateBedAssignment(String patientId, String bedId) throws Exception;
    void discharge(String patientId) throws Exception;
    List<Patient> getAllPatients() throws Exception;
    Patient getPatientByBed(String bedId) throws Exception;
    void update(Patient patient) throws Exception;
    void delete(String id) throws Exception;
    String getPatientBed(String patientId) throws Exception;
    void assignBedToPatient(String patientId, String bedId) throws Exception;
    void endBedAssignment(String patientId) throws Exception;
}