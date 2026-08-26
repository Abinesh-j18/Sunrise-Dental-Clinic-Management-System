package service;

/**
 * Custom domain exception thrown when a requested resource (e.g. appointment number)
 * does not exist in the system. Translates to HTTP 404 (Not Found).
 *
 * @author Student
 */
public class ResourceNotFoundException extends Exception {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
