package CareHome.Service;

import CareHome.Model.ActionType;
import CareHome.Model.Audit.AuditLogger;
import CareHome.Model.Medical.Prescription;
import CareHome.Model.Medical.MedicationAdministration;
import CareHome.dao.PrescriptionDAO;
import CareHome.dao.PrescriptionDAOImpl;

import java.util.ArrayList;
import java.util.List;

public class PrescriptionServiceImpl implements PrescriptionService {
    private PrescriptionDAO prescriptionDAO;

    // Initializes the service with a concrete PrescriptionDAO
    public PrescriptionServiceImpl() {
        this.prescriptionDAO = new PrescriptionDAOImpl();
    }

    // Saves a prescription to the database
    @Override
    public void addPrescription(Prescription prescription, String doctorId) throws Exception {
        prescriptionDAO.save(prescription);
        System.out.println("Prescription added to database: " + prescription.getMedicationName());
    }

    // Additional database methods
    public List<Prescription> getAllPrescriptions() throws Exception {
        return prescriptionDAO.findAll();
    }

}

