package integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import domain.*;
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

import business.NewsPaperService;
import dal.transaction.jpa.JpaTransactionManager;
import domain.Player.Club;

/**
 * integrates {@link NewsPaperService} with {@link JpaTransactionManager} and,
 * consequently, JPA Entity Repositories
 */
@DisplayName("a NewsPaperService")
public class NewsPaperServiceIT {

	private static SessionFactory sessionFactory;
	private JpaTransactionManager transactionManager;
	private NewsPaperService newspaperService;

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
					.addAnnotatedClass(League.class)
					.addAnnotatedClass(MatchDay.class)
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
		newspaperService = new NewsPaperService(transactionManager);
	}

	@AfterAll
	static void tearDown() {
		sessionFactory.close();
	}

	@Test
	@DisplayName("can retrieve Players to be graded in a League")
	public void getPlayersToGrade() {

		// GIVEN some Contracts are added to the system
		FantaUser admin = new FantaUser("mail", "pswd");
		League league = new League(admin, "Lega", "codice");
		Player player1 = new Player.Forward("player", "1", Club.ATALANTA);
		Player player2 = new Player.Forward("player", "2", Club.ATALANTA);
		FantaTeam teamA = new FantaTeam("", league, 0, admin, new HashSet<>());
		teamA.getContracts().add(new Contract(teamA, player1));
		FantaTeam teamB = new FantaTeam("", league, 0, admin, new HashSet<>());
		teamB.getContracts().add(new Contract(teamB, player2));
//		Match match = new Match(matchDay, teamA, teamB);

		transactionManager.inTransaction(context -> {
			context.getFantaUserRepository().saveFantaUser(admin);
	        context.getLeagueRepository().saveLeague(league);
	        context.getPlayerRepository().addPlayer(player1);
	        context.getPlayerRepository().addPlayer(player2);
	        context.getTeamRepository().saveTeam(teamA);
	        context.getTeamRepository().saveTeam(teamB);
		});

		// WHEN
		Set<Player> players = newspaperService.getPlayersToGrade(league);
		
		// THEN
		assertThat(players).containsExactlyInAnyOrder(player1, player2);
	}
	
	@Test
	@DisplayName("can assign Grade to Players")
	public void assignGradesToPlayers() {

		// GIVEN some Contracts are added to the system
		FantaUser admin = new FantaUser("mail", "pswd");
		League league = new League(admin, "Lega", "codice");
		Player player1 = new Player.Forward("player", "1", Club.ATALANTA);
		Player player2 = new Player.Forward("player", "2", Club.ATALANTA);
		FantaTeam teamA = new FantaTeam("", league, 0, admin, new HashSet<>());
		teamA.getContracts().add(new Contract(teamA, player1));
		FantaTeam teamB = new FantaTeam("", league, 0, admin, new HashSet<>());
		teamB.getContracts().add(new Contract(teamB, player2));

		// AND the League is in 'ongoing-MatchDay' state
		MatchDay latestEnded = new MatchDay("prima giornata", 1, MatchDay.Status.PAST, league);
		MatchDay ongoing = new MatchDay("seconda giornata", 2, MatchDay.Status.PRESENT, league);
		Match match = new Match(ongoing, teamA, teamB);

		transactionManager.inTransaction(context -> {
			context.getFantaUserRepository().saveFantaUser(admin);
			context.getLeagueRepository().saveLeague(league);
			context.getPlayerRepository().addPlayer(player1);
			context.getPlayerRepository().addPlayer(player2);
			context.getMatchDayRepository().saveMatchDay(latestEnded);
			context.getMatchDayRepository().saveMatchDay(ongoing);
			context.getTeamRepository().saveTeam(teamA);
			context.getTeamRepository().saveTeam(teamB);
			context.getMatchRepository().saveMatch(match);
		});

		// WHEN
		Grade grade1 = new Grade(player1, ongoing, 23.0);
		Grade grade2 = new Grade(player2, ongoing, 20.0);
		newspaperService.save(Set.of(grade1, grade2));

		// THEN
		List<Grade> persisted = transactionManager.fromTransaction(c -> c.getGradeRepository().getAllGrades(ongoing));
		assertThat(persisted).containsExactlyInAnyOrder(grade2, grade1);
	}
}
