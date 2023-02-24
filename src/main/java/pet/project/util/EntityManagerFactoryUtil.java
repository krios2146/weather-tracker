package pet.project.util;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class EntityManagerFactoryUtil {
    private static final EntityManagerFactory INSTANCE;

    static {
        INSTANCE = Persistence.createEntityManagerFactory("weatherPersistenceUnit");
    }

    public static EntityManagerFactory getInstance() {
        return INSTANCE;
    }
}
