package service;

import model.Administrator;
import model.User;
import server.SessionManager;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for SessionManager token lifecycle and security.
 *
 * @author Student
 */
public class SessionManagerTest {
    private SessionManager sessionManager;
    private User testUser;

    @Before
    public void setUp() {
        sessionManager = new SessionManager();
        testUser = new Administrator(1, "admin", "hash", "Dr. Aruna Bandara", "admin@sunrisedental.lk");
    }

    @Test
    public void testCreateAndValidateSession() {
        String token = sessionManager.createSession(testUser);
        assertNotNull("Session token should not be null", token);
        assertFalse("Session token should not be empty", token.trim().isEmpty());

        User validatedUser = sessionManager.validateSession(token);
        assertNotNull("Validated user should not be null", validatedUser);
        assertEquals(testUser.getUsername(), validatedUser.getUsername());
        assertEquals("Administrator", validatedUser.getRole());
    }

    @Test
    public void testInvalidateSession() {
        String token = sessionManager.createSession(testUser);
        assertNotNull(sessionManager.validateSession(token));

        boolean invalidated = sessionManager.invalidateSession(token);
        assertTrue("Session invalidation should succeed", invalidated);

        User validatedAfterLogout = sessionManager.validateSession(token);
        assertNull("Invalidated token must return null user", validatedAfterLogout);
    }

    @Test
    public void testInvalidTokenReturnsNull() {
        assertNull("Fake token must not validate", sessionManager.validateSession("invalid_token_12345"));
        assertNull("Null token must not validate", sessionManager.validateSession(null));
        assertNull("Empty token must not validate", sessionManager.validateSession("   "));
    }
}
