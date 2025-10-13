package business;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;

import domain.scheme.Scheme433;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import business.ports.repository.ContractRepository;
import business.ports.repository.FantaTeamRepository;
import business.ports.repository.GradeRepository;
import business.ports.repository.LeagueRepository;
import business.ports.repository.LineUpRepository;
import business.ports.repository.MatchDayRepository;
import business.ports.repository.MatchRepository;
import business.ports.repository.PlayerRepository;
import business.ports.repository.ProposalRepository;
import business.ports.repository.ResultsRepository;
import business.ports.transaction.TransactionManager;
import business.ports.transaction.TransactionManager.TransactionContext;
import domain.*;
import domain.Player.Defender;
import domain.Player.Forward;
import domain.Player.Goalkeeper;
import domain.Player.Midfielder;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Tag("mockito-agent")
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
		String leagueCode = "L001";

		// League code does not exist yet
		when(leagueRepository.getLeagueByCode(leagueCode)).thenReturn(Optional.empty());

		userService.createLeague("My League", admin, leagueCode);

		// Verify that saveLeague was called
        League myLeague = new League(admin, "My League", leagueCode);
        verify(leagueRepository, times(1)).saveLeague(myLeague);
        ArgumentCaptor<MatchDay> captor = ArgumentCaptor.forClass(MatchDay.class);
        verify(matchDayRepository, times(MatchDay.MATCH_DAYS_IN_LEAGUE)).saveMatchDay(captor.capture());
        List<MatchDay> allValues = captor.getAllValues();
        allValues.forEach(matchDay -> {
            assertThat(matchDay.getLeague()).isEqualTo(myLeague);
            assertThat(matchDay.getStatus()).isEqualTo(MatchDay.Status.FUTURE);
        });
        assertThat(allValues.stream().map(MatchDay::getNumber).toList()).containsExactlyInAnyOrderElementsOf(IntStream.range(1, MatchDay.MATCH_DAYS_IN_LEAGUE+1).boxed().toList());
        assertThat(allValues.stream().map(MatchDay::getName).toList()).containsExactlyInAnyOrderElementsOf(IntStream.range(1, MatchDay.MATCH_DAYS_IN_LEAGUE+1).mapToObj(value ->
                "MatchDay " + value).toList());
	}

	@Test
	void testCreateLeague_LeagueCodeExists() {
		FantaUser admin = new FantaUser("admin@test.com", "pwd");
		String leagueCode = "L001";

		League existingLeague = new League(admin, "Existing League", leagueCode);
		when(leagueRepository.getLeagueByCode(leagueCode)).thenReturn(Optional.of(existingLeague));

		assertThatThrownBy(() -> userService.createLeague("New League", admin, leagueCode))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("A league with the same league code already exists");
	}

    //TODO ricontrolla tutta la logica che mi convince poco
	@Test
	void testJoinLeague() {
		FantaUser user = new FantaUser("user@test.com", "pwd");
		League league = new League(user, "Test League", "L002");
		FantaTeam team = new FantaTeam("Team A", league, 0, user, Set.of());

		when(leagueRepository.getLeaguesByMember(user)).thenReturn(Set.of());

		userService.joinLeague(team, league);
		verify(teamRepository, times(1)).saveTeam(team);
	}

	@Test
	void testJoinLeague_TooManyTeams() {
		FantaUser user = new FantaUser("user@test.com", "pwd");
		League league = new League(user, "Test League", "L002");
		FantaTeam team = new FantaTeam("Team A", league, 0, user, Set.of());

		List<FantaTeam> teamList = new ArrayList<FantaTeam>();
		for (int i = 0; i < 30; i++) {
			teamList.add(new FantaTeam(null, league, i, user, null));
		}
		when(leagueRepository.getAllTeams(league)).thenReturn(teamList);

		assertThatThrownBy(() -> userService.joinLeague(team, league)).isInstanceOf(UnsupportedOperationException.class)
				.hasMessageContaining("Maximum 12 teams per league");

	}

	@Test
	void testJoinLeague_UserAlreadyInLeague() {
		FantaUser user = new FantaUser("user@test.com", "pwd");
		League league = new League(user, "Test League", "L002");
		FantaTeam team = new FantaTeam("Team A", league, 0, user, Set.of());

		when(leagueRepository.getLeaguesByMember(user)).thenReturn(Set.of(league));

		assertThatThrownBy(() -> userService.joinLeague(team, league)).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("You have already a team in this league");

		verify(teamRepository, never()).saveTeam(any());
	}

	@Test
	void testSaveLineUp() {
		FantaUser user = new FantaUser("user@test.com", "pwd");
		League league = new League(user, "Test League", "L003");
		MatchDay matchDay = new MatchDay("MD1",1, MatchDay.Status.FUTURE, league);
		
		// Players for LineUp
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
		
		// team & contracts
		HashSet<Contract> contracts = new HashSet<>();
		FantaTeam team = new FantaTeam("Dream Team", league, 30, user, contracts);
		players.forEach(player -> contracts.add(new Contract(team, player)));

		// match
		Match match = new Match(matchDay, team, team);
		
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

		UserService spyService = spy(userService);
		doReturn(team).when(spyService).getFantaTeamByUserAndLeague(league, user);
        //TODO posso eliminarlo tranquillamente giusto?
		//doReturn(LocalDate.of(2025, 9, 15)).when(spyService).today(); // Current Monday

		// Stub repos
		when(context.getMatchDayRepository().getLatestEndedMatchDay(any())).thenReturn(Optional.empty());
		when(context.getLineUpRepository().getLineUpByMatchAndTeam(match, team)).thenReturn(Optional.empty());

        //TODO serve spy?
		spyService.saveLineUp(lineUp);

		verify(context.getLineUpRepository()).saveLineUp(lineUp);
	}

	@Test
	void testSaveLineUp_AfterMatchDate() {
		FantaUser user = new FantaUser("user@test.com", "pwd");
		League league = new League(user, "Test League", "L003");
		MatchDay matchDay = new MatchDay("MD1",1, MatchDay.Status.PAST, league); // Monday
		FantaTeam team = new FantaTeam("Dream Team", league, 30, user, new HashSet<>());
		Match match = new Match(matchDay, team, team);
		LineUp lineUp = LineUp.build()
				.forTeam(team)
				.inMatch(match)
				.withStarterLineUp(Scheme433.starterLineUp()
						.withGoalkeeper(new Goalkeeper("portiere", "titolare", Player.Club.ATALANTA))
						.withDefenders(
								new Player.Defender("difensore1", "titolare", Player.Club.ATALANTA),
								new Player.Defender("difensore2", "titolare", Player.Club.ATALANTA),
								new Player.Defender("difensore3", "titolare", Player.Club.ATALANTA),
								new Player.Defender("difensore4", "titolare", Player.Club.ATALANTA))
						.withMidfielders(
								new Player.Midfielder("centrocampista1", "titolare", Player.Club.ATALANTA),
								new Player.Midfielder("centrocampista2", "titolare", Player.Club.ATALANTA),
								new Player.Midfielder("centrocampista3", "titolare", Player.Club.ATALANTA))
						.withForwards(
								new Player.Forward("attaccante1", "titolare", Player.Club.ATALANTA),
								new Player.Forward("attaccante2", "titolare", Player.Club.ATALANTA),
								new Player.Forward("attaccante3", "titolare", Player.Club.ATALANTA)))
				.withSubstituteGoalkeepers(
						new Goalkeeper("portiere1", "panchina", Player.Club.ATALANTA),
						new Goalkeeper("portiere2", "panchina", Player.Club.ATALANTA),
						new Goalkeeper("portiere3", "panchina", Player.Club.ATALANTA))
				.withSubstituteDefenders(
						new Player.Defender("difensore1", "panchina", Player.Club.ATALANTA),
						new Player.Defender("difensore2", "panchina", Player.Club.ATALANTA),
						new Player.Defender("difensore3", "panchina", Player.Club.ATALANTA))
				.withSubstituteMidfielders(
						new Player.Midfielder("centrocampista1", "panchina", Player.Club.ATALANTA),
						new Player.Midfielder("centrocampista2", "panchina", Player.Club.ATALANTA),
						new Player.Midfielder("centrocampista3", "panchina", Player.Club.ATALANTA))
				.withSubstituteForwards(
						new Player.Forward("attaccante1", "panchina", Player.Club.ATALANTA),
						new Player.Forward("attaccante2", "panchina", Player.Club.ATALANTA),
						new Player.Forward("attaccante3", "panchina", Player.Club.ATALANTA));

		UserService spyService = spy(userService);
		doReturn(team).when(spyService).getFantaTeamByUserAndLeague(league, user);
        //TODO serve spy?
		//doReturn(LocalDate.of(2025, 9, 16)).when(spyService).today(); // Current date after match

		assertThatThrownBy(() -> spyService.saveLineUp(lineUp)).isInstanceOf(UnsupportedOperationException.class)
				.hasMessageContaining("Can't modify the lineup after the match is over");
	}

	@Test
	void testSaveLineUp_Weekend() {
		FantaUser user = new FantaUser("user@test.com", "pwd");
		League league = new League(user, "Test League", "L003");
		MatchDay matchDay = new MatchDay("MD1",1, MatchDay.Status.FUTURE, league); // Monday match
		FantaTeam team = new FantaTeam("Dream Team", league, 30, user, new HashSet<>());
		Match match = new Match(matchDay, team, team);
		LineUp lineUp = LineUp.build()
				.forTeam(team)
				.inMatch(match)
				.withStarterLineUp(Scheme433.starterLineUp()
						.withGoalkeeper(new Goalkeeper("portiere", "titolare", Player.Club.ATALANTA))
						.withDefenders(
								new Player.Defender("difensore1", "titolare", Player.Club.ATALANTA),
								new Player.Defender("difensore2", "titolare", Player.Club.ATALANTA),
								new Player.Defender("difensore3", "titolare", Player.Club.ATALANTA),
								new Player.Defender("difensore4", "titolare", Player.Club.ATALANTA))
						.withMidfielders(
								new Player.Midfielder("centrocampista1", "titolare", Player.Club.ATALANTA),
								new Player.Midfielder("centrocampista2", "titolare", Player.Club.ATALANTA),
								new Player.Midfielder("centrocampista3", "titolare", Player.Club.ATALANTA))
						.withForwards(
								new Player.Forward("attaccante1", "titolare", Player.Club.ATALANTA),
								new Player.Forward("attaccante2", "titolare", Player.Club.ATALANTA),
								new Player.Forward("attaccante3", "titolare", Player.Club.ATALANTA)))
				.withSubstituteGoalkeepers(
						new Goalkeeper("portiere1", "panchina", Player.Club.ATALANTA),
						new Goalkeeper("portiere2", "panchina", Player.Club.ATALANTA),
						new Goalkeeper("portiere3", "panchina", Player.Club.ATALANTA))
				.withSubstituteDefenders(
						new Player.Defender("difensore1", "panchina", Player.Club.ATALANTA),
						new Player.Defender("difensore2", "panchina", Player.Club.ATALANTA),
						new Player.Defender("difensore3", "panchina", Player.Club.ATALANTA))
				.withSubstituteMidfielders(
						new Player.Midfielder("centrocampista1", "panchina", Player.Club.ATALANTA),
						new Player.Midfielder("centrocampista2", "panchina", Player.Club.ATALANTA),
						new Player.Midfielder("centrocampista3", "panchina", Player.Club.ATALANTA))
				.withSubstituteForwards(
						new Player.Forward("attaccante1", "panchina", Player.Club.ATALANTA),
						new Player.Forward("attaccante2", "panchina", Player.Club.ATALANTA),
						new Player.Forward("attaccante3", "panchina", Player.Club.ATALANTA));

		UserService spyService = spy(userService);
		doReturn(team).when(spyService).getFantaTeamByUserAndLeague(league, user);
        //TODO stesso discorso
		//doReturn(LocalDate.of(2025, 9, 20)).when(spyService).today(); // Saturday

		assertThatThrownBy(() -> spyService.saveLineUp(lineUp)).isInstanceOf(UnsupportedOperationException.class)
				.hasMessageContaining("Can't modify the lineup during Saturday and Sunday");
	}

	@Test
	void testSaveLineUp_PlayerNotInTeam() {
		FantaUser user = new FantaUser("user@test.com", "pwd");
		League league = new League(user, "Test League", "L003");
		MatchDay matchDay = new MatchDay("MD1",1, MatchDay.Status.FUTURE, league);
		FantaTeam team = new FantaTeam("Dream Team", league, 30, user, new HashSet<>());

		// Goalkeeper gk = new Goalkeeper("Gianluigi", "Buffon", Player.Club.JUVENTUS);
		Match match = new Match(matchDay, team, team);

		LineUp lineUp = LineUp.build()
				.forTeam(team)
				.inMatch(match)
				.withStarterLineUp(Scheme433.starterLineUp()
						.withGoalkeeper(new Goalkeeper("portiere", "titolare", Player.Club.ATALANTA))
						.withDefenders(
								new Player.Defender("difensore1", "titolare", Player.Club.ATALANTA),
								new Player.Defender("difensore2", "titolare", Player.Club.ATALANTA),
								new Player.Defender("difensore3", "titolare", Player.Club.ATALANTA),
								new Player.Defender("difensore4", "titolare", Player.Club.ATALANTA))
						.withMidfielders(
								new Player.Midfielder("centrocampista1", "titolare", Player.Club.ATALANTA),
								new Player.Midfielder("centrocampista2", "titolare", Player.Club.ATALANTA),
								new Player.Midfielder("centrocampista3", "titolare", Player.Club.ATALANTA))
						.withForwards(
								new Player.Forward("attaccante1", "titolare", Player.Club.ATALANTA),
								new Player.Forward("attaccante2", "titolare", Player.Club.ATALANTA),
								new Player.Forward("attaccante3", "titolare", Player.Club.ATALANTA)))
				.withSubstituteGoalkeepers(
						new Goalkeeper("portiere1", "panchina", Player.Club.ATALANTA),
						new Goalkeeper("portiere2", "panchina", Player.Club.ATALANTA),
						new Goalkeeper("portiere3", "panchina", Player.Club.ATALANTA))
				.withSubstituteDefenders(
						new Player.Defender("difensore1", "panchina", Player.Club.ATALANTA),
						new Player.Defender("difensore2", "panchina", Player.Club.ATALANTA),
						new Player.Defender("difensore3", "panchina", Player.Club.ATALANTA))
				.withSubstituteMidfielders(
						new Player.Midfielder("centrocampista1", "panchina", Player.Club.ATALANTA),
						new Player.Midfielder("centrocampista2", "panchina", Player.Club.ATALANTA),
						new Player.Midfielder("centrocampista3", "panchina", Player.Club.ATALANTA))
				.withSubstituteForwards(
						new Player.Forward("attaccante1", "panchina", Player.Club.ATALANTA),
						new Player.Forward("attaccante2", "panchina", Player.Club.ATALANTA),
						new Player.Forward("attaccante3", "panchina", Player.Club.ATALANTA));

		UserService spyService = spy(userService);
		doReturn(team).when(spyService).getFantaTeamByUserAndLeague(league, user);
        //TODO same
		//doReturn(LocalDate.of(2025, 9, 15)).when(spyService).today(); // Monday

		assertThatThrownBy(() -> spyService.saveLineUp(lineUp)).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("does not belong to FantaTeam");
	}

	@Test
	void testSaveLineUp_PreviousMatchNotGraded() {
		FantaUser user = new FantaUser("user@test.com", "pwd");
		League league = new League(user, "Test League", "L004");
		MatchDay matchDay = new MatchDay("MD2",2, MatchDay.Status.FUTURE, league);
		FantaTeam team = new FantaTeam("Dream Team", league, 30, user, new HashSet<>());
		Match previousMatch = mock(Match.class);
		Match match = new Match(matchDay, team, team);
		LineUp lineUp = LineUp.build()
				.forTeam(team)
				.inMatch(match)
				.withStarterLineUp(Scheme433.starterLineUp()
						.withGoalkeeper(new Goalkeeper("portiere", "titolare", Player.Club.ATALANTA))
						.withDefenders(
								new Player.Defender("difensore1", "titolare", Player.Club.ATALANTA),
								new Player.Defender("difensore2", "titolare", Player.Club.ATALANTA),
								new Player.Defender("difensore3", "titolare", Player.Club.ATALANTA),
								new Player.Defender("difensore4", "titolare", Player.Club.ATALANTA))
						.withMidfielders(
								new Player.Midfielder("centrocampista1", "titolare", Player.Club.ATALANTA),
								new Player.Midfielder("centrocampista2", "titolare", Player.Club.ATALANTA),
								new Player.Midfielder("centrocampista3", "titolare", Player.Club.ATALANTA))
						.withForwards(
								new Player.Forward("attaccante1", "titolare", Player.Club.ATALANTA),
								new Player.Forward("attaccante2", "titolare", Player.Club.ATALANTA),
								new Player.Forward("attaccante3", "titolare", Player.Club.ATALANTA)))
				.withSubstituteGoalkeepers(
						new Goalkeeper("portiere1", "panchina", Player.Club.ATALANTA),
						new Goalkeeper("portiere2", "panchina", Player.Club.ATALANTA),
						new Goalkeeper("portiere3", "panchina", Player.Club.ATALANTA))
				.withSubstituteDefenders(
						new Player.Defender("difensore1", "panchina", Player.Club.ATALANTA),
						new Player.Defender("difensore2", "panchina", Player.Club.ATALANTA),
						new Player.Defender("difensore3", "panchina", Player.Club.ATALANTA))
				.withSubstituteMidfielders(
						new Player.Midfielder("centrocampista1", "panchina", Player.Club.ATALANTA),
						new Player.Midfielder("centrocampista2", "panchina", Player.Club.ATALANTA),
						new Player.Midfielder("centrocampista3", "panchina", Player.Club.ATALANTA))
				.withSubstituteForwards(
						new Player.Forward("attaccante1", "panchina", Player.Club.ATALANTA),
						new Player.Forward("attaccante2", "panchina", Player.Club.ATALANTA),
						new Player.Forward("attaccante3", "panchina", Player.Club.ATALANTA));

		UserService spyService = spy(userService);
		doReturn(team).when(spyService).getFantaTeamByUserAndLeague(league, user);
        //TODO same
		//doReturn(LocalDate.of(2025, 9, 15)).when(spyService).today(); // Monday

		when(context.getMatchDayRepository().getLatestEndedMatchDay(league))
				.thenReturn(Optional.of(mock(MatchDay.class)));
		when(context.getMatchRepository().getMatchBy(any(), eq(team))).thenReturn(Optional.of(previousMatch));
		when(context.getResultsRepository().getResultFor(previousMatch)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> spyService.saveLineUp(lineUp)).isInstanceOf(UnsupportedOperationException.class)
				.hasMessageContaining("The grades for the previous match were not calculated");
	}

	@Test
	void testSaveLineUp_AlreadyExistsLineUp() {
		FantaUser user = new FantaUser("user@test.com", "pwd");
		League league = new League(user, "Test League", "L005");
		MatchDay matchDay = new MatchDay("MD3",3, MatchDay.Status.FUTURE, league); // Monday
		
		// Players for LineUp
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
		
		HashSet<Contract> contracts = new HashSet<>();
		FantaTeam team = new FantaTeam("Dream Team", league, 30, user, contracts);
		players.stream().map(player -> new Contract(team, player)).forEach(contracts::add);
		Match match = new Match(matchDay, team, team);
		
		// LineUps
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
		LineUp oldLineUp = mock(LineUp.class);

		UserService spyService = spy(userService);
		doReturn(team).when(spyService).getFantaTeamByUserAndLeague(league, user);
        //TODO same
		//doReturn(LocalDate.of(2025, 9, 15)).when(spyService).today(); // Monday

		when(context.getMatchDayRepository().getLatestEndedMatchDay(any())).thenReturn(Optional.empty());
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
		when(context.getPlayerRepository().findAll()).thenReturn(Set.of(p1, p2));

		Set<Player> result = userService.getAllPlayers();
		assertThat(result).containsExactlyInAnyOrder(p1, p2);
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
        FantaUser user = new FantaUser("user@test.com", "pwd");
        League league = new League(user, "Test League", "L005");
		MatchDay day1 = new MatchDay("1 giornata", 1, MatchDay.Status.FUTURE, league);
		Match m1 = new Match(day1, null, null);
		when(context.getMatchDayRepository().getAllMatchDays(league)).thenReturn(List.of(day1));
		when(context.getMatchRepository().getAllMatchesIn(day1)).thenReturn(List.of(m1));

		Map<MatchDay, List<Match>> result = userService.getAllMatches(league);
		assertThat(result.get(day1)).containsExactly(m1);
	}

	@Test
	void testGetStandings() {
        FantaUser user = new FantaUser("user@test.com", "pwd");
        League league = new League(user, "Test League", "L005");
		FantaTeam t1 = mock(FantaTeam.class);
		FantaTeam t2 = mock(FantaTeam.class);

		when(t1.getPoints()).thenReturn(10);
		when(t2.getPoints()).thenReturn(20);

        //TODO ha senso spy?
		UserService spyService = spy(userService);
		doReturn(List.of(t1, t2)).when(spyService).getAllFantaTeams(league);

		List<FantaTeam> standings = spyService.getStandings(league);

		assertThat(standings).containsExactly(t2, t1);
	}

	@Test
	void testGetFantaTeamByUserAndLeague() {
        FantaUser user = new FantaUser("user@test.com", "pwd");
        League league = new League(user, "Test League", "L005");
        FantaTeam team = new FantaTeam("FantaTeam", league, 0, user, new HashSet<>());
		when(teamRepository.getFantaTeamByUserAndLeague(league, user)).thenReturn(Optional.of(team));

		Optional<FantaTeam> result = userService.getFantaTeamByUserAndLeague(league, user);
		assertThat(result).hasValue(team);
	}

	@Test
	void testGetAllTeamProposals() {
        FantaUser user = new FantaUser("user@test.com", "pwd");
        League league = new League(user, "Test League", "L005");
        FantaTeam team = new FantaTeam("FantaTeam", league, 0, user, new HashSet<>());
		Proposal p = new Proposal(null, null);
		when(context.getProposalRepository().getProposalsFor(team)).thenReturn(Set.of(p));

		Set<Proposal> result = userService.getAllTeamProposals(league, team);
		assertThat(result).containsExactly(p);
	}

	@Test
	void testGetResultByMatch() {
		Match match = new Match(null, null, null);
		Result res = new Result(0, 0, 0, 0, match);
		when(resultRepository.getResultFor(match)).thenReturn(Optional.of(res));

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

		Proposal proposal = mock(Proposal.class);
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
		Proposal proposal = mock(Proposal.class);
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

		Proposal proposal = mock(Proposal.class);
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
		Proposal proposal = mock(Proposal.class);
		when(proposal.getRequestedContract()).thenReturn(requestedContract);
		when(proposal.getOfferedContract()).thenReturn(offeredContract);

		// Stub isSameTeam to make the involvement check succeed
		when(myTeam.isSameTeam(myTeam)).thenReturn(true);
		when(myTeam.isSameTeam(otherTeam)).thenReturn(false);

		// Call the method under test
		userService.rejectProposal(proposal, myTeam);

		// Verify repository interactions
		verify(context.getProposalRepository()).deleteProposal(proposal);
        //TODO perchè any?
		verify(context.getProposalRepository()).saveProposal(any(Proposal.class));
	}

	@Test
	void testRejectProposal_RequestedNotInvolved() {
		FantaTeam myTeam = mock(FantaTeam.class);
		Proposal proposal = mock(Proposal.class);
		FantaTeam reqTeam = mock(FantaTeam.class);
		FantaTeam offTeam = mock(FantaTeam.class);
		when(proposal.getRequestedContract()).thenReturn(mock(Contract.class));
		when(proposal.getRequestedContract().getTeam()).thenReturn(reqTeam);
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
        FantaUser user = new FantaUser("user@test.com", "pwd");
        League league = new League(user, "Test League", "L003");
		FantaTeam myTeam = spy(new FantaTeam("My Team", league, 0, user, new HashSet<>()));
		FantaTeam opponentTeam = new FantaTeam("Opponent", league, 0, user, new HashSet<>());
		Player player = new Player.Midfielder(null, null, null);

		Contract offeredContract = new Contract(myTeam, player);
		Contract requestedContract = new Contract(opponentTeam, player);
		myTeam.getContracts().add(offeredContract);
		opponentTeam.getContracts().add(requestedContract);

        //TODO bo?
		when(context.getProposalRepository().getProposalBy(offeredContract, requestedContract))
				.thenReturn(Optional.of(new Proposal(offeredContract,requestedContract)));

		assertThatThrownBy(() -> userService.createProposal(player, player, myTeam, opponentTeam))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("The proposal already exists");
	}

	@Test
	void testCreateProposal_HappyPath() {
        FantaUser user = new FantaUser("user@test.com", "pwd");
        League league = new League(user, "Test League", "L003");
		FantaTeam myTeam = spy(new FantaTeam("My Team", league, 0, user, new HashSet<>()));
		FantaTeam opponentTeam = new FantaTeam("Opponent", league, 0, user, new HashSet<>());
		Player offeredPlayer = new Player.Defender(null, null, null);
		Player requestedPlayer = new Player.Defender(null, null, null);

		Contract offeredContract = new Contract(myTeam, offeredPlayer);
		Contract requestedContract = new Contract(opponentTeam, requestedPlayer);
		myTeam.getContracts().add(offeredContract);
		opponentTeam.getContracts().add(requestedContract);

		when(context.getProposalRepository().getProposalBy(offeredContract, requestedContract))
				.thenReturn(Optional.empty());
		when(context.getProposalRepository().saveProposal(any())).thenReturn(true);

		assertThat(userService.createProposal(requestedPlayer, offeredPlayer, myTeam, opponentTeam)).isTrue();
	}

	@Test
	void testGetAllMatchGrades() {
        FantaUser user = new FantaUser("user@test.com", "pwd");
        League league = new League(user, "Test League", "L003");
        MatchDay matchDay = new MatchDay("MD1",1, MatchDay.Status.FUTURE, league);
		Match match = new Match(matchDay, null, null);
		Grade grade = new Grade(null, null, 0);

		when(context.getGradeRepository().getAllGrades(matchDay)).thenReturn(List.of(grade));
		List<Grade> result = userService.getAllMatchGrades(match);
		assertThat(result).containsExactly(grade);
	}

	@Test
	void testGetAllFantaTeams() {
		League league = new League(null, "My League", "L999");
		FantaTeam t1 = new FantaTeam("Team 1", league, 0, new FantaUser("u1", "pwd"), Set.of());
		FantaTeam t2 = new FantaTeam("Team 2", league, 0, new FantaUser("u2", "pwd"), Set.of());

		when(context.getTeamRepository().getAllTeams(league)).thenReturn(Set.of(t1, t2));

		Set<FantaTeam> result = userService.getAllFantaTeams(league);

		assertThat(result).containsExactlyInAnyOrder(t1, t2);
		verify(context.getTeamRepository(), times(1)).getAllTeams(league);
	}

}
