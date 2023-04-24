package pet.project.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import pet.project.model.User;
import pet.project.util.EntityManagerFactoryUtil;

import java.util.Optional;

public class UserDao {
    private final EntityManager entityManager = EntityManagerFactoryUtil.getInstance().createEntityManager();

    public Optional<User> findByLogin(String login) {
        TypedQuery<User> query = entityManager.createQuery("SELECT u FROM User u WHERE u.login = :login", User.class);
        query.setParameter("login", login);
        User user = query.getSingleResult();
        return Optional.ofNullable(user);
    }

    public boolean isPresent(User user) {
        Query query = entityManager.createQuery("SELECT COUNT(*) FROM User u " +
                "WHERE u.login = :login AND " +
                "u.password = :password"
        );

        query.setParameter("login", user.getLogin());
        query.setParameter("password", user.getPassword());

        Long result = (Long) query.getSingleResult();
        return result > 0;
    }

    public void save(User entity) {
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();

            entityManager.persist(entity);
            entityManager.flush();

            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            throw new RuntimeException(e);
        }
    }

    public void delete(User entity) {
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();

            entityManager.remove(entity);
            entityManager.flush();

            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            throw new RuntimeException(e);
        }
    }
}
