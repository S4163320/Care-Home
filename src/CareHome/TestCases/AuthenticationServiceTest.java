package CareHome.TestCases;

import CareHome.Service.AuthenticationServiceImpl;
import CareHome.Model.Person.*;
import CareHome.Model.Audit.AuditLogger;
import org.testng.annotations.*;
import org.testng.Assert;


 // Core test suite for AuthenticationService demonstrating main business rules

public class AuthenticationServiceTest {
    private AuthenticationServiceImpl authService;
    private AuditLogger auditLogger;

    @BeforeMethod
    public void setUp() {
        auditLogger = new AuditLogger();
        authService = new AuthenticationServiceImpl(auditLogger);
    }

    @AfterMethod
    public void tearDown() {
        // Logout after each test to clean state
        Staff currentUser = authService.getCurrentUser();
        if (currentUser != null) {
            authService.logout(currentUser);
        }
    }

    @Test
    public void testSuccessfulManagerLogin() throws Exception {
        // BUSINESS RULE: Valid credentials should allow successful login
        Staff loggedInUser = authService.login("manager", "pass123");
        
        Assert.assertNotNull(loggedInUser, "Login should return a staff member");
        Assert.assertEquals(loggedInUser.getUsername(), "manager", "Username should match");
        Assert.assertTrue(loggedInUser instanceof Manager, "User should be a Manager");
        
        System.out.println("✓ PASSED: Manager logged in successfully");
    }

    @Test
    public void testSuccessfulDoctorLogin() throws Exception {
        // BUSINESS RULE: Doctors should be able to log in with valid credentials
        Staff loggedInUser = authService.login("doctor", "pass123");
        
        Assert.assertNotNull(loggedInUser, "Login should return a staff member");
        Assert.assertTrue(loggedInUser instanceof Doctor, "User should be a Doctor");
        
        System.out.println("✓ PASSED: Doctor logged in successfully");
    }

    @Test
    public void testSuccessfulNurseLogin() throws Exception {
        // BUSINESS RULE: Nurses should be able to log in with valid credentials
        Staff loggedInUser = authService.login("nurse", "pass123");
        
        Assert.assertNotNull(loggedInUser, "Login should return a staff member");
        Assert.assertTrue(loggedInUser instanceof Nurse, "User should be a Nurse");
        
        System.out.println("✓ PASSED: Nurse logged in successfully");
    }

    @Test
    public void testLogoutClearsCurrentUser() throws Exception {
        // BUSINESS RULE: Logout should clear the current user session
        Staff user = authService.login("manager", "pass123");
        Assert.assertNotNull(authService.getCurrentUser(), "User should be logged in");
        
        authService.logout(user);
        
        Assert.assertNull(authService.getCurrentUser(), "Current user should be null after logout");
        System.out.println("✓ PASSED: Logout cleared current user session");
    }

    @Test
    public void testAuditLoggerAvailable() throws Exception {
        // BUSINESS RULE: All authentication events should be logged for audit trail
        Assert.assertNotNull(authService.getAuditLogger(), "Audit logger should be available");
        
        authService.login("manager", "pass123");
        
        Assert.assertNotNull(authService.getAuditLogger(), "Audit logger should remain available");
        
        System.out.println("✓ PASSED: Authentication service maintains audit logger");
    }
}
