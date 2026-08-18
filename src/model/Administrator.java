package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Concrete domain model representing a Clinic Administrator user.
 * Overrides getMenuItems() polymorphically to provide management,
 * analytics, and system configuration capabilities.
 *
 * @author Student
 */
public class Administrator extends User {

    public Administrator() {
        super();
        setRole("Administrator");
    }

    public Administrator(int id, String username, String passwordHash, String fullName, String email) {
        super(id, username, passwordHash, "Administrator", fullName, email);
    }

    @Override
    public List<DashboardMenuItem> getMenuItems() {
        List<DashboardMenuItem> items = new ArrayList<>();
        items.add(new DashboardMenuItem("DASHBOARD", "Executive Overview", "Clinic revenue KPIs, patient statistics & operational summary", "dashboard", "Core"));
        items.add(new DashboardMenuItem("REPORTS", "Financial & Clinical Analytics", "Revenue by treatment, top procedures & schedule analysis", "chart-bar", "Analytics"));
        items.add(new DashboardMenuItem("ADMIN_MGMT", "Treatment Catalog & Staff", "Manage clinic treatment tariffs and clinician directory", "settings", "Management"));
        items.add(new DashboardMenuItem("DISPLAY_APPT", "Search Appointment Records", "Audit and inspect all clinic appointment bookings", "search", "Audit"));
        items.add(new DashboardMenuItem("HELP", "Help Manual", "System configuration and operational documentation", "help-circle", "General"));
        items.add(new DashboardMenuItem("EXIT", "Exit Application", "Safely close session and client", "log-out", "General"));
        return Collections.unmodifiableList(items);
    }
}
