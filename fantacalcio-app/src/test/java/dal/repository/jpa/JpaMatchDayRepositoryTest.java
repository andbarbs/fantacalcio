package dal.repository.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import domain.Contract;
import domain.FantaTeam;
import domain.FantaUser;
import domain.League;
import domain.MatchDaySerieA;
import domain.Player;
import jakarta.persistence.EntityManager;

class JpaMatchDayRepositoryTest {

	private static SessionFactory sessionFactory;

	private JpaMatchDayRepository matchDayRepository;

	private EntityManager entityManager;
    private FantaUser manager;
    private League league;

	@BeforeAll
	static void initializeSessionFactory() {
		try {
			StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
					.configure("hibernate-test.cfg.xml").build();

			Metadata metadata = new MetadataSources(serviceRegistry).addAnnotatedClass(MatchDaySerieA.class)
					.addAnnotatedClass(FantaUser.class).addAnnotatedClass(FantaTeam.class)
					.addAnnotatedClass(League.class)
					.addAnnotatedClass(Contract.class).addAnnotatedClass(Player.class)
					.getMetadataBuilder().build();

			sessionFactory = metadata.getSessionFactoryBuilder().build();

		} catch (Throwable ex) {
			throw new ExceptionInInitializerError(ex);
		}
	}

	@BeforeEach
	void setup() {
		// ensures tests work on empty tables without having to recreate a
		// SessionFactory instance
		sessionFactory.getSchemaManager().truncateMappedObjects();

		// Instantiates the SUT using the static SessionFactory
		entityManager = sessionFactory.createEntityManager();
		matchDayRepository = new JpaMatchDayRepository(entityManager);
        sessionFactory.inTransaction(t -> {
            manager = new FantaUser("manager@example.com", "securePass");
            t.persist(manager);
            league = new League(manager, "Serie A", "code");
            t.persist(league);
        });
	}

	@AfterAll
	static void tear() {
		sessionFactory.close();
	}

	@Test
	@DisplayName("getAllMatchDays() on an empty table")
	public void testGetAllMatchDaysWhenNoMatchDaysExist() {
		EntityManager repositorySession = sessionFactory.createEntityManager();
		assertThat(matchDayRepository.getAllMatchDays(league)).isEmpty();
		repositorySession.close();
	}


	@Test
	@DisplayName("getAllMatchDays() when two days have been persisted")
	public void testGetAllMatchDaysWhenTwoMatchDaysExist() {
		
		sessionFactory.inTransaction(session -> {
			session.persist(new MatchDaySerieA("prima giornata", 1, MatchDaySerieA.Status.FUTURE, league));
			session.persist(new MatchDaySerieA("seconda giornata", 2, MatchDaySerieA.Status.FUTURE,league));
		});

		EntityManager repositorySession = sessionFactory.createEntityManager();
		assertThat(matchDayRepository.getAllMatchDays(league)).containsExactly(
                new MatchDaySerieA("prima giornata", 1, MatchDaySerieA.Status.FUTURE, league),
                new MatchDaySerieA("seconda giornata", 2, MatchDaySerieA.Status.FUTURE,league));
		repositorySession.close();
	}



	@Test
	@DisplayName("getPreviousMatchDay() when previous day doesn't exist")
	public void testGetPreviousMatchDayWhenNoPreviousMatchDayExists() {


		sessionFactory.inTransaction(session -> {
			session.persist(new MatchDaySerieA("prima giornata", 1, MatchDaySerieA.Status.FUTURE, league));
		});

		assertThat(matchDayRepository.getPreviousMatchDay(league).isEmpty()).isTrue();

	}

	@Test
	@DisplayName("getPreviousMatchDay() when previous day exists")
	public void testGetPreviousMatchDayWhenPreviousMatchDayExists() {

		sessionFactory.inTransaction(session -> {
			session.persist(new MatchDaySerieA("prima giornata", 1, MatchDaySerieA.Status.PAST, league));
			session.persist(new MatchDaySerieA("seconda giornata", 2, MatchDaySerieA.Status.FUTURE, league));
		});

		assertThat(matchDayRepository.getPreviousMatchDay(league).isPresent()).isTrue();

	}

	@Test
	@DisplayName("getPreviousMatchDay() when many previous days exist")
	public void testGetPreviousMatchDayWhenMultiplePreviousMatchDayExist() {

		MatchDaySerieA previousDay = new MatchDaySerieA("seconda giornata", 2, MatchDaySerieA.Status.PAST, league);

		sessionFactory.inTransaction(session -> {
			session.persist(new MatchDaySerieA("prima giornata", 1, MatchDaySerieA.Status.PAST, league ));
			session.persist(previousDay);
			session.persist(new MatchDaySerieA("terza giornata", 3, MatchDaySerieA.Status.FUTURE, league));
		});

		assertThat(matchDayRepository.getPreviousMatchDay(league).get()).isEqualTo(previousDay);

	}

