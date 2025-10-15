package CareHome.dao;

import CareHome.Model.Person.Staff;
import java.util.List;

public interface StaffDAO {
    void save(Staff staff) throws Exception;
    Staff findById(String id) throws Exception;
    Staff findByUsername(String username) throws Exception;
    void updatePassword(String username, String newPassword) throws Exception;
    Staff findByStaffId(String staffId) throws Exception;
    List<Staff> findAll() throws Exception;
    void updatePasswordByUsername(String username, String newPassword) throws Exception;

}