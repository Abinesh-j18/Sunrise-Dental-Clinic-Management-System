package server.dto;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Appointment booking request payload.
 *
 * @author Student
 */
public class BookAppointmentRequest {
    private int patientId;
    private int dentistId;
    private int treatmentId;
    private LocalDate date;
    private LocalTime time;
    private String notes;

    public BookAppointmentRequest() {
    }

    public BookAppointmentRequest(int patientId, int dentistId, int treatmentId, LocalDate date, LocalTime time, String notes) {
        this.patientId = patientId;
        this.dentistId = dentistId;
        this.treatmentId = treatmentId;
        this.date = date;
        this.time = time;
        this.notes = notes;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public int getDentistId() {
        return dentistId;
    }

    public void setDentistId(int dentistId) {
        this.dentistId = dentistId;
    }

    public int getTreatmentId() {
        return treatmentId;
    }

    public void setTreatmentId(int treatmentId) {
        this.treatmentId = treatmentId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
