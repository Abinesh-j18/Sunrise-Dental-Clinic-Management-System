package service.reports;

import dao.ReportDAO;
import model.DailyAppointmentReportItem;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Concrete report generator for Daily Appointments schedule.
 *
 * @author Student
 */
public class DailyAppointmentsReport implements Report {
    private final ReportDAO reportDAO;

    public DailyAppointmentsReport(ReportDAO reportDAO) {
        this.reportDAO = reportDAO;
    }

    @Override
    public Object generate(Map<String, Object> parameters) {
        Integer dentistId = null;
        LocalDate date = null;

        if (parameters != null) {
            if (parameters.containsKey("dentistId") && parameters.get("dentistId") != null) {
                Object val = parameters.get("dentistId");
                if (val instanceof Number) {
                    dentistId = ((Number) val).intValue();
                } else if (val instanceof String && !((String) val).trim().isEmpty()) {
                    dentistId = Integer.parseInt(((String) val).trim());
                }
            }
            if (parameters.containsKey("date") && parameters.get("date") != null) {
                Object val = parameters.get("date");
                if (val instanceof LocalDate) {
                    date = (LocalDate) val;
                } else if (val instanceof String && !((String) val).trim().isEmpty()) {
                    date = LocalDate.parse(((String) val).trim());
                }
            }
        }

        List<DailyAppointmentReportItem> items = reportDAO.getDailyAppointments(dentistId, date);
        return items;
    }

    @Override
    public String getReportTitle() {
        return "Daily Appointments Schedule Report";
    }
}
