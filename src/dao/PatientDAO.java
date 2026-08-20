package dao;

import model.Patient;
import java.util.List;

/**
 * DAO interface for patient management operations.
 *
 * @author Student
 */
public interface PatientDAO {
    Patient create(Patient patient);
    Patient findById(int id);
    List<Patient> searchByNameOrContact(String keyword);
    List<Patient> findAll();
}
