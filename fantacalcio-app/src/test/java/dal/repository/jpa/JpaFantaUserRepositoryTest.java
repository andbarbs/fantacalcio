package dal.repository.jpa;

import jakarta.persistence.EntityManager;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.hibernate.Session;
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
	@DisplayName("can persist a FantaUser instance to the database")
	void testSaveFantaUser() {

		// GIVEN a User is instatiated
		FantaUser user = new FantaUser("john@example.com", "secret");

		// WHEN the SUT is used to persist it
		entityManager.getTransaction().begin();
		fantaUserRepository.saveFantaUser(user);
		entityManager.getTransaction().commit();
		entityManager.clear();

		// THEN the User is persisted to the database
		assertThat(sessionFactory.fromTransaction(
				(Session em) -> em.createQuery("FROM FantaUser", FantaUser.class).getResultStream().toList()))
				.containsExactly(user);
	}
	
	@Nested
	@DisplayName("can look up a FantaUser from the database")
	class Retrieval {	
		
		@Test
		@DisplayName("when the given credentials exist in the database")
		void testGetUser_Found() {
			
			// GIVEN the test User is manually persisted
			FantaUser user = new FantaUser("anna@example.com", "mypassword");
			sessionFactory.inTransaction(session -> session.persist(user));
			
			// WHEN the SUT is used to retrieve the test User
			entityManager.getTransaction().begin();
			Optional<FantaUser> result = fantaUserRepository.getUser("anna@example.com", "mypassword");
			entityManager.getTransaction().commit();
			entityManager.clear();
			
			// THEN the expected User is retrieved
			assertThat(result).hasValue(user);
		}
		
		@Test
		@DisplayName("when the given credentials do not exist in the database")
		void testGetUser_NotFound() {
			
			// GIVEN no User has been persisted with some credentials
			FantaUser user = new FantaUser("anna@example.com", "mypassword");
			sessionFactory.inTransaction(session -> session.persist(user));
			
			// WHEN the SUT is used to retrieve a User based on those credentials
			entityManager.getTransaction().begin();
			Optional<FantaUser> result = fantaUserRepository.getUser("nonexistent@example.com", "wrong");
			entityManager.getTransaction().commit();
			entityManager.clear();
			
			// THEN an empty Optional is returned
			assertThat(result).isEmpty();
		}    
	}

}

