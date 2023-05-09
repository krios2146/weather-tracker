package pet.project.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;
import pet.project.model.Session;
import pet.project.util.PersistenceUtil;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public class SessionDao {
    private final EntityManager entityManager = PersistenceUtil.getEntityManagerFactory().createEntityManager();

    public Optional<Session> findById(UUID id) {
        Session session = entityManager.find(Session.class, id);
        return Optional.ofNullable(session);
    }

    public void deleteSessionsExpiredAtTime(LocalDateTime time) {
        Query query = entityManager.createQuery("DELETE FROM Session s WHERE s.expiresAt <= :time");
        query.setParameter("time", time);

        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();

            query.executeUpdate();

            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            throw new RuntimeException(e);
        }
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
}
