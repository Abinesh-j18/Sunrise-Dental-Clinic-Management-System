package service;

/**
 * Custom domain exception indicating failed authentication or expired/invalid session token.
 * Translates to HTTP 401 (Unauthorized).
 *
 * @author Student
 */
public class AuthenticationException extends Exception {
    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
