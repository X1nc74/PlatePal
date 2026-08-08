package platepal.repository;

import java.util.List;
import java.util.Optional;

import platepal.model.User;

/**
 * Storage operations for users.
 */
public interface UserRepository {

    List<User> findAll();

    Optional<User> findById(String id);
    Optional<User> findByUsername(String username);

    void save(User user);

    void deleteById(String id);
}
