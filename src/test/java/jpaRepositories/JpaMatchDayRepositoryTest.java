package jpaRepositories;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
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

import domainModel.MatchDaySerieA;
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
			session.persist(new MatchDaySerieA("prima giornata", LocalDate.of(2020, 1, 12)));
			session.persist(new MatchDaySerieA("seconda giornata", LocalDate.of(2020, 1, 19)));
		});

		EntityManager repositorySession = sessionFactory.createEntityManager();
		assertThat(matchDayRepository.getAllMatchDays()).containsExactly(new MatchDaySerieA("prima giornata", LocalDate.of(2020, 1, 12)), new MatchDaySerieA("seconda giornata", LocalDate.of(2020, 1, 19)));
		repositorySession.close();
	}

	@Test
	@DisplayName("getPreviousMatchDay() when previous day doesn't exist")
	public void testGetPreviousMatchDayWhenNoPreviousMatchDayExists() {

		LocalDate matchDate = LocalDate.of(2020, 1, 12);

		sessionFactory.inTransaction(session -> {
			session.persist(new MatchDaySerieA("prima giornata", matchDate));
		});

		assertThat(matchDayRepository.getPreviousMatchDay(matchDate).isEmpty()).isTrue();

	}

	@Test
	@DisplayName("getPreviousMatchDay() when previous day exists")
	public void testGetPreviousMatchDayWhenPreviousMatchDayExists() {

		LocalDate secondDate = LocalDate.of(2020, 1, 19);

		sessionFactory.inTransaction(session -> {
			session.persist(new MatchDaySerieA("prima giornata", LocalDate.of(2020, 1, 12)));
			session.persist(new MatchDaySerieA("seconda giornata", secondDate));
		});

		assertThat(matchDayRepository.getPreviousMatchDay(secondDate).isPresent()).isTrue();

	}

	@Test
	@DisplayName("getPreviousMatchDay() when many previous days exist")
	public void testGetPreviousMatchDayWhenMultiplePreviousMatchDayExist() {

		MatchDaySerieA previousDay = new MatchDaySerieA("seconda giornata", LocalDate.of(2020, 1, 19));
		LocalDate lastDate = LocalDate.of(2020, 1, 26);

		sessionFactory.inTransaction(session -> {
			session.persist(new MatchDaySerieA("prima giornata", LocalDate.of(2020, 1, 12)));
			session.persist(previousDay);
			session.persist(new MatchDaySerieA("terza giornata", lastDate));
		});

		assertThat(matchDayRepository.getPreviousMatchDay(lastDate).get()).isEqualTo(previousDay);

	}

	@Test
	@DisplayName("getNextMatchDay() when next day doesn't exist")
	public void testGetNextMatchDayWhenNoNextMatchDayExists() {

		LocalDate matchDate = LocalDate.of(2020, 1, 12);

		sessionFactory.inTransaction(session -> {
			session.persist(new MatchDaySerieA("ultima giornata", matchDate));
		});

		assertThat(matchDayRepository.getNextMatchDay(matchDate).isEmpty()).isTrue();

	}

	@Test
	@DisplayName("getNextMatchDay() when next day exists")
	public void testGetNextMatchDayWhenNextMatchDayExists() {

		LocalDate firstDate = LocalDate.of(2020, 1, 12);

		sessionFactory.inTransaction(session -> {
			session.persist(new MatchDaySerieA("prima giornata", firstDate));
			session.persist(new MatchDaySerieA("seconda giornata", LocalDate.of(2020, 1, 19)));
		});

		assertThat(matchDayRepository.getNextMatchDay(firstDate).isPresent()).isTrue();

	}

	@Test
	@DisplayName("getNextMatchDay() when many next days exist")
	public void testGetNextMatchDayWhenMultipleNextMatchDayExist() {

		LocalDate firstDate = LocalDate.of(2020, 1, 12);
		MatchDaySerieA nextDay = new MatchDaySerieA("seconda giornata", LocalDate.of(2020, 1, 19));
		
		sessionFactory.inTransaction(session -> {
			session.persist(new MatchDaySerieA("prima giornata", firstDate));
			session.persist(nextDay);
			session.persist(new MatchDaySerieA("seconda giornata", LocalDate.of(2020, 1, 26)));
		});

		assertThat(matchDayRepository.getNextMatchDay(firstDate).get()).isEqualTo(nextDay);
	}

}