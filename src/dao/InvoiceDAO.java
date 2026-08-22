package dao;

import model.Invoice;
import model.InvoiceCalculation;
import java.util.List;

/**
 * DAO interface for invoice calculation and persistence.
 *
 * @author Student
 */
public interface InvoiceDAO {
    /**
     * Calls MySQL stored procedure CalculateInvoiceTotal to compute invoice amounts.
     *
     * @param appointmentId the target appointment id.
     * @return populated InvoiceCalculation instance.
     */
    InvoiceCalculation calculateBill(int appointmentId);

    Invoice createInvoice(Invoice invoice);
    Invoice findByAppointmentId(int appointmentId);
    Invoice findById(int id);
    List<Invoice> findAll();
}
