package pet.project.util;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class PersistenceUtil {
    private static final EntityManagerFactory ENTITY_MANAGER_FACTORY_INSTANCE;

    static {
        ENTITY_MANAGER_FACTORY_INSTANCE = Persistence.createEntityManagerFactory("weatherPersistenceUnit");
    }

    public static EntityManagerFactory getEntityManagerFactory() {
        return ENTITY_MANAGER_FACTORY_INSTANCE;
    }
}
