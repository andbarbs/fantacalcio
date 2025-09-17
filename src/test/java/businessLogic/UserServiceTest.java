package businessLogic;

import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

import domainModel.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import businessLogic.repositories.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import domainModel.Player.Club;
import domainModel.Player.Goalkeeper;

public class UserServiceTest {

	private MatchRepository matchRepository;
	private GradeRepository gradeRepository;
	private LineUpRepository lineUpRepository;
	private ResultsRepository resultRepository;
	private MatchDayRepository matchDayRepository;
	private FantaTeamRepository fantaTeamRepository;
	private LeagueRepository leagueRepository;
	private FantaTeamRepository teamRepository;
	private PlayerRepository playerRepository;
	private ProposalRepository proposalRepository;
	private ContractRepository contractRepository;
	private TransactionManager transactionManager;
	private TransactionContext context;
	private UserService userService;

	@BeforeEach
	void setUp() {
		transactionManager = mock(TransactionManager.class);
		context = mock(TransactionContext.class);

		// Mock repositories
		matchRepository = mock(MatchRepository.class);
		gradeRepository = mock(GradeRepository.class);
		lineUpRepository = mock(LineUpRepository.class);
		resultRepository = mock(ResultsRepository.class);
		matchDayRepository = mock(MatchDayRepository.class);
		fantaTeamRepository = mock(FantaTeamRepository.class);
		leagueRepository = mock(LeagueRepository.class);
		teamRepository = mock(FantaTeamRepository.class);
		playerRepository = mock(PlayerRepository.class);
		proposalRepository = mock(ProposalRepository.class);
		contractRepository = mock(ContractRepository.class);

		// When context.getXRepository() is called, return the mocked repository
		when(context.getMatchRepository()).thenReturn(matchRepository);
		when(context.getGradeRepository()).thenReturn(gradeRepository);
		when(context.getLineUpRepository()).thenReturn(lineUpRepository);
		when(context.getResultsRepository()).thenReturn(resultRepository);
		when(context.getMatchDayRepository()).thenReturn(matchDayRepository);
		when(context.getTeamRepository()).thenReturn(fantaTeamRepository);
		when(context.getLeagueRepository()).thenReturn(leagueRepository);
		when(context.getTeamRepository()).thenReturn(teamRepository);
		when(context.getPlayerRepository()).thenReturn(playerRepository);
		when(context.getProposalRepository()).thenReturn(proposalRepository);
		when(context.getContractRepository()).thenReturn(contractRepository);

		// For inTransaction
		doAnswer(invocation -> {
			Consumer<TransactionContext> code = invocation.getArgument(0);
			code.accept(context);
			return null;
		}).when(transactionManager).inTransaction(any());

		// For fromTransaction
		when(transactionManager.fromTransaction(any())).thenAnswer(invocation -> {
			Function<TransactionContext, Object> code = invocation.getArgument(0);
			return code.apply(context);
		});

		userService = new UserService(transactionManager);
	}

	@Test
	void testCreateLeague() {
		FantaUser admin = new FantaUser("admin@test.com", "pwd");
		NewsPaper np = new NewsPaper("Gazzetta");
		String leagueCode = "L001";

		// League code does not exist yet
		when(leagueRepository.getLeagueByCode(leagueCode)).thenReturn(Optional.empty());

		userService.createLeague("My League", admin, np, leagueCode);

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

		assertThatThrownBy(() -> userService.createLeague("New League", admin, np, leagueCode))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("A league with the same league code already exists");
	}
	
	@Test
	void testJoinLeague() {
		FantaUser user = new FantaUser("user@test.com", "pwd");
		League league = new League(user, "Test League", new NewsPaper("Gazzetta"), "L002");
		FantaTeam team = new FantaTeam("Team A", league, 0, user, Set.of());

		when(leagueRepository.getLeaguesByUser(user)).thenReturn(Collections.emptyList());

		userService.joinLeague(team, league);

		verify(teamRepository, times(1)).saveTeam(team);
	}
	
