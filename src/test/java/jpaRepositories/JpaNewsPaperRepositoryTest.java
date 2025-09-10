package jpaRepositories;

import domainModel.NewsPaper;
import jakarta.persistence.EntityManager;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JpaNewsPaperRepositoryTest {

    private static SessionFactory sessionFactory;
    private EntityManager entityManager;
    private JpaNewsPaperRepository repository;

    @BeforeAll
    static void setUpAll() {
        StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                .configure("hibernate-test.cfg.xml") // H2 config
                .build();

        Metadata metadata = new MetadataSources(serviceRegistry)
                .addAnnotatedClass(NewsPaper.class)
                .getMetadataBuilder()
                .build();

        sessionFactory = metadata.getSessionFactoryBuilder().build();
    }

    @BeforeEach
    void setUp() {
        entityManager = sessionFactory.createEntityManager();
        repository = new JpaNewsPaperRepository(entityManager);
        entityManager.getTransaction().begin();
    }

    @AfterEach
    void tearDown() {
        if (entityManager.getTransaction().isActive()) {
            entityManager.getTransaction().rollback();
        }
        entityManager.close();
    }

    @AfterAll
    static void tearDownAll() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }

    @Test
    @DisplayName("getAllNewspapers() should return all persisted newspapers")
    void testGetAllNewspapers() {
        
        NewsPaper np1 = new NewsPaper("Gazzetta dello Sport");
        NewsPaper np2 = new NewsPaper("Corriere dello Sport");
        NewsPaper np3 = new NewsPaper("Tuttosport");

        entityManager.persist(np1);
        entityManager.persist(np2);
        entityManager.persist(np3);
        entityManager.getTransaction().commit();

        List<NewsPaper> result = repository.getAllNewspapers();

        assertThat(result)
                .hasSize(3)
                .extracting(NewsPaper::getName)
                .containsExactlyInAnyOrder("Gazzetta dello Sport", "Corriere dello Sport", "Tuttosport");
    }
}
