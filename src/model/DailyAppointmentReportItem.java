package model;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Report data model representing an appointment entry in the Daily Appointments report.
 *
 * @author Student
 */
public class DailyAppointmentReportItem {
    private String appointmentNumber;
    private String patientName;
    private String patientContact;
    private String dentistName;
    private String treatmentType;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    private String status;

    public DailyAppointmentReportItem() {
    }

    public DailyAppointmentReportItem(String appointmentNumber, String patientName, String patientContact,
                                      String dentistName, String treatmentType, LocalDate appointmentDate,
                                      LocalTime appointmentTime, String status) {
        this.appointmentNumber = appointmentNumber;
        this.patientName = patientName;
        this.patientContact = patientContact;
        this.dentistName = dentistName;
        this.treatmentType = treatmentType;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.status = status;
    }

    public String getAppointmentNumber() {
        return appointmentNumber;
    }

    public void setAppointmentNumber(String appointmentNumber) {
        this.appointmentNumber = appointmentNumber;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientContact() {
        return patientContact;
    }

    public void setPatientContact(String patientContact) {
        this.patientContact = patientContact;
    }

    public String getDentistName() {
        return dentistName;
    }

    public void setDentistName(String dentistName) {
        this.dentistName = dentistName;
    }

    public String getTreatmentType() {
        return treatmentType;
    }

    public void setTreatmentType(String treatmentType) {
        this.treatmentType = treatmentType;
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
}
