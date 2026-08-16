package model;

/**
 * Domain entity representing a clinical dental treatment and standard fee.
 *
 * @author Student
 */
public class Treatment {
    private int id;
    private String type;
    private double cost;
    private String description;

    public Treatment() {
    }

    public Treatment(int id, String type, double cost, String description) {
        this.id = id;
        this.type = type;
        this.cost = cost;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return String.format("%s (LKR %.2f)", type, cost);
    }
}
