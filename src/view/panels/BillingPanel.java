package view.panels;

import client.ApiClient;
import client.ApiException;
import model.Appointment;
import model.Invoice;
import model.InvoiceItem;
import view.MainDashboardFrame;
import view.UITheme;
import view.receipt.ReceiptPrinter;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;

/**
 * Screen for calculating, generating, and printing patient invoices.
 * Interacts with POST /invoices/{appointmentId} which invokes the MySQL stored procedure.
 *
 * @author Student
 */
public class BillingPanel extends JPanel {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final ApiClient apiClient;
    private final MainDashboardFrame parentFrame;

    private JTextField txtApptNumber;
    private JButton btnSearch;
    private JComboBox<String> cmbPaymentMethod;
    private JButton btnGenerateBill;
    private JButton btnPrintReceipt;
    private JButton btnViewReceiptText;
    private JLabel lblStatus;
    private JProgressBar progressBar;

    // Billing Breakdown Card Components
    private JPanel invoiceCard;
    private JLabel lblInvoiceId;
    private JLabel lblInvoiceDate;
    private JLabel lblPatientName;
    private JLabel lblDentistName;
    private JLabel lblTotalAmount;
    private JTable tblInvoiceItems;
    private DefaultTableModel itemsTableModel;

    private Appointment loadedAppointment;
    private Invoice currentInvoice;

    public BillingPanel(ApiClient apiClient) {
        this(apiClient, null);
    }

