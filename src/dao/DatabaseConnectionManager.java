package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Singleton database connection manager for MySQL / XAMPP.
 * 
 * PATTERN RATIONALE (Design Pattern Explanation):
 * The Singleton design pattern is deliberately chosen here for the following architectural reasons:
 * 1. Single Controlled Access Point: Guarantees that all DAO components access the database
 *    through a unified, centralized connection factory with standardized credentials and URL settings.
 * 2. Resource & Socket Management: Prevents redundant connection manager instantiations across
 *    different service threads, avoiding socket exhaustion and unnecessary driver re-registrations.
 * 3. Thread-Safe Global State: Utilizes double-checked locking / synchronized initialization
 *    to provide consistent, safe access in a multi-threaded server environment.
 *
 * @author Student
 */
public final class DatabaseConnectionManager {
    private static final Logger LOGGER = Logger.getLogger(DatabaseConnectionManager.class.getName());

    // Default configuration targeting standard XAMPP MySQL
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/sunrisedb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Colombo&characterEncoding=UTF-8";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "";

    private static volatile DatabaseConnectionManager instance;

    private String dbUrl;
    private String dbUser;
    private String dbPassword;

    /**
     * Private constructor to prevent direct instantiation from external classes.
     * Registers the MySQL JDBC driver.
     */
    private DatabaseConnectionManager() {
        this.dbUrl = DEFAULT_URL;
        this.dbUser = DEFAULT_USER;
        this.dbPassword = DEFAULT_PASSWORD;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            LOGGER.info("MySQL JDBC Driver registered successfully.");
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "MySQL JDBC Driver not found in classpath", e);
            throw new RuntimeException("MySQL JDBC Driver missing", e);
        }
    }

    /**
     * Returns the global Singleton instance of DatabaseConnectionManager.
     * Uses double-checked locking for high performance and thread safety.
     *
     * @return the unique DatabaseConnectionManager instance.
     */
    public static DatabaseConnectionManager getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnectionManager.class) {
                if (instance == null) {
                    instance = new DatabaseConnectionManager();
                }
            }
        }
        return instance;
    }

    /**
     * Configures custom database connection parameters (e.g. for test environments).
     *
     * @param url      JDBC connection URL.
     * @param user     Database user.
     * @param password Database password.
     */
    public synchronized void configure(String url, String user, String password) {
        this.dbUrl = url;
        this.dbUser = user;
        this.dbPassword = password;
        LOGGER.info("DatabaseConnectionManager reconfigured for: " + url);
    }

    /**
     * Obtains a new active connection to the database.
     * Callers are responsible for closing the connection (using try-with-resources).
     *
     * @return an open java.sql.Connection.
     * @throws SQLException if a database access error occurs.
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }

    /**
     * Tests the database connectivity.
     *
     * @return true if connection can be established, false otherwise.
     */
    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Database connectivity test failed: " + e.getMessage());
            return false;
        }
    }
}
