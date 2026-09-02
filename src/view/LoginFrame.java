package view;

import client.ApiClient;
import client.ApiException;
import client.SessionContext;
import model.User;
import server.dto.LoginResponse;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;

/**
 * Login window for staff authentication.
 * Allows receptionists, dentists, and administrators to sign in.
 *
 * @author Student
 */
public class LoginFrame extends JFrame {
    private final ApiClient apiClient;

    
    
    private JButton btnTogglePassword;
    
    private JPanel errorBanner;
    private JLabel lblErrorText;
    
    private boolean isPasswordVisible = false;

    public LoginFrame() {
        this(new ApiClient());
    }

    public LoginFrame(ApiClient apiClient) {
        this.apiClient = apiClient;
        initComponents(); initUI();
    }

    private void initUI() {
        setTitle("Sunrise Dental Clinic — Staff Portal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(920, 600);
        setMinimumSize(new Dimension(860, 560));
        setLocationRelativeTo(null);
        setResizable(false);

        // Full Background Panel
        BackgroundPanel bgPanel = new BackgroundPanel(new GridBagLayout(), 0.0f);

        // Centered Login Card Panel
        JPanel cardPanel = new JPanel();
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        cardPanel.setBackground(Color.WHITE);
        cardPanel.setPreferredSize(new Dimension(460, 420));
        cardPanel.setMaximumSize(new Dimension(460, 420));
        cardPanel.setBorder(new CompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(36, 40, 36, 40)
        ));

        // Brand Icon & Title
        JLabel lblClinicName = new JLabel("SUNRISE DENTAL CLINIC");
        lblClinicName.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblClinicName.setForeground(UITheme.COLOR_ACCENT_HOVER);
        lblClinicName.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblWelcome = new JLabel("Staff Portal Login");
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblWelcome.setForeground(UITheme.COLOR_TEXT_PRIMARY);
        lblWelcome.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSub = new JLabel("Sign in with your authorized clinic credentials.");
        lblSub.setFont(UITheme.FONT_BODY);
        lblSub.setForeground(UITheme.COLOR_TEXT_MUTED);
        lblSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Error Banner
        errorBanner = new JPanel(new BorderLayout(8, 0));
        errorBanner.setBackground(UITheme.COLOR_DANGER_LIGHT);
        errorBanner.setBorder(new CompoundBorder(
                new LineBorder(new Color(252, 165, 165), 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        errorBanner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        errorBanner.setAlignmentX(Component.LEFT_ALIGNMENT);
        errorBanner.setVisible(false);

        JLabel lblErrorIcon = new JLabel("!");
        lblErrorIcon.setFont(UITheme.FONT_BODY_BOLD);
        lblErrorIcon.setForeground(UITheme.COLOR_DANGER);

        lblErrorText = new JLabel("Invalid credentials.");
        lblErrorText.setFont(UITheme.FONT_SMALL_BOLD);
        lblErrorText.setForeground(new Color(153, 27, 27));

        errorBanner.add(lblErrorIcon, BorderLayout.WEST);
        errorBanner.add(lblErrorText, BorderLayout.CENTER);

        // Username Field
        JLabel lblUser = UITheme.createFieldLabel("Username / Staff ID");
        lblUser.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtUsername = UITheme.createTextField();
        txtUsername.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        txtUsername.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtUsername.setText("receptionist1");

        // Password Field with Show/Hide Toggle
        JLabel lblPass = UITheme.createFieldLabel("Password");
        lblPass.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel passwordContainer = new JPanel(new BorderLayout(6, 0));
        passwordContainer.setOpaque(false);
        passwordContainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        passwordContainer.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtPassword = UITheme.createPasswordField();
        txtPassword.setText("admin123");

        btnTogglePassword = new JButton("Show");
        btnTogglePassword.setFont(UITheme.FONT_SMALL_BOLD);
        btnTogglePassword.setForeground(UITheme.COLOR_TEXT_MUTED);
        btnTogglePassword.setFocusPainted(false);
        btnTogglePassword.setBackground(Color.WHITE);
        btnTogglePassword.setBorder(new CompoundBorder(new LineBorder(UITheme.COLOR_BORDER, 1, true), new EmptyBorder(4, 12, 4, 12)));
        btnTogglePassword.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnTogglePassword.addActionListener(e -> togglePasswordVisibility());

        passwordContainer.add(txtPassword, BorderLayout.CENTER);
        passwordContainer.add(btnTogglePassword, BorderLayout.EAST);

        // Progress Bar
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 4));
        progressBar.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Sign In Action Button
        btnLogin = UITheme.createPrimaryButton("Sign In to Portal");
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btnLogin.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnLogin.addActionListener(e -> executeLogin());

        // Enter key listener
        KeyAdapter enterListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    executeLogin();
                }
            }
        };
        txtUsername.addKeyListener(enterListener);
        txtPassword.addKeyListener(enterListener);

        cardPanel.add(lblClinicName);
        cardPanel.add(Box.createVerticalStrut(2));
        cardPanel.add(lblWelcome);
        cardPanel.add(Box.createVerticalStrut(4));
        cardPanel.add(lblSub);
        cardPanel.add(Box.createVerticalStrut(16));
        cardPanel.add(errorBanner);
        cardPanel.add(Box.createVerticalStrut(8));
        cardPanel.add(lblUser);
        cardPanel.add(Box.createVerticalStrut(4));
        cardPanel.add(txtUsername);
        cardPanel.add(Box.createVerticalStrut(12));
        cardPanel.add(lblPass);
        cardPanel.add(Box.createVerticalStrut(4));
        cardPanel.add(passwordContainer);
        cardPanel.add(Box.createVerticalStrut(14));
        cardPanel.add(progressBar);
        cardPanel.add(Box.createVerticalStrut(6));
        cardPanel.add(btnLogin);

        bgPanel.add(cardPanel, new GridBagConstraints());

        setContentPane(bgPanel);
    }

    private void togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible;
        if (isPasswordVisible) {
            txtPassword.setEchoChar((char) 0);
            btnTogglePassword.setText("Hide");
        } else {
            txtPassword.setEchoChar('•');
            btnTogglePassword.setText("Show");
        }
    }

    private void executeLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password.");
            return;
        }

        errorBanner.setVisible(false);
        btnLogin.setEnabled(false);
        progressBar.setVisible(true);

        SwingWorker<LoginResponse, Void> worker = new SwingWorker<>() {
            @Override
            protected LoginResponse doInBackground() throws Exception {
                return apiClient.login(username, password);
            }

            @Override
            protected void done() {
                btnLogin.setEnabled(true);
                progressBar.setVisible(false);
                try {
                    LoginResponse response = get();
                    if (response != null && response.getToken() != null) {
                        User user;
                        if ("Administrator".equalsIgnoreCase(response.getRole())) {
                            user = new model.Administrator(response.getUserId(), response.getUsername(), "", response.getFullName(), response.getEmail());
                        } else if ("Dentist".equalsIgnoreCase(response.getRole())) {
                            int dId = response.getDentistId() != null ? response.getDentistId() : (response.getUserId() == 3 ? 1 : (response.getUserId() == 4 ? 2 : 3));
                            user = new model.DentistUser(response.getUserId(), response.getUsername(), "", response.getFullName(), response.getEmail(),
                                    dId, "Dentist");
                            response.setDentistId(dId);
                        } else {
                            user = new model.Receptionist(response.getUserId(), response.getUsername(), "", response.getFullName(), response.getEmail());
                        }

                        SessionContext.getInstance().setSession(response.getToken(), user, response.getDentistId());

                        SwingUtilities.invokeLater(() -> {
                            MainDashboardFrame mainFrame = new MainDashboardFrame(apiClient);
                            mainFrame.setVisible(true);
                            dispose();
                        });
                    }
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    if (cause instanceof ApiException) {
                        showError(cause.getMessage());
                    } else if (cause instanceof IOException || cause instanceof InterruptedException) {
                        showError("Cannot connect to server at http://localhost:8080. Is the server running?");
                    } else {
                        showError("Authentication failed: " + cause.getMessage());
                    }
                }
            }
        };

        worker.execute();
    }

    private void showError(String message) {
        lblErrorText.setText(message);
        errorBanner.setVisible(true);
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

        cardPanel = new javax.swing.JPanel();
        lblClinicBrand = new javax.swing.JLabel();
        lblWelcome = new javax.swing.JLabel();
        lblSub = new javax.swing.JLabel();
        lblUsername = new javax.swing.JLabel();
        txtUsername = new javax.swing.JTextField();
        lblPassword = new javax.swing.JLabel();
        txtPassword = new javax.swing.JPasswordField();
        btnLogin = new javax.swing.JButton();
        progressBar = new javax.swing.JProgressBar();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Sunrise Dental Clinic â€” Staff Portal");
        setResizable(false);

        cardPanel.setBackground(new java.awt.Color(255, 255, 255));

        lblClinicBrand.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblClinicBrand.setForeground(new java.awt.Color(13, 92, 117));
        lblClinicBrand.setText("SUNRISE DENTAL CLINIC");

        lblWelcome.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        lblWelcome.setText("Staff Portal Login");

        lblSub.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        lblSub.setForeground(new java.awt.Color(102, 102, 102));
        lblSub.setText("Sign in with your authorized clinic credentials.");

        lblUsername.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblUsername.setText("Username:");

        txtUsername.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N

        lblPassword.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        lblPassword.setText("Password:");

        txtPassword.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N

        btnLogin.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnLogin.setText("Sign In to System");

        progressBar.setIndeterminate(true);

        javax.swing.GroupLayout cardPanelLayout = new javax.swing.GroupLayout(cardPanel);
        cardPanel.setLayout(cardPanelLayout);
        cardPanelLayout.setHorizontalGroup(
            cardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardPanelLayout.createSequentialGroup()
                .addGap(36, 36, 36)
                .addGroup(cardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblClinicBrand)
                    .addComponent(lblWelcome)
                    .addComponent(lblSub)
                    .addComponent(lblUsername)
                    .addComponent(txtUsername, javax.swing.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                    .addComponent(lblPassword)
                    .addComponent(txtPassword, javax.swing.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                    .addComponent(btnLogin, javax.swing.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                    .addComponent(progressBar, javax.swing.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE))
                .addContainerGap(42, Short.MAX_VALUE))
        );
        cardPanelLayout.setVerticalGroup(
            cardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cardPanelLayout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addComponent(lblClinicBrand)
                .addGap(4, 4, 4)
                .addComponent(lblWelcome)
                .addGap(4, 4, 4)
                .addComponent(lblSub)
                .addGap(20, 20, 20)
                .addComponent(lblUsername)
                .addGap(6, 6, 6)
                .addComponent(txtUsername, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14)
                .addComponent(lblPassword)
                .addGap(6, 6, 6)
                .addComponent(txtPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20)
                .addComponent(btnLogin, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 8, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(30, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(220, 220, 220)
                .addComponent(cardPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(220, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(60, 60, 60)
                .addComponent(cardPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(60, Short.MAX_VALUE))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnLogin;
    private javax.swing.JPanel cardPanel;
    private javax.swing.JLabel lblClinicBrand;
    private javax.swing.JLabel lblPassword;
    private javax.swing.JLabel lblSub;
    private javax.swing.JLabel lblUsername;
    private javax.swing.JLabel lblWelcome;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JPasswordField txtPassword;
    private javax.swing.JTextField txtUsername;
    // End of variables declaration//GEN-END:variables
}