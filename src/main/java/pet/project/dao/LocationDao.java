package pet.project.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
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
        TypedQuery<Integer> query = entityManager.createQuery("SELECT COUNT(*) FROM Location l " +
                        "WHERE l.name = :name AND" +
                        "l.longitude = :longitude AND" +
                        "l.latitude = :latitude",
                Integer.class);

        query.setParameter("name", location.getName());
        query.setParameter("longitude", location.getLongitude());
        query.setParameter("latitude", location.getLatitude());

        List<Integer> resultList = query.getResultList();
        return resultList.isEmpty() ? false : true;
    }

    public List<Location> findByUser(User user) {
        TypedQuery<Location> query = entityManager.createQuery("SELECT l FROM Location l " +
                        "JOIN l.users u " +
                        "WHERE u.id = :userId",
                Location.class);
        query.setParameter("userId", user.getId());
        return query.getResultList();
    }

    public List<Location> findByName(String name) {
        TypedQuery<Location> query = entityManager.createQuery("SELECT * FROM Location l " +
                        "WHERE l.name = :name",
                Location.class);
        query.setParameter("name", name);
        return query.getResultList();
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