    public BillingPanel(ApiClient apiClient, MainDashboardFrame parentFrame) {
        this.apiClient = apiClient;
        this.parentFrame = parentFrame;
        initComponents(); initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 20));
        setOpaque(false);
        setBorder(new EmptyBorder(25, 30, 25, 30));

        // Header Title
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Calculate & Print Patient Bill");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.COLOR_TEXT_PRIMARY);

        JLabel lblDesc = new JLabel("Calculates consultation fee + treatment cost via MySQL Stored Procedure and prints official receipts.");
        lblDesc.setFont(UITheme.FONT_BODY);
        lblDesc.setForeground(UITheme.COLOR_TEXT_MUTED);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 4));
        titleBlock.setOpaque(false);
        titleBlock.add(lblTitle);
        titleBlock.add(lblDesc);

        headerPanel.add(titleBlock, BorderLayout.WEST);

        // Top Control Card (Search + Payment Method + Action)
        JPanel controlCard = UITheme.createCardPanel();
        controlCard.setLayout(new BoxLayout(controlCard, BoxLayout.Y_AXIS));

        // Row 1: Search Section
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        row1.setOpaque(false);
        row1.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSearch = UITheme.createFieldLabel("Appointment #:");
        txtApptNumber = UITheme.createTextField();
        txtApptNumber.setPreferredSize(new Dimension(200, 38));
        txtApptNumber.setFont(new Font("Segoe UI", Font.BOLD, 13));

        btnSearch = UITheme.createSecondaryButton("Find Appointment");
        btnSearch.setPreferredSize(new Dimension(160, 38));
        btnSearch.addActionListener(e -> findAppointment(txtApptNumber.getText().trim()));

        row1.add(lblSearch);
        row1.add(txtApptNumber);
        row1.add(btnSearch);

        // Row 2: Payment Method & Action
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        row2.setOpaque(false);
        row2.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblPayment = UITheme.createFieldLabel("Payment Method:");
        String[] paymentMethods = {"Cash", "Credit / Debit Card", "Health Insurance Direct", "Online Bank Transfer"};
        cmbPaymentMethod = new JComboBox<>(paymentMethods);
        cmbPaymentMethod.setFont(UITheme.FONT_BODY);
        cmbPaymentMethod.setPreferredSize(new Dimension(200, 38));

        btnGenerateBill = UITheme.createPrimaryButton("Calculate & Generate Bill");
        btnGenerateBill.setPreferredSize(new Dimension(220, 38));
        btnGenerateBill.addActionListener(e -> generateBill());

        row2.add(lblPayment);
        row2.add(cmbPaymentMethod);
        row2.add(btnGenerateBill);

        // Row 3: Status Message & Progress Bar
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        progressBar.setPreferredSize(new Dimension(120, 4));

        lblStatus = new JLabel("Enter an appointment number (e.g. APT-2026-0004) to calculate invoice.");
        lblStatus.setFont(UITheme.FONT_BODY);
        lblStatus.setForeground(UITheme.COLOR_TEXT_MUTED);

        JPanel row3 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        row3.setOpaque(false);
        row3.setAlignmentX(Component.LEFT_ALIGNMENT);
        row3.add(progressBar);
        row3.add(lblStatus);

        controlCard.add(row1);
        controlCard.add(Box.createVerticalStrut(4));
        controlCard.add(row2);
        controlCard.add(Box.createVerticalStrut(6));
        controlCard.add(row3);

        JPanel topSection = new JPanel(new BorderLayout(0, 15));
        topSection.setOpaque(false);
        topSection.add(headerPanel, BorderLayout.NORTH);
        topSection.add(controlCard, BorderLayout.SOUTH);

        add(topSection, BorderLayout.NORTH);

        // Center Invoice Card
        invoiceCard = UITheme.createCardPanel();
        invoiceCard.setLayout(new BorderLayout(0, 15));
        invoiceCard.setVisible(false);

        // Invoice Header
        JPanel invHeader = new JPanel(new BorderLayout());
        invHeader.setOpaque(false);

        JPanel invMetaLeft = new JPanel(new GridLayout(3, 1, 0, 4));
        invMetaLeft.setOpaque(false);

        lblInvoiceId = new JLabel("INVOICE # INV-00000");
        lblInvoiceId.setFont(UITheme.FONT_SUBTITLE);
        lblInvoiceId.setForeground(UITheme.COLOR_PRIMARY);

        lblPatientName = new JLabel("Patient: -");
        lblPatientName.setFont(UITheme.FONT_BODY_BOLD);

        lblDentistName = new JLabel("Dentist: -");
        lblDentistName.setFont(UITheme.FONT_BODY);

        invMetaLeft.add(lblInvoiceId);
        invMetaLeft.add(lblPatientName);
        invMetaLeft.add(lblDentistName);

        JPanel invMetaRight = new JPanel(new GridLayout(2, 1, 0, 4));
        invMetaRight.setOpaque(false);

        lblInvoiceDate = new JLabel("Date: -", SwingConstants.RIGHT);
        lblInvoiceDate.setFont(UITheme.FONT_BODY);
        lblInvoiceDate.setForeground(UITheme.COLOR_TEXT_MUTED);

        JLabel lblPaidBadge = UITheme.createStatusBadge("PAID IN FULL");
        lblPaidBadge.setHorizontalAlignment(SwingConstants.RIGHT);

        invMetaRight.add(lblInvoiceDate);
        invMetaRight.add(lblPaidBadge);

        invHeader.add(invMetaLeft, BorderLayout.WEST);
        invHeader.add(invMetaRight, BorderLayout.EAST);

        // Itemized Table
        String[] columns = {"Description", "Category", "Amount (LKR)"};
        itemsTableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblInvoiceItems = new JTable(itemsTableModel);
        tblInvoiceItems.setFont(UITheme.FONT_BODY);
        tblInvoiceItems.setFillsViewportHeight(true);

        JScrollPane itemsScroll = new JScrollPane(tblInvoiceItems);
        itemsScroll.setBorder(new LineBorder(UITheme.COLOR_BORDER, 1, true));

        // Invoice Footer (Total + Print Actions)
        JPanel invFooter = new JPanel(new BorderLayout());
        invFooter.setOpaque(false);

        lblTotalAmount = new JLabel("TOTAL DUE: LKR 0.00");
        lblTotalAmount.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTotalAmount.setForeground(UITheme.COLOR_PRIMARY);

        btnPrintReceipt = UITheme.createPrimaryButton("Print Official Bill / Receipt");
        btnPrintReceipt.setPreferredSize(new Dimension(230, 42));
        btnPrintReceipt.addActionListener(e -> printInvoice());

        btnViewReceiptText = UITheme.createSecondaryButton("View Printable Text");
        btnViewReceiptText.addActionListener(e -> showReceiptTextDialog());

        JPanel printButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        printButtons.setOpaque(false);
        printButtons.add(btnViewReceiptText);
        printButtons.add(btnPrintReceipt);

        invFooter.add(lblTotalAmount, BorderLayout.WEST);
        invFooter.add(printButtons, BorderLayout.EAST);

        invoiceCard.add(invHeader, BorderLayout.NORTH);
        invoiceCard.add(itemsScroll, BorderLayout.CENTER);
        invoiceCard.add(invFooter, BorderLayout.SOUTH);

        add(invoiceCard, BorderLayout.CENTER);
    }

    public void loadAppointmentForBilling(String apptNumber) {
        if (apptNumber != null && !apptNumber.isEmpty()) {
            txtApptNumber.setText(apptNumber);
            findAppointment(apptNumber);
        }
    }

    private void findAppointment(String apptNumber) {
        if (apptNumber == null || apptNumber.trim().isEmpty()) {
            lblStatus.setText("Please enter an appointment number.");
            lblStatus.setForeground(UITheme.COLOR_DANGER);
            return;
        }

        btnSearch.setEnabled(false);
        progressBar.setVisible(true);
        lblStatus.setText("Locating appointment: " + apptNumber + "...");
        lblStatus.setForeground(UITheme.COLOR_PRIMARY);

        SwingWorker<Appointment, Void> worker = new SwingWorker<>() {
            @Override
            protected Appointment doInBackground() throws Exception {
                return apiClient.getAppointmentByNumber(apptNumber.trim());
            }

            @Override
            protected void done() {
                btnSearch.setEnabled(true);
                progressBar.setVisible(false);
                try {
                    loadedAppointment = get();
                    lblStatus.setText("Found appointment for " + loadedAppointment.getPatient().getName() + ". Click 'Calculate & Generate Bill'.");
                    lblStatus.setForeground(UITheme.COLOR_SUCCESS);

                    // Check if already has an invoice
                    checkForExistingInvoice(loadedAppointment.getId());
                } catch (Exception e) {
                    loadedAppointment = null;
                    invoiceCard.setVisible(false);
                    lblStatus.setText("Appointment not found: " + e.getMessage());
                    lblStatus.setForeground(UITheme.COLOR_DANGER);
                }
            }
        };

        worker.execute();
    }

    private void checkForExistingInvoice(int apptId) {
        SwingWorker<Invoice, Void> worker = new SwingWorker<>() {
            @Override
            protected Invoice doInBackground() throws Exception {
                try {
                    return apiClient.getInvoiceByAppointmentId(apptId);
                } catch (ApiException ae) {
                    return null; // Not created yet
                }
            }

            @Override
            protected void done() {
                try {
                    Invoice inv = get();
                    if (inv != null) {
                        currentInvoice = inv;
                        displayInvoice(inv);
                        lblStatus.setText("Existing invoice retrieved for " + loadedAppointment.getAppointmentNumber());
                        lblStatus.setForeground(UITheme.COLOR_SUCCESS);
                    }
                } catch (Exception ignored) {
                }
            }
        };
        worker.execute();
    }

    private void generateBill() {
        if (loadedAppointment == null) {
            String num = txtApptNumber.getText().trim();
            if (!num.isEmpty()) {
                findAppointment(num);
            } else {
                lblStatus.setText("Please find an appointment first.");
                lblStatus.setForeground(UITheme.COLOR_DANGER);
            }
            return;
        }

        String paymentMethod = (String) cmbPaymentMethod.getSelectedItem();
        btnGenerateBill.setEnabled(false);
        progressBar.setVisible(true);
        lblStatus.setText("Invoking MySQL CalculateInvoiceTotal stored procedure...");
        lblStatus.setForeground(UITheme.COLOR_PRIMARY);

        SwingWorker<Invoice, Void> worker = new SwingWorker<>() {
            @Override
            protected Invoice doInBackground() throws Exception {
                return apiClient.calculateAndGenerateInvoice(loadedAppointment.getId(), paymentMethod);
            }

            @Override
            protected void done() {
                btnGenerateBill.setEnabled(true);
                progressBar.setVisible(false);
                try {
                    Invoice inv = get();
                    currentInvoice = inv;
                    displayInvoice(inv);
                    lblStatus.setText("Invoice generated successfully via stored procedure!");
                    lblStatus.setForeground(UITheme.COLOR_SUCCESS);
                } catch (Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    lblStatus.setText("Billing calculation failed: " + cause.getMessage());
                    lblStatus.setForeground(UITheme.COLOR_DANGER);
                    JOptionPane.showMessageDialog(BillingPanel.this,
                            cause.getMessage(),
                            "Billing Calculation Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }

    private void displayInvoice(Invoice inv) {
        lblInvoiceId.setText("INVOICE # INV-" + String.format("%05d", inv.getId()) + "  •  Appt: " + (inv.getAppointment() != null ? inv.getAppointment().getAppointmentNumber() : ""));
        lblInvoiceDate.setText("Generated: " + (inv.getGeneratedDate() != null ? inv.getGeneratedDate().format(DATE_TIME_FORMATTER) : "-"));

        if (inv.getAppointment() != null) {
            if (inv.getAppointment().getPatient() != null) {
                lblPatientName.setText("Patient: " + inv.getAppointment().getPatient().getName() + " (Contact: " + inv.getAppointment().getPatient().getContactNumber() + ")");
            }
            if (inv.getAppointment().getDentist() != null) {
                lblDentistName.setText("Attending Dentist: " + inv.getAppointment().getDentist().getFullName() + " - " + inv.getAppointment().getDentist().getSpecialization());
            }
        }

        itemsTableModel.setRowCount(0);
        if (inv.getItems() != null && !inv.getItems().isEmpty()) {
            for (InvoiceItem item : inv.getItems()) {
                String category = item.getDescription().toLowerCase().contains("consultation") ? "Clinic Fee" : "Treatment";
                itemsTableModel.addRow(new Object[]{
                        item.getDescription(),
                        category,
                        String.format("LKR %.2f", item.getAmount())
                });
            }
        } else {
            itemsTableModel.addRow(new Object[]{"Doctor Consultation Fee", "Clinic Fee", String.format("LKR %.2f", inv.getConsultationFee())});
            itemsTableModel.addRow(new Object[]{"Dental Treatment Fee", "Treatment", String.format("LKR %.2f", inv.getTreatmentCost())});
        }

        lblTotalAmount.setText("TOTAL AMOUNT: LKR " + String.format("%.2f", inv.getTotalAmount()));
        invoiceCard.setVisible(true);
        revalidate();
        repaint();
    }

    private void printInvoice() {
        if (currentInvoice == null) {
            JOptionPane.showMessageDialog(this, "No active invoice to print.", "Print Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        ReceiptPrinter printer = new ReceiptPrinter(currentInvoice);
        boolean success = printer.printReceipt();
        if (success) {
            JOptionPane.showMessageDialog(this, "Invoice sent to printer successfully.", "Print Completed", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void showReceiptTextDialog() {
        if (currentInvoice == null) return;

        StringBuilder sb = new StringBuilder();
        sb.append("====================================================\n");
        sb.append("               SUNRISE DENTAL CLINIC                \n");
        sb.append("          45 Galle Road, Colombo 03, Sri Lanka      \n");
        sb.append("           Tel: 011-2345678 | info@sunrisedental.lk \n");
        sb.append("====================================================\n");
        sb.append("INVOICE ID     : INV-").append(String.format("%05d", currentInvoice.getId())).append("\n");
        sb.append("DATE & TIME    : ").append(currentInvoice.getGeneratedDate() != null ? currentInvoice.getGeneratedDate().format(DATE_TIME_FORMATTER) : "N/A").append("\n");
        if (currentInvoice.getAppointment() != null) {
            sb.append("APPOINTMENT NO : ").append(currentInvoice.getAppointment().getAppointmentNumber()).append("\n");
            sb.append("PATIENT NAME   : ").append(currentInvoice.getAppointment().getPatient().getName()).append("\n");
            sb.append("CONTACT        : ").append(currentInvoice.getAppointment().getPatient().getContactNumber()).append("\n");
            sb.append("DENTIST        : ").append(currentInvoice.getAppointment().getDentist().getFullName()).append("\n");
            sb.append("SPECIALTY      : ").append(currentInvoice.getAppointment().getDentist().getSpecialization()).append("\n");
        }
        sb.append("----------------------------------------------------\n");
        sb.append(String.format("%-36s %14s\n", "DESCRIPTION", "AMOUNT (LKR)"));
        sb.append("----------------------------------------------------\n");
        if (currentInvoice.getItems() != null) {
            for (InvoiceItem it : currentInvoice.getItems()) {
                sb.append(String.format("%-36s %14.2f\n", it.getDescription(), it.getAmount()));
            }
        }
        sb.append("----------------------------------------------------\n");
        sb.append(String.format("%-36s %14.2f\n", "TOTAL DUE", currentInvoice.getTotalAmount()));
        sb.append("====================================================\n");
        sb.append("PAYMENT STATUS : ").append(currentInvoice.getStatus()).append(" (").append(currentInvoice.getPaymentMethod()).append(")\n\n");
        sb.append("Thank you for choosing Sunrise Dental Clinic!\n");

        JTextArea ta = new JTextArea(sb.toString(), 20, 48);
        ta.setFont(UITheme.FONT_MONO);
        ta.setEditable(false);
        JScrollPane sp = new JScrollPane(ta);

        JOptionPane.showMessageDialog(this, sp, "Official Invoice Receipt Preview", JOptionPane.PLAIN_MESSAGE);
    }

    /**`n     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}