package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Domain entity representing a patient billing invoice.
 * Demonstrates:
 * - Aggregation: References Appointment (which exists independently)
 * - Composition: Owns its InvoiceItem line items (items lifecycle is managed by Invoice)
 *
 * @author Student
 */
public class Invoice {
    private int id;
    private Appointment appointment;       // Aggregation
    private double consultationFee;
    private double treatmentCost;
    private double totalAmount;
    private LocalDateTime generatedDate;
    private String status;                // PAID, PENDING, CANCELLED
    private String paymentMethod;
    private List<InvoiceItem> items = new ArrayList<>(); // Composition

    public Invoice() {
        this.generatedDate = LocalDateTime.now();
        this.status = "PAID";
        this.paymentMethod = "Cash";
    }

    public Invoice(int id, Appointment appointment, double consultationFee, double treatmentCost,
                   double totalAmount, LocalDateTime generatedDate, String status, String paymentMethod) {
        this.id = id;
        this.appointment = appointment;
        this.consultationFee = consultationFee;
        this.treatmentCost = treatmentCost;
        this.totalAmount = totalAmount;
        this.generatedDate = generatedDate != null ? generatedDate : LocalDateTime.now();
        this.status = status != null ? status : "PAID";
        this.paymentMethod = paymentMethod != null ? paymentMethod : "Cash";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Appointment getAppointment() {
        return appointment;
    }

    public void setAppointment(Appointment appointment) {
        this.appointment = appointment;
    }

    public double getConsultationFee() {
        return consultationFee;
    }

    public void setConsultationFee(double consultationFee) {
        this.consultationFee = consultationFee;
    }

    public double getTreatmentCost() {
        return treatmentCost;
    }

    public void setTreatmentCost(double treatmentCost) {
        this.treatmentCost = treatmentCost;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public LocalDateTime getGeneratedDate() {
        return generatedDate;
    }

    public void setGeneratedDate(LocalDateTime generatedDate) {
        this.generatedDate = generatedDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public List<InvoiceItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public void setItems(List<InvoiceItem> items) {
        this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
    }

    /**
     * Composition management: Adds a line item owned by this invoice.
     *
     * @param item the invoice item to attach.
     */
    public void addItem(InvoiceItem item) {
        if (item != null) {
            this.items.add(item);
        }
    }

    @Override
    public String toString() {
        return "Invoice{" +
                "id=" + id +
                ", appointment=" + (appointment != null ? appointment.getAppointmentNumber() : null) +
                ", consultationFee=" + consultationFee +
                ", treatmentCost=" + treatmentCost +
                ", totalAmount=" + totalAmount +
                ", generatedDate=" + generatedDate +
                '}';
    }
}
