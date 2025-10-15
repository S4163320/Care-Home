package CareHome.dao;

import CareHome.Model.Person.*;
import CareHome.Model.Gender;
import CareHome.config.DatabaseConfig;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StaffDAOImpl implements StaffDAO {

    // Inserts a new active staff member row with type-specific license info
    public void save(Staff staff) throws Exception {
        String sql = "INSERT INTO staff(id, first_name, last_name, gender, age, staff_id, username, password, staff_type, license_number, is_active) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, staff.getId());
            pstmt.setString(2, staff.getFirstName());
            pstmt.setString(3, staff.getLastName());
            pstmt.setString(4, staff.getGender().name());
            pstmt.setInt(5, staff.getAge());
            pstmt.setString(6, staff.getStaffId());
            pstmt.setString(7, staff.getUsername());
            pstmt.setString(8, staff.getPassword());
            pstmt.setString(9, staff.getClass().getSimpleName().toUpperCase());
            pstmt.setString(10, getLicenseNumber(staff));
            pstmt.setBoolean(11, true);
            pstmt.executeUpdate();
        }
    }

    // Updates a staff member’s password by username
    @Override
    public void updatePassword(String username, String newPassword) throws Exception {
        String sql = "UPDATE staff SET password = ? WHERE username = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newPassword);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
        }
    }

    // Retrieves an active staff member by internal id
    @Override
    public Staff findById(String id) throws Exception {
        String sql = "SELECT * FROM staff WHERE id = ? AND is_active = true";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return createStaffFromResultSet(rs);
            return null;
        }
    }

    // Retrieves an active staff member by username
    @Override
    public Staff findByUsername(String username) throws Exception {
        String sql = "SELECT * FROM staff WHERE username = ? AND is_active = true";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return createStaffFromResultSet(rs);
            return null;
        }
    }

    // Retrieves an active staff member by staff_id
    @Override
    public Staff findByStaffId(String staffId) throws Exception {
        String sql = "SELECT * FROM staff WHERE staff_id = ? AND is_active = true";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, staffId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return createStaffFromResultSet(rs);
            return null;
        }
    }

    // Returns all active staff members
    @Override
    public List<Staff> findAll() throws Exception {
        String sql = "SELECT * FROM staff WHERE is_active = true";
        List<Staff> staffList = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                try {
                    staffList.add(createStaffFromResultSet(rs));
                } catch (IllegalArgumentException ex) {
                    System.err.println("Skipping unknown staff type: " + ex.getMessage());
                }
            }
        }
        return staffList;
    }

    // Updates password by username
    @Override
    public void updatePasswordByUsername(String username, String newPassword) throws Exception {
        String sql = "UPDATE staff SET password = ? WHERE username = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newPassword);
            stmt.setString(2, username);
            stmt.executeUpdate();
        }
    }


    // Maps a ResultSet row to the appropriate Staff subtype (Doctor/Nurse/Manager)
    private Staff createStaffFromResultSet(ResultSet rs) throws SQLException {
        String staffType = rs.getString("staff_type").toUpperCase().trim();
        String id = rs.getString("id");
        String firstName = rs.getString("first_name");
        String lastName = rs.getString("last_name");
        Gender gender = Gender.valueOf(rs.getString("gender"));
        int age = rs.getInt("age");
        String staffId = rs.getString("staff_id");
        String username = rs.getString("username");
        String password = rs.getString("password");
        String licenseNumber = rs.getString("license_number");
        switch (staffType) {
            case "DOCTOR":
                return new Doctor(id, firstName, lastName, gender, age, staffId, username, password, licenseNumber);
            case "NURSE":
                return new Nurse(id, firstName, lastName, gender, age, staffId, username, password, licenseNumber);
            case "MANAGER":
                return new Manager(id, firstName, lastName, gender, age, staffId, username, password);
            default:
                throw new IllegalArgumentException("Unknown staff type: " + staffType);
        }
    }

    // Extracts the license number field if the staff subtype has one, else null
    private String getLicenseNumber(Staff staff) {
        if (staff instanceof Doctor) {
            return ((Doctor) staff).getMedicalLicenseNumber();
        } else if (staff instanceof Nurse) {
            return ((Nurse) staff).getNursingLicenseNumber();
        }
        return null;
    }
}
