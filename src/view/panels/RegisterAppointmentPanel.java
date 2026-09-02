package view.panels;

import client.ApiClient;
import client.ApiException;
import model.Appointment;
import model.DentistProfile;
import model.Patient;
import model.Treatment;
import server.dto.BookAppointmentRequest;
import view.MainDashboardFrame;
import view.UITheme;
import util.ValidationHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Screen for registering new patients and scheduling dental appointments.
 * Performs client-side validation and executes booking via SwingWorker.
 *
 * @author Student
 */
public class RegisterAppointmentPanel extends JPanel {
    private final ApiClient apiClient;
    private final MainDashboardFrame parentFrame;

    // Patient Fields
    private JRadioButton rbNewPatient;
    private JRadioButton rbExistingPatient;
    private JComboBox<PatientComboItem> cmbExistingPatients;
    
    
    
    

    // Appointment Fields
    
    
     // Format YYYY-MM-DD
    
    

    // UI Feedback
    private JLabel lblStatus;
    private JProgressBar progressBar;
    

    public RegisterAppointmentPanel(ApiClient apiClient) {
        this(apiClient, null);
    }

    public RegisterAppointmentPanel(ApiClient apiClient, MainDashboardFrame parentFrame) {
        this.apiClient = apiClient;
        this.parentFrame = parentFrame;
        initComponents(); initUI();
        loadDropdownData();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 20));
        setOpaque(false);
        setBorder(new EmptyBorder(25, 30, 25, 30));

        // Title Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Register New Appointment");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.COLOR_TEXT_PRIMARY);

        JLabel lblDesc = new JLabel("Enter patient registration details and schedule a treatment slot with a clinician.");
        lblDesc.setFont(UITheme.FONT_BODY);
        lblDesc.setForeground(UITheme.COLOR_TEXT_MUTED);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 4));
        titleBlock.setOpaque(false);
        titleBlock.add(lblTitle);
        titleBlock.add(lblDesc);

        headerPanel.add(titleBlock, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        // Main Form Content (2 Columns: Left Patient Info, Right Appointment Info)
        JPanel formGrid = new JPanel(new GridLayout(1, 2, 25, 0));
        formGrid.setOpaque(false);

        // --- Column 1: Patient Information Card ---
        JPanel patientCard = UITheme.createCardPanel();
        patientCard.setLayout(new BoxLayout(patientCard, BoxLayout.Y_AXIS));

        JLabel lblSec1 = new JLabel("1. Patient Details");
        lblSec1.setFont(UITheme.FONT_SUBTITLE);
        lblSec1.setForeground(UITheme.COLOR_PRIMARY);
        lblSec1.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel modePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        modePanel.setOpaque(false);
        modePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        rbNewPatient = new JRadioButton("New Patient", true);
        rbExistingPatient = new JRadioButton("Existing Patient", false);
        ButtonGroup bgPatient = new ButtonGroup();
        bgPatient.add(rbNewPatient);
        bgPatient.add(rbExistingPatient);
        modePanel.add(rbNewPatient);
        modePanel.add(rbExistingPatient);

        cmbExistingPatients = new JComboBox<>();
        cmbExistingPatients.setFont(UITheme.FONT_BODY);
        cmbExistingPatients.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        cmbExistingPatients.setAlignmentX(Component.LEFT_ALIGNMENT);
        cmbExistingPatients.setVisible(false);

        txtPatientName = UITheme.createTextField();
        txtPatientName.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        txtPatientName.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtPatientContact = UITheme.createTextField();
        txtPatientContact.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        txtPatientContact.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtPatientAddress = UITheme.createTextField();
        txtPatientAddress.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        txtPatientAddress.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtPatientEmail = UITheme.createTextField();
        txtPatientEmail.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        txtPatientEmail.setAlignmentX(Component.LEFT_ALIGNMENT);

        rbNewPatient.addActionListener(e -> togglePatientMode(true));
        rbExistingPatient.addActionListener(e -> togglePatientMode(false));
        cmbExistingPatients.addActionListener(e -> onExistingPatientSelected());

        patientCard.add(lblSec1);
        patientCard.add(Box.createVerticalStrut(12));
        patientCard.add(modePanel);
        patientCard.add(Box.createVerticalStrut(10));
        patientCard.add(cmbExistingPatients);
        patientCard.add(Box.createVerticalStrut(10));

        patientCard.add(UITheme.createFieldLabel("Full Name *"));
        patientCard.add(Box.createVerticalStrut(4));
        patientCard.add(txtPatientName);
        patientCard.add(Box.createVerticalStrut(12));

        patientCard.add(UITheme.createFieldLabel("Contact Number (10 Digits) *"));
        patientCard.add(Box.createVerticalStrut(4));
        patientCard.add(txtPatientContact);
        patientCard.add(Box.createVerticalStrut(12));

        patientCard.add(UITheme.createFieldLabel("Residential Address *"));
        patientCard.add(Box.createVerticalStrut(4));
        patientCard.add(txtPatientAddress);
        patientCard.add(Box.createVerticalStrut(12));

        patientCard.add(UITheme.createFieldLabel("Email Address (Optional for e-receipts)"));
        patientCard.add(Box.createVerticalStrut(4));
        patientCard.add(txtPatientEmail);
        patientCard.add(Box.createVerticalGlue());

        // --- Column 2: Appointment Information Card ---
        JPanel apptCard = UITheme.createCardPanel();
        apptCard.setLayout(new BoxLayout(apptCard, BoxLayout.Y_AXIS));

        JLabel lblSec2 = new JLabel("2. Appointment Schedule & Treatment");
        lblSec2.setFont(UITheme.FONT_SUBTITLE);
        lblSec2.setForeground(UITheme.COLOR_PRIMARY);
        lblSec2.setAlignmentX(Component.LEFT_ALIGNMENT);

        cmbDentist = new JComboBox<>();
        cmbDentist.setFont(UITheme.FONT_BODY);
        cmbDentist.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        cmbDentist.setAlignmentX(Component.LEFT_ALIGNMENT);

        cmbTreatment = new JComboBox<>();
        cmbTreatment.setFont(UITheme.FONT_BODY);
        cmbTreatment.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        cmbTreatment.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Date Picker (Default tomorrow)
        txtAppointmentDate = UITheme.createTextField();
        txtAppointmentDate.setText(LocalDate.now().plusDays(1).toString());
        txtAppointmentDate.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        txtAppointmentDate.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Time Slot dropdown
        String[] slots = {
                "09:00:00", "09:30:00", "10:00:00", "10:30:00", "11:00:00", "11:30:00",
                "14:00:00", "14:30:00", "15:00:00", "15:30:00", "16:00:00", "16:30:00", "17:00:00"
        };
        cmbTimeSlot = new JComboBox<>(slots);
        cmbTimeSlot.setFont(UITheme.FONT_BODY);
        cmbTimeSlot.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        cmbTimeSlot.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtNotes = new JTextArea(3, 20);
        txtNotes.setFont(UITheme.FONT_BODY);
        txtNotes.setLineWrap(true);
        txtNotes.setWrapStyleWord(true);
        JScrollPane notesScroll = new JScrollPane(txtNotes);
        notesScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        notesScroll.setAlignmentX(Component.LEFT_ALIGNMENT);

        apptCard.add(lblSec2);
        apptCard.add(Box.createVerticalStrut(14));

        apptCard.add(UITheme.createFieldLabel("Assigned Dentist *"));
        apptCard.add(Box.createVerticalStrut(4));
        apptCard.add(cmbDentist);
        apptCard.add(Box.createVerticalStrut(12));

        apptCard.add(UITheme.createFieldLabel("Treatment Type *"));
        apptCard.add(Box.createVerticalStrut(4));
        apptCard.add(cmbTreatment);
        apptCard.add(Box.createVerticalStrut(12));

        apptCard.add(UITheme.createFieldLabel("Appointment Date (YYYY-MM-DD) *"));
        apptCard.add(Box.createVerticalStrut(4));
        apptCard.add(txtAppointmentDate);
        apptCard.add(Box.createVerticalStrut(12));

        apptCard.add(UITheme.createFieldLabel("Appointment Time Slot *"));
        apptCard.add(Box.createVerticalStrut(4));
        apptCard.add(cmbTimeSlot);
        apptCard.add(Box.createVerticalStrut(12));

        apptCard.add(UITheme.createFieldLabel("Clinical / Staff Notes (Optional)"));
        apptCard.add(Box.createVerticalStrut(4));
        apptCard.add(notesScroll);
        apptCard.add(Box.createVerticalGlue());

        formGrid.add(patientCard);
        formGrid.add(apptCard);

        add(formGrid, BorderLayout.CENTER);

        // Bottom Action & Status Bar
        JPanel bottomBar = UITheme.createCardPanel();
        bottomBar.setLayout(new BorderLayout());

        lblStatus = new JLabel("Fill required fields marked with * and click Confirm Booking.");
        lblStatus.setFont(UITheme.FONT_BODY);
        lblStatus.setForeground(UITheme.COLOR_TEXT_MUTED);

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        progressBar.setPreferredSize(new Dimension(140, 6));

        JPanel leftStatus = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftStatus.setOpaque(false);
        leftStatus.add(progressBar);
        leftStatus.add(lblStatus);

        btnBook = UITheme.createPrimaryButton("Confirm & Book Appointment");
        btnBook.setPreferredSize(new Dimension(240, 42));
        btnBook.addActionListener(e -> executeBooking());

        JButton btnReset = UITheme.createSecondaryButton("Clear Form");
        btnReset.addActionListener(e -> resetForm());

        JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightButtons.setOpaque(false);
        rightButtons.add(btnReset);
        rightButtons.add(btnBook);

        bottomBar.add(leftStatus, BorderLayout.WEST);
        bottomBar.add(rightButtons, BorderLayout.EAST);

        add(bottomBar, BorderLayout.SOUTH);
    }

    private void togglePatientMode(boolean isNew) {
        cmbExistingPatients.setVisible(!isNew);
        txtPatientName.setEditable(isNew);
        txtPatientContact.setEditable(isNew);
        txtPatientAddress.setEditable(isNew);
        txtPatientEmail.setEditable(isNew);

        if (isNew) {
            txtPatientName.setText("");
            txtPatientContact.setText("");
            txtPatientAddress.setText("");
            txtPatientEmail.setText("");
        } else {
            onExistingPatientSelected();
        }
    }

    private void onExistingPatientSelected() {
        PatientComboItem item = (PatientComboItem) cmbExistingPatients.getSelectedItem();
        if (item != null && item.patient != null) {
            Patient p = item.patient;
            txtPatientName.setText(p.getName());
            txtPatientContact.setText(p.getContactNumber());
            txtPatientAddress.setText(p.getAddress());
            txtPatientEmail.setText(p.getEmail() != null ? p.getEmail() : "");
        }
    }

    public void loadDropdownData() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            private List<DentistProfile> dentists;
            private List<Treatment> treatments;
            private List<Patient> patients;

            @Override
            protected Void doInBackground() throws Exception {
                dentists = apiClient.getDentists();
                treatments = apiClient.getTreatments();
                patients = apiClient.getPatients("");
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    cmbDentist.removeAllItems();
                    if (dentists != null) {
                        for (DentistProfile d : dentists) {
                            cmbDentist.addItem(new DentistComboItem(d));
                        }
                    }

                    cmbTreatment.removeAllItems();
                    if (treatments != null) {
                        for (Treatment t : treatments) {
                            cmbTreatment.addItem(new TreatmentComboItem(t));
                        }
                    }

                    cmbExistingPatients.removeAllItems();
                    if (patients != null) {
                        for (Patient p : patients) {
                            cmbExistingPatients.addItem(new PatientComboItem(p));
                        }
                    }
                } catch (Exception e) {
                    lblStatus.setText("Error loading clinic data: " + e.getMessage());
                    lblStatus.setForeground(UITheme.COLOR_DANGER);
                }
            }
        };
        worker.execute();
    }

    private void executeBooking() {
        // 1. Client-Side Validation
        String patientName = txtPatientName.getText().trim();
        String contact = txtPatientContact.getText().trim();
        String address = txtPatientAddress.getText().trim();
        String email = txtPatientEmail.getText().trim();
        String dateStr = txtAppointmentDate.getText().trim();
        String timeStr = (String) cmbTimeSlot.getSelectedItem();
        String notes = txtNotes.getText().trim();

        if (patientName.isEmpty()) {
            showError("Patient full name is required.");
            txtPatientName.requestFocus();
            return;
        }
        if (!ValidationHelper.isValidPhoneNumber(contact)) {
            showError("Invalid contact number. Must be a valid telephone number (e.g. 0771234567).");
            txtPatientContact.requestFocus();
            return;
        }
        if (address.isEmpty()) {
            showError("Patient residential address is required.");
            txtPatientAddress.requestFocus();
            return;
        }
        if (!ValidationHelper.isValidEmail(email)) {
            showError("Invalid email address format.");
            txtPatientEmail.requestFocus();
            return;
        }

        LocalDate date;
        try {
            date = LocalDate.parse(dateStr);
        } catch (Exception e) {
            showError("Invalid date format. Please use YYYY-MM-DD.");
            txtAppointmentDate.requestFocus();
            return;
        }

        LocalTime time = LocalTime.parse(timeStr);
        if (!ValidationHelper.isFutureDateTime(date, time)) {
            showError("Appointment date & time cannot be in the past.");
            return;
        }

        DentistComboItem dentistItem = (DentistComboItem) cmbDentist.getSelectedItem();
        TreatmentComboItem treatmentItem = (TreatmentComboItem) cmbTreatment.getSelectedItem();

        if (dentistItem == null || treatmentItem == null) {
            showError("Please select a dentist and treatment type.");
            return;
        }

        btnBook.setEnabled(false);
        progressBar.setVisible(true);
        lblStatus.setText("Validating clinician availability and processing booking...");
        lblStatus.setForeground(UITheme.COLOR_PRIMARY);

        // 2. Asynchronous Booking via SwingWorker
        SwingWorker<Appointment, Void> worker = new SwingWorker<>() {
            @Override
            protected Appointment doInBackground() throws Exception {
                int patientId;
                if (rbNewPatient.isSelected()) {
                    Patient newP = new Patient(0, patientName, address, contact, email);
                    Patient saved = apiClient.registerPatient(newP);
                    patientId = saved.getId();
                } else {
                    PatientComboItem pItem = (PatientComboItem) cmbExistingPatients.getSelectedItem();
                    patientId = pItem.patient.getId();
                }

                BookAppointmentRequest req = new BookAppointmentRequest(
                        patientId,
                        dentistItem.dentist.getId(),
                        treatmentItem.treatment.getId(),
                        date,
                        time,
                        notes
                );

                return apiClient.bookAppointment(req);
            }

            @Override
            protected void done() {
                btnBook.setEnabled(true);
                progressBar.setVisible(false);
                try {
                    Appointment booked = get();
                    lblStatus.setText("Appointment booked successfully: " + booked.getAppointmentNumber());
                    lblStatus.setForeground(UITheme.COLOR_SUCCESS);

                    // Show Success Dialog with Generated Appointment Number
                    showSuccessDialog(booked);
                    resetForm();
                    loadDropdownData();
                } catch (Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    String message = cause.getMessage();
                    lblStatus.setText(message);
                    lblStatus.setForeground(UITheme.COLOR_DANGER);
                    JOptionPane.showMessageDialog(RegisterAppointmentPanel.this,
                            message,
                            "Booking Validation Error",
                            JOptionPane.WARNING_MESSAGE);
                }
            }
        };

        worker.execute();
    }

    private void showSuccessDialog(Appointment appt) {
        String msg = String.format(
                "<html><body style='width: 360px; font-family: Segoe UI; padding: 10px;'>" +
                        "<h2 style='color: #0D5C75; margin-bottom: 4px;'>Appointment Confirmed!</h2>" +
                        "<p style='font-size: 14px; color: #10B981; font-weight: bold;'>Unique Appointment Number: %s</p>" +
                        "<hr style='border: 0; border-top: 1px solid #E2E8F0; margin: 10px 0;'/>" +
                        "<p><b>Patient:</b> %s</p>" +
                        "<p><b>Dentist:</b> %s (%s)</p>" +
                        "<p><b>Treatment:</b> %s</p>" +
                        "<p><b>Date & Time:</b> %s at %s</p>" +
                        "<p style='color: #64748B; font-size: 11px; margin-top: 8px;'>Confirmation email queued for patient.</p>" +
                        "</body></html>",
                appt.getAppointmentNumber(),
                appt.getPatient().getName(),
                appt.getDentist().getFullName(),
                appt.getDentist().getSpecialization(),
                appt.getTreatment().getType(),
                appt.getAppointmentDate(),
                appt.getAppointmentTime()
        );

        Object[] options = {"Done", "Display Details", "Generate Bill"};
        int choice = JOptionPane.showOptionDialog(
                this,
                msg,
                "Booking Successful",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == 1) { // Display Details
            parentFrame.showDisplayAppointment(appt.getAppointmentNumber());
        } else if (choice == 2) { // Generate Bill
            parentFrame.showBilling(appt.getAppointmentNumber());
        }
    }

    private void showError(String message) {
        lblStatus.setText(message);
        lblStatus.setForeground(UITheme.COLOR_DANGER);
    }

    private void resetForm() {
        txtPatientName.setText("");
        txtPatientContact.setText("");
        txtPatientAddress.setText("");
        txtPatientEmail.setText("");
        txtNotes.setText("");
        txtAppointmentDate.setText(LocalDate.now().plusDays(1).toString());
        rbNewPatient.setSelected(true);
        togglePatientMode(true);
        lblStatus.setText("Form reset.");
        lblStatus.setForeground(UITheme.COLOR_TEXT_MUTED);
    }

    // Helper combo wrappers
    private static class DentistComboItem {
        final DentistProfile dentist;
        DentistComboItem(DentistProfile d) { this.dentist = d; }
        @Override public String toString() { return dentist.getFullName() + " (" + dentist.getSpecialization() + ")"; }
    }

    private static class TreatmentComboItem {
        final Treatment treatment;
        TreatmentComboItem(Treatment t) { this.treatment = t; }
        @Override public String toString() { return String.format("%s — LKR %.2f", treatment.getType(), treatment.getCost()); }
    }

    private static class PatientComboItem {
        final Patient patient;
        PatientComboItem(Patient p) { this.patient = p; }
        @Override public String toString() { return patient.getName() + " (" + patient.getContactNumber() + ")"; }
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
        patientCard = new javax.swing.JPanel();
        lblPName = new javax.swing.JLabel();
        txtPatientName = new javax.swing.JTextField();
        lblPAddress = new javax.swing.JLabel();
        txtPatientAddress = new javax.swing.JTextField();
        lblPContact = new javax.swing.JLabel();
        txtPatientContact = new javax.swing.JTextField();
        lblPEmail = new javax.swing.JLabel();
        txtPatientEmail = new javax.swing.JTextField();
        apptCard = new javax.swing.JPanel();
        lblDentist = new javax.swing.JLabel();
        cmbDentist = new javax.swing.JComboBox<>();
        lblTreatment = new javax.swing.JLabel();
        cmbTreatment = new javax.swing.JComboBox<>();
        lblDate = new javax.swing.JLabel();
        txtAppointmentDate = new javax.swing.JTextField();
        lblTime = new javax.swing.JLabel();
        cmbTimeSlot = new javax.swing.JComboBox<>();
        lblNotes = new javax.swing.JLabel();
        scrollNotes = new javax.swing.JScrollPane();
        txtNotes = new javax.swing.JTextArea();
        actionPanel = new javax.swing.JPanel();
        btnClear = new javax.swing.JButton();
        btnBook = new javax.swing.JButton();

        lblTitle.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblTitle.setText("Book Dental Appointment");

        lblDesc.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblDesc.setText("Register patients and schedule appointments with real-time double-booking validation.");

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

        patientCard.setBackground(new java.awt.Color(255, 255, 255));
        patientCard.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "1. Patient Information", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 1, 14))); // NOI18N

        lblPName.setText("Full Name:");

        lblPAddress.setText("Residential Address:");

        lblPContact.setText("Contact Phone Number:");

        lblPEmail.setText("Email Address:");

        javax.swing.GroupLayout patientCardLayout = new javax.swing.GroupLayout(patientCard);
        patientCard.setLayout(patientCardLayout);
        patientCardLayout.setHorizontalGroup(
            patientCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(patientCardLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(patientCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtPatientName)
                    .addComponent(txtPatientAddress)
                    .addComponent(txtPatientContact)
                    .addComponent(txtPatientEmail)
                    .addGroup(patientCardLayout.createSequentialGroup()
                        .addGroup(patientCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblPName)
                            .addComponent(lblPAddress)
                            .addComponent(lblPContact)
                            .addComponent(lblPEmail))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addGap(16, 16, 16))
        );
        patientCardLayout.setVerticalGroup(
            patientCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(patientCardLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(lblPName)
                .addGap(4, 4, 4)
                .addComponent(txtPatientName, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(lblPAddress)
                .addGap(4, 4, 4)
                .addComponent(txtPatientAddress, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(lblPContact)
                .addGap(4, 4, 4)
                .addComponent(txtPatientContact, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(lblPEmail)
                .addGap(4, 4, 4)
                .addComponent(txtPatientEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(20, Short.MAX_VALUE))
        );

        apptCard.setBackground(new java.awt.Color(255, 255, 255));
        apptCard.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "2. Appointment & Treatment Details", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 1, 14))); // NOI18N

        lblDentist.setText("Assigned Dentist:");

        lblTreatment.setText("Dental Procedure:");

        lblDate.setText("Date (YYYY-MM-DD):");

        lblTime.setText("Time Slot:");

        cmbTimeSlot.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "09:00", "10:00", "11:00", "12:00", "14:00", "15:00", "16:00", "17:00" }));

        lblNotes.setText("Clinical Notes / Symptoms:");

        txtNotes.setColumns(20);
        txtNotes.setRows(3);
        scrollNotes.setViewportView(txtNotes);

        javax.swing.GroupLayout apptCardLayout = new javax.swing.GroupLayout(apptCard);
        apptCard.setLayout(apptCardLayout);
        apptCardLayout.setHorizontalGroup(
            apptCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(apptCardLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(apptCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cmbDentist, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cmbTreatment, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(apptCardLayout.createSequentialGroup()
                        .addGroup(apptCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtAppointmentDate, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblDate))
                        .addGap(16, 16, 16)
                        .addGroup(apptCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblTime)
                            .addComponent(cmbTimeSlot, 0, 216, Short.MAX_VALUE)))
                    .addComponent(scrollNotes)
                    .addGroup(apptCardLayout.createSequentialGroup()
                        .addGroup(apptCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblDentist)
                            .addComponent(lblTreatment)
                            .addComponent(lblNotes))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addGap(16, 16, 16))
        );
        apptCardLayout.setVerticalGroup(
            apptCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(apptCardLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(lblDentist)
                .addGap(4, 4, 4)
                .addComponent(cmbDentist, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(lblTreatment)
                .addGap(4, 4, 4)
                .addComponent(cmbTreatment, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addGroup(apptCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblDate)
                    .addComponent(lblTime))
                .addGap(4, 4, 4)
                .addGroup(apptCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtAppointmentDate, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbTimeSlot, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addComponent(lblNotes)
                .addGap(4, 4, 4)
                .addComponent(scrollNotes, javax.swing.GroupLayout.DEFAULT_SIZE, 60, Short.MAX_VALUE)
                .addGap(12, 12, 12))
        );

        actionPanel.setOpaque(false);

        btnClear.setText("Clear Form");

        btnBook.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnBook.setText("Confirm & Book Appointment");

        javax.swing.GroupLayout actionPanelLayout = new javax.swing.GroupLayout(actionPanel);
        actionPanel.setLayout(actionPanelLayout);
        actionPanelLayout.setHorizontalGroup(
            actionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, actionPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnClear, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(btnBook, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        actionPanelLayout.setVerticalGroup(
            actionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(actionPanelLayout.createSequentialGroup()
                .addGroup(actionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnBook, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnClear, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(headerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(patientCard, javax.swing.GroupLayout.PREFERRED_SIZE, 420, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(20, 20, 20)
                        .addComponent(apptCard, javax.swing.GroupLayout.DEFAULT_SIZE, 460, Short.MAX_VALUE))
                    .addComponent(actionPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(25, 25, 25))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(headerPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(15, 15, 15)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(patientCard, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(apptCard, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(15, 15, 15)
                .addComponent(actionPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(20, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel actionPanel;
    private javax.swing.JPanel apptCard;
    private javax.swing.JButton btnBook;
    private javax.swing.JButton btnClear;
    private javax.swing.JComboBox<Object> cmbDentist;
    private javax.swing.JComboBox<String> cmbTimeSlot;
    private javax.swing.JComboBox<Object> cmbTreatment;
    private javax.swing.JPanel headerPanel;
    private javax.swing.JLabel lblDate;
    private javax.swing.JLabel lblDentist;
    private javax.swing.JLabel lblDesc;
    private javax.swing.JLabel lblNotes;
    private javax.swing.JLabel lblPAddress;
    private javax.swing.JLabel lblPContact;
    private javax.swing.JLabel lblPEmail;
    private javax.swing.JLabel lblPName;
    private javax.swing.JLabel lblTime;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JLabel lblTreatment;
    private javax.swing.JPanel patientCard;
    private javax.swing.JScrollPane scrollNotes;
    private javax.swing.JTextField txtAppointmentDate;
    private javax.swing.JTextArea txtNotes;
    private javax.swing.JTextField txtPatientAddress;
    private javax.swing.JTextField txtPatientContact;
    private javax.swing.JTextField txtPatientEmail;
    private javax.swing.JTextField txtPatientName;
    // End of variables declaration//GEN-END:variables
}