package server;

import com.sun.net.httpserver.HttpServer;
import service.ClinicService;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Embedded REST HTTP Server for Sunrise Dental Clinic.
 * Uses lightweight built-in com.sun.net.httpserver.HttpServer with a dedicated
 * fixed thread pool (Executors.newFixedThreadPool) to process concurrent REST requests.
 *
 * @author Student
 */
public class SunriseServer {
    private static final Logger LOGGER = Logger.getLogger(SunriseServer.class.getName());
    public static final int DEFAULT_PORT = 8080;

    private final int port;
    private final ClinicService clinicService;
    private final SessionManager sessionManager;
    private HttpServer httpServer;
    private ExecutorService threadPool;

    public SunriseServer() {
        this(DEFAULT_PORT, new ClinicService(), new SessionManager());
    }

    public SunriseServer(int port) {
        this(port, new ClinicService(), new SessionManager());
    }

    public SunriseServer(int port, ClinicService clinicService, SessionManager sessionManager) {
        this.port = port;
        this.clinicService = clinicService;
        this.sessionManager = sessionManager;
    }

    /**
     * Starts the HTTP REST Server with thread pool executor.
     *
     * @throws IOException if server socket fails to bind.
     */
    public synchronized void start() throws IOException {
        if (httpServer != null) {
            LOGGER.warning("Server is already running.");
            return;
        }

        httpServer = HttpServer.create(new InetSocketAddress(port), 0);
        
        // Multi-threaded concurrent request handling via fixed thread pool
        threadPool = Executors.newFixedThreadPool(10, r -> {
            Thread t = new Thread(r, "HttpWorker-" + System.currentTimeMillis());
            t.setDaemon(true);
            return t;
        });
        httpServer.setExecutor(threadPool);

        // Bind root context to central REST handler
        RestHandler handler = new RestHandler(clinicService, sessionManager);
        httpServer.createContext("/", handler);

        httpServer.start();
        LOGGER.info("Sunrise Dental Clinic REST Server successfully started on port: " + port);
    }

    /**
     * Gracefully terminates the server and background thread pool.
     */
    public synchronized void stop() {
        if (httpServer != null) {
            LOGGER.info("Stopping Sunrise Dental Clinic REST Server...");
            httpServer.stop(1); // 1-second delay for in-flight requests
            httpServer = null;
        }
        if (threadPool != null) {
            threadPool.shutdown();
            threadPool = null;
        }
        sessionManager.clearAllSessions();
        LOGGER.info("Server stopped cleanly.");
    }

    public int getPort() {
        return port;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public ClinicService getClinicService() {
        return clinicService;
    }

    /**
     * Standalone server entry point.
     *
     * @param args command line arguments.
     */
    public static void main(String[] args) {
        try {
            SunriseServer server = new SunriseServer();
            server.start();
            System.out.println("Sunrise Dental Server running on http://localhost:" + DEFAULT_PORT);
            System.out.println("Press Ctrl+C to stop.");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to start server", e);
        }
    }
}
