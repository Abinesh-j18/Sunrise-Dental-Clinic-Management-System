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

    private DefaultTableModel itemsTableModel;
    private Appointment loadedAppointment;
    private Invoice currentInvoice;

    public BillingPanel(ApiClient apiClient) {
        this(apiClient, null);
    }

    public BillingPanel(ApiClient apiClient, MainDashboardFrame parentFrame) {
        this.apiClient = apiClient;
        this.parentFrame = parentFrame;
        initComponents();
        initLogic();
    }

    private void initLogic() {
        setOpaque(false);
        itemsTableModel = (DefaultTableModel) tblInvoiceItems.getModel();
        tblInvoiceItems.getTableHeader().setFont(UITheme.FONT_SECTION);
        tblInvoiceItems.setRowHeight(32);

        btnSearch.addActionListener(e -> findAppointment(txtApptNumber.getText().trim()));
        btnGenerateBill.addActionListener(e -> generateBill());
        btnPrintReceipt.addActionListener(e -> printReceipt());
        btnViewReceiptText.addActionListener(e -> showReceiptTextDialog());

        invoiceCard.setVisible(false);
    }

    public void loadAppointmentForBilling(String apptNumber) {
        if (apptNumber != null && !apptNumber.trim().isEmpty()) {
            txtApptNumber.setText(apptNumber.trim());
            findAppointment(apptNumber.trim());
        }
    }

    private void findAppointment(String apptNumber) {
        if (apptNumber == null || apptNumber.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter an appointment number.", "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        btnSearch.setEnabled(false);

        SwingWorker<Appointment, Void> worker = new SwingWorker<>() {
            @Override
            protected Appointment doInBackground() throws Exception {
                return apiClient.getAppointmentByNumber(apptNumber.trim());
            }

            @Override
            protected void done() {
                btnSearch.setEnabled(true);
                try {
                    loadedAppointment = get();
                    checkForExistingInvoice(loadedAppointment.getId());
                } catch (Exception e) {
                    loadedAppointment = null;
                    invoiceCard.setVisible(false);
                    JOptionPane.showMessageDialog(BillingPanel.this, "Appointment not found: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
                    return null;
                }
            }

            @Override
            protected void done() {
                try {
                    Invoice inv = get();
                    if (inv != null) {
                        currentInvoice = inv;
                        displayInvoice(inv);
                    } else {
                        invoiceCard.setVisible(false);
                        btnGenerateBill.setEnabled(true);
                        btnPrintReceipt.setEnabled(false);
                        btnViewReceiptText.setEnabled(false);
                        JOptionPane.showMessageDialog(BillingPanel.this, "Appointment loaded for " + loadedAppointment.getPatient().getName() + ". Click 'Calculate & Generate Bill'.", "Appointment Found", JOptionPane.INFORMATION_MESSAGE);
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
            if (num.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter an appointment number first.", "Action Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            findAppointment(num);
            return;
        }

        btnGenerateBill.setEnabled(false);

        String paymentMethod = (String) cmbPaymentMethod.getSelectedItem();
        if (paymentMethod == null) paymentMethod = "Cash";

        final String method = paymentMethod;
        SwingWorker<Invoice, Void> worker = new SwingWorker<>() {
            @Override
            protected Invoice doInBackground() throws Exception {
                return apiClient.calculateAndGenerateInvoice(loadedAppointment.getId(), method);
            }

            @Override
            protected void done() {
                btnGenerateBill.setEnabled(true);
                try {
                    currentInvoice = get();
                    displayInvoice(currentInvoice);
                    JOptionPane.showMessageDialog(BillingPanel.this, "Invoice generated successfully via Stored Procedure!\nTotal: LKR " + String.format("%,.2f", currentInvoice.getTotalAmount()), "Success", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(BillingPanel.this, "Failed to generate bill: " + e.getMessage(), "Billing Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }

    private void displayInvoice(Invoice inv) {
        if (inv == null) return;

        lblInvoiceId.setText("Invoice #: " + inv.getId());
        lblInvoiceDate.setText("Generated: " + (inv.getGeneratedDate() != null ? inv.getGeneratedDate().format(DATE_TIME_FORMATTER) : "N/A"));
        lblPatientName.setText("Patient: " + (loadedAppointment != null ? loadedAppointment.getPatient().getName() : "--"));
        lblDentistName.setText("Dentist: " + (loadedAppointment != null ? loadedAppointment.getDentist().getFullName() : "--"));

        itemsTableModel.setRowCount(0);
        if (inv.getItems() != null && !inv.getItems().isEmpty()) {
            for (InvoiceItem item : inv.getItems()) {
                itemsTableModel.addRow(new Object[]{
                        item.getDescription(),
                        String.format("%,.2f", item.getAmount())
                });
            }
        }

        lblTotalAmount.setText("TOTAL: LKR " + String.format("%,.2f", inv.getTotalAmount()));

        btnPrintReceipt.setEnabled(true);
        btnViewReceiptText.setEnabled(true);
        btnGenerateBill.setEnabled(false);
        invoiceCard.setVisible(true);
        revalidate();
        repaint();
    }

    private void printReceipt() {
        if (currentInvoice == null) {
            JOptionPane.showMessageDialog(this, "No active invoice to print. Please generate an invoice first.", "Print Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        ReceiptPrinter printer = new ReceiptPrinter(currentInvoice);
        printer.printReceipt();
    }

    private void showReceiptTextDialog() {
        if (currentInvoice == null || loadedAppointment == null) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("====================================================\n");
        sb.append("           SUNRISE DENTAL CLINIC                    \n");
        sb.append("      Official Patient Treatment Receipt            \n");
        sb.append("====================================================\n");
        sb.append(String.format("%-20s: %s\n", "Invoice No", currentInvoice.getId()));
        sb.append(String.format("%-20s: %s\n", "Appointment No", loadedAppointment.getAppointmentNumber()));
        sb.append(String.format("%-20s: %s\n", "Date", currentInvoice.getGeneratedDate() != null ? currentInvoice.getGeneratedDate().format(DATE_TIME_FORMATTER) : "N/A"));
        sb.append(String.format("%-20s: %s\n", "Patient Name", loadedAppointment.getPatient().getName()));
        sb.append(String.format("%-20s: %s\n", "Dentist", loadedAppointment.getDentist().getFullName()));
        sb.append(String.format("%-20s: %s\n", "Treatment", loadedAppointment.getTreatment().getType()));
        sb.append("----------------------------------------------------\n");
        sb.append(String.format("%-36s %14s\n", "ITEM DESCRIPTION", "AMOUNT (LKR)"));
        sb.append("----------------------------------------------------\n");

        if (currentInvoice.getItems() != null) {
            for (InvoiceItem item : currentInvoice.getItems()) {
                sb.append(String.format("%-36s %14.2f\n", item.getDescription(), item.getAmount()));
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

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        headerPanel = new javax.swing.JPanel();
        lblTitle = new javax.swing.JLabel();
        lblDesc = new javax.swing.JLabel();
        controlCard = new javax.swing.JPanel();
        lblSearch = new javax.swing.JLabel();
        txtApptNumber = new javax.swing.JTextField();
        btnSearch = new javax.swing.JButton();
        lblPayment = new javax.swing.JLabel();
        cmbPaymentMethod = new javax.swing.JComboBox<>();
        btnGenerateBill = new javax.swing.JButton();
        btnPrintReceipt = new javax.swing.JButton();
        btnViewReceiptText = new javax.swing.JButton();
        invoiceCard = new javax.swing.JPanel();
        lblInvoiceId = new javax.swing.JLabel();
        lblInvoiceDate = new javax.swing.JLabel();
        lblPatientName = new javax.swing.JLabel();
        lblDentistName = new javax.swing.JLabel();
        scrollItems = new javax.swing.JScrollPane();
        tblInvoiceItems = new javax.swing.JTable();
        lblTotalAmount = new javax.swing.JLabel();

        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblTitle.setText("Calculate & Print Patient Bill");

        lblDesc.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblDesc.setText("Calculates consultation fee + treatment cost via MySQL Stored Procedure and prints official receipts.");

        javax.swing.GroupLayout headerPanelLayout = new javax.swing.GroupLayout(headerPanel);
        headerPanel.setLayout(headerPanelLayout);
        headerPanelLayout.setHorizontalGroup(
            headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(headerPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTitle)
                    .addComponent(lblDesc))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        headerPanelLayout.setVerticalGroup(
            headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(headerPanelLayout.createSequentialGroup()
                .addComponent(lblTitle)
                .addGap(4, 4, 4)
                .addComponent(lblDesc))
        );

        controlCard.setBackground(new java.awt.Color(255, 255, 255));
        controlCard.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(226, 232, 240), 1, true));

        lblSearch.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblSearch.setText("Appointment #:");

        txtApptNumber.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N

        btnSearch.setText("Find Appointment");

        lblPayment.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblPayment.setText("Payment Method:");

        cmbPaymentMethod.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Cash", "Credit Card", "Insurance" }));

        btnGenerateBill.setText("Calculate & Generate Bill");

        btnPrintReceipt.setText("Print Official Bill");
        btnPrintReceipt.setEnabled(false);

        btnViewReceiptText.setText("Receipt Preview");
        btnViewReceiptText.setEnabled(false);

        javax.swing.GroupLayout controlCardLayout = new javax.swing.GroupLayout(controlCard);
        controlCard.setLayout(controlCardLayout);
        controlCardLayout.setHorizontalGroup(
            controlCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(controlCardLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(controlCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(controlCardLayout.createSequentialGroup()
                        .addComponent(lblSearch)
                        .addGap(8, 8, 8)
                        .addComponent(txtApptNumber, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(btnSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(controlCardLayout.createSequentialGroup()
                        .addComponent(lblPayment)
                        .addGap(8, 8, 8)
                        .addComponent(cmbPaymentMethod, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(btnGenerateBill, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(btnPrintReceipt, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(10, 10, 10)
                        .addComponent(btnViewReceiptText, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(16, Short.MAX_VALUE))
        );
        controlCardLayout.setVerticalGroup(
            controlCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(controlCardLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(controlCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblSearch)
                    .addComponent(txtApptNumber, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addGroup(controlCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblPayment)
                    .addComponent(cmbPaymentMethod, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnGenerateBill, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnPrintReceipt, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnViewReceiptText, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(14, 14, 14))
        );

        invoiceCard.setBackground(new java.awt.Color(255, 255, 255));
        invoiceCard.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(226, 232, 240), 1, true));

        lblInvoiceId.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblInvoiceId.setText("Invoice #: --");

        lblInvoiceDate.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblInvoiceDate.setText("Date: --");

        lblPatientName.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblPatientName.setText("Patient: --");

        lblDentistName.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblDentistName.setText("Dentist: --");

        tblInvoiceItems.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Item Description", "Amount (LKR)"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        scrollItems.setViewportView(tblInvoiceItems);

        lblTotalAmount.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblTotalAmount.setText("TOTAL: LKR 0.00");

        javax.swing.GroupLayout invoiceCardLayout = new javax.swing.GroupLayout(invoiceCard);
        invoiceCard.setLayout(invoiceCardLayout);
        invoiceCardLayout.setHorizontalGroup(
            invoiceCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(invoiceCardLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(invoiceCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(scrollItems)
                    .addGroup(invoiceCardLayout.createSequentialGroup()
                        .addGroup(invoiceCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblPatientName)
                            .addComponent(lblInvoiceId))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 200, Short.MAX_VALUE)
                        .addGroup(invoiceCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblDentistName)
                            .addComponent(lblInvoiceDate)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, invoiceCardLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(lblTotalAmount)))
                .addGap(16, 16, 16))
        );
        invoiceCardLayout.setVerticalGroup(
            invoiceCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(invoiceCardLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(invoiceCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblInvoiceId)
                    .addComponent(lblInvoiceDate))
                .addGap(8, 8, 8)
                .addGroup(invoiceCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblPatientName)
                    .addComponent(lblDentistName))
                .addGap(12, 12, 12)
                .addComponent(scrollItems, javax.swing.GroupLayout.DEFAULT_SIZE, 230, Short.MAX_VALUE)
                .addGap(10, 10, 10)
                .addComponent(lblTotalAmount)
                .addGap(14, 14, 14))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(headerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(controlCard, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(invoiceCard, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(25, 25, 25))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(headerPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(15, 15, 15)
                .addComponent(controlCard, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(15, 15, 15)
                .addComponent(invoiceCard, javax.swing.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                .addGap(20, 20, 20))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnGenerateBill;
    private javax.swing.JButton btnPrintReceipt;
    private javax.swing.JButton btnSearch;
    private javax.swing.JButton btnViewReceiptText;
    private javax.swing.JComboBox<String> cmbPaymentMethod;
    private javax.swing.JPanel controlCard;
    private javax.swing.JPanel headerPanel;
    private javax.swing.JPanel invoiceCard;
    private javax.swing.JLabel lblDentistName;
    private javax.swing.JLabel lblDesc;
    private javax.swing.JLabel lblInvoiceDate;
    private javax.swing.JLabel lblInvoiceId;
    private javax.swing.JLabel lblPatientName;
    private javax.swing.JLabel lblPayment;
    private javax.swing.JLabel lblSearch;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JLabel lblTotalAmount;
    private javax.swing.JScrollPane scrollItems;
    private javax.swing.JTable tblInvoiceItems;
    private javax.swing.JTextField txtApptNumber;
    // End of variables declaration//GEN-END:variables
}