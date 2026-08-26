package util;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.regex.Pattern;

/**
 * Validation utility providing strict verification for clinic inputs.
 * Ensures consistent validation across client and server tiers.
 *
 * @author Student
 */
public final class ValidationHelper {

    // Matches standard 10-digit Sri Lankan phone numbers (e.g. 0771234567, 0719876543, 0112345678) or international +94...
    private static final Pattern PHONE_PATTERN = Pattern.compile("^(?:0|\\+94)?[0-9]{9,10}$");
    
    // Basic standard email format RFC-5322 approximation
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$");

    private ValidationHelper() {
        // Utility class
    }

    /**
     * Checks if a string is non-null and not blank.
     *
     * @param str string to check.
     * @return true if non-empty, false otherwise.
     */
    public static boolean isNotEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }

    /**
     * Validates contact number format.
     *
     * @param phone phone number string.
     * @return true if valid numeric phone number, false otherwise.
     */
    public static boolean isValidPhoneNumber(String phone) {
        if (!isNotEmpty(phone)) {
            return false;
        }
        String clean = phone.trim().replaceAll("[\\s-]", "");
        return PHONE_PATTERN.matcher(clean).matches();
    }

    /**
     * Validates email format.
     *
     * @param email email string.
     * @return true if valid email syntax, false otherwise.
     */
    public static boolean isValidEmail(String email) {
        if (!isNotEmpty(email)) {
            return true; // Optional field
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Validates that an appointment date and time are not in the past.
     *
     * @param date appointment date.
     * @param time appointment time.
     * @return true if appointment is in the present or future, false if past.
     */
    public static boolean isFutureDateTime(LocalDate date, LocalTime time) {
        if (date == null || time == null) {
            return false;
        }
        LocalDate today = LocalDate.now();
        if (date.isBefore(today)) {
            return false;
        }
        if (date.isEqual(today)) {
            return time.isAfter(LocalTime.now());
        }
        return true;
    }
}
