package view.panels;

import client.ApiClient;
import client.SessionContext;
import model.Appointment;
import model.User;
import view.MainDashboardFrame;
import view.UITheme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Overview dashboard panel presenting clinic schedule, key metrics, and role summary.
 *
 * @author Student
 */
public class OverviewPanel extends JPanel {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");

    private final ApiClient apiClient;
    private final MainDashboardFrame parentFrame;

    private JLabel lblTotalAppts;
    private JLabel lblScheduledToday;
    private JLabel lblActiveDentists;
    private JTable tblTodayAppts;
    private DefaultTableModel tableModel;
    private JProgressBar progressBar;

    private JLabel lblTableTitle;

    public OverviewPanel(ApiClient apiClient) {
        this(apiClient, null);
    }

    public OverviewPanel(ApiClient apiClient, MainDashboardFrame parentFrame) {
        this.apiClient = apiClient;
        this.parentFrame = parentFrame;
        initComponents(); initUI();
        loadDashboardData();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 20));
        setOpaque(false);
        setBorder(new EmptyBorder(25, 30, 25, 30));

        // Top Greeting Banner
        JPanel bannerPanel = UITheme.createCardPanel();
        bannerPanel.setLayout(new BorderLayout());

        User user = SessionContext.getInstance().getCurrentUser();
        String greetingName = user != null ? user.getFullName() : "Staff Member";
        String role = user != null ? user.getRole() : "Staff";

        JPanel leftGreeting = new JPanel(new GridLayout(2, 1, 0, 4));
        leftGreeting.setOpaque(false);

        JLabel lblGreeting = new JLabel("Welcome back, " + greetingName);
        lblGreeting.setFont(UITheme.FONT_TITLE);
        lblGreeting.setForeground(UITheme.COLOR_TEXT_PRIMARY);

        JLabel lblDate = new JLabel("Today is " + LocalDate.now().format(DATE_FORMATTER) + "  •  Role: " + role);
        lblDate.setFont(UITheme.FONT_BODY);
        lblDate.setForeground(UITheme.COLOR_TEXT_MUTED);

        leftGreeting.add(lblGreeting);
        leftGreeting.add(lblDate);

        bannerPanel.add(leftGreeting, BorderLayout.WEST);

        // Metric Stat Cards Row
        JPanel statsRow = new JPanel(new GridLayout(1, 3, 20, 0));
        statsRow.setOpaque(false);

        lblTotalAppts = new JLabel("0", SwingConstants.CENTER);
        lblScheduledToday = new JLabel("0", SwingConstants.CENTER);
        lblActiveDentists = new JLabel("3", SwingConstants.CENTER);

        statsRow.add(createMetricCard("Total Appointments in System", lblTotalAppts, UITheme.COLOR_PRIMARY));
        statsRow.add(createMetricCard("Today's Appointments", lblScheduledToday, UITheme.COLOR_ACCENT));
        statsRow.add(createMetricCard("Active Clinic Dentists", lblActiveDentists, UITheme.COLOR_SUCCESS));

        JPanel topSection = new JPanel(new BorderLayout(0, 20));
        topSection.setOpaque(false);
        topSection.add(bannerPanel, BorderLayout.NORTH);
        topSection.add(statsRow, BorderLayout.SOUTH);

        add(topSection, BorderLayout.NORTH);

        // Center: Today's Appointments Table Card
        JPanel tableCard = UITheme.createCardPanel();
        tableCard.setLayout(new BorderLayout(0, 15));

        JPanel tableHeaderPanel = new JPanel(new BorderLayout());
        tableHeaderPanel.setOpaque(false);

        lblTableTitle = new JLabel("Appointments Schedule Queue");
        lblTableTitle.setFont(UITheme.FONT_SUBTITLE);
        lblTableTitle.setForeground(UITheme.COLOR_TEXT_PRIMARY);

        JButton btnRefresh = UITheme.createSecondaryButton("Refresh");
        btnRefresh.addActionListener(e -> loadDashboardData());

        JButton btnInspect = UITheme.createPrimaryButton("Examine Patient / Update Treatment");
        btnInspect.addActionListener(e -> {
            int selRow = tblTodayAppts.getSelectedRow();
            if (selRow >= 0 && parentFrame != null) {
                String apptNum = (String) tableModel.getValueAt(selRow, 0);
                parentFrame.showDisplayAppointment(apptNum);
            } else {
                JOptionPane.showMessageDialog(this, "Please select an appointment from the table to examine.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        progressBar.setPreferredSize(new Dimension(100, 4));

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightActions.setOpaque(false);
        rightActions.add(progressBar);
        rightActions.add(btnRefresh);
        rightActions.add(btnInspect);

        tableHeaderPanel.add(lblTableTitle, BorderLayout.WEST);
        tableHeaderPanel.add(rightActions, BorderLayout.EAST);

        String[] columns = {"Appt #", "Patient Name", "Contact", "Dentist", "Treatment", "Date & Time", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblTodayAppts = new JTable(tableModel);
        tblTodayAppts.setFont(UITheme.FONT_BODY);
        tblTodayAppts.setFillsViewportHeight(true);
        tblTodayAppts.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblTodayAppts.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2 && parentFrame != null) {
                    int row = tblTodayAppts.getSelectedRow();
                    if (row >= 0) {
                        String apptNum = (String) tableModel.getValueAt(row, 0);
                        parentFrame.showDisplayAppointment(apptNum);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(tblTodayAppts);
        scrollPane.setBorder(new LineBorder(UITheme.COLOR_BORDER, 1, true));

        tableCard.add(tableHeaderPanel, BorderLayout.NORTH);
        tableCard.add(scrollPane, BorderLayout.CENTER);

        add(tableCard, BorderLayout.CENTER);
    }

    private JPanel createMetricCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BorderLayout(0, 8));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(UITheme.FONT_SMALL_BOLD);
        lblTitle.setForeground(UITheme.COLOR_TEXT_MUTED);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(accentColor);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    public void loadDashboardData() {
        progressBar.setVisible(true);

        User currentUser = SessionContext.getInstance().getCurrentUser();
        String role = currentUser != null ? currentUser.getRole() : "Staff";
        Integer dentistId = SessionContext.getInstance().getCurrentDentistId();

        if (lblTableTitle != null) {
            if ("Dentist".equalsIgnoreCase(role)) {
                lblTableTitle.setText("Doctor's Clinical Queue - Assigned Patient Appointments (" + (currentUser != null ? currentUser.getFullName() : "Doctor") + ")");
            } else if ("Receptionist".equalsIgnoreCase(role)) {
                lblTableTitle.setText("Reception Desk - Master Clinic Appointments & Booking Queue");
            } else {
                lblTableTitle.setText("Clinic-Wide Appointments Master Stream (Executive Audit View)");
            }
        }

        SwingWorker<OverviewResult, Void> worker = new SwingWorker<>() {
            @Override
            protected OverviewResult doInBackground() throws Exception {
                List<Appointment> appts = apiClient.getAllAppointments();
                int dCount = 0;
                try {
                    List<model.DentistProfile> dentists = apiClient.getDentists();
                    dCount = dentists != null ? dentists.size() : 0;
                } catch (Exception ignored) {
                }
                return new OverviewResult(appts, dCount);
            }

            @Override
            protected void done() {
                progressBar.setVisible(false);
                try {
                    OverviewResult res = get();
                    List<Appointment> list = res.appointments;
                    tableModel.setRowCount(0);

                    LocalDate today = LocalDate.now();
                    int todayCount = 0;
                    int myTotalCount = 0;
                    double estimatedRevenue = 0.0;

                    if (list != null) {
                        for (Appointment a : list) {
                            boolean isForThisDentist = true;
                            if ("Dentist".equalsIgnoreCase(role)) {
                                if (dentistId != null && dentistId > 0 && a.getDentist() != null) {
                                    isForThisDentist = (a.getDentist().getId() == dentistId.intValue() ||
                                            (currentUser != null && a.getDentist().getFullName() != null &&
                                             a.getDentist().getFullName().trim().equalsIgnoreCase(currentUser.getFullName().trim())));
                                }
                            }

                            if (isForThisDentist) {
                                myTotalCount++;
                                if (today.equals(a.getAppointmentDate())) {
                                    todayCount++;
                                }
                                if (a.getTreatment() != null) {
                                    estimatedRevenue += a.getTreatment().getCost() + 1500.00; // Treatment + Consultation fee
                                }

                                tableModel.addRow(new Object[]{
                                        a.getAppointmentNumber(),
                                        a.getPatient() != null ? a.getPatient().getName() : "-",
                                        a.getPatient() != null ? a.getPatient().getContactNumber() : "-",
                                        a.getDentist() != null ? a.getDentist().getFullName() : "-",
                                        a.getTreatment() != null ? a.getTreatment().getType() : "-",
                                        a.getAppointmentDate() + " " + (a.getAppointmentTime() != null ? a.getAppointmentTime().toString() : ""),
                                        a.getStatus()
                                });
                            }
                        }
                    }

                    if ("Administrator".equalsIgnoreCase(role)) {
                        lblTotalAppts.setText(String.valueOf(list != null ? list.size() : 0));
                        lblScheduledToday.setText(String.format("LKR %.0f", estimatedRevenue));
                        lblActiveDentists.setText(res.dentistCount + " Dentists");
                    } else if ("Dentist".equalsIgnoreCase(role)) {
                        lblTotalAppts.setText(String.valueOf(myTotalCount));
                        lblScheduledToday.setText(String.valueOf(todayCount));
                        lblActiveDentists.setText("Active");
                    } else {
                        // Receptionist
                        lblTotalAppts.setText(String.valueOf(list != null ? list.size() : 0));
                        lblScheduledToday.setText(String.valueOf(todayCount));
                        lblActiveDentists.setText(res.dentistCount + " Dentists");
                    }
                } catch (Exception e) {
                    lblTotalAppts.setText("-");
                    lblScheduledToday.setText("-");
                }
            }
        };

        worker.execute();
    }

    private static class OverviewResult {
        final List<Appointment> appointments;
        final int dentistCount;

        OverviewResult(List<Appointment> appointments, int dentistCount) {
            this.appointments = appointments;
            this.dentistCount = dentistCount;
        }
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