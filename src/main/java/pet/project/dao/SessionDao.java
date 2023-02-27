package pet.project.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import pet.project.model.Session;
import pet.project.util.EntityManagerFactoryUtil;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SessionDao {
    private final EntityManager entityManager = EntityManagerFactoryUtil.getInstance().createEntityManager();

    public Optional<Session> findById(UUID id) {
        Session session = entityManager.find(Session.class, id);
        return Optional.ofNullable(session);
    }

    public List<Session> findAll() {
        TypedQuery<Session> query = entityManager.createQuery("SELECT * FROM sessions", Session.class);
        return query.getResultList();
    }

    public void save(Session entity) {
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

    public void delete(Session entity) {
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

    public void update(Session entity) {
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
