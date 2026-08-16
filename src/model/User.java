package model;

import java.util.List;

/**
 * Abstract domain model representing an authenticated clinic staff user.
 * Demonstrates polymorphism via the abstract getMenuItems() method, which
 * each specific role subclass (Administrator, Receptionist, DentistUser)
 * implements to return its own dynamic role-based dashboard actions.
 *
 * @author Student
 */
public abstract class User {
    private int id;
    private String username;
    private String passwordHash;
    private String role;
    private String fullName;
    private String email;

    public User() {
    }

    public User(int id, String username, String passwordHash, String role, String fullName, String email) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.fullName = fullName;
        this.email = email;
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

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
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

    /**
     * Polymorphic method to return role-specific dashboard actions and navigation items.
     * Overridden by Administrator, Receptionist, and Dentist subclasses.
     *
     * @return List of DashboardMenuItem instances configured for this role.
     */
    public abstract List<DashboardMenuItem> getMenuItems();

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", role='" + role + '\'' +
                ", fullName='" + fullName + '\'' +
                '}';
    }
}
