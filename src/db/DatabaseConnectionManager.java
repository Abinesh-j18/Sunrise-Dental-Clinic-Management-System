package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Singleton database connection manager for MySQL / XAMPP.
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
     * Private constructor to prevent direct instantiation.
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
     * Configures custom database connection parameters.
     */
    public synchronized void configure(String url, String user, String password) {
        this.dbUrl = url;
        this.dbUser = user;
        this.dbPassword = password;
        LOGGER.info("DatabaseConnectionManager reconfigured for: " + url);
    }

    /**
     * Obtains a new active connection to the database.
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }

    /**
     * Tests the database connectivity.
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
