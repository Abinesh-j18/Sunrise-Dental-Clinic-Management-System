package view;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Enterprise Medical Design System and Theme for Sunrise Dental Clinic.
 * Provides high-contrast clinical color tokens, responsive typography,
 * rounded components, and polished table/form renderers.
 *
 * @author Student
 */
public final class UITheme {
    private static final Logger LOGGER = Logger.getLogger(UITheme.class.getName());

    // --- Enterprise Medical Color Palette ---
    public static final Color COLOR_PRIMARY = new Color(13, 92, 117);          // Deep Medical Teal #0D5C75
    public static final Color COLOR_PRIMARY_DARK = new Color(9, 64, 82);        // Dark Teal #094052
    public static final Color COLOR_PRIMARY_HOVER = new Color(15, 118, 148);    // Teal Hover #0F7694
    public static final Color COLOR_PRIMARY_LIGHT = new Color(224, 242, 254);   // Soft Sky #E0F2FE

    public static final Color COLOR_ACCENT = new Color(14, 165, 233);           // Bright Cyan #0EA5E9
    public static final Color COLOR_ACCENT_HOVER = new Color(2, 132, 199);      // Cyan 600

    public static final Color COLOR_BG = new Color(245, 247, 250);              // Slate 50/100 #F5F7FA
    public static final Color COLOR_SURFACE = Color.WHITE;
    public static final Color COLOR_SURFACE_ALT = new Color(248, 250, 252);     // Slate 50 #F8FAFC
    public static final Color COLOR_SIDEBAR = new Color(15, 23, 42);            // Slate 900 #0F172A
    public static final Color COLOR_SIDEBAR_HOVER = new Color(30, 41, 59);      // Slate 800 #1E293B
    public static final Color COLOR_SIDEBAR_ACTIVE = new Color(13, 92, 117);

    public static final Color COLOR_TEXT_PRIMARY = new Color(15, 23, 42);       // Slate 900 #0F172A
    public static final Color COLOR_TEXT_MUTED = new Color(100, 116, 139);      // Slate 500 #64748B
    public static final Color COLOR_TEXT_LIGHT = new Color(241, 245, 249);      // Slate 100 #F1F5F9

    public static final Color COLOR_BORDER = new Color(226, 232, 240);          // Slate 200 #E2E8F0
    public static final Color COLOR_BORDER_FOCUS = new Color(14, 165, 233);     // Accent border

    public static final Color COLOR_SUCCESS = new Color(16, 185, 129);          // Emerald 500 #10B981
    public static final Color COLOR_SUCCESS_LIGHT = new Color(209, 250, 229);   // Emerald 100 #D1FAE5
    public static final Color COLOR_WARNING = new Color(245, 158, 11);          // Amber 500 #F59E0B
    public static final Color COLOR_WARNING_LIGHT = new Color(254, 243, 199);   // Amber 100 #FEF3C7
    public static final Color COLOR_DANGER = new Color(239, 68, 68);            // Red 500 #EF4444
    public static final Color COLOR_DANGER_LIGHT = new Color(254, 226, 226);    // Red 100 #FEE2E2

    // --- Typography (Clean Segoe UI Hierarchy) ---
    public static final Font FONT_HERO = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_SECTION = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_SMALL_BOLD = new Font("Segoe UI", Font.BOLD, 11);
    public static final Font FONT_BUTTON = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_MONO = new Font("Consolas", Font.PLAIN, 12);

    private UITheme() {
    }

