package CareHome.dao;

import CareHome.Model.Schedule.Shift;
import java.time.DayOfWeek;
import java.util.List;

public interface ShiftDAO {
    void saveShift(Shift shift) throws Exception;
    List<Shift> findShiftsByStaffId(String staffId) throws Exception;
    List<Shift> findShiftsByDay(DayOfWeek day) throws Exception;
    void updateShift(Shift shift) throws Exception;
    void deleteShift(String shiftId) throws Exception;
    Shift findShiftById(String shiftId) throws Exception;
    List<Shift> getAllShifts() throws Exception;
}
