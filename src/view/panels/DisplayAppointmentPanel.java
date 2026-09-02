package view.panels;

import client.ApiClient;
import client.ApiException;
import client.SessionContext;
import model.Appointment;
import model.User;
import view.MainDashboardFrame;
import view.UITheme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Screen for searching and displaying comprehensive appointment details.
 * Communicates with GET /appointments/{apptNumber} via SwingWorker.
 *
 * @author Student
 */
public class DisplayAppointmentPanel extends JPanel {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");

    private final ApiClient apiClient;
    private final MainDashboardFrame parentFrame;

    
    
    private JProgressBar progressBar;
    private JLabel lblStatus;

    // Detail Display Components
    private JPanel detailsContainer;
    private JLabel lblApptNumberValue;
    private JLabel lblStatusBadge;
    private JLabel lblDateTimeValue;
    private JLabel lblPatientNameValue;
    private JLabel lblPatientContactValue;
    private JLabel lblPatientAddressValue;
    private JLabel lblPatientEmailValue;
    private JLabel lblDentistNameValue;
    private JLabel lblDentistSpecValue;
    private JLabel lblDentistContactValue;
    private JLabel lblTreatmentTypeValue;
    private JLabel lblTreatmentCostValue;
    private JTextArea txtNotesValue;
    private JButton btnProceedToBilling;

    private Appointment currentAppointment;

    public DisplayAppointmentPanel(ApiClient apiClient) {
        this(apiClient, null);
    }

    public DisplayAppointmentPanel(ApiClient apiClient, MainDashboardFrame parentFrame) {
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

        JLabel lblTitle = new JLabel("Display Appointment Details");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.COLOR_TEXT_PRIMARY);

