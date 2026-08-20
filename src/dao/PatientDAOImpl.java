package dao;

import model.Patient;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JDBC implementation of PatientDAO.
 *
 * @author Student
 */
public class PatientDAOImpl implements PatientDAO {
    private static final Logger LOGGER = Logger.getLogger(PatientDAOImpl.class.getName());
    private final DatabaseConnectionManager connectionManager;

    public PatientDAOImpl() {
        this.connectionManager = DatabaseConnectionManager.getInstance();
    }

    public PatientDAOImpl(DatabaseConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public Patient create(Patient patient) {
        if (patient == null) {
            return null;
        }

        final String sql = "INSERT INTO patients (name, address, contact_number, email) VALUES (?, ?, ?, ?)";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, patient.getName());
            ps.setString(2, patient.getAddress());
            ps.setString(3, patient.getContactNumber());
            ps.setString(4, patient.getEmail());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet gk = ps.getGeneratedKeys()) {
                    if (gk.next()) {
                        patient.setId(gk.getInt(1));
                    }
                }
                return patient;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error inserting patient: " + patient.getName(), e);
        }
        return null;
    }

    @Override
    public Patient findById(int id) {
        final String sql = "SELECT id, name, address, contact_number, email, created_at FROM patients WHERE id = ?";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapPatientFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding patient by id: " + id, e);
        }
        return null;
    }

    @Override
    public List<Patient> searchByNameOrContact(String keyword) {
        List<Patient> list = new ArrayList<>();
        if (keyword == null || keyword.trim().isEmpty()) {
            return findAll();
        }

        final String sql = "SELECT id, name, address, contact_number, email, created_at FROM patients " +
                "WHERE name LIKE ? OR contact_number LIKE ? ORDER BY name ASC";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String pattern = "%" + keyword.trim() + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapPatientFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error searching patients with keyword: " + keyword, e);
        }
        return list;
    }

    @Override
    public List<Patient> findAll() {
        List<Patient> list = new ArrayList<>();
        final String sql = "SELECT id, name, address, contact_number, email, created_at FROM patients ORDER BY id DESC";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapPatientFromResultSet(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching all patients", e);
        }
        return list;
    }

    private Patient mapPatientFromResultSet(ResultSet rs) throws SQLException {
        Patient p = new Patient();
        p.setId(rs.getInt("id"));
        p.setName(rs.getString("name"));
        p.setAddress(rs.getString("address"));
        p.setContactNumber(rs.getString("contact_number"));
        p.setEmail(rs.getString("email"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            p.setCreatedAt(ts.toLocalDateTime());
        }
        return p;
    }
}
