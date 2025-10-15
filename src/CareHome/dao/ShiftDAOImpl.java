package CareHome.dao;

import CareHome.Model.Schedule.Shift;
import CareHome.Model.ShiftType;
import CareHome.config.DatabaseConfig;
import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ShiftDAOImpl implements ShiftDAO {

    // Inserts a new shift row with computed timing fields
    @Override
    public void saveShift(Shift shift) throws Exception {
        String sql = """
            INSERT INTO shifts (shift_id, staff_id, day_of_week, shift_type, start_hour, 
                              end_hour, duration_hours, is_assigned, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, shift.getShiftId());
            stmt.setString(2, shift.getAssignedStaffId());
            stmt.setString(3, shift.getDayOfWeek().toString());
            stmt.setString(4, shift.getShiftType().toString());
            stmt.setInt(5, shift.getShiftType().getStartHour());
            stmt.setInt(6, shift.getShiftType().getEndHour());
            stmt.setInt(7, shift.getShiftType().getDurationHours());
            stmt.setBoolean(8, shift.isAssigned());
            stmt.setString(9, LocalDateTime.now().toString());

            stmt.executeUpdate();
        }
    }

    // Returns all assigned shifts for a given staff member
    @Override
    public List<Shift> findShiftsByStaffId(String staffId) throws Exception {
        String sql = "SELECT * FROM shifts WHERE staff_id = ? AND is_assigned = TRUE ORDER BY day_of_week";
        List<Shift> shifts = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, staffId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                shifts.add(createShiftFromResultSet(rs));
            }
        }
        return shifts;
    }

    // Returns all assigned shifts for a specific day
    @Override
    public List<Shift> findShiftsByDay(DayOfWeek day) throws Exception {
        String sql = "SELECT * FROM shifts WHERE day_of_week = ? AND is_assigned = TRUE ORDER BY start_hour";
        List<Shift> shifts = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, day.toString());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                shifts.add(createShiftFromResultSet(rs));
            }
        }
        return shifts;
    }

    @Override
    public void updateShift(Shift shift) throws Exception {
        String sql = """
            UPDATE shifts SET staff_id = ?, day_of_week = ?, shift_type = ?, 
                            start_hour = ?, end_hour = ?, duration_hours = ?, is_assigned = ?
            WHERE shift_id = ?
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, shift.getAssignedStaffId());
            stmt.setString(2, shift.getDayOfWeek().toString());
            stmt.setString(3, shift.getShiftType().toString());
            stmt.setInt(4, shift.getShiftType().getStartHour());
            stmt.setInt(5, shift.getShiftType().getEndHour());
            stmt.setInt(6, shift.getShiftType().getDurationHours());
            stmt.setBoolean(7, shift.isAssigned());
            stmt.setString(8, shift.getShiftId());

            stmt.executeUpdate();
        }
    }

    @Override
    public void deleteShift(String shiftId) throws Exception {
        String sql = "UPDATE shifts SET is_assigned = FALSE WHERE shift_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, shiftId);
            stmt.executeUpdate();
        }
    }

    @Override
    public Shift findShiftById(String shiftId) throws Exception {
        String sql = "SELECT * FROM shifts WHERE shift_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, shiftId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return createShiftFromResultSet(rs);
            }
            return null;
        }
    }

    // Returns all currently assigned shifts ordered by day and start time
    @Override
    public List<Shift> getAllShifts() throws Exception {
        String sql = "SELECT * FROM shifts WHERE is_assigned = TRUE ORDER BY day_of_week, start_hour";
        List<Shift> shifts = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                shifts.add(createShiftFromResultSet(rs));
            }
        }
        return shifts;
    }

    // Maps a ResultSet row to a Shift domain object
    private Shift createShiftFromResultSet(ResultSet rs) throws SQLException {
        String shiftId = rs.getString("shift_id");
        DayOfWeek dayOfWeek = DayOfWeek.valueOf(rs.getString("day_of_week"));
        ShiftType shiftType = ShiftType.valueOf(rs.getString("shift_type"));
        String staffId = rs.getString("staff_id");

        Shift shift = new Shift(shiftId, dayOfWeek, shiftType);
        if (staffId != null && rs.getBoolean("is_assigned")) {
            shift.assignStaff(staffId);
        }

        return shift;
    }
}
