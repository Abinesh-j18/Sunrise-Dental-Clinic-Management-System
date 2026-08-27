package service.reports;

import java.util.Map;

/**
 * Common interface for all clinic report generators in the Factory Method architecture.
 *
 * @author Student
 */
public interface Report {
    /**
     * Executes report calculation and returns structured reporting payload.
     *
     * @param parameters key-value parameters such as dentistId, date, month, year, or limit.
     * @return report outcome object (List or summary DTO).
     */
    Object generate(Map<String, Object> parameters);

    /**
     * Returns the human-readable title of this report.
     *
     * @return report title string.
     */
    String getReportTitle();
}
