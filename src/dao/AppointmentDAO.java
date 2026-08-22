package dao;

import model.Appointment;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * DAO interface for appointment scheduling and retrieval.
 *
 * @author Student
 */
public interface AppointmentDAO {
    /**
     * Calls MySQL stored function CheckDentistAvailability to ensure no double-bookings.
     *
     * @param dentistId ID of the dentist.
     * @param date      Appointment date.
     * @param time      Appointment time.
     * @return true if dentist is free for that slot, false if occupied.
     */
    boolean isDentistAvailable(int dentistId, LocalDate date, LocalTime time);

    Appointment create(Appointment appointment);
    Appointment findByAppointmentNumber(String appointmentNumber);
    Appointment findById(int id);
    List<Appointment> findByDentistAndDate(int dentistId, LocalDate date);
    List<Appointment> findAll();
    boolean updateStatus(int appointmentId, String status);
    boolean updateTreatmentAndNotes(int appointmentId, int treatmentId, String clinicalNotes, String status);
}
