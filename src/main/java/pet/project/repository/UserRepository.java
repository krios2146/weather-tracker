package pet.project.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import pet.project.model.User;
import pet.project.util.EntityManagerFactoryUtil;

import java.util.List;
import java.util.Optional;

public class UserRepository implements CrudRepository<User> {
    private final EntityManager entityManager = EntityManagerFactoryUtil.getInstance().createEntityManager();

    @Override
    public Optional<User> findById(Long id) {
        User user = entityManager.find(User.class, id);
        return Optional.ofNullable(user);
    }

    @Override
    public List<User> findAll() {
        TypedQuery<User> query = entityManager.createQuery("SELECT * FROM users", User.class);
        return query.getResultList();
    }

    @Override
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

    @Override
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

    @Override
    public void update(User entity) {
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();

            entityManager.merge(entity);
            entityManager.flush();

            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            throw new RuntimeException(e);
        }
    }
}
