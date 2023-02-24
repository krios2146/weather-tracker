package pet.project.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import pet.project.model.Location;
import pet.project.util.EntityManagerFactoryUtil;

import java.util.List;
import java.util.Optional;

public class LocationRepository implements CrudRepository<Location> {
    private final EntityManager entityManager = EntityManagerFactoryUtil.getInstance().createEntityManager();

    @Override
    public Optional<Location> findById(Long id) {
        Location location = entityManager.find(Location.class, id);
        return Optional.ofNullable(location);
    }

    @Override
    public List<Location> findAll() {
        TypedQuery<Location> query = entityManager.createQuery("SELECT * FROM locations", Location.class);
        return query.getResultList();
    }

    @Override
    public void save(Location entity) {
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
    public void delete(Location entity) {
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
    public void update(Location entity) {
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
