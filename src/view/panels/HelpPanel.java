package view.panels;

import view.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Help Section providing comprehensive step-by-step operational workflows for clinic staff.
 *
 * @author Student
 */
public class HelpPanel extends JPanel {

    public HelpPanel() {
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 20));
        setOpaque(false);
        setBorder(new EmptyBorder(25, 30, 25, 30));

        // Header Title
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Staff Operational User Guide & Help Manual");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.COLOR_TEXT_PRIMARY);

        JLabel lblDesc = new JLabel("Standard operating procedures (SOPs) for clinic receptionists, dentists, and administrators.");
        lblDesc.setFont(UITheme.FONT_BODY);
        lblDesc.setForeground(UITheme.COLOR_TEXT_MUTED);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 4));
        titleBlock.setOpaque(false);
        titleBlock.add(lblTitle);
        titleBlock.add(lblDesc);

        headerPanel.add(titleBlock, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        // Content Area with Accordion / Card Guides
        JPanel contentCard = UITheme.createCardPanel();
        contentCard.setLayout(new BorderLayout());

        JEditorPane helpContent = new JEditorPane();
        helpContent.setContentType("text/html");
        helpContent.setEditable(false);
        helpContent.setBackground(Color.WHITE);
        helpContent.setText(getHelpHTMLContent());
        helpContent.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(helpContent);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        contentCard.add(scrollPane, BorderLayout.CENTER);
        add(contentCard, BorderLayout.CENTER);
    }

    private String getHelpHTMLContent() {
        return "<html>" +
                "<body style='font-family: Segoe UI, sans-serif; font-size: 13px; color: #1E293B; line-height: 1.6; padding: 20px 25px; background-color: #FFFFFF;'>" +

                // Header Banner
                "<table width='100%' cellpadding='0' cellspacing='0' style='margin-bottom: 20px; border-bottom: 2px solid #0E7490; padding-bottom: 12px;'>" +
                "  <tr>" +
                "    <td>" +
                "      <h1 style='color: #0E7490; margin: 0; font-size: 22px;'>Sunrise Dental Clinic — Standard Operating Procedures (SOP)</h1>" +
                "      <p style='color: #64748B; margin: 4px 0 0 0; font-size: 13px;'>Comprehensive Clinic Staff Operational Manual & Architectural Reference</p>" +
                "    </td>" +
                "  </tr>" +
                "</table>" +

                // Section 1: Roles
                "<h3 style='color: #0E7490; margin-top: 15px; font-size: 16px;'>1. System Authentication & Role-Based Access Control (RBAC)</h3>" +
                "<p>Sunrise Dental Clinic Management System enforces strict role-based access control across three user tiers:</p>" +
                "<table width='100%' cellpadding='10' cellspacing='0' style='border: 1px solid #E2E8F0; background-color: #F8FAFC; margin-bottom: 15px; border-collapse: collapse;'>" +
                "  <tr style='border-bottom: 1px solid #E2E8F0;'>" +
                "    <td width='25%' style='font-weight: bold; color: #0E7490;'>Receptionist (Front Desk)</td>" +
                "    <td>Handles all-in-one patient intake, appointment scheduling, double-booking conflict resolution, patient search, billing calculations, and printable receipts.</td>" +
                "  </tr>" +
                "  <tr style='border-bottom: 1px solid #E2E8F0;'>" +
                "    <td style='font-weight: bold; color: #0284C7;'>Dentist (Dental Surgeon)</td>" +
                "    <td>Accesses the assigned doctor clinical queue, examines booked patients, updates clinical diagnosis procedures, enters patient medical notes, and updates appointment status to <code>COMPLETED</code>.</td>" +
                "  </tr>" +
                "  <tr>" +
                "    <td style='font-weight: bold; color: #059669;'>Administrator (Executive)</td>" +
                "    <td>Oversees clinic-wide KPIs, manages the staff directory (adding/removing receptionists and dentists), maintains the dental treatment tariff catalog, and analyzes revenue reports.</td>" +
                "  </tr>" +
                "</table>" +

                // Section 2: Receptionist SOP
                "<h3 style='color: #0E7490; margin-top: 25px; font-size: 16px;'>2. Patient Registration & Appointment Scheduling (Receptionist SOP)</h3>" +
                "<ol style='margin-left: 20px; padding-left: 0;'>" +
                "  <li><b>Select Patient Intake Mode:</b> Choose <b>New Patient</b> to register a first-time visitor (Full Name, 10-digit Phone, Residential Address, and optional Email), or select <b>Existing Patient</b> to automatically populate returning patient records.</li>" +
                "  <li><b>Assign Doctor & Tariff:</b> Select the attending <b>Dentist</b> and the prescribed initial <b>Treatment Type</b>.</li>" +
                "  <li><b>Set Date & Time Slot:</b> Choose a present or future booking date and select an available 30-minute time slot.</li>" +
                "  <li><b>Double-Booking Conflict Check:</b> When you click <b>Confirm & Book Appointment</b>, the system invokes the MySQL stored function <code>CheckDentistAvailability</code>. If the doctor already has a booking at that time slot, the system automatically rejects the duplicate booking.</li>" +
                "  <li><b>Serial Number Generation:</b> Upon booking, an immutable appointment number (e.g. <code>APT-2026-0001</code>) is automatically generated by the MySQL database trigger.</li>" +
                "</ol>" +

                // Section 3: Clinical Doctor SOP
                "<h3 style='color: #0E7490; margin-top: 25px; font-size: 16px;'>3. Doctor Clinical Examination & Diagnosis Updates (Dentist SOP)</h3>" +
                "<ol style='margin-left: 20px; padding-left: 0;'>" +
                "  <li><b>View Assigned Queue:</b> On the <b>Dentist Dashboard</b>, the doctor views only their assigned patients for the day.</li>" +
                "  <li><b>Examine Patient:</b> Select an appointment row and click <b>Update Clinical Diagnosis & Notes</b> (or double-click the row).</li>" +
                "  <li><b>Revise Treatment Diagnosis:</b> If clinical examination reveals a different condition (e.g. patient booked for <i>General Consultation</i> but requires a <i>Root Canal Treatment</i> or <i>Composite Tooth Filling</i>), select the revised procedure from the dropdown.</li>" +
                "  <li><b>Record Findings:</b> Enter detailed clinical findings, tooth surface notes, and prescribed medications in the <b>Doctor's Clinical Notes</b> box.</li>" +
                "  <li><b>Mark Completed:</b> Set the clinical status to <b>COMPLETED</b> and click <b>Save Diagnosis & Update Treatment</b>. The billing system will automatically charge the doctor's diagnosed treatment tariff.</li>" +
                "</ol>" +

                // Section 4: Billing SOP
                "<h3 style='color: #0E7490; margin-top: 25px; font-size: 16px;'>4. Invoicing & Official Hardware Receipt Printing (Billing SOP)</h3>" +
                "<ol style='margin-left: 20px; padding-left: 0;'>" +
                "  <li>Open <b>Calculate & Print Bill</b> from the sidebar.</li>" +
                "  <li>Enter the Appointment Number (e.g. <code>APT-2026-0001</code>) and click <b>Find Appointment</b>.</li>" +
                "  <li>Select the payment mode (<b>Cash</b>, <b>Credit/Debit Card</b>, <b>Health Insurance Direct</b>, or <b>Online Bank Transfer</b>).</li>" +
                "  <li>Click <b>Calculate & Generate Bill</b>. The system calls the MySQL Stored Procedure <code>CalculateInvoiceTotal</code> to compute the doctor consultation fee + treatment cost directly inside the database.</li>" +
                "  <li>Click <b>Print Official Bill / Receipt</b> to send the receipt to the printer, or click <b>View Printable Text</b> for digital copy.</li>" +
                "</ol>" +

                // Section 5: Staff & Treatment Catalog Administration
                "<h3 style='color: #0E7490; margin-top: 25px; font-size: 16px;'>5. Staff Directory & Treatment Tariff Catalog (Administrator SOP)</h3>" +
                "<p>Administrators have exclusive access to the <b>Treatment Catalog & Staff</b> panel:</p>" +
                "<ul>" +
                "  <li><b>Add Staff Member:</b> Register new Receptionists, Dental Surgeons, or Administrators with automatic specialization enforcement.</li>" +
                "  <li><b>Remove Staff:</b> Safely deactivate staff accounts when personnel leave the clinic.</li>" +
                "  <li><b>Treatment Catalog Management:</b> Add new dental procedures with official clinic standard fees (e.g. <i>Laser Whitening</i>, <i>Dental Implants</i>) or remove deprecated procedures.</li>" +
                "</ul>" +

                // Section 6: Analytics & Reports
                "<h3 style='color: #0E7490; margin-top: 25px; font-size: 16px;'>6. Clinical Analytics & Revenue Intelligence</h3>" +
                "<p>Under <b>Daily Schedule Report / Analytics</b>, clinic management can access three real-time reports:</p>" +
                "<ul>" +
                "  <li><b>Daily Appointments Schedule:</b> View and filter all appointments by clinician and specific calendar date.</li>" +
                "  <li><b>Revenue By Treatment:</b> Aggregates revenue and volume per dental procedure across months and years.</li>" +
                "  <li><b>Top Requested Treatments:</b> Ranks the highest volume dental procedures with exact revenue calculations.</li>" +
                "</ul>" +

                // Section 7: Security & Sign Out
                "<h3 style='color: #0E7490; margin-top: 25px; font-size: 16px;'>7. Security & Session Termination</h3>" +
                "<p>Always click <b>Sign Out</b> in the top right corner when finishing your work shift. The server will immediately invalidate the active authentication token, ensuring patient data confidentiality.</p>" +

                "</body></html>";
    }
}
