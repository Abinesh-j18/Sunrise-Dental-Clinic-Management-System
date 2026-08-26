package util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Security utility for hashing and verifying passwords using SHA-256.
 *
 * @author Student
 */
public final class PasswordUtil {
    private static final Logger LOGGER = Logger.getLogger(PasswordUtil.class.getName());

    private PasswordUtil() {
        // Prevent instantiation of utility class
    }

    /**
     * Hashes a plain text password with SHA-256 and returns a hex string.
     *
     * @param plainPassword the plain text password.
     * @return 64-character lowercase hexadecimal hash string.
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(plainPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            LOGGER.log(Level.SEVERE, "SHA-256 algorithm not found", e);
            throw new RuntimeException("Cryptographic failure in password hashing", e);
        }
    }

    /**
     * Verifies if a plain text password matches an existing SHA-256 hash.
     *
     * @param plainPassword the plain text candidate password.
     * @param expectedHash  the stored SHA-256 hash.
     * @return true if matches, false otherwise.
     */
    public static boolean verifyPassword(String plainPassword, String expectedHash) {
        if (plainPassword == null || expectedHash == null) {
            return false;
        }
        String calculated = hashPassword(plainPassword);
        return calculated.equalsIgnoreCase(expectedHash);
    }
}
