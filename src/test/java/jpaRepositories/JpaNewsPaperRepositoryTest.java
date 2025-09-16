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
import java.util.Optional;

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
    void setup() {
    	sessionFactory.getSchemaManager().truncateMappedObjects();
        entityManager = sessionFactory.createEntityManager();
        repository = new JpaNewsPaperRepository(entityManager);
        entityManager.getTransaction().begin();
    }

    @AfterAll
    static void tearDown() {
    	sessionFactory.close();
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
    
    @Test
    @DisplayName("getNewspaper() should return an Optional containing the newspaper if it exists")
    void testGetNewspaper_Found() {
        NewsPaper np = new NewsPaper("Gazzetta dello Sport");
        entityManager.persist(np);
        entityManager.getTransaction().commit();

        Optional<NewsPaper> result = repository.getNewspaper("Gazzetta dello Sport");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Gazzetta dello Sport");
    }
    
    @Test
    @DisplayName("getNewspaper() should return Optional.empty if no newspaper with the given name exists")
    void testGetNewspaper_NotFound() {
        entityManager.getTransaction().commit();
        entityManager.clear();

        Optional<NewsPaper> result = repository.getNewspaper("NonExistent");

        assertThat(result).isEmpty();
    }
    
    @Test
    @DisplayName("saveNewsPaper() should persist the newspaper")
    void testSaveNewsPaper() {
    	NewsPaper np = new NewsPaper("Gazzetta dello Sport");

        repository.saveNewsPaper(np);
        entityManager.getTransaction().commit();
        
        entityManager.clear();

        List<NewsPaper> result = entityManager
        		.createQuery("SELECT n FROM NewsPaper n WHERE n.name = :name", NewsPaper.class)
                .setParameter("name", "Gazzetta dello Sport")
                .getResultList();

        assertThat(result).hasSize(1);
		assertThat(result.get(0).getName()).isEqualTo("Gazzetta dello Sport");
	}

}
