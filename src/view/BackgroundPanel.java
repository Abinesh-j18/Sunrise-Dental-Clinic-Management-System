package view;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Custom JPanel that paints the clinic dental backdrop image scaled to fit.
 *
 * @author Student
 */
public class BackgroundPanel extends JPanel {
    private static final Logger LOGGER = Logger.getLogger(BackgroundPanel.class.getName());
    private static BufferedImage backgroundImage;

    static {
        try {
            InputStream is = BackgroundPanel.class.getResourceAsStream("/view/resources/background.jpg");
            if (is != null) {
                backgroundImage = ImageIO.read(is);
            } else {
                LOGGER.warning("Background image resource not found: /view/resources/background.jpg");
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to load background image", e);
        }
    }

    private final float overlayAlpha;

    public BackgroundPanel() {
        this(new BorderLayout(), 0.0f);
    }

    public BackgroundPanel(LayoutManager layout) {
        this(layout, 0.0f);
    }

    public BackgroundPanel(LayoutManager layout, float overlayAlpha) {
        super(layout);
        this.overlayAlpha = overlayAlpha;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (backgroundImage != null) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            
            // Draw background image scaled to panel dimensions
            g2.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);

            // Optional soft overlay for maximum text/panel legibility
            if (overlayAlpha > 0.0f) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, overlayAlpha));
                g2.setColor(UITheme.COLOR_BG);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }

            g2.dispose();
        } else {
            g.setColor(UITheme.COLOR_BG);
            g.fillRect(0, 0, getWidth(), getHeight());
        }
        super.paintComponent(g);
    }
}
