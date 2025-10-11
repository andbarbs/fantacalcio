package dal.repository.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.junit.jupiter.api.*;

import domain.*;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@DisplayName("tests for HibernateResultsRepository")
class JpaResultsRepositoryTest {

	private static SessionFactory sessionFactory;
	private EntityManager entityManager;
	private JpaResultsRepository resultsRepository;

	private Match match;

	@BeforeAll
	static void initializeSessionFactory() {
		try {
			StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
					.configure("hibernate-test.cfg.xml").build();

			Metadata metadata = new MetadataSources(serviceRegistry).addAnnotatedClass(Result.class)
					.addAnnotatedClass(Match.class).addAnnotatedClass(MatchDay.class)
					.addAnnotatedClass(FantaTeam.class).addAnnotatedClass(FantaUser.class)
					.addAnnotatedClass(League.class)
					.addAnnotatedClass(Contract.class).addAnnotatedClass(Player.class).
					getMetadataBuilder().build();

			sessionFactory = metadata.getSessionFactoryBuilder().build();

		} catch (Throwable ex) {
			throw new ExceptionInInitializerError(ex);
		}
	}

	@BeforeEach
	void setup() {
		sessionFactory.getSchemaManager().truncateMappedObjects();
		entityManager = sessionFactory.createEntityManager();
		resultsRepository = new JpaResultsRepository(entityManager);

		// Minimal setup for a Match and related entities

		FantaUser admin = new FantaUser("admin@l001.com", "pwd");
		League league = new League(admin, "League L001", "L001");
        MatchDay matchDay = new MatchDay("MD1", 1, MatchDay.Status.FUTURE, league);
		FantaUser user1 = new FantaUser("a@a.com", "pwd");
		FantaUser user2 = new FantaUser("b@b.com", "pwd");
		FantaTeam t1 = new FantaTeam("Team A", league, 0, user1, Set.of());
		FantaTeam t2 = new FantaTeam("Team B", league, 0, user2, Set.of());
        match = new Match(matchDay, t1, t2);

		sessionFactory.inTransaction(session -> {
            session.persist(admin);
            session.persist(league);
            session.persist(matchDay);
            session.persist(user1);
            session.persist(user2);
            session.persist(t1);
            session.persist(t2);
            session.persist(match);
        });


	}

	@AfterEach
	void tearDown() {
		entityManager.close();
	}

	@AfterAll
	static void tear() {
		sessionFactory.close();
	}

	@Test
	@DisplayName("saveResult() should persist a result")
	void testSaveResult() {
		Result result = new Result(3.0, 1.0, 2, 0, match);

		entityManager.getTransaction().begin();
		resultsRepository.saveResult(result);
		entityManager.getTransaction().commit();
        entityManager.clear();

		sessionFactory.inTransaction((Session session) -> {
			List<Result> results = session.createQuery("from Result", Result.class).getResultList();
			assertThat(results).containsExactly(result);
		});
	}

	@Test
	@DisplayName("getResult() should return the persisted result")
	void testGetResultWhenExists() {
		Result result = new Result(2.0, 2.0, 1, 1, match);

        sessionFactory.inTransaction((Session session) -> {
            session.persist(result);
        });
		entityManager.getTransaction().begin();
        Optional<Result> retrieved = resultsRepository.getResult(match);
		entityManager.getTransaction().commit();
        entityManager.clear();

		assertThat(retrieved).hasValue(result);

	}

	@Test
	@DisplayName("getResult() should return empty if no result exists")
	void testGetResultWhenNotExists() {
        entityManager.getTransaction().begin();
		Optional<Result> retrieved = resultsRepository.getResult(match);
        entityManager.getTransaction().commit();
        entityManager.clear();
		assertTrue(retrieved.isEmpty());
	}
}
