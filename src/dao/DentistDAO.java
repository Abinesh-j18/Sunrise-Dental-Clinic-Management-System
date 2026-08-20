package dao;

import model.DentistProfile;
import java.util.List;

/**
 * DAO interface for dentist entity queries.
 *
 * @author Student
 */
public interface DentistDAO {
    List<DentistProfile> findAll();
    DentistProfile findById(int id);
    DentistProfile findByUserId(int userId);
    boolean createDentist(DentistProfile dentist);
    boolean deleteDentist(int id);
}
