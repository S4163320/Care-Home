package CareHome.dao;

import CareHome.Model.Medical.Prescription;
import CareHome.config.DatabaseConfig;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PrescriptionDAOImpl implements PrescriptionDAO {

    // Inserts a new prescription row into prescriptions
    @Override
    public void save(Prescription prescription) throws Exception {
        String sql = """
            INSERT INTO prescriptions (prescription_id, patient_id, doctor_id, medication_name, 
                                     dosage, frequency, start_date, end_date, is_active, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, prescription.getPrescriptionId());
            stmt.setString(2, prescription.getPatientId());
            stmt.setString(3, prescription.getDoctorId());
            stmt.setString(4, prescription.getMedicationName());
            stmt.setString(5, prescription.getDosage());
            stmt.setString(6, prescription.getFrequency());
            stmt.setString(7, prescription.getStartDate().toString());
            stmt.setString(8, prescription.getEndDate() != null ? prescription.getEndDate().toString() : null);
            stmt.setBoolean(9, prescription.isActive());
            stmt.setString(10, prescription.getCreatedDate().toString());

            stmt.executeUpdate();
        }
    }

    // Retrieves a prescription by primary key
    @Override
    public Prescription findById(String prescriptionId) throws Exception {
        String sql = "SELECT * FROM prescriptions WHERE prescription_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, prescriptionId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return createPrescriptionFromResultSet(rs);
            }
            return null;
        }
    }

    // Returns all active prescriptions ordered by recency
    @Override
    public List<Prescription> findAll() throws Exception {
        String sql = "SELECT * FROM prescriptions WHERE is_active = TRUE ORDER BY created_at DESC";
        List<Prescription> prescriptions = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                prescriptions.add(createPrescriptionFromResultSet(rs));
            }
        }
        return prescriptions;
    }

    @Override
    public void update(Prescription prescription) throws Exception {
        String sql = """
            UPDATE prescriptions SET medication_name = ?, dosage = ?, frequency = ?, 
                                   start_date = ?, end_date = ?, is_active = ?
            WHERE prescription_id = ?
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, prescription.getMedicationName());
            stmt.setString(2, prescription.getDosage());
            stmt.setString(3, prescription.getFrequency());
            stmt.setString(4, prescription.getStartDate().toString());
            stmt.setString(5, prescription.getEndDate() != null ? prescription.getEndDate().toString() : null);
            stmt.setBoolean(6, prescription.isActive());
            stmt.setString(7, prescription.getPrescriptionId());

            stmt.executeUpdate();
        }
    }

    @Override
    public void delete(String prescriptionId) throws Exception {
        deactivatePrescription(prescriptionId);
    }

    // Lists active prescriptions for a given patient
    @Override
    public List<Prescription> findByPatientId(String patientId) throws Exception {
        String sql = "SELECT * FROM prescriptions WHERE patient_id = ? AND is_active = TRUE ORDER BY created_at DESC";
        List<Prescription> prescriptions = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, patientId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                prescriptions.add(createPrescriptionFromResultSet(rs));
            }
        }
        return prescriptions;
    }


    // Lists all currently active prescriptions
    @Override
    public List<Prescription> findActivePrescriptions() throws Exception {
        String sql = "SELECT * FROM prescriptions WHERE is_active = TRUE ORDER BY created_at DESC";
        List<Prescription> prescriptions = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                prescriptions.add(createPrescriptionFromResultSet(rs));
            }
        }
        return prescriptions;
    }

    // Marks a prescription as inactive by id
    @Override
    public void deactivatePrescription(String prescriptionId) throws Exception {
        String sql = "UPDATE prescriptions SET is_active = FALSE WHERE prescription_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, prescriptionId);
            stmt.executeUpdate();
        }
    }

    // Maps a ResultSet row to a Prescription domain object
    private Prescription createPrescriptionFromResultSet(ResultSet rs) throws SQLException {
        Prescription prescription = new Prescription(
                rs.getString("prescription_id"),
                rs.getString("patient_id"),
                rs.getString("doctor_id"),
                rs.getString("medication_name"),
                rs.getString("dosage"),
                rs.getString("frequency"),
                LocalDate.parse(rs.getString("start_date")),
                rs.getString("end_date") != null ? LocalDate.parse(rs.getString("end_date")) : null
        );
        prescription.setActive(rs.getBoolean("is_active"));
        return prescription;
    }
}