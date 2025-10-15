package CareHome.TestCases;

import CareHome.Service.AuthenticationServiceImpl;
import CareHome.Model.Person.*;
import CareHome.Model.Gender;
import CareHome.Model.Audit.AuditLogger;
import CareHome.Exception.AuthorizationException;
import org.testng.annotations.*;
import org.testng.Assert;

public class AuthenticationServiceTest {
    private AuthenticationServiceImpl authService;
    private AuditLogger auditLogger;
    private Manager testManager;
    private Doctor testDoctor;
    private Nurse testNurse;

    @BeforeMethod
    public void setUp() {
        auditLogger = new AuditLogger();
        authService = new AuthenticationServiceImpl(auditLogger);

        // Create test staff
        testManager = new Manager("M1", "John", "Manager", Gender.MALE, 45, "MGR001", "testmgr", "pass123");
        testDoctor = new Doctor("D1", "Jane", "Smith", Gender.FEMALE, 40, "DOC001", "testdoc", "pass123", "MED12345");
        testNurse = new Nurse("N1", "Bob", "Johnson", Gender.MALE, 35, "NUR001", "testnur", "pass123", "NUR67890");
    }

    @Test
    public void testSuccessfulLogin() throws Exception {
        // This test verifies basic login functionality
        Staff loggedInUser = authService.login("manager", "pass123");
        Assert.assertNotNull(loggedInUser, "Login should return a staff member");
        Assert.assertEquals(loggedInUser.getUsername(), "manager", "Username should match");
    }

    @Test(expectedExceptions = AuthorizationException.class)
    public void testInvalidLoginCredentials() throws Exception {
        // Tests that invalid credentials throw proper exception
        authService.login("invalid", "wrong");
    }

    @Test
    public void testCurrentUserTracking() throws Exception {
        Staff user = authService.login("manager", "pass123");
        Assert.assertEquals(authService.getCurrentUser(), user, "Current user should be tracked");

        authService.logout(user);
        Assert.assertNull(authService.getCurrentUser(), "Current user should be null after logout");
    }

}