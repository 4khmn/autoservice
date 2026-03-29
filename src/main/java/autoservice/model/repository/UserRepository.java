package autoservice.model.repository;

import autoservice.model.entities.User;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepository extends HibernateAbstractDAO<User, Long> {

    public UserRepository() {
        super(User.class);
    }


    public Optional<User> findByUsername(String username) {
        String hql = "FROM User u WHERE u.username = :username";
        return getSession().createQuery(hql, User.class)
                .setParameter("username", username)
                .uniqueResultOptional();
    }
}
