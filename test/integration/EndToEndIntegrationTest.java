package integration;

import client.ApiClient;
import client.ApiException;
import client.SessionContext;
import model.*;
import server.SunriseServer;
import server.dto.BookAppointmentRequest;
import server.dto.LoginResponse;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Full 3-Tier End-to-End Integration Test Suite.
 * Validates genuine distributed communication between Tier 1 ApiClient,
 * Tier 2 REST HttpServer, and Tier 3 MySQL Database (including Trigger, Stored Procedure, and Function).
 *
 * @author Student
 */
public class EndToEndIntegrationTest {
    private static final int TEST_PORT = 8089;
    private static SunriseServer server;
    private static ApiClient apiClient;

    @BeforeClass
    public static void setUpServer() throws Exception {
        server = new SunriseServer(TEST_PORT);
        server.start();
        apiClient = new ApiClient("http://localhost:" + TEST_PORT);
    }

    @AfterClass
    public static void tearDownServer() {
        if (server != null) {
            server.stop();
        }
        // Clean up temporary test patient records created during test runs
        try (java.sql.Connection conn = dao.DatabaseConnectionManager.getInstance().getConnection();
             java.sql.Statement st = conn.createStatement()) {
            st.executeUpdate("DELETE FROM invoice_items WHERE invoice_id IN (SELECT id FROM invoices WHERE appointment_id IN (SELECT id FROM appointments WHERE patient_id IN (SELECT id FROM patients WHERE name LIKE 'E2E Test%' OR name LIKE 'Doctor Workflow%')))");
            st.executeUpdate("DELETE FROM invoices WHERE appointment_id IN (SELECT id FROM appointments WHERE patient_id IN (SELECT id FROM patients WHERE name LIKE 'E2E Test%' OR name LIKE 'Doctor Workflow%'))");
            st.executeUpdate("DELETE FROM appointments WHERE patient_id IN (SELECT id FROM patients WHERE name LIKE 'E2E Test%' OR name LIKE 'Doctor Workflow%')");
            st.executeUpdate("DELETE FROM patients WHERE name LIKE 'E2E Test%' OR name LIKE 'Doctor Workflow%'");
        } catch (Exception ignored) {
        }
    }

