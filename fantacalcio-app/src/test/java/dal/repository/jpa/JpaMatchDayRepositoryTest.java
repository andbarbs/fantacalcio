package dal.repository.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

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

			Metadata metadata = new MetadataSources(serviceRegistry)
					.addAnnotatedClass(MatchDay.class)
					.addAnnotatedClass(FantaUser.class)
					.addAnnotatedClass(FantaTeam.class)
					.addAnnotatedClass(League.class)
					.addAnnotatedClass(Contract.class)
					.addAnnotatedClass(Player.class)
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
	
	@Nested
	@DisplayName("can look up all MatchDays related to a given League")
	class LookupByLeague {
		
		@Test
		@DisplayName("when no MatchDays associated to the given League exist in the database")
		public void testGetAllMatchDaysWhenNoMatchDaysExist() {
			
			// GIVEN no MatchDays exist in the database for the givne League
			
			// WHEN the SUT is used to retrieve MatchDays for the given League
			entityManager.getTransaction().begin();
			List<MatchDay> matchDays = matchDayRepository.getAllMatchDays(league);
			entityManager.getTransaction().commit();
			entityManager.clear();
			
			// THEN an empty Set is returned
			assertThat(matchDays).isEmpty();
		}
		
		
		@Test
		@DisplayName("when some MatchDays associated to the given League exist in the database")
		public void testGetAllMatchDaysWhenTwoMatchDaysExist() {
			
			// GIVEN some MatchDays exist in the database for a given League
			MatchDay matchDay1 = new MatchDay("prima giornata", 1, MatchDay.Status.FUTURE, league);
			MatchDay matchDay2 = new MatchDay("seconda giornata", 2, MatchDay.Status.FUTURE, league);
			
			sessionFactory.inTransaction(session -> {
				session.persist(matchDay1);
				session.persist(matchDay2);
			});
			
			// WHEN the SUT is used to retrieve MatchDays for a given League
			entityManager.getTransaction().begin();
			List<MatchDay> matchDays = matchDayRepository.getAllMatchDays(league);
			entityManager.getTransaction().commit();
			entityManager.clear();
			
			// THEN only the expected MatchDays are retrieved
			assertThat(matchDays).containsExactly(matchDay1, matchDay2);
		}
	}

	@Nested
	@DisplayName("can look up MatchDays associated with a given League which are")
	class LookUpByLeagueAndStatus {
		
		@Nested
		@DisplayName("the latest ended MatchDay for the given League")
		class LatestEnded {
			
			@Test
			@DisplayName("when the given League has no ended MatchDay")
			public void testGetPreviousMatchDayWhenNoPreviousMatchDayExists() {
				
				// GIVEN a League has no ended MatchDay
				sessionFactory.inTransaction(
						session -> session.persist(new MatchDay("prima giornata", 1, MatchDay.Status.FUTURE, league)));
				
				// WHEN the SUT is used to retrieve a League's latest ended MatchDay
				entityManager.getTransaction().begin();
				Optional<MatchDay> previousMatchDay = matchDayRepository.getLatestEndedMatchDay(league);
				entityManager.getTransaction().commit();
				entityManager.clear();
				
				// THEN an empty Optional is returned
				assertThat(previousMatchDay.isEmpty()).isTrue();
			}
			
			@Test
			@DisplayName("when the given League has exactly one ended MatchDay")
			public void testGetPreviousMatchDayWhenPreviousMatchDayExists() {
				
				// GIVEN a League has one ended MatchDay
				MatchDay pastMatchDay = new MatchDay("prima giornata", 1, MatchDay.Status.PAST, league);
				sessionFactory.inTransaction(session -> {
					session.persist(pastMatchDay);
					session.persist(new MatchDay("seconda giornata", 2, MatchDay.Status.FUTURE, league));
				});
				
				// WHEN the SUT is used to retrieve a League's latest ended MatchDay
				entityManager.getTransaction().begin();
				Optional<MatchDay> previousMatchDay = matchDayRepository.getLatestEndedMatchDay(league);
				entityManager.getTransaction().commit();
				entityManager.clear();
				
				// THEN the expected MatchDay is retrieved
				assertThat(previousMatchDay).hasValue(pastMatchDay);
			}
			
			@Test
			@DisplayName("when the given League has several ended MatchDays")
			public void testGetPreviousMatchDayWhenMultiplePreviousMatchDayExist() {
				
				// GIVEN a League has several ended MatchDays
				MatchDay previousDay = new MatchDay("seconda giornata", 2, MatchDay.Status.PAST, league);
				
				sessionFactory.inTransaction(session -> {
					session.persist(new MatchDay("prima giornata", 1, MatchDay.Status.PAST, league ));
					session.persist(previousDay);
					session.persist(new MatchDay("terza giornata", 3, MatchDay.Status.FUTURE, league));
				});
				
				// WHEN the SUT is used to retrieve a League's latest ended MatchDay
				entityManager.getTransaction().begin();
				Optional<MatchDay> previousMatchDay = matchDayRepository.getLatestEndedMatchDay(league);
				entityManager.getTransaction().commit();
				entityManager.clear();
				
				// THEN only the latest ended MatchDay is retrieved
				assertThat(previousMatchDay).hasValue(previousDay);
			}
		}
		
		@Nested
		@DisplayName("the earliest upcoming MatchDay for the given League")
		class EarliestUpcoming {
			
			@Test
			@DisplayName("when the given League has no upcoming MatchDay")
			public void testGetNextMatchDayWhenNoNextMatchDayExists() {
				
				// GIVEN a League has no upcoming MatchDays
				sessionFactory.inTransaction(session -> {
					session.persist(new MatchDay("ultima giornata", 20, MatchDay.Status.PAST, league));
				});
				
				// WHEN the SUT is used to retrieve a League's latest ended MatchDay
				entityManager.getTransaction().begin();
				Optional<MatchDay> nextMatchDay = matchDayRepository.getEarliestUpcomingMatchDay(league);
				entityManager.getTransaction().commit();
				entityManager.clear();
				
				// THEN an empty Optional is returned
				assertThat(nextMatchDay).isEmpty();
			}
			
			@Test
			@DisplayName("when the given League has exactly one upcoming MatchDay")
			public void testGetNextMatchDayWhenNextMatchDayExists() {
				
				// GIVEN a League has one upcoming MatchDay
				MatchDay expected = new MatchDay("seconda giornata", 2, MatchDay.Status.FUTURE, league);
				sessionFactory.inTransaction(session -> {
					session.persist(new MatchDay("prima giornata", 1, MatchDay.Status.PAST, league));
					session.persist(expected);
				});
				
				// WHEN the SUT is used to retrieve a League's latest ended MatchDay
				entityManager.getTransaction().begin();
				Optional<MatchDay> nextMatchDay = matchDayRepository.getEarliestUpcomingMatchDay(league);
				entityManager.getTransaction().commit();
				entityManager.clear();
				
				// THEN the expected MatchDay is retrieved
				assertThat(nextMatchDay).hasValue(expected);
			}
			
			@Test
			@DisplayName("when the given League has several upcoming MatchDays")
			public void testGetNextMatchDayWhenMultipleNextMatchDayExist() {
				
				// GIVEN a League has several upcoming MatchDays
				MatchDay nextDay = new MatchDay("seconda giornata", 2, MatchDay.Status.FUTURE, league);
				
				sessionFactory.inTransaction(session -> {
					session.persist(new MatchDay("prima giornata", 1, MatchDay.Status.PAST, league));
					session.persist(nextDay);
					session.persist(new MatchDay("terza giornata", 3, MatchDay.Status.FUTURE, league ));
				});
				
				// WHEN the SUT is used to retrieve a League's earliest upcoming MatchDay
				entityManager.getTransaction().begin();
				Optional<MatchDay> nextMatchDay = matchDayRepository.getEarliestUpcomingMatchDay(league);
				entityManager.getTransaction().commit();
				entityManager.clear();
				
				// THEN only the earliest upcoming MatchDay is retrieved
				assertThat(nextMatchDay).hasValue(nextDay);
			}
		}
		
		@Nested
		@DisplayName("the ongoing MatchDay for the given League")
		class Ongoing {
			
			@Test
			@DisplayName("when the given League has no ongoing MatchDay")
			public void testGetMatchDayWhenNotExists() {
				
				// GIVEN a League has no ongoing MatchDay
				sessionFactory.inTransaction(
						session -> session.persist(new MatchDay("prima giornata", 1, MatchDay.Status.FUTURE, league)));
				
				// WHEN the SUT is used to retrieve a League's ongoing MatchDay
				entityManager.getTransaction().begin();
				Optional<MatchDay> retrieved = matchDayRepository.getOngoingMatchDay(league);
				entityManager.getTransaction().commit();
				entityManager.clear();
				
				// THEN an empty Optional is returned
				assertThat(retrieved).isEmpty();		
			}
			
			@Test
			@DisplayName("when a League has exactly one MatchDay that is ongoing")
			public void testGetMatchDayWhenExists() {
				
				// GIVEN a League has an ongoing MatchDay
				MatchDay expected = new MatchDay("prima giornata", 1, MatchDay.Status.PRESENT, league);
				sessionFactory.inTransaction(session -> session.persist(expected));
				
				// WHEN the SUT is used to retrieve a League's ongoing MatchDay
				entityManager.getTransaction().begin();
				Optional<MatchDay> matchDay = matchDayRepository.getOngoingMatchDay(league);
				entityManager.getTransaction().commit();
				entityManager.clear();
				
				// THEN the appropriate MatchDay is returned
				assertThat(matchDay).hasValue(expected);
			}
			
			@Test
			@DisplayName("when a League has an ongoing MatchDays among other MatchDays")
			public void testGetMatchDayWhenMultipleDaysExist() {
				
				// GIVEN a League has an ongoing MatchDay
				MatchDay expected = new MatchDay("seconda giornata", 2, MatchDay.Status.PRESENT, league);
				
				sessionFactory.inTransaction(session -> {
					session.persist(new MatchDay("prima giornata", 1,  MatchDay.Status.PAST, league));
					session.persist(expected);
					session.persist(new MatchDay("terza giornata", 3,  MatchDay.Status.FUTURE, league));
				});
				
				// WHEN the SUT is used to retrieve a League's ongoing MatchDay
				entityManager.getTransaction().begin();
				Optional<MatchDay> matchDay = matchDayRepository.getOngoingMatchDay(league);
				entityManager.getTransaction().commit();
				entityManager.clear();
				
				// THEN the appropriate MatchDay is returned
				assertThat(matchDay).hasValue(expected);
			}
		}		
	}
	
	@Test
	@DisplayName("can persist a MatchDay instance to the database")
	void testSaveMatchDay() {

		// GIVEN a MatchDay instance exists

		MatchDay matchDay = new MatchDay("MD1", 1, MatchDay.Status.FUTURE, league);

		// WHEN the SUT is used to persist that MatchDay to the database
        entityManager.getTransaction().begin();
		matchDayRepository.saveMatchDay(matchDay);
		entityManager.getTransaction().commit();
        entityManager.clear();

        // THEN the MatchDay is present in the database
		assertThat(sessionFactory.fromTransaction(
				(Session em) -> em.createQuery("FROM MatchDay md "
						+ "JOIN FETCH md.league l JOIN FETCH l.admin", MatchDay.class).getResultStream().toList()))
				.containsExactly(matchDay);
	}
}