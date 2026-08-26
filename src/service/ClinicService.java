package service;

import dao.*;
import model.*;
import service.reports.Report;
import service.reports.ReportFactory;
import service.reports.ReportType;
import util.PasswordUtil;
import util.ValidationHelper;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Core business service layer encapsulating validation, authorization,
 * transaction coordination, and domain workflows.
 *
 * @author Student
 */
public class ClinicService {
    private static final Logger LOGGER = Logger.getLogger(ClinicService.class.getName());

    private final UserDAO userDAO;
    private final PatientDAO patientDAO;
    private final DentistDAO dentistDAO;
    private final TreatmentDAO treatmentDAO;
    private final AppointmentDAO appointmentDAO;
    private final InvoiceDAO invoiceDAO;
    private final ReportFactory reportFactory;
    private final EmailNotificationService emailNotificationService;

    public ClinicService() {
        this.userDAO = new UserDAOImpl();
        this.patientDAO = new PatientDAOImpl();
        this.dentistDAO = new DentistDAOImpl();
        this.treatmentDAO = new TreatmentDAOImpl();
        this.appointmentDAO = new AppointmentDAOImpl();
        this.invoiceDAO = new InvoiceDAOImpl();
        this.reportFactory = new ReportFactory(new ReportDAOImpl());
        this.emailNotificationService = new EmailNotificationService();
    }

    public ClinicService(UserDAO userDAO, PatientDAO patientDAO, DentistDAO dentistDAO,
                         TreatmentDAO treatmentDAO, AppointmentDAO appointmentDAO,
                         InvoiceDAO invoiceDAO, ReportFactory reportFactory,
                         EmailNotificationService emailNotificationService) {
        this.userDAO = userDAO;
        this.patientDAO = patientDAO;
        this.dentistDAO = dentistDAO;
        this.treatmentDAO = treatmentDAO;
        this.appointmentDAO = appointmentDAO;
        this.invoiceDAO = invoiceDAO;
        this.reportFactory = reportFactory;
        this.emailNotificationService = emailNotificationService;
    }

    /**
     * Authenticates clinic staff credentials.
     * Throws a generic AuthenticationException on failure to prevent username harvesting.
     *
     * @param username staff username.
     * @param password plain text password.
     * @return polymorphic User instance (Administrator, Receptionist, or DentistUser).
     * @throws ValidationException     if inputs are empty.
     * @throws AuthenticationException if credentials do not match.
     */
    public User authenticate(String username, String password) throws ValidationException, AuthenticationException {
        if (!ValidationHelper.isNotEmpty(username) || !ValidationHelper.isNotEmpty(password)) {
            throw new ValidationException("Username and password are required.");
        }

        User user = userDAO.findByUsername(username.trim());
        if (user == null) {
            // Constant-time-like behaviour: reject with identical error message
            throw new AuthenticationException("Invalid credentials. Please check your username and password.");
        }

        boolean valid = PasswordUtil.verifyPassword(password, user.getPasswordHash());
        if (!valid) {
            throw new AuthenticationException("Invalid credentials. Please check your username and password.");
        }

        return user;
    }

    /**
     * Registers a new patient with strict input validation.
     *
     * @param patient patient entity.
     * @return persisted Patient with generated ID.
     * @throws ValidationException on invalid input fields.
     */
    public Patient registerPatient(Patient patient) throws ValidationException {
        if (patient == null) {
            throw new ValidationException("Patient data cannot be null.");
        }
        if (!ValidationHelper.isNotEmpty(patient.getName())) {
            throw new ValidationException("Patient full name is required.");
        }
        if (!ValidationHelper.isNotEmpty(patient.getAddress())) {
            throw new ValidationException("Patient residential address is required.");
        }
        if (!ValidationHelper.isValidPhoneNumber(patient.getContactNumber())) {
            throw new ValidationException("Invalid contact number format. Please provide a valid 10-digit telephone number.");
        }
        if (!ValidationHelper.isValidEmail(patient.getEmail())) {
            throw new ValidationException("Invalid email format.");
        }

        Patient created = patientDAO.create(patient);
        if (created == null) {
            throw new RuntimeException("Failed to register patient in database.");
        }
        return created;
    }

    public List<Patient> getPatients(String search) {
        if (ValidationHelper.isNotEmpty(search)) {
            return patientDAO.searchByNameOrContact(search);
        }
        return patientDAO.findAll();
    }

    public Patient getPatientById(int id) throws ResourceNotFoundException {
        Patient p = patientDAO.findById(id);
        if (p == null) {
            throw new ResourceNotFoundException("Patient not found with ID: " + id);
        }
        return p;
    }