        JLabel lblDesc = new JLabel("Search by unique appointment number (e.g. APT-2026-0001) to view complete clinical and patient details.");
        lblDesc.setFont(UITheme.FONT_BODY);
        lblDesc.setForeground(UITheme.COLOR_TEXT_MUTED);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 4));
        titleBlock.setOpaque(false);
        titleBlock.add(lblTitle);
        titleBlock.add(lblDesc);

        headerPanel.add(titleBlock, BorderLayout.WEST);

        // Search Bar Card
        JPanel searchCard = UITheme.createCardPanel();
        searchCard.setLayout(new BorderLayout(15, 0));

        JPanel searchInputPanel = new JPanel(new BorderLayout(10, 0));
        searchInputPanel.setOpaque(false);

        JLabel lblSearch = new JLabel("Appointment Number:");
        lblSearch.setFont(UITheme.FONT_BODY_BOLD);
        lblSearch.setForeground(UITheme.COLOR_TEXT_PRIMARY);

        txtSearchApptNumber = UITheme.createTextField();
        txtSearchApptNumber.setFont(new Font("Segoe UI", Font.BOLD, 14));
        txtSearchApptNumber.setToolTipText("Enter format APT-YYYY-XXXX (e.g. APT-2026-0001)");

        txtSearchApptNumber.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    searchAppointment(txtSearchApptNumber.getText().trim());
                }
            }
        });

        btnSearch = UITheme.createPrimaryButton("Search Appointment");
        btnSearch.setPreferredSize(new Dimension(180, 38));
        btnSearch.addActionListener(e -> searchAppointment(txtSearchApptNumber.getText().trim()));

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        progressBar.setPreferredSize(new Dimension(100, 4));

        lblStatus = new JLabel(" ");
        lblStatus.setFont(UITheme.FONT_BODY);
        lblStatus.setForeground(UITheme.COLOR_TEXT_MUTED);

        searchInputPanel.add(lblSearch, BorderLayout.WEST);
        searchInputPanel.add(txtSearchApptNumber, BorderLayout.CENTER);
        searchInputPanel.add(btnSearch, BorderLayout.EAST);

        searchCard.add(searchInputPanel, BorderLayout.CENTER);
        searchCard.add(lblStatus, BorderLayout.SOUTH);

        JPanel topContainer = new JPanel(new BorderLayout(0, 15));
        topContainer.setOpaque(false);
        topContainer.add(headerPanel, BorderLayout.NORTH);
        topContainer.add(searchCard, BorderLayout.SOUTH);

        add(topContainer, BorderLayout.NORTH);

        // Details Container (3 Sections in 2 Columns)
        detailsContainer = new JPanel(new GridLayout(1, 2, 25, 0));
        detailsContainer.setOpaque(false);
        detailsContainer.setVisible(false);

        // Left Details Card: Patient & Clinical Notes
        JPanel leftCard = UITheme.createCardPanel();
        leftCard.setLayout(new BoxLayout(leftCard, BoxLayout.Y_AXIS));

        JLabel lblPatientSec = new JLabel("Patient Record");
        lblPatientSec.setFont(UITheme.FONT_SUBTITLE);
        lblPatientSec.setForeground(UITheme.COLOR_PRIMARY);
        lblPatientSec.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblPatientSec.setHorizontalAlignment(SwingConstants.LEFT);

        lblPatientNameValue = new JLabel("-");
        lblPatientNameValue.setFont(UITheme.FONT_SECTION);
        lblPatientNameValue.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblPatientContactValue = new JLabel("-");
        lblPatientContactValue.setFont(UITheme.FONT_BODY);
        lblPatientContactValue.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblPatientAddressValue = new JLabel("-");
        lblPatientAddressValue.setFont(UITheme.FONT_BODY);
        lblPatientAddressValue.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblPatientEmailValue = new JLabel("-");
        lblPatientEmailValue.setFont(UITheme.FONT_BODY);
        lblPatientEmailValue.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtNotesValue = new JTextArea(4, 20);
        txtNotesValue.setFont(UITheme.FONT_BODY);
        txtNotesValue.setEditable(false);
        txtNotesValue.setLineWrap(true);
        txtNotesValue.setWrapStyleWord(true);
        txtNotesValue.setBackground(UITheme.COLOR_BG);
        txtNotesValue.setBorder(new CompoundBorder(new LineBorder(UITheme.COLOR_BORDER, 1, true), new EmptyBorder(8, 8, 8, 8)));
        txtNotesValue.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblNotesHeader = UITheme.createFieldLabel("Appointment Notes:");
        lblNotesHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblNotesHeader.setHorizontalAlignment(SwingConstants.LEFT);

        leftCard.add(lblPatientSec);
        leftCard.add(Box.createVerticalStrut(14));
        leftCard.add(createDetailRow("Patient Full Name:", lblPatientNameValue));
        leftCard.add(Box.createVerticalStrut(10));
        leftCard.add(createDetailRow("Contact Number:", lblPatientContactValue));
        leftCard.add(Box.createVerticalStrut(10));
        leftCard.add(createDetailRow("Residential Address:", lblPatientAddressValue));
        leftCard.add(Box.createVerticalStrut(10));
        leftCard.add(createDetailRow("Email Address:", lblPatientEmailValue));
        leftCard.add(Box.createVerticalStrut(16));
        leftCard.add(lblNotesHeader);
        leftCard.add(Box.createVerticalStrut(6));
        leftCard.add(txtNotesValue);
        leftCard.add(Box.createVerticalGlue());

        // Right Details Card: Appointment & Treatment & Dentist
        JPanel rightCard = UITheme.createCardPanel();
        rightCard.setLayout(new BoxLayout(rightCard, BoxLayout.Y_AXIS));

        JPanel apptHeaderRow = new JPanel(new BorderLayout());
        apptHeaderRow.setOpaque(false);
        apptHeaderRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        apptHeaderRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblApptNumberValue = new JLabel("APT-0000-0000");
        lblApptNumberValue.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblApptNumberValue.setForeground(UITheme.COLOR_PRIMARY);

        lblStatusBadge = UITheme.createStatusBadge("SCHEDULED");

        apptHeaderRow.add(lblApptNumberValue, BorderLayout.WEST);
        apptHeaderRow.add(lblStatusBadge, BorderLayout.EAST);

        lblDateTimeValue = new JLabel("-");
        lblDateTimeValue.setFont(UITheme.FONT_BODY_BOLD);

        lblDentistNameValue = new JLabel("-");
        lblDentistNameValue.setFont(UITheme.FONT_BODY_BOLD);

        lblDentistSpecValue = new JLabel("-");
        lblDentistSpecValue.setFont(UITheme.FONT_BODY);

        lblDentistContactValue = new JLabel("-");
        lblDentistContactValue.setFont(UITheme.FONT_BODY);

        lblTreatmentTypeValue = new JLabel("-");
        lblTreatmentTypeValue.setFont(UITheme.FONT_BODY_BOLD);

        lblTreatmentCostValue = new JLabel("-");
        lblTreatmentCostValue.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTreatmentCostValue.setForeground(UITheme.COLOR_PRIMARY);

        btnProceedToBilling = UITheme.createSecondaryButton("Proceed to Billing & Invoice");
        btnProceedToBilling.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnProceedToBilling.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btnProceedToBilling.setPreferredSize(new Dimension(Integer.MAX_VALUE, 42));
        btnProceedToBilling.addActionListener(e -> {
            if (currentAppointment != null && parentFrame != null) {
                parentFrame.showBilling(currentAppointment.getAppointmentNumber());
            }
        });

        JButton btnDoctorUpdateTreatment = UITheme.createPrimaryButton("Update Clinical Diagnosis & Notes");
        btnDoctorUpdateTreatment.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnDoctorUpdateTreatment.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btnDoctorUpdateTreatment.setPreferredSize(new Dimension(Integer.MAX_VALUE, 42));
        btnDoctorUpdateTreatment.addActionListener(e -> openDoctorDiagnosisDialog());

        User curUser = SessionContext.getInstance().getCurrentUser();
        boolean canBill = curUser != null && ("Receptionist".equalsIgnoreCase(curUser.getRole()) || "Administrator".equalsIgnoreCase(curUser.getRole()));

        JPanel actionsPanel = new JPanel(new GridLayout(canBill ? 2 : 1, 1, 0, 8));
        actionsPanel.setOpaque(false);
        actionsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, canBill ? 92 : 44));
        actionsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        actionsPanel.add(btnDoctorUpdateTreatment);
        if (canBill) {
            actionsPanel.add(btnProceedToBilling);
        }

        rightCard.add(apptHeaderRow);
        rightCard.add(Box.createVerticalStrut(14));
        rightCard.add(createDetailRow("Scheduled Date & Time:", lblDateTimeValue));
        rightCard.add(Box.createVerticalStrut(10));
        rightCard.add(createDetailRow("Attending Dentist:", lblDentistNameValue));
        rightCard.add(Box.createVerticalStrut(6));
        rightCard.add(createDetailRow("Specialization:", lblDentistSpecValue));
        rightCard.add(Box.createVerticalStrut(6));
        rightCard.add(createDetailRow("Dentist Contact:", lblDentistContactValue));
        rightCard.add(Box.createVerticalStrut(14));
        rightCard.add(new JSeparator());
        rightCard.add(Box.createVerticalStrut(12));
        rightCard.add(createDetailRow("Prescribed Treatment:", lblTreatmentTypeValue));
        rightCard.add(Box.createVerticalStrut(8));
        rightCard.add(createDetailRow("Standard Treatment Cost:", lblTreatmentCostValue));
        rightCard.add(Box.createVerticalStrut(18));
        rightCard.add(actionsPanel);
        rightCard.add(Box.createVerticalGlue());

        detailsContainer.add(leftCard);
        detailsContainer.add(rightCard);

        add(detailsContainer, BorderLayout.CENTER);
    }

    private void openDoctorDiagnosisDialog() {
        if (currentAppointment == null) {
            return;
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Doctor's Clinical Diagnosis & Treatment Update", true);
        dialog.setSize(560, 560);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        // 1. Header Banner
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, UITheme.COLOR_BORDER),
                new EmptyBorder(16, 24, 16, 24)
        ));

        JLabel lblModalTitle = new JLabel("Update Clinical Treatment & Notes");
        lblModalTitle.setFont(UITheme.FONT_SUBTITLE);
        lblModalTitle.setForeground(UITheme.COLOR_TEXT_PRIMARY);

        String patientName = currentAppointment.getPatient() != null ? currentAppointment.getPatient().getName() : "-";
        JLabel lblModalSub = new JLabel("Patient: " + patientName + "  |  Appt: " + currentAppointment.getAppointmentNumber());
        lblModalSub.setFont(UITheme.FONT_SMALL);
        lblModalSub.setForeground(UITheme.COLOR_TEXT_MUTED);

        JPanel headerText = new JPanel(new GridLayout(2, 1, 0, 3));
        headerText.setOpaque(false);
        headerText.add(lblModalTitle);
        headerText.add(lblModalSub);
        header.add(headerText, BorderLayout.CENTER);

        // 2. Center Form Body (strictly left-aligned)
        JPanel formCenter = new JPanel();
        formCenter.setLayout(new BoxLayout(formCenter, BoxLayout.Y_AXIS));
        formCenter.setBackground(UITheme.COLOR_BG);
        formCenter.setBorder(new EmptyBorder(18, 24, 18, 24));

        JLabel lblTrt = UITheme.createFieldLabel("Diagnosed Clinical Treatment Procedure:");
        lblTrt.setAlignmentX(Component.LEFT_ALIGNMENT);

        JComboBox<model.Treatment> cbTreatments = new JComboBox<>();
        cbTreatments.setFont(UITheme.FONT_BODY);
        cbTreatments.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        cbTreatments.setPreferredSize(new Dimension(Integer.MAX_VALUE, 38));
        cbTreatments.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Load treatments from API
        try {
            List<model.Treatment> allTrts = apiClient.getTreatments();
            for (model.Treatment t : allTrts) {
                cbTreatments.addItem(t);
                if (currentAppointment.getTreatment() != null && t.getId() == currentAppointment.getTreatment().getId()) {
                    cbTreatments.setSelectedItem(t);
                }
            }
        } catch (Exception ex) {
            cbTreatments.addItem(currentAppointment.getTreatment());
        }

        JLabel lblStatusLabel = UITheme.createFieldLabel("Appointment Clinical Status:");
        lblStatusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JComboBox<String> cbStatus = new JComboBox<>(new String[]{"SCHEDULED", "IN PROGRESS", "COMPLETED", "CANCELLED"});
        cbStatus.setFont(UITheme.FONT_BODY);
        cbStatus.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        cbStatus.setPreferredSize(new Dimension(Integer.MAX_VALUE, 38));
        cbStatus.setAlignmentX(Component.LEFT_ALIGNMENT);
        cbStatus.setSelectedItem(currentAppointment.getStatus());

        JLabel lblNotes = UITheme.createFieldLabel("Doctor's Clinical Findings & Notes:");
        lblNotes.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea txtClinicalNotes = new JTextArea(currentAppointment.getNotes() != null ? currentAppointment.getNotes() : "");
        txtClinicalNotes.setFont(UITheme.FONT_BODY);
        txtClinicalNotes.setLineWrap(true);
        txtClinicalNotes.setWrapStyleWord(true);
        JScrollPane scrollNotes = new JScrollPane(txtClinicalNotes);
        scrollNotes.setPreferredSize(new Dimension(Integer.MAX_VALUE, 110));
        scrollNotes.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        scrollNotes.setBorder(new LineBorder(UITheme.COLOR_BORDER, 1, true));
        scrollNotes.setAlignmentX(Component.LEFT_ALIGNMENT);

        formCenter.add(lblTrt);
        formCenter.add(Box.createVerticalStrut(6));
        formCenter.add(cbTreatments);
        formCenter.add(Box.createVerticalStrut(14));
        formCenter.add(lblStatusLabel);
        formCenter.add(Box.createVerticalStrut(6));
        formCenter.add(cbStatus);
        formCenter.add(Box.createVerticalStrut(14));
        formCenter.add(lblNotes);
        formCenter.add(Box.createVerticalStrut(6));
        formCenter.add(scrollNotes);
        formCenter.add(Box.createVerticalGlue());

        // 3. Action Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 14));
        footer.setBackground(Color.WHITE);
        footer.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, UITheme.COLOR_BORDER),
                new EmptyBorder(0, 24, 0, 24)
        ));

        JButton btnCancel = UITheme.createSecondaryButton("Cancel");
        btnCancel.setPreferredSize(new Dimension(100, 40));
        btnCancel.addActionListener(e -> dialog.dispose());

        JButton btnSave = UITheme.createPrimaryButton("Save Diagnosis & Update Treatment");
        btnSave.setPreferredSize(new Dimension(270, 40));
        btnSave.addActionListener(e -> {
            model.Treatment selectedTrt = (model.Treatment) cbTreatments.getSelectedItem();
            if (selectedTrt == null) {
                JOptionPane.showMessageDialog(dialog, "Please select a treatment.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            btnSave.setEnabled(false);
            SwingWorker<Appointment, Void> saveWorker = new SwingWorker<>() {
                @Override
                protected Appointment doInBackground() throws Exception {
                    return apiClient.updateAppointmentTreatment(
                            currentAppointment.getId(),
                            selectedTrt.getId(),
                            txtClinicalNotes.getText().trim(),
                            (String) cbStatus.getSelectedItem()
                    );
                }

                @Override
                protected void done() {
                    btnSave.setEnabled(true);
                    try {
                        Appointment updated = get();
                        currentAppointment = updated;
                        displayAppointmentDetails(updated);
                        dialog.dispose();
                        JOptionPane.showMessageDialog(
                                DisplayAppointmentPanel.this,
                                "Clinical treatment updated to: " + updated.getTreatment().getType() +
                                        "\nStandard fee: LKR " + String.format("%.2f", updated.getTreatment().getCost()) +
                                        "\n\nThe billing system will automatically apply this updated treatment tariff.",
                                "Diagnosis Updated",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(dialog, "Failed to update treatment: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            saveWorker.execute();
        });

        footer.add(btnCancel);
        footer.add(btnSave);

        dialog.add(header, BorderLayout.NORTH);
        dialog.add(formCenter, BorderLayout.CENTER);
        dialog.add(footer, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private JPanel createDetailRow(String label, JComponent valueComponent) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_BODY);
        lbl.setForeground(UITheme.COLOR_TEXT_MUTED);
        lbl.setPreferredSize(new Dimension(175, 26));

        row.add(lbl, BorderLayout.WEST);
        row.add(valueComponent, BorderLayout.CENTER);
        return row;
    }

    public void searchAppointment(String apptNumber) {
        if (apptNumber == null || apptNumber.trim().isEmpty()) {
            lblStatus.setText("Please enter an appointment number to search.");
            lblStatus.setForeground(UITheme.COLOR_DANGER);
            return;
        }

        txtSearchApptNumber.setText(apptNumber.trim());
        btnSearch.setEnabled(false);
        lblStatus.setText("Searching for appointment: " + apptNumber + "...");
        lblStatus.setForeground(UITheme.COLOR_PRIMARY);

        SwingWorker<Appointment, Void> worker = new SwingWorker<>() {
            @Override
            protected Appointment doInBackground() throws Exception {
                return apiClient.getAppointmentByNumber(apptNumber.trim());
            }

            @Override
            protected void done() {
                btnSearch.setEnabled(true);
                try {
                    Appointment appt = get();
                    currentAppointment = appt;
                    displayAppointmentDetails(appt);
                    lblStatus.setText("Appointment details loaded successfully.");
                    lblStatus.setForeground(UITheme.COLOR_SUCCESS);
                } catch (Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    currentAppointment = null;
                    detailsContainer.setVisible(false);
                    lblStatus.setText(cause.getMessage());
                    lblStatus.setForeground(UITheme.COLOR_DANGER);
                }
            }
        };

        worker.execute();
    }

    private void displayAppointmentDetails(Appointment appt) {
        lblApptNumberValue.setText(appt.getAppointmentNumber());
        lblStatusBadge.setText(" " + appt.getStatus() + " ");

        lblDateTimeValue.setText(appt.getAppointmentDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd (EEE)")) + " at " + appt.getAppointmentTime());

        if (appt.getPatient() != null) {
            lblPatientNameValue.setText(appt.getPatient().getName());
            lblPatientContactValue.setText(appt.getPatient().getContactNumber());
            lblPatientAddressValue.setText(appt.getPatient().getAddress());
            lblPatientEmailValue.setText(appt.getPatient().getEmail() != null ? appt.getPatient().getEmail() : "N/A");
        }

        if (appt.getDentist() != null) {
            lblDentistNameValue.setText(appt.getDentist().getFullName());
            lblDentistSpecValue.setText(appt.getDentist().getSpecialization());
            lblDentistContactValue.setText(appt.getDentist().getContactNumber());
        }

        if (appt.getTreatment() != null) {
            lblTreatmentTypeValue.setText(appt.getTreatment().getType());
            lblTreatmentCostValue.setText(String.format("LKR %.2f", appt.getTreatment().getCost()));
        }

        txtNotesValue.setText(appt.getNotes() != null && !appt.getNotes().isEmpty() ? appt.getNotes() : "No notes recorded.");

        detailsContainer.setVisible(true);
        revalidate();
        repaint();
    }

    /**`n     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        headerPanel = new javax.swing.JPanel();
        lblTitle = new javax.swing.JLabel();
        lblDesc = new javax.swing.JLabel();
        searchCard = new javax.swing.JPanel();
        lblSearch = new javax.swing.JLabel();
        txtSearchApptNumber = new javax.swing.JTextField();
        btnSearch = new javax.swing.JButton();
        detailsCard = new javax.swing.JPanel();
        lblPatientInfo = new javax.swing.JLabel();
        lblDentistInfo = new javax.swing.JLabel();
        lblTreatmentInfo = new javax.swing.JLabel();
        lblDateInfo = new javax.swing.JLabel();
        lblStatusInfo = new javax.swing.JLabel();

        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblTitle.setText("View Appointment Details");

        lblDesc.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblDesc.setText("Search and retrieve full appointment files including patient history, assigned dentist, and clinical notes.");

        javax.swing.GroupLayout headerPanelLayout = new javax.swing.GroupLayout(headerPanel);
        headerPanel.setLayout(headerPanelLayout);
        headerPanelLayout.setHorizontalGroup(
            headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(headerPanelLayout.createSequentialGroup()
                .addComponent(lblTitle)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(headerPanelLayout.createSequentialGroup()
                .addComponent(lblDesc)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        headerPanelLayout.setVerticalGroup(
            headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(headerPanelLayout.createSequentialGroup()
                .addComponent(lblTitle)
                .addGap(4, 4, 4)
                .addComponent(lblDesc))
        );

        searchCard.setBackground(new java.awt.Color(255, 255, 255));
        searchCard.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(226, 232, 240), 1, true));

        lblSearch.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblSearch.setText("Appointment # :");

        txtSearchApptNumber.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N

        btnSearch.setText("Search Appointment");

        javax.swing.GroupLayout searchCardLayout = new javax.swing.GroupLayout(searchCard);
        searchCard.setLayout(searchCardLayout);
        searchCardLayout.setHorizontalGroup(
            searchCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(searchCardLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(lblSearch)
                .addGap(10, 10, 10)
                .addComponent(txtSearchApptNumber, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(btnSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(360, Short.MAX_VALUE))
        );
        searchCardLayout.setVerticalGroup(
            searchCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(searchCardLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(searchCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblSearch)
                    .addComponent(txtSearchApptNumber, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(14, 14, 14))
        );

        detailsCard.setBackground(new java.awt.Color(255, 255, 255));
        detailsCard.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Appointment Summary & Status", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 1, 14))); // NOI18N

        lblPatientInfo.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblPatientInfo.setText("Patient: --");

        lblDentistInfo.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblDentistInfo.setText("Dentist: --");

        lblTreatmentInfo.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblTreatmentInfo.setText("Treatment: --");

        lblDateInfo.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblDateInfo.setText("Date & Time: --");

        lblStatusInfo.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblStatusInfo.setText("Booking Status: --");

        javax.swing.GroupLayout detailsCardLayout = new javax.swing.GroupLayout(detailsCard);
        detailsCard.setLayout(detailsCardLayout);
        detailsCardLayout.setHorizontalGroup(
            detailsCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(detailsCardLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(detailsCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblPatientInfo)
                    .addComponent(lblDentistInfo)
                    .addComponent(lblTreatmentInfo)
                    .addComponent(lblDateInfo)
                    .addComponent(lblStatusInfo))
                .addContainerGap(500, Short.MAX_VALUE))
        );
        detailsCardLayout.setVerticalGroup(
            detailsCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(detailsCardLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(lblPatientInfo)
                .addGap(12, 12, 12)
                .addComponent(lblDentistInfo)
                .addGap(12, 12, 12)
                .addComponent(lblTreatmentInfo)
                .addGap(12, 12, 12)
                .addComponent(lblDateInfo)
                .addGap(12, 12, 12)
                .addComponent(lblStatusInfo)
                .addContainerGap(160, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(headerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(searchCard, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(detailsCard, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(25, 25, 25))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(headerPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(15, 15, 15)
                .addComponent(searchCard, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(15, 15, 15)
                .addComponent(detailsCard, javax.swing.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                .addGap(20, 20, 20))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnSearch;
    private javax.swing.JPanel detailsCard;
    private javax.swing.JPanel headerPanel;
    private javax.swing.JLabel lblDateInfo;
    private javax.swing.JLabel lblDentistInfo;
    private javax.swing.JLabel lblDesc;
    private javax.swing.JLabel lblPatientInfo;
    private javax.swing.JLabel lblSearch;
    private javax.swing.JLabel lblStatusInfo;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JLabel lblTreatmentInfo;
    private javax.swing.JPanel searchCard;
    private javax.swing.JTextField txtSearchApptNumber;
    // End of variables declaration//GEN-END:variables
}