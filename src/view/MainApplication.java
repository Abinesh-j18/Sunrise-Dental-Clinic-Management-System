package view;

import client.ApiClient;
import server.SunriseServer;

import javax.swing.*;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Desktop Application Launcher for Sunrise Dental Clinic Management System.
 * Ensures the Tier 2 REST Service is running and launches the Tier 1 FlatLaf Swing UI.
 *
 * @author Student
 */
public class MainApplication {
    private static final Logger LOGGER = Logger.getLogger(MainApplication.class.getName());
    private static SunriseServer embeddedServer;

    public static void main(String[] args) {
        // 1. Setup FlatLaf look and feel
        UITheme.setupLookAndFeel();

        // 2. Ensure Tier 2 REST Service is running
        ensureServerRunning();

        // 3. Launch Swing UI on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            ApiClient apiClient = new ApiClient("http://localhost:8080");
            LoginFrame loginFrame = new LoginFrame(apiClient);
            loginFrame.setVisible(true);
        });
    }

    private static void ensureServerRunning() {
        boolean isRunning = checkServerConnectivity();
        if (!isRunning) {
            LOGGER.info("Starting local Tier 2 REST Server on port 8080...");
            try {
                embeddedServer = new SunriseServer(8080);
                embeddedServer.start();
                LOGGER.info("Embedded REST Server started successfully.");
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Failed to start local REST Server", e);
            }
        } else {
            LOGGER.info("Connected to existing Tier 2 REST Server on port 8080.");
        }
    }

    private static boolean checkServerConnectivity() {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofMillis(800))
                    .build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/"))
                    .timeout(Duration.ofMillis(800))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 401 || response.statusCode() == 200 || response.statusCode() == 404;
        } catch (Exception e) {
            return false;
        }
    }
}
