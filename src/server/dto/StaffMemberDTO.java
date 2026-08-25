package server.dto;

/**
 * DTO representing a clinic staff member for administrative management.
 *
 * @author Student
 */
public class StaffMemberDTO {
    private int id; // User ID
    private String username;
    private String role;
    private String fullName;
    private String email;
    private String specialization;
    private String contactNumber;
    private Integer dentistId;

    public StaffMemberDTO() {
    }

    public StaffMemberDTO(int id, String username, String role, String fullName, String email, String specialization, String contactNumber, Integer dentistId) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.fullName = fullName;
        this.email = email;
        this.specialization = specialization;
        this.contactNumber = contactNumber;
        this.dentistId = dentistId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public Integer getDentistId() {
        return dentistId;
    }

    public void setDentistId(Integer dentistId) {
        this.dentistId = dentistId;
    }
}
