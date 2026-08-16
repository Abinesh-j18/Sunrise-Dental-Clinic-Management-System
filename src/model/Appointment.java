package model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Domain entity representing a scheduled dental appointment.
 * Appointment demonstrates aggregation with Patient, DentistProfile, and Treatment.
 * The appointmentNumber is generated automatically via database trigger (e.g. APT-2026-0001)
 * and is never manually assigned.
 *
 * @author Student
 */
public class Appointment {
    private int id;
    private String appointmentNumber;
    private Patient patient;              // Aggregation
    private DentistProfile dentist;       // Aggregation
    private Treatment treatment;          // Aggregation
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    private String status;               // SCHEDULED, COMPLETED, CANCELLED
    private String notes;
    private LocalDateTime createdAt;

    public Appointment() {
        this.status = "SCHEDULED";
    }

    public Appointment(int id, String appointmentNumber, Patient patient, DentistProfile dentist,
                       Treatment treatment, LocalDate appointmentDate, LocalTime appointmentTime,
                       String status, String notes) {
        this.id = id;
        this.appointmentNumber = appointmentNumber;
        this.patient = patient;
        this.dentist = dentist;
        this.treatment = treatment;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.status = status;
        this.notes = notes;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAppointmentNumber() {
        return appointmentNumber;
    }

    public void setAppointmentNumber(String appointmentNumber) {
        this.appointmentNumber = appointmentNumber;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public DentistProfile getDentist() {
        return dentist;
    }

    public void setDentist(DentistProfile dentist) {
        this.dentist = dentist;
    }

    public Treatment getTreatment() {
        return treatment;
    }

    public void setTreatment(Treatment treatment) {
        this.treatment = treatment;
    }

    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(LocalDate appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public LocalTime getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(LocalTime appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Appointment{" +
                "id=" + id +
                ", appointmentNumber='" + appointmentNumber + '\'' +
                ", patient=" + (patient != null ? patient.getName() : null) +
                ", dentist=" + (dentist != null ? dentist.getFullName() : null) +
                ", date=" + appointmentDate +
                ", time=" + appointmentTime +
                ", status='" + status + '\'' +
                '}';
    }
}
