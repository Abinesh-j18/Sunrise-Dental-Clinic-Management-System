package service;

import dao.ReportDAOImpl;
import service.reports.*;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Unit tests for the Factory Method pattern for Report generation.
 *
 * @author Student
 */
public class ReportFactoryTest {
    private ReportFactory reportFactory;

    @Before
    public void setUp() {
        reportFactory = new ReportFactory(new ReportDAOImpl());
    }

    @Test
    public void testFactoryCreatesDailyAppointmentsReport() {
        Report report = reportFactory.createReport(ReportType.DAILY_APPOINTMENTS);
        assertNotNull("Report instance must not be null", report);
        assertTrue("Must be instance of DailyAppointmentsReport", report instanceof DailyAppointmentsReport);
        assertEquals("Daily Appointments Schedule Report", report.getReportTitle());

        Map<String, Object> params = new HashMap<>();
        Object result = report.generate(params);
        assertNotNull("Report result should not be null", result);
        assertTrue("Result should be a list", result instanceof List);
    }

    @Test
    public void testFactoryCreatesRevenueByTreatmentReport() {
        Report report = reportFactory.createReport(ReportType.REVENUE_BY_TREATMENT);
        assertNotNull("Report instance must not be null", report);
        assertTrue("Must be instance of RevenueByTreatmentReport", report instanceof RevenueByTreatmentReport);
        assertEquals("Revenue By Treatment Analysis Report", report.getReportTitle());

        Map<String, Object> params = new HashMap<>();
        Object result = report.generate(params);
        assertNotNull("Report result should not be null", result);
        assertTrue("Result should be a list", result instanceof List);
    }

    @Test
    public void testFactoryCreatesTopTreatmentsReport() {
        Report report = reportFactory.createReport(ReportType.TOP_TREATMENTS);
        assertNotNull("Report instance must not be null", report);
        assertTrue("Must be instance of TopTreatmentsReport", report instanceof TopTreatmentsReport);
        assertEquals("Top Requested Treatments Report", report.getReportTitle());

        Map<String, Object> params = new HashMap<>();
        params.put("limit", 3);
        Object result = report.generate(params);
        assertNotNull("Report result should not be null", result);
        assertTrue("Result should be a list", result instanceof List);
    }
}
