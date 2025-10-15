package CareHome.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConfig {
    private static final String URL = "jdbc:sqlite:carehome.db";
    private static Connection connection;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL);
            initializeTables();
        }
        return connection;
    }

    private static void initializeTables() throws SQLException {
        Statement stmt = connection.createStatement();

        // Create patients table
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS patients (
                id TEXT PRIMARY KEY,
                first_name TEXT NOT NULL,
                last_name TEXT NOT NULL,
                gender TEXT NOT NULL,
                age INTEGER NOT NULL,
                patient_id TEXT UNIQUE NOT NULL,
                admission_date TEXT NOT NULL,
                needs_isolation BOOLEAN DEFAULT FALSE,
                bed_id TEXT,
                is_active BOOLEAN DEFAULT TRUE,
                created_at TEXT DEFAULT CURRENT_TIMESTAMP
            )
        """);

        // Create staff table
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS staff (
                id TEXT PRIMARY KEY,
                first_name TEXT NOT NULL,
                last_name TEXT NOT NULL,
                gender TEXT NOT NULL,
                age INTEGER NOT NULL,
                staff_id TEXT UNIQUE NOT NULL,
                username TEXT UNIQUE NOT NULL,
                password TEXT NOT NULL,
                staff_type TEXT NOT NULL,
                license_number TEXT,
                is_active BOOLEAN DEFAULT TRUE,
                created_at TEXT DEFAULT CURRENT_TIMESTAMP
            )
        """);

        // Create beds table - NEW TABLE for bed management
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS beds (
                bed_id TEXT PRIMARY KEY,
                ward_id TEXT NOT NULL,
                room_number INTEGER NOT NULL,
                room_capacity INTEGER NOT NULL,
                patient_id TEXT,
                is_occupied BOOLEAN DEFAULT FALSE,
                created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (patient_id) REFERENCES patients(id)
            )
        """);

        // Create patient_bed history table
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS patient_bed (
                assignment_id INTEGER PRIMARY KEY AUTOINCREMENT,
                patient_id TEXT NOT NULL,
                bed_id TEXT NOT NULL,
                start_time TEXT NOT NULL,
                end_time TEXT,
                FOREIGN KEY (patient_id) REFERENCES patients(id),
                FOREIGN KEY (bed_id) REFERENCES beds(bed_id)
            )
        """);

        // Create shifts table - NEW TABLE for shift management
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS shifts (
                shift_id TEXT PRIMARY KEY,
                staff_id TEXT NOT NULL,
                day_of_week TEXT NOT NULL,
                shift_type TEXT NOT NULL,
                start_hour INTEGER NOT NULL,
                end_hour INTEGER NOT NULL,
                duration_hours INTEGER NOT NULL,
                is_assigned BOOLEAN DEFAULT TRUE,
                created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (staff_id) REFERENCES staff(staff_id)
            )
        """);

        // Create prescriptions table - ENHANCED
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS prescriptions (
                prescription_id TEXT PRIMARY KEY,
                patient_id TEXT NOT NULL,
                doctor_id TEXT NOT NULL,
                medication_name TEXT NOT NULL,
                dosage TEXT NOT NULL,
                frequency TEXT NOT NULL,
                start_date TEXT NOT NULL,
                end_date TEXT,
                is_active BOOLEAN DEFAULT TRUE,
                created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (patient_id) REFERENCES patients(patient_id),
                FOREIGN KEY (doctor_id) REFERENCES staff(id)
            )
        """);

        // Create medication_administration table
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS medication_administration (
                admin_id TEXT PRIMARY KEY,
                prescription_id TEXT NOT NULL,
                nurse_id TEXT NOT NULL,
                administered_at TEXT NOT NULL,
                notes TEXT,
                created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (prescription_id) REFERENCES prescriptions(prescription_id),
                FOREIGN KEY (nurse_id) REFERENCES staff(id)
            )
        """);

        // Create audit_log table
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS audit_log (
                entry_id TEXT PRIMARY KEY,
                staff_id TEXT NOT NULL,
                action_type TEXT NOT NULL,
                target_id TEXT,
                details TEXT,
                timestamp TEXT DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (staff_id) REFERENCES staff(staff_id)
            )
        """);

        // Insert default staff - ONLY if not exists
        insertDefaultStaff(stmt);

        // Initialize bed structure - ONLY if beds table is empty
        initializeBedStructure(stmt);

        stmt.close();
    }

    private static void insertDefaultStaff(Statement stmt) throws SQLException {
        stmt.executeUpdate("""
            INSERT OR IGNORE INTO staff (
                id, first_name, last_name, gender, age,
                staff_id, username, password, staff_type,
                is_active, created_at
            ) VALUES (
                'M001', 'John', 'Manager', 'MALE', 45,
                'MGR001', 'manager', 'pass123', 'MANAGER',
                TRUE, CURRENT_TIMESTAMP
            )
        """);

        stmt.executeUpdate("""
            INSERT OR IGNORE INTO staff (
                id, first_name, last_name, gender, age,
                staff_id, username, password, staff_type,
                is_active, created_at
            ) VALUES (
                'D001', 'Jane', 'Smith', 'FEMALE', 40,
                'DOC001', 'doctor', 'pass123', 'DOCTOR',
                TRUE, CURRENT_TIMESTAMP
            )
        """);

        stmt.executeUpdate("""
            INSERT OR IGNORE INTO staff (
                id, first_name, last_name, gender, age,
                staff_id, username, password, staff_type,
                license_number, is_active, created_at
            ) VALUES (
                'D001', 'Jane', 'Smith', 'FEMALE', 40,
                'DOC001', 'doctor', 'pass123', 'DOCTOR',
                'MED12345', TRUE, CURRENT_TIMESTAMP
            )
        """);

        stmt.executeUpdate("""
            INSERT OR IGNORE INTO staff (
                id, first_name, last_name, gender, age,
                staff_id, username, password, staff_type,
                license_number, is_active, created_at
            ) VALUES (
                'N001', 'Bob', 'Johnson', 'MALE', 35,
                'NUR001', 'nurse', 'pass123', 'NURSE',
                'NUR67890', TRUE, CURRENT_TIMESTAMP
            )
        """);
    }

    private static void initializeBedStructure(Statement stmt) throws SQLException {
        // Check if beds already exist
        var rs = stmt.executeQuery("SELECT COUNT(*) FROM beds");
        rs.next();
        if (rs.getInt(1) > 0) {
            rs.close();
            return; // Beds already initialized
        }
        rs.close();

        // Ward 1 - High Care Ward
        String[][] ward1Config = {
                {"W1R1", "1"}, {"W1R2", "2"}, {"W1R3", "4"},
                {"W1R4", "4"}, {"W1R5", "4"}, {"W1R6", "4"}
        };

        // Ward 2 - Standard Care Ward
        String[][] ward2Config = {
                {"W2R1", "1"}, {"W2R2", "2"}, {"W2R3", "4"},
                {"W2R4", "4"}, {"W2R5", "4"}, {"W2R6", "4"}
        };

        // Initialize Ward 1
        for (String[] roomConfig : ward1Config) {
            String roomId = roomConfig[0];
            int capacity = Integer.parseInt(roomConfig[1]);

            for (int bedNum = 1; bedNum <= capacity; bedNum++) {
                String bedId = roomId + "B" + bedNum;
                stmt.executeUpdate(String.format(
                        "INSERT INTO beds (bed_id, ward_id, room_number, room_capacity, is_occupied) VALUES ('%s', 'W1', %d, %d, FALSE)",
                        bedId, Integer.parseInt(roomId.substring(3)), capacity
                ));
            }
        }

        // Initialize Ward 2
        for (String[] roomConfig : ward2Config) {
            String roomId = roomConfig[0];
            int capacity = Integer.parseInt(roomConfig[1]);

            for (int bedNum = 1; bedNum <= capacity; bedNum++) {
                String bedId = roomId + "B" + bedNum;
                stmt.executeUpdate(String.format(
                        "INSERT INTO beds (bed_id, ward_id, room_number, room_capacity, is_occupied) VALUES ('%s', 'W2', %d, %d, FALSE)",
                        bedId, Integer.parseInt(roomId.substring(3)), capacity
                ));
            }
        }
    }

    public static void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}