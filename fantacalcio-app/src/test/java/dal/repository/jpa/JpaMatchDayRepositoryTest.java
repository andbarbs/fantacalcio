package dal.repository.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import domain.*;
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

import domain.MatchDay;
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

			Metadata metadata = new MetadataSources(serviceRegistry).addAnnotatedClass(MatchDay.class)
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
        entityManager.getTransaction().begin();
		assertThat(matchDayRepository.getAllMatchDays(league)).isEmpty();
        entityManager.getTransaction().commit();
        entityManager.clear();
	}


	@Test
	@DisplayName("getAllMatchDays() when two days have been persisted")
	public void testGetAllMatchDaysWhenTwoMatchDaysExist() {
		MatchDay matchDay1 = new MatchDay("prima giornata", 1, MatchDay.Status.FUTURE, league);
        MatchDay matchDay2 = new MatchDay("seconda giornata", 2, MatchDay.Status.FUTURE,league);
		sessionFactory.inTransaction(session -> {
			session.persist(matchDay1);
			session.persist(matchDay2);
		});

        entityManager.getTransaction().begin();
        List<MatchDay> matchDays = matchDayRepository.getAllMatchDays(league);
        entityManager.getTransaction().commit();
        entityManager.clear();
		assertThat(matchDays).containsExactly(
                matchDay1,
                matchDay2);
	}



	@Test
	@DisplayName("getPreviousMatchDay() when previous day doesn't exist")
	public void testGetPreviousMatchDayWhenNoPreviousMatchDayExists() {


		sessionFactory.inTransaction(session -> {
			session.persist(new MatchDay("prima giornata", 1, MatchDay.Status.FUTURE, league));
		});

        entityManager.getTransaction().begin();
        Optional<MatchDay> previousMatchDay = matchDayRepository.getPreviousMatchDay(league);
        entityManager.getTransaction().commit();
        entityManager.clear();
		assertThat(previousMatchDay.isEmpty()).isTrue();

	}

	@Test
	@DisplayName("getPreviousMatchDay() when previous day exists")
	public void testGetPreviousMatchDayWhenPreviousMatchDayExists() {

        MatchDay pastMatchDay = new MatchDay("prima giornata", 1, MatchDay.Status.PAST, league);
		sessionFactory.inTransaction(session -> {
			session.persist(pastMatchDay);
			session.persist(new MatchDay("seconda giornata", 2, MatchDay.Status.FUTURE, league));
		});

        entityManager.getTransaction().begin();
        Optional<MatchDay> previousMatchDay = matchDayRepository.getPreviousMatchDay(league);
        entityManager.getTransaction().commit();
        entityManager.clear();
        assertThat(previousMatchDay.isPresent()).isTrue();
        assertThat(previousMatchDay).hasValue(pastMatchDay);
	}

	@Test
	@DisplayName("getPreviousMatchDay() when many previous days exist")
	public void testGetPreviousMatchDayWhenMultiplePreviousMatchDayExist() {

		MatchDay previousDay = new MatchDay("seconda giornata", 2, MatchDay.Status.PAST, league);

		sessionFactory.inTransaction(session -> {
			session.persist(new MatchDay("prima giornata", 1, MatchDay.Status.PAST, league ));
			session.persist(previousDay);
			session.persist(new MatchDay("terza giornata", 3, MatchDay.Status.FUTURE, league));
		});

        entityManager.getTransaction().begin();
        Optional<MatchDay> previousMatchDay = matchDayRepository.getPreviousMatchDay(league);
        entityManager.getTransaction().commit();
        entityManager.clear();
		assertThat(previousMatchDay).hasValue(previousDay);

	}

	@Test
	@DisplayName("getNextMatchDay() when next day doesn't exist")
	public void testGetNextMatchDayWhenNoNextMatchDayExists() {

		sessionFactory.inTransaction(session -> {
			session.persist(new MatchDay("ultima giornata", 20, MatchDay.Status.PAST, league));
		});

        entityManager.getTransaction().begin();
        Optional<MatchDay> nextMatchDay = matchDayRepository.getNextMatchDay(league);
        entityManager.getTransaction().commit();
        entityManager.clear();
		assertThat(nextMatchDay.isEmpty()).isTrue();

	}

	@Test
	@DisplayName("getNextMatchDay() when next day exists")
	public void testGetNextMatchDayWhenNextMatchDayExists() {

        MatchDay expected = new MatchDay("seconda giornata", 2, MatchDay.Status.FUTURE, league);
		sessionFactory.inTransaction(session -> {
			session.persist(new MatchDay("prima giornata", 1, MatchDay.Status.PAST, league));
			session.persist(expected);
		});
        entityManager.getTransaction().begin();
        Optional<MatchDay> nextMatchDay = matchDayRepository.getNextMatchDay(league);
        entityManager.getTransaction().commit();
        entityManager.clear();
		assertThat(nextMatchDay).hasValue(expected);

	}

	@Test
	@DisplayName("getNextMatchDay() when many next days exist")
	public void testGetNextMatchDayWhenMultipleNextMatchDayExist() {

		MatchDay nextDay = new MatchDay("seconda giornata", 2, MatchDay.Status.FUTURE, league);
		
		sessionFactory.inTransaction(session -> {
			session.persist(new MatchDay("prima giornata", 1, MatchDay.Status.PAST, league));
			session.persist(nextDay);
			session.persist(new MatchDay("terza giornata", 3, MatchDay.Status.FUTURE, league ));
		});
        entityManager.getTransaction().begin();
        Optional<MatchDay> nextMatchDay = matchDayRepository.getNextMatchDay(league);
        entityManager.getTransaction().commit();
        entityManager.clear();
        assertThat(nextMatchDay).hasValue(nextDay);
	}
	
	@Test
	@DisplayName("getMatchDay() when the match day does not exist")
	public void testGetMatchDayWhenNotExists() {

	    sessionFactory.inTransaction(session -> {
	        session.persist(new MatchDay("prima giornata", 1, MatchDay.Status.FUTURE, league));
	    });
        entityManager.getTransaction().begin();
	    assertThat(matchDayRepository.getMatchDay(league)).isEmpty();
        entityManager.getTransaction().commit();
        entityManager.clear();
	}

	@Test
	@DisplayName("getMatchDay() when the match day exists")
	public void testGetMatchDayWhenExists() {
	    MatchDay expected = new MatchDay("prima giornata", 1, MatchDay.Status.PRESENT, league);

	    sessionFactory.inTransaction(session -> {
	        session.persist(expected);
	    });

        entityManager.getTransaction().begin();
        Optional<MatchDay> matchDay = matchDayRepository.getMatchDay(league);
        entityManager.getTransaction().commit();
        entityManager.clear();
	    assertThat(matchDay).hasValue(expected);
	}

	@Test
	@DisplayName("getMatchDay() when multiple days exist but only one matches")
	public void testGetMatchDayWhenMultipleDaysExist() {
	    MatchDay expected = new MatchDay("seconda giornata", 2, MatchDay.Status.PRESENT, league);

	    sessionFactory.inTransaction(session -> {
	        session.persist(new MatchDay("prima giornata", 1,  MatchDay.Status.PAST, league));
	        session.persist(expected);
	        session.persist(new MatchDay("terza giornata", 3,  MatchDay.Status.FUTURE, league));
	    });

        entityManager.getTransaction().begin();
        Optional<MatchDay> matchDay = matchDayRepository.getMatchDay(league);
        entityManager.getTransaction().commit();
        entityManager.clear();
        assertThat(matchDay).hasValue(expected);
	}

	@Test
	@DisplayName("saveMatchDay() should persist a match")
	void testSaveMatchDay() {


		MatchDay matchDay = new MatchDay("MD1", 1, MatchDay.Status.FUTURE, league);

        entityManager.getTransaction().begin();
		matchDayRepository.saveMatchDay(matchDay);
		entityManager.getTransaction().commit();
        entityManager.clear();

        assertThat(entityManager.createQuery("FROM MatchDay", MatchDay.class).getResultStream().findFirst()).hasValue(matchDay);
	}
	

}