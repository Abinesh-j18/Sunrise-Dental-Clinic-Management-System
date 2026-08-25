package server.dto;

/**
 * Data Transfer Object for updating diagnosed treatment, clinical notes, and status.
 *
 * @author Student
 */
public class UpdateTreatmentRequest {
    private int treatmentId;
    private String clinicalNotes;
    private String status;

    public UpdateTreatmentRequest() {
    }

    public UpdateTreatmentRequest(int treatmentId, String clinicalNotes, String status) {
        this.treatmentId = treatmentId;
        this.clinicalNotes = clinicalNotes;
        this.status = status;
    }

    public int getTreatmentId() {
        return treatmentId;
    }

    public void setTreatmentId(int treatmentId) {
        this.treatmentId = treatmentId;
    }

    public String getClinicalNotes() {
        return clinicalNotes;
    }

    public void setClinicalNotes(String clinicalNotes) {
        this.clinicalNotes = clinicalNotes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
