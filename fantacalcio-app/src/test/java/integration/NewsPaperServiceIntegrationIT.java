package integration;

import static org.assertj.core.api.Assertions.assertThat;
import java.time.LocalDate;
import java.util.Set;

import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import business.NewsPaperService;
import dal.repository.jpa.JpaFantaTeamRepository;
import dal.repository.jpa.JpaFantaUserRepository;
import dal.repository.jpa.JpaGradeRepository;
import dal.repository.jpa.JpaLeagueRepository;
import dal.repository.jpa.JpaMatchRepository;
import dal.repository.jpa.JpaNewsPaperRepository;
import dal.repository.jpa.JpaPlayerRepository;
import dal.transaction.jpa.JpaTransactionManager;
import domain.Contract;
import domain.FantaTeam;
import domain.FantaUser;
import domain.Fielding;
import domain.Grade;
import domain.League;
import domain.LineUp;
import domain.Match;
import domain.MatchDaySerieA;
import domain.NewsPaper;
import domain.Player;
import domain.Result;
import domain.Player.Club;
import jakarta.persistence.EntityManager;

public class NewsPaperServiceIntegrationIT {

	private static SessionFactory sessionFactory;
	private JpaTransactionManager transactionManager;
	private NewsPaperService newspaperservice;
	private EntityManager entityManager;

	private JpaFantaUserRepository fantaUserRepository;
	private JpaMatchRepository matchRepository;
	private JpaGradeRepository gradeRepository;
	private JpaFantaTeamRepository fantaTeamRepository;
	private JpaLeagueRepository leagueRepository;
	private JpaPlayerRepository playerRepository;
	private JpaNewsPaperRepository newspaperRepository;

	@BeforeAll
	static void initializeSessionFactory() {

		try {
			StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
					.configure("hibernate-test.cfg.xml").build();

			Metadata metadata = new MetadataSources(serviceRegistry)
					.addAnnotatedClass(Contract.class)
					.addAnnotatedClass(FantaTeam.class)
					.addAnnotatedClass(Player.class)
					.addAnnotatedClass(Player.Goalkeeper.class)
					.addAnnotatedClass(Player.Defender.class)
					.addAnnotatedClass(Player.Midfielder.class)
					.addAnnotatedClass(Player.Forward.class)
					.addAnnotatedClass(FantaUser.class)
					.addAnnotatedClass(NewsPaper.class)
					.addAnnotatedClass(League.class)
					.addAnnotatedClass(MatchDaySerieA.class)
					.addAnnotatedClass(Match.class)
					.addAnnotatedClass(Fielding.class)
					.addAnnotatedClass(LineUp.class)
					.addAnnotatedClass(Result.class)
					.addAnnotatedClass(Grade.class)
					.getMetadataBuilder().build();

			sessionFactory = metadata.getSessionFactoryBuilder().build();

		} catch (Throwable ex) {
			throw new ExceptionInInitializerError(ex);
		}

	}

	@BeforeEach
	void setup() {

		sessionFactory.getSchemaManager().truncateMappedObjects();
		transactionManager = new JpaTransactionManager(sessionFactory);
		entityManager = sessionFactory.createEntityManager();

		fantaUserRepository = new JpaFantaUserRepository(entityManager);
		matchRepository = new JpaMatchRepository(entityManager);
		gradeRepository = new JpaGradeRepository(entityManager);
		fantaTeamRepository = new JpaFantaTeamRepository(entityManager);
		leagueRepository = new JpaLeagueRepository(entityManager);
		playerRepository = new JpaPlayerRepository(entityManager);
		newspaperRepository = new JpaNewsPaperRepository(entityManager);

	}

	@AfterAll
	static void tearDown() {
		sessionFactory.close();
	}

	@Test
	public void assignGradesToPlayers() {

		entityManager.getTransaction().begin();

		FantaUser admin = new FantaUser("mail", "pswd");
		fantaUserRepository.saveFantaUser(admin);

		NewsPaper newsPaper = new NewsPaper("Gazzetta");
		newspaperRepository.saveNewsPaper(newsPaper);

		Player player = new Player.Forward("player", "1", Club.ATALANTA);
		Player player2 = new Player.Forward("player", "2", Club.ATALANTA);
		playerRepository.addPlayer(player);
		playerRepository.addPlayer(player2);

		MatchDaySerieA previousDay = new MatchDaySerieA("prima giornata", LocalDate.of(2020, 1, 13), 1);
		MatchDaySerieA matchDay = new MatchDaySerieA("seconda giornata", LocalDate.of(2025, 9, 20), 2);
		MatchDaySerieA nextDay = new MatchDaySerieA("terza giornata", LocalDate.of(2020, 1, 26), 3);
		entityManager.persist(previousDay);
		entityManager.persist(matchDay);
		entityManager.persist(nextDay);

		League league = new League(admin, "lega", newsPaper, "1234");
		leagueRepository.saveLeague(league);

		FantaTeam team1 = new FantaTeam("", league, 0, admin, Set.of());
		FantaTeam team2 = new FantaTeam("", league, 0, admin, Set.of());
		fantaTeamRepository.saveTeam(team1);
		fantaTeamRepository.saveTeam(team2);

		Match match = new Match(matchDay, team1, team2);
		matchRepository.saveMatch(match);

		Grade grade = new Grade(player, matchDay, 25.0, newsPaper);
		Grade grade2 = new Grade(player2, matchDay, 20.0, newsPaper);

		entityManager.getTransaction().commit();

		newspaperservice = new NewsPaperService(transactionManager) {
			@Override
			protected LocalDate today() {
				return LocalDate.of(2025, 9, 22); // after 21/09
			}
		};

		Set<Player> players = newspaperservice.getPlayersToGrade(Player.Club.ATALANTA);
		assertThat(players.size()).isEqualTo(2);
		assertThat(players).anyMatch(t -> t.getName() == "player" && t.getSurname() == "1");
		assertThat(players).anyMatch(t -> t.getName() == "player" && t.getSurname() == "2");

		newspaperservice.setVoteToPlayers(Set.of(grade, grade2));
		assertThat(gradeRepository.getAllMatchGrades(match, newsPaper));
	}

}
