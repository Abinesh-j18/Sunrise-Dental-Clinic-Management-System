package view.receipt;

import model.Invoice;
import model.InvoiceItem;

import java.awt.*;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.time.format.DateTimeFormatter;

/**
 * Receipt print renderer implementing java.awt.print.Printable.
 * Formats and prints an official clinic invoice receipt.
 *
 * @author Student
 */
public class ReceiptPrinter implements Printable {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final Invoice invoice;

    public ReceiptPrinter(Invoice invoice) {
        this.invoice = invoice;
    }

    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        if (pageIndex > 0) {
            return NO_SUCH_PAGE;
        }

        Graphics2D g2d = (Graphics2D) graphics;
        g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

        // Receipt layout settings
        int x = 40;
        int y = 50;
        int width = (int) pageFormat.getImageableWidth() - 80;

        // --- Header ---
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 18));
        g2d.setColor(new Color(13, 92, 117));
        g2d.drawString("SUNRISE DENTAL CLINIC", x, y);
        y += 18;

        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        g2d.setColor(Color.DARK_GRAY);
        g2d.drawString("45 Galle Road, Colombo 03 | Tel: 011-2345678 | info@sunrisedental.lk", x, y);
        y += 15;

        g2d.setColor(Color.LIGHT_GRAY);
        g2d.drawLine(x, y, x + width, y);
        y += 20;

        // --- Receipt Title & Number ---
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 13));
        g2d.setColor(Color.BLACK);
        g2d.drawString("OFFICIAL TREATMENT INVOICE & RECEIPT", x, y);
        y += 16;

        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        g2d.drawString("Invoice ID      : INV-" + String.format("%05d", invoice.getId()), x, y);
        g2d.drawString("Date & Time     : " + (invoice.getGeneratedDate() != null ? invoice.getGeneratedDate().format(DATE_TIME_FORMATTER) : "N/A"), x + 240, y);
        y += 14;

        if (invoice.getAppointment() != null) {
            g2d.drawString("Appointment No  : " + invoice.getAppointment().getAppointmentNumber(), x, y);
            g2d.drawString("Payment Status  : " + invoice.getStatus() + " (" + invoice.getPaymentMethod() + ")", x + 240, y);
            y += 18;

            g2d.setColor(new Color(240, 240, 240));
            g2d.fillRect(x, y, width, 40);
            g2d.setColor(Color.BLACK);

            y += 14;
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 10));
            g2d.drawString("Patient Details:", x + 8, y);
            g2d.drawString("Attending Dentist:", x + 240, y);
            y += 14;

            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            if (invoice.getAppointment().getPatient() != null) {
                g2d.drawString(invoice.getAppointment().getPatient().getName() + " (Tel: " + invoice.getAppointment().getPatient().getContactNumber() + ")", x + 8, y);
            }
            if (invoice.getAppointment().getDentist() != null) {
                g2d.drawString(invoice.getAppointment().getDentist().getFullName() + " - " + invoice.getAppointment().getDentist().getSpecialization(), x + 240, y);
            }
            y += 25;
        }

        // --- Itemized Table Header ---
        g2d.setColor(new Color(13, 92, 117));
        g2d.fillRect(x, y, width, 22);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 10));
        g2d.drawString("DESCRIPTION", x + 10, y + 15);
        g2d.drawString("AMOUNT (LKR)", x + width - 100, y + 15);
        y += 26;

        // --- Table Lines ---
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 10));

        if (invoice.getItems() != null && !invoice.getItems().isEmpty()) {
            for (InvoiceItem item : invoice.getItems()) {
                g2d.drawString(item.getDescription(), x + 10, y + 12);
                String amtStr = String.format("%.2f", item.getAmount());
                g2d.drawString(amtStr, x + width - 80, y + 12);
                y += 18;
                g2d.setColor(new Color(240, 240, 240));
                g2d.drawLine(x, y, x + width, y);
                g2d.setColor(Color.BLACK);
            }
        } else {
            g2d.drawString("Consultation & Examination Fee", x + 10, y + 12);
            g2d.drawString(String.format("%.2f", invoice.getConsultationFee()), x + width - 80, y + 12);
            y += 18;
            g2d.drawString("Dental Treatment Cost", x + 10, y + 12);
            g2d.drawString(String.format("%.2f", invoice.getTreatmentCost()), x + width - 80, y + 12);
            y += 18;
        }

        y += 15;
        g2d.setColor(Color.BLACK);
        g2d.drawLine(x + width - 180, y, x + width, y);
        y += 18;

        g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
        g2d.drawString("TOTAL AMOUNT:", x + width - 180, y);
        g2d.drawString("LKR " + String.format("%.2f", invoice.getTotalAmount()), x + width - 90, y);
        y += 8;
        g2d.drawLine(x + width - 180, y, x + width, y);
        g2d.drawLine(x + width - 180, y + 2, x + width, y + 2);
        y += 40;

        // --- Footer ---
        g2d.setFont(new Font("Segoe UI", Font.ITALIC, 9));
        g2d.setColor(Color.GRAY);
        g2d.drawString("Thank you for trusting Sunrise Dental Clinic with your dental care!", x, y);
        y += 12;
        g2d.drawString("Please retain this receipt for clinical follow-ups and insurance claims.", x, y);

        return PAGE_EXISTS;
    }

    /**
     * Opens the standard Java print dialog to print this receipt.
     */
    public boolean printReceipt() {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(this);
        if (job.printDialog()) {
            try {
                job.print();
                return true;
            } catch (PrinterException e) {
                return false;
            }
        }
        return false;
    }
}
