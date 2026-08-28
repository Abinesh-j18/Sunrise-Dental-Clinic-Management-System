package client;

import model.User;

/**
 * Client-side session state holder.
 * Stores authenticated User and Bearer session token in memory for Tier 1 desktop client.
 *
 * @author Student
 */
public class SessionContext {
    private static volatile SessionContext instance;

    private String token;
    private User currentUser;
    private Integer currentDentistId;

    private SessionContext() {
    }

    public static SessionContext getInstance() {
        if (instance == null) {
            synchronized (SessionContext.class) {
                if (instance == null) {
                    instance = new SessionContext();
                }
            }
        }
        return instance;
    }

    public void setSession(String token, User user, Integer dentistId) {
        this.token = token;
        this.currentUser = user;
        this.currentDentistId = dentistId;
    }

    public void clearSession() {
        this.token = null;
        this.currentUser = null;
        this.currentDentistId = null;
    }

    public boolean isAuthenticated() {
        return token != null && !token.trim().isEmpty() && currentUser != null;
    }

    public String getToken() {
        return token;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public Integer getCurrentDentistId() {
        if (currentDentistId != null && currentDentistId > 0) {
            return currentDentistId;
        }
        if (currentUser instanceof model.DentistUser) {
            return ((model.DentistUser) currentUser).getDentistId();
        }
        return null;
    }
}
