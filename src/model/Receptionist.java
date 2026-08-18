package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Concrete domain model representing a Front-Desk Receptionist.
 * Overrides getMenuItems() polymorphically to expose appointment registration,
 * patient search, appointment lookup, and billing operations.
 *
 * @author Student
 */
public class Receptionist extends User {

    public Receptionist() {
        super();
        setRole("Receptionist");
    }

    public Receptionist(int id, String username, String passwordHash, String fullName, String email) {
        super(id, username, passwordHash, "Receptionist", fullName, email);
    }

    @Override
    public List<DashboardMenuItem> getMenuItems() {
        List<DashboardMenuItem> items = new ArrayList<>();
        items.add(new DashboardMenuItem("DASHBOARD", "Overview Dashboard", "Today's clinic schedule and reception summary", "dashboard", "Core"));
        items.add(new DashboardMenuItem("REGISTER_APPT", "Register Appointment", "Register new patients & book dental visits", "calendar-plus", "Operations"));
        items.add(new DashboardMenuItem("DISPLAY_APPT", "Display Appointment", "Search appointment by unique number", "search", "Operations"));
        items.add(new DashboardMenuItem("BILLING", "Calculate & Print Bill", "Generate and print patient treatment invoice", "receipt", "Billing"));
        items.add(new DashboardMenuItem("REPORTS", "Daily Schedule Report", "View daily appointments by dentist", "calendar", "Reports"));
        items.add(new DashboardMenuItem("HELP", "Help Guide", "Receptionist operational workflow guidelines", "help-circle", "General"));
        items.add(new DashboardMenuItem("EXIT", "Exit Application", "Safely close session and client", "log-out", "General"));
        return Collections.unmodifiableList(items);
    }
}
