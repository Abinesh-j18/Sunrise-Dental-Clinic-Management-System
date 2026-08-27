package service.reports;

import dao.ReportDAO;
import model.RevenueByTreatmentReportItem;

import java.util.List;
import java.util.Map;

/**
 * Concrete report generator for Monthly / Yearly Treatment Revenue breakdown.
 *
 * @author Student
 */
public class RevenueByTreatmentReport implements Report {
    private final ReportDAO reportDAO;

    public RevenueByTreatmentReport(ReportDAO reportDAO) {
        this.reportDAO = reportDAO;
    }

    @Override
    public Object generate(Map<String, Object> parameters) {
        Integer month = null;
        Integer year = null;

        if (parameters != null) {
            if (parameters.containsKey("month") && parameters.get("month") != null) {
                Object val = parameters.get("month");
                if (val instanceof Number) {
                    month = ((Number) val).intValue();
                } else if (val instanceof String && !((String) val).trim().isEmpty()) {
                    month = Integer.parseInt(((String) val).trim());
                }
            }
            if (parameters.containsKey("year") && parameters.get("year") != null) {
                Object val = parameters.get("year");
                if (val instanceof Number) {
                    year = ((Number) val).intValue();
                } else if (val instanceof String && !((String) val).trim().isEmpty()) {
                    year = Integer.parseInt(((String) val).trim());
                }
            }
        }

        List<RevenueByTreatmentReportItem> items = reportDAO.getRevenueByTreatment(month, year);
        return items;
    }

    @Override
    public String getReportTitle() {
        return "Revenue By Treatment Analysis Report";
    }
}
