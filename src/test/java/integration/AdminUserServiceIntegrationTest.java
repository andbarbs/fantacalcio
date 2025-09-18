package integration;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.List;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
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
import businessLogic.repositories.ContractRepository;
import businessLogic.repositories.FantaTeamRepository;
import businessLogic.repositories.GradeRepository;
import businessLogic.repositories.LeagueRepository;
import businessLogic.repositories.LineUpRepository;
import businessLogic.repositories.MatchDayRepository;
import businessLogic.repositories.MatchRepository;
import businessLogic.repositories.NewsPaperRepository;
import businessLogic.repositories.PlayerRepository;
import businessLogic.repositories.ProposalRepository;
import businessLogic.repositories.ResultsRepository;
import domainModel.Contract;
import domainModel.FantaTeam;
import domainModel.FantaUser;
import domainModel.Fielding;
import domainModel.Grade;
import domainModel.League;
import domainModel.LineUp;
import domainModel.Match;
import domainModel.MatchDaySerieA;
import domainModel.NewsPaper;
import domainModel.Player;
import domainModel.Result;
import domainModel._433LineUp;
import domainModel.Player.Club;
import domainModel.Player.Defender;
import domainModel.Player.Forward;
import domainModel.Player.Goalkeeper;
import domainModel.Player.Midfielder;
import jakarta.persistence.EntityManager;
import jpaRepositories.JpaContractRepository;
import jpaRepositories.JpaFantaTeamRepository;
import jpaRepositories.JpaGradeRepository;
import jpaRepositories.JpaLeagueRepository;
import jpaRepositories.JpaLineUpRepository;
import jpaRepositories.JpaMatchDayRepository;
import jpaRepositories.JpaMatchRepository;
import jpaRepositories.JpaNewsPaperRepository;
import jpaRepositories.JpaPlayerRepository;
import jpaRepositories.JpaProposalRepository;
import jpaRepositories.JpaResultsRepository;

class AdminUserServiceIntegrationTest {

	private static SessionFactory sessionFactory;
	private AdminUserService adminUserService;
	private JpaTransactionManager transactionManager;
	private FantaUser admin;
	private NewsPaper newsPaper;
	private League league;
	private FantaUser user;
	private FantaTeam team1;
	private FantaTeam team2;
	private EntityManager entityManager;

	private MatchRepository matchRepository;
	private GradeRepository gradeRepository;
	private LineUpRepository lineUpRepository;
	private ResultsRepository resultRepository;
	private MatchDayRepository matchDayRepository;
	private FantaTeamRepository fantaTeamRepository;
	private LeagueRepository leagueRepository;
	private PlayerRepository playerRepository;
	private ProposalRepository proposalRepository;
	private ContractRepository contractRepository;
	private NewsPaperRepository newspaperRepository;

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
					.addAnnotatedClass(Match.class).addAnnotatedClass(_433LineUp.class)
					.addAnnotatedClass(Fielding.class).addAnnotatedClass(Result.class).addAnnotatedClass(Grade.class)
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
		adminUserService = new AdminUserService(transactionManager);
		entityManager = sessionFactory.createEntityManager();

		sessionFactory.inTransaction(t -> {
			admin = new FantaUser("mail", "pswd");
			user = new FantaUser("mail2", "pswd2");
			newsPaper = new NewsPaper("Gazzetta");
			league = new League(admin, "lega", newsPaper, "0000");
			team1 = new FantaTeam("team1", league, 0, admin, new HashSet<Contract>());
			team2 = new FantaTeam("team2", league, 0, user, new HashSet<Contract>());
			t.persist(admin);
			t.persist(user);
			t.persist(newsPaper);
			t.persist(league);
			t.persist(team1);
			t.persist(team2);
		});

		matchRepository = new JpaMatchRepository(entityManager);
		gradeRepository = new JpaGradeRepository(entityManager);
		lineUpRepository = new JpaLineUpRepository(entityManager);
		resultRepository = new JpaResultsRepository(entityManager);
		matchDayRepository = new JpaMatchDayRepository(entityManager);
		fantaTeamRepository = new JpaFantaTeamRepository(entityManager);
		leagueRepository = new JpaLeagueRepository(entityManager);
		playerRepository = new JpaPlayerRepository(entityManager);
		proposalRepository = new JpaProposalRepository(entityManager);
		contractRepository = new JpaContractRepository(entityManager);
		newspaperRepository = new JpaNewsPaperRepository(entityManager);

	}

	@AfterAll
	static void tearDown() {
		sessionFactory.close();
	}

	@Test
	void createLeague() {

		adminUserService.createLeague("lega", admin, newsPaper, "1234");

		Optional<League> result = leagueRepository.getLeagueByCode("1234");

		assertThat(result.isPresent());
		League league = result.get();

		assertThat(league.getName()).isEqualTo("lega");
		assertThat(league.getAdmin()).isEqualTo(admin);
		assertThat(league.getNewsPaper()).isEqualTo(newsPaper);
		assertThat(league.getLeagueCode()).isEqualTo("1234");
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

		List<MatchDaySerieA> matchDays = new ArrayList<MatchDaySerieA>();
		for (int i = 0; i < 38; i++) {
			matchDays.add(new MatchDaySerieA("Match " + String.valueOf(i), LocalDate.of(2025, 9, 7).plusWeeks(i)));
		}

		sessionFactory.inTransaction(t -> {
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
				assertThat(matchByMatchDay.getTeam1().equals(team1) || matchByMatchDay.getTeam2().equals(team1))
						.isTrue();
			}
		});
	}

	@Test
	void testCalculateGrades_SavesResultsAndUpdatesPoints() {

		LocalDate matchDate = LocalDate.of(2025, 9, 14);
		MatchDaySerieA prevDay = new MatchDaySerieA("Day0", matchDate.minusWeeks(1));
		MatchDaySerieA dayToCalc = new MatchDaySerieA("Day1", matchDate);

		// Match
		Match prevMatch = new Match(prevDay, team1, team2);
		Match match = new Match(dayToCalc, team1, team2);

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

		LineUp lineup1 = new _433LineUp._443LineUpBuilder(match, team1).withGoalkeeper(gk1)
				.withDefenders(d1, d2, d3, d4).withMidfielders(m1, m2, m3).withForwards(f1, f2, f3)
				.withSubstituteGoalkeepers(List.of(gk2)).withSubstituteDefenders(List.of(d5))
				.withSubstituteMidfielders(List.of(m4)).withSubstituteForwards(List.of(f4)).build();
		LineUp lineup2 = new _433LineUp._443LineUpBuilder(match, team2).withGoalkeeper(gk1)
				.withDefenders(d1, d2, d3, d4).withMidfielders(m1, m2, m3).withForwards(f1, f2, f3)
				.withSubstituteGoalkeepers(List.of(gk2)).withSubstituteDefenders(List.of(d5))
				.withSubstituteMidfielders(List.of(m4)).withSubstituteForwards(List.of(f4)).build();

		// Grades
		Grade grade1 = new Grade(gk1, dayToCalc, 70.0, newsPaper);
		Grade grade2 = new Grade(gk2, dayToCalc, 60.0, newsPaper);

		entityManager.getTransaction().begin();

		entityManager.persist(prevDay);
		entityManager.persist(dayToCalc);
		entityManager.persist(prevMatch);
		entityManager.persist(match);
		players.forEach(entityManager::persist);
		entityManager.persist(lineup1);
		entityManager.persist(lineup2);
		entityManager.persist(grade1);
		entityManager.persist(grade2);

		entityManager.getTransaction().commit();

		adminUserService.calculateGrades(admin, league);
		
		assertThat(gradeRepository.getAllMatchGrades(match, newsPaper).size()).isEqualTo(2);

		Optional<Result> optResult = resultRepository.getResult(match);
		assertThat(optResult).isPresent();
		Result result = optResult.get();
		assertThat(result.getTeam1Goals()).isEqualTo(1);
		assertThat(result.getTeam2Goals()).isEqualTo(0);

		assertThat(team1.getPoints()).isEqualTo(3);
		assertThat(team2.getPoints()).isEqualTo(0);
	}

}
