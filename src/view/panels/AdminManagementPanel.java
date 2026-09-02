package view.panels;

import client.ApiClient;
import model.Treatment;
import server.dto.RegisterStaffRequest;
import server.dto.StaffMemberDTO;
import view.MainDashboardFrame;
import view.UITheme;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Administrator Exclusive Management Panel.
 * Provides complete management of Clinic Staff (Receptionists & Dentists)
 * and Dental Treatment Procedures Catalog & Pricing Tariffs.
 *
 * @author Student
 */
public class AdminManagementPanel extends JPanel {
    private final ApiClient apiClient;
    private final MainDashboardFrame parentFrame;

    private JTable tblTreatments;
    private DefaultTableModel modelTreatments;
    private JTable tblStaff;
    private DefaultTableModel modelStaff;
    private JProgressBar progressBar;
    private JLabel lblStatus;

    public AdminManagementPanel(ApiClient apiClient) {
        this(apiClient, null);
    }

    public AdminManagementPanel(ApiClient apiClient, MainDashboardFrame parentFrame) {
        this.apiClient = apiClient;
        this.parentFrame = parentFrame;
        initComponents(); initUI();
        loadCatalogAndStaffData();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 20));
        setOpaque(false);
        setBorder(new EmptyBorder(25, 30, 25, 30));

        // Header Title
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Clinic Administration & Staff Management");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.COLOR_TEXT_PRIMARY);

        JLabel lblDesc = new JLabel("Executive control: Manage clinic staff (Receptionists & Dentists), treatment procedures, and pricing tariffs.");
        lblDesc.setFont(UITheme.FONT_BODY);
        lblDesc.setForeground(UITheme.COLOR_TEXT_MUTED);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 4));
        titleBlock.setOpaque(false);
        titleBlock.add(lblTitle);
        titleBlock.add(lblDesc);

        JButton btnRefresh = UITheme.createSecondaryButton("Refresh Data");
        btnRefresh.addActionListener(e -> loadCatalogAndStaffData());

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        progressBar.setPreferredSize(new Dimension(100, 4));

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightActions.setOpaque(false);
        rightActions.add(progressBar);
        rightActions.add(btnRefresh);

        headerPanel.add(titleBlock, BorderLayout.WEST);
        headerPanel.add(rightActions, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Center Content: Tabbed View for Staff Management and Treatment Catalog
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UITheme.FONT_BODY_BOLD);

        // --- TAB 1: Staff Management (Receptionists & Dentists) ---
        JPanel cardStaff = UITheme.createCardPanel();
        cardStaff.setLayout(new BorderLayout(0, 15));

        JPanel staffHeader = new JPanel(new BorderLayout(15, 0));
        staffHeader.setOpaque(false);
        JLabel lblStaffTitle = new JLabel("Clinic Staff Directory & Access Control (Receptionists & Dentists)");
        lblStaffTitle.setFont(UITheme.FONT_SUBTITLE);
        lblStaffTitle.setForeground(UITheme.COLOR_PRIMARY);

        JButton btnAddStaff = UITheme.createPrimaryButton("+ Add Staff Member");
        btnAddStaff.setPreferredSize(new Dimension(170, 36));
        btnAddStaff.addActionListener(e -> openAddStaffDialog());

        JButton btnDeleteStaff = UITheme.createSecondaryButton("Remove Staff");
        btnDeleteStaff.setPreferredSize(new Dimension(130, 36));
        btnDeleteStaff.addActionListener(e -> deleteSelectedStaff());

        JPanel staffBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        staffBtns.setOpaque(false);
        staffBtns.add(btnAddStaff);
        staffBtns.add(btnDeleteStaff);

        staffHeader.add(lblStaffTitle, BorderLayout.WEST);
        staffHeader.add(staffBtns, BorderLayout.EAST);

        String[] staffCols = {"ID", "Username", "Staff Full Name", "System Role", "Specialization / Desk", "Contact Number", "Email Address"};
        modelStaff = new DefaultTableModel(staffCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblStaff = new JTable(modelStaff);
        tblStaff.setFont(UITheme.FONT_BODY);
        tblStaff.setFillsViewportHeight(true);
        tblStaff.setRowHeight(28);
        tblStaff.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblStaff.getColumnModel().getColumn(0).setPreferredWidth(45);
        tblStaff.getColumnModel().getColumn(0).setMaxWidth(60);
        tblStaff.getColumnModel().getColumn(1).setPreferredWidth(120);
        tblStaff.getColumnModel().getColumn(2).setPreferredWidth(200);
        tblStaff.getColumnModel().getColumn(3).setPreferredWidth(120);
        tblStaff.getColumnModel().getColumn(4).setPreferredWidth(220);
        tblStaff.getColumnModel().getColumn(5).setPreferredWidth(130);
        tblStaff.getColumnModel().getColumn(6).setPreferredWidth(200);

        JScrollPane scrollStaff = new JScrollPane(tblStaff);
        scrollStaff.setBorder(new LineBorder(UITheme.COLOR_BORDER, 1, true));

        cardStaff.add(staffHeader, BorderLayout.NORTH);
        cardStaff.add(scrollStaff, BorderLayout.CENTER);

        // --- TAB 2: Treatments & Tariff Pricing ---
        JPanel cardTreatments = UITheme.createCardPanel();
        cardTreatments.setLayout(new BorderLayout(0, 15));

        JPanel trtHeader = new JPanel(new BorderLayout(15, 0));
        trtHeader.setOpaque(false);
        JLabel lblTrtTitle = new JLabel("Dental Treatment Procedures & Tariff Fee Catalog");
        lblTrtTitle.setFont(UITheme.FONT_SUBTITLE);
        lblTrtTitle.setForeground(UITheme.COLOR_PRIMARY);

        JButton btnAddTreatment = UITheme.createPrimaryButton("+ Add New Treatment");
        btnAddTreatment.setPreferredSize(new Dimension(180, 36));
        btnAddTreatment.addActionListener(e -> openAddTreatmentDialog());

        JButton btnDeleteTreatment = UITheme.createSecondaryButton("Delete Treatment");
        btnDeleteTreatment.setPreferredSize(new Dimension(140, 36));
        btnDeleteTreatment.addActionListener(e -> deleteSelectedTreatment());

        JPanel trtBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        trtBtns.setOpaque(false);
        trtBtns.add(btnAddTreatment);
        trtBtns.add(btnDeleteTreatment);

        trtHeader.add(lblTrtTitle, BorderLayout.WEST);
        trtHeader.add(trtBtns, BorderLayout.EAST);

        String[] trtCols = {"ID", "Treatment Procedure Name", "Standard Tariff Fee (LKR)", "Clinical Procedure Description"};
        modelTreatments = new DefaultTableModel(trtCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblTreatments = new JTable(modelTreatments);
        tblTreatments.setFont(UITheme.FONT_BODY);
        tblTreatments.setFillsViewportHeight(true);
        tblTreatments.setRowHeight(28);
        tblTreatments.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblTreatments.getColumnModel().getColumn(0).setPreferredWidth(45);
        tblTreatments.getColumnModel().getColumn(0).setMaxWidth(60);
        tblTreatments.getColumnModel().getColumn(1).setPreferredWidth(240);
        tblTreatments.getColumnModel().getColumn(2).setPreferredWidth(160);
        tblTreatments.getColumnModel().getColumn(3).setPreferredWidth(380);

        JScrollPane scrollTrt = new JScrollPane(tblTreatments);
        scrollTrt.setBorder(new LineBorder(UITheme.COLOR_BORDER, 1, true));

        cardTreatments.add(trtHeader, BorderLayout.NORTH);
        cardTreatments.add(scrollTrt, BorderLayout.CENTER);

        tabbedPane.addTab("Staff Management (Receptionists & Dentists)", cardStaff);
        tabbedPane.addTab("Dental Treatment Tariff Catalog", cardTreatments);

        add(tabbedPane, BorderLayout.CENTER);

        // Bottom status note
        lblStatus = new JLabel("All dental treatments, receptionists, and doctors synchronized with Tier 3 MySQL Database.");
        lblStatus.setFont(UITheme.FONT_SMALL);
        lblStatus.setForeground(UITheme.COLOR_TEXT_MUTED);
        lblStatus.setBorder(new EmptyBorder(5, 5, 0, 0));
        add(lblStatus, BorderLayout.SOUTH);
    }

    public void loadCatalogAndStaffData() {
        progressBar.setVisible(true);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            private List<Treatment> treatments;
            private List<StaffMemberDTO> staffList;

            @Override
            protected Void doInBackground() throws Exception {
                treatments = apiClient.getTreatments();
                staffList = apiClient.getStaffMembers();
                return null;
            }

            @Override
            protected void done() {
                progressBar.setVisible(false);
                try {
                    get();
                    modelTreatments.setRowCount(0);
                    if (treatments != null) {
                        for (Treatment t : treatments) {
                            modelTreatments.addRow(new Object[]{
                                    t.getId(),
                                    t.getType(),
                                    String.format("LKR %.2f", t.getCost()),
                                    t.getDescription() != null ? t.getDescription() : "-"
                            });
                        }
                    }

                    modelStaff.setRowCount(0);
                    if (staffList != null) {
                        for (StaffMemberDTO s : staffList) {
                            modelStaff.addRow(new Object[]{
                                    s.getId(),
                                    s.getUsername(),
                                    s.getFullName(),
                                    s.getRole(),
                                    s.getSpecialization() != null ? s.getSpecialization() : "-",
                                    s.getContactNumber() != null ? s.getContactNumber() : "-",
                                    s.getEmail()
                            });
                        }
                    }
                    lblStatus.setText("Loaded " + (treatments != null ? treatments.size() : 0) + " treatments and " + (staffList != null ? staffList.size() : 0) + " staff members.");
                } catch (Exception e) {
                    lblStatus.setText("Failed to load catalog: " + e.getMessage());
                }
            }
        };

        worker.execute();
    }

    private void openAddStaffDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Register Clinic Staff Member", true);
        dialog.setSize(640, 540);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        // 1. Header Banner
        JPanel header = new JPanel(new BorderLayout(10, 4));
        header.setBackground(Color.WHITE);
        header.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, UITheme.COLOR_BORDER),
                new EmptyBorder(16, 24, 16, 24)
        ));

        JLabel lblHeaderTitle = new JLabel("Register Clinic Staff Member");
        lblHeaderTitle.setFont(UITheme.FONT_SUBTITLE);
        lblHeaderTitle.setForeground(UITheme.COLOR_TEXT_PRIMARY);

        JLabel lblHeaderSub = new JLabel("Create authorized system credentials and profile assignments for clinic personnel.");
        lblHeaderSub.setFont(UITheme.FONT_SMALL);
        lblHeaderSub.setForeground(UITheme.COLOR_TEXT_MUTED);

        JPanel headerText = new JPanel(new GridLayout(2, 1, 0, 2));
        headerText.setOpaque(false);
        headerText.add(lblHeaderTitle);
        headerText.add(lblHeaderSub);
        header.add(headerText, BorderLayout.CENTER);

        // 2. Center Form (2 Columns with strictly left-aligned components)
        JPanel formCenter = new JPanel(new GridLayout(1, 2, 24, 0));
        formCenter.setBackground(UITheme.COLOR_BG);
        formCenter.setBorder(new EmptyBorder(20, 26, 20, 26));

        // Left Column: Account Credentials & Role
        JPanel leftCol = new JPanel();
        leftCol.setLayout(new BoxLayout(leftCol, BoxLayout.Y_AXIS));
        leftCol.setOpaque(false);

        String[] roleOptions = {
                "Dentist (Dental Surgeon)",
                "Receptionist (Front Desk Officer)",
                "Administrator (Executive)"
        };
        JComboBox<String> cbRole = new JComboBox<>(roleOptions);
        cbRole.setFont(UITheme.FONT_BODY);
        cbRole.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        cbRole.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField txtUser = UITheme.createTextField();
        txtUser.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        txtUser.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPasswordField txtPass = UITheme.createPasswordField();
        txtPass.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        txtPass.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSpec = UITheme.createFieldLabel("Clinical Specialization (Required for Dentists):");
        JTextField txtSpec = UITheme.createTextField();
        txtSpec.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        txtSpec.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtSpec.setText("General Dentistry & Orthodontics");
        txtSpec.setEnabled(true);
        txtSpec.setToolTipText("e.g. Orthodontics, Periodontics, Oral Surgery, Endodontics");

        cbRole.addActionListener(e -> {
            String selected = (String) cbRole.getSelectedItem();
            if (selected != null && selected.startsWith("Dentist")) {
                lblSpec.setText("Clinical Specialization (Required for Dentists):");
                txtSpec.setText("");
                txtSpec.setEnabled(true);
                txtSpec.setToolTipText("e.g. Orthodontics, Periodontics, Oral Surgery, Endodontics");
            } else if (selected != null && selected.startsWith("Administrator")) {
                lblSpec.setText("Assigned Department / Designation:");
                txtSpec.setText("Executive Systems Management");
                txtSpec.setEnabled(false);
            } else {
                lblSpec.setText("Assigned Department / Designation:");
                txtSpec.setText("Front Desk & Patient Intake");
                txtSpec.setEnabled(false);
            }
        });

        leftCol.add(UITheme.createFieldLabel("Staff System Role:"));
        leftCol.add(Box.createVerticalStrut(5));
        leftCol.add(cbRole);
        leftCol.add(Box.createVerticalStrut(12));
        leftCol.add(UITheme.createFieldLabel("Username / Login ID:"));
        leftCol.add(Box.createVerticalStrut(5));
        leftCol.add(txtUser);
        leftCol.add(Box.createVerticalStrut(12));
        leftCol.add(UITheme.createFieldLabel("Initial Password:"));
        leftCol.add(Box.createVerticalStrut(5));
        leftCol.add(txtPass);
        leftCol.add(Box.createVerticalStrut(12));
        leftCol.add(lblSpec);
        leftCol.add(Box.createVerticalStrut(5));
        leftCol.add(txtSpec);

        // Right Column: Personal & Contact Info
        JPanel rightCol = new JPanel();
        rightCol.setLayout(new BoxLayout(rightCol, BoxLayout.Y_AXIS));
        rightCol.setOpaque(false);

        JTextField txtName = UITheme.createTextField();
        txtName.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        txtName.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField txtEmail = UITheme.createTextField();
        txtEmail.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        txtEmail.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField txtPhone = UITheme.createTextField();
        txtPhone.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        txtPhone.setAlignmentX(Component.LEFT_ALIGNMENT);

        rightCol.add(UITheme.createFieldLabel("Full Name (e.g. Dr. Kasun / Anne):"));
        rightCol.add(Box.createVerticalStrut(5));
        rightCol.add(txtName);
        rightCol.add(Box.createVerticalStrut(12));
        rightCol.add(UITheme.createFieldLabel("Email Address:"));
        rightCol.add(Box.createVerticalStrut(5));
        rightCol.add(txtEmail);
        rightCol.add(Box.createVerticalStrut(12));
        rightCol.add(UITheme.createFieldLabel("Contact Phone Number:"));
        rightCol.add(Box.createVerticalStrut(5));
        rightCol.add(txtPhone);

        formCenter.add(leftCol);
        formCenter.add(rightCol);

        // 3. Action Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 14));
        footer.setBackground(Color.WHITE);
        footer.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, UITheme.COLOR_BORDER),
                new EmptyBorder(0, 24, 0, 24)
        ));

        JButton btnCancel = UITheme.createSecondaryButton("Cancel");
        btnCancel.setPreferredSize(new Dimension(110, 40));
        btnCancel.addActionListener(e -> dialog.dispose());

        JButton btnSubmit = UITheme.createPrimaryButton("Create Staff Account");
        btnSubmit.setPreferredSize(new Dimension(210, 40));
        btnSubmit.addActionListener(e -> {
            String selectedRole = (String) cbRole.getSelectedItem();
            String role = "Receptionist";
            if (selectedRole != null) {
                if (selectedRole.startsWith("Dentist")) role = "Dentist";
                else if (selectedRole.startsWith("Administrator")) role = "Administrator";
            }

            String username = txtUser.getText().trim();
            String password = new String(txtPass.getPassword()).trim();
            String name = txtName.getText().trim();
            String email = txtEmail.getText().trim();
            String phone = txtPhone.getText().trim();
            String spec = txtSpec.getText().trim();

            if (username.isEmpty() || password.isEmpty() || name.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill in all required fields (Username, Password, Full Name, Email).", "Validation Required", JOptionPane.WARNING_MESSAGE);
                return;
            }

            btnSubmit.setEnabled(false);
            final String finalRole = role;
            SwingWorker<StaffMemberDTO, Void> w = new SwingWorker<>() {
                @Override
                protected StaffMemberDTO doInBackground() throws Exception {
                    RegisterStaffRequest req = new RegisterStaffRequest(username, password, finalRole, name, email, spec, phone);
                    return apiClient.registerStaffMember(req);
                }

                @Override
                protected void done() {
                    btnSubmit.setEnabled(true);
                    try {
                        StaffMemberDTO created = get();
                        dialog.dispose();
                        loadCatalogAndStaffData();
                        JOptionPane.showMessageDialog(AdminManagementPanel.this,
                                "Successfully created " + created.getRole() + " account for " + created.getFullName() + " (" + created.getUsername() + ").",
                                "Staff Account Created", JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception ex) {
                        Throwable c = ex.getCause() != null ? ex.getCause() : ex;
                        JOptionPane.showMessageDialog(dialog, "Failed to create staff: " + c.getMessage(), "Registration Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            w.execute();
        });

        footer.add(btnCancel);
        footer.add(btnSubmit);

        dialog.add(header, BorderLayout.NORTH);
        dialog.add(formCenter, BorderLayout.CENTER);
        dialog.add(footer, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void deleteSelectedStaff() {
        int row = tblStaff.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a staff member from the table to remove.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int userId = (Integer) modelStaff.getValueAt(row, 0);
        String name = (String) modelStaff.getValueAt(row, 2);
        String role = (String) modelStaff.getValueAt(row, 3);

        if (userId <= 1) {
            JOptionPane.showMessageDialog(this, "Primary Master Administrator account cannot be removed.", "Action Denied", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to remove " + role + " " + name + " (User ID: " + userId + ")?\nThis action cannot be undone.",
                "Confirm Staff Deletion",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            SwingWorker<Boolean, Void> w = new SwingWorker<>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return apiClient.deleteStaffMember(userId);
                }

                @Override
                protected void done() {
                    try {
                        get();
                        loadCatalogAndStaffData();
                        JOptionPane.showMessageDialog(AdminManagementPanel.this, "Staff member removed successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception ex) {
                        Throwable c = ex.getCause() != null ? ex.getCause() : ex;
                        JOptionPane.showMessageDialog(AdminManagementPanel.this, "Failed to remove staff: " + c.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            w.execute();
        }
    }

    private void openAddTreatmentDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add Dental Treatment Procedure", true);
        dialog.setSize(500, 440);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        // Header Banner
        JPanel header = new JPanel(new BorderLayout(10, 4));
        header.setBackground(Color.WHITE);
        header.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, UITheme.COLOR_BORDER),
                new EmptyBorder(16, 24, 16, 24)
        ));

        JLabel lblHeaderTitle = new JLabel("Register Dental Treatment Procedure");
        lblHeaderTitle.setFont(UITheme.FONT_SUBTITLE);
        lblHeaderTitle.setForeground(UITheme.COLOR_TEXT_PRIMARY);

        JLabel lblHeaderSub = new JLabel("Add a clinical dental procedure and standard tariff fee to the catalog.");
        lblHeaderSub.setFont(UITheme.FONT_SMALL);
        lblHeaderSub.setForeground(UITheme.COLOR_TEXT_MUTED);

        JPanel headerText = new JPanel(new GridLayout(2, 1, 0, 2));
        headerText.setOpaque(false);
        headerText.add(lblHeaderTitle);
        headerText.add(lblHeaderSub);
        header.add(headerText, BorderLayout.CENTER);

        // Center Form
        JPanel formCenter = new JPanel();
        formCenter.setLayout(new BoxLayout(formCenter, BoxLayout.Y_AXIS));
        formCenter.setBackground(UITheme.COLOR_BG);
        formCenter.setBorder(new EmptyBorder(18, 24, 18, 24));

        JTextField txtType = UITheme.createTextField();
        txtType.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        JTextField txtCost = UITheme.createTextField();
        txtCost.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        JTextArea txtDesc = new JTextArea(3, 20);
        txtDesc.setFont(UITheme.FONT_BODY);
        txtDesc.setLineWrap(true);
        txtDesc.setWrapStyleWord(true);
        JScrollPane scrollDesc = new JScrollPane(txtDesc);
        scrollDesc.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        scrollDesc.setBorder(new LineBorder(UITheme.COLOR_BORDER, 1, true));

        formCenter.add(UITheme.createFieldLabel("Treatment Procedure Name:"));
        formCenter.add(Box.createVerticalStrut(4));
        formCenter.add(txtType);
        formCenter.add(Box.createVerticalStrut(10));
        formCenter.add(UITheme.createFieldLabel("Standard Tariff Fee (LKR):"));
        formCenter.add(Box.createVerticalStrut(4));
        formCenter.add(txtCost);
        formCenter.add(Box.createVerticalStrut(10));
        formCenter.add(UITheme.createFieldLabel("Clinical Procedure Description:"));
        formCenter.add(Box.createVerticalStrut(4));
        formCenter.add(scrollDesc);

        // Action Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 14));
        footer.setBackground(Color.WHITE);
        footer.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, UITheme.COLOR_BORDER),
                new EmptyBorder(0, 24, 0, 24)
        ));

        JButton btnCancel = UITheme.createSecondaryButton("Cancel");
        btnCancel.setPreferredSize(new Dimension(100, 38));
        btnCancel.addActionListener(e -> dialog.dispose());

        JButton btnSubmit = UITheme.createPrimaryButton("+ Save Treatment to Catalog");
        btnSubmit.setPreferredSize(new Dimension(210, 38));
        btnSubmit.addActionListener(e -> {
            String type = txtType.getText().trim();
            String costStr = txtCost.getText().trim();
            String desc = txtDesc.getText().trim();

            if (type.isEmpty() || costStr.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter procedure name and standard cost.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                double cost = Double.parseDouble(costStr);
                btnSubmit.setEnabled(false);
                SwingWorker<Treatment, Void> w = new SwingWorker<>() {
                    @Override
                    protected Treatment doInBackground() throws Exception {
                        return apiClient.createTreatment(new Treatment(0, type, cost, desc));
                    }

                    @Override
                    protected void done() {
                        btnSubmit.setEnabled(true);
                        try {
                            get();
                            dialog.dispose();
                            loadCatalogAndStaffData();
                            JOptionPane.showMessageDialog(AdminManagementPanel.this, "Treatment added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                        } catch (Exception ex) {
                            Throwable c = ex.getCause() != null ? ex.getCause() : ex;
                            JOptionPane.showMessageDialog(dialog, "Failed to add treatment: " + c.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                };
                w.execute();
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(dialog, "Cost must be a valid numeric value.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            }
        });

        footer.add(btnCancel);
        footer.add(btnSubmit);

        dialog.add(header, BorderLayout.NORTH);
        dialog.add(formCenter, BorderLayout.CENTER);
        dialog.add(footer, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void deleteSelectedTreatment() {
        int row = tblTreatments.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a treatment from the table to remove.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int trtId = (Integer) modelTreatments.getValueAt(row, 0);
        String trtName = (String) modelTreatments.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to remove treatment '" + trtName + "' (ID: " + trtId + ") from the catalog?",
                "Confirm Treatment Deletion",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            SwingWorker<Boolean, Void> w = new SwingWorker<>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return apiClient.deleteTreatment(trtId);
                }

                @Override
                protected void done() {
                    try {
                        get();
                        loadCatalogAndStaffData();
                        JOptionPane.showMessageDialog(AdminManagementPanel.this, "Treatment removed successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception ex) {
                        Throwable c = ex.getCause() != null ? ex.getCause() : ex;
                        JOptionPane.showMessageDialog(AdminManagementPanel.this, "Failed to remove treatment: " + c.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            w.execute();
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