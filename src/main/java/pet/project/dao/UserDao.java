package pet.project.dao;

import jakarta.persistence.*;
import pet.project.exception.authentication.UserExistsException;
import pet.project.model.User;
import pet.project.util.PersistenceUtil;

import java.util.Optional;

public class UserDao {
    private final EntityManager entityManager = PersistenceUtil.getEntityManagerFactory().createEntityManager();

    public Optional<User> findByLogin(String login) {
        TypedQuery<User> query = entityManager.createQuery("SELECT u FROM User u WHERE u.login = :login", User.class);
        query.setParameter("login", login);

        // Catching RuntimeException is not good
        try {
            User user = query.getSingleResult();
            return Optional.of(user);

        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public void save(User entity) {
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();

            entityManager.persist(entity);
            entityManager.flush();

            transaction.commit();

        } catch (EntityExistsException e) {
            transaction.rollback();
            throw new UserExistsException("User: " + entity.getLogin() + " already exists");

        } catch (Exception e) {
            transaction.rollback();
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
