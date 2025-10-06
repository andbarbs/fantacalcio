package jpaRepositories;

import domainModel.*;
import domainModel.scheme.Scheme433;
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
	@DisplayName("deleteLineUp should remove the LineUp by match and team")
	void testDeleteLineUpRemovesCorrectly() {
	    entityManager.getTransaction().begin();

		Goalkeeper gk1 = new Goalkeeper("portiere", "titolare", Player.Club.ATALANTA);

		Defender d1 = new Defender("difensore1", "titolare", Player.Club.ATALANTA);
		Defender d2 = new Defender("difensore2", "titolare", Player.Club.ATALANTA);
		Defender d3 = new Defender("difensore3", "titolare", Player.Club.ATALANTA);
		Defender d4 = new Defender("difensore4", "titolare", Player.Club.ATALANTA);

		Midfielder m1 = new Midfielder("centrocampista1", "titolare", Player.Club.ATALANTA);
		Midfielder m2 = new Midfielder("centrocampista2", "titolare", Player.Club.ATALANTA);
		Midfielder m3 = new Midfielder("centrocampista3", "titolare", Player.Club.ATALANTA);

		Forward f1 = new Forward("attaccante1", "titolare", Player.Club.ATALANTA);
		Forward f2 = new Forward("attaccante2", "titolare", Player.Club.ATALANTA);
		Forward f3 = new Forward("attaccante3", "titolare", Player.Club.ATALANTA);

		Goalkeeper sgk1 = new Goalkeeper("portiere1", "panchina", Player.Club.ATALANTA);
		Goalkeeper sgk2 = new Goalkeeper("portiere2", "panchina", Player.Club.ATALANTA);
		Goalkeeper sgk3 = new Goalkeeper("portiere3", "panchina", Player.Club.ATALANTA);

		Defender sd1 = new Defender("difensore1", "panchina", Player.Club.ATALANTA);
		Defender sd2 = new Defender("difensore2", "panchina", Player.Club.ATALANTA);
		Defender sd3 = new Defender("difensore3", "panchina", Player.Club.ATALANTA);

		Midfielder sm1 = new Midfielder("centrocampista1", "panchina", Player.Club.ATALANTA);
		Midfielder sm2 = new Midfielder("centrocampista2", "panchina", Player.Club.ATALANTA);
		Midfielder sm3 = new Midfielder("centrocampista3", "panchina", Player.Club.ATALANTA);

		Forward sf1 = new Forward("attaccante1", "panchina", Player.Club.ATALANTA);
		Forward sf2 = new Forward("attaccante2", "panchina", Player.Club.ATALANTA);
		Forward sf3 = new Forward("attaccante3", "panchina", Player.Club.ATALANTA);

		List<Player> players = List.of(
				gk1, 
				d1, d2, d3, d4, 
				m1, m2, m3, 
				f1, f2, f3, 
				sgk1, sgk2, sgk3, 
				sd1, sd2, sd3,
				sm1, sm2, sm3,
				sf1, sf2, sf3);

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

		LineUp lineUp = LineUp.build()
				.forTeam(team)
				.inMatch(match)
				.withStarterLineUp(Scheme433.starterLineUp()
						.withGoalkeeper(gk1)
						.withDefenders(d1, d2, d3, d4)
						.withMidfielders(m1, m2, m3)
						.withForwards(f1, f2, f3))
				.withSubstituteGoalkeepers(sgk1, sgk2, sgk3)
				.withSubstituteDefenders(sd1, sd2, sd3)
				.withSubstituteMidfielders(sm1, sm2, sm3)
				.withSubstituteForwards(sf1, sf2, sf3);
		entityManager.persist(lineUp);
	    entityManager.getTransaction().commit();
	    entityManager.clear();

	    // Verify it exists
	    Optional<LineUp> beforeDelete = lineUpRepository.getLineUpByMatchAndTeam(match, team);
	    assertThat(beforeDelete).isPresent();

	    // Delete
	    entityManager.getTransaction().begin();
	    lineUpRepository.deleteLineUp(lineUp);
	    entityManager.getTransaction().commit();
	    entityManager.clear();

	    // Verify it is gone
	    Optional<LineUp> afterDelete = lineUpRepository.getLineUpByMatchAndTeam(match, team);
	    assertThat(afterDelete).isEmpty();
	}

	
	@Test
	@DisplayName("saveLineUp should persist a LineUp correctly")
	void testSaveLineUpPersistsCorrectly() {
		entityManager.getTransaction().begin();
		
		// Players
		Goalkeeper gk1 = new Goalkeeper("portiere", "titolare", Player.Club.ATALANTA);

		Defender d1 = new Defender("difensore1", "titolare", Player.Club.ATALANTA);
		Defender d2 = new Defender("difensore2", "titolare", Player.Club.ATALANTA);
		Defender d3 = new Defender("difensore3", "titolare", Player.Club.ATALANTA);
		Defender d4 = new Defender("difensore4", "titolare", Player.Club.ATALANTA);

		Midfielder m1 = new Midfielder("centrocampista1", "titolare", Player.Club.ATALANTA);
		Midfielder m2 = new Midfielder("centrocampista2", "titolare", Player.Club.ATALANTA);
		Midfielder m3 = new Midfielder("centrocampista3", "titolare", Player.Club.ATALANTA);

		Forward f1 = new Forward("attaccante1", "titolare", Player.Club.ATALANTA);
		Forward f2 = new Forward("attaccante2", "titolare", Player.Club.ATALANTA);
		Forward f3 = new Forward("attaccante3", "titolare", Player.Club.ATALANTA);

		Goalkeeper sgk1 = new Goalkeeper("portiere1", "panchina", Player.Club.ATALANTA);
		Goalkeeper sgk2 = new Goalkeeper("portiere2", "panchina", Player.Club.ATALANTA);
		Goalkeeper sgk3 = new Goalkeeper("portiere3", "panchina", Player.Club.ATALANTA);

		Defender sd1 = new Defender("difensore1", "panchina", Player.Club.ATALANTA);
		Defender sd2 = new Defender("difensore2", "panchina", Player.Club.ATALANTA);
		Defender sd3 = new Defender("difensore3", "panchina", Player.Club.ATALANTA);

		Midfielder sm1 = new Midfielder("centrocampista1", "panchina", Player.Club.ATALANTA);
		Midfielder sm2 = new Midfielder("centrocampista2", "panchina", Player.Club.ATALANTA);
		Midfielder sm3 = new Midfielder("centrocampista3", "panchina", Player.Club.ATALANTA);

		Forward sf1 = new Forward("attaccante1", "panchina", Player.Club.ATALANTA);
		Forward sf2 = new Forward("attaccante2", "panchina", Player.Club.ATALANTA);
		Forward sf3 = new Forward("attaccante3", "panchina", Player.Club.ATALANTA);

		List<Player> players = List.of(
				gk1, 
				d1, d2, d3, d4, 
				m1, m2, m3, 
				f1, f2, f3, 
				sgk1, sgk2, sgk3, 
				sd1, sd2, sd3,
				sm1, sm2, sm3,
				sf1, sf2, sf3);

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
		lineUpRepository.saveLineUp(LineUp.build()
				.forTeam(team)
				.inMatch(match)
				.withStarterLineUp(Scheme433.starterLineUp()
						.withGoalkeeper(gk1)
						.withDefenders(d1, d2, d3, d4)
						.withMidfielders(m1, m2, m3)
						.withForwards(f1, f2, f3))
				.withSubstituteGoalkeepers(sgk1, sgk2, sgk3)
				.withSubstituteDefenders(sd1, sd2, sd3)
				.withSubstituteMidfielders(sm1, sm2, sm3)
				.withSubstituteForwards(sf1, sf2, sf3));
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
		Goalkeeper gk1 = new Goalkeeper("portiere", "titolare", Player.Club.ATALANTA);

		Defender d1 = new Defender("difensore1", "titolare", Player.Club.ATALANTA);
		Defender d2 = new Defender("difensore2", "titolare", Player.Club.ATALANTA);
		Defender d3 = new Defender("difensore3", "titolare", Player.Club.ATALANTA);
		Defender d4 = new Defender("difensore4", "titolare", Player.Club.ATALANTA);

		Midfielder m1 = new Midfielder("centrocampista1", "titolare", Player.Club.ATALANTA);
		Midfielder m2 = new Midfielder("centrocampista2", "titolare", Player.Club.ATALANTA);
		Midfielder m3 = new Midfielder("centrocampista3", "titolare", Player.Club.ATALANTA);

		Forward f1 = new Forward("attaccante1", "titolare", Player.Club.ATALANTA);
		Forward f2 = new Forward("attaccante2", "titolare", Player.Club.ATALANTA);
		Forward f3 = new Forward("attaccante3", "titolare", Player.Club.ATALANTA);

		Goalkeeper sgk1 = new Goalkeeper("portiere1", "panchina", Player.Club.ATALANTA);
		Goalkeeper sgk2 = new Goalkeeper("portiere2", "panchina", Player.Club.ATALANTA);
		Goalkeeper sgk3 = new Goalkeeper("portiere3", "panchina", Player.Club.ATALANTA);

		Defender sd1 = new Defender("difensore1", "panchina", Player.Club.ATALANTA);
		Defender sd2 = new Defender("difensore2", "panchina", Player.Club.ATALANTA);
		Defender sd3 = new Defender("difensore3", "panchina", Player.Club.ATALANTA);

		Midfielder sm1 = new Midfielder("centrocampista1", "panchina", Player.Club.ATALANTA);
		Midfielder sm2 = new Midfielder("centrocampista2", "panchina", Player.Club.ATALANTA);
		Midfielder sm3 = new Midfielder("centrocampista3", "panchina", Player.Club.ATALANTA);

		Forward sf1 = new Forward("attaccante1", "panchina", Player.Club.ATALANTA);
		Forward sf2 = new Forward("attaccante2", "panchina", Player.Club.ATALANTA);
		Forward sf3 = new Forward("attaccante3", "panchina", Player.Club.ATALANTA);

		List<Player> players = List.of(
				gk1, 
				d1, d2, d3, d4, 
				m1, m2, m3, 
				f1, f2, f3, 
				sgk1, sgk2, sgk3, 
				sd1, sd2, sd3,
				sm1, sm2, sm3,
				sf1, sf2, sf3);

		players.forEach(entityManager::persist);

		// Contracts, FantaTeam and Match
		Set<Contract> contracts = new HashSet<Contract>();
		FantaTeam team = new FantaTeam("Dream Team", league, 30, manager, contracts);
		players.stream().map(p -> new Contract(team, p)).forEach(contracts::add);

		entityManager.persist(team);

		Match match = new Match(matchDay, team, opponent);
		entityManager.persist(match);

		LineUp lineUp = LineUp.build()
				.forTeam(team)
				.inMatch(match)
				.withStarterLineUp(Scheme433.starterLineUp()
						.withGoalkeeper(gk1)
						.withDefenders(d1, d2, d3, d4)
						.withMidfielders(m1, m2, m3)
						.withForwards(f1, f2, f3))
				.withSubstituteGoalkeepers(sgk1, sgk2, sgk3)
				.withSubstituteDefenders(sd1, sd2, sd3)
				.withSubstituteMidfielders(sm1, sm2, sm3)
				.withSubstituteForwards(sf1, sf2, sf3);
				
		// Build LineUp with starters and substitutes, save and commit
		entityManager.persist(lineUp);
		entityManager.getTransaction().commit();
		entityManager.clear(); // the Session is not closed! SUT instance is still used for verifications

		// Verify
		Optional<LineUp> retrieved = lineUpRepository.getLineUpByMatchAndTeam(match, team);
		assertThat(retrieved).isPresent();

		LineUp persistedLineUp = retrieved.get();
		assertThat(persistedLineUp.getTeam()).isEqualTo(team);
		assertThat(persistedLineUp.getMatch()).isEqualTo(match);
		assertThat(persistedLineUp.getMatch()).isEqualTo(match);
		assertThat(persistedLineUp.getTeam()).isEqualTo(team);

		assertThat(persistedLineUp.extract().starterGoalkeepers()).containsExactly(gk1);
		assertThat(persistedLineUp.extract().starterDefenders()).containsExactlyInAnyOrder(d1, d2, d3, d4);
		assertThat(persistedLineUp.extract().starterMidfielders()).containsExactlyInAnyOrder(m1, m2, m3);
		assertThat(persistedLineUp.extract().starterForwards()).containsExactlyInAnyOrder(f1, f2, f3);

		assertThat(persistedLineUp.extract().substituteGoalkeepers()).containsExactly(sgk1, sgk2, sgk3);
		assertThat(persistedLineUp.extract().substituteDefenders()).containsExactly(sd1, sd2, sd3);
		assertThat(persistedLineUp.extract().substituteMidfielders()).containsExactly(sm1, sm2, sm3);
		assertThat(persistedLineUp.extract().substituteForwards()).containsExactly(sf1, sf2, sf3);
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
