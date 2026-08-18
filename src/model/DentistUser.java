package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Concrete domain model representing a Practicing Dentist.
 * Overrides getMenuItems() polymorphically to display appointments assigned
 * to the dentist, daily treatment schedule, and clinical records.
 *
 * @author Student
 */
public class DentistUser extends User {
    private int dentistId;
    private String specialization;

    public DentistUser() {
        super();
        setRole("Dentist");
    }

    public DentistUser(int id, String username, String passwordHash, String fullName, String email, int dentistId, String specialization) {
        super(id, username, passwordHash, "Dentist", fullName, email);
        this.dentistId = dentistId;
        this.specialization = specialization;
    }

    public int getDentistId() {
        return dentistId;
    }

    public void setDentistId(int dentistId) {
        this.dentistId = dentistId;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    @Override
    public List<DashboardMenuItem> getMenuItems() {
        List<DashboardMenuItem> items = new ArrayList<>();
        items.add(new DashboardMenuItem("DASHBOARD", "Dentist Dashboard", "My daily treatment schedule and booked patients", "dashboard", "Core"));
        items.add(new DashboardMenuItem("DISPLAY_APPT", "Display Appointment", "Search & examine patient clinical history", "search", "Clinical"));
        items.add(new DashboardMenuItem("REPORTS", "Daily Schedule", "View personal schedule and appointment notes", "calendar", "Clinical"));
        items.add(new DashboardMenuItem("HELP", "Help Guide", "Clinical schedule navigation guide", "help-circle", "General"));
        items.add(new DashboardMenuItem("EXIT", "Exit Application", "Safely close session and client", "log-out", "General"));
        return Collections.unmodifiableList(items);
    }
}
