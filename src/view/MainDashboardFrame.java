package view;

import client.ApiClient;
import client.SessionContext;
import model.DashboardMenuItem;
import model.User;
import view.panels.*;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main Application Dashboard Frame for Sunrise Dental Clinic.
 * Dynamically builds the navigation sidebar polymorphically from User.getMenuItems().
 *
 * @author Student
 */
public class MainDashboardFrame extends JFrame {
    private final ApiClient apiClient;

    private CardLayout cardLayout;
    private JPanel mainContentPanel;
    private JPanel sidebarPanel;

    private OverviewPanel overviewPanel;
    private RegisterAppointmentPanel registerAppointmentPanel;
    private DisplayAppointmentPanel displayAppointmentPanel;
    private BillingPanel billingPanel;
    private ReportsPanel reportsPanel;
    private HelpPanel helpPanel;
    private AdminManagementPanel adminManagementPanel;

    private final Map<String, JButton> sidebarButtons = new HashMap<>();
    private String activePanelKey = "DASHBOARD";

    public MainDashboardFrame(ApiClient apiClient) {
        this.apiClient = apiClient;
        initUI();
    }

    private void initUI() {
        setTitle("Sunrise Dental Clinic Management System — Staff Portal");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1440, 900);
        setMinimumSize(new Dimension(1024, 768));
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmAndExit();
            }
        });

        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(UITheme.COLOR_BG);

        // 1. Top Header Bar
        JPanel topHeader = createTopHeader();
        rootPanel.add(topHeader, BorderLayout.NORTH);

        // 2. Center Workspace (Sidebar on Left, Card Content on Right)
        BackgroundPanel workspace = new BackgroundPanel(new BorderLayout(), 0.08f);

        sidebarPanel = createSidebar();
        workspace.add(sidebarPanel, BorderLayout.WEST);

        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        mainContentPanel.setOpaque(false);

        // Instantiate Panels
        overviewPanel = new OverviewPanel(apiClient, this);
        registerAppointmentPanel = new RegisterAppointmentPanel(apiClient, this);
        displayAppointmentPanel = new DisplayAppointmentPanel(apiClient, this);
        billingPanel = new BillingPanel(apiClient, this);
        reportsPanel = new ReportsPanel(apiClient, this);
        helpPanel = new HelpPanel();
        adminManagementPanel = new AdminManagementPanel(apiClient, this);

        mainContentPanel.add(overviewPanel, "DASHBOARD");
        mainContentPanel.add(registerAppointmentPanel, "REGISTER_APPT");
        mainContentPanel.add(displayAppointmentPanel, "DISPLAY_APPT");
        mainContentPanel.add(billingPanel, "BILLING");
        mainContentPanel.add(reportsPanel, "REPORTS");
        mainContentPanel.add(adminManagementPanel, "ADMIN_MGMT");
        mainContentPanel.add(helpPanel, "HELP");

        workspace.add(mainContentPanel, BorderLayout.CENTER);
        rootPanel.add(workspace, BorderLayout.CENTER);

        setContentPane(rootPanel);
        switchView("DASHBOARD");
    }

    private JPanel createTopHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.COLOR_PRIMARY);
        header.setBorder(new EmptyBorder(14, 25, 14, 25));

        // Brand Title Left
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        brandPanel.setOpaque(false);

        JLabel lblBrand = new JLabel("SUNRISE DENTAL CLINIC");
        lblBrand.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblBrand.setForeground(Color.WHITE);

        brandPanel.add(lblBrand);

        // User Info & Status Right
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        userPanel.setOpaque(false);

        User currentUser = SessionContext.getInstance().getCurrentUser();
        String name = currentUser != null ? currentUser.getFullName() : "Clinic Staff";
        String role = currentUser != null ? currentUser.getRole() : "Staff";

        JLabel lblOnline = new JLabel("● Online (REST API)");
        lblOnline.setFont(UITheme.FONT_SMALL_BOLD);
        lblOnline.setForeground(new Color(52, 211, 153)); // Soft green

        JLabel lblUser = new JLabel(name);
        lblUser.setFont(UITheme.FONT_BODY_BOLD);
        lblUser.setForeground(Color.WHITE);

        JLabel lblRoleBadge = UITheme.createStatusBadge(role);

        JButton btnLogout = UITheme.createSecondaryButton("Sign Out");
        btnLogout.setFont(UITheme.FONT_SMALL_BOLD);
        btnLogout.addActionListener(e -> logoutAndReturnToLogin());

        userPanel.add(lblOnline);
        userPanel.add(lblUser);
        userPanel.add(lblRoleBadge);
        userPanel.add(btnLogout);

        header.add(brandPanel, BorderLayout.WEST);
        header.add(userPanel, BorderLayout.EAST);
        return header;
    }

    /**
     * Builds the left sidebar navigation dynamically and polymorphically
     * from the authenticated User subclass getMenuItems() method.
     */
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(UITheme.COLOR_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(260, 0));
        sidebar.setBorder(new EmptyBorder(20, 15, 20, 15));

        JLabel lblNav = new JLabel("MAIN NAVIGATION");
        lblNav.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblNav.setForeground(new Color(148, 163, 184)); // Slate 400
        lblNav.setBorder(new EmptyBorder(0, 10, 10, 0));
        lblNav.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(lblNav);

        User user = SessionContext.getInstance().getCurrentUser();
        if (user != null) {
            // Polymorphic method call — returns role-specific list of menu items
            List<DashboardMenuItem> menuItems = user.getMenuItems();
            for (DashboardMenuItem item : menuItems) {
                JButton btn = createSidebarNavButton(item);
                sidebarButtons.put(item.getId(), btn);
                sidebar.add(btn);
                sidebar.add(Box.createVerticalStrut(6));
            }
        }

        sidebar.add(Box.createVerticalGlue());

        // Footer version info
        JLabel lblVer = new JLabel("Sunrise Dental v1.0.0");
        lblVer.setFont(UITheme.FONT_SMALL);
        lblVer.setForeground(new Color(100, 116, 139));
        lblVer.setBorder(new EmptyBorder(0, 10, 0, 0));
        lblVer.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(lblVer);

        return sidebar;
    }

    private JButton createSidebarNavButton(DashboardMenuItem item) {
        JButton btn = new JButton(item.getTitle());
        btn.setFont(UITheme.FONT_BODY_BOLD);
        btn.setForeground(UITheme.COLOR_TEXT_LIGHT);
        btn.setBackground(UITheme.COLOR_SIDEBAR);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(12, 14, 12, 14));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (!item.getId().equals(activePanelKey)) {
                    btn.setBackground(UITheme.COLOR_SIDEBAR_HOVER);
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (!item.getId().equals(activePanelKey)) {
                    btn.setBackground(UITheme.COLOR_SIDEBAR);
                }
            }
        });

        btn.addActionListener(e -> {
            if ("EXIT".equals(item.getId())) {
                confirmAndExit();
            } else {
                switchView(item.getId());
            }
        });

        return btn;
    }

    public void switchView(String key) {
        this.activePanelKey = key;

        // Update button highlights
        for (Map.Entry<String, JButton> entry : sidebarButtons.entrySet()) {
            if (entry.getKey().equals(key)) {
                entry.getValue().setBackground(UITheme.COLOR_SIDEBAR_ACTIVE);
                entry.getValue().setForeground(Color.WHITE);
            } else {
                entry.getValue().setBackground(UITheme.COLOR_SIDEBAR);
                entry.getValue().setForeground(UITheme.COLOR_TEXT_LIGHT);
            }
        }

        // Refresh target panel if needed
        if ("DASHBOARD".equals(key)) {
            overviewPanel.loadDashboardData();
        } else if ("REGISTER_APPT".equals(key)) {
            registerAppointmentPanel.loadDropdownData();
        } else if ("REPORTS".equals(key) || "ANALYTICS".equals(key)) {
            reportsPanel.refreshReports();
        } else if ("ADMIN_MGMT".equals(key) && adminManagementPanel != null) {
            adminManagementPanel.loadCatalogAndStaffData();
        }

        cardLayout.show(mainContentPanel, key);
    }

    public void showDisplayAppointment(String apptNumber) {
        switchView("DISPLAY_APPT");
        displayAppointmentPanel.searchAppointment(apptNumber);
    }

    public void showBilling(String apptNumber) {
        switchView("BILLING");
        billingPanel.loadAppointmentForBilling(apptNumber);
    }

    private void logoutAndReturnToLogin() {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to sign out of the clinic portal?",
                "Confirm Sign Out",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            apiClient.logout();
            SwingUtilities.invokeLater(() -> {
                LoginFrame loginFrame = new LoginFrame(apiClient);
                loginFrame.setVisible(true);
                dispose();
            });
        }
    }

    private void confirmAndExit() {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Do you want to safely close the Sunrise Dental Clinic Management System?",
                "Confirm Exit",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            apiClient.logout();
            dispose();
            System.exit(0);
        }
    }
}