	@Test
	void testJoinLeague_TooManyTeams() {
		FantaUser user = new FantaUser("user@test.com", "pwd");
		League league = new League(user, "Test League", new NewsPaper("Gazzetta"), "L002");
		FantaTeam team = new FantaTeam("Team A", league, 0, user, Set.of());

		List<FantaTeam> teamList = new ArrayList<FantaTeam>();
		for (int i = 0; i < 30; i++) {
			teamList.add(new FantaTeam(null, league, i, user, null));
		}
		when(leagueRepository.getAllTeams(league)).thenReturn(teamList);

		assertThatThrownBy( () -> userService.joinLeague(team, league)).isInstanceOf(UnsupportedOperationException.class)
			.hasMessageContaining("Maximum 12 teams per league");
		
	}
	

	@Test
	void testJoinLeague_UserAlreadyInLeague() {
		FantaUser user = new FantaUser("user@test.com", "pwd");
		League league = new League(user, "Test League", new NewsPaper("Gazzetta"), "L002");
		FantaTeam team = new FantaTeam("Team A", league, 0, user, Set.of());

		when(leagueRepository.getLeaguesByUser(user)).thenReturn(List.of(league));

		assertThatThrownBy(() -> userService.joinLeague(team, league)).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("You have already a team in this league");

		verify(teamRepository, never()).saveTeam(any());
	}

	@Test
	void testSaveLineUp() {
		FantaUser user = new FantaUser("user@test.com", "pwd");
		League league = new League(user, "Test League", new NewsPaper("Gazzetta"), "L003");
		MatchDaySerieA matchDay = new MatchDaySerieA("MD1", LocalDate.of(2025, 9, 15)); // Monday
		FantaTeam team = new FantaTeam("Dream Team", league, 30, user, new HashSet<>());
		Match match = new Match(matchDay, team, team);
		LineUp lineUp = new _433LineUp._443LineUpBuilder(match, team).build();

		UserService spyService = spy(userService);
		doReturn(team).when(spyService).getFantaTeamByUserAndLeague(league, user);
		doReturn(LocalDate.of(2025, 9, 15)).when(spyService).today(); // Current Monday

		// Stub repos
		when(context.getMatchDayRepository().getPreviousMatchDay(any())).thenReturn(Optional.empty());
		when(context.getLineUpRepository().getLineUpByMatchAndTeam(match, team)).thenReturn(Optional.empty());

		spyService.saveLineUp(lineUp);

		verify(context.getLineUpRepository()).saveLineUp(lineUp);
	}

	@Test
	void testSaveLineUp_AfterMatchDate() {
		FantaUser user = new FantaUser("user@test.com", "pwd");
		League league = new League(user, "Test League", new NewsPaper("Gazzetta"), "L003");
		MatchDaySerieA matchDay = new MatchDaySerieA("MD1", LocalDate.of(2025, 9, 15)); // Monday
		FantaTeam team = new FantaTeam("Dream Team", league, 30, user, new HashSet<>());
		Match match = new Match(matchDay, team, team);
		LineUp lineUp = new _433LineUp._443LineUpBuilder(match, team).build();

		UserService spyService = spy(userService);
		doReturn(team).when(spyService).getFantaTeamByUserAndLeague(league, user);
		doReturn(LocalDate.of(2025, 9, 16)).when(spyService).today(); // Current date after match

		assertThatThrownBy(() -> spyService.saveLineUp(lineUp)).isInstanceOf(UnsupportedOperationException.class)
				.hasMessageContaining("Can't modify the lineup after the match is over");
	}

