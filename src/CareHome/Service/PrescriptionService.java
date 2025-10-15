package CareHome.Service;

import CareHome.Model.Location.Ward;
import CareHome.Model.Medical.Prescription;
import CareHome.Model.Medical.MedicationAdministration;
import java.util.List;

public interface PrescriptionService {
    void addPrescription(Prescription prescription, String doctorId) throws Exception;
   }
