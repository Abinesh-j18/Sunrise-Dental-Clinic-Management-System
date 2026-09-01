package dao;

import model.Appointment;
import model.Invoice;
import model.InvoiceCalculation;
import model.InvoiceItem;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for InvoiceDAO, CalculateInvoiceTotal stored procedure,
 * and transaction handling.
 *
 * @author Student
 */
public class InvoiceDAOTest {
    private InvoiceDAO invoiceDAO;
    private AppointmentDAO appointmentDAO;

    @Before
    public void setUp() {
        invoiceDAO = new InvoiceDAOImpl();
        appointmentDAO = new AppointmentDAOImpl();
    }

    @Test
    public void testCalculateInvoiceTotalStoredProcedure() {
        // Appointment 1 has Treatment 2 (Dental Scaling & Polishing, Cost: 3500.00)
        // Standard consultation fee is 1500.00 -> Total should be 5000.00
        InvoiceCalculation calc = invoiceDAO.calculateBill(1);
        assertNotNull("Invoice calculation from stored procedure should not be null", calc);
        assertEquals("Consultation fee should be 1500.00", 1500.00, calc.getConsultationFee(), 0.01);
        assertEquals("Treatment cost should be 3500.00", 3500.00, calc.getTreatmentCost(), 0.01);
        assertEquals("Total amount should equal consultation fee + treatment cost", 5000.00, calc.getTotalAmount(), 0.01);
    }

    @Test
    public void testCreateInvoiceWithItemsComposition() {
        Appointment appt = appointmentDAO.findById(2);
        assertNotNull("Appointment 2 should exist", appt);

        Invoice existing = invoiceDAO.findByAppointmentId(appt.getId());
        if (existing == null) {
            InvoiceCalculation calc = invoiceDAO.calculateBill(appt.getId());
            assertNotNull("Calculation must succeed", calc);

            Invoice invoice = new Invoice();
            invoice.setAppointment(appt);
            invoice.setConsultationFee(calc.getConsultationFee());
            invoice.setTreatmentCost(calc.getTreatmentCost());
            invoice.setTotalAmount(calc.getTotalAmount());
            invoice.setStatus("PAID");
            invoice.setPaymentMethod("Cash");

            // Add composed line items
            invoice.addItem(new InvoiceItem("Doctor Consultation Fee", calc.getConsultationFee()));
            invoice.addItem(new InvoiceItem(appt.getTreatment().getType(), calc.getTreatmentCost()));

            Invoice created = invoiceDAO.createInvoice(invoice);
            assertNotNull("Created invoice must not be null", created);
            assertTrue("Created invoice must have an ID", created.getId() > 0);
            assertNotNull("Created invoice must contain line items", created.getItems());
            assertEquals(2, created.getItems().size());
        } else {
            assertNotNull("Existing invoice found", existing);
            assertTrue("Invoice total must be greater than 0", existing.getTotalAmount() > 0);
        }
    }
}
