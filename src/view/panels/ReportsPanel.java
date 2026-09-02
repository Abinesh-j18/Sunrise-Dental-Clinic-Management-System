package view.panels;

import client.ApiClient;
import client.SessionContext;
import model.DailyAppointmentReportItem;
import model.DentistProfile;
import model.DentistUser;
import model.RevenueByTreatmentReportItem;
import model.TopTreatmentReportItem;
import model.User;
import view.MainDashboardFrame;
import view.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

/**
 * Management Analytics and Operational Reports Screen.
 * Provides Daily Appointments, Revenue By Treatment, and Top Treatments reports.
 *
 * @author Student
 */
public class ReportsPanel extends JPanel {
    private final ApiClient apiClient;
    private final MainDashboardFrame parentFrame;

    private JTabbedPane tabbedPane;

    // Tab 1: Daily Appointments
    private JComboBox<DentistFilterItem> cmbDailyDentist;
    private JTextField txtDailyDate;
    private JTable tblDailyAppts;
    private DefaultTableModel modelDailyAppts;
    private JProgressBar pbDaily;
    private JLabel lblDailySummary;

    // Tab 2: Revenue By Treatment
    private JComboBox<String> cmbRevenueMonth;
    private JSpinner spRevenueYear;
    private JTable tblRevenue;
    private DefaultTableModel modelRevenue;
    private JProgressBar pbRevenue;
    private JLabel lblRevenueSummary;

    // Tab 3: Top Treatments
    private JComboBox<Integer> cmbTopLimit;
    private JTable tblTopTreatments;
    private DefaultTableModel modelTopTreatments;
    private JProgressBar pbTop;
    private JLabel lblTopSummary;

    public ReportsPanel(ApiClient apiClient) {
        this(apiClient, null);
    }