	@Test
	void testSaveLineUp_Weekend() {
		FantaUser user = new FantaUser("user@test.com", "pwd");
		League league = new League(user, "Test League", new NewsPaper("Gazzetta"), "L003");
		MatchDaySerieA matchDay = new MatchDaySerieA("MD1", LocalDate.of(2025, 9, 20)); // Monday match
		FantaTeam team = new FantaTeam("Dream Team", league, 30, user, new HashSet<>());
		Match match = new Match(matchDay, team, team);
		LineUp lineUp = new _433LineUp._443LineUpBuilder(match, team).build();

		UserService spyService = spy(userService);
		doReturn(team).when(spyService).getFantaTeamByUserAndLeague(league, user);
		doReturn(LocalDate.of(2025, 9, 20)).when(spyService).today(); // Saturday

		assertThatThrownBy(() -> spyService.saveLineUp(lineUp)).isInstanceOf(UnsupportedOperationException.class)
				.hasMessageContaining("Can't modify the lineup during Saturday and Sunday");
	}

	@Test
	void testSaveLineUp_PlayerNotInTeam() {
		FantaUser user = new FantaUser("user@test.com", "pwd");
		League league = new League(user, "Test League", new NewsPaper("Gazzetta"), "L003");
		MatchDaySerieA matchDay = new MatchDaySerieA("MD1", LocalDate.of(2025, 9, 15)); // Monday
		FantaTeam team = new FantaTeam("Dream Team", league, 30, user, new HashSet<>());

		Goalkeeper gk = new Goalkeeper("Gianluigi", "Buffon", Player.Club.JUVENTUS);
		Match match = new Match(matchDay, team, team);

		LineUp lineUp = new _433LineUp._443LineUpBuilder(match, team).withGoalkeeper(gk).build();

		UserService spyService = spy(userService);
		doReturn(team).when(spyService).getFantaTeamByUserAndLeague(league, user);
		doReturn(LocalDate.of(2025, 9, 15)).when(spyService).today(); // Monday

		assertThatThrownBy(() -> spyService.saveLineUp(lineUp)).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("does not belong to FantaTeam");
	}

	@Test
	void testSaveLineUp_PreviousMatchNotGraded() {
		FantaUser user = new FantaUser("user@test.com", "pwd");
		League league = new League(user, "Test League", new NewsPaper("Gazzetta"), "L004");
		MatchDaySerieA matchDay = new MatchDaySerieA("MD2", LocalDate.of(2025, 9, 15)); // Monday
		FantaTeam team = new FantaTeam("Dream Team", league, 30, user, new HashSet<>());
		Match previousMatch = mock(Match.class);
		Match match = new Match(matchDay, team, team);
		LineUp lineUp = new _433LineUp._443LineUpBuilder(match, team).build();

		UserService spyService = spy(userService);
		doReturn(team).when(spyService).getFantaTeamByUserAndLeague(league, user);
		doReturn(LocalDate.of(2025, 9, 15)).when(spyService).today(); // Monday

		when(context.getMatchDayRepository().getPreviousMatchDay(matchDay.getDate()))
				.thenReturn(Optional.of(mock(MatchDaySerieA.class)));
		when(context.getMatchRepository().getMatchByMatchDay(any(), eq(league), eq(team))).thenReturn(previousMatch);
		when(context.getResultsRepository().getResult(previousMatch)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> spyService.saveLineUp(lineUp)).isInstanceOf(UnsupportedOperationException.class)
				.hasMessageContaining("The grades for the previous match were not calculated");
	}

