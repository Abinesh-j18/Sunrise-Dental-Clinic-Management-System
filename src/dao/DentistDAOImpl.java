package dao;

import model.DentistProfile;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JDBC implementation of DentistDAO.
 *
 * @author Student
 */
public class DentistDAOImpl implements DentistDAO {
    private static final Logger LOGGER = Logger.getLogger(DentistDAOImpl.class.getName());
    private final DatabaseConnectionManager connectionManager;

    public DentistDAOImpl() {
        this.connectionManager = DatabaseConnectionManager.getInstance();
    }

    public DentistDAOImpl(DatabaseConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public List<DentistProfile> findAll() {
        List<DentistProfile> list = new ArrayList<>();
        final String sql = "SELECT id, user_id, full_name, specialization, contact_number, email FROM dentists ORDER BY full_name ASC";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapDentistFromResultSet(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching all dentists", e);
        }
        return list;
    }

    @Override
    public DentistProfile findById(int id) {
        final String sql = "SELECT id, user_id, full_name, specialization, contact_number, email FROM dentists WHERE id = ?";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapDentistFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding dentist by id: " + id, e);
        }
        return null;
    }

    @Override
    public DentistProfile findByUserId(int userId) {
        final String sql = "SELECT id, user_id, full_name, specialization, contact_number, email FROM dentists WHERE user_id = ?";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapDentistFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding dentist by user id: " + userId, e);
        }
        return null;
    }

    @Override
    public boolean createDentist(DentistProfile dentist) {
        if (dentist == null) {
            return false;
        }
        final String sql = "INSERT INTO dentists (user_id, full_name, specialization, contact_number, email) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            if (dentist.getUserId() != null && dentist.getUserId() > 0) {
                ps.setInt(1, dentist.getUserId());
            } else {
                ps.setNull(1, java.sql.Types.INTEGER);
            }
            ps.setString(2, dentist.getFullName());
            ps.setString(3, dentist.getSpecialization());
            ps.setString(4, dentist.getContactNumber());
            ps.setString(5, dentist.getEmail());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet gk = ps.getGeneratedKeys()) {
                    if (gk.next()) {
                        dentist.setId(gk.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating dentist: " + dentist.getFullName(), e);
        }
        return false;
    }

    @Override
    public boolean deleteDentist(int id) {
        final String sql = "DELETE FROM dentists WHERE id = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting dentist id: " + id, e);
        }
        return false;
    }

    private DentistProfile mapDentistFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int userIdVal = rs.getInt("user_id");
        Integer userId = rs.wasNull() ? null : userIdVal;
        String fullName = rs.getString("full_name");
        String spec = rs.getString("specialization");
        String contact = rs.getString("contact_number");
        String email = rs.getString("email");
        return new DentistProfile(id, userId, fullName, spec, contact, email);
    }
}
