package dal.repository.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.junit.jupiter.api.*;

import domain.*;
import jakarta.persistence.EntityManager;
import java.util.Optional;

@DisplayName("tests for HibernateResultsRepository")
class JpaResultsRepositoryTest {

	private static SessionFactory sessionFactory;
	private EntityManager entityManager;
	private JpaResultsRepository resultsRepository;

	// setup entities
	private League league;
	private MatchDay matchDay;
	private FantaTeam t1;
	private FantaTeam t2;
	private Match match;

	@BeforeAll
	static void initializeSessionFactory() {
		try {
			StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
					.configure("hibernate-test.cfg.xml").build();

			Metadata metadata = new MetadataSources(serviceRegistry)
					.addAnnotatedClass(FantaUser.class)
					.addAnnotatedClass(League.class)
					.addAnnotatedClass(MatchDay.class)
					.addAnnotatedClass(FantaTeam.class)
					.addAnnotatedClass(Match.class)
					.addAnnotatedClass(Result.class)
					.addAnnotatedClass(Contract.class)
					.addAnnotatedClass(Player.class).
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

		// GIVEN a Result's ancillary entities are persisted
		FantaUser admin = new FantaUser("admin@l001.com", "pwd");
		league = new League(admin, "League L001", "L001");
        matchDay = new MatchDay("MD1", 1, MatchDay.Status.FUTURE, league);
		FantaUser user1 = new FantaUser("a@a.com", "pwd");
		FantaUser user2 = new FantaUser("b@b.com", "pwd");
		t1 = new FantaTeam("Team A", league, 0, user1, null);
		t2 = new FantaTeam("Team B", league, 0, user2, null);
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
	@DisplayName("can persist a Result instance to the database")
	void testSaveResult() {
		
		// GIVEN the SUT is used to persist a Result
		Result result = new Result(3.0, 1.0, 2, 0, match);
		entityManager.getTransaction().begin();
		resultsRepository.saveResult(result);
		entityManager.getTransaction().commit();
        entityManager.clear();

        // THEN the Result is present in the database
		assertThat(sessionFactory
				.fromTransaction((Session session) -> session.createQuery("FROM Result r JOIN FETCH r.match m "
						+ "JOIN FETCH m.team1 t1 JOIN FETCH m.team2 t2 JOIN FETCH t1.fantaManager JOIN FETCH t2.fantaManager "
						+ "JOIN FETCH m.matchDay day JOIN FETCH day.league league JOIN FETCH league.admin", Result.class).getResultList()))
				.containsExactly(result);
	}
	
	@Nested
	@DisplayName("can look up a Result in the database")
	class Retrieval {
		
		@Test
		@DisplayName("when a Result exists in the database for a given Match")
		void testGetResultWhenExists() {
			
			// GIVEN a Result is manually persisted to the database
			Result result = new Result(2.0, 2.0, 1, 1, match);		
			
			Match match2 = new Match(matchDay, t1, t2);	
			Result result2 = new Result(2.0, 2.0, 1, 1, match2);	
			
			sessionFactory.inTransaction((Session session) -> {
				session.persist(result);
				session.persist(match2);
				session.persist(result2);
			});
			
			// WHEN the SUT is used to retrieve Results for a given Match
			entityManager.getTransaction().begin();
			Optional<Result> retrieved = resultsRepository.getResultFor(match);
			entityManager.getTransaction().commit();
			entityManager.clear();
			
			// THEN only the expected Result is retrieved
			assertThat(retrieved).hasValue(result);
		}
		
		@Test
		@DisplayName("when no Result exists in the database for a given Match")
		void testGetResultWhenNotExists() {
			
			// GIVEN no Result has been persisted for a given Match
			Result result = new Result(2.0, 2.0, 1, 1, match);		
			
			Match match2 = new Match(matchDay, t1, t2);
			
			sessionFactory.inTransaction((Session session) -> {
				session.persist(result);
				session.persist(match2);
			});
			
			// WHEN the SUT is used to retrieve a non-exixtent Result
			entityManager.getTransaction().begin();
			Optional<Result> retrieved = resultsRepository.getResultFor(match2);
			entityManager.getTransaction().commit();
			entityManager.clear();
			
			// THEN an empty Optional is returned
			assertThat(retrieved).isEmpty();
		}
	}
}
