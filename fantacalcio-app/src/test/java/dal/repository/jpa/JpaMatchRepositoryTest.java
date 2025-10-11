package dal.repository.jpa;

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

import static org.assertj.core.api.Assertions.*;

class JpaMatchRepositoryTest {

	private static SessionFactory sessionFactory;
	private JpaMatchRepository matchRepository;
	private EntityManager entityManager;

	@BeforeAll
	static void initializeSessionFactory() {
		try {
			StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
					.configure("hibernate-test.cfg.xml").build();

			Metadata metadata = new MetadataSources(serviceRegistry)
					.addAnnotatedClass(FantaUser.class)
					.addAnnotatedClass(League.class)
					.addAnnotatedClass(FantaTeam.class)
					.addAnnotatedClass(MatchDay.class)
					.addAnnotatedClass(Match.class)
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
		sessionFactory.getSchemaManager().truncateMappedObjects();
		entityManager = sessionFactory.createEntityManager();
		matchRepository = new JpaMatchRepository(entityManager);
	}

	@AfterAll
	static void tearDown() {
		sessionFactory.close();
	}

	@Test
	@DisplayName("saveMatch() should persist a match")
	void testSaveMatch() {
		
		// GIVEN a Match's auxiliary entities are manually persisted
		FantaUser admin = new FantaUser("admin@" + "L001" + ".com", "pwd");
		League league = new League(admin, "League " + "L001", "L001");
        MatchDay matchDay = new MatchDay("MD1", 1, MatchDay.Status.FUTURE, league);
		FantaUser user1 = new FantaUser("a@a.com", "pwd");
		FantaTeam t1 = new FantaTeam("Team A", league, 0, user1, null);
		FantaUser user2 = new FantaUser("b@b.com", "pwd");
		FantaTeam t2 = new FantaTeam("Team B", league, 0, user2, null);
		
		sessionFactory.inTransaction(entityManager -> {
			entityManager.persist(admin);
			entityManager.persist(league);
			entityManager.persist(matchDay);
			entityManager.persist(user1);
			entityManager.persist(user2);
			entityManager.persist(t1);
			entityManager.persist(t2);
		});

		// GIVEN the SUT is used to persist a Match
		Match match = new Match(matchDay, t1, t2);
		entityManager.getTransaction().begin();
		matchRepository.saveMatch(match);
		entityManager.getTransaction().commit();
		entityManager.clear();

		// THEN the Match is correctly persisted to the database
		assertThat(sessionFactory.fromTransaction((Session session) -> session.createQuery("from Match m "
				+ "JOIN FETCH m.team1 t1 JOIN FETCH m.team2 t2 JOIN FETCH t1.fantaManager JOIN FETCH t2.fantaManager "
				+ "JOIN FETCH m.matchDay day JOIN FETCH day.league league JOIN FETCH league.admin", Match.class)
				.getResultList())).containsExactly(match);
	}

	@Test
	@DisplayName("getAllMatchesByMatchDay() should return all matches of a given MatchDay")
	void testGetAllMatchesByMatchDay() {
		
		// GIVEN two Match instances are manually persisted for test MatchDay
		FantaUser admin = new FantaUser("admin@" + "L002" + ".com", "pwd");
		League league = new League(admin, "League " + "L002", "L002");
        MatchDay matchDay = new MatchDay("MD2", 2, MatchDay.Status.FUTURE, league);
		FantaUser user1 = new FantaUser("c@c.com", "pwd");
		FantaTeam t1 = new FantaTeam("Team C", league, 0, user1, Set.of());
		FantaUser user2 = new FantaUser("d@d.com", "pwd");
		FantaTeam t2 = new FantaTeam("Team D", league, 0, user2, Set.of());
		FantaUser user3 = new FantaUser("e@e.com", "pwd");
		FantaTeam t3 = new FantaTeam("Team E", league, 0, user3, Set.of());

		Match m1 = new Match(matchDay, t1, t2);
		Match m2 = new Match(matchDay, t3, t1);
		
		sessionFactory.inTransaction(em -> {
			em.persist(admin);
			em.persist(league);
			em.persist(matchDay);
			em.persist(user1);
			em.persist(user2);
			em.persist(user3);
			em.persist(t1);
			em.persist(t2);
			em.persist(t3);
			em.persist(m1);
			em.persist(m2);
		});

		// WHEN the SUT is used to retrieve all Matches in a MatchDay
		entityManager.getTransaction().begin();
		List<Match> matches = matchRepository.getAllMatchesIn(matchDay);		
		entityManager.getTransaction().commit();
		entityManager.clear();

		// THEN the expected Matches are returned
		assertThat(matches).containsExactlyInAnyOrder(m1, m2);
	}

	@Test
	@DisplayName("getMatchByMatchDay() should return the correct match when team is team1")
	void testGetMatchByMatchDayWithTeam1() {
		
		// GIVEN a Match is manually persisted for a Team as home team
	    FantaUser admin = new FantaUser("admin@L005.com", "pwd");
	    League league = new League(admin, "League L005", "L005");
        MatchDay matchDay = new MatchDay("MD5", 5, MatchDay.Status.FUTURE, league);
	    FantaUser user1 = new FantaUser("i@i.com", "pwd");
	    FantaTeam t1 = new FantaTeam("Team I", league, 0, user1, Set.of());
	    FantaUser user2 = new FantaUser("j@j.com", "pwd");
	    FantaTeam t2 = new FantaTeam("Team J", league, 0, user2, Set.of());

	    Match match = new Match(matchDay, t1, t2);
	    
	    sessionFactory.inTransaction(em -> {
	    	em.persist(admin);
	    	em.persist(league);
	    	em.persist(matchDay);
		    em.persist(user1);
		    em.persist(user2);
		    em.persist(t1);
		    em.persist(t2);
		    em.persist(match);
	    });

	    // WHEN the SUT is used to retrieve the Match associated with a given Team as home
	    entityManager.getTransaction().begin();	    
	    Optional<Match> found = matchRepository.getMatchBy(matchDay, t1);
	    entityManager.getTransaction().commit();
	    entityManager.clear();

	    // THEN the retrieved Match is the expected one
	    assertThat(found).hasValue(match);
	}

	@Test
	@DisplayName("getMatchByMatchDay() should return the correct match when team is team2")
	void testGetMatchByMatchDayWithTeam2() {
		
		// GIVEN a Match is manually persisted for a Team as away team
	    FantaUser admin = new FantaUser("admin@L006.com", "pwd");
	    League league = new League(admin, "League L006", "L006");
        MatchDay matchDay = new MatchDay("MD6", 6, MatchDay.Status.FUTURE, league);
	    FantaUser user1 = new FantaUser("k@k.com", "pwd");
	    FantaTeam t1 = new FantaTeam("Team K", league, 0, user1, Set.of());
	    FantaUser user2 = new FantaUser("l@l.com", "pwd");
	    FantaTeam t2 = new FantaTeam("Team L", league, 0, user2, Set.of());

	    Match match = new Match(matchDay, t1, t2);
	    
	    sessionFactory.inTransaction(em -> {
	    	em.persist(admin);
	    	em.persist(league);
	    	em.persist(matchDay);
		    em.persist(user1);
		    em.persist(user2);
		    em.persist(t1);
		    em.persist(t2);
		    em.persist(match);
	    });

	    // WHEN the SUT is used to retrieve the Match associated with a given Team as away
	    entityManager.getTransaction().begin();
	    Optional<Match> found = matchRepository.getMatchBy(matchDay, t2);
	    entityManager.getTransaction().commit();
	    entityManager.clear();

	    // THEN the retrieved Match is the expected one
	    assertThat(found).hasValue(match);
	}

	@Test
	@DisplayName("getMatchByMatchDay() should throw NoSuchElementException when no match exists")
	void testGetMatchByMatchDayWhenNoMatchExists() {
		
		// GIVEN a Match's auxiliary entities are manually persisted
	    FantaUser admin = new FantaUser("admin@L007.com", "pwd");
	    League league = new League(admin, "League L007", "L007");
        MatchDay matchDay = new MatchDay("MD7", 7, MatchDay.Status.FUTURE, league);
	    FantaUser user1 = new FantaUser("m@m.com", "pwd");
	    FantaTeam t1 = new FantaTeam("Team M", league, 0, user1, Set.of());
	    
	    sessionFactory.inTransaction(em -> {
	    	em.persist(admin);
	    	em.persist(league);
	    	em.persist(matchDay);
		    em.persist(user1);
		    em.persist(t1);
	    });

	    // WHEN the SUT is used to retrieve a Match for a Team that doesn't exist
	    entityManager.getTransaction().begin();
	    Optional<Match> retrieved = matchRepository.getMatchBy(matchDay, t1);
	    entityManager.getTransaction().commit();
	    entityManager.clear();

	    // THEN an empty Optional is returned
	    assertThat(retrieved).isEmpty();
	}
}
