package server.dto;

/**
 * Data Transfer Object for Administrator staff registration (Dentists & Receptionists).
 *
 * @author Student
 */
public class RegisterStaffRequest {
    private String username;
    private String password;
    private String role;
    private String fullName;
    private String email;
    private String specialization;
    private String contactNumber;

    public RegisterStaffRequest() {
    }

    public RegisterStaffRequest(String username, String password, String role, String fullName, String email, String specialization, String contactNumber) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.fullName = fullName;
        this.email = email;
        this.specialization = specialization;
        this.contactNumber = contactNumber;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }
}
