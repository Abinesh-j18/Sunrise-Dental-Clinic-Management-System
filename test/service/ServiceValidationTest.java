package service;

import model.Patient;
import model.User;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for business logic validations and authentication.
 *
 * @author Student
 */
public class ServiceValidationTest {
    private ClinicService clinicService;

    @Before
    public void setUp() {
        clinicService = new ClinicService();
    }

    @Test
    public void testSuccessfulAuthentication() throws Exception {
        User user = clinicService.authenticate("admin", "admin123");
        assertNotNull("Authenticated user should not be null", user);
        assertEquals("Administrator", user.getRole());
        assertEquals("admin", user.getUsername());
    }

    @Test(expected = AuthenticationException.class)
    public void testAuthenticationFailureWrongPassword() throws Exception {
        clinicService.authenticate("admin", "wrong_password_123");
    }

    @Test(expected = AuthenticationException.class)
    public void testAuthenticationFailureUnknownUsername() throws Exception {
        clinicService.authenticate("unknown_user_999", "admin123");
    }

    @Test(expected = ValidationException.class)
    public void testAuthenticationEmptyCredentials() throws Exception {
        clinicService.authenticate("", "");
    }

    @Test(expected = ValidationException.class)
    public void testRegisterPatientInvalidPhone() throws Exception {
        Patient invalidPhonePatient = new Patient(0, "John Doe", "123 Main St", "1234", "john@example.com");
        clinicService.registerPatient(invalidPhonePatient);
    }

    @Test(expected = ValidationException.class)
    public void testRegisterPatientMissingName() throws Exception {
        Patient missingNamePatient = new Patient(0, "", "123 Main St", "0771234567", "john@example.com");
        clinicService.registerPatient(missingNamePatient);
    }
}
