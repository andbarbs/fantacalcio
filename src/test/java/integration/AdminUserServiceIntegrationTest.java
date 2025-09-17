package integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
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

import businessLogic.AdminUserService;
import businessLogic.JpaTransactionManager;
import businessLogic.TransactionContext;
import businessLogic.repositories.MatchRepository;
import domainModel.Contract;
import domainModel.FantaTeam;
import domainModel.FantaUser;
import domainModel.Grade;
import domainModel.League;
import domainModel.LineUp;
import domainModel.Match;
import domainModel.MatchDaySerieA;
import domainModel.NewsPaper;
import domainModel.Player;
import domainModel._433LineUp;

class AdminUserServiceIntegrationTest {

	private static SessionFactory sessionFactory;
	private AdminUserService adminUserService;
	private JpaTransactionManager transactionManager;
	private FantaUser admin;
	private NewsPaper newsPaper;
	private League league;

	@BeforeAll
	static void initializeSessionFactory() {

		try {
			StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
					.configure("hibernate-test.cfg.xml").build();

			Metadata metadata = new MetadataSources(serviceRegistry).addAnnotatedClass(Contract.class)
					.addAnnotatedClass(FantaTeam.class).addAnnotatedClass(Player.class)
					.addAnnotatedClass(Player.Goalkeeper.class).addAnnotatedClass(Player.Defender.class)
					.addAnnotatedClass(Player.Midfielder.class).addAnnotatedClass(Player.Forward.class)
					.addAnnotatedClass(FantaUser.class).addAnnotatedClass(NewsPaper.class)
					.addAnnotatedClass(League.class).addAnnotatedClass(MatchDaySerieA.class)
					.addAnnotatedClass(Match.class).getMetadataBuilder().build();

			sessionFactory = metadata.getSessionFactoryBuilder().build();

		} catch (Throwable ex) {
			throw new ExceptionInInitializerError(ex);
		}

	}

	@BeforeEach
	void setup() {

		sessionFactory.getSchemaManager().truncateMappedObjects();
		transactionManager = new JpaTransactionManager(sessionFactory);
		adminUserService = new AdminUserService(transactionManager);
		sessionFactory.createEntityManager();

		sessionFactory.inTransaction(t -> {
			admin = new FantaUser("mail", "pswd");
			newsPaper = new NewsPaper("Gazzetta");
			league = new League(admin, "lega", newsPaper, "0000");
			t.persist(admin);
			t.persist(newsPaper);
			t.persist(league);
		});

	}

	@AfterAll
	static void tearDown() {
		sessionFactory.close();
	}

	@Test
	void createLeague() {

		adminUserService.createLeague("lega", admin, newsPaper, "1234");

		transactionManager.inTransaction(context -> {
			Optional<League> result = context.getLeagueRepository().getLeagueByCode("1234");

			assertThat(result.isPresent());
			League league = result.get();

			assertThat(league.getName()).isEqualTo("lega");
			assertThat(league.getAdmin()).isEqualTo(admin);
			assertThat(league.getNewsPaper()).isEqualTo(newsPaper);
			assertThat(league.getLeagueCode()).isEqualTo("1234");
		});
	}

// TODO in teoria è già testato
//	@Test
//	void createLeague_LeagueAlreadyExists() {
//
//		adminUserService.createLeague("lega", admin, newsPaper, "1234");
//		assertThatThrownBy(() -> adminUserService.createLeague("lega", admin, newsPaper, "1234"))
//				.isInstanceOf(IllegalArgumentException.class)
//				.hasMessage("A league with the same league code already exists");
//
//		transactionManager.inTransaction(context -> {
//			Optional<League> result = context.getLeagueRepository().getLeagueByCode("1234");
//
//			assertThat(result.isEmpty());
//		});
//	}

	@Test
	void generateCalendar() {

		FantaTeam team1 = new FantaTeam("team1", league, 0, admin, new HashSet<Contract>());
		FantaUser user = new FantaUser("mail2", "pswd2");
		FantaTeam team2 = new FantaTeam("team2", league, 0, user, new HashSet<Contract>());
		List<MatchDaySerieA> matchDays = new ArrayList<MatchDaySerieA>();
		for (int i = 0; i < 38; i++) {
			matchDays.add(new MatchDaySerieA("Match " + String.valueOf(i), LocalDate.of(2025, 9, 7).plusWeeks(i)));
		}

		sessionFactory.inTransaction(t -> {
			t.persist(team1);
			t.persist(user);
			t.persist(team2);
			for (MatchDaySerieA matchDaySerieA : matchDays) {
				t.persist(matchDaySerieA);
			}
		});

		adminUserService.generateCalendar(league);

		transactionManager.inTransaction(context -> {
			MatchRepository matchRepository = context.getMatchRepository();
			for (MatchDaySerieA matchDaySerieA : matchDays) {
				Match matchByMatchDay = matchRepository.getMatchByMatchDay(matchDaySerieA, league, team1);
				
				assertThat(matchByMatchDay.getMatchDaySerieA()).isEqualTo(matchDaySerieA);
				assertThat(matchByMatchDay.getTeam1().equals(team1) || matchByMatchDay.getTeam2().equals(team1)).isTrue();
			}
		});
	}
	
	@Test
	void testCalculateGrades_SavesResultsAndUpdatesPoints() {

		NewsPaper newspaper = new NewsPaper("Gazzetta");

		LocalDate matchDate = LocalDate.of(2025, 9, 21); // Sunday
		MatchDaySerieA prevDay = new MatchDaySerieA("Day0", matchDate.minusWeeks(1));
		MatchDaySerieA dayToCalc = new MatchDaySerieA("Day1", matchDate);

		// Teams
		FantaTeam team1 = new FantaTeam("Team1", league, 0, admin, Set.of());
		FantaTeam team2 = new FantaTeam("Team2", league, 0, admin, Set.of());

		// Match
		Match match = new Match(dayToCalc, team1, team2);

		// Players
		Player.Goalkeeper gk1 = new Player.Goalkeeper("G1", "Alpha", Player.Club.ATALANTA);
		Player.Goalkeeper gk2 = new Player.Goalkeeper("G2", "Beta", Player.Club.BOLOGNA);

		LineUp lineup1 = new _433LineUp._443LineUpBuilder(match, team1).withGoalkeeper(gk1).build();
		LineUp lineup2 = new _433LineUp._443LineUpBuilder(match, team2).withGoalkeeper(gk2).build();

		// Grades
		Grade grade1 = new Grade(gk1, dayToCalc, 70.0, newspaper);
		Grade grade2 = new Grade(gk2, dayToCalc, 60.0, newspaper);


		// Assert: Result persisted
		adminUserService.calculateGrades(admin, league);
		
		// Assert: team points updated
		assertThat(team1.getPoints()).isEqualTo(3);
		assertThat(team2.getPoints()).isEqualTo(0);
	}

}
