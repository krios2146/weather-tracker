package pet.project.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import pet.project.model.Location;
import pet.project.model.User;
import pet.project.util.EntityManagerFactoryUtil;

import java.util.List;
import java.util.Optional;

public class LocationDao {
    private final EntityManager entityManager = EntityManagerFactoryUtil.getInstance().createEntityManager();

    public Optional<Location> findById(Long id) {
        Location location = entityManager.find(Location.class, id);
        return Optional.ofNullable(location);
    }

    public List<Location> findAll() {
        TypedQuery<Location> query = entityManager.createQuery("SELECT * FROM locations", Location.class);
        return query.getResultList();
    }

    public boolean isPresent(Location location) {
        Query query = entityManager.createQuery("SELECT COUNT(*) FROM Location l " +
                "WHERE l.name = :name AND " +
                "l.longitude = :longitude AND " +
                "l.latitude = :latitude"
        );

        query.setParameter("name", location.getName());
        query.setParameter("longitude", location.getLongitude());
        query.setParameter("latitude", location.getLatitude());

        Long result = (Long) query.getSingleResult();
        return result > 0;
    }

    public List<Location> findByUser(User user) {
        TypedQuery<Location> query = entityManager.createQuery("SELECT l FROM Location l " +
                        "JOIN l.users u " +
                        "WHERE u.id = :userId",
                Location.class);
        query.setParameter("userId", user.getId());
        return query.getResultList();
    }

    public Optional<Location> findByCoordinates(Double latitude, Double longitude) {
        TypedQuery<Location> query = entityManager.createQuery("SELECT l FROM Location l " +
                        "WHERE l.latitude = :latitude AND" +
                        "l.longitude = :longitude",
                Location.class);
        query.setParameter("latitude", latitude);
        query.setParameter("longitude", longitude);
        Location location = query.getSingleResult();
        return Optional.ofNullable(location);
    }

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
