package dal.repository.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

import domain.Contract;
import domain.FantaTeam;
import domain.FantaUser;
import domain.Fielding;
import domain.League;
import domain.LineUp;
import domain.Match;
import domain.MatchDaySerieA;
import domain.NewsPaper;
import domain.Player;
import domain.Player.Defender;
import domain.Player.Forward;
import domain.Player.Goalkeeper;
import domain.Player.Midfielder;
import domain.scheme.Scheme433;
import jakarta.persistence.EntityManager;

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
					.addAnnotatedClass(Fielding.class)
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
		fieldingRepository = new JpaFieldingRepository(entityManager);

		sessionFactory.inTransaction(t -> {
			manager = new FantaUser("manager@example.com", "securePass");
			t.persist(manager);
			newsPaper = new NewsPaper("Gazzetta");
			t.persist(newsPaper);
			league = new League(manager, "Serie A", newsPaper, "code");
			t.persist(league);
			matchDay = new MatchDaySerieA("Matchday 1", LocalDate.of(2025, 6, 19), 1);
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
		entityManager.clear(); // the Session is not closed! SUT instance is still used for verifications
		
		List<Fielding> result = fieldingRepository.getAllFieldings(lineUp);
        assertThat(result).hasSize(players.size());
	}

}
