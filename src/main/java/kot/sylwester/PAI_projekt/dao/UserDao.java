package kot.sylwester.PAI_projekt.dao;

import kot.sylwester.PAI_projekt.entities.User;
import org.springframework.data.repository.CrudRepository;

public interface UserDao extends CrudRepository<User, Integer> {
    public User findByLogin(String login);
}
