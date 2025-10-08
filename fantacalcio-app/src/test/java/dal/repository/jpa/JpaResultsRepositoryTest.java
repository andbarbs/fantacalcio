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
import java.time.LocalDate;
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
					.addAnnotatedClass(Match.class).addAnnotatedClass(MatchDaySerieA.class)
					.addAnnotatedClass(FantaTeam.class).addAnnotatedClass(FantaUser.class)
					.addAnnotatedClass(League.class).addAnnotatedClass(NewsPaper.class)
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
		entityManager.getTransaction().begin();

		MatchDaySerieA matchDay = new MatchDaySerieA("MD1", LocalDate.now(), 1);
		FantaUser admin = new FantaUser("admin@l001.com", "pwd");
		NewsPaper np = new NewsPaper("Gazzetta L001");
		League league = new League(admin, "League L001", np, "L001");
		FantaUser user1 = new FantaUser("a@a.com", "pwd");
		FantaUser user2 = new FantaUser("b@b.com", "pwd");
		FantaTeam t1 = new FantaTeam("Team A", league, 0, user1, Set.of());
		FantaTeam t2 = new FantaTeam("Team B", league, 0, user2, Set.of());

		entityManager.persist(matchDay);
		entityManager.persist(admin);
		entityManager.persist(np);
		entityManager.persist(league);
		entityManager.persist(user1);
		entityManager.persist(user2);
		entityManager.persist(t1);
		entityManager.persist(t2);

		match = new Match(matchDay, t1, t2);
		entityManager.persist(match);

		entityManager.getTransaction().commit();
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

		sessionFactory.inTransaction((Session session) -> {
			List<Result> results = session.createQuery("from Result", Result.class).getResultList();
			assertThat(results).containsExactly(result);
		});
	}

	@Test
	@DisplayName("getResult() should return the persisted result")
	void testGetResultWhenExists() {
		Result result = new Result(2.0, 2.0, 1, 1, match);

		entityManager.getTransaction().begin();
		entityManager.persist(result);
		entityManager.getTransaction().commit();

		Optional<Result> retrieved = resultsRepository.getResult(match);
		assertTrue(retrieved.isPresent());
		assertThat(retrieved.get()).isEqualTo(result);
	}

	@Test
	@DisplayName("getResult() should return empty if no result exists")
	void testGetResultWhenNotExists() {
		Optional<Result> retrieved = resultsRepository.getResult(match);
		assertFalse(retrieved.isPresent());
	}
}