    public List<DentistProfile> getDentists() {
        return dentistDAO.findAll();
    }

    public DentistProfile getDentistById(int id) throws ResourceNotFoundException {
        DentistProfile d = dentistDAO.findById(id);
        if (d == null) {
            throw new ResourceNotFoundException("Dentist not found with ID: " + id);
        }
        return d;
    }

    public List<Treatment> getTreatments() {
        return treatmentDAO.findAll();
    }

    public Treatment getTreatmentById(int id) throws ResourceNotFoundException {
        Treatment t = treatmentDAO.findById(id);
        if (t == null) {
            throw new ResourceNotFoundException("Treatment not found with ID: " + id);
        }
        return t;
    }

    /**
     * Books an appointment with double-booking prevention, DB trigger execution,
     * and asynchronous email notification dispatch.
     *
     * @param patientId   ID of the registered patient.
     * @param dentistId   ID of the assigned dentist.
     * @param treatmentId ID of the clinical treatment.
     * @param date        Appointment date.
     * @param time        Appointment time.
     * @param notes       Staff / clinical notes.
     * @return fully populated Appointment with trigger-generated appointment number.
     * @throws ValidationException      if input data or date is invalid/past.
     * @throws DoubleBookingException   if dentist already has a scheduled slot at date & time.
     * @throws ResourceNotFoundException if patient, dentist, or treatment does not exist.
     */
    public Appointment bookAppointment(int patientId, int dentistId, int treatmentId,
                                       LocalDate date, LocalTime time, String notes)
            throws ValidationException, DoubleBookingException, ResourceNotFoundException {

        if (date == null || time == null) {
            throw new ValidationException("Appointment date and time are required.");
        }
        if (!ValidationHelper.isFutureDateTime(date, time)) {
            throw new ValidationException("Appointment date and time must not be in the past.");
        }

        Patient patient = getPatientById(patientId);
        DentistProfile dentist = getDentistById(dentistId);
        Treatment treatment = getTreatmentById(treatmentId);

        // Advanced DB Stored Function availability check (solves double booking problem)
        boolean isAvailable = appointmentDAO.isDentistAvailable(dentistId, date, time);
        if (!isAvailable) {
            throw new DoubleBookingException(String.format(
                    "Double Booking Conflict: %s is already scheduled for an appointment on %s at %s. Please choose a different time or dentist.",
                    dentist.getFullName(), date, time
            ));
        }

        Appointment appt = new Appointment();
        appt.setPatient(patient);
        appt.setDentist(dentist);
        appt.setTreatment(treatment);
        appt.setAppointmentDate(date);
        appt.setAppointmentTime(time);
        appt.setStatus("SCHEDULED");
        appt.setNotes(notes != null ? notes.trim() : "");

        Appointment created = appointmentDAO.create(appt);
        if (created == null) {
            throw new RuntimeException("Failed to persist appointment in database.");
        }

        // Asynchronous JavaMail confirmation notification
        emailNotificationService.sendBookingConfirmationAsync(created);

        return created;
    }

    /**
     * Retrieves an appointment by its unique formatted number (e.g. APT-2026-0001).
     *
     * @param apptNumber unique appointment number.
     * @return full Appointment with patient, dentist, and treatment details.
     * @throws ValidationException      if appointment number format is blank.
     * @throws ResourceNotFoundException if no matching appointment is found.
     */
    public Appointment getAppointmentByNumber(String apptNumber) throws ValidationException, ResourceNotFoundException {
        if (!ValidationHelper.isNotEmpty(apptNumber)) {
            throw new ValidationException("Appointment number cannot be empty.");
        }

        Appointment appt = appointmentDAO.findByAppointmentNumber(apptNumber.trim());
        if (appt == null) {
            throw new ResourceNotFoundException("No appointment found matching appointment number: " + apptNumber.trim());
        }
        return appt;
    }

    public List<Appointment> getAllAppointments() {
        return appointmentDAO.findAll();
    }

    public List<Appointment> getAppointmentsByDentistAndDate(int dentistId, LocalDate date) {
        return appointmentDAO.findByDentistAndDate(dentistId, date != null ? date : LocalDate.now());
    }

