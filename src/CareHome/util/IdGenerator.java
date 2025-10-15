// src/CareHome/util/IdGenerator.java
package CareHome.util;

import CareHome.config.DatabaseConfig;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class IdGenerator {

    // Generic method to get next incremental ID per prefix
    public static String getNextStaffId(String rolePrefix) throws Exception {
        String prefix;
        switch (rolePrefix.toLowerCase()) {
            case "doctor": prefix = "DOC"; break;
            case "nurse":  prefix = "NUR"; break;
            case "manager": prefix = "MGR"; break;
            default: prefix = "STA"; // fallback
        }

        // Count how many existing staff IDs start with this prefix
        String sql = "SELECT COUNT(*) FROM staff WHERE staff_id LIKE ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, prefix + "%");
            ResultSet rs = stmt.executeQuery();

            int count = 0;
            if (rs.next()) count = rs.getInt(1);

            // If default 01 exists, next is count + 1
            int nextNum = count + 1;
            return String.format("%s%02d", prefix, nextNum);
        }
    }

    public static String getNextPatientId() throws Exception {
        String prefix = "P";
        String sql = "SELECT COUNT(*) FROM patients";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            int count = 0;
            if (rs.next()) count = rs.getInt(1);
            int nextNum = count + 1;
            return String.format("%s%02d", prefix, nextNum);
        }
    }
}