	@Test
	void testSaveLineUp_AlreadyExistsLineUp() {
		FantaUser user = new FantaUser("user@test.com", "pwd");
		League league = new League(user, "Test League", new NewsPaper("Gazzetta"), "L005");
		MatchDaySerieA matchDay = new MatchDaySerieA("MD3", LocalDate.of(2025, 9, 15)); // Monday
		FantaTeam team = new FantaTeam("Dream Team", league, 30, user, new HashSet<>());
		Match match = new Match(matchDay, team, team);
		LineUp lineUp = new _433LineUp._443LineUpBuilder(match, team).build();
		LineUp oldLineUp = mock(LineUp.class);

		UserService spyService = spy(userService);
		doReturn(team).when(spyService).getFantaTeamByUserAndLeague(league, user);
		doReturn(LocalDate.of(2025, 9, 15)).when(spyService).today(); // Monday

		when(context.getMatchDayRepository().getPreviousMatchDay(any())).thenReturn(Optional.empty());
		when(context.getLineUpRepository().getLineUpByMatchAndTeam(match, team)).thenReturn(Optional.of(oldLineUp));

		spyService.saveLineUp(lineUp);

		// verify that the old lineup is deleted and the new one saved
		verify(context.getLineUpRepository()).deleteLineUp(any(LineUp.class));
		verify(context.getLineUpRepository()).saveLineUp(lineUp);
	}

	@Test
	void testGetAllPlayers() {
		Player p1 = new Player.Goalkeeper(null, null, null);
		Player p2 = new Player.Goalkeeper(null, null, null);
		when(context.getPlayerRepository().findAll()).thenReturn(List.of(p1, p2));

		List<Player> result = userService.getAllPlayers();
		assertThat(result).containsExactly(p1, p2);
	}

	@Test
	void testGetPlayersBySurname() {
		Player p = new Player.Goalkeeper(null, null, null);
		when(context.getPlayerRepository().findBySurname("Rossi")).thenReturn(List.of(p));

		List<Player> result = userService.getPlayersBySurname("Rossi");
		assertThat(result).containsExactly(p);
	}

	@Test
	void testGetAllMatches() {
		League league = new League(null, null, null, null);
		MatchDaySerieA day1 = new MatchDaySerieA(null, null);
		Match m1 = new Match(day1, null, null);
		when(context.getMatchDayRepository().getAllMatchDays()).thenReturn(List.of(day1));
		when(context.getMatchRepository().getAllMatchesByMatchDay(day1, league)).thenReturn(List.of(m1));

		Map<MatchDaySerieA, List<Match>> result = userService.getAllMatches(league);
		assertThat(result.get(day1)).containsExactly(m1);
	}

	@Test
	void testGetNextMatch() {
		League league = new League(null, null, null, null);
		FantaTeam team = new FantaTeam(null, league, 0, null, null);
		MatchDaySerieA prev = new MatchDaySerieA(null, null);
		MatchDaySerieA next = new MatchDaySerieA(null, null);
		Match prevMatch = new Match(next, team, team);
		Match nextMatch = new Match(next, team, team);

		when(context.getMatchDayRepository().getPreviousMatchDay(any())).thenReturn(Optional.of(prev));
		when(context.getMatchRepository().getMatchByMatchDay(prev, league, team)).thenReturn(prevMatch);
		when(resultRepository.getResult(prevMatch)).thenReturn(Optional.of(mock(Result.class)));
		when(context.getMatchDayRepository().getNextMatchDay(any())).thenReturn(Optional.of(next));
		when(context.getMatchRepository().getMatchByMatchDay(next, league, team)).thenReturn(nextMatch);

		Match result = userService.getNextMatch(league, team, LocalDate.now());
		assertThat(result).isEqualTo(nextMatch);
	}

	@Test
	void testGetNextMatch_PreviousResultMissing() {
		League league = new League(null, null, null, null);
		FantaTeam team = new FantaTeam(null, league, 0, null, null);
		LocalDate today = LocalDate.now();
		MatchDaySerieA prev = new MatchDaySerieA(null, today);

		when(context.getMatchDayRepository().getPreviousMatchDay(today)).thenReturn(Optional.of(prev));
		Match prevMatch = new Match(prev, team, team);
		when(context.getMatchRepository().getMatchByMatchDay(prev, league, team)).thenReturn(prevMatch);
		when(resultRepository.getResult(prevMatch)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> userService.getNextMatch(league, team, today)).isInstanceOf(RuntimeException.class)
				.hasMessageContaining("results for the previous match have not been calculated yet");
	}

