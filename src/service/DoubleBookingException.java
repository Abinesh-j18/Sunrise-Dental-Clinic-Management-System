package service;

/**
 * Custom domain exception thrown when attempting to book an appointment
 * for a dentist at a date/time slot that is already occupied.
 * Directly enforces clinic business rules and translates to HTTP 409 (Conflict).
 *
 * @author Student
 */
public class DoubleBookingException extends Exception {
    public DoubleBookingException(String message) {
        super(message);
    }

    public DoubleBookingException(String message, Throwable cause) {
        super(message, cause);
    }
}
