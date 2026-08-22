package dao;

import model.DailyAppointmentReportItem;
import model.RevenueByTreatmentReportItem;
import model.TopTreatmentReportItem;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JDBC implementation of ReportDAO.
 * Exclusively uses parameterized PreparedStatements to aggregate reporting metrics.
 *
 * @author Student
 */
public class ReportDAOImpl implements ReportDAO {
    private static final Logger LOGGER = Logger.getLogger(ReportDAOImpl.class.getName());
    private final DatabaseConnectionManager connectionManager;

    public ReportDAOImpl() {
        this.connectionManager = DatabaseConnectionManager.getInstance();
    }

    public ReportDAOImpl(DatabaseConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public List<DailyAppointmentReportItem> getDailyAppointments(Integer dentistId, LocalDate date) {
        List<DailyAppointmentReportItem> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT a.appointment_number, p.name AS patient_name, p.contact_number AS patient_contact, " +
                        "d.full_name AS dentist_name, t.type AS treatment_type, a.appointment_date, a.appointment_time, a.status " +
                        "FROM appointments a " +
                        "JOIN patients p ON a.patient_id = p.id " +
                        "JOIN dentists d ON a.dentist_id = d.id " +
                        "JOIN treatments t ON a.treatment_id = t.id " +
                        "WHERE 1=1 "
        );

        if (dentistId != null && dentistId > 0) {
            sql.append("AND a.dentist_id = ? ");
        }
        if (date != null) {
            sql.append("AND a.appointment_date = ? ");
        }
        sql.append("ORDER BY a.appointment_date ASC, a.appointment_time ASC");

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int paramIndex = 1;
            if (dentistId != null && dentistId > 0) {
                ps.setInt(paramIndex++, dentistId);
            }
            if (date != null) {
                ps.setDate(paramIndex++, Date.valueOf(date));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DailyAppointmentReportItem item = new DailyAppointmentReportItem(
                            rs.getString("appointment_number"),
                            rs.getString("patient_name"),
                            rs.getString("patient_contact"),
                            rs.getString("dentist_name"),
                            rs.getString("treatment_type"),
                            rs.getDate("appointment_date").toLocalDate(),
                            rs.getTime("appointment_time").toLocalTime(),
                            rs.getString("status")
                    );
                    list.add(item);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error generating Daily Appointments report", e);
        }
        return list;
    }

    @Override
    public List<RevenueByTreatmentReportItem> getRevenueByTreatment(Integer month, Integer year) {
        List<RevenueByTreatmentReportItem> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT t.type AS treatment_type, t.cost AS unit_cost, " +
                        "COUNT(a.id) AS appt_count, " +
                        "COALESCE(SUM(CASE WHEN a.id IS NOT NULL THEN t.cost ELSE 0 END), 0.00) AS total_revenue " +
                        "FROM treatments t " +
                        "LEFT JOIN appointments a ON t.id = a.treatment_id AND a.status != 'CANCELLED' "
        );

        if (month != null && month > 0 && year != null && year > 0) {
            sql.append("AND MONTH(a.appointment_date) = ? AND YEAR(a.appointment_date) = ? ");
        } else if (year != null && year > 0) {
            sql.append("AND YEAR(a.appointment_date) = ? ");
        } else if (month != null && month > 0) {
            sql.append("AND MONTH(a.appointment_date) = ? ");
        }

        sql.append("GROUP BY t.id, t.type, t.cost ORDER BY total_revenue DESC");

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int paramIndex = 1;
            if (month != null && month > 0 && year != null && year > 0) {
                ps.setInt(paramIndex++, month);
                ps.setInt(paramIndex++, year);
            } else if (year != null && year > 0) {
                ps.setInt(paramIndex++, year);
            } else if (month != null && month > 0) {
                ps.setInt(paramIndex++, month);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    RevenueByTreatmentReportItem item = new RevenueByTreatmentReportItem(
                            rs.getString("treatment_type"),
                            rs.getInt("appt_count"),
                            rs.getDouble("unit_cost"),
                            rs.getDouble("total_revenue")
                    );
                    list.add(item);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error generating Revenue By Treatment report", e);
        }
        return list;
    }

    @Override
    public List<TopTreatmentReportItem> getTopTreatments(int limit) {
        List<TopTreatmentReportItem> list = new ArrayList<>();
        final String sql = "SELECT t.type AS treatment_type, t.cost AS unit_cost, " +
                "COUNT(a.id) AS bookings_count, " +
                "COALESCE(SUM(CASE WHEN a.id IS NOT NULL THEN t.cost ELSE 0 END), 0.00) AS total_revenue " +
                "FROM treatments t " +
                "LEFT JOIN appointments a ON t.id = a.treatment_id AND a.status != 'CANCELLED' " +
                "GROUP BY t.id, t.type, t.cost " +
                "ORDER BY bookings_count DESC, total_revenue DESC " +
                "LIMIT ?";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit > 0 ? limit : 5);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TopTreatmentReportItem item = new TopTreatmentReportItem(
                            rs.getString("treatment_type"),
                            rs.getInt("bookings_count"),
                            rs.getDouble("unit_cost"),
                            rs.getDouble("total_revenue")
                    );
                    list.add(item);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error generating Top Treatments report", e);
        }
        return list;
    }
}
