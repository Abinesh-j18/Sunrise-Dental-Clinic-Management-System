package dao;

import model.Treatment;
import java.util.List;

/**
 * DAO interface for clinical treatments.
 *
 * @author Student
 */
public interface TreatmentDAO {
    List<Treatment> findAll();
    Treatment findById(int id);
    Treatment findByType(String type);
    boolean createTreatment(Treatment treatment);
    boolean deleteTreatment(int id);
}
