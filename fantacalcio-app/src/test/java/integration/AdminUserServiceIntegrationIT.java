package integration;

import static java.util.stream.Collectors.toSet;
import static java.util.stream.IntStream.range;
import static org.assertj.core.api.Assertions.assertThat;
import java.util.List;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

import business.AdminUserService;
import dal.transaction.jpa.JpaTransactionManager;
import domain.Player.Club;
import domain.Player.Defender;
import domain.Player.Forward;
import domain.Player.Goalkeeper;
import domain.Player.Midfielder;
import domain.scheme.Scheme433;

@DisplayName("An AdminUserService")
class AdminUserServiceIntegrationIT {

	private static SessionFactory sessionFactory;
	private AdminUserService adminUserService;
	private JpaTransactionManager transactionManager;

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
					.addAnnotatedClass(Fielding.StarterFielding.class)
					.addAnnotatedClass(Fielding.SubstituteFielding.class)
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
		adminUserService = new AdminUserService(transactionManager);
	}

	@AfterAll
	static void tearDown() {
		sessionFactory.close();
	}

	@Test
	@DisplayName("can generate a League's calendar")
	void generateCalendar() {

		// GIVEN a League containing some Teams is persisted to the database
		int numberOfTeams = 8;
		FantaUser admin = new FantaUser("mail", "pswd");
		FantaUser user = new FantaUser("user2", "pswd2");
		League league = new League(admin, "lega", "0000");
		List<FantaTeam> teams = range(0, numberOfTeams)
				.mapToObj(i -> new FantaTeam("Team" + (i + 1), league, 0, user, null)).toList();
		List<MatchDay> matchDays = range(0, MatchDay.MATCH_DAYS_IN_LEAGUE)
				.mapToObj(i -> new MatchDay("MatchDay", i, MatchDay.Status.FUTURE, league)).toList();

		transactionManager.inTransaction(context -> {
			context.getFantaUserRepository().saveFantaUser(admin);
			context.getFantaUserRepository().saveFantaUser(user);
			context.getLeagueRepository().saveLeague(league);
			teams.forEach(context.getTeamRepository()::saveTeam);
			matchDays.forEach(context.getMatchDayRepository()::saveMatchDay);
		});

		// WHEN the SUT is used to generate the League's calendar
		adminUserService.generateCalendar(league);

		// THEN the right Matches are persisted to the database
		Set<Match> persistedMatches = transactionManager.fromTransaction(
				context -> matchDays.stream().map(matchDay -> context.getMatchRepository().getAllMatchesIn(matchDay))
						.flatMap(Collection::stream).collect(Collectors.toSet()));
		int daysForRR = numberOfTeams - 1;

		// 1. every FantaTeam plays on every MatchDay
		persistedMatches.stream()
				.collect(Collectors.groupingBy(Match::getMatchDay,
						Collectors.flatMapping(match -> Stream.<FantaTeam>of(match.getTeam1(), match.getTeam2()),
								Collectors.toSet())))
				.values().stream()
				.forEach(teamsInDay -> assertThat(teamsInDay).containsExactlyInAnyOrderElementsOf(teams));

		// 2. matches in the 'outward' round are round-robin couples
		Set<Set<FantaTeam>> roundRobin = range(0, teams.size()).boxed()
				.flatMap(i -> range(i + 1, teams.size()).mapToObj(j -> Set.of(teams.get(i), teams.get(j))))
				.collect(Collectors.toSet());

		Set<List<FantaTeam>> outwardPairings = persistedMatches.stream()
				.filter(match -> range(0, daysForRR).boxed().toList().contains(match.getMatchDay().getNumber()))
				.map(match -> List.of(match.getTeam1(), match.getTeam2())).collect(toSet());

		assertThat(outwardPairings.stream().map(Set::copyOf).collect(toSet()))
				.containsExactlyInAnyOrderElementsOf(roundRobin);

		// 3. pairings in the 'return' round are reverses of those in 'outward'
		Set<List<FantaTeam>> returnPairings = persistedMatches.stream().filter(
				match -> range(daysForRR, 2 * daysForRR).boxed().toList().contains(match.getMatchDay().getNumber()))
				.map(match -> List.of(match.getTeam1(), match.getTeam2())).collect(toSet());

		assertThat(returnPairings).containsExactlyInAnyOrderElementsOf(
				outwardPairings.stream().map(pair -> List.of(pair.get(1), pair.get(0))).collect(toSet()));

		// 4. 'second outward' pairings are taken form 'first outward'
		Set<List<FantaTeam>> secondOutwardPairings = persistedMatches.stream()
				.filter(match -> range(2 * daysForRR, MatchDay.MATCH_DAYS_IN_LEAGUE).boxed().toList()
						.contains(match.getMatchDay().getNumber()))
				.map(match -> List.of(match.getTeam1(), match.getTeam2())).collect(toSet());

		assertThat(secondOutwardPairings).isSubsetOf(outwardPairings);
	}

	@Test
	@DisplayName("can calculate Results for a Match")
	void calculateResults() {
		
		// TODO fa salvare un solo Result, un po' ingenuo

		// GIVEN the database contains LineUps for a Match and Grades for those Players
		FantaUser admin = new FantaUser("mail", "pswd");
		FantaUser user = new FantaUser("mail2", "pswd2");
		League league = new League(admin, "lega", "0000");
		FantaTeam team1 = new FantaTeam("teamA", league, 0, admin, new HashSet<Contract>());
		FantaTeam team2 = new FantaTeam("teamB", league, 0, user, new HashSet<Contract>());
		MatchDay prevDay = new MatchDay("Day1",1, MatchDay.Status.PAST,league);
		MatchDay dayToCalc = new MatchDay("Day2", 2, MatchDay.Status.PAST, league);
		Match prevMatch = new Match(prevDay, team1, team2);
		Match match = new Match(dayToCalc, team1, team2);

		// only players that will be given nonzero Grades
		Goalkeeper gkOnly1 = new Goalkeeper("portiere1", "titolare", Player.Club.ATALANTA);		
		Goalkeeper gkOnly2 = new Goalkeeper("portiere2", "titolare", Player.Club.ATALANTA);

		// placeholder players, just for instantiating LineUp
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

		List<Player> placeholderPlayers = List.of(
				d1, d2, d3, d4, 
				m1, m2, m3, 
				f1, f2, f3, 
				sgk1, sgk2, sgk3, 
				sd1, sd2, sd3,
				sm1, sm2, sm3,
				sf1, sf2, sf3);		

		Grade grade1 = new Grade(gkOnly1, dayToCalc, 70.0);
		Grade grade2 = new Grade(gkOnly2, dayToCalc, 60.0);
		
		LineUp lineup1 = LineUp.build()
				.forTeam(team1)
				.inMatch(match)
				.withStarterLineUp(Scheme433.starterLineUp()
						.withGoalkeeper(gkOnly1)
						.withDefenders(d1, d2, d3, d4)
						.withMidfielders(m1, m2, m3)
						.withForwards(f1, f2, f3))
				.withSubstituteGoalkeepers(sgk1, sgk2, sgk3)
				.withSubstituteDefenders(sd1, sd2, sd3)
				.withSubstituteMidfielders(sm1, sm2, sm3)
				.withSubstituteForwards(sf1, sf2, sf3);
		
		LineUp lineup2 = LineUp.build()
				.forTeam(team2)
				.inMatch(match)
				.withStarterLineUp(Scheme433.starterLineUp()
						.withGoalkeeper(gkOnly2)
						.withDefenders(d1, d2, d3, d4)
						.withMidfielders(m1, m2, m3)
						.withForwards(f1, f2, f3))
				.withSubstituteGoalkeepers(sgk1, sgk2, sgk3)
				.withSubstituteDefenders(sd1, sd2, sd3)
				.withSubstituteMidfielders(sm1, sm2, sm3)
				.withSubstituteForwards(sf1, sf2, sf3);
		
		transactionManager.inTransaction(context -> {
			context.getFantaUserRepository().saveFantaUser(admin);
			context.getFantaUserRepository().saveFantaUser(user);
			context.getLeagueRepository().saveLeague(league);
			context.getTeamRepository().saveTeam(team1);
			context.getTeamRepository().saveTeam(team2);
			context.getMatchDayRepository().saveMatchDay(prevDay);
			context.getMatchDayRepository().saveMatchDay(dayToCalc);
			context.getMatchRepository().saveMatch(prevMatch);
			context.getMatchRepository().saveMatch(match);
			
			// Contracts and Grades are persisted for special players
			context.getPlayerRepository().addPlayer(gkOnly1);
			context.getContractRepository().saveContract(new Contract(team1, gkOnly1));			
			context.getPlayerRepository().addPlayer(gkOnly2);
			context.getContractRepository().saveContract(new Contract(team2, gkOnly2));
			context.getGradeRepository().saveGrade(grade1);
			context.getGradeRepository().saveGrade(grade2);
			
			// Contracts and Grades are persisted for placeholder Players
			placeholderPlayers.forEach(player -> {
				context.getPlayerRepository().addPlayer(player);
				context.getContractRepository().saveContract(new Contract(team1, player));
				context.getContractRepository().saveContract(new Contract(team2, player));
				context.getGradeRepository().saveGrade(new Grade(player, dayToCalc, 0));
			});
			
			// LineUps are persisted
			context.getLineUpRepository().saveLineUp(lineup1);
			context.getLineUpRepository().saveLineUp(lineup2);
		});

		// WHEN the SUT is used to calculate the League's results
		adminUserService.calculateResults(league);

		// THEN Teams' points have been updated in the database
		List<FantaTeam> teamsInDb = transactionManager
				.fromTransaction(context -> context.getTeamRepository().getAllTeams(league).stream()
						.filter(team -> List.of("teamA", "teamB").contains(team.getName())))
						.sorted(Comparator.comparing(FantaTeam::getName))
						.collect(Collectors.toList());
		assertThat(teamsInDb.stream().sorted(Comparator.comparing(FantaTeam::getPoints)).map(FantaTeam::getPoints)
				.toList()).containsExactly(0, 3);

		// AND the correct Result is persisted to the database
		Optional<Result> persistedResult = transactionManager
				.fromTransaction(context -> context.getResultsRepository().getResultFor(match));
		assertThat(persistedResult.get())
				.isEqualTo(new Result(70, 60, 1, 0, new Match(dayToCalc, teamsInDb.get(0), teamsInDb.get(1))));
	}

	@Test
	@DisplayName("can assign Players to Teams")
	void setPlayersToTeam() {

		// GIVEN a Team and two Players are persisted to the database
		FantaUser admin = new FantaUser("mail", "pswd");
		League league = new League(admin, "lega", "1234");
		FantaTeam team = new FantaTeam("", league, 0, admin, Set.of());
		Player.Forward player1 = new Player.Forward("Lionel", "Messi", Club.CREMONESE);
		Player.Goalkeeper player2 = new Player.Goalkeeper("Gigi", "Buffon", Club.JUVENTUS);
		
		transactionManager.inTransaction(context -> {
			context.getFantaUserRepository().saveFantaUser(admin);
			context.getLeagueRepository().saveLeague(league);
			context.getTeamRepository().saveTeam(team);
			context.getPlayerRepository().addPlayer(player1);
			context.getPlayerRepository().addPlayer(player2);
		});
		
		// WHEN the SUT is used to assign two Players to a Team
		adminUserService.setPlayerToTeam(team, player1);
		adminUserService.setPlayerToTeam(team, player2);
		
		// THEN the right Contracts are persisted to the database
		Optional<Contract> persistedContract1 = transactionManager
				.fromTransaction(context -> context.getContractRepository().getContract(team, player1));
		assertThat(persistedContract1).hasValue(new Contract(team, player1));
		
		Optional<Contract> persistedContract2 = transactionManager
				.fromTransaction(context -> context.getContractRepository().getContract(team, player2));
		assertThat(persistedContract2).hasValue(new Contract(team, player2));
	}

}
