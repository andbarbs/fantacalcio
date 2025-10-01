package businessLogic;

import domainModel.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import businessLogic.repositories.*;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AdminUserServiceTest {

	private TransactionManager transactionManager;
	private TransactionContext context;
	private AdminUserService adminUserService;

	// Repositories
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

	@BeforeEach
	void setUp() {
		transactionManager = mock(TransactionManager.class);
		context = mock(TransactionContext.class);

		// Setup inTransaction
		doAnswer(invocation -> {
			Consumer<TransactionContext> code = invocation.getArgument(0);
			code.accept(context);
			return null;
		}).when(transactionManager).inTransaction(any());

		// Setup fromTransaction
		when(transactionManager.fromTransaction(any())).thenAnswer(invocation -> {
			Function<TransactionContext, Object> code = invocation.getArgument(0);
			return code.apply(context);
		});

		adminUserService = new AdminUserService(transactionManager);

		// Create mocks for all repositories
		matchRepository = mock(MatchRepository.class);
		gradeRepository = mock(GradeRepository.class);
		lineUpRepository = mock(LineUpRepository.class);
		resultRepository = mock(ResultsRepository.class);
		matchDayRepository = mock(MatchDayRepository.class);
		fantaTeamRepository = mock(FantaTeamRepository.class);
		leagueRepository = mock(LeagueRepository.class);
		playerRepository = mock(PlayerRepository.class);
		proposalRepository = mock(ProposalRepository.class);
		contractRepository = mock(ContractRepository.class);
		newspaperRepository = mock(NewsPaperRepository.class);

		// Configure context to return all mocks
		when(context.getMatchRepository()).thenReturn(matchRepository);
		when(context.getGradeRepository()).thenReturn(gradeRepository);
		when(context.getLineUpRepository()).thenReturn(lineUpRepository);
		when(context.getResultsRepository()).thenReturn(resultRepository);
		when(context.getMatchDayRepository()).thenReturn(matchDayRepository);
		when(context.getTeamRepository()).thenReturn(fantaTeamRepository);
		when(context.getLeagueRepository()).thenReturn(leagueRepository);
		when(context.getPlayerRepository()).thenReturn(playerRepository);
		when(context.getProposalRepository()).thenReturn(proposalRepository);
		when(context.getContractRepository()).thenReturn(contractRepository);
		when(context.getNewspaperRepository()).thenReturn(newspaperRepository);
	}

	@Test
	void testCreateLeague() {
		FantaUser admin = new FantaUser("admin@test.com", "pwd");
		NewsPaper np = new NewsPaper("Gazzetta");
		String leagueCode = "L001";

		// League code does not exist yet
		when(leagueRepository.getLeagueByCode(leagueCode)).thenReturn(Optional.empty());

		adminUserService.createLeague("My League", admin, np, leagueCode);

		// Verify that saveLeague was called
		verify(leagueRepository, times(1)).saveLeague(any(League.class));
	}

	@Test
	void testCreateLeague_LeagueCodeExists() {
		FantaUser admin = new FantaUser("admin@test.com", "pwd");
		NewsPaper np = new NewsPaper("Gazzetta");
		String leagueCode = "L001";

		League existingLeague = new League(admin, "Existing League", np, leagueCode);
		when(leagueRepository.getLeagueByCode(leagueCode)).thenReturn(Optional.of(existingLeague));

		assertThatThrownBy(() -> adminUserService.createLeague("New League", admin, np, leagueCode))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("A league with the same league code already exists");
	}
	

	@Test
	void testSetPlayerToTeam_SavesContract_WhenBelowLimits() {
		FantaTeam team = new FantaTeam("Team", null, 0, null, new HashSet<>());
		Player.Goalkeeper gk = new Player.Goalkeeper("Gigi", "Buffon", Player.Club.JUVENTUS);

		adminUserService.setPlayerToTeam(team, gk);

		verify(contractRepository).saveContract(argThat(c -> c.getTeam().equals(team) && c.getPlayer().equals(gk)));
	}

	@Test
	void testSetPlayerToTeam_Throws_WhenTeamHas25Players() {
		FantaTeam team = new FantaTeam("Team", null, 0, null, new HashSet<>());
		Set<Contract> contracts = new HashSet<Contract>();
		for (int i = 0; i < 25; i++) {
			Player p = new Player.Midfielder("Player" + i, "Test", Player.Club.ATALANTA);
			contracts.add(new Contract(team, p));
		}
		team.setContracts(contracts);

		Player newPlayer = new Player.Defender("New", "Player", Player.Club.BOLOGNA);

		assertThatThrownBy(() -> adminUserService.setPlayerToTeam(team, newPlayer))
				.isInstanceOf(UnsupportedOperationException.class).hasMessageContaining("Maximum 25 players");
	}

	@Test
	void testSetPlayerToTeam_DoesNotSave_WhenGoalkeepersLimitReached() {
		FantaTeam team = new FantaTeam("Team", null, 0, null, new HashSet<>());

		Set<Contract> contracts = new HashSet<Contract>();

		// Add 3 goalkeepers to reach the limit
		contracts.add(new Contract(team, new Player.Goalkeeper("G1", "A", Player.Club.ATALANTA)));
		contracts.add(new Contract(team, new Player.Goalkeeper("G2", "B", Player.Club.BOLOGNA)));
		contracts.add(new Contract(team, new Player.Goalkeeper("G3", "C", Player.Club.CAGLIARI)));

		team.setContracts(contracts);

		Player.Goalkeeper newGk = new Player.Goalkeeper("New", "Keeper", Player.Club.ROMA);

		adminUserService.setPlayerToTeam(team, newGk);

		verify(contractRepository, never()).saveContract(any());
	}

	@Test
	void testSetPlayerToTeam_DoesNotSave_WhenDefendersLimitReached() {
		FantaTeam team = new FantaTeam("Team", null, 0, null, new HashSet<>());

		Set<Contract> contracts = new HashSet<Contract>();

		// Add 8 defenders to reach the limit
		contracts.add(new Contract(team, new Player.Defender("G1", "A", Player.Club.ATALANTA)));
		contracts.add(new Contract(team, new Player.Defender("G2", "B", Player.Club.BOLOGNA)));
		contracts.add(new Contract(team, new Player.Defender("G3", "C", Player.Club.CAGLIARI)));
		contracts.add(new Contract(team, new Player.Defender("G4", "A", Player.Club.ATALANTA)));
		contracts.add(new Contract(team, new Player.Defender("G5", "B", Player.Club.BOLOGNA)));
		contracts.add(new Contract(team, new Player.Defender("G6", "C", Player.Club.CAGLIARI)));
		contracts.add(new Contract(team, new Player.Defender("G7", "B", Player.Club.BOLOGNA)));
		contracts.add(new Contract(team, new Player.Defender("G8", "C", Player.Club.CAGLIARI)));

		team.setContracts(contracts);

		Player.Defender newDf = new Player.Defender("New", "Keeper", Player.Club.ROMA);

		adminUserService.setPlayerToTeam(team, newDf);

		verify(contractRepository, never()).saveContract(any());
	}

	@Test
	void testSetPlayerToTeam_DoesNotSave_WhenMidfieldersLimitReached() {
		FantaTeam team = new FantaTeam("Team", null, 0, null, new HashSet<>());

		Set<Contract> contracts = new HashSet<Contract>();

		// Add 8 defenders to reach the limit
		contracts.add(new Contract(team, new Player.Midfielder("G1", "A", Player.Club.ATALANTA)));
		contracts.add(new Contract(team, new Player.Midfielder("G2", "B", Player.Club.BOLOGNA)));
		contracts.add(new Contract(team, new Player.Midfielder("G3", "C", Player.Club.CAGLIARI)));
		contracts.add(new Contract(team, new Player.Midfielder("G4", "A", Player.Club.ATALANTA)));
		contracts.add(new Contract(team, new Player.Midfielder("G5", "B", Player.Club.BOLOGNA)));
		contracts.add(new Contract(team, new Player.Midfielder("G6", "C", Player.Club.CAGLIARI)));
		contracts.add(new Contract(team, new Player.Midfielder("G7", "B", Player.Club.BOLOGNA)));
		contracts.add(new Contract(team, new Player.Midfielder("G8", "C", Player.Club.CAGLIARI)));

		team.setContracts(contracts);

		Player.Midfielder newMf = new Player.Midfielder("New", "Keeper", Player.Club.ROMA);

		adminUserService.setPlayerToTeam(team, newMf);

		verify(contractRepository, never()).saveContract(any());
	}

	@Test
	void testSetPlayerToTeam_DoesNotSave_WhenForwardsLimitReached() {
		FantaTeam team = new FantaTeam("Team", null, 0, null, new HashSet<>());

		Set<Contract> contracts = new HashSet<Contract>();

		// Add 8 defenders to reach the limit
		contracts.add(new Contract(team, new Player.Forward("G1", "A", Player.Club.ATALANTA)));
		contracts.add(new Contract(team, new Player.Forward("G2", "B", Player.Club.BOLOGNA)));
		contracts.add(new Contract(team, new Player.Forward("G3", "C", Player.Club.CAGLIARI)));
		contracts.add(new Contract(team, new Player.Forward("G4", "A", Player.Club.ATALANTA)));
		contracts.add(new Contract(team, new Player.Forward("G5", "B", Player.Club.BOLOGNA)));
		contracts.add(new Contract(team, new Player.Forward("G6", "C", Player.Club.CAGLIARI)));

		team.setContracts(contracts);

		Player.Forward newFw = new Player.Forward("New", "Keeper", Player.Club.ROMA);

		adminUserService.setPlayerToTeam(team, newFw);

		verify(contractRepository, never()).saveContract(any());
	}

	@Test
	void testRemovePlayerFromTeam_WhenContractExists() {
		FantaTeam team = new FantaTeam("Team", null, 0, null, new HashSet<>());
		Player player = new Player.Forward("Cristiano", "Ronaldo", Player.Club.JUVENTUS);
		Contract contract = new Contract(team, player);
		team.setContracts(Set.of(contract));

		when(contractRepository.getContract(team, player)).thenReturn(Optional.of(contract));

		adminUserService.removePlayerFromTeam(team, player);

		verify(contractRepository).deleteContract(contract);
	}

	@Test
	void testRemovePlayerFromTeam_WhenContractDoesNotExist() {
		FantaTeam team = new FantaTeam("Team", null, 0, null, new HashSet<>());
		Player player = new Player.Defender("Giorgio", "Chiellini", Player.Club.JUVENTUS);

		when(contractRepository.getContract(team, player)).thenReturn(Optional.empty());

		adminUserService.removePlayerFromTeam(team, player);

		verify(contractRepository, never()).deleteContract(any());
	}

	@Test
	void testGetAllNewspapers() {
		NewsPaper np1 = new NewsPaper("Gazzetta");
		NewsPaper np2 = new NewsPaper("Corriere");
		when(newspaperRepository.getAllNewspapers()).thenReturn(List.of(np1, np2));

		List<NewsPaper> result = adminUserService.getAllNewspapers();

		assertThat(result).containsExactly(np1, np2);
	}

	@Test
	void testGenerateCalendar_SavesMatches() {
		FantaUser admin = new FantaUser(null, null);
		League league = new League(admin, "Serie A", null, null);

		// Create 4 real teams (even number for round-robin)
		FantaTeam team1 = new FantaTeam("Team1", null, 0, null, new HashSet<>());
		FantaTeam team2 = new FantaTeam("Team2", null, 0, null, new HashSet<>());
		FantaTeam team3 = new FantaTeam("Team3", null, 0, null, new HashSet<>());
		FantaTeam team4 = new FantaTeam("Team4", null, 0, null, new HashSet<>());
		List<FantaTeam> teams = List.of(team1, team2, team3, team4);

		// 38 match days (real objects are okay)
		List<MatchDaySerieA> matchDays = new ArrayList<>();
		for (int i = 0; i < 38; i++)
			matchDays.add(new MatchDaySerieA("match", LocalDate.now()));

		// Mock repositories
		when(fantaTeamRepository.getAllTeams(league)).thenReturn(teams);
		when(matchDayRepository.getAllMatchDays()).thenReturn(matchDays);

		adminUserService.generateCalendar(league);

		verify(matchRepository, atLeastOnce()).saveMatch(any(Match.class));
	}

	@Test
	void testGenerateCalendar_LessThanTwoTeams_Throws() {
		FantaUser admin = new FantaUser(null, null);
		League league = new League(admin, "Serie A", null, null);
		FantaTeam onlyTeam = new FantaTeam("Solo", null, 0, null, new HashSet<>());

		when(fantaTeamRepository.getAllTeams(league)).thenReturn(List.of(onlyTeam));

		assertThatThrownBy(() -> adminUserService.generateCalendar(league)).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("At least 2 teams are required");
	}

	@Test
	void testGenerateCalendar_OddNumberOfTeams_Throws() {
		FantaUser admin = new FantaUser(null, null);
		League league = new League(admin, "Serie A", null, null);
		FantaTeam t1 = new FantaTeam("Team1", null, 0, null, new HashSet<>());
		FantaTeam t2 = new FantaTeam("Team2", null, 0, null, new HashSet<>());
		FantaTeam t3 = new FantaTeam("Team3", null, 0, null, new HashSet<>());

		when(fantaTeamRepository.getAllTeams(league)).thenReturn(List.of(t1, t2, t3));

		assertThatThrownBy(() -> adminUserService.generateCalendar(league)).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Number of teams must be even");
	}

	@Test
	void testGenerateSchedule_EvenNumberOfTeams_Success() throws Exception {
		FantaTeam t1 = new FantaTeam("T1", null, 0, null, new HashSet<>());
		FantaTeam t2 = new FantaTeam("T2", null, 0, null, new HashSet<>());
		FantaTeam t3 = new FantaTeam("T3", null, 0, null, new HashSet<>());
		FantaTeam t4 = new FantaTeam("T4", null, 0, null, new HashSet<>());

		var method = AdminUserService.class.getDeclaredMethod("generateSchedule", List.class);
		method.setAccessible(true);

		@SuppressWarnings("unchecked")
		List<List<FantaTeam[]>> schedule = (List<List<FantaTeam[]>>) method.invoke(adminUserService, List.of(t1, t2, t3, t4));

		int expectedRounds = (4 - 1) * 2; // double round robin
		assertThat(schedule.size()).isEqualTo(expectedRounds);

		for (List<FantaTeam[]> round : schedule) {
			assertThat(round.size()).isEqualTo(2); // n/2 matches per round
		}
	}

	@Test
	void testGenerateSchedule_OddNumberOfTeams_Throws() throws Exception {
		FantaTeam t1 = new FantaTeam("T1", null, 0, null, new HashSet<>());
		FantaTeam t2 = new FantaTeam("T2", null, 0, null, new HashSet<>());
		FantaTeam t3 = new FantaTeam("T3", null, 0, null, new HashSet<>());

		assertThatThrownBy(() -> {
			var method = AdminUserService.class.getDeclaredMethod("generateSchedule", List.class);
			method.setAccessible(true);
			method.invoke(adminUserService, List.of(t1, t2, t3));
		}).hasCauseInstanceOf(IllegalArgumentException.class)
				.satisfies(ex -> assertThat(ex.getCause().getMessage()).contains("Number of teams must be even"));
	}

	@Test
	void testGenerateSchedule_DoubleRoundRobin_MirrorsCorrectly() throws Exception {
		FantaTeam t1 = new FantaTeam("T1", null, 0, null, new HashSet<>());
		FantaTeam t2 = new FantaTeam("T2", null, 0, null, new HashSet<>());
		FantaTeam t3 = new FantaTeam("T3", null, 0, null, new HashSet<>());
		FantaTeam t4 = new FantaTeam("T4", null, 0, null, new HashSet<>());

		var method = AdminUserService.class.getDeclaredMethod("generateSchedule", List.class);
		method.setAccessible(true);
		@SuppressWarnings("unchecked")
		List<List<FantaTeam[]>> schedule = (List<List<FantaTeam[]>>) method.invoke(adminUserService, List.of(t1, t2, t3, t4));

		int firstLegRounds = schedule.size() / 2;

		for (int i = 0; i < firstLegRounds; i++) {
			List<FantaTeam[]> firstLeg = schedule.get(i);
			List<FantaTeam[]> secondLeg = schedule.get(i + firstLegRounds);
			for (int j = 0; j < firstLeg.size(); j++) {
				assertThat(secondLeg.get(j)[0]).isEqualTo(firstLeg.get(j)[1]);
				assertThat(secondLeg.get(j)[1]).isEqualTo(firstLeg.get(j)[0]);
			}
		}
	}

	@Test
	void testCreateMatches_Success() {
		FantaTeam t1 = new FantaTeam("T1", null, 0, null, new HashSet<>());
		FantaTeam t2 = new FantaTeam("T2", null, 0, null, new HashSet<>());

		FantaTeam[] match = new FantaTeam[] { t1, t2 };
		List<MatchDaySerieA> matchDays = List.of(new MatchDaySerieA(null, null));

		List<FantaTeam[]> round = new ArrayList<>();
		round.add(match);
		List<List<FantaTeam[]>> schedule = new ArrayList<>();
		schedule.add(round);

		List<Match> matches = adminUserService.createMatches(schedule, matchDays);

		assertThat(matches).hasSize(1);
		assertThat(matches.get(0).getTeam1()).isEqualTo(t1);
		assertThat(matches.get(0).getTeam2()).isEqualTo(t2);
		assertThat(matches.get(0).getMatchDaySerieA()).isEqualTo(matchDays.get(0));
	}

	@Test
	void testCreateMatches_MismatchThrows() {
		FantaTeam t1 = new FantaTeam("T1", null, 0, null, new HashSet<>());
		FantaTeam t2 = new FantaTeam("T2", null, 0, null, new HashSet<>());

		FantaTeam[] match = new FantaTeam[] { t1, t2 };

		List<FantaTeam[]> round = new ArrayList<>();
		round.add(match);
		List<List<FantaTeam[]>> schedule = new ArrayList<>();
		schedule.add(round);

		List<MatchDaySerieA> matchDays = List.of(); // empty -> mismatch

		assertThatThrownBy(() -> adminUserService.createMatches(schedule, matchDays))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Schedule rounds and matchDays must have the same size");
	}

	@Test
	void testCreateMatches_MoreRoundsThanMatchDays_Throws() {
		FantaTeam team1 = new FantaTeam("Team1", null, 0, null, new HashSet<>());
		FantaTeam team2 = new FantaTeam("Team2", null, 0, null, new HashSet<>());
		FantaTeam team3 = new FantaTeam("Team3", null, 0, null, new HashSet<>());
		FantaTeam team4 = new FantaTeam("Team4", null, 0, null, new HashSet<>());

		FantaTeam[] teamsArray1 = new FantaTeam[] { team1, team2 };
		FantaTeam[] teamsArray2 = new FantaTeam[] { team3, team4 };

		List<FantaTeam[]> round1 = new ArrayList<>();
		round1.add(teamsArray1);
		List<FantaTeam[]> round2 = new ArrayList<>();
		round1.add(teamsArray2);
		List<List<FantaTeam[]>> schedule = new ArrayList<>();
		schedule.add(round1);
		schedule.add(round2);

		List<MatchDaySerieA> matchDays = List.of(mock(MatchDaySerieA.class)); // only 1 match day

		assertThatThrownBy(() -> adminUserService.createMatches(schedule, matchDays))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Schedule rounds and matchDays must have the same size");
	}

	@Test
	void testCreateMatches_EmptySchedule_ReturnsEmptyList() {
		List<List<FantaTeam[]>> schedule = List.of();
		List<MatchDaySerieA> matchDays = List.of();

		List<Match> matches = adminUserService.createMatches(schedule, matchDays);

		assertThat(matches).isEmpty();
	}

	@Test
	void testCalculateGrades_UserNotAdmin_Throws() {
		// The user who is NOT the league admin
		FantaUser user = new FantaUser("user", "pswd");

		// League with a different admin
		FantaUser admin = new FantaUser("admin", "pswd");
		NewsPaper newspaper = new NewsPaper("Gazzetta");
		League league = new League(admin, "league", newspaper, "12345");

		// Create a previous match day so that the "season started" check passes
		MatchDaySerieA previousMatchDay = new MatchDaySerieA(null, null);
		when(matchDayRepository.getPreviousMatchDay(any())).thenReturn(Optional.of(previousMatchDay));

		// Set up a match day to calculate with at least one match
		FantaTeam team1 = new FantaTeam("Team1", league, 0, user, Set.of());
		FantaTeam team2 = new FantaTeam("Team2", league, 0, admin, Set.of());
		Match match = new Match(previousMatchDay, team1, team2);
		List<Match> matches = List.of(match);
		when(matchRepository.getAllMatchesByMatchDay(any(), eq(league))).thenReturn(matches);

		// Set up grades, lineups, and results so the calculation can proceed
		Grade grade1 = new Grade(new Player.Goalkeeper(null, null, null), previousMatchDay, 6.0, newspaper);
		Grade grade2 = new Grade(new Player.Forward(null, null, null), previousMatchDay, 7.0, newspaper);
		when(gradeRepository.getAllMatchGrades(match, newspaper)).thenReturn(List.of(grade1, grade2));

		LineUp lineup1 = new _433LineUp._443LineUpBuilder(match, team1).build();
		LineUp lineup2 = new _433LineUp._443LineUpBuilder(match, team2).build();
		when(lineUpRepository.getLineUpByMatchAndTeam(match, team1)).thenReturn(Optional.of(lineup1));
		when(lineUpRepository.getLineUpByMatchAndTeam(match, team2)).thenReturn(Optional.of(lineup2));

		// Call the method; it should throw because `user` is not admin
		assertThatThrownBy(() -> adminUserService.calculateGrades(user, league)).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("You are not the admin of the league");

		// Ensure that no results were saved
		verify(context.getResultsRepository(), never()).saveResult(any());
	}

	@Test
	void testCalculateGrades_SeasonNotStarted_Throws() {
		FantaUser admin = new FantaUser(null, null);
		NewsPaper newspaper = new NewsPaper(null);
		League league = new League(admin, "Serie A", newspaper, null);

		when(matchDayRepository.getPreviousMatchDay(any())).thenReturn(Optional.empty());

		assertThatThrownBy(() -> adminUserService.calculateGrades(admin, league)).isInstanceOf(RuntimeException.class)
				.hasMessageContaining("The season hasn't started yet");
	}

	@Test
	void testCalculateGrades_NoResultsToCalculate_Throws() {

		LocalDate previousSaturday = LocalDate.of(2025, 9, 6);
		LocalDate saturday = LocalDate.of(2025, 9, 13);

		League league = mock(League.class);
		FantaUser admin = mock(FantaUser.class);
		when(league.getAdmin()).thenReturn(admin);

		when(matchDayRepository.getPreviousMatchDay(saturday))
				.thenReturn(Optional.of(new MatchDaySerieA("", previousSaturday)));

		// Override today() to return the Saturday we want to test
		AdminUserService serviceWithSaturday = new AdminUserService(transactionManager) {
			@Override
			protected LocalDate today() {
				return saturday;
			}
		};

		when(serviceWithSaturday.getNextMatchDayToCalculate(saturday, context, league, admin))
				.thenReturn(Optional.empty());

		assertThatThrownBy(() -> serviceWithSaturday.calculateGrades(admin, league))
				.isInstanceOf(RuntimeException.class).hasMessageContaining("There are no results to calculate");
	}

	@Test
	void testIsLegalToCalculateResults_Saturday() throws Exception {
		LocalDate previousSaturday = LocalDate.of(2025, 9, 6);
		LocalDate saturday = LocalDate.of(2025, 9, 13);

		League league = mock(League.class);
		FantaUser admin = mock(FantaUser.class);
		when(league.getAdmin()).thenReturn(admin);

		when(matchDayRepository.getPreviousMatchDay(saturday))
				.thenReturn(Optional.of(new MatchDaySerieA("", previousSaturday)));

		// Override today() to return the Saturday we want to test
		AdminUserService serviceWithSaturday = new AdminUserService(transactionManager) {
			@Override
			protected LocalDate today() {
				return saturday;
			}
		};

		when(serviceWithSaturday.getNextMatchDayToCalculate(saturday, context, league, admin))
				.thenReturn(Optional.of(new MatchDaySerieA(null, saturday)));

		assertThatThrownBy(() -> serviceWithSaturday.calculateGrades(admin, league))
				.isInstanceOf(RuntimeException.class).hasMessageContaining("The matches are not finished yet");
	}

	@Test
	void testCalculateGrades_IllegalOnSunday() {
		LocalDate previousSaturday = LocalDate.of(2025, 9, 6);
		LocalDate sunday = LocalDate.of(2025, 9, 14);

		League league = mock(League.class);
		FantaUser admin = mock(FantaUser.class);
		when(league.getAdmin()).thenReturn(admin);

		when(matchDayRepository.getPreviousMatchDay(sunday))
				.thenReturn(Optional.of(new MatchDaySerieA("", previousSaturday)));

		AdminUserService serviceWithSunday = new AdminUserService(transactionManager) {
			@Override
			protected LocalDate today() {
				return sunday;
			}
		};

		when(serviceWithSunday.getNextMatchDayToCalculate(sunday, context, league, admin))
				.thenReturn(Optional.of(new MatchDaySerieA(null, sunday)));

		assertThatThrownBy(() -> serviceWithSunday.calculateGrades(admin, league)).isInstanceOf(RuntimeException.class)
				.hasMessageContaining("The matches are not finished yet");
	}

	@Test
	void testCalculateGrades_IllegalOnWeekday() {
		LocalDate sunday = LocalDate.of(2025, 9, 14);
		LocalDate monday = LocalDate.of(2025, 9, 15);

		League league = mock(League.class);
		FantaUser admin = mock(FantaUser.class);

		when(league.getAdmin()).thenReturn(admin);
		when(matchDayRepository.getPreviousMatchDay(monday)).thenReturn(Optional.of(new MatchDaySerieA("", sunday)));

		AdminUserService serviceWithMonday = new AdminUserService(transactionManager) {
			@Override
			protected LocalDate today() {
				return monday;
			}
		};

		when(serviceWithMonday.getNextMatchDayToCalculate(monday, context, league, admin))
				.thenReturn(Optional.of(new MatchDaySerieA(null, monday)));

		assertThatThrownBy(() -> serviceWithMonday.calculateGrades(admin, league)).isInstanceOf(RuntimeException.class)
				.hasMessageContaining("The matches are not finished yet");
	}

	@Test
	void testCalculateGrades_SavesResultsAndUpdatesPoints() {

		FantaUser admin = new FantaUser("admin@example.com", "pwd");
		NewsPaper newspaper = new NewsPaper("Gazzetta");
		League league = new League(admin, "Serie A", newspaper, "1234");

		LocalDate matchDate = LocalDate.of(2025, 9, 21); // Sunday
		MatchDaySerieA prevDay = new MatchDaySerieA("Day0", matchDate.minusWeeks(1));
		MatchDaySerieA dayToCalc = new MatchDaySerieA("Day1", matchDate);

		when(matchDayRepository.getPreviousMatchDay(any())).thenReturn(Optional.of(prevDay));

		AdminUserService serviceWithFixedDate = new AdminUserService(transactionManager) {
			@Override
			protected LocalDate today() {
				return matchDate.plusDays(5);
			}

			@Override
			protected Optional<MatchDaySerieA> getNextMatchDayToCalculate(LocalDate d, TransactionContext c, League l,
					FantaUser u) {
				return Optional.of(dayToCalc);
			}
		};

		// Teams
		FantaTeam team1 = new FantaTeam("Team1", league, 0, admin, Set.of());
		FantaTeam team2 = new FantaTeam("Team2", league, 0, admin, Set.of());

		// Match
		Match match = new Match(dayToCalc, team1, team2);
		when(matchRepository.getAllMatchesByMatchDay(dayToCalc, league))
				.thenReturn(List.of(new Match(dayToCalc, team1, team2)));

		// Players
		Player.Goalkeeper gk1 = new Player.Goalkeeper("G1", "Alpha", Player.Club.ATALANTA);
		Player.Goalkeeper gk2 = new Player.Goalkeeper("G2", "Beta", Player.Club.BOLOGNA);

		LineUp lineup1 = new _433LineUp._443LineUpBuilder(match, team1).withGoalkeeper(gk1).build();
		LineUp lineup2 = new _433LineUp._443LineUpBuilder(match, team2).withGoalkeeper(gk2).build();

		when(lineUpRepository.getLineUpByMatchAndTeam(match, team1)).thenReturn(Optional.of(lineup1));
		when(lineUpRepository.getLineUpByMatchAndTeam(match, team2)).thenReturn(Optional.of(lineup2));

		// Grades
		Grade grade1 = new Grade(gk1, dayToCalc, 70.0, newspaper);
		Grade grade2 = new Grade(gk2, dayToCalc, 60.0, newspaper);
		when(gradeRepository.getAllMatchGrades(match, newspaper)).thenReturn(List.of(grade1, grade2));

		// Act
		serviceWithFixedDate.calculateGrades(admin, league);

		// Assert: Result persisted
		verify(resultRepository).saveResult(any());

		// Assert: team points updated
		assertThat(team1.getPoints()).isEqualTo(3);
		assertThat(team2.getPoints()).isEqualTo(0);
	}

}
