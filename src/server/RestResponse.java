package server;

/**
 * Standard REST API response wrapper.
 *
 * @param <T> data payload type.
 * @author Student
 */
public class RestResponse<T> {
    private boolean success;
    private int status;
    private String message;
    private String error;
    private T data;

    public RestResponse() {
    }

    public static <T> RestResponse<T> ok(T data, String message) {
        RestResponse<T> resp = new RestResponse<>();
        resp.success = true;
        resp.status = 200;
        resp.data = data;
        resp.message = message;
        return resp;
    }

    public static <T> RestResponse<T> created(T data, String message) {
        RestResponse<T> resp = new RestResponse<>();
        resp.success = true;
        resp.status = 201;
        resp.data = data;
        resp.message = message;
        return resp;
    }

    public static <T> RestResponse<T> error(int status, String error, String message) {
        RestResponse<T> resp = new RestResponse<>();
        resp.success = false;
        resp.status = status;
        resp.error = error;
        resp.message = message;
        return resp;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
