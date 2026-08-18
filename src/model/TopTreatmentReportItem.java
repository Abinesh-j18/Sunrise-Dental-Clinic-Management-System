package model;

/**
 * Report model representing most frequent treatments.
 *
 * @author Student
 */
public class TopTreatmentReportItem {
    private String treatmentType;
    private int bookingsCount;
    private double unitCost;
    private double totalRevenueGenerated;

    public TopTreatmentReportItem() {
    }

    public TopTreatmentReportItem(String treatmentType, int bookingsCount, double unitCost, double totalRevenueGenerated) {
        this.treatmentType = treatmentType;
        this.bookingsCount = bookingsCount;
        this.unitCost = unitCost;
        this.totalRevenueGenerated = totalRevenueGenerated;
    }

    public String getTreatmentType() {
        return treatmentType;
    }

    public void setTreatmentType(String treatmentType) {
        this.treatmentType = treatmentType;
    }

    public int getBookingsCount() {
        return bookingsCount;
    }

    public void setBookingsCount(int bookingsCount) {
        this.bookingsCount = bookingsCount;
    }

    public double getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(double unitCost) {
        this.unitCost = unitCost;
    }

    public double getTotalRevenueGenerated() {
        return totalRevenueGenerated;
    }

    public void setTotalRevenueGenerated(double totalRevenueGenerated) {
        this.totalRevenueGenerated = totalRevenueGenerated;
    }
}