	@Test
	void testGetNextMatch_LeagueEnded() {
		League league = new League(null, null, null, null);
		FantaTeam team = new FantaTeam(null, league, 0, null, null);
		LocalDate today = LocalDate.now();

		when(context.getMatchDayRepository().getPreviousMatchDay(today)).thenReturn(Optional.empty());
		when(context.getMatchDayRepository().getNextMatchDay(today)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> userService.getNextMatch(league, team, today)).isInstanceOf(RuntimeException.class)
				.hasMessageContaining("The league ended");
	}

	@Test
	void testGetStandings() {
		League league = new League(null, null, null, null);
		FantaTeam t1 = mock(FantaTeam.class);
		FantaTeam t2 = mock(FantaTeam.class);

		when(t1.getPoints()).thenReturn(10);
		when(t2.getPoints()).thenReturn(20);

		UserService spyService = spy(userService);
		doReturn(List.of(t1, t2)).when(spyService).getAllFantaTeams(league);

		List<FantaTeam> standings = spyService.getStandings(league);

		assertThat(standings).containsExactly(t2, t1);
	}

	@Test
	void testGetFantaTeamByUserAndLeague() {
		League league = new League(null, null, null, null);
		FantaUser user = new FantaUser(null, null);
		FantaTeam team = new FantaTeam(null, league, 0, user, null);
		when(teamRepository.getFantaTeamByUserAndLeague(league, user)).thenReturn(team);

		FantaTeam result = userService.getFantaTeamByUserAndLeague(league, user);
		assertThat(result).isEqualTo(team);
	}

	@Test
	void testGetAllTeamProposals() {
		League league = new League(null, null, null, null);
		FantaTeam team = new FantaTeam(null, league, 0, null, null);
		Proposal p = new Proposal.PendingProposal(null, null);
		when(context.getProposalRepository().getMyProposals(league, team)).thenReturn(List.of(p));

		List<Proposal> result = userService.getAllTeamProposals(league, team);
		assertThat(result).containsExactly(p);
	}

	@Test
	void testGetResultByMatch() {
		Match match = new Match(null, null, null);
		Result res = new Result(0, 0, 0, 0, match);
		when(resultRepository.getResult(match)).thenReturn(Optional.of(res));

		Optional<Result> result = userService.getResultByMatch(match);
		assertThat(result).contains(res);
	}

	@Test
	void testGetLineUpByMatch() {
		Match match = new Match(null, null, null);
		FantaTeam team = new FantaTeam(null, null, 0, null, null);
		LineUp lineUp = mock(LineUp.class);
		when(context.getLineUpRepository().getLineUpByMatchAndTeam(match, team)).thenReturn(Optional.of(lineUp));

		Optional<LineUp> result = userService.getLineUpByMatch(match, team);
		assertThat(result).contains(lineUp);
	}

	@Test
	void testAcceptProposal_NotInvolved() {
		FantaTeam myTeam = mock(FantaTeam.class);
		FantaTeam otherTeam = mock(FantaTeam.class);

		Contract requestedContract = mock(Contract.class);
		when(requestedContract.getTeam()).thenReturn(otherTeam);

		Contract offeredContract = mock(Contract.class);
		when(offeredContract.getTeam()).thenReturn(mock(FantaTeam.class));

		Proposal.PendingProposal proposal = mock(Proposal.PendingProposal.class);
		when(proposal.getRequestedContract()).thenReturn(requestedContract);
		when(proposal.getOfferedContract()).thenReturn(offeredContract);

		// Stub isSameTeam to return false
		when(otherTeam.isSameTeam(myTeam)).thenReturn(false);

		assertThatThrownBy(() -> userService.acceptProposal(proposal, myTeam))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("You are not involved in this proposal");
	}

