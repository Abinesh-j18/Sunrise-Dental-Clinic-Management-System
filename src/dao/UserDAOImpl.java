package dao;

import model.Administrator;
import model.DentistUser;
import model.Receptionist;
import model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JDBC implementation of UserDAO.
 * Uses PreparedStatements exclusively and instantiates polymorphic User subclasses
 * (Administrator, Receptionist, DentistUser) based on the stored database role.
 *
 * @author Student
 */
public class UserDAOImpl implements UserDAO {
    private static final Logger LOGGER = Logger.getLogger(UserDAOImpl.class.getName());
    private final DatabaseConnectionManager connectionManager;

    public UserDAOImpl() {
        this.connectionManager = DatabaseConnectionManager.getInstance();
    }

    public UserDAOImpl(DatabaseConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public User findByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }

        final String sql = "SELECT u.id, u.username, u.password_hash, u.role, u.full_name, u.email, " +
                "d.id AS dentist_id, d.specialization " +
                "FROM users u " +
                "LEFT JOIN dentists d ON u.id = d.user_id " +
                "WHERE u.username = ?";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapUserFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding user by username: " + username, e);
        }
        return null;
    }

    @Override
    public User findById(int id) {
        final String sql = "SELECT u.id, u.username, u.password_hash, u.role, u.full_name, u.email, " +
                "d.id AS dentist_id, d.specialization " +
                "FROM users u " +
                "LEFT JOIN dentists d ON u.id = d.user_id " +
                "WHERE u.id = ?";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapUserFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding user by id: " + id, e);
        }
        return null;
    }

    @Override
    public List<User> findAll() {
        List<User> list = new ArrayList<>();
        final String sql = "SELECT u.id, u.username, u.password_hash, u.role, u.full_name, u.email, " +
                "d.id AS dentist_id, d.specialization " +
                "FROM users u " +
                "LEFT JOIN dentists d ON u.id = d.user_id " +
                "ORDER BY u.id ASC";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapUserFromResultSet(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching all users", e);
        }
        return list;
    }

    @Override
    public boolean createUser(User user) {
        if (user == null) {
            return false;
        }

        final String sql = "INSERT INTO users (username, password_hash, role, full_name, email) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getRole());
            ps.setString(4, user.getFullName());
            ps.setString(5, user.getEmail());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet gk = ps.getGeneratedKeys()) {
                    if (gk.next()) {
                        user.setId(gk.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating user: " + user.getUsername(), e);
        }
        return false;
    }

    @Override
    public boolean deleteUser(int id) {
        final String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting user id: " + id, e);
        }
        return false;
    }

    private User mapUserFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String username = rs.getString("username");
        String passwordHash = rs.getString("password_hash");
        String role = rs.getString("role");
        String fullName = rs.getString("full_name");
        String email = rs.getString("email");

        if ("Administrator".equalsIgnoreCase(role)) {
            return new Administrator(id, username, passwordHash, fullName, email);
        } else if ("Dentist".equalsIgnoreCase(role)) {
            int dentistId = rs.getInt("dentist_id");
            String spec = rs.getString("specialization");
            return new DentistUser(id, username, passwordHash, fullName, email, dentistId, spec != null ? spec : "General Dentistry");
        } else {
            // Default to Receptionist
            return new Receptionist(id, username, passwordHash, fullName, email);
        }
    }
}
