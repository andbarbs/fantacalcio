package dal.repository.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
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

	@BeforeAll
	static void initializeSessionFactory() {
		try {
			StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
					.configure("hibernate-test.cfg.xml").build();

			Metadata metadata = new MetadataSources(serviceRegistry).addAnnotatedClass(MatchDaySerieA.class)
					.addAnnotatedClass(FantaUser.class).addAnnotatedClass(FantaTeam.class)
					.addAnnotatedClass(League.class).addAnnotatedClass(NewsPaper.class)
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
	}

	@AfterAll
	static void tear() {
		sessionFactory.close();
	}

	@Test
	@DisplayName("getAllMatchDays() on an empty table")
	public void testGetAllMatchDaysWhenNoMatchDaysExist() {
		EntityManager repositorySession = sessionFactory.createEntityManager();
		assertThat(matchDayRepository.getAllMatchDays()).isEmpty();
		repositorySession.close();
	}

	@Test
	@DisplayName("getAllMatchDays() when two days have been persisted")
	public void testGetAllMatchDaysWhenTwoMatchDaysExist() {
		
		sessionFactory.inTransaction(session -> {
			session.persist(new MatchDaySerieA("prima giornata", LocalDate.of(2020, 1, 12), 1));
			session.persist(new MatchDaySerieA("seconda giornata", LocalDate.of(2020, 1, 19), 2));
		});

		EntityManager repositorySession = sessionFactory.createEntityManager();
		assertThat(matchDayRepository.getAllMatchDays()).containsExactly(
				new MatchDaySerieA("prima giornata", LocalDate.of(2020, 1, 12), 1),
				new MatchDaySerieA("seconda giornata", LocalDate.of(2020, 1, 19), 2));
		repositorySession.close();
	}

	@Test
	@DisplayName("getPreviousMatchDay() when previous day doesn't exist")
	public void testGetPreviousMatchDayWhenNoPreviousMatchDayExists() {

		LocalDate matchDate = LocalDate.of(2020, 1, 12);

		sessionFactory.inTransaction(session -> {
			session.persist(new MatchDaySerieA("prima giornata", matchDate, 1));
		});

		assertThat(matchDayRepository.getPreviousMatchDay(matchDate).isEmpty()).isTrue();

	}

	@Test
	@DisplayName("getPreviousMatchDay() when previous day exists")
	public void testGetPreviousMatchDayWhenPreviousMatchDayExists() {

		LocalDate secondDate = LocalDate.of(2020, 1, 19);

		sessionFactory.inTransaction(session -> {
			session.persist(new MatchDaySerieA("prima giornata", LocalDate.of(2020, 1, 12), 1));
			session.persist(new MatchDaySerieA("seconda giornata", secondDate, 1));
		});

		assertThat(matchDayRepository.getPreviousMatchDay(secondDate).isPresent()).isTrue();

	}

	@Test
	@DisplayName("getPreviousMatchDay() when many previous days exist")
	public void testGetPreviousMatchDayWhenMultiplePreviousMatchDayExist() {

		MatchDaySerieA previousDay = new MatchDaySerieA("seconda giornata", LocalDate.of(2020, 1, 19),1 );
		LocalDate lastDate = LocalDate.of(2020, 1, 26);

		sessionFactory.inTransaction(session -> {
			session.persist(new MatchDaySerieA("prima giornata", LocalDate.of(2020, 1, 12),1 ));
			session.persist(previousDay);
			session.persist(new MatchDaySerieA("terza giornata", lastDate, 1));
		});

		assertThat(matchDayRepository.getPreviousMatchDay(lastDate).get()).isEqualTo(previousDay);

	}

	@Test
	@DisplayName("getNextMatchDay() when next day doesn't exist")
	public void testGetNextMatchDayWhenNoNextMatchDayExists() {

		LocalDate matchDate = LocalDate.of(2020, 1, 12);

		sessionFactory.inTransaction(session -> {
			session.persist(new MatchDaySerieA("ultima giornata", matchDate, 1));
		});

		assertThat(matchDayRepository.getNextMatchDay(matchDate).isEmpty()).isTrue();

	}

	@Test
	@DisplayName("getNextMatchDay() when next day exists")
	public void testGetNextMatchDayWhenNextMatchDayExists() {

		LocalDate firstDate = LocalDate.of(2020, 1, 12);

		sessionFactory.inTransaction(session -> {
			session.persist(new MatchDaySerieA("prima giornata", firstDate, 1));
			session.persist(new MatchDaySerieA("seconda giornata", LocalDate.of(2020, 1, 19),1 ));
		});

		assertThat(matchDayRepository.getNextMatchDay(firstDate).isPresent()).isTrue();

	}

	@Test
	@DisplayName("getNextMatchDay() when many next days exist")
	public void testGetNextMatchDayWhenMultipleNextMatchDayExist() {

		LocalDate firstDate = LocalDate.of(2020, 1, 12);
		MatchDaySerieA nextDay = new MatchDaySerieA("seconda giornata", LocalDate.of(2020, 1, 19), 1);
		
		sessionFactory.inTransaction(session -> {
			session.persist(new MatchDaySerieA("prima giornata", firstDate, 1));
			session.persist(nextDay);
			session.persist(new MatchDaySerieA("seconda giornata", LocalDate.of(2020, 1, 26), 1));
		});

		assertThat(matchDayRepository.getNextMatchDay(firstDate).get()).isEqualTo(nextDay);
	}
	
	@Test
	@DisplayName("getMatchDay() when the match day does not exist")
	public void testGetMatchDayWhenNotExists() {
	    LocalDate date = LocalDate.of(2020, 1, 12);

	    sessionFactory.inTransaction(session -> {
	        session.persist(new MatchDaySerieA("prima giornata", date.plusDays(7), 1));
	    });

	    assertThat(matchDayRepository.getMatchDay(date)).isEmpty();
	}

	@Test
	@DisplayName("getMatchDay() when the match day exists")
	public void testGetMatchDayWhenExists() {
	    LocalDate date = LocalDate.of(2020, 1, 12);
	    MatchDaySerieA expected = new MatchDaySerieA("prima giornata", date, 1);

	    sessionFactory.inTransaction(session -> {
	        session.persist(expected);
	    });

	    assertThat(matchDayRepository.getMatchDay(date))
	        .isPresent()
	        .get()
	        .isEqualTo(expected);
	}

	@Test
	@DisplayName("getMatchDay() when multiple days exist but only one matches")
	public void testGetMatchDayWhenMultipleDaysExist() {
	    LocalDate matchDate = LocalDate.of(2020, 1, 19);
	    MatchDaySerieA expected = new MatchDaySerieA("seconda giornata", matchDate, 1);

	    sessionFactory.inTransaction(session -> {
	        session.persist(new MatchDaySerieA("prima giornata", LocalDate.of(2020, 1, 12), 1));
	        session.persist(expected);
	        session.persist(new MatchDaySerieA("terza giornata", LocalDate.of(2020, 1, 26), 1));
	    });

	    assertThat(matchDayRepository.getMatchDay(matchDate))
	        .isPresent()
	        .get()
	        .isEqualTo(expected);
	}

	@Test
	@DisplayName("saveMatch() should persist a match")
	void testSaveMatch() {
		FantaUser admin = new FantaUser("admin@" + "L001" + ".com", "pwd");
		NewsPaper np = new NewsPaper("Gazzetta " + "L001");
		League league = new League(admin, "League " + "L001", np, "L001");
		FantaUser user1 = new FantaUser("a@a.com", "pwd");
		FantaTeam t1 = new FantaTeam("Team A", league, 0, user1, Set.of());
		FantaUser user2 = new FantaUser("b@b.com", "pwd");
		FantaTeam t2 = new FantaTeam("Team B", league, 0, user2, Set.of());

		entityManager.getTransaction().begin();
		entityManager.persist(admin);
		entityManager.persist(np);
		entityManager.persist(league);
		entityManager.persist(user1);
		entityManager.persist(user2);
		entityManager.persist(t1);
		entityManager.persist(t2);

		MatchDaySerieA matchDay = new MatchDaySerieA("MD1", LocalDate.now(), 1);
		
		matchDayRepository.saveMatchDay(matchDay);
		entityManager.getTransaction().commit();

		sessionFactory.inTransaction((Session session) -> {
			List<MatchDaySerieA> result = session.createQuery("from MatchDaySerieA", MatchDaySerieA.class).getResultList();
			assertThat(result.size()).isEqualTo(1);
			MatchDaySerieA resultMatch = result.get(0);
			assertThat(resultMatch.getName()).isEqualTo("MD1");
			assertThat(resultMatch.getDate()).isEqualTo(LocalDate.now());
		});
	}
	

}