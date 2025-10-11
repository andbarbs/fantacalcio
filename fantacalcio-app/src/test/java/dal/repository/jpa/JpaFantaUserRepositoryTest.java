package dal.repository.jpa;

import jakarta.persistence.EntityManager;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.junit.jupiter.api.*;

import domain.FantaUser;

class JpaFantaUserRepositoryTest {

    private static SessionFactory sessionFactory;
    private EntityManager entityManager;
    private JpaFantaUserRepository fantaUserRepository;

    //TODO è corretto?
    @BeforeAll
    static void setUpAll() {
        StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                .configure("hibernate-test.cfg.xml") // H2 config
                .build();

        Metadata metadata = new MetadataSources(serviceRegistry)
                .addAnnotatedClass(FantaUser.class)
                .getMetadataBuilder()
                .build();

        sessionFactory = metadata.getSessionFactoryBuilder().build();
    }

    @BeforeEach
    void setup() {
    	sessionFactory.getSchemaManager().truncateMappedObjects();
        entityManager = sessionFactory.createEntityManager();
        fantaUserRepository = new JpaFantaUserRepository(entityManager);
    }

    @AfterAll
    static void tearDown() {
    	sessionFactory.close();
    }

    @Test
    @DisplayName("saveFantaUser() should persist a new user")
    void testSaveFantaUser() {
        FantaUser user = new FantaUser("john@example.com", "secret");

        entityManager.getTransaction().begin();
        fantaUserRepository.saveFantaUser(user);
        entityManager.getTransaction().commit();

        entityManager.clear();

        List<FantaUser> result = entityManager
                .createQuery("SELECT u FROM FantaUser u WHERE u.email = :email", FantaUser.class)
                .setParameter("email", "john@example.com")
                .getResultList();
        
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("john@example.com");
        assertThat(result.get(0).getPassword()).isEqualTo("secret");
    }

    @Test
    @DisplayName("getUser() should return a user if email and password match")
    void testGetUser_Found() {
        FantaUser user = new FantaUser("anna@example.com", "mypassword");
        sessionFactory.inTransaction(session -> {
            session.save(user);
        });
        entityManager.getTransaction().begin();
        Optional<FantaUser> result = fantaUserRepository.getUser("anna@example.com", "mypassword");
        entityManager.getTransaction().commit();
        entityManager.clear();

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("anna@example.com");
    }

    @Test
    @DisplayName("getUser() should return empty if credentials do not match")
    void testGetUser_NotFound() {
        entityManager.getTransaction().begin();
        Optional<FantaUser> result = fantaUserRepository.getUser("nonexistent@example.com", "wrong");
        entityManager.getTransaction().commit();
        entityManager.clear();

        assertThat(result).isEmpty();
    }
    
}