    /**
     * Allows a Dentist or staff member to update the diagnosed treatment,
     * clinical notes, and appointment status following clinical consultation.
     *
     * @param appointmentId target appointment ID.
     * @param treatmentId   ID of the diagnosed clinical treatment.
     * @param clinicalNotes clinician notes and findings.
     * @param status        new status (e.g. "COMPLETED", "SCHEDULED").
     * @return updated Appointment object.
     * @throws ResourceNotFoundException if appointment or treatment is not found.
     */
    public Appointment updateAppointmentTreatment(int appointmentId, int treatmentId, String clinicalNotes, String status)
            throws ResourceNotFoundException {
        Appointment appt = appointmentDAO.findById(appointmentId);
        if (appt == null) {
            throw new ResourceNotFoundException("Appointment not found with ID: " + appointmentId);
        }
        Treatment treatment = getTreatmentById(treatmentId);
        if (treatment == null) {
            throw new ResourceNotFoundException("Treatment not found with ID: " + treatmentId);
        }

        boolean ok = appointmentDAO.updateTreatmentAndNotes(appointmentId, treatmentId, clinicalNotes, status);
        if (!ok) {
            throw new RuntimeException("Failed to update appointment treatment in database.");
        }

        return appointmentDAO.findById(appointmentId);
    }

    /**
     * Calculates total billing and generates an invoice for an appointment.
     * Invokes the MySQL stored procedure CalculateInvoiceTotal and creates itemized lines.
     *
     * @param appointmentId target appointment ID.
     * @param paymentMethod payment method (e.g. "Cash", "Card", "Insurance").
     * @return populated Invoice instance ready for viewing and printing.
     * @throws ResourceNotFoundException if appointment is not found.
     */
    public Invoice calculateAndGenerateInvoice(int appointmentId, String paymentMethod) throws ResourceNotFoundException {
        Appointment appt = appointmentDAO.findById(appointmentId);
        if (appt == null) {
            throw new ResourceNotFoundException("Appointment not found with ID: " + appointmentId);
        }

        // Check if invoice already exists
        Invoice existing = invoiceDAO.findByAppointmentId(appointmentId);
        if (existing != null) {
            return existing;
        }

        // Execute MySQL Stored Procedure CalculateInvoiceTotal
        InvoiceCalculation calc = invoiceDAO.calculateBill(appointmentId);
        if (calc == null) {
            throw new RuntimeException("Failed to compute invoice total via database stored procedure.");
        }

        Invoice invoice = new Invoice();
        invoice.setAppointment(appt);
        invoice.setConsultationFee(calc.getConsultationFee());
        invoice.setTreatmentCost(calc.getTreatmentCost());
        invoice.setTotalAmount(calc.getTotalAmount());
        invoice.setStatus("PAID");
        invoice.setPaymentMethod(ValidationHelper.isNotEmpty(paymentMethod) ? paymentMethod.trim() : "Cash");

        // Line item composition
        invoice.addItem(new InvoiceItem("Doctor Consultation & Clinic Fee", calc.getConsultationFee()));
        invoice.addItem(new InvoiceItem("Dental Treatment: " + appt.getTreatment().getType(), calc.getTreatmentCost()));

        Invoice created = invoiceDAO.createInvoice(invoice);
        if (created == null) {
            throw new RuntimeException("Failed to persist invoice transaction.");
        }
        return created;
    }

    public Invoice getInvoiceByAppointmentId(int appointmentId) throws ResourceNotFoundException {
        Invoice invoice = invoiceDAO.findByAppointmentId(appointmentId);
        if (invoice == null) {
            throw new ResourceNotFoundException("No invoice generated yet for appointment ID: " + appointmentId);
        }
        return invoice;
    }

    /**
     * Generates a report using the Factory Method pattern.
     *
     * @param type       ReportType enum.
     * @param parameters Report parameters map.
     * @return report payload.
     */
    public Object generateReport(ReportType type, Map<String, Object> parameters) {
        Report report = reportFactory.createReport(type);
        return report.generate(parameters);
    }

    /**
     * Retrieves all registered clinic staff (Administrators, Receptionists, and Dentists).
     */
    public List<server.dto.StaffMemberDTO> getAllStaffMembers() {
        List<server.dto.StaffMemberDTO> list = new ArrayList<>();
        List<User> users = userDAO.findAll();
        for (User u : users) {
            String spec = "-";
            String contact = "-";
            Integer dentistId = null;

            if (u instanceof DentistUser) {
                DentistUser du = (DentistUser) u;
                spec = du.getSpecialization();
                dentistId = du.getDentistId();
                DentistProfile dp = dentistDAO.findByUserId(u.getId());
                if (dp != null) {
                    contact = dp.getContactNumber();
                    if (dp.getSpecialization() != null) spec = dp.getSpecialization();
                }
            } else if ("Receptionist".equalsIgnoreCase(u.getRole())) {
                spec = "Front Desk & Patient Intake";
            } else if ("Administrator".equalsIgnoreCase(u.getRole())) {
                spec = "Executive Management";
            }

            list.add(new server.dto.StaffMemberDTO(
                    u.getId(),
                    u.getUsername(),
                    u.getRole(),
                    u.getFullName(),
                    u.getEmail(),
                    spec,
                    contact,
                    dentistId
            ));
        }
        return list;
    }

