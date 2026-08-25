package server.dto;

/**
 * Invoice generation request payload.
 *
 * @author Student
 */
public class GenerateInvoiceRequest {
    private String paymentMethod;

    public GenerateInvoiceRequest() {
        this.paymentMethod = "Cash";
    }

    public GenerateInvoiceRequest(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
