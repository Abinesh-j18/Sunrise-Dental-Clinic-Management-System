package dao;

import model.Appointment;
import model.Invoice;
import model.InvoiceCalculation;
import model.InvoiceItem;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JDBC implementation of InvoiceDAO.
 * Interacts with MySQL stored procedure CalculateInvoiceTotal and manages
 * transactional persistence of Invoices and composed InvoiceItems.
 *
 * @author Student
 */
public class InvoiceDAOImpl implements InvoiceDAO {
    private static final Logger LOGGER = Logger.getLogger(InvoiceDAOImpl.class.getName());
    private final DatabaseConnectionManager connectionManager;
    private final AppointmentDAO appointmentDAO;

    public InvoiceDAOImpl() {
        this.connectionManager = DatabaseConnectionManager.getInstance();
        this.appointmentDAO = new AppointmentDAOImpl(connectionManager);
    }

    public InvoiceDAOImpl(DatabaseConnectionManager connectionManager, AppointmentDAO appointmentDAO) {
        this.connectionManager = connectionManager;
        this.appointmentDAO = appointmentDAO;
    }

    @Override
    public InvoiceCalculation calculateBill(int appointmentId) {
        final String sql = "{CALL CalculateInvoiceTotal(?, ?, ?, ?)}";

        try (Connection conn = connectionManager.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setInt(1, appointmentId);
            cs.registerOutParameter(2, Types.DECIMAL);
            cs.registerOutParameter(3, Types.DECIMAL);
            cs.registerOutParameter(4, Types.DECIMAL);

            cs.execute();

            double treatmentCost = cs.getDouble(2);
            double consultationFee = cs.getDouble(3);
            double total = cs.getDouble(4);

            return new InvoiceCalculation(appointmentId, consultationFee, treatmentCost, total);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error executing CalculateInvoiceTotal for appointment: " + appointmentId, e);
        }
        return null;
    }

    @Override
    public Invoice createInvoice(Invoice invoice) {
        if (invoice == null || invoice.getAppointment() == null) {
            return null;
        }

        final String insertInvoiceSql = "INSERT INTO invoices (appointment_id, consultation_fee, treatment_cost, total_amount, status, payment_method) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        final String insertItemSql = "INSERT INTO invoice_items (invoice_id, description, amount) VALUES (?, ?, ?)";

        Connection conn = null;
        try {
            conn = connectionManager.getConnection();
            conn.setAutoCommit(false); // Begin transaction for composition integrity

            int invoiceId;
            try (PreparedStatement psInv = conn.prepareStatement(insertInvoiceSql, Statement.RETURN_GENERATED_KEYS)) {
                psInv.setInt(1, invoice.getAppointment().getId());
                psInv.setDouble(2, invoice.getConsultationFee());
                psInv.setDouble(3, invoice.getTreatmentCost());
                psInv.setDouble(4, invoice.getTotalAmount());
                psInv.setString(5, invoice.getStatus() != null ? invoice.getStatus() : "PAID");
                psInv.setString(6, invoice.getPaymentMethod() != null ? invoice.getPaymentMethod() : "Cash");

                int affected = psInv.executeUpdate();
                if (affected == 0) {
                    conn.rollback();
                    return null;
                }

                try (ResultSet gk = psInv.getGeneratedKeys()) {
                    if (gk.next()) {
                        invoiceId = gk.getInt(1);
                        invoice.setId(invoiceId);
                    } else {
                        conn.rollback();
                        return null;
                    }
                }
            }

            // Insert line items
            if (invoice.getItems() != null && !invoice.getItems().isEmpty()) {
                try (PreparedStatement psItem = conn.prepareStatement(insertItemSql)) {
                    for (InvoiceItem item : invoice.getItems()) {
                        psItem.setInt(1, invoiceId);
                        psItem.setString(2, item.getDescription());
                        psItem.setDouble(3, item.getAmount());
                        psItem.addBatch();
                    }
                    psItem.executeBatch();
                }
            }

            conn.commit(); // Commit transaction
            return findById(invoiceId);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating invoice transactionally", e);
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Error rolling back invoice transaction", ex);
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Error closing connection", e);
                }
            }
        }
        return null;
    }

    @Override
    public Invoice findByAppointmentId(int appointmentId) {
        final String sql = "SELECT id, appointment_id, consultation_fee, treatment_cost, total_amount, generated_date, status, payment_method " +
                "FROM invoices WHERE appointment_id = ?";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, appointmentId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapInvoiceFromResultSet(rs, conn);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding invoice by appointment id: " + appointmentId, e);
        }
        return null;
    }

    @Override
    public Invoice findById(int id) {
        final String sql = "SELECT id, appointment_id, consultation_fee, treatment_cost, total_amount, generated_date, status, payment_method " +
                "FROM invoices WHERE id = ?";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapInvoiceFromResultSet(rs, conn);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding invoice by id: " + id, e);
        }
        return null;
    }

    @Override
    public List<Invoice> findAll() {
        List<Invoice> list = new ArrayList<>();
        final String sql = "SELECT id, appointment_id, consultation_fee, treatment_cost, total_amount, generated_date, status, payment_method " +
                "FROM invoices ORDER BY id DESC";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapInvoiceFromResultSet(rs, conn));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching all invoices", e);
        }
        return list;
    }

    private Invoice mapInvoiceFromResultSet(ResultSet rs, Connection conn) throws SQLException {
        Invoice invoice = new Invoice();
        int invoiceId = rs.getInt("id");
        int apptId = rs.getInt("appointment_id");

        invoice.setId(invoiceId);
        invoice.setConsultationFee(rs.getDouble("consultation_fee"));
        invoice.setTreatmentCost(rs.getDouble("treatment_cost"));
        invoice.setTotalAmount(rs.getDouble("total_amount"));
        Timestamp ts = rs.getTimestamp("generated_date");
        if (ts != null) {
            invoice.setGeneratedDate(ts.toLocalDateTime());
        }
        invoice.setStatus(rs.getString("status"));
        invoice.setPaymentMethod(rs.getString("payment_method"));

        // Fetch associated appointment
        Appointment appt = appointmentDAO.findById(apptId);
        invoice.setAppointment(appt);

        // Fetch composed items
        final String itemsSql = "SELECT id, invoice_id, description, amount FROM invoice_items WHERE invoice_id = ? ORDER BY id ASC";
        try (PreparedStatement psItems = conn.prepareStatement(itemsSql)) {
            psItems.setInt(1, invoiceId);
            try (ResultSet rsItems = psItems.executeQuery()) {
                while (rsItems.next()) {
                    InvoiceItem item = new InvoiceItem(
                            rsItems.getInt("id"),
                            rsItems.getInt("invoice_id"),
                            rsItems.getString("description"),
                            rsItems.getDouble("amount")
                    );
                    invoice.addItem(item);
                }
            }
        }

        return invoice;
    }
}
