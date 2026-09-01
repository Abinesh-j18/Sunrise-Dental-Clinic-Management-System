package service;

import model.Appointment;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.Assert.*;

/**
 * Unit tests for double booking prevention and exception handling.
 *
 * @author Student
 */
public class DoubleBookingTest {
    private ClinicService clinicService;

    @Before
    public void setUp() {
        clinicService = new ClinicService();
    }

    @Test(expected = DoubleBookingException.class)
    public void testDoubleBookingThrowsException() throws Exception {
        // Book initial appointment with unique date per test execution
        LocalDate futureDate = LocalDate.now().plusDays((int) (System.currentTimeMillis() % 2000) + 120);
        LocalTime slotTime = LocalTime.of(11, 30);

        // Obtain valid patient IDs dynamically
        java.util.List<model.Patient> patients = clinicService.getPatients("");
        int p1 = (patients != null && !patients.isEmpty()) ? patients.get(0).getId() :
                clinicService.registerPatient(new model.Patient(0, "Test Patient One", "Colombo", "0771234567", "p1@test.lk")).getId();
        int p2 = (patients != null && patients.size() > 1) ? patients.get(1).getId() :
                clinicService.registerPatient(new model.Patient(0, "Test Patient Two", "Colombo", "0771234568", "p2@test.lk")).getId();

        // First booking should succeed
        Appointment first = clinicService.bookAppointment(p1, 1, 1, futureDate, slotTime, "First booking");
        assertNotNull("First booking must succeed", first);

        // Second booking for same dentist at same date/time MUST throw DoubleBookingException
        clinicService.bookAppointment(p2, 1, 2, futureDate, slotTime, "Attempted duplicate booking");
    }
}
