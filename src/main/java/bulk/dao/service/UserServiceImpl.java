package bulk.dao.service;

import bulk.dao.repository.UserRepository;
import bulk.dto.UserRequest;
import bulk.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * Person service implementation.
 */
@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserRepository userRepository;

    @Override
    @Transactional
    public List<User> getPersons(UserRequest userRequest) {
        return userRepository.findUsers();

    }

    @Override
    @Transactional
    public User addUser(User user) {
        return userRepository.saveAndFlush(user);
    }
}
