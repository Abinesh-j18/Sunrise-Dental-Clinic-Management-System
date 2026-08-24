package server;

import model.User;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Thread-safe in-memory session token manager.
 * Manages active user sessions, token validity, and role-based permissions.
 *
 * @author Student
 */
public class SessionManager {
    private static final Logger LOGGER = Logger.getLogger(SessionManager.class.getName());
    private static final long SESSION_TIMEOUT_MS = 8 * 60 * 60 * 1000L; // 8 hours session duration
    private static final SecureRandom RANDOM = new SecureRandom();

    private final Map<String, UserSession> activeSessions = new ConcurrentHashMap<>();

    /**
     * Session data container.
     */
    public static class UserSession {
        private final String token;
        private final User user;
        private final long createdAt;
        private volatile long lastAccessedAt;

        public UserSession(String token, User user) {
            this.token = token;
            this.user = user;
            this.createdAt = System.currentTimeMillis();
            this.lastAccessedAt = this.createdAt;
        }

        public String getToken() {
            return token;
        }

        public User getUser() {
            return user;
        }

        public boolean isExpired() {
            return (System.currentTimeMillis() - lastAccessedAt) > SESSION_TIMEOUT_MS;
        }

        public void touch() {
            this.lastAccessedAt = System.currentTimeMillis();
        }
    }

    /**
     * Creates a new session for an authenticated user.
     *
     * @param user the authenticated user.
     * @return cryptographically secure session token string.
     */
    public String createSession(User user) {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        
        UserSession session = new UserSession(token, user);
        activeSessions.put(token, session);
        LOGGER.info("Session created for user: " + user.getUsername() + " [Role: " + user.getRole() + "]");
        return token;
    }

    /**
     * Validates a token and returns the corresponding User.
     *
     * @param token session token.
     * @return User if token is valid and active, null otherwise.
     */
    public User validateSession(String token) {
        if (token == null || token.trim().isEmpty()) {
            return null;
        }
        UserSession session = activeSessions.get(token.trim());
        if (session == null) {
            return null;
        }
        if (session.isExpired()) {
            activeSessions.remove(token.trim());
            LOGGER.info("Session expired for user: " + session.getUser().getUsername());
            return null;
        }
        session.touch();
        return session.getUser();
    }

    /**
     * Invalidates/logs out a session token.
     *
     * @param token session token to invalidate.
     * @return true if session was found and removed, false otherwise.
     */
    public boolean invalidateSession(String token) {
        if (token == null) {
            return false;
        }
        UserSession session = activeSessions.remove(token.trim());
        if (session != null) {
            LOGGER.info("Session invalidated for user: " + session.getUser().getUsername());
            return true;
        }
        return false;
    }

    /**
     * Clears all active sessions (e.g. on server shutdown).
     */
    public void clearAllSessions() {
        activeSessions.clear();
    }

    /**
     * Returns count of active sessions.
     *
     * @return active session count.
     */
    public int getActiveSessionCount() {
        return activeSessions.size();
    }
}