	@Test
	@DisplayName("getNextMatchDay() when next day doesn't exist")
	public void testGetNextMatchDayWhenNoNextMatchDayExists() {

		sessionFactory.inTransaction(session -> {
			session.persist(new MatchDaySerieA("ultima giornata", 20, MatchDaySerieA.Status.PAST, league));
		});

		assertThat(matchDayRepository.getNextMatchDay(league).isEmpty()).isTrue();

	}

	@Test
	@DisplayName("getNextMatchDay() when next day exists")
	public void testGetNextMatchDayWhenNextMatchDayExists() {

		sessionFactory.inTransaction(session -> {
			session.persist(new MatchDaySerieA("prima giornata", 1, MatchDaySerieA.Status.PAST, league));
			session.persist(new MatchDaySerieA("seconda giornata", 2, MatchDaySerieA.Status.FUTURE, league));
		});

		assertThat(matchDayRepository.getNextMatchDay(league).isPresent()).isTrue();

	}

	@Test
	@DisplayName("getNextMatchDay() when many next days exist")
	public void testGetNextMatchDayWhenMultipleNextMatchDayExist() {

		MatchDaySerieA nextDay = new MatchDaySerieA("seconda giornata", 2, MatchDaySerieA.Status.FUTURE, league);
		
		sessionFactory.inTransaction(session -> {
			session.persist(new MatchDaySerieA("prima giornata", 1, MatchDaySerieA.Status.PAST, league));
			session.persist(nextDay);
			session.persist(new MatchDaySerieA("terza giornata", 3, MatchDaySerieA.Status.FUTURE, league ));
		});

		assertThat(matchDayRepository.getNextMatchDay(league).get()).isEqualTo(nextDay);
	}
	
	@Test
	@DisplayName("getMatchDay() when the match day does not exist")
	public void testGetMatchDayWhenNotExists() {

	    sessionFactory.inTransaction(session -> {
	        session.persist(new MatchDaySerieA("prima giornata", 1, MatchDaySerieA.Status.FUTURE, league));
	    });

	    assertThat(matchDayRepository.getMatchDay(league)).isEmpty();
	}

	@Test
	@DisplayName("getMatchDay() when the match day exists")
	public void testGetMatchDayWhenExists() {
	    MatchDaySerieA expected = new MatchDaySerieA("prima giornata", 1, MatchDaySerieA.Status.PRESENT, league);

	    sessionFactory.inTransaction(session -> {
	        session.persist(expected);
	    });

	    assertThat(matchDayRepository.getMatchDay(league))
	        .isPresent()
	        .get()
	        .isEqualTo(expected);
	}

	@Test
	@DisplayName("getMatchDay() when multiple days exist but only one matches")
	public void testGetMatchDayWhenMultipleDaysExist() {
	    MatchDaySerieA expected = new MatchDaySerieA("seconda giornata", 2, MatchDaySerieA.Status.PRESENT, league);

	    sessionFactory.inTransaction(session -> {
	        session.persist(new MatchDaySerieA("prima giornata", 1,  MatchDaySerieA.Status.PAST, league));
	        session.persist(expected);
	        session.persist(new MatchDaySerieA("terza giornata", 3,  MatchDaySerieA.Status.FUTURE, league));
	    });

	    assertThat(matchDayRepository.getMatchDay(league))
	        .isPresent()
	        .get()
	        .isEqualTo(expected);
	}

	@Test
	@DisplayName("saveMatch() should persist a match")
	void testSaveMatch() {
		FantaUser user1 = new FantaUser("a@a.com", "pwd");
		FantaTeam t1 = new FantaTeam("Team A", league, 0, user1, Set.of());
		FantaUser user2 = new FantaUser("b@b.com", "pwd");
		FantaTeam t2 = new FantaTeam("Team B", league, 0, user2, Set.of());

		entityManager.getTransaction().begin();
		entityManager.persist(league);
		entityManager.persist(user1);
		entityManager.persist(user2);
		entityManager.persist(t1);
		entityManager.persist(t2);

		MatchDaySerieA matchDay = new MatchDaySerieA("MD1", 1, MatchDaySerieA.Status.FUTURE, league);
		
		matchDayRepository.saveMatchDay(matchDay);
		entityManager.getTransaction().commit();

		sessionFactory.inTransaction((Session session) -> {
			List<MatchDaySerieA> result = session.createQuery("from MatchDaySerieA", MatchDaySerieA.class).getResultList();
			assertThat(result.size()).isEqualTo(1);
			MatchDaySerieA resultMatch = result.get(0);
			assertThat(resultMatch.getName()).isEqualTo("MD1");
			assertThat(resultMatch.getNumber()).isEqualTo(1);
            assertThat(resultMatch.getStatus()).isEqualTo(MatchDaySerieA.Status.FUTURE);
            assertThat(resultMatch.getLeague()).isEqualTo(league);
		});
	}
	

}