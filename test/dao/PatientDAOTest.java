package dao;

import model.Patient;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Genuine JUnit test suite for PatientDAO operations.
 *
 * @author Student
 */
public class PatientDAOTest {
    private PatientDAO patientDAO;

    @Before
    public void setUp() {
        patientDAO = new PatientDAOImpl();
    }

    @Test
    public void testFindById() {
        Patient patient = patientDAO.findById(1);
        assertNotNull("Patient with ID 1 should exist in database", patient);
        assertNotNull("Patient name must not be null", patient.getName());
        assertNotNull("Patient contact number must not be null", patient.getContactNumber());
    }

    @Test
    public void testCreatePatient() {
        long timestamp = System.currentTimeMillis();
        Patient newPatient = new Patient(
                0,
                "Test Patient " + timestamp,
                "123 Test Street, Colombo",
                "077000" + (timestamp % 10000),
                "test." + timestamp + "@example.com"
        );

        Patient created = patientDAO.create(newPatient);
        assertNotNull("Created patient should not be null", created);
        assertTrue("Created patient must have generated ID", created.getId() > 0);

        Patient fetched = patientDAO.findById(created.getId());
        assertNotNull("Fetched patient must exist", fetched);
        assertEquals(newPatient.getName(), fetched.getName());
        assertEquals(newPatient.getContactNumber(), fetched.getContactNumber());
    }

    @Test
    public void testSearchByNameOrContact() {
        List<Patient> results = patientDAO.searchByNameOrContact("Kamal");
        assertNotNull("Search results should not be null", results);
        assertFalse("Should find patient Kamal", results.isEmpty());
        assertTrue(results.stream().anyMatch(p -> p.getName().contains("Kamal")));
    }
}
