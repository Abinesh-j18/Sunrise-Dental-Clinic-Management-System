package model;

/**
 * Data holder for invoice computation returned by the stored procedure CalculateInvoiceTotal.
 *
 * @author Student
 */
public class InvoiceCalculation {
    private int appointmentId;
    private double consultationFee;
    private double treatmentCost;
    private double totalAmount;

    public InvoiceCalculation() {
    }

    public InvoiceCalculation(int appointmentId, double consultationFee, double treatmentCost, double totalAmount) {
        this.appointmentId = appointmentId;
        this.consultationFee = consultationFee;
        this.treatmentCost = treatmentCost;
        this.totalAmount = totalAmount;
    }

    public int getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(int appointmentId) {
        this.appointmentId = appointmentId;
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

    @Override
    public String toString() {
        return String.format("Calculation [Consultation: LKR %.2f, Treatment: LKR %.2f, Total: LKR %.2f]",
                consultationFee, treatmentCost, totalAmount);
    }
}