	@Test
	void testAcceptProposal() {
		// Setup teams and players
		FantaTeam myTeam = mock(FantaTeam.class);
		FantaTeam offeringTeam = new FantaTeam(null, null, 0, null, null);
		Player offeredPlayer = new Player.Forward(null, null, null);
		Player requestedPlayer = new Player.Midfielder(null, null, null);

		// Contracts
		Contract offeredContract = mock(Contract.class);
		when(offeredContract.getTeam()).thenReturn(offeringTeam);
		when(offeredContract.getPlayer()).thenReturn(offeredPlayer);

		Contract requestedContract = mock(Contract.class);
		when(requestedContract.getTeam()).thenReturn(myTeam);
		when(requestedContract.getPlayer()).thenReturn(requestedPlayer);

		// Proposal
		Proposal.PendingProposal proposal = mock(Proposal.PendingProposal.class);
		when(proposal.getRequestedContract()).thenReturn(requestedContract);
		when(proposal.getOfferedContract()).thenReturn(offeredContract);

		// Stub isSameTeam
		when(myTeam.isSameTeam(myTeam)).thenReturn(true);

		// Stub searchContract to return non-empty Optionals
		UserService userServiceSpy = spy(userService);
		doReturn(Optional.of(requestedContract)).when(userServiceSpy).searchContract(myTeam, requestedPlayer);
		doReturn(Optional.of(offeredContract)).when(userServiceSpy).searchContract(offeringTeam, offeredPlayer);

		// Run test
		userServiceSpy.acceptProposal(proposal, myTeam);

		// Verify repository interactions
		verify(context.getContractRepository()).deleteContract(requestedContract);
		verify(context.getContractRepository()).deleteContract(offeredContract);
		verify(context.getContractRepository(), times(2)).saveContract(any(Contract.class));
		verify(context.getProposalRepository()).deleteProposal(proposal);
	}

	@Test
	void testAcceptProposal_ContractsMissing() {
		FantaTeam myTeam = mock(FantaTeam.class);
		FantaTeam offeringTeam = new FantaTeam(null, null, 0, null, null);
		Player offeredPlayer = mock(Player.class);
		Player requestedPlayer = mock(Player.class);

		Contract offeredContract = mock(Contract.class);
		when(offeredContract.getTeam()).thenReturn(offeringTeam);
		when(offeredContract.getPlayer()).thenReturn(offeredPlayer);

		Contract requestedContract = mock(Contract.class);
		when(requestedContract.getTeam()).thenReturn(myTeam);
		when(requestedContract.getPlayer()).thenReturn(requestedPlayer);

		Proposal.PendingProposal proposal = mock(Proposal.PendingProposal.class);
		when(proposal.getRequestedContract()).thenReturn(requestedContract);
		when(proposal.getOfferedContract()).thenReturn(offeredContract);

		// Stub isSameTeam so the first check passes
		when(myTeam.isSameTeam(myTeam)).thenReturn(true);

		// Create a simple subclass of UserService for testing that overrides
		// searchContract
		UserService testService = new UserService(transactionManager) {
			@Override
			protected Optional<Contract> searchContract(FantaTeam team, Player player) {
				// Return empty to simulate missing contracts
				return Optional.empty();
			}

		};

		assertThatThrownBy(() -> testService.acceptProposal(proposal, myTeam))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("One or both players do not play anymore");
	}

	@Test
	void testRejectProposal() {
		// The team calling rejectProposal
		FantaTeam myTeam = mock(FantaTeam.class);

		// The other team involved in the proposal
		FantaTeam otherTeam = new FantaTeam(null, null, 0, null, null);

		// Requested and offered contracts
		Contract requestedContract = mock(Contract.class);
		Contract offeredContract = mock(Contract.class);

		// Set the teams in the contracts
		when(requestedContract.getTeam()).thenReturn(myTeam); // myTeam is requesting
		when(offeredContract.getTeam()).thenReturn(otherTeam); // other team is offering

		// Create a mock proposal
		Proposal.PendingProposal proposal = mock(Proposal.PendingProposal.class);
		when(proposal.getRequestedContract()).thenReturn(requestedContract);
		when(proposal.getOfferedContract()).thenReturn(offeredContract);

		// Stub isSameTeam to make the involvement check succeed
		when(myTeam.isSameTeam(myTeam)).thenReturn(true);
		when(myTeam.isSameTeam(otherTeam)).thenReturn(false);

		// Call the method under test
		userService.rejectProposal(proposal, myTeam);

		// Verify repository interactions
		verify(context.getProposalRepository()).deleteProposal(proposal);
		verify(context.getProposalRepository()).saveProposal(any(Proposal.RejectedProposal.class));
	}

