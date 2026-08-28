package client;

/**
 * Exception thrown by ApiClient when the REST service returns an error response.
 *
 * @author Student
 */
public class ApiException extends Exception {
    private final int statusCode;
    private final String errorType;

    public ApiException(int statusCode, String errorType, String message) {
        super(message);
        this.statusCode = statusCode;
        this.errorType = errorType;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getErrorType() {
        return errorType;
    }

    @Override
    public String toString() {
        return String.format("[%d %s] %s", statusCode, errorType, getMessage());
    }
}
