package bulk.dao.service;

import bulk.dto.UserRequest;
import bulk.model.User;

import java.util.List;

/**
 * User service.
 */
public interface UserService {

    /**
     * Get persons by {@link UserRequest}
     *
     * @param userRequest person request entity.
     * @return list of persons.
     */
    List<User> getPersons(UserRequest userRequest);

    User addUser(User user);
}