    public ReportsPanel(ApiClient apiClient, MainDashboardFrame parentFrame) {
        this.apiClient = apiClient;
        this.parentFrame = parentFrame;
        initComponents(); initUI();
        loadDentistsForFilter();
        loadDailyAppointmentsReport();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 20));
        setOpaque(false);
        setBorder(new EmptyBorder(25, 30, 25, 30));

        // Header Title
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Clinic Management Reports & Analytics");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.COLOR_TEXT_PRIMARY);

        JLabel lblDesc = new JLabel("Generate real-time operational schedules, treatment volume analysis, and revenue breakdowns.");
        lblDesc.setFont(UITheme.FONT_BODY);
        lblDesc.setForeground(UITheme.COLOR_TEXT_MUTED);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 4));
        titleBlock.setOpaque(false);
        titleBlock.add(lblTitle);
        titleBlock.add(lblDesc);

        headerPanel.add(titleBlock, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        // Tabbed Pane for Reports
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UITheme.FONT_SECTION);
        tabbedPane.setBackground(UITheme.COLOR_BG);

        tabbedPane.addTab("  Daily Appointments Schedule  ", createDailyAppointmentsTab());
        tabbedPane.addTab("  Revenue By Treatment Analysis  ", createRevenueByTreatmentTab());
        tabbedPane.addTab("  Top Requested Treatments  ", createTopTreatmentsTab());

        add(tabbedPane, BorderLayout.CENTER);
    }

    // --- Tab 1: Daily Appointments ---
    private JPanel createDailyAppointmentsTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(UITheme.COLOR_BG);
        panel.setBorder(new EmptyBorder(15, 10, 15, 10));

        JPanel filterCard = UITheme.createCardPanel();
        filterCard.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 0));

        cmbDailyDentist = new JComboBox<>();
        cmbDailyDentist.setPreferredSize(new Dimension(240, 38));
        cmbDailyDentist.addItem(new DentistFilterItem(null, "All Dentists"));

        txtDailyDate = UITheme.createTextField();
        txtDailyDate.setText(LocalDate.now().toString());
        txtDailyDate.setPreferredSize(new Dimension(140, 38));

        JButton btnFilter = UITheme.createPrimaryButton("Generate Report");
        btnFilter.addActionListener(e -> loadDailyAppointmentsReport());

        pbDaily = new JProgressBar();
        pbDaily.setIndeterminate(true);
        pbDaily.setVisible(false);
        pbDaily.setPreferredSize(new Dimension(100, 4));

        lblDailySummary = new JLabel("Showing appointments");
        lblDailySummary.setFont(UITheme.FONT_BODY);
        lblDailySummary.setForeground(UITheme.COLOR_TEXT_MUTED);

        filterCard.add(UITheme.createFieldLabel("Dentist:"));
        filterCard.add(cmbDailyDentist);
        filterCard.add(UITheme.createFieldLabel("Date (YYYY-MM-DD):"));
        filterCard.add(txtDailyDate);
        filterCard.add(btnFilter);
        filterCard.add(pbDaily);
        filterCard.add(lblDailySummary);

        // Table
        String[] cols = {"Appt #", "Patient Name", "Contact", "Dentist", "Treatment", "Date", "Time", "Status"};
        modelDailyAppts = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblDailyAppts = new JTable(modelDailyAppts);
        tblDailyAppts.setFont(UITheme.FONT_BODY);
        tblDailyAppts.setFillsViewportHeight(true);

        JScrollPane scroll = new JScrollPane(tblDailyAppts);
        scroll.setBorder(new LineBorder(UITheme.COLOR_BORDER, 1, true));

        panel.add(filterCard, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    // --- Tab 2: Revenue By Treatment ---
    private JPanel createRevenueByTreatmentTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(UITheme.COLOR_BG);
        panel.setBorder(new EmptyBorder(15, 10, 15, 10));

        JPanel filterCard = UITheme.createCardPanel();
        filterCard.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 0));

        String[] months = {"All Months", "1 - January", "2 - February", "3 - March", "4 - April", "5 - May", "6 - June",
                "7 - July", "8 - August", "9 - September", "10 - October", "11 - November", "12 - December"};
        cmbRevenueMonth = new JComboBox<>(months);
        cmbRevenueMonth.setPreferredSize(new Dimension(160, 38));

        spRevenueYear = new JSpinner(new SpinnerNumberModel(LocalDate.now().getYear(), 2020, 2030, 1));
        spRevenueYear.setPreferredSize(new Dimension(90, 38));
        spRevenueYear.setFont(UITheme.FONT_BODY);
        spRevenueYear.setEditor(new JSpinner.NumberEditor(spRevenueYear, "#"));

        JButton btnFilter = UITheme.createPrimaryButton("Calculate Revenue");
        btnFilter.addActionListener(e -> loadRevenueReport());

        pbRevenue = new JProgressBar();
        pbRevenue.setIndeterminate(true);
        pbRevenue.setVisible(false);
        pbRevenue.setPreferredSize(new Dimension(100, 4));

        lblRevenueSummary = new JLabel("Total Clinic Revenue: LKR 0.00");
        lblRevenueSummary.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblRevenueSummary.setForeground(UITheme.COLOR_PRIMARY);

        filterCard.add(UITheme.createFieldLabel("Month:"));
        filterCard.add(cmbRevenueMonth);
        filterCard.add(UITheme.createFieldLabel("Year:"));
        filterCard.add(spRevenueYear);
        filterCard.add(btnFilter);
        filterCard.add(pbRevenue);
        filterCard.add(Box.createHorizontalStrut(15));
        filterCard.add(lblRevenueSummary);

        // Table
        String[] cols = {"Treatment Type", "Unit Cost (LKR)", "Total Completed Bookings", "Total Revenue Generated (LKR)"};
        modelRevenue = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblRevenue = new JTable(modelRevenue);
        tblRevenue.setFont(UITheme.FONT_BODY);
        tblRevenue.setFillsViewportHeight(true);

        JScrollPane scroll = new JScrollPane(tblRevenue);
        scroll.setBorder(new LineBorder(UITheme.COLOR_BORDER, 1, true));

        panel.add(filterCard, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    // --- Tab 3: Top Treatments ---
    private JPanel createTopTreatmentsTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(UITheme.COLOR_BG);
        panel.setBorder(new EmptyBorder(15, 10, 15, 10));

        JPanel filterCard = UITheme.createCardPanel();
        filterCard.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 0));

        Integer[] limits = {3, 5, 10, 20};
        cmbTopLimit = new JComboBox<>(limits);
        cmbTopLimit.setSelectedItem(5);
        cmbTopLimit.setPreferredSize(new Dimension(90, 38));

        JButton btnFilter = UITheme.createPrimaryButton("Show Rankings");
        btnFilter.addActionListener(e -> loadTopTreatmentsReport());

        pbTop = new JProgressBar();
        pbTop.setIndeterminate(true);
        pbTop.setVisible(false);
        pbTop.setPreferredSize(new Dimension(100, 4));

        lblTopSummary = new JLabel("Ranking highest volume dental procedures");
        lblTopSummary.setFont(UITheme.FONT_BODY);
        lblTopSummary.setForeground(UITheme.COLOR_TEXT_MUTED);

        filterCard.add(UITheme.createFieldLabel("Top Count:"));
        filterCard.add(cmbTopLimit);
        filterCard.add(btnFilter);
        filterCard.add(pbTop);
        filterCard.add(lblTopSummary);

        // Table
        String[] cols = {"Rank", "Treatment Procedure", "Total Patient Bookings", "Unit Cost (LKR)", "Total Revenue (LKR)"};
        modelTopTreatments = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblTopTreatments = new JTable(modelTopTreatments);
        tblTopTreatments.setFont(UITheme.FONT_BODY);
        tblTopTreatments.setFillsViewportHeight(true);

        JScrollPane scroll = new JScrollPane(tblTopTreatments);
        scroll.setBorder(new LineBorder(UITheme.COLOR_BORDER, 1, true));

        panel.add(filterCard, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    public void refreshReports() {
        loadDentistsForFilter();
        loadDailyAppointmentsReport();
    }

    private void loadDentistsForFilter() {
        SwingWorker<List<DentistProfile>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<DentistProfile> doInBackground() throws Exception {
                return apiClient.getDentists();
            }

            @Override
            protected void done() {
                try {
                    List<DentistProfile> list = get();
                    DentistFilterItem currentSelection = (DentistFilterItem) cmbDailyDentist.getSelectedItem();
                    Integer curDentistId = currentSelection != null ? currentSelection.dentistId : null;

                    User curUser = SessionContext.getInstance().getCurrentUser();
                    Integer loggedInDentistId = null;
                    if (curUser instanceof DentistUser) {
                        loggedInDentistId = ((DentistUser) curUser).getDentistId();
                    }

                    cmbDailyDentist.removeAllItems();
                    cmbDailyDentist.addItem(new DentistFilterItem(null, "All Dentists"));
                    DentistFilterItem toSelect = null;

                    if (list != null) {
                        for (DentistProfile d : list) {
                            DentistFilterItem item = new DentistFilterItem(d.getId(), d.getFullName());
                            cmbDailyDentist.addItem(item);
                            if (curDentistId != null && curDentistId.equals(d.getId())) {
                                toSelect = item;
                            } else if (toSelect == null && loggedInDentistId != null && loggedInDentistId.equals(d.getId())) {
                                toSelect = item;
                            }
                        }
                    }

                    if (toSelect != null) {
                        cmbDailyDentist.setSelectedItem(toSelect);
                    }
                } catch (Exception ignored) {
                }
            }
        };
        worker.execute();
    }

    public void loadDailyAppointmentsReport() {
        DentistFilterItem dItem = (DentistFilterItem) cmbDailyDentist.getSelectedItem();
        Integer dentistId = dItem != null ? dItem.dentistId : null;

        String dateStr = txtDailyDate.getText().trim();
        LocalDate date = null;
        if (!dateStr.isEmpty()) {
            try {
                date = LocalDate.parse(dateStr);
            } catch (Exception ignored) {
            }
        }

        pbDaily.setVisible(true);

        LocalDate finalDate = date;
        SwingWorker<List<DailyAppointmentReportItem>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<DailyAppointmentReportItem> doInBackground() throws Exception {
                return apiClient.getDailyAppointmentsReport(dentistId, finalDate);
            }

            @Override
            protected void done() {
                pbDaily.setVisible(false);
                try {
                    List<DailyAppointmentReportItem> items = get();
                    modelDailyAppts.setRowCount(0);
                    if (items != null) {
                        for (DailyAppointmentReportItem it : items) {
                            modelDailyAppts.addRow(new Object[]{
                                    it.getAppointmentNumber(),
                                    it.getPatientName(),
                                    it.getPatientContact(),
                                    it.getDentistName(),
                                    it.getTreatmentType(),
                                    it.getAppointmentDate(),
                                    it.getAppointmentTime(),
                                    it.getStatus()
                            });
                        }
                        lblDailySummary.setText("Found " + items.size() + " appointment(s).");
                    }
                } catch (Exception e) {
                    lblDailySummary.setText("Failed to load report: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void loadRevenueReport() {
        int selectedMonthIdx = cmbRevenueMonth.getSelectedIndex();
        Integer month = selectedMonthIdx > 0 ? selectedMonthIdx : null;
        Integer year = (Integer) spRevenueYear.getValue();

        pbRevenue.setVisible(true);

        SwingWorker<List<RevenueByTreatmentReportItem>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<RevenueByTreatmentReportItem> doInBackground() throws Exception {
                return apiClient.getRevenueByTreatmentReport(month, year);
            }

            @Override
            protected void done() {
                pbRevenue.setVisible(false);
                try {
                    List<RevenueByTreatmentReportItem> items = get();
                    modelRevenue.setRowCount(0);
                    double grandTotal = 0.0;

                    if (items != null) {
                        for (RevenueByTreatmentReportItem it : items) {
                            grandTotal += it.getTotalRevenue();
                            modelRevenue.addRow(new Object[]{
                                    it.getTreatmentType(),
                                    String.format("%.2f", it.getUnitCost()),
                                    it.getAppointmentCount(),
                                    String.format("%.2f", it.getTotalRevenue())
                            });
                        }
                    }
                    lblRevenueSummary.setText(String.format("Total Clinic Revenue: LKR %.2f", grandTotal));
                } catch (Exception e) {
                    lblRevenueSummary.setText("Failed to load revenue data: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void loadTopTreatmentsReport() {
        Integer limit = (Integer) cmbTopLimit.getSelectedItem();
        int finalLimit = limit != null ? limit : 5;

        pbTop.setVisible(true);

        SwingWorker<List<TopTreatmentReportItem>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<TopTreatmentReportItem> doInBackground() throws Exception {
                return apiClient.getTopTreatmentsReport(finalLimit);
            }

            @Override
            protected void done() {
                pbTop.setVisible(false);
                try {
                    List<TopTreatmentReportItem> items = get();
                    modelTopTreatments.setRowCount(0);
                    if (items != null) {
                        int rank = 1;
                        for (TopTreatmentReportItem it : items) {
                            modelTopTreatments.addRow(new Object[]{
                                    "#" + (rank++),
                                    it.getTreatmentType(),
                                    it.getBookingsCount(),
                                    String.format("%.2f", it.getUnitCost()),
                                    String.format("%.2f", it.getTotalRevenueGenerated())
                            });
                        }
                        lblTopSummary.setText("Top " + items.size() + " procedure(s) listed.");
                    }
                } catch (Exception e) {
                    lblTopSummary.setText("Failed to load top treatments: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private static class DentistFilterItem {
        final Integer dentistId;
        final String name;

        DentistFilterItem(Integer id, String name) {
            this.dentistId = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
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