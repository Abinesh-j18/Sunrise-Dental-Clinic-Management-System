package service;

/**
 * Custom domain exception indicating invalid business or user input.
 * Translates to HTTP 400 (Bad Request).
 *
 * @author Student
 */
public class ValidationException extends Exception {
    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
