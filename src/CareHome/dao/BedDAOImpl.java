package CareHome.dao;

import CareHome.Model.Location.Bed;
import CareHome.config.DatabaseConfig;
import CareHome.config.IsolationConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class BedDAOImpl implements BedDAO {

    // Assigns a patient to a free bed atomically and marks it occupied
    @Override
    public void assignPatientToBed(String bedId, String patientId) throws Exception {
        String sql = "UPDATE beds SET patient_id = ?, is_occupied = TRUE WHERE bed_id = ? AND is_occupied = FALSE";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, patientId);
            stmt.setString(2, bedId);

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated == 0) {
                throw new Exception("Bed " + bedId + " is not available or doesn't exist");
            }
        }
    }

    // Frees a bed by clearing patient_id and setting is_occupied = FALSE
    @Override
    public void freeBed(String bedId) throws Exception {
        String sql = "UPDATE beds SET patient_id = NULL, is_occupied = FALSE WHERE bed_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, bedId);
            stmt.executeUpdate();
        }
    }

    // Returns the bed_id currently occupied by the given patient, or null if none
    @Override
    public String findPatientBed(String patientId) throws Exception {
        String sql = "SELECT bed_id FROM beds WHERE patient_id = ? AND is_occupied = TRUE";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, patientId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("bed_id");
            }
            return null;
        }
    }

    // Lists all bed_ids that are currently not occupied
    @Override
    public List<String> getAvailableBeds() throws Exception {
        String sql = "SELECT bed_id FROM beds WHERE is_occupied = FALSE ORDER BY bed_id";
        List<String> availableBeds = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                availableBeds.add(rs.getString("bed_id"));
            }
        }
        return availableBeds;
    }

    // Returns all beds with basic attributes (ward, room, id)
    @Override
    public List<Bed> getAllBeds() throws Exception {
        List<Bed> allBeds = new ArrayList<>();
        String sql = "SELECT * FROM beds ORDER BY ward_id, room_number, bed_id";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                allBeds.add(new Bed(rs.getString("bed_id"), rs.getString("ward_id"), rs.getInt("room_number")));
            }
        }
        return allBeds;
    }

    // Checks whether a bed exists and is currently free to be assigned
    @Override
    public boolean isBedAvailable(String bedId) throws Exception {
        String sql = "SELECT is_occupied FROM beds WHERE bed_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, bedId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return !rs.getBoolean("is_occupied");
            }
            return false;
        }
    }

    // Returns a single free bed id according to gender compatibility
    @Override
    public String findSuitableBed(String patientGender, boolean needsIsolation) throws Exception {
        try (Connection conn = DatabaseConfig.getConnection()) {

            // ===== ISOLATION PATIENTS =====
            if (needsIsolation) {
                if (IsolationConfig.MODE == IsolationConfig.Mode.RESERVED_ONLY) {
                    // OPTION 1: Use only RESERVED_ISOLATION_BEDS, but ensure the ENTIRE room is empty
                    if (!IsolationConfig.RESERVED_ISOLATION_BEDS.isEmpty()) {
                        // Build IN (?, ?, ?, ?)
                        StringJoiner sj = new StringJoiner(",", "(", ")");
                        int placeholders = IsolationConfig.RESERVED_ISOLATION_BEDS.size();
                        for (int i = 0; i < placeholders; i++) sj.add("?");

                        String sql = """
                            SELECT b1.bed_id
                            FROM beds b1
                            WHERE b1.bed_id IN %s
                              AND b1.is_occupied = FALSE
                              AND NOT EXISTS (
                                  SELECT 1
                                  FROM beds b2
                                  WHERE b2.ward_id = b1.ward_id
                                    AND b2.room_number = b1.room_number
                                    AND b2.is_occupied = TRUE
                              )
                            ORDER BY b1.ward_id, b1.room_number, b1.bed_id
                            LIMIT 1
                        """.formatted(sj.toString());

                        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                            int i = 1;
                            for (String bedId : IsolationConfig.RESERVED_ISOLATION_BEDS) {
                                stmt.setString(i++, bedId);
                            }
                            try (ResultSet rs = stmt.executeQuery()) {
                                if (rs.next()) return rs.getString("bed_id");
                            }
                        }
                    }

                    // No reserved empty room available → no placement (strict policy).
                    return null;
                } else {
                    // OPTION 2: Any empty room (no occupied bed in that room)
                    String emptyRoomSql = """
                        SELECT b1.bed_id
                        FROM beds b1
                        WHERE b1.is_occupied = FALSE
                          AND NOT EXISTS (
                                SELECT 1
                                FROM beds b2
                                WHERE b2.ward_id = b1.ward_id
                                  AND b2.room_number = b1.room_number
                                  AND b2.is_occupied = TRUE
                          )
                        ORDER BY b1.ward_id, b1.room_number, b1.bed_id
                        LIMIT 1
                    """;
                    try (PreparedStatement stmt = conn.prepareStatement(emptyRoomSql);
                         ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) return rs.getString("bed_id");
                    }
                    return null; // no empty rooms
                }
            }

            // ===== NON-ISOLATION PATIENTS =====
            // Gender-compatible room (no occupant in that room with opposite gender).
            // If using RESERVED_ONLY, keep non-isolation patients OUT of reserved rooms.
            String reservedExclusion = "";
            List<String> reserved = new ArrayList<>(IsolationConfig.RESERVED_ISOLATION_BEDS);
            if (IsolationConfig.MODE == IsolationConfig.Mode.RESERVED_ONLY && !reserved.isEmpty()) {
                StringJoiner sj = new StringJoiner(",", "(", ")");
                for (int i = 0; i < reserved.size(); i++) sj.add("?");
                reservedExclusion = "AND b1.bed_id NOT IN " + sj;
            }

            String compatibleSql = """
                SELECT b1.bed_id
                FROM beds b1
                WHERE b1.is_occupied = FALSE
                  %s
                  AND NOT EXISTS (
                        SELECT 1
                        FROM beds b2
                        JOIN patients p ON p.id = b2.patient_id
                        WHERE b2.ward_id = b1.ward_id
                          AND b2.room_number = b1.room_number
                          AND b2.is_occupied = TRUE
                          AND p.gender <> ?
                  )
                ORDER BY b1.ward_id, b1.room_number, b1.bed_id
                LIMIT 1
            """.formatted(reservedExclusion);

            try (PreparedStatement stmt = conn.prepareStatement(compatibleSql)) {
                int idx = 1;
                if (!reservedExclusion.isBlank()) {
                    for (String bedId : reserved) {
                        stmt.setString(idx++, bedId);
                    }
                }
                stmt.setString(idx, patientGender);  // "MALE"/"FEMALE"
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) return rs.getString("bed_id");
                }
            }
        }

        // Nothing suitable found
        return null;
    }

    @Override
    public boolean isRoomGenderCompatible(String bedId, String patientGender) throws Exception {
        String sql = """
            SELECT COUNT(*)
            FROM beds b
            JOIN patients p ON p.id = b.patient_id
            WHERE b.ward_id = (SELECT ward_id FROM beds WHERE bed_id = ?)
              AND b.room_number = (SELECT room_number FROM beds WHERE bed_id = ?)
              AND b.is_occupied = TRUE
              AND p.gender <> ?
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, bedId);
            stmt.setString(2, bedId);
            stmt.setString(3, patientGender);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0;
                }
            }
        }
        return true; // If room is empty or something goes wrong, assume compatible
    }


    // Returns total number of beds in the system
    @Override
    public int getTotalBeds() throws Exception {
        String sql = "SELECT COUNT(*) FROM beds";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        }
    }

    // Returns number of currently free beds
    @Override
    public int getAvailableBedCount() throws Exception {
        String sql = "SELECT COUNT(*) FROM beds WHERE is_occupied = FALSE";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        }
    }
}