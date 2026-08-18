package model;

/**
 * Domain entity representing an itemized line entry within an invoice.
 * Demonstrates composition relationship with Invoice (owned by Invoice).
 *
 * @author Student
 */
public class InvoiceItem {
    private int id;
    private int invoiceId;
    private String description;
    private double amount;

    public InvoiceItem() {
    }

    public InvoiceItem(String description, double amount) {
        this.description = description;
        this.amount = amount;
    }

    public InvoiceItem(int id, int invoiceId, String description, double amount) {
        this.id = id;
        this.invoiceId = invoiceId;
        this.description = description;
        this.amount = amount;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(int invoiceId) {
        this.invoiceId = invoiceId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return String.format("%s: LKR %.2f", description, amount);
    }
}
