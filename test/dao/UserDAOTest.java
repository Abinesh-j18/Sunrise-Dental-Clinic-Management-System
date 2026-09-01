package dao;

import model.Administrator;
import model.DentistUser;
import model.Receptionist;
import model.User;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Genuine JUnit test suite for UserDAO operations and polymorphism verification.
 *
 * @author Student
 */
public class UserDAOTest {
    private UserDAO userDAO;

    @Before
    public void setUp() {
        userDAO = new UserDAOImpl();
    }

    @Test
    public void testFindAdminByUsername() {
        User user = userDAO.findByUsername("admin");
        assertNotNull("Admin user should exist in seed data", user);
        assertEquals("Administrator", user.getRole());
        assertTrue("Admin user should instantiate Administrator class", user instanceof Administrator);
        assertNotNull("Admin dashboard items must not be null", user.getMenuItems());
        assertFalse("Admin menu items should not be empty", user.getMenuItems().isEmpty());
    }

    @Test
    public void testFindReceptionistByUsername() {
        User user = userDAO.findByUsername("receptionist1");
        assertNotNull("Receptionist user should exist in seed data", user);
        assertEquals("Receptionist", user.getRole());
        assertTrue("User should instantiate Receptionist class polymorphically", user instanceof Receptionist);
        assertFalse("Receptionist menu items should not be empty", user.getMenuItems().isEmpty());
    }

    @Test
    public void testFindDentistByUsername() {
        User user = userDAO.findByUsername("dr.silva");
        assertNotNull("Dentist user should exist in seed data", user);
        assertEquals("Dentist", user.getRole());
        assertTrue("User should instantiate DentistUser class polymorphically", user instanceof DentistUser);
        DentistUser dentist = (DentistUser) user;
        assertTrue("Dentist ID should be greater than 0", dentist.getDentistId() > 0);
        assertNotNull("Dentist specialization should be set", dentist.getSpecialization());
    }

    @Test
    public void testFindAllUsers() {
        List<User> users = userDAO.findAll();
        assertNotNull("Users list should not be null", users);
        assertTrue("There should be at least 3 seed users", users.size() >= 3);
    }

    @Test
    public void testNonExistentUserReturnsNull() {
        User user = userDAO.findByUsername("nonexistent_user_9999");
        assertNull("Non-existent username should return null", user);
    }
}
