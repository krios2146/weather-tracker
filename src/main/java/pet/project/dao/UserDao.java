package pet.project.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import pet.project.model.User;
import pet.project.util.PersistenceUtil;

import java.util.Optional;

public class UserDao {
    private final EntityManager entityManager = PersistenceUtil.getEntityManagerFactory().createEntityManager();

    public Optional<User> findByLogin(String login) {
        TypedQuery<User> query = entityManager.createQuery("SELECT u FROM User u WHERE u.login = :login", User.class);
        query.setParameter("login", login);
        User user = query.getSingleResult();
        return Optional.ofNullable(user);
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
