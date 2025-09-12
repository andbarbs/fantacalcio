package businessLogic;

import domainModel.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import businessLogic.repositories.*;

import java.time.LocalDate;
import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class AdminUserServiceTest {

	private TransactionManager transactionManager;
	private TransactionContext context;
	private AdminUserService service;

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
			@SuppressWarnings("unchecked")
			java.util.function.Consumer<TransactionContext> code = (java.util.function.Consumer<TransactionContext>) invocation
					.getArgument(0);
			code.accept(context);
			return null;
		}).when(transactionManager).inTransaction(any());

		// Setup fromTransaction
		when(transactionManager.fromTransaction(any())).thenAnswer(invocation -> {
			@SuppressWarnings("unchecked")
			java.util.function.Function<TransactionContext, Object> code = (java.util.function.Function<TransactionContext, Object>) invocation
					.getArgument(0);
			return code.apply(context);
		});

		service = new AdminUserService(transactionManager);

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
	void testSetPlayerToTeam() {
		FantaTeam team = mock(FantaTeam.class);
		Player player = mock(Player.class);

		service.setPlayerToTeam(team, player);

		verify(contractRepository).saveContract(argThat(c -> c.getTeam().equals(team) && c.getPlayer().equals(player)));
	}

	@Test
	void testGetAllNewspapers() {
		NewsPaper np1 = mock(NewsPaper.class);
		NewsPaper np2 = mock(NewsPaper.class);
		when(newspaperRepository.getAllNewspapers()).thenReturn(List.of(np1, np2));

		List<NewsPaper> result = service.getAllNewspapers();

		assertThat(result).containsExactly(np1, np2);
	}

	@Test
	void testSetNewspaperForLeague() {
		NewsPaper newspaper = mock(NewsPaper.class);
		League league = mock(League.class);

		service.setNewspaperForLeague(newspaper, league);

		verify(newspaperRepository).setNewsPaperForLeague(newspaper, league);
	}

	@Test
	void testGenerateCalendar_SavesMatches() {
		League league = mock(League.class);

		// Create 4 real teams (even number for round-robin)
		FantaTeam team1 = new FantaTeam("Team1", null, 0, null, Set.of());
		FantaTeam team2 = new FantaTeam("Team2", null, 0, null, Set.of());
		FantaTeam team3 = new FantaTeam("Team3", null, 0, null, Set.of());
		FantaTeam team4 = new FantaTeam("Team4", null, 0, null, Set.of());
		List<FantaTeam> teams = List.of(team1, team2, team3, team4);

		// 38 match days
		List<MatchDaySerieA> matchDays = new ArrayList<>();
		for (int i = 0; i < 38; i++) {
			matchDays.add(mock(MatchDaySerieA.class));
		}

		// Mock repositories
		when(fantaTeamRepository.getAllTeams(league)).thenReturn(teams);
		when(matchDayRepository.getAllMatchDays()).thenReturn(matchDays);

		// Call real method
		service.generateCalendar(league);

		// Verify matches saved
		verify(matchRepository, atLeastOnce()).saveMatch(any(Match.class));
	}

	@Test
	void testGenerateCalendar_LessThanTwoTeams_Throws() {
		League league = mock(League.class);
		when(fantaTeamRepository.getAllTeams(league)).thenReturn(List.of(mock(FantaTeam.class)));

		assertThatThrownBy(() -> service.generateCalendar(league)).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("At least 2 teams are required");
	}

	@Test
	void testGenerateCalendar_OddNumberOfTeams_Throws() {
		League league = mock(League.class);
		List<FantaTeam> teams = List.of(mock(FantaTeam.class), mock(FantaTeam.class), mock(FantaTeam.class));
		when(fantaTeamRepository.getAllTeams(league)).thenReturn(teams);

		assertThatThrownBy(() -> service.generateCalendar(league)).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Number of teams must be even");
	}

	@Test
	void testGenerateSchedule_EvenNumberOfTeams_Success() throws Exception {
		FantaTeam t1 = mock(FantaTeam.class);
		FantaTeam t2 = mock(FantaTeam.class);
		FantaTeam t3 = mock(FantaTeam.class);
		FantaTeam t4 = mock(FantaTeam.class);

		var method = AdminUserService.class.getDeclaredMethod("generateSchedule", List.class);
		method.setAccessible(true);
		@SuppressWarnings("unchecked")
		List<List<FantaTeam[]>> schedule = (List<List<FantaTeam[]>>) method.invoke(service, List.of(t1, t2, t3, t4));

		int expectedRounds = (4 - 1) * 2; // double round robin
		assertThat(schedule.size()).isEqualTo(expectedRounds);

		for (List<FantaTeam[]> round : schedule) {
			assertThat(round.size()).isEqualTo(2); // n/2 matches per round
		}
	}

	@Test
	void testGenerateSchedule_OddNumberOfTeams_Throws() throws Exception {
		FantaTeam t1 = mock(FantaTeam.class);
		FantaTeam t2 = mock(FantaTeam.class);
		FantaTeam t3 = mock(FantaTeam.class);

		assertThatThrownBy(() -> {
			var method = AdminUserService.class.getDeclaredMethod("generateSchedule", List.class);
			method.setAccessible(true);
			method.invoke(service, List.of(t1, t2, t3));
		}).hasCauseInstanceOf(IllegalArgumentException.class)
				.satisfies(ex -> assertThat(ex.getCause().getMessage()).contains("Number of teams must be even"));
	}

	@Test
	void testGenerateSchedule_DoubleRoundRobin_MirrorsCorrectly() throws Exception {
		FantaTeam t1 = mock(FantaTeam.class);
		FantaTeam t2 = mock(FantaTeam.class);
		FantaTeam t3 = mock(FantaTeam.class);
		FantaTeam t4 = mock(FantaTeam.class);

		var method = AdminUserService.class.getDeclaredMethod("generateSchedule", List.class);
		method.setAccessible(true);
		@SuppressWarnings("unchecked")
		List<List<FantaTeam[]>> schedule = (List<List<FantaTeam[]>>) method.invoke(service, List.of(t1, t2, t3, t4));

		int firstLegRounds = schedule.size() / 2;

		// Verify second leg is mirrored
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
		FantaTeam t1 = mock(FantaTeam.class);
		FantaTeam t2 = mock(FantaTeam.class);
		FantaTeam[] match = new FantaTeam[] { t1, t2 };

		List<FantaTeam[]> round = new ArrayList<>();
		round.add(match);
		List<List<FantaTeam[]>> schedule = new ArrayList<>();
		schedule.add(round);

		MatchDaySerieA matchDay = mock(MatchDaySerieA.class);
		List<MatchDaySerieA> matchDays = List.of(matchDay);

		List<Match> matches = service.createMatches(schedule, matchDays);
		assertThat(matches).hasSize(1);
		assertThat(matches.get(0).getTeam1()).isEqualTo(t1);
		assertThat(matches.get(0).getTeam2()).isEqualTo(t2);
		assertThat(matches.get(0).getMatchDaySerieA()).isEqualTo(matchDay);
	}

	@Test
	void testCreateMatches_MismatchThrows() {
		FantaTeam team1 = new FantaTeam("A", null, 0, null, Set.of());
		FantaTeam team2 = new FantaTeam("B", null, 0, null, Set.of());
		FantaTeam[] teamsArray = new FantaTeam[] { team1, team2 };

		List<FantaTeam[]> round = new ArrayList<>();
		round.add(teamsArray);
		List<List<FantaTeam[]>> schedule = new ArrayList<>();
		schedule.add(round);

		List<MatchDaySerieA> matchDays = List.of(); // empty, mismatch

		assertThatThrownBy(() -> service.createMatches(schedule, matchDays))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Schedule rounds and matchDays must have the same size");
	}

	@Test
	void testCreateMatches_MoreRoundsThanMatchDays_Throws() {
		FantaTeam team1 = mock(FantaTeam.class);
		FantaTeam team2 = mock(FantaTeam.class);
		FantaTeam team3 = mock(FantaTeam.class);
		FantaTeam team4 = mock(FantaTeam.class);

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

		System.out.println(schedule.size() + ", " + matchDays.size());

		assertThatThrownBy(() -> service.createMatches(schedule, matchDays))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Schedule rounds and matchDays must have the same size");
	}

	@Test
	void testCreateMatches_EmptySchedule_ReturnsEmptyList() {
		List<List<FantaTeam[]>> schedule = List.of();
		List<MatchDaySerieA> matchDays = List.of();

		List<Match> matches = service.createMatches(schedule, matchDays);

		assertThat(matches).isEmpty();
	}

	@Test
	void testCalculateGrades_UserNotAdmin_Throws() {
		FantaUser user = mock(FantaUser.class);
		League league = mock(League.class);
		when(league.getAdmin()).thenReturn(mock(FantaUser.class)); // different user

		assertThatThrownBy(() -> service.calculateGrades(user, league)).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("You are not the admin of the league");
	}

	@Test
	void testCalculateGrades_SeasonNotStarted_Throws() {
		FantaUser admin = mock(FantaUser.class);
		League league = mock(League.class);
		when(league.getAdmin()).thenReturn(admin);

		when(matchDayRepository.getPreviousMatchDay(any())).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.calculateGrades(admin, league)).isInstanceOf(RuntimeException.class)
				.hasMessageContaining("The season hasn't started yet");
	}

	@Test
	void testIsLegalToCalculateResults_Saturday() throws Exception {
		LocalDate saturday = LocalDate.of(2025, 9, 13); // Saturday
		var method = AdminUserService.class.getDeclaredMethod("isLegalToCalculateResults", LocalDate.class);
		method.setAccessible(true);

		boolean result = (boolean) method.invoke(service, saturday);
		assertThat(result).isFalse(); // cannot calculate immediately
	}

	@Test
	void testIsLegalToCalculateResults_Sunday() throws Exception {
		LocalDate sunday = LocalDate.of(2025, 9, 14); // Sunday
		var method = AdminUserService.class.getDeclaredMethod("isLegalToCalculateResults", LocalDate.class);
		method.setAccessible(true);

		boolean result = (boolean) method.invoke(service, sunday);
		assertThat(result).isFalse();
	}

	@Test
	void testIsLegalToCalculateResults_Weekday() throws Exception {
		LocalDate monday = LocalDate.of(2025, 9, 15); // Monday
		var method = AdminUserService.class.getDeclaredMethod("isLegalToCalculateResults", LocalDate.class);
		method.setAccessible(true);

		boolean result = (boolean) method.invoke(service, monday);
		assertThat(result).isFalse();
	}

}
