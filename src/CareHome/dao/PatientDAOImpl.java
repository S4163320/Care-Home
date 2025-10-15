package CareHome.dao;

import CareHome.Model.Gender;
import CareHome.Model.Person.Patient;
import CareHome.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PatientDAOImpl implements PatientDAO {

    // Inserts a new active patient row into patients
    @Override
    public void save(Patient patient) throws Exception {
        String sql = "INSERT INTO patients (id, first_name, last_name, gender, age, patient_id, admission_date, needs_isolation, is_active) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, patient.getId());
            stmt.setString(2, patient.getFirstName());
            stmt.setString(3, patient.getLastName());
            stmt.setString(4, patient.getGender().toString());
            stmt.setInt(5, patient.getAge());
            stmt.setString(6, patient.getPatientId());
            stmt.setString(7, patient.getAdmissionDate().toString());
            stmt.setBoolean(8, patient.needsIsolation());
            stmt.setBoolean(9, true);
            stmt.executeUpdate();
        }
    }

    // Retrieves an active patient by internal id (not patient_id)
    @Override
    public Patient findById(String id) throws Exception {
        String sql = "SELECT * FROM patients WHERE id = ? AND is_active = true";
        try (Connection conn = DatabaseConfig.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return createPatientFromResultSet(rs);
                }
            }
        }
        return null;
    }

    // Returns all active patients
    @Override
    public List<Patient> findAll() throws Exception {
        return getAllPatients();
    }

    // Updates mutable patient attributes by id
    @Override
    public void update(Patient patient) throws Exception {
        String sql = "UPDATE patients SET first_name = ?, last_name = ?, age = ?, needs_isolation = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, patient.getFirstName());
            stmt.setString(2, patient.getLastName());
            stmt.setInt(3, patient.getAge());
            stmt.setBoolean(4, patient.needsIsolation());
            stmt.setString(5, patient.getId());
            stmt.executeUpdate();
        }
    }

    @Override
    public void delete(String id) throws Exception {
        String sql = "UPDATE patients SET is_active = false WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            stmt.executeUpdate();
        }
    }

    @Override
    public List<Patient> getAllPatients() throws Exception {
        List<Patient> patients = new ArrayList<>();
        String sql = "SELECT * FROM patients WHERE is_active = true";
        try (Connection conn = DatabaseConfig.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                patients.add(createPatientFromResultSet(rs));
            }
        }
        return patients;
    }

    @Override
    public String getPatientBed(String patientId) throws Exception {
        String sql = "SELECT bed_id FROM patient_bed WHERE patient_id = ? AND end_time IS NULL";
        try (Connection conn = DatabaseConfig.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, patientId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("bed_id");
                }
            }
        }
        return null;
    }

    @Override
    public Patient getPatientByBed(String bedId) throws Exception {
        String sql = "SELECT p.* FROM patients p JOIN patient_bed pb ON p.id = pb.patient_id WHERE pb.bed_id = ? AND pb.end_time IS NULL";
        try (Connection conn = DatabaseConfig.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, bedId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return createPatientFromResultSet(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Patient> findByWardId(String wardId) throws Exception {
        List<Patient> patients = new ArrayList<>();
        String sql = "SELECT p.* FROM patients p JOIN patient_bed pb ON p.id = pb.patient_id WHERE pb.bed_id LIKE ? AND pb.end_time IS NULL AND p.is_active = true";
        try (Connection conn = DatabaseConfig.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, wardId + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    patients.add(createPatientFromResultSet(rs));
                }
            }
        }
        return patients;
    }

    @Override
    public Patient findByBedId(String bedId) throws Exception {
        return getPatientByBed(bedId);
    }

    @Override
    public void updateBedAssignment(String patientId, String bedId) throws Exception {
        // This is a complex operation involving ending the old assignment and starting a new one.
        // For now, we assume this is handled in the service layer.
    }

    // Soft-deactivates a patient and closes their active bed assignment
    @Override
    public void discharge(String patientId) throws Exception {
        String sql = "UPDATE patients SET is_active = false WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, patientId);
            stmt.executeUpdate();
        }
        String sql2 = "UPDATE patient_bed SET end_time = CURRENT_TIMESTAMP WHERE patient_id = ? AND end_time IS NULL";
        try (Connection conn = DatabaseConfig.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql2)) {
            stmt.setString(1, patientId);
            stmt.executeUpdate();
        }
    }

    // Starts a new patient_bed assignment for a patient
    @Override
    public void assignBedToPatient(String patientId, String bedId) throws Exception {
        String sql = "INSERT INTO patient_bed (patient_id, bed_id, start_time) VALUES (?, ?, CURRENT_TIMESTAMP)";
        try (Connection conn = DatabaseConfig.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, patientId);
            stmt.setString(2, bedId);
            stmt.executeUpdate();
        }
    }

    // Closes the current patient_bed assignment for a patient
    @Override
    public void endBedAssignment(String patientId) throws Exception {
        String sql = "UPDATE patient_bed SET end_time = CURRENT_TIMESTAMP WHERE patient_id = ? AND end_time IS NULL";
        try (Connection conn = DatabaseConfig.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, patientId);
            stmt.executeUpdate();
        }
    }

    // Maps a ResultSet row to a Patient domain object
    private Patient createPatientFromResultSet(ResultSet rs) throws SQLException {
        Patient patient = new Patient(
                rs.getString("id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                Gender.valueOf(rs.getString("gender")),
                rs.getInt("age"),
                rs.getString("patient_id"),
                LocalDate.parse(rs.getString("admission_date"))
        );
        patient.setNeedsIsolation(rs.getBoolean("needs_isolation"));
        return patient;
    }
}