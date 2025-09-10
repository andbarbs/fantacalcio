package jpaRepositories;

import domainModel.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.junit.jupiter.api.*;

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
					.addAnnotatedClass(FantaTeam.class).addAnnotatedClass(MatchDaySerieA.class)
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
		MatchDaySerieA matchDay = new MatchDaySerieA("MD1", LocalDate.now());
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
			assertThat(result).containsExactly(match);
		});
	}

	@Test
	@DisplayName("getAllMatchesByMatchDay() should return all matches of a given MatchDay")
	void testGetAllMatchesByMatchDay() {
		MatchDaySerieA matchDay = new MatchDaySerieA("MD2", LocalDate.now());
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
	@DisplayName("getMatchByMatchDay() should return the match when team is either team1 or team2")
	void testGetMatchByMatchDay() {
		MatchDaySerieA matchDay = new MatchDaySerieA("MD3", LocalDate.now());
		FantaUser admin = new FantaUser("admin@" + "L003" + ".com", "pwd");
		NewsPaper np = new NewsPaper("Gazzetta " + "L003");
		League league = new League(admin, "League " + "L003", np, "L003");
		FantaUser user1 = new FantaUser("f@f.com", "pwd");
		FantaTeam t1 = new FantaTeam("Team F", league, 0, user1, Set.of());
		FantaUser user2 = new FantaUser("g@g.com", "pwd");
		FantaTeam t2 = new FantaTeam("Team G", league, 0, user2, Set.of());

		Match match = new Match(matchDay, t1, t2);

		entityManager.getTransaction().begin();
		entityManager.persist(matchDay);
		entityManager.persist(league.getAdmin());
		entityManager.persist(league.getNewsPaper());
		entityManager.persist(league);
		entityManager.persist(t1.getFantaManager());
		entityManager.persist(t2.getFantaManager());
		entityManager.persist(t1);
		entityManager.persist(t2);
		entityManager.persist(match);
		entityManager.getTransaction().commit();

		Match found1 = matchRepository.getMatchByMatchDay(matchDay, league, t1);
		Match found2 = matchRepository.getMatchByMatchDay(matchDay, league, t2);

		assertThat(found1).isEqualTo(match);
		assertThat(found2).isEqualTo(match);
	}

	@Test
	@DisplayName("getMatchByMatchDay() should return empty when no match exists")
	void testGetMatchByMatchDayWhenNoMatch() {
		MatchDaySerieA matchDay = new MatchDaySerieA("MD4", LocalDate.now());
		FantaUser admin = new FantaUser("admin@" + "L004" + ".com", "pwd");
		NewsPaper np = new NewsPaper("Gazzetta " + "L004");
		League league = new League(admin, "League " + "L004", np, "L004");
		FantaUser user = new FantaUser("h@h.com", "pwd");
		FantaTeam t1 = new FantaTeam("Team H", league, 0, user, Set.of());

		entityManager.getTransaction().begin();
		entityManager.persist(matchDay);
		entityManager.persist(league.getAdmin());
		entityManager.persist(league.getNewsPaper());
		entityManager.persist(league);
		entityManager.persist(t1.getFantaManager());
		entityManager.persist(t1);
		entityManager.getTransaction().commit();

		List<Match> results = matchRepository.getAllMatchesByMatchDay(matchDay, league);

		assertThat(results).isEmpty();
	}
}
