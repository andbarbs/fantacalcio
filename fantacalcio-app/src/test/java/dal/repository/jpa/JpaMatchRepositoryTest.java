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

import java.time.LocalDate;
import java.util.List;
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

			Metadata metadata = new MetadataSources(serviceRegistry).addAnnotatedClass(FantaUser.class)
					.addAnnotatedClass(NewsPaper.class).addAnnotatedClass(League.class)
					.addAnnotatedClass(FantaTeam.class).addAnnotatedClass(MatchDay.class)
					.addAnnotatedClass(Match.class).addAnnotatedClass(Contract.class).addAnnotatedClass(Player.class)
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
		MatchDay matchDay = new MatchDay("MD1", LocalDate.now(), 1);
		FantaUser admin = new FantaUser("admin@" + "L001" + ".com", "pwd");
		NewsPaper np = new NewsPaper("Gazzetta " + "L001");
		League league = new League(admin, "League " + "L001", np, "L001");
		FantaUser user1 = new FantaUser("a@a.com", "pwd");
		FantaTeam t1 = new FantaTeam("Team A", league, 0, user1, Set.of());
		FantaUser user2 = new FantaUser("b@b.com", "pwd");
		FantaTeam t2 = new FantaTeam("Team B", league, 0, user2, Set.of());

		Match match = new Match(matchDay, t1, t2);

		entityManager.getTransaction().begin();
		entityManager.persist(matchDay);
		entityManager.persist(admin);
		entityManager.persist(np);
		entityManager.persist(league);
		entityManager.persist(user1);
		entityManager.persist(user2);
		entityManager.persist(t1);
		entityManager.persist(t2);

		matchRepository.saveMatch(match);
		entityManager.getTransaction().commit();

		sessionFactory.inTransaction((Session session) -> {
			List<Match> result = session.createQuery("from Match", Match.class).getResultList();
			assertThat(result.size()).isEqualTo(1);
			Match resultMatch = result.get(0);
			assertThat(resultMatch.getMatchDay()).isEqualTo(matchDay);
			assertThat(resultMatch.getTeam1()).isEqualTo(t1);
			assertThat(resultMatch.getTeam2()).isEqualTo(t2);
		});
	}

	@Test
	@DisplayName("getAllMatchesByMatchDay() should return all matches of a given MatchDay")
	void testGetAllMatchesByMatchDay() {
		MatchDay matchDay = new MatchDay("MD2", LocalDate.now(), 1);
		FantaUser admin = new FantaUser("admin@" + "L002" + ".com", "pwd");
		NewsPaper np = new NewsPaper("Gazzetta " + "L002");
		League league = new League(admin, "League " + "L002", np, "L002");
		FantaUser user1 = new FantaUser("c@c.com", "pwd");
		FantaTeam t1 = new FantaTeam("Team C", league, 0, user1, Set.of());
		FantaUser user2 = new FantaUser("d@d.com", "pwd");
		FantaTeam t2 = new FantaTeam("Team D", league, 0, user2, Set.of());
		FantaUser user3 = new FantaUser("e@e.com", "pwd");
		FantaTeam t3 = new FantaTeam("Team E", league, 0, user3, Set.of());

		Match m1 = new Match(matchDay, t1, t2);
		Match m2 = new Match(matchDay, t3, t1);

		entityManager.getTransaction().begin();
		entityManager.persist(matchDay);
		entityManager.persist(admin);
		entityManager.persist(np);
		entityManager.persist(league);
		entityManager.persist(user1);
		entityManager.persist(user2);
		entityManager.persist(user3);
		entityManager.persist(t1);
		entityManager.persist(t2);
		entityManager.persist(t3);
		entityManager.persist(m1);
		entityManager.persist(m2);
		entityManager.getTransaction().commit();

		List<Match> matches = matchRepository.getAllMatchesByMatchDay(matchDay, league);

		assertThat(matches).containsExactlyInAnyOrder(m1, m2);
	}

	@Test
	@DisplayName("getMatchByMatchDay() should return the correct match when team is team1")
	void testGetMatchByMatchDayWithTeam1() {
	    MatchDay matchDay = new MatchDay("MD5", LocalDate.now(), 1);
	    FantaUser admin = new FantaUser("admin@L005.com", "pwd");
	    NewsPaper np = new NewsPaper("Gazzetta L005");
	    League league = new League(admin, "League L005", np, "L005");
	    FantaUser user1 = new FantaUser("i@i.com", "pwd");
	    FantaTeam t1 = new FantaTeam("Team I", league, 0, user1, Set.of());
	    FantaUser user2 = new FantaUser("j@j.com", "pwd");
	    FantaTeam t2 = new FantaTeam("Team J", league, 0, user2, Set.of());

	    Match match = new Match(matchDay, t1, t2);

	    entityManager.getTransaction().begin();
	    entityManager.persist(matchDay);
	    entityManager.persist(admin);
	    entityManager.persist(np);
	    entityManager.persist(league);
	    entityManager.persist(user1);
	    entityManager.persist(user2);
	    entityManager.persist(t1);
	    entityManager.persist(t2);
	    entityManager.persist(match);
	    entityManager.getTransaction().commit();

	    Match found = matchRepository.getMatchByMatchDay(matchDay, league, t1);
	    assertThat(found).isEqualTo(match);
	}

	@Test
	@DisplayName("getMatchByMatchDay() should return the correct match when team is team2")
	void testGetMatchByMatchDayWithTeam2() {
	    MatchDay matchDay = new MatchDay("MD6", LocalDate.now(), 1);
	    FantaUser admin = new FantaUser("admin@L006.com", "pwd");
	    NewsPaper np = new NewsPaper("Gazzetta L006");
	    League league = new League(admin, "League L006", np, "L006");
	    FantaUser user1 = new FantaUser("k@k.com", "pwd");
	    FantaTeam t1 = new FantaTeam("Team K", league, 0, user1, Set.of());
	    FantaUser user2 = new FantaUser("l@l.com", "pwd");
	    FantaTeam t2 = new FantaTeam("Team L", league, 0, user2, Set.of());

	    Match match = new Match(matchDay, t1, t2);

	    entityManager.getTransaction().begin();
	    entityManager.persist(matchDay);
	    entityManager.persist(admin);
	    entityManager.persist(np);
	    entityManager.persist(league);
	    entityManager.persist(user1);
	    entityManager.persist(user2);
	    entityManager.persist(t1);
	    entityManager.persist(t2);
	    entityManager.persist(match);
	    entityManager.getTransaction().commit();

	    Match found = matchRepository.getMatchByMatchDay(matchDay, league, t2);
	    assertThat(found).isEqualTo(match);
	}

	@Test
	@DisplayName("getMatchByMatchDay() should throw NoSuchElementException when no match exists")
	void testGetMatchByMatchDayWhenNoMatchExists() {
	    MatchDay matchDay = new MatchDay("MD7", LocalDate.now(),1 );
	    FantaUser admin = new FantaUser("admin@L007.com", "pwd");
	    NewsPaper np = new NewsPaper("Gazzetta L007");
	    League league = new League(admin, "League L007", np, "L007");
	    FantaUser user1 = new FantaUser("m@m.com", "pwd");
	    FantaTeam t1 = new FantaTeam("Team M", league, 0, user1, Set.of());

	    entityManager.getTransaction().begin();
	    entityManager.persist(matchDay);
	    entityManager.persist(admin);
	    entityManager.persist(np);
	    entityManager.persist(league);
	    entityManager.persist(user1);
	    entityManager.persist(t1);
	    entityManager.getTransaction().commit();

	    assertThatThrownBy(() -> matchRepository.getMatchByMatchDay(matchDay, league, t1))
	        .isInstanceOf(java.util.NoSuchElementException.class);
	}

}
