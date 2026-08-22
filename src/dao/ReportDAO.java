package dao;

import model.DailyAppointmentReportItem;
import model.RevenueByTreatmentReportItem;
import model.TopTreatmentReportItem;

import java.time.LocalDate;
import java.util.List;

/**
 * DAO interface for clinic analytics and reports.
 *
 * @author Student
 */
public interface ReportDAO {
    List<DailyAppointmentReportItem> getDailyAppointments(Integer dentistId, LocalDate date);
    List<RevenueByTreatmentReportItem> getRevenueByTreatment(Integer month, Integer year);
    List<TopTreatmentReportItem> getTopTreatments(int limit);
}
