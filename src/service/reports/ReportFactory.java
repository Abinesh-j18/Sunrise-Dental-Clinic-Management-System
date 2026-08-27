package service.reports;

import dao.ReportDAO;

/**
 * Factory Method implementation for creating Report generator instances.
 *
 * DESIGN PATTERN TRADE-OFF EXPLANATION:
 * Pattern Applied: Factory Method Pattern (Creational).
 *
 * Rationale & Benefits:
 * 1. Open/Closed Principle (OCP): New clinical, administrative, or financial report types
 *    (e.g., DentistProductivityReport, MonthlyExpensesReport, PatientDemographicsReport) can
 *    be added by implementing the Report interface and registering them here, without altering
 *    existing HTTP handlers or calling controller workflows.
 * 2. Encapsulation of Instantiation: Calling code (REST handlers / services) remains decoupled
 *    from concrete report implementation classes and their specific DAO dependency wiring.
 *
 * Accepted Trade-offs:
 * - Structural Overhead: Introduces additional abstractions (Report interface, individual generator
 *   classes, and factory router) instead of simple procedural SQL helper methods. This slight
 *   increase in code volume is accepted to achieve genuine loose coupling, modular testability,
 *   and high academic/industry-standard maintainability.
 *
 * @author Student
 */
public class ReportFactory {
    private final ReportDAO reportDAO;

    public ReportFactory(ReportDAO reportDAO) {
        this.reportDAO = reportDAO;
    }

    /**
     * Factory Method to instantiate the appropriate Report implementation.
     *
     * @param type the requested ReportType enum value.
     * @return concrete Report instance.
     * @throws IllegalArgumentException if an unsupported report type is requested.
     */
    public Report createReport(ReportType type) {
        if (type == null) {
            throw new IllegalArgumentException("ReportType cannot be null");
        }

        switch (type) {
            case DAILY_APPOINTMENTS:
                return new DailyAppointmentsReport(reportDAO);
            case REVENUE_BY_TREATMENT:
                return new RevenueByTreatmentReport(reportDAO);
            case TOP_TREATMENTS:
                return new TopTreatmentsReport(reportDAO);
            default:
                throw new IllegalArgumentException("Unsupported report type: " + type);
        }
    }
}
