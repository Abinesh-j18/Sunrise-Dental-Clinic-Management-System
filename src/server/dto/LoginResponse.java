package server.dto;

import model.DashboardMenuItem;
import model.User;
import java.util.List;

/**
 * DTO returned on successful login containing session token and authenticated user details.
 *
 * @author Student
 */
public class LoginResponse {
    private String token;
    private int userId;
    private String username;
    private String fullName;
    private String role;
    private String email;
    private Integer dentistId;
    private List<DashboardMenuItem> menuItems;

    public LoginResponse() {
    }

    public LoginResponse(String token, User user) {
        this.token = token;
        this.userId = user.getId();
        this.username = user.getUsername();
        this.fullName = user.getFullName();
        this.role = user.getRole();
        this.email = user.getEmail();
        this.menuItems = user.getMenuItems(); // Polymorphic invocation
        if (user instanceof model.DentistUser) {
            this.dentistId = ((model.DentistUser) user).getDentistId();
        }
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getDentistId() {
        return dentistId;
    }

    public void setDentistId(Integer dentistId) {
        this.dentistId = dentistId;
    }

    public List<DashboardMenuItem> getMenuItems() {
        return menuItems;
    }

    public void setMenuItems(List<DashboardMenuItem> menuItems) {
        this.menuItems = menuItems;
    }
}
