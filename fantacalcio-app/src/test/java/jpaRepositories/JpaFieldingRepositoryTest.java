package jpaRepositories;

import static org.assertj.core.api.Assertions.assertThat;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import domainModel.scheme.Scheme433;
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

import domainModel.Contract;
import domainModel.FantaTeam;
import domainModel.FantaUser;
import domainModel.Fielding;
import domainModel.League;
import domainModel.LineUp;
import domainModel.Match;
import domainModel.MatchDaySerieA;
import domainModel.NewsPaper;
import domainModel.Player;
import domainModel.Player.Defender;
import domainModel.Player.Forward;
import domainModel.Player.Goalkeeper;
import domainModel.Player.Midfielder;
import jakarta.persistence.EntityManager;
import domainModel.Player.Club;

class JpaFieldingRepositoryTest {

	private static SessionFactory sessionFactory;
	private JpaFieldingRepository fieldingRepository;
	private EntityManager entityManager;
	private League league;
	private FantaUser manager;
	private MatchDaySerieA matchDay;
	private NewsPaper newsPaper;
	private FantaTeam opponent;

	@BeforeAll
	static void initializeSessionFactory() {
		try {
			StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
					.configure("hibernate-test.cfg.xml").build();

			Metadata metadata = new MetadataSources(serviceRegistry)
					.addAnnotatedClass(LineUp.class).addAnnotatedClass(MatchDaySerieA.class)
					.addAnnotatedClass(NewsPaper.class).addAnnotatedClass(FantaUser.class)
					.addAnnotatedClass(Match.class).addAnnotatedClass(FantaTeam.class).addAnnotatedClass(Fielding.class)
					.addAnnotatedClass(Fielding.StarterFielding.class)
					.addAnnotatedClass(Fielding.SubstituteFielding.class).addAnnotatedClass(Goalkeeper.class)
					.addAnnotatedClass(Defender.class).addAnnotatedClass(Midfielder.class)
					.addAnnotatedClass(Forward.class).addAnnotatedClass(Contract.class).addAnnotatedClass(League.class)
					.addAnnotatedClass(Fielding.class).getMetadataBuilder().build();

			sessionFactory = metadata.getSessionFactoryBuilder().build();

		} catch (Throwable ex) {
			throw new ExceptionInInitializerError(ex);
		}
	}

	@BeforeEach
	void setup() {
		sessionFactory.getSchemaManager().truncateMappedObjects();
		entityManager = sessionFactory.createEntityManager();
		fieldingRepository = new JpaFieldingRepository(entityManager);

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
	static void tear() {
		sessionFactory.close();
	}

	@Test
	@DisplayName("getAllFieldings() with some fieldings in the lineup")
	public void testGetAllFieldings() {
		entityManager.getTransaction().begin();

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

		Forward f1 = new Forward("Roberto", "Baggio", Club.ROMA);
		Forward f2 = new Forward("Francesco", "Totti", Club.ROMA);
		Forward f3 = new Forward("Alessandro", "Del Piero", Club.JUVENTUS);
		Forward f4 = new Forward("Lorenzo", "Insigne", Club.NAPOLI);

		List<Player> players = List.of(gk1, gk2, d1, d2, d3, d4, d5, m1, m2, m3, m4, f1, f2, f3, f4);
		
		players.forEach(entityManager::persist);

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
						.withGoalkeeper(new Goalkeeper("portiere", "titolare", Player.Club.ATALANTA))
						.withDefenders(
								new Defender("difensore1", "titolare", Player.Club.ATALANTA),
								new Defender("difensore2", "titolare", Player.Club.ATALANTA),
								new Defender("difensore3", "titolare", Player.Club.ATALANTA),
								new Defender("difensore4", "titolare", Player.Club.ATALANTA))
						.withMidfielders(
								new Midfielder("centrocampista1", "titolare", Player.Club.ATALANTA),
								new Midfielder("centrocampista2", "titolare", Player.Club.ATALANTA),
								new Midfielder("centrocampista3", "titolare", Player.Club.ATALANTA))
						.withForwards(
								new Forward("attaccante1", "titolare", Player.Club.ATALANTA),
								new Forward("attaccante2", "titolare", Player.Club.ATALANTA),
								new Forward("attaccante3", "titolare", Player.Club.ATALANTA)))
				.withSubstituteGoalkeepers(
						new Goalkeeper("portiere1", "panchina", Player.Club.ATALANTA),
						new Goalkeeper("portiere2", "panchina", Player.Club.ATALANTA),
						new Goalkeeper("portiere3", "panchina", Player.Club.ATALANTA))
				.withSubstituteDefenders(
						new Defender("difensore1", "panchina", Player.Club.ATALANTA),
						new Defender("difensore2", "panchina", Player.Club.ATALANTA),
						new Defender("difensore3", "panchina", Player.Club.ATALANTA))
				.withSubstituteMidfielders(
						new Midfielder("centrocampista1", "panchina", Player.Club.ATALANTA),
						new Midfielder("centrocampista2", "panchina", Player.Club.ATALANTA),
						new Midfielder("centrocampista3", "panchina", Player.Club.ATALANTA))
				.withSubstituteForwards(
						new Forward("attaccante1", "panchina", Player.Club.ATALANTA),
						new Forward("attaccante2", "panchina", Player.Club.ATALANTA),
						new Forward("attaccante3", "panchina", Player.Club.ATALANTA));
		entityManager.persist(lineUp);

		entityManager.getTransaction().commit();
		entityManager.clear(); // the Session is not closed! SUT instance is still used for verifications
		
		List<Fielding> result = fieldingRepository.getAllFieldings(lineUp);
        assertThat(result).hasSize(players.size());
	}

}
