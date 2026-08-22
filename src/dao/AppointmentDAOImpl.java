package dao;

import model.Appointment;
import model.DentistProfile;
import model.Patient;
import model.Treatment;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JDBC implementation of AppointmentDAO.
 * Interacts with MySQL stored function CheckDentistAvailability and DB Trigger trg_before_insert_appointment.
 *
 * @author Student
 */
public class AppointmentDAOImpl implements AppointmentDAO {
    private static final Logger LOGGER = Logger.getLogger(AppointmentDAOImpl.class.getName());
    private final DatabaseConnectionManager connectionManager;

    public AppointmentDAOImpl() {
        this.connectionManager = DatabaseConnectionManager.getInstance();
    }

    public AppointmentDAOImpl(DatabaseConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public boolean isDentistAvailable(int dentistId, LocalDate date, LocalTime time) {
        final String sql = "SELECT CheckDentistAvailability(?, ?, ?) AS is_free";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, dentistId);
            ps.setDate(2, Date.valueOf(date));
            ps.setTime(3, Time.valueOf(time));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int isFree = rs.getInt("is_free");
                    return isFree == 1;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, String.format("Error checking dentist availability (Dentist: %d, Date: %s, Time: %s)",
                    dentistId, date, time), e);
        }
        return false;
    }

    @Override
    public Appointment create(Appointment appointment) {
        if (appointment == null) {
            return null;
        }

        // We do NOT insert appointment_number here; the MySQL BEFORE INSERT trigger creates it
        final String sql = "INSERT INTO appointments (patient_id, dentist_id, treatment_id, appointment_date, appointment_time, status, notes) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, appointment.getPatient().getId());
            ps.setInt(2, appointment.getDentist().getId());
            ps.setInt(3, appointment.getTreatment().getId());
            ps.setDate(4, Date.valueOf(appointment.getAppointmentDate()));
            ps.setTime(5, Time.valueOf(appointment.getAppointmentTime()));
            ps.setString(6, appointment.getStatus() != null ? appointment.getStatus() : "SCHEDULED");
            ps.setString(7, appointment.getNotes());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet gk = ps.getGeneratedKeys()) {
                    if (gk.next()) {
                        int generatedId = gk.getInt(1);
                        return findById(generatedId);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating appointment", e);
        }
        return null;
    }

    @Override
    public Appointment findByAppointmentNumber(String appointmentNumber) {
        if (appointmentNumber == null || appointmentNumber.trim().isEmpty()) {
            return null;
        }

        final String sql = "SELECT a.id, a.appointment_number, a.appointment_date, a.appointment_time, a.status, a.notes, a.created_at, " +
                "p.id AS patient_id, p.name AS patient_name, p.address AS patient_address, p.contact_number AS patient_contact, p.email AS patient_email, " +
                "d.id AS dentist_id, d.user_id AS dentist_user_id, d.full_name AS dentist_name, d.specialization AS dentist_spec, d.contact_number AS dentist_contact, d.email AS dentist_email, " +
                "t.id AS treatment_id, t.type AS treatment_type, t.cost AS treatment_cost, t.description AS treatment_desc " +
                "FROM appointments a " +
                "JOIN patients p ON a.patient_id = p.id " +
                "JOIN dentists d ON a.dentist_id = d.id " +
                "JOIN treatments t ON a.treatment_id = t.id " +
                "WHERE a.appointment_number = ?";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, appointmentNumber.trim());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapAppointmentFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding appointment by number: " + appointmentNumber, e);
        }
        return null;
    }

    @Override
    public Appointment findById(int id) {
        final String sql = "SELECT a.id, a.appointment_number, a.appointment_date, a.appointment_time, a.status, a.notes, a.created_at, " +
                "p.id AS patient_id, p.name AS patient_name, p.address AS patient_address, p.contact_number AS patient_contact, p.email AS patient_email, " +
                "d.id AS dentist_id, d.user_id AS dentist_user_id, d.full_name AS dentist_name, d.specialization AS dentist_spec, d.contact_number AS dentist_contact, d.email AS dentist_email, " +
                "t.id AS treatment_id, t.type AS treatment_type, t.cost AS treatment_cost, t.description AS treatment_desc " +
                "FROM appointments a " +
                "JOIN patients p ON a.patient_id = p.id " +
                "JOIN dentists d ON a.dentist_id = d.id " +
                "JOIN treatments t ON a.treatment_id = t.id " +
                "WHERE a.id = ?";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapAppointmentFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding appointment by id: " + id, e);
        }
        return null;
    }

    @Override
    public List<Appointment> findByDentistAndDate(int dentistId, LocalDate date) {
        List<Appointment> list = new ArrayList<>();
        final String sql = "SELECT a.id, a.appointment_number, a.appointment_date, a.appointment_time, a.status, a.notes, a.created_at, " +
                "p.id AS patient_id, p.name AS patient_name, p.address AS patient_address, p.contact_number AS patient_contact, p.email AS patient_email, " +
                "d.id AS dentist_id, d.user_id AS dentist_user_id, d.full_name AS dentist_name, d.specialization AS dentist_spec, d.contact_number AS dentist_contact, d.email AS dentist_email, " +
                "t.id AS treatment_id, t.type AS treatment_type, t.cost AS treatment_cost, t.description AS treatment_desc " +
                "FROM appointments a " +
                "JOIN patients p ON a.patient_id = p.id " +
                "JOIN dentists d ON a.dentist_id = d.id " +
                "JOIN treatments t ON a.treatment_id = t.id " +
                "WHERE a.dentist_id = ? AND a.appointment_date = ? " +
                "ORDER BY a.appointment_time ASC";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, dentistId);
            ps.setDate(2, Date.valueOf(date));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapAppointmentFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding appointments for dentist: " + dentistId + " on date: " + date, e);
        }
        return list;
    }

    @Override
    public List<Appointment> findAll() {
        List<Appointment> list = new ArrayList<>();
        final String sql = "SELECT a.id, a.appointment_number, a.appointment_date, a.appointment_time, a.status, a.notes, a.created_at, " +
                "p.id AS patient_id, p.name AS patient_name, p.address AS patient_address, p.contact_number AS patient_contact, p.email AS patient_email, " +
                "d.id AS dentist_id, d.user_id AS dentist_user_id, d.full_name AS dentist_name, d.specialization AS dentist_spec, d.contact_number AS dentist_contact, d.email AS dentist_email, " +
                "t.id AS treatment_id, t.type AS treatment_type, t.cost AS treatment_cost, t.description AS treatment_desc " +
                "FROM appointments a " +
                "JOIN patients p ON a.patient_id = p.id " +
                "JOIN dentists d ON a.dentist_id = d.id " +
                "JOIN treatments t ON a.treatment_id = t.id " +
                "ORDER BY a.appointment_date DESC, a.appointment_time DESC";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapAppointmentFromResultSet(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching all appointments", e);
        }
        return list;
    }

    @Override
    public boolean updateStatus(int appointmentId, String status) {
        final String sql = "UPDATE appointments SET status = ? WHERE id = ?";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, appointmentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating appointment status for id: " + appointmentId, e);
        }
        return false;
    }

    @Override
    public boolean updateTreatmentAndNotes(int appointmentId, int treatmentId, String clinicalNotes, String status) {
        final String sql = "UPDATE appointments SET treatment_id = ?, notes = ?, status = ? WHERE id = ?";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, treatmentId);
            ps.setString(2, clinicalNotes != null ? clinicalNotes : "");
            ps.setString(3, status != null ? status : "SCHEDULED");
            ps.setInt(4, appointmentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating treatment and clinical notes for appointment id: " + appointmentId, e);
        }
        return false;
    }

    private Appointment mapAppointmentFromResultSet(ResultSet rs) throws SQLException {
        Appointment a = new Appointment();
        a.setId(rs.getInt("id"));
        a.setAppointmentNumber(rs.getString("appointment_number"));
        a.setAppointmentDate(rs.getDate("appointment_date").toLocalDate());
        a.setAppointmentTime(rs.getTime("appointment_time").toLocalTime());
        a.setStatus(rs.getString("status"));
        a.setNotes(rs.getString("notes"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            a.setCreatedAt(ts.toLocalDateTime());
        }

        Patient p = new Patient();
        p.setId(rs.getInt("patient_id"));
        p.setName(rs.getString("patient_name"));
        p.setAddress(rs.getString("patient_address"));
        p.setContactNumber(rs.getString("patient_contact"));
        p.setEmail(rs.getString("patient_email"));
        a.setPatient(p);

        DentistProfile d = new DentistProfile();
        d.setId(rs.getInt("dentist_id"));
        d.setUserId(rs.getInt("dentist_user_id"));
        d.setFullName(rs.getString("dentist_name"));
        d.setSpecialization(rs.getString("dentist_spec"));
        d.setContactNumber(rs.getString("dentist_contact"));
        d.setEmail(rs.getString("dentist_email"));
        a.setDentist(d);

        Treatment t = new Treatment();
        t.setId(rs.getInt("treatment_id"));
        t.setType(rs.getString("treatment_type"));
        t.setCost(rs.getDouble("treatment_cost"));
        t.setDescription(rs.getString("treatment_desc"));
        a.setTreatment(t);

        return a;
    }
}
