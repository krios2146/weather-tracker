package pet.project.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import pet.project.model.Session;
import pet.project.util.EntityManagerFactoryUtil;

import java.util.List;
import java.util.Optional;

public class SessionRepository implements CrudRepository<Session> {
    private final EntityManager entityManager = EntityManagerFactoryUtil.getInstance().createEntityManager();

    @Override
    public Optional<Session> findById(Long id) {
        Session session = entityManager.find(Session.class, id);
        return Optional.ofNullable(session);
    }

    @Override
    public List<Session> findAll() {
        TypedQuery<Session> query = entityManager.createQuery("SELECT * FROM sessions", Session.class);
        return query.getResultList();
    }

    @Override
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

    @Override
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

    @Override
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