    /**
     * Registers a new staff member (Receptionist or Dentist) for Administrator management.
     */
    public server.dto.StaffMemberDTO registerStaffMember(server.dto.RegisterStaffRequest req) throws ValidationException {
        if (req == null) {
            throw new ValidationException("Staff registration data cannot be empty.");
        }
        if (!ValidationHelper.isNotEmpty(req.getUsername()) || req.getUsername().trim().length() < 3) {
            throw new ValidationException("Username must be at least 3 characters long.");
        }
        if (!ValidationHelper.isNotEmpty(req.getPassword()) || req.getPassword().length() < 4) {
            throw new ValidationException("Password must be at least 4 characters long.");
        }
        if (!ValidationHelper.isNotEmpty(req.getFullName())) {
            throw new ValidationException("Full name is required.");
        }
        if (!ValidationHelper.isValidEmail(req.getEmail())) {
            throw new ValidationException("A valid email address is required.");
        }
        if (!ValidationHelper.isNotEmpty(req.getRole())) {
            throw new ValidationException("Staff role is required (Receptionist or Dentist).");
        }

        // Check if username already exists
        if (userDAO.findByUsername(req.getUsername().trim()) != null) {
            throw new ValidationException("Username '" + req.getUsername().trim() + "' is already registered in the clinic system.");
        }

        String passwordHash = PasswordUtil.hashPassword(req.getPassword());
        String role = req.getRole().trim();
        User user;

        if ("Dentist".equalsIgnoreCase(role)) {
            user = new DentistUser(0, req.getUsername().trim(), passwordHash, req.getFullName().trim(), req.getEmail().trim(), 0, req.getSpecialization());
        } else if ("Administrator".equalsIgnoreCase(role)) {
            user = new Administrator(0, req.getUsername().trim(), passwordHash, req.getFullName().trim(), req.getEmail().trim());
        } else {
            user = new Receptionist(0, req.getUsername().trim(), passwordHash, req.getFullName().trim(), req.getEmail().trim());
        }

        boolean userCreated = userDAO.createUser(user);
        if (!userCreated) {
            throw new RuntimeException("Failed to create user account in database.");
        }

        Integer dentistId = null;
        if ("Dentist".equalsIgnoreCase(role)) {
            DentistProfile dp = new DentistProfile(
                    0,
                    user.getId(),
                    req.getFullName().trim(),
                    ValidationHelper.isNotEmpty(req.getSpecialization()) ? req.getSpecialization().trim() : "General Dentistry",
                    ValidationHelper.isNotEmpty(req.getContactNumber()) ? req.getContactNumber().trim() : "N/A",
                    req.getEmail().trim()
            );
            boolean dentistCreated = dentistDAO.createDentist(dp);
            if (!dentistCreated) {
                LOGGER.log(Level.WARNING, "User created but failed to link dentist profile for user ID: " + user.getId());
            } else {
                dentistId = dp.getId();
            }
        }

        return new server.dto.StaffMemberDTO(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                user.getFullName(),
                user.getEmail(),
                req.getSpecialization(),
                req.getContactNumber(),
                dentistId
        );
    }

    /**
     * Deactivates/removes a staff member from the system (Admin action).
     */
    public boolean deleteStaffMember(int userId) throws ValidationException {
        if (userId <= 1) {
            throw new ValidationException("Primary Master Administrator account cannot be deleted.");
        }
        DentistProfile dp = dentistDAO.findByUserId(userId);
        if (dp != null) {
            dentistDAO.deleteDentist(dp.getId());
        }
        return userDAO.deleteUser(userId);
    }

    /**
     * Adds a new dental treatment to the clinic catalog (Admin action).
     */
    public Treatment createTreatment(Treatment treatment) throws ValidationException {
        if (treatment == null) {
            throw new ValidationException("Treatment data cannot be empty.");
        }
        if (!ValidationHelper.isNotEmpty(treatment.getType())) {
            throw new ValidationException("Treatment procedure name is required.");
        }
        if (treatment.getCost() <= 0) {
            throw new ValidationException("Treatment cost must be greater than zero.");
        }

        boolean ok = treatmentDAO.createTreatment(treatment);
        if (!ok) {
            throw new RuntimeException("Failed to persist treatment in database.");
        }
        return treatment;
    }

    /**
     * Removes a dental treatment from the catalog (Admin action).
     */
    public boolean deleteTreatment(int treatmentId) {
        return treatmentDAO.deleteTreatment(treatmentId);
    }
}