	@Test
	void testRejectProposal_RequestedNotInvolved() {
		FantaTeam myTeam = mock(FantaTeam.class);
		Proposal.PendingProposal proposal = mock(Proposal.PendingProposal.class);
		FantaTeam reqTeam = new FantaTeam(null, null, 0, null, null);
		FantaTeam offTeam = new FantaTeam(null, null, 0, null, null);
		when(proposal.getRequestedContract()).thenReturn(mock(Contract.class));
		when(proposal.getRequestedContract().getTeam()).thenReturn(reqTeam);
		when(proposal.getOfferedContract()).thenReturn(mock(Contract.class));
		when(proposal.getOfferedContract().getTeam()).thenReturn(offTeam);

		assertThatThrownBy(() -> userService.rejectProposal(proposal, myTeam))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("You are not involved in this proposal");
	}

	@Test
	void testRejectProposal_OfferedNotInvolved() {
		FantaTeam myTeam = mock(FantaTeam.class);
		Proposal.PendingProposal proposal = mock(Proposal.PendingProposal.class);
		FantaTeam reqTeam = new FantaTeam(null, null, 0, null, null);
		FantaTeam offTeam = new FantaTeam(null, null, 0, null, null);
		when(proposal.getRequestedContract()).thenReturn(new Contract(reqTeam, new Player.Goalkeeper(null, null, Club.ATALANTA)));
		when(proposal.getOfferedContract()).thenReturn(mock(Contract.class));
		when(proposal.getOfferedContract().getTeam()).thenReturn(offTeam);

		assertThatThrownBy(() -> userService.rejectProposal(proposal, myTeam))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("You are not involved in this proposal");
	}

	@Test
	void testCreateProposal_DifferentRoles() {
		FantaTeam myTeam = new FantaTeam(null, null, 0, null, null);
		FantaTeam opponentTeam = new FantaTeam(null, null, 0, null, null);
		Player requestedPlayer = new Player.Forward(null, null, null);
		Player offeredPlayer = new Player.Goalkeeper(null, null, null);

		assertThatThrownBy(() -> userService.createProposal(requestedPlayer, offeredPlayer, myTeam, opponentTeam))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("The players don't have the same role");
	}

	@Test
	void testCreateProposal_EveryContractMissing() {
		FantaTeam myTeam = new FantaTeam(null, null, 0, null, new HashSet<Contract>());
		FantaTeam opponentTeam = new FantaTeam(null, null, 0, null, new HashSet<Contract>());
		Player myPlayer = new Player.Forward(null, null, null);
		Player oppPlayer = new Player.Forward(null, null, null);

		assertThat(userService.createProposal(myPlayer, oppPlayer, myTeam, opponentTeam)).isFalse();
	}
	
	@Test
	void testCreateProposal_RequestedContractMissing() {
		FantaTeam myTeam = new FantaTeam(null, null, 0, null, new HashSet<Contract>());
		FantaTeam opponentTeam = new FantaTeam(null, null, 0, null, new HashSet<Contract>());
		Player myPlayer = new Player.Forward(null, null, null);
		Player oppPlayer = new Player.Forward(null, null, null);

		myTeam.setContracts(Set.of(new Contract(myTeam, myPlayer)));

		assertThat(userService.createProposal(myPlayer, oppPlayer, myTeam, opponentTeam)).isFalse();
	}
	
