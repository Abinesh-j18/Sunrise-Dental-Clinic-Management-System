package model;

/**
 * Report model representing revenue generated grouped by treatment type.
 *
 * @author Student
 */
public class RevenueByTreatmentReportItem {
    private String treatmentType;
    private int appointmentCount;
    private double unitCost;
    private double totalRevenue;

    public RevenueByTreatmentReportItem() {
    }

    public RevenueByTreatmentReportItem(String treatmentType, int appointmentCount, double unitCost, double totalRevenue) {
        this.treatmentType = treatmentType;
        this.appointmentCount = appointmentCount;
        this.unitCost = unitCost;
        this.totalRevenue = totalRevenue;
    }

    public String getTreatmentType() {
        return treatmentType;
    }

    public void setTreatmentType(String treatmentType) {
        this.treatmentType = treatmentType;
    }

    public int getAppointmentCount() {
        return appointmentCount;
    }

    public void setAppointmentCount(int appointmentCount) {
        this.appointmentCount = appointmentCount;
    }

    public double getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(double unitCost) {
        this.unitCost = unitCost;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
}
