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
import domainModel.scheme.Scheme433;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * tests that a {@link JpaLineUnRepository} correctly 
 * persists and retrieves {@link LineUp}s.
 * 
 * <h1>UNIT TEST ISOLATION</h1>
 * Assertions are made more readable through the
 * {@linkplain LineUpViewer extraction API} offered by
 * {@link LineUp#extract()}
 */
class JpaLineUpRepositoryTest {

	private static SessionFactory sessionFactory;
	private EntityManager repositoryEntityManager;
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

			Metadata metadata = new MetadataSources(serviceRegistry)
					.addAnnotatedClass(LineUp.class)
					.addAnnotatedClass(MatchDaySerieA.class)
					.addAnnotatedClass(NewsPaper.class)
					.addAnnotatedClass(FantaUser.class)
					.addAnnotatedClass(Match.class)
					.addAnnotatedClass(FantaTeam.class)
					.addAnnotatedClass(Fielding.class)
					.addAnnotatedClass(Fielding.StarterFielding.class)
					.addAnnotatedClass(Fielding.SubstituteFielding.class)
					.addAnnotatedClass(Goalkeeper.class)
					.addAnnotatedClass(Defender.class)
					.addAnnotatedClass(Midfielder.class)
					.addAnnotatedClass(Forward.class)
					.addAnnotatedClass(Contract.class)
					.addAnnotatedClass(League.class)
					.getMetadataBuilder().build();

			sessionFactory = metadata.getSessionFactoryBuilder().build();
		} catch (Throwable ex) {
			throw new ExceptionInInitializerError(ex);
		}
	}

	@BeforeEach
	void setup() {
		sessionFactory.getSchemaManager().truncateMappedObjects();
		repositoryEntityManager = sessionFactory.createEntityManager();
		lineUpRepository = new JpaLineUpRepository(repositoryEntityManager);

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
		repositoryEntityManager.getTransaction().begin();
		
		Goalkeeper gk1 = new Goalkeeper("Gianluigi", "Buffon");
		Goalkeeper gk2 = new Goalkeeper("Samir", "Handanović");
		Goalkeeper gk3 = new Goalkeeper("Donnarumma", "Gianluigi");
		Goalkeeper gk4 = new Goalkeeper("Walter", "Zenga");

		Defender d1 = new Defender("Paolo", "Maldini");
		Defender d2 = new Defender("Franco", "Baresi");
		Defender d3 = new Defender("Alessandro", "Nesta");
		Defender d4 = new Defender("Giorgio", "Chiellini");
		Defender d5 = new Defender("Leonardo", "Bonucci");
		Defender d6 = new Defender("Fabio", "Cannavaro");
		Defender d7 = new Defender("Javier", "Zanetti");

		Midfielder m1 = new Midfielder("Andrea", "Pirlo");
		Midfielder m2 = new Midfielder("Daniele", "De Rossi");
		Midfielder m3 = new Midfielder("Marco", "Verratti");
		Midfielder m4 = new Midfielder("Claudio", "Marchisio");
		Midfielder m5 = new Midfielder("Gennaro", "Gattuso");
		Midfielder m6 = new Midfielder("Francesco", "Totti");

		Forward f1 = new Forward("Roberto", "Baggio");
		Forward f2 = new Forward("Alessandro", "Del Piero");
		Forward f3 = new Forward("Lorenzo", "Insigne");
		Forward f4 = new Forward("Filippo", "Inzaghi");
		Forward f5 = new Forward("Luca", "Toni");
		Forward f6 = new Forward("Christian", "Vieri");

		List<Player> players = List.of(
				gk1, gk2, gk3, gk4, 
				d1, d2, d3, d4, d5, d6, d7,
				m1, m2, m3, m4, m5, m6,
				f1, f2, f3, f4, f5, f6);

		// GIVEN a LineUp's associated entities are persisted
		players.forEach(repositoryEntityManager::persist);

		// Contracts, FantaTeam and Match
		Set<Contract> contracts = new HashSet<Contract>();
		FantaTeam team = new FantaTeam("Dream Team", league, 30, manager, contracts);
		players.stream().map(p -> new Contract(team, p)).forEach(contracts::add);

		repositoryEntityManager.persist(team);
		
		FantaTeam opponent = new FantaTeam("Challengers", league, 25, manager, new HashSet<>());
		repositoryEntityManager.persist(opponent);

		Match match = new Match(matchDay, team, opponent);
		repositoryEntityManager.persist(match);

		// WHEN the SUT is used to persist a LineUp
		lineUpRepository.saveLineUp(LineUp.build()
				.forTeam(team)
				.inMatch(match)
				.withStarterLineUp(Scheme433.starterLineUp()
						.withGoalkeeper(gk1)
						.withDefenders(d1, d2, d3, d4)
						.withMidfielders(m1, m2, m3)
						.withForwards(f1, f2, f3))
				.withSubstituteGoalkeepers(gk2, gk3, gk4)
				.withSubstituteDefenders(d5, d6, d7)
				.withSubstituteMidfielders(m4, m5, m6)
				.withSubstituteForwards(f4, f5, f6));
		repositoryEntityManager.getTransaction().commit();
		repositoryEntityManager.clear(); // the Session is not closed! SUT instance is still used for verifications

		// THEN a direct query to the db retrieves exactly the persisted LineUp
		sessionFactory.inTransaction((Session em) -> {
			Optional<LineUp> result = em
					.createQuery("FROM LineUp l WHERE l.match = :match AND l.team = :team", LineUp.class)
					.setParameter("match", match).setParameter("team", team).getResultStream().findFirst();

			assertThat(result).isPresent();
			LineUp persistedLineUp = result.get();
			assertThat(persistedLineUp.getTeam()).isEqualTo(team);
			assertThat(persistedLineUp.getMatch()).isEqualTo(match);
			assertThat(persistedLineUp.getMatch()).isEqualTo(match);
			assertThat(persistedLineUp.getTeam()).isEqualTo(team);

			assertThat(persistedLineUp.extract().starterGoalkeepers()).containsExactly(gk1);
			assertThat(persistedLineUp.extract().starterDefenders()).containsExactlyInAnyOrder(d1, d2, d3, d4);
			assertThat(persistedLineUp.extract().starterMidfielders()).containsExactlyInAnyOrder(m1, m2, m3);
			assertThat(persistedLineUp.extract().starterForwards()).containsExactlyInAnyOrder(f1, f2, f3);

			assertThat(persistedLineUp.extract().substituteGoalkeepers()).containsExactly(gk2, gk3, gk4);
			assertThat(persistedLineUp.extract().substituteDefenders()).containsExactly(d5, d6, d7);
			assertThat(persistedLineUp.extract().substituteMidfielders()).containsExactly(m4, m5, m6);
			assertThat(persistedLineUp.extract().substituteForwards()).containsExactly(f4, f5, f6);
		});
	}

	@Test
	@DisplayName("getLineUpByMatchAndTeam should retrieve a saved LineUp correctly")
	void testGetLineUpByMatchAndTeamRetrievesCorrectly() {
		repositoryEntityManager.getTransaction().begin();

		Goalkeeper gk1 = new Goalkeeper("Gianluigi", "Buffon");
		Goalkeeper gk2 = new Goalkeeper("Samir", "Handanović");
		Goalkeeper gk3 = new Goalkeeper("Donnarumma", "Gianluigi");
		Goalkeeper gk4 = new Goalkeeper("Walter", "Zenga");

		Defender d1 = new Defender("Paolo", "Maldini");
		Defender d2 = new Defender("Franco", "Baresi");
		Defender d3 = new Defender("Alessandro", "Nesta");
		Defender d4 = new Defender("Giorgio", "Chiellini");
		Defender d5 = new Defender("Leonardo", "Bonucci");
		Defender d6 = new Defender("Fabio", "Cannavaro");
		Defender d7 = new Defender("Javier", "Zanetti");

		Midfielder m1 = new Midfielder("Andrea", "Pirlo");
		Midfielder m2 = new Midfielder("Daniele", "De Rossi");
		Midfielder m3 = new Midfielder("Marco", "Verratti");
		Midfielder m4 = new Midfielder("Claudio", "Marchisio");
		Midfielder m5 = new Midfielder("Gennaro", "Gattuso");
		Midfielder m6 = new Midfielder("Francesco", "Totti");

		Forward f1 = new Forward("Roberto", "Baggio");
		Forward f2 = new Forward("Alessandro", "Del Piero");
		Forward f3 = new Forward("Lorenzo", "Insigne");
		Forward f4 = new Forward("Filippo", "Inzaghi");
		Forward f5 = new Forward("Luca", "Toni");
		Forward f6 = new Forward("Christian", "Vieri");

		List<Player> players = List.of(
				gk1, gk2, gk3, gk4, 
				d1, d2, d3, d4, d5, d6, d7,
				m1, m2, m3, m4, m5, m6,
				f1, f2, f3, f4, f5, f6);

		players.forEach(repositoryEntityManager::persist);

		// GIVEN a LineUp's and its associated entities are persisted
		Set<Contract> contracts = new HashSet<Contract>();
		FantaTeam team = new FantaTeam("Dream Team", league, 30, manager, contracts);
		players.stream().map(p -> new Contract(team, p)).forEach(contracts::add);

		repositoryEntityManager.persist(team);

		Match match = new Match(matchDay, team, opponent);
		repositoryEntityManager.persist(match);

		repositoryEntityManager.persist(LineUp.build()
				.forTeam(team)
				.inMatch(match)
				.withStarterLineUp(Scheme433.starterLineUp()
						.withGoalkeeper(gk1)
						.withDefenders(d1, d2, d3, d4)
						.withMidfielders(m1, m2, m3)
						.withForwards(f1, f2, f3))
				.withSubstituteGoalkeepers(gk2, gk3, gk4)
				.withSubstituteDefenders(d5, d6, d7)
				.withSubstituteMidfielders(m4, m5, m6)
				.withSubstituteForwards(f4, f5, f6));
		repositoryEntityManager.getTransaction().commit();
		repositoryEntityManager.clear(); // the Session is not closed! SUT instance is still used for verifications

		// WHEN the SUT is used to retrieve a LineUp
		Optional<LineUp> retrieved = lineUpRepository.getLineUpByMatchAndTeam(league, match, team);
		
		// THEN the LineUp retrieved through the SUT is the expected one
		assertThat(retrieved).isPresent();

		LineUp persistedLineUp = retrieved.get();
		assertThat(persistedLineUp.getMatch()).isEqualTo(match);
		assertThat(persistedLineUp.getTeam()).isEqualTo(team);

		assertThat(persistedLineUp.extract().starterGoalkeepers()).containsExactly(gk1);
		assertThat(persistedLineUp.extract().starterDefenders()).containsExactlyInAnyOrder(d1, d2, d3, d4);
		assertThat(persistedLineUp.extract().starterMidfielders()).containsExactlyInAnyOrder(m1, m2, m3);
		assertThat(persistedLineUp.extract().starterForwards()).containsExactlyInAnyOrder(f1, f2, f3);

		assertThat(persistedLineUp.extract().substituteGoalkeepers()).containsExactly(gk2, gk3, gk4);
		assertThat(persistedLineUp.extract().substituteDefenders()).containsExactly(d5, d6, d7);
		assertThat(persistedLineUp.extract().substituteMidfielders()).containsExactly(m4, m5, m6);
		assertThat(persistedLineUp.extract().substituteForwards()).containsExactly(f4, f5, f6);
	}

	@Test
	@DisplayName("getLineUpByMatchAndTeam should return empty if no lineup exists")
	void testGetLineUpByMatchAndTeamEmpty() {
		repositoryEntityManager.getTransaction().begin();

		// GIVEN a LineUp's natural identifier is persisted, but NO LineUp is
		FantaTeam team = new FantaTeam("Dream Team", league, 30, manager, new HashSet<Contract>());
		repositoryEntityManager.persist(team);

		Match match = new Match(matchDay, team, opponent);
		repositoryEntityManager.persist(match);

		repositoryEntityManager.getTransaction().commit();

		// WHEN the SUT is used to retrieve a LineUp for the persisted natural identifier
		Optional<LineUp> result = lineUpRepository.getLineUpByMatchAndTeam(league, match, team);
		
		// THEN the SUT correctly indicates no LineUp can be retrieved
		assertThat(result).isEmpty();
	}
}
