package jpaRepositories;

import domainModel.*;
import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.junit.jupiter.api.*;
import domainModel.Player.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;

class JpaLineUpRepositoryTest {

	private static SessionFactory sessionFactory;
	private EntityManager entityManager;
	private JpaLineUpRepository lineUpRepository;
	private FantaUser manager;
	private NewsPaper newsPaper;
	private League league;
	private MatchDaySerieA matchDay;
	private FantaTeam opponent;

	@BeforeAll
	static void initializeSessionFactory() {
		try {
			StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
					.configure("hibernate-test.cfg.xml").build();

			Metadata metadata = new MetadataSources(serviceRegistry).addAnnotatedClass(_433LineUp.class)
					.addAnnotatedClass(LineUp.class).addAnnotatedClass(MatchDaySerieA.class)
					.addAnnotatedClass(NewsPaper.class).addAnnotatedClass(FantaUser.class)
					.addAnnotatedClass(Match.class).addAnnotatedClass(FantaTeam.class).addAnnotatedClass(Fielding.class)
					.addAnnotatedClass(Fielding.StarterFielding.class)
					.addAnnotatedClass(Fielding.SubstituteFielding.class).addAnnotatedClass(Goalkeeper.class)
					.addAnnotatedClass(Defender.class).addAnnotatedClass(Midfielder.class)
					.addAnnotatedClass(Forward.class).addAnnotatedClass(Contract.class).addAnnotatedClass(League.class)
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
		lineUpRepository = new JpaLineUpRepository(entityManager);

		sessionFactory.inTransaction(t -> {
			manager = new FantaUser("manager@example.com", "securePass");
			t.persist(manager);
			newsPaper = new NewsPaper("Gazzetta");
			t.persist(newsPaper);
			league = new League(manager, "Serie A", newsPaper, "code");
			t.persist(league);
			matchDay = new MatchDaySerieA("Matchday 1", LocalDate.of(2025, 6, 19));
			t.persist(matchDay);
			opponent = new FantaTeam("Challengers", league, 25, manager, new HashSet<>());
			t.persist(opponent);
		});

	}

	@AfterAll
	static void tearDown() {
		sessionFactory.close();
	}

	@Test
	@DisplayName("saveLineUp should persist a LineUp correctly")
	void testSaveLineUpPersistsCorrectly() {
		entityManager.getTransaction().begin();
		
		// Players
		Goalkeeper gk1 = new Goalkeeper("Gianluigi", "Buffon", Club.JUVENTUS);
		Goalkeeper gk2 = new Goalkeeper("Samir", "Handanović", Club.INTER);

		Defender d1 = new Defender("Paolo", "Maldini", Club.MILAN);
		Defender d2 = new Defender("Franco", "Baresi", Club.JUVENTUS);
		Defender d3 = new Defender("Alessandro", "Nesta", Club.LAZIO);
		Defender d4 = new Defender("Giorgio", "Chiellini", Club.JUVENTUS);
		Defender d5 = new Defender("Leonardo", "Bonucci", Club.JUVENTUS);

		Midfielder m1 = new Midfielder("Andrea", "Pirlo", Club.JUVENTUS);
		Midfielder m2 = new Midfielder("Daniele", "De Rossi", Club.ROMA);
		Midfielder m3 = new Midfielder("Marco", "Verratti", Club.CREMONESE);
		Midfielder m4 = new Midfielder("Claudio", "Marchisio", Club.JUVENTUS);

		Forward f1 = new Forward("Roberto", "Baggio", Club.BOLOGNA);
		Forward f2 = new Forward("Francesco", "Totti", Club.ROMA);
		Forward f3 = new Forward("Alessandro", "Del Piero", Club.JUVENTUS);
		Forward f4 = new Forward("Lorenzo", "Insigne", Club.NAPOLI);

		List<Player> players = List.of(gk1, gk2, d1, d2, d3, d4, d5, m1, m2, m3, m4, f1, f2, f3, f4);

		players.forEach(entityManager::persist);

		// Contracts, FantaTeam and Match
		Set<Contract> contracts = new HashSet<Contract>();
		FantaTeam team = new FantaTeam("Dream Team", league, 30, manager, contracts);
		players.stream().map(p -> new Contract(team, p)).forEach(contracts::add);

		entityManager.persist(team);

		FantaTeam opponent = new FantaTeam("Challengers", league, 25, manager, new HashSet<>());
		entityManager.persist(opponent);

		Match match = new Match(matchDay, team, opponent);
		entityManager.persist(match);

		// Build LineUp with starters and substitutes, save and commit
		lineUpRepository.saveLineUp(new _433LineUp._443LineUpBuilder(match, team).withGoalkeeper(gk1)
				.withDefenders(d1, d2, d3, d4).withMidfielders(m1, m2, m3).withForwards(f1, f2, f3)
				.withSubstituteGoalkeepers(List.of(gk2)).withSubstituteDefenders(List.of(d5))
				.withSubstituteMidfielders(List.of(m4)).withSubstituteForwards(List.of(f4)).build());
		entityManager.getTransaction().commit();
		entityManager.clear();

		// Assert
		sessionFactory.inTransaction((Session em) -> {
			Optional<LineUp> result = em
					.createQuery("FROM LineUp l WHERE l.match = :match AND l.team = :team", LineUp.class)
					.setParameter("match", match).setParameter("team", team).getResultStream().findFirst();

			assertThat(result).isPresent();
			LineUp found = result.get();
			assertThat(found.getTeam()).isEqualTo(team);
			assertThat(found.getMatch()).isEqualTo(match);
		});

	}

	@Test
	@DisplayName("getLineUpByMatchAndTeam should retrieve a saved LineUp correctly")
	void testGetLineUpByMatchAndTeamRetrievesCorrectly() {
		entityManager.getTransaction().begin();

		// Players
		Goalkeeper gk1 = new Goalkeeper("Gianluigi", "Buffon", Club.JUVENTUS);
		Goalkeeper gk2 = new Goalkeeper("Samir", "Handanović", Club.INTER);

		Defender d1 = new Defender("Paolo", "Maldini", Club.MILAN);
		Defender d2 = new Defender("Franco", "Baresi", Club.JUVENTUS);
		Defender d3 = new Defender("Alessandro", "Nesta", Club.LAZIO);
		Defender d4 = new Defender("Giorgio", "Chiellini", Club.JUVENTUS);
		Defender d5 = new Defender("Leonardo", "Bonucci", Club.JUVENTUS);

		Midfielder m1 = new Midfielder("Andrea", "Pirlo", Club.JUVENTUS);
		Midfielder m2 = new Midfielder("Daniele", "De Rossi", Club.ROMA);
		Midfielder m3 = new Midfielder("Marco", "Verratti", Club.CREMONESE);
		Midfielder m4 = new Midfielder("Claudio", "Marchisio", Club.JUVENTUS);

		Forward f1 = new Forward("Roberto", "Baggio", Club.BOLOGNA);
		Forward f2 = new Forward("Francesco", "Totti", Club.ROMA);
		Forward f3 = new Forward("Alessandro", "Del Piero", Club.JUVENTUS);
		Forward f4 = new Forward("Lorenzo", "Insigne", Club.NAPOLI);

		List<Player> players = List.of(gk1, gk2, d1, d2, d3, d4, d5, m1, m2, m3, m4, f1, f2, f3, f4);

		players.forEach(entityManager::persist);

		// Contracts, FantaTeam and Match
		Set<Contract> contracts = new HashSet<Contract>();
		FantaTeam team = new FantaTeam("Dream Team", league, 30, manager, contracts);
		players.stream().map(p -> new Contract(team, p)).forEach(contracts::add);

		entityManager.persist(team);

		Match match = new Match(matchDay, team, opponent);
		entityManager.persist(match);

		// Build LineUp with starters and substitutes, save and commit
		entityManager.persist(new _433LineUp._443LineUpBuilder(match, team).withGoalkeeper(gk1)
				.withDefenders(d1, d2, d3, d4).withMidfielders(m1, m2, m3).withForwards(f1, f2, f3)
				.withSubstituteGoalkeepers(List.of(gk2)).withSubstituteDefenders(List.of(d5))
				.withSubstituteMidfielders(List.of(m4)).withSubstituteForwards(List.of(f4)).build());
		entityManager.getTransaction().commit();
		entityManager.clear(); // the Session is not closed! SUT instance is still used for verifications

		// Verify
		Optional<LineUp> retrieved = lineUpRepository.getLineUpByMatchAndTeam(match, team);
		assertThat(retrieved).isPresent();

		LineUp persistedLineUp = retrieved.get();
		assertThat(persistedLineUp.getMatch()).isEqualTo(match);
		assertThat(persistedLineUp.getTeam()).isEqualTo(team);

		// Validate starters
		assertThat(persistedLineUp.extract().starterGoalkeepers()).containsExactly(gk1);
		assertThat(persistedLineUp.extract().starterDefenders()).containsExactlyInAnyOrder(d1, d2, d3, d4);
		assertThat(persistedLineUp.extract().starterMidfielders()).containsExactlyInAnyOrder(m1, m2, m3);
		assertThat(persistedLineUp.extract().starterForwards()).containsExactlyInAnyOrder(f1, f2, f3);

		// Validate substitutes (which are returned as Lists, and ordering may be
		// significant)
		assertThat(persistedLineUp.extract().substituteGoalkeepers()).containsExactly(gk2);
		assertThat(persistedLineUp.extract().substituteDefenders()).containsExactly(d5);
		assertThat(persistedLineUp.extract().substituteMidfielders()).containsExactly(m4);
		assertThat(persistedLineUp.extract().substituteForwards()).containsExactly(f4);
	}

	@Test
	@DisplayName("getLineUpByMatchAndTeam should return empty if no lineup exists")
	void testGetLineUpByMatchAndTeamEmpty() {
		entityManager.getTransaction().begin();

		// FantaTeam and Match
		FantaTeam team = new FantaTeam("Dream Team", league, 30, manager, new HashSet<Contract>());
		entityManager.persist(team);

		Match match = new Match(matchDay, team, opponent);
		entityManager.persist(match);

		entityManager.getTransaction().commit();

		Optional<LineUp> result = lineUpRepository.getLineUpByMatchAndTeam(match, team);
		assertThat(result).isEmpty();
	}

}
