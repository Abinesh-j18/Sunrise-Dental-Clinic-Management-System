package dao;

import model.User;
import java.util.List;

/**
 * Data Access Object interface for user entity operations.
 *
 * @author Student
 */
public interface UserDAO {
    User findByUsername(String username);
    User findById(int id);
    List<User> findAll();
    boolean createUser(User user);
    boolean deleteUser(int id);
}