    /**
     * Initializes FlatLaf Look and Feel with enterprise medical defaults.
     */
    public static void setupLookAndFeel() {
        try {
            System.setProperty("flatlaf.useNativeLibrary", "false");
            FlatLightLaf.setup();
            UIManager.put("Button.arc", 8);
            UIManager.put("Component.arc", 8);
            UIManager.put("ProgressBar.arc", 8);
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("Table.alternateRowColor", new Color(248, 250, 252));
            UIManager.put("Table.selectionBackground", COLOR_PRIMARY_LIGHT);
            UIManager.put("Table.selectionForeground", COLOR_TEXT_PRIMARY);
            UIManager.put("Table.rowHeight", 38);
            UIManager.put("TableHeader.font", FONT_SECTION);
            UIManager.put("TableHeader.height", 38);
            UIManager.put("TableHeader.background", new Color(241, 245, 249));
            UIManager.put("TableHeader.foreground", COLOR_TEXT_PRIMARY);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.width", 10);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to initialize FlatLaf", e);
        }
    }

    /**
     * Retrieves the clinic application logo icon.
     */
    public static ImageIcon getAppLogo() {
        return getAppLogo(32, 22);
    }

    /**
     * Retrieves the clinic application logo icon resized to custom dimensions.
     */
    public static ImageIcon getAppLogo(int width, int height) {
        try {
            java.net.URL url = UITheme.class.getResource("/view/resources/sunrise-logo-small.png");
            if (url != null) {
                ImageIcon orig = new ImageIcon(url);
                Image scaled = orig.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(scaled);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    /**
     * Retrieves the clinic application logo image for window title bars and taskbars.
     */
    public static Image getAppLogoImage() {
        try {
            java.net.URL url = UITheme.class.getResource("/view/resources/sunrise-logo-small.png");
            if (url != null) {
                return new ImageIcon(url).getImage();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    /**
     * Creates a styled primary action button with modern gradient and subtle hover feedback.
     */
    public static JButton createPrimaryButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int w = getWidth();
                int h = getHeight();
                
                if (getModel().isPressed()) {
                    g2.setColor(COLOR_PRIMARY_DARK);
                } else if (getModel().isRollover()) {
                    g2.setPaint(new GradientPaint(0, 0, new Color(15, 118, 148), 0, h, new Color(13, 92, 117)));
                } else {
                    g2.setPaint(new GradientPaint(0, 0, new Color(14, 116, 144), 0, h, new Color(13, 92, 117)));
                }
                
                g2.fillRoundRect(0, 0, w, h, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };

        button.setFont(FONT_BUTTON);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorder(new EmptyBorder(10, 22, 10, 22));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    /**
     * Creates a styled secondary outline button.
     */
    public static JButton createSecondaryButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                if (getModel().isRollover()) {
                    g2.setColor(new Color(241, 245, 249));
                    g2.fillRoundRect(0, 0, w - 1, h - 1, 8, 8);
                } else {
                    g2.setColor(COLOR_SURFACE);
                    g2.fillRoundRect(0, 0, w - 1, h - 1, 8, 8);
                }
                g2.setColor(COLOR_BORDER);
                g2.drawRoundRect(0, 0, w - 1, h - 1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        button.setFont(FONT_BUTTON);
        button.setForeground(COLOR_TEXT_PRIMARY);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorder(new EmptyBorder(8, 18, 8, 18));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    /**
     * Creates a styled danger button.
     */
    public static JButton createDangerButton(String text) {
        JButton button = new JButton(text);
        button.setFont(FONT_BUTTON);
        button.setBackground(COLOR_DANGER);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(10, 18, 10, 18));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    /**
     * Creates a standardized card panel container with clean white background and crisp border.
     */
    public static JPanel createCardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(COLOR_SURFACE);
        panel.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(20, 24, 20, 24)
        ));
        return panel;
    }

    /**
     * Creates a styled metric KPI summary card.
     */
    public static JPanel createMetricCard(String title, String value, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(0, 8)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(accentColor);
                g2.fillRect(0, 0, getWidth(), 4); // Colored top accent bar
                g2.dispose();
            }
        };
        card.setBackground(COLOR_SURFACE);
        card.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(16, 20, 16, 20)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(FONT_SMALL_BOLD);
        lblTitle.setForeground(COLOR_TEXT_MUTED);

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblValue.setForeground(COLOR_TEXT_PRIMARY);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);
        return card;
    }

    /**
     * Creates a styled form field label (strictly left-aligned).
     */
    public static JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_BODY_BOLD);
        label.setForeground(COLOR_TEXT_PRIMARY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setHorizontalAlignment(SwingConstants.LEFT);
        return label;
    }

    /**
     * Creates a styled text field.
     */
    public static JTextField createTextField() {
        JTextField tf = new JTextField();
        tf.setFont(FONT_BODY);
        tf.setBackground(COLOR_SURFACE_ALT);
        tf.setPreferredSize(new Dimension(tf.getPreferredSize().width, 38));
        tf.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(6, 12, 6, 12)
        ));
        return tf;
    }

    /**
     * Creates a styled password field.
     */
    public static JPasswordField createPasswordField() {
        JPasswordField pf = new JPasswordField();
        pf.setFont(FONT_BODY);
        pf.setBackground(COLOR_SURFACE_ALT);
        pf.setPreferredSize(new Dimension(pf.getPreferredSize().width, 38));
        pf.setBorder(new CompoundBorder(
                new LineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(6, 12, 6, 12)
        ));
        return pf;
    }

    /**
     * Creates a status badge label with rounded appearance.
     */
    public static JLabel createStatusBadge(String status) {
        JLabel badge = new JLabel("  " + status + "  ") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setFont(FONT_SMALL_BOLD);
        badge.setOpaque(false);
        badge.setBorder(new EmptyBorder(3, 8, 3, 8));

        if ("SCHEDULED".equalsIgnoreCase(status) || "PAID".equalsIgnoreCase(status) || "PAID IN FULL".equalsIgnoreCase(status)) {
            badge.setBackground(COLOR_SUCCESS_LIGHT);
            badge.setForeground(new Color(6, 95, 70));
        } else if ("COMPLETED".equalsIgnoreCase(status) || "Administrator".equalsIgnoreCase(status)) {
            badge.setBackground(COLOR_PRIMARY_LIGHT);
            badge.setForeground(COLOR_PRIMARY);
        } else if ("CANCELLED".equalsIgnoreCase(status)) {
            badge.setBackground(COLOR_DANGER_LIGHT);
            badge.setForeground(new Color(153, 27, 27));
        } else if ("Dentist".equalsIgnoreCase(status)) {
            badge.setBackground(new Color(237, 233, 254));
            badge.setForeground(new Color(109, 40, 217));
        } else {
            badge.setBackground(COLOR_WARNING_LIGHT);
            badge.setForeground(new Color(146, 64, 14));
        }
        return badge;
    }
}
