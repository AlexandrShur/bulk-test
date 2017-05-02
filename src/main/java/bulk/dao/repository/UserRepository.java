package bulk.dao.repository;

import bulk.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * User repository.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT user FROM User AS user")
    List<User> findUsers();

    User saveAndFlush(User user);
}