	@Test
	void testCreateProposal_OfferedContractMissing() {
		FantaTeam myTeam = new FantaTeam(null, null, 0, null, new HashSet<Contract>());
		FantaTeam opponentTeam = new FantaTeam(null, null, 0, null, new HashSet<Contract>());
		Player myPlayer = new Player.Forward(null, null, null);
		Player oppPlayer = new Player.Forward(null, null, null);
		
		opponentTeam.setContracts(Set.of(new Contract(opponentTeam, oppPlayer)));

		assertThat(userService.createProposal(myPlayer, oppPlayer, myTeam, opponentTeam)).isFalse();
	}

	@Test
	void testCreateProposal_AlreadyExists() {
		League league = new League(null, null, null, null);
		FantaUser user = new FantaUser(null, null);
		FantaTeam myTeam = spy(new FantaTeam("My Team", league, 0, user, new HashSet<>()));
		FantaTeam opponentTeam = new FantaTeam("Opponent", league, 0, user, new HashSet<>());
		Player player = new Player.Midfielder(null, null, null);

		Contract offeredContract = new Contract(myTeam, player);
		Contract requestedContract = new Contract(opponentTeam, player);
		myTeam.getContracts().add(offeredContract);
		opponentTeam.getContracts().add(requestedContract);

		when(context.getProposalRepository().getProposal(offeredContract, requestedContract))
				.thenReturn(Optional.of(new Proposal.PendingProposal()));

		assertThatThrownBy(() -> userService.createProposal(player, player, myTeam, opponentTeam))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("The proposal already exists");
	}

	@Test
	void testCreateProposal_HappyPath() {
		League league = new League(null, null, null, null);
		FantaUser user = new FantaUser(null, null);
		FantaTeam myTeam = spy(new FantaTeam("My Team", league, 0, user, new HashSet<>()));
		FantaTeam opponentTeam = new FantaTeam("Opponent", league, 0, user, new HashSet<>());
		Player offeredPlayer = new Player.Defender(null, null, null);
		Player requestedPlayer = new Player.Defender(null, null, null);

		Contract offeredContract = new Contract(myTeam, offeredPlayer);
		Contract requestedContract = new Contract(opponentTeam, requestedPlayer);
		myTeam.getContracts().add(offeredContract);
		opponentTeam.getContracts().add(requestedContract);

		when(context.getProposalRepository().getProposal(offeredContract, requestedContract))
				.thenReturn(Optional.empty());
		when(context.getProposalRepository().saveProposal(any())).thenReturn(true);

		assertThat(userService.createProposal(requestedPlayer, offeredPlayer, myTeam, opponentTeam)).isTrue();
	}

	@Test
	void testGetAllMatchGrades() {
		Match match = new Match(null, null, null);
		Grade grade = new Grade(null, null, 0, null);
		NewsPaper newsPaper = new NewsPaper("Gazzetta");

		when(context.getGradeRepository().getAllMatchGrades(match, newsPaper)).thenReturn(List.of(grade));
		List<Grade> result = userService.getAllMatchGrades(match, newsPaper);
		assertThat(result).containsExactly(grade);
	}
	
	@Test
	void testGetAllFantaTeams() {
	    League league = new League(null, "My League", new NewsPaper("Gazzetta"), "L999");
	    FantaTeam t1 = new FantaTeam("Team 1", league, 0, new FantaUser("u1", "pwd"), Set.of());
	    FantaTeam t2 = new FantaTeam("Team 2", league, 0, new FantaUser("u2", "pwd"), Set.of());

	    when(context.getTeamRepository().getAllTeams(league)).thenReturn(List.of(t1, t2));

	    List<FantaTeam> result = userService.getAllFantaTeams(league);

	    assertThat(result).containsExactly(t1, t2);
	    verify(context.getTeamRepository(), times(1)).getAllTeams(league);
	}

}
