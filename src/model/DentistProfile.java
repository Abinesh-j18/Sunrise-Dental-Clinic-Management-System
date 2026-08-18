package model;

/**
 * Domain entity representing a practicing Dentist profile.
 *
 * @author Student
 */
public class DentistProfile {
    private int id;
    private Integer userId;
    private String fullName;
    private String specialization;
    private String contactNumber;
    private String email;

    public DentistProfile() {
    }

    public DentistProfile(int id, Integer userId, String fullName, String specialization, String contactNumber, String email) {
        this.id = id;
        this.userId = userId;
        this.fullName = fullName;
        this.specialization = specialization;
        this.contactNumber = contactNumber;
        this.email = email;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return fullName + " - " + specialization;
    }
}
