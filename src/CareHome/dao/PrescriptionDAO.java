package CareHome.dao;

import CareHome.Model.Medical.Prescription;
import java.util.List;

public interface PrescriptionDAO extends BaseDAO<Prescription, String> {
    List<Prescription> findByPatientId(String patientId) throws Exception;
    List<Prescription> findActivePrescriptions() throws Exception;
    void deactivatePrescription(String prescriptionId) throws Exception;
}