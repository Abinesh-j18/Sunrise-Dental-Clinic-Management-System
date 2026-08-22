package dao;

import model.Treatment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JDBC implementation of TreatmentDAO.
 *
 * @author Student
 */
public class TreatmentDAOImpl implements TreatmentDAO {
    private static final Logger LOGGER = Logger.getLogger(TreatmentDAOImpl.class.getName());
    private final DatabaseConnectionManager connectionManager;

    public TreatmentDAOImpl() {
        this.connectionManager = DatabaseConnectionManager.getInstance();
    }

    public TreatmentDAOImpl(DatabaseConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public List<Treatment> findAll() {
        List<Treatment> list = new ArrayList<>();
        final String sql = "SELECT id, type, cost, description FROM treatments ORDER BY id ASC";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapTreatmentFromResultSet(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching all treatments", e);
        }
        return list;
    }

    @Override
    public Treatment findById(int id) {
        final String sql = "SELECT id, type, cost, description FROM treatments WHERE id = ?";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapTreatmentFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding treatment by id: " + id, e);
        }
        return null;
    }

    @Override
    public Treatment findByType(String type) {
        final String sql = "SELECT id, type, cost, description FROM treatments WHERE type = ?";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, type);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapTreatmentFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding treatment by type: " + type, e);
        }
        return null;
    }

    @Override
    public boolean createTreatment(Treatment treatment) {
        if (treatment == null) {
            return false;
        }
        final String sql = "INSERT INTO treatments (type, cost, description) VALUES (?, ?, ?)";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, treatment.getType());
            ps.setDouble(2, treatment.getCost());
            ps.setString(3, treatment.getDescription() != null ? treatment.getDescription() : "");

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet gk = ps.getGeneratedKeys()) {
                    if (gk.next()) {
                        treatment.setId(gk.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating treatment: " + treatment.getType(), e);
        }
        return false;
    }

    @Override
    public boolean deleteTreatment(int id) {
        final String sql = "DELETE FROM treatments WHERE id = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting treatment id: " + id, e);
        }
        return false;
    }

    private Treatment mapTreatmentFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String type = rs.getString("type");
        double cost = rs.getDouble("cost");
        String description = rs.getString("description");
        return new Treatment(id, type, cost, description);
    }
}
