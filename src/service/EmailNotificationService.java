package service;

import model.Appointment;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service for sending email confirmation notifications upon appointment booking using JavaMail API.
 * Wrapped with asynchronous execution and exception isolation so that any SMTP transport failure
 * never blocks or rolls back the core appointment creation workflow.
 *
 * @author Student
 */
public class EmailNotificationService {
    private static final Logger LOGGER = Logger.getLogger(EmailNotificationService.class.getName());
    private final ExecutorService emailExecutor;
    private final Properties mailProperties;
    private final String senderEmail;
    private final String senderPassword;

    public EmailNotificationService() {
        this.emailExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "EmailNotificationWorker");
            t.setDaemon(true);
            return t;
        });

        this.mailProperties = new Properties();
        // Configure standard SMTP properties (e.g. Gmail / Mailtrap / Clinic SMTP)
        mailProperties.put("mail.smtp.auth", "true");
        mailProperties.put("mail.smtp.starttls.enable", "true");
        mailProperties.put("mail.smtp.host", "smtp.gmail.com");
        mailProperties.put("mail.smtp.port", "587");
        mailProperties.put("mail.smtp.connectiontimeout", "3000");
        mailProperties.put("mail.smtp.timeout", "3000");

        this.senderEmail = "notifications@sunrisedental.lk";
        this.senderPassword = "dummy_smtp_password";
    }

    /**
     * Dispatches an appointment booking confirmation email asynchronously.
     * Guaranteed non-blocking and fault-tolerant.
     *
     * @param appointment the successfully scheduled appointment.
     */
    public void sendBookingConfirmationAsync(Appointment appointment) {
        if (appointment == null || appointment.getPatient() == null) {
            return;
        }

        emailExecutor.submit(() -> {
            try {
                String recipientEmail = appointment.getPatient().getEmail();
                if (recipientEmail == null || recipientEmail.trim().isEmpty()) {
                    LOGGER.info("Patient has no email configured; skipping email dispatch for appointment " + appointment.getAppointmentNumber());
                    return;
                }

                Session session = Session.getInstance(mailProperties, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(senderEmail, senderPassword);
                    }
                });

                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(senderEmail, "Sunrise Dental Clinic"));
                message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipientEmail.trim()));
                message.setSubject("Appointment Confirmation: " + appointment.getAppointmentNumber() + " - Sunrise Dental Clinic");

                String emailBody = buildConfirmationEmailBody(appointment);
                message.setText(emailBody);

                // Note: In local offline/development mode without live SMTP credentials, Transport.send will catch gracefully
                try {
                    Transport.send(message);
                    LOGGER.info("Booking confirmation email successfully dispatched to: " + recipientEmail);
                } catch (Exception smtpEx) {
                    LOGGER.log(Level.WARNING, "SMTP delivery failed (expected in test/offline environment). Booking remains intact. Reason: " + smtpEx.getMessage());
                }
            } catch (Exception e) {
                // Safeguard: Never propagate email errors to the booking flow
                LOGGER.log(Level.WARNING, "Failed to assemble confirmation email for appointment: " + appointment.getAppointmentNumber(), e);
            }
        });
    }

    private String buildConfirmationEmailBody(Appointment appointment) {
        StringBuilder sb = new StringBuilder();
        sb.append("Dear ").append(appointment.getPatient().getName()).append(",\n\n");
        sb.append("Your dental appointment at Sunrise Dental Clinic has been successfully booked.\n\n");
        sb.append("Appointment Details:\n");
        sb.append("---------------------------------------------------\n");
        sb.append("Appointment Number : ").append(appointment.getAppointmentNumber()).append("\n");
        sb.append("Dentist            : ").append(appointment.getDentist().getFullName()).append(" (").append(appointment.getDentist().getSpecialization()).append(")\n");
        sb.append("Treatment          : ").append(appointment.getTreatment().getType()).append("\n");
        sb.append("Date               : ").append(appointment.getAppointmentDate()).append("\n");
        sb.append("Time               : ").append(appointment.getAppointmentTime()).append("\n");
        sb.append("Location           : Sunrise Dental Clinic, 45 Galle Road, Colombo 03\n");
        sb.append("---------------------------------------------------\n\n");
        sb.append("Please arrive 10 minutes prior to your scheduled time.\n");
        sb.append("If you need to reschedule, please call our clinic front desk at 011-2345678.\n\n");
        sb.append("Warm regards,\nSunrise Dental Clinic Team");
        return sb.toString();
    }

    /**
     * Gracefully shuts down background email executor service.
     */
    public void shutdown() {
        emailExecutor.shutdown();
    }
}
