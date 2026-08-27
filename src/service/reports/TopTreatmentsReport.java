package service.reports;

import dao.ReportDAO;
import model.TopTreatmentReportItem;

import java.util.List;
import java.util.Map;

/**
 * Concrete report generator for Top Most Requested Treatments.
 *
 * @author Student
 */
public class TopTreatmentsReport implements Report {
    private final ReportDAO reportDAO;

    public TopTreatmentsReport(ReportDAO reportDAO) {
        this.reportDAO = reportDAO;
    }

    @Override
    public Object generate(Map<String, Object> parameters) {
        int limit = 5;
        if (parameters != null && parameters.containsKey("limit") && parameters.get("limit") != null) {
            Object val = parameters.get("limit");
            if (val instanceof Number) {
                limit = ((Number) val).intValue();
            } else if (val instanceof String && !((String) val).trim().isEmpty()) {
                limit = Integer.parseInt(((String) val).trim());
            }
        }

        List<TopTreatmentReportItem> items = reportDAO.getTopTreatments(limit);
        return items;
    }

    @Override
    public String getReportTitle() {
        return "Top Requested Treatments Report";
    }
}