    @Test
    public void testUnauthorizedAccessWithoutToken() {
        // Clear session token on client
        SessionContext.getInstance().clearSession();

        try {
            apiClient.getDentists();
            fail("Expected ApiException (401 Unauthorized) when accessing protected endpoint without token");
        } catch (ApiException e) {
            assertEquals("HTTP Status should be 401", 401, e.getStatusCode());
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    public void testAuthenticationAndPolymorphicMenuRetrieval() throws Exception {
        // Test Receptionist Login
        LoginResponse recResp = apiClient.login("receptionist1", "admin123");
        assertNotNull("Login response should not be null", recResp);
        assertNotNull("Session token should be issued", recResp.getToken());
        assertEquals("Receptionist", recResp.getRole());
        assertNotNull("Polymorphic menu items must be present", recResp.getMenuItems());
        assertTrue(recResp.getMenuItems().stream().anyMatch(m -> "REGISTER_APPT".equals(m.getId())));
        assertTrue(recResp.getMenuItems().stream().anyMatch(m -> "BILLING".equals(m.getId())));

        // Test Administrator Login
        LoginResponse adminResp = apiClient.login("admin", "admin123");
        assertEquals("Administrator", adminResp.getRole());
        assertTrue(adminResp.getMenuItems().stream().anyMatch(m -> "REPORTS".equals(m.getId())));

        // Test Dentist Login
        LoginResponse dentResp = apiClient.login("dr.silva", "admin123");
        assertEquals("Dentist", dentResp.getRole());
        assertNotNull("Dentist ID must be populated", dentResp.getDentistId());
    }

    @Test
    public void testFullEndToEndPatientBookingBillingWorkflow() throws Exception {
        // 1. Log in as Receptionist
        LoginResponse loginResp = apiClient.login("receptionist1", "admin123");
        SessionContext.getInstance().setSession(loginResp.getToken(),
                new Receptionist(loginResp.getUserId(), loginResp.getUsername(), "", loginResp.getFullName(), loginResp.getEmail()),
                null);

        // 2. Fetch active dentists and treatments
        List<DentistProfile> dentists = apiClient.getDentists();
        assertNotNull("Dentists list must not be null", dentists);
        assertFalse("Dentists list must not be empty", dentists.isEmpty());
        DentistProfile targetDentist = dentists.get(0);

        List<Treatment> treatments = apiClient.getTreatments();
        assertNotNull("Treatments list must not be null", treatments);
        assertFalse("Treatments list must not be empty", treatments.isEmpty());
        Treatment targetTreatment = treatments.get(0);

        // 3. Register a new patient
        long uniqueTs = System.currentTimeMillis();
        Patient newPatient = new Patient(
                0,
                "E2E Test Patient " + uniqueTs,
                "45 Galle Road, Colombo 03",
                "0771" + String.format("%06d", (uniqueTs % 1000000)),
                "e2e." + uniqueTs + "@sunrisedental.lk"
        );
        Patient registeredPatient = apiClient.registerPatient(newPatient);
        assertNotNull("Registered patient must not be null", registeredPatient);
        assertTrue("Patient ID must be generated", registeredPatient.getId() > 0);

        // 4. Book an appointment (Unique date & time slot)
        LocalDate apptDate = LocalDate.now().plusDays((int) (uniqueTs % 300) + 150);
        LocalTime apptTime = LocalTime.of(10, 30);

        BookAppointmentRequest bookReq = new BookAppointmentRequest(
                registeredPatient.getId(),
                targetDentist.getId(),
                targetTreatment.getId(),
                apptDate,
                apptTime,
                "E2E Integration Test Case"
        );

        Appointment bookedAppt = apiClient.bookAppointment(bookReq);
        assertNotNull("Booked appointment must not be null", bookedAppt);
        assertTrue("Appointment ID must be generated", bookedAppt.getId() > 0);
        assertNotNull("Appointment number must be generated by MySQL trigger", bookedAppt.getAppointmentNumber());
        assertTrue("Appointment number must match APT-YYYY-XXXX format: " + bookedAppt.getAppointmentNumber(),
                bookedAppt.getAppointmentNumber().matches("^APT-\\d{4}-\\d{4}$"));

        // 5. Test Double Booking Prevention (Re-booking the exact same dentist slot MUST fail with 409 Conflict)
        try {
            apiClient.bookAppointment(bookReq);
            fail("Expected DoubleBookingException / HTTP 409 Conflict on duplicate slot booking");
        } catch (ApiException ae) {
            assertEquals("Expected HTTP 409 Conflict for double-booking", 409, ae.getStatusCode());
            assertTrue("Message should mention double booking or conflict", ae.getMessage().toLowerCase().contains("double booking") || ae.getMessage().toLowerCase().contains("already scheduled"));
        }

        // 6. Search appointment by appointment number
        Appointment fetched = apiClient.getAppointmentByNumber(bookedAppt.getAppointmentNumber());
        assertNotNull("Fetched appointment must not be null", fetched);
        assertEquals(bookedAppt.getId(), fetched.getId());
        assertEquals(registeredPatient.getName(), fetched.getPatient().getName());
        assertEquals(targetDentist.getFullName(), fetched.getDentist().getFullName());
        assertEquals(targetTreatment.getType(), fetched.getTreatment().getType());

        // 7. Calculate and generate invoice via MySQL Stored Procedure CalculateInvoiceTotal
        Invoice generatedInvoice = apiClient.calculateAndGenerateInvoice(bookedAppt.getId(), "Cash");
        assertNotNull("Generated invoice must not be null", generatedInvoice);
        assertTrue("Invoice ID must be generated", generatedInvoice.getId() > 0);
        assertEquals("Standard consultation fee should be 1500.00", 1500.00, generatedInvoice.getConsultationFee(), 0.01);
        assertEquals("Treatment cost should match treatment base price", targetTreatment.getCost(), generatedInvoice.getTreatmentCost(), 0.01);
        assertEquals("Total must equal consultation + treatment fee",
                1500.00 + targetTreatment.getCost(), generatedInvoice.getTotalAmount(), 0.01);
        assertNotNull("Composed invoice items must be present", generatedInvoice.getItems());
        assertEquals("Invoice must own exactly 2 line items", 2, generatedInvoice.getItems().size());

        // 8. Generate Reports via Factory Method API
        List<DailyAppointmentReportItem> dailyReport = apiClient.getDailyAppointmentsReport(targetDentist.getId(), apptDate);
        assertNotNull("Daily report must not be null", dailyReport);
        assertTrue("Daily report should include the booked appointment",
                dailyReport.stream().anyMatch(r -> r.getAppointmentNumber().equals(bookedAppt.getAppointmentNumber())));

        List<RevenueByTreatmentReportItem> revenueReport = apiClient.getRevenueByTreatmentReport(null, apptDate.getYear());
        assertNotNull("Revenue report must not be null", revenueReport);

        List<TopTreatmentReportItem> topReport = apiClient.getTopTreatmentsReport(5);
        assertNotNull("Top treatments report must not be null", topReport);
        assertFalse("Top treatments report should not be empty", topReport.isEmpty());

        // 9. Logout
        apiClient.logout();
    }

    @Test
    public void testDoctorDiagnosisAndTreatmentUpdateWorkflow() throws Exception {
        // 1. Receptionist logs in and registers initial appointment (e.g. General Consultation)
        LoginResponse recResp = apiClient.login("receptionist1", "admin123");
        SessionContext.getInstance().setSession(recResp.getToken(),
                new Receptionist(recResp.getUserId(), recResp.getUsername(), "", recResp.getFullName(), recResp.getEmail()), null);

        List<DentistProfile> dentists = apiClient.getDentists();
        List<Treatment> treatments = apiClient.getTreatments();
        assertTrue("Must have at least 2 treatments to test change", treatments.size() >= 2);
        Treatment initialTreatment = treatments.get(0); // e.g. General Consultation (2000)
        Treatment diagnosedTreatment = treatments.get(1); // e.g. Teeth Cleaning or Root Canal

        long uniqueTs = System.currentTimeMillis() + 999;
        Patient patient = apiClient.registerPatient(new Patient(0, "Doctor Workflow Patient " + uniqueTs, "Colombo", "0711" + String.format("%06d", (uniqueTs % 1000000)), "doc." + uniqueTs + "@sunrisedental.lk"));

        LocalDate date = LocalDate.now().plusDays((int) (uniqueTs % 200) + 400);
        Appointment booked = apiClient.bookAppointment(new BookAppointmentRequest(
                patient.getId(), dentists.get(0).getId(), initialTreatment.getId(), date, LocalTime.of(14, 0), "Initial booking request"
        ));
        assertEquals(initialTreatment.getId(), booked.getTreatment().getId());

        // 2. Dentist logs in to clinical portal
        LoginResponse docResp = apiClient.login("dr.silva", "admin123");
        SessionContext.getInstance().setSession(docResp.getToken(),
                new DentistUser(docResp.getUserId(), docResp.getUsername(), "", docResp.getFullName(), docResp.getEmail(), docResp.getDentistId(), "Dentist"), docResp.getDentistId());

        // 3. Dentist updates diagnosis, treatment procedure, and notes
        String clinicalFindings = "Doctor Diagnosis: Clinical exam showed active cavity. Performed " + diagnosedTreatment.getType();
        Appointment updatedByDoctor = apiClient.updateAppointmentTreatment(
                booked.getId(),
                diagnosedTreatment.getId(),
                clinicalFindings,
                "COMPLETED"
        );

        assertNotNull(updatedByDoctor);
        assertEquals("Treatment must be updated to doctor's diagnosed procedure", diagnosedTreatment.getType(), updatedByDoctor.getTreatment().getType());
        assertEquals(diagnosedTreatment.getId(), updatedByDoctor.getTreatment().getId());
        assertEquals("COMPLETED", updatedByDoctor.getStatus());
        assertEquals(clinicalFindings, updatedByDoctor.getNotes());

        // 4. Receptionist logs in to calculate final bill -> Stored procedure calculates updated treatment tariff
        LoginResponse recResp2 = apiClient.login("receptionist1", "admin123");
        SessionContext.getInstance().setSession(recResp2.getToken(),
                new Receptionist(recResp2.getUserId(), recResp2.getUsername(), "", recResp2.getFullName(), recResp2.getEmail()), null);

        Invoice finalInvoice = apiClient.calculateAndGenerateInvoice(booked.getId(), "Credit Card");
        assertNotNull("Generated invoice must not be null", finalInvoice);
        assertEquals("Invoice must reflect doctor's updated treatment cost", diagnosedTreatment.getCost(), finalInvoice.getTreatmentCost(), 0.01);
        assertEquals("Total amount must equal consultation fee (1500) + diagnosed treatment fee",
                1500.00 + diagnosedTreatment.getCost(), finalInvoice.getTotalAmount(), 0.01);

        apiClient.logout();
    }
}
