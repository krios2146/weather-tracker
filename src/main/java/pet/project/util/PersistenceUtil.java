package pet.project.util;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.HashMap;
import java.util.Map;

public class PersistenceUtil {
    private static final EntityManagerFactory ENTITY_MANAGER_FACTORY_INSTANCE;

    static {
        Map<String, String> config = new HashMap<>();

        String jdbcUrl = System.getenv("JAKARTA_PERSISTENCE_JDBC_URL");
        String jdbcUser = System.getenv("JAKARTA_PERSISTENCE_JDBC_USER");
        String jdbcPassword = System.getenv("JAKARTA_PERSISTENCE_JDBC_PASSWORD");
        String hibernateDdlAuto = System.getenv("HIBERNATE_DDL_AUTO");

        config.put("jakarta.persistence.jdbc.url", jdbcUrl);
        config.put("jakarta.persistence.jdbc.user", jdbcUser);
        config.put("jakarta.persistence.jdbc.password", jdbcPassword);
        config.put("hibernate.hbm2ddl.auto", hibernateDdlAuto);

        ENTITY_MANAGER_FACTORY_INSTANCE = Persistence.createEntityManagerFactory("weatherPersistenceUnit", config);
    }

    public static EntityManagerFactory getEntityManagerFactory() {
        return ENTITY_MANAGER_FACTORY_INSTANCE;
    }
}
