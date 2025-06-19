package jpaRepositories;

import domainModel.*;
import jakarta.persistence.EntityManager;
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
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

class JpaLineUpRepositoryTest {

	private static SessionFactory sessionFactory;
	private EntityManager entityManager;
	private JpaLineUpRepository lineUpRepository;

	@BeforeAll
	static void initializeSessionFactory() {
		try {
			StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
					.configure("hibernate-test.cfg.xml")
					.build();

			Metadata metadata = new MetadataSources(serviceRegistry)
					.addAnnotatedClass(_433LineUp.class)
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
					.getMetadataBuilder()
					.build();

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
	}

	@AfterAll
	static void tearDown() {
		sessionFactory.close();
	}

	@Test
	@DisplayName("saveLineUp and getLineUpByMatchAndTeam should persist and fetch correctly")
	void testSaveAndRetrieveLineUp() {
		entityManager.getTransaction().begin();

		// League and manager
		FantaUser manager = new FantaUser("manager@example.com", "securePass");
		entityManager.persist(manager);
		NewsPaper newsPaper = new NewsPaper("Gazzetta");
		entityManager.persist(newsPaper);
		League league = new League(manager,"Serie A", newsPaper, "code");
		entityManager.persist(league);


		// Players
		Goalkeeper gk1 = new Goalkeeper("Gianluigi", "Buffon");
		Goalkeeper gk2 = new Goalkeeper("Samir", "Handanović");

		Defender d1 = new Defender("Paolo", "Maldini");
		Defender d2 = new Defender("Franco", "Baresi");
		Defender d3 = new Defender("Alessandro", "Nesta");
		Defender d4 = new Defender("Giorgio", "Chiellini");
		Defender d5 = new Defender("Leonardo", "Bonucci");

		Midfielder m1 = new Midfielder("Andrea", "Pirlo");
		Midfielder m2 = new Midfielder("Daniele", "De Rossi");
		Midfielder m3 = new Midfielder("Marco", "Verratti");
		Midfielder m4 = new Midfielder("Claudio", "Marchisio");

		Forward f1 = new Forward("Roberto", "Baggio");
		Forward f2 = new Forward("Francesco", "Totti");
		Forward f3 = new Forward("Alessandro", "Del Piero");
		Forward f4 = new Forward("Lorenzo", "Insigne");

		List<Player> players = List.of(
				gk1, gk2,
				d1, d2, d3, d4, d5,
				m1, m2, m3, m4,
				f1, f2, f3, f4
		);
		players.forEach(entityManager::persist);

		// Contracts and FantaTeam
		FantaTeam team = new FantaTeam("Dream Team", league, 30, manager, null);
		Set<Contract> contracts = players.stream()
				.map(p -> new Contract(team, p)) // temporary null, patched below
				.collect(Collectors.toSet());
		team.setContracts(contracts);


		entityManager.persist(team);
		contracts.forEach(entityManager::persist);

		// Opponent and Match
		FantaTeam opponent = new FantaTeam("Challengers", league, 25, manager, new HashSet<>());
		entityManager.persist(opponent);

		MatchDaySerieA matchDay = new MatchDaySerieA("Matchday 1", LocalDate.of(2025, 6, 19));
		entityManager.persist(matchDay);

		Match match = new Match(matchDay, team, opponent);
		entityManager.persist(match);

		// Build LineUp with starters and substitutes
		_433LineUp lineUp = new _433LineUp._443LineUpBuilder(match, team)
				.withGoalkeeper(gk1)
				.withDefenders(d1, d2, d3, d4)
				.withMidfielders(m1, m2, m3)
				.withForwards(f1, f2, f3)
				.withSubstituteGoalkeepers(List.of(gk2))
				.withSubstituteDefenders(List.of(d5))
				.withSubstituteMidfielders(List.of(m4))
				.withSubstituteForwards(List.of(f4))
				.build();

		// Save and commit
		lineUpRepository.saveLineUp(lineUp);
		entityManager.getTransaction().commit();
		entityManager.clear();

		// Verify
		Optional<LineUp> retrieved = lineUpRepository.getLineUpByMatchAndTeam(league, match, team);
		assertThat(retrieved).isPresent();
		LineUp persisted = retrieved.get();

		assertThat(persisted.getMatch()).isEqualTo(match);
		assertThat(persisted.getTeam()).isEqualTo(team);

		// Extract the LineUpViewer instance
		LineUpViewer viewer = lineUp.extract();

		// Validate starters:
		// Since there's only one goalkeeper, using containsExactly is fine.
		assertThat(viewer.starterGoalkeepers()).containsExactly(gk1);
		// For defenders, midfielders, and forwards which are returned as Sets,
		// the order is not guaranteed; therefore, containsExactlyInAnyOrder is safer.
		assertThat(viewer.starterDefenders()).containsExactlyInAnyOrder(d1, d2, d3, d4);
		assertThat(viewer.starterMidfielders()).containsExactlyInAnyOrder(m1, m2, m3);
		assertThat(viewer.starterForwards()).containsExactlyInAnyOrder(f1, f2, f3);

		// Validate substitutes (which are returned as Lists, and ordering may be significant)
		assertThat(viewer.substituteGoalkeepers()).containsExactly(gk2);
		assertThat(viewer.substituteDefenders()).containsExactly(d5);
		assertThat(viewer.substituteMidfielders()).containsExactly(m4);
		assertThat(viewer.substituteForwards()).containsExactly(f4);
	}

	}
/*
	@Test
	@DisplayName("getLineUpByMatchAndTeam should return empty if no lineup exists")
	void testGetLineUpByMatchAndTeamEmpty() {
		entityManager.getTransaction().begin();

		League league = new League("Serie A");
		Match match = new Match(1, "Matchday 1", league);
		FantaTeam team = new FantaTeam("Team B", league);

		entityManager.persist(league);
		entityManager.persist(match);
		entityManager.persist(team);

		entityManager.getTransaction().commit();

		Optional<LineUp> result = lineUpRepository.getLineUpByMatchAndTeam(league, match, team);
		assertThat(result).isEmpty();
	}
}
*/
