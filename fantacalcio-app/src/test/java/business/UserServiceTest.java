package business;

import static org.junit.jupiter.api.Assertions.assertTrue;
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
		for (int i = 0; i < 8; i++) {
			teamList.add(new FantaTeam(null, league, i, user, null));
		}
		when(leagueRepository.getAllTeams(league)).thenReturn(teamList);

		assertThatThrownBy(() -> userService.joinLeague(team, league)).isInstanceOf(UnsupportedOperationException.class)
				.hasMessageContaining("Maximum 8 teams per league");

	}

    @Test
    void testJoinLeagueAsJournalist_AdminTriesToJoin(){
        FantaUser user = new FantaUser("user@test.com", "pwd");
        League league = new League(user, "Test League", "L002");

        assertThatThrownBy(() -> userService.joinLeagueAsJournalist(league,user)).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("l'admin non può essere il giornalista");
    }

    @Test
    void testJoinLeagueAsJournalist_JournalistPresent(){
        FantaUser user = new FantaUser("user@test.com", "pwd");
        League league = new League(user, "Test League", "L002");
        FantaUser journalist = new FantaUser("j@mail", "psw");
        FantaUser otherJournalist = new FantaUser("o@mail", "psw");
        league.setNewsPaper(journalist);

        assertThatThrownBy(() -> userService.joinLeagueAsJournalist(league,journalist)).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("La lega ha già un giornalista associato!");
    }

    @Test
    void testJoinLeagueAsJournalist() {
        FantaUser user = new FantaUser("user@test.com", "pwd");
        League league = new League(user, "Test League", "L002");
        FantaUser journalist = new FantaUser("j@mail", "psw");

        when(leagueRepository.getLeaguesByMember(user)).thenReturn(Set.of());

        userService.joinLeagueAsJournalist(league,journalist);
        assertTrue(league.getNewsPaper().equals(journalist));
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
        FantaUser admin = new FantaUser("admin@test.com", "pwd");
        FantaUser user = new FantaUser("user@test.com", "pwd");
        FantaUser user2 = new FantaUser("user2@test.com", "pwd");
        League league = new League(admin, "Test League", "L003");
        MatchDay matchDay = new MatchDay("MD2",2, MatchDay.Status.FUTURE, league);
        MatchDay previousMatchDay = new MatchDay("MD1",1, MatchDay.Status.PAST, league);
        HashSet<Contract> contracts = new HashSet<>();
        FantaTeam team = new FantaTeam("Dream Team", league, 30, user, contracts);
        FantaTeam team2 = new FantaTeam("Dream Team2", league, 30, user2, null);
        Match match = new Match(matchDay, team, team2);
        Match previousMatch = new Match(matchDay, team2, team);
        Result previousResult = new Result(70, 60, 1, 0, previousMatch);
		
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
		players.forEach(player -> contracts.add(new Contract(team, player)));
		
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

		// Stub repos
        when(context.getMatchDayRepository().getEarliestUpcomingMatchDay(league)).thenReturn(Optional.of(matchDay));
        when(context.getMatchRepository().getMatchBy(matchDay, team)).thenReturn(Optional.of(match));
		when(context.getMatchDayRepository().getLatestEndedMatchDay(league)).thenReturn(Optional.of(previousMatchDay));
        when(context.getMatchRepository().getMatchBy(previousMatchDay, team)).thenReturn(Optional.of(previousMatch));
        when(context.getResultsRepository().getResultFor(previousMatch)).thenReturn(Optional.of(previousResult));
		when(context.getLineUpRepository().getLineUpByMatchAndTeam(match, team)).thenReturn(Optional.empty());

		userService.saveLineUp(lineUp);

		verify(context.getLineUpRepository()).saveLineUp(lineUp);
	}

	@Test
	void testSaveLineUp_MatchDayNotCorrect() {
        FantaUser admin = new FantaUser("admin@test.com", "pwd");
        FantaUser user = new FantaUser("user@test.com", "pwd");
        FantaUser user2 = new FantaUser("user2@test.com", "pwd");
        League league = new League(admin, "Test League", "L003");
        MatchDay matchDay = new MatchDay("MD1",1, MatchDay.Status.PAST, league);
        MatchDay nextMatchDay = new MatchDay("MD2",2, MatchDay.Status.FUTURE, league);
        HashSet<Contract> contracts = new HashSet<>();
        FantaTeam team = new FantaTeam("Dream Team", league, 30, user, contracts);
        FantaTeam team2 = new FantaTeam("Dream Team2", league, 30, user2, null);
        Match match = new Match(matchDay, team, team2);
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

        when(context.getMatchDayRepository().getEarliestUpcomingMatchDay(league)).thenReturn(Optional.of(nextMatchDay));

		assertThatThrownBy(() -> userService.saveLineUp(lineUp)).isInstanceOf(RuntimeException.class)
				.hasMessageContaining("The matchDay of the lineUp is incorrect");
        verifyNoMoreInteractions(lineUpRepository);
	}

	@Test
	void testSaveLineUp_IncorrectTeamInLineUp() {
        FantaUser admin = new FantaUser("admin@test.com", "pwd");
        FantaUser user = new FantaUser("user@test.com", "pwd");
        FantaUser user2 = new FantaUser("user2@test.com", "pwd");
        League league = new League(admin, "Test League", "L003");
        MatchDay matchDay = new MatchDay("MD2",2, MatchDay.Status.FUTURE, league);
        HashSet<Contract> contracts = new HashSet<>();
        FantaTeam team = new FantaTeam("Dream Team", league, 30, user, contracts);
        FantaTeam team2 = new FantaTeam("Dream Team2", league, 30, user2, null);
        FantaTeam team3 = new FantaTeam("Dream Team3", league, 30, admin, null);
        Match match = new Match(matchDay, team, team2);
		LineUp lineUp = LineUp.build()
				.forTeam(team3)
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


        when(context.getMatchDayRepository().getEarliestUpcomingMatchDay(league)).thenReturn(Optional.of(matchDay));
		assertThatThrownBy(() -> userService.saveLineUp(lineUp)).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("The fantaTeam in the lineUp is not correct");
        verifyNoMoreInteractions(lineUpRepository);
	}

    @Test
    void testSaveLineUp_LeagueEnded() {
        FantaUser admin = new FantaUser("admin@test.com", "pwd");
        FantaUser user = new FantaUser("user@test.com", "pwd");
        FantaUser user2 = new FantaUser("user2@test.com", "pwd");
        League league = new League(admin, "Test League", "L003");
        MatchDay matchDay = new MatchDay("MD2",2, MatchDay.Status.FUTURE, league);
        HashSet<Contract> contracts = new HashSet<>();
        FantaTeam team = new FantaTeam("Dream Team", league, 30, user, contracts);
        FantaTeam team2 = new FantaTeam("Dream Team2", league, 30, user2, null);
        Match match = new Match(matchDay, team, team2);

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
        players.forEach(player -> contracts.add(new Contract(team, player)));

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

        // Stub repos
        when(context.getMatchDayRepository().getEarliestUpcomingMatchDay(league)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.saveLineUp(lineUp)).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("The league ended");
        verifyNoMoreInteractions(lineUpRepository);
    }

    @Test
    void testSaveLineUp_NoSuchMatchExist() {
        FantaUser admin = new FantaUser("admin@test.com", "pwd");
        FantaUser user = new FantaUser("user@test.com", "pwd");
        FantaUser user2 = new FantaUser("user2@test.com", "pwd");
        League league = new League(admin, "Test League", "L003");
        MatchDay matchDay = new MatchDay("MD2",2, MatchDay.Status.FUTURE, league);
        HashSet<Contract> contracts = new HashSet<>();
        FantaTeam team = new FantaTeam("Dream Team", league, 30, user, contracts);
        FantaTeam team2 = new FantaTeam("Dream Team2", league, 30, user2, null);
        Match match = new Match(matchDay, team, team2);
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
        players.forEach(player -> contracts.add(new Contract(team, player)));

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

        // Stub repos
        when(context.getMatchDayRepository().getEarliestUpcomingMatchDay(league)).thenReturn(Optional.of(matchDay));
        when(context.getMatchRepository().getMatchBy(matchDay, team)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.saveLineUp(lineUp)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("The match does not exists");
        verifyNoMoreInteractions(lineUpRepository);
    }

    @Test
    void testSaveLineUp_TeamInLineUpNotCorrect() {
        FantaUser admin = new FantaUser("admin@test.com", "pwd");
        FantaUser user = new FantaUser("user@test.com", "pwd");
        FantaUser user2 = new FantaUser("user2@test.com", "pwd");
        League league = new League(admin, "Test League", "L003");
        MatchDay matchDay = new MatchDay("MD2",2, MatchDay.Status.FUTURE, league);
        HashSet<Contract> contracts = new HashSet<>();
        FantaTeam team = new FantaTeam("Dream Team", league, 30, user, contracts);
        FantaTeam team2 = new FantaTeam("Dream Team2", league, 30, user2, null);
        FantaTeam team3 = new FantaTeam("Dream Team3", league, 30, admin, null);
        Match incorrectMatch = new Match(matchDay, team, team2);
        Match correctMatch = new Match(matchDay, team, team3);


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
        players.forEach(player -> contracts.add(new Contract(team, player)));

        LineUp lineUp = LineUp.build()
                .forTeam(team)
                .inMatch(incorrectMatch)
                .withStarterLineUp(Scheme433.starterLineUp()
                        .withGoalkeeper(gk1)
                        .withDefenders(d1, d2, d3, d4)
                        .withMidfielders(m1, m2, m3)
                        .withForwards(f1, f2, f3))
                .withSubstituteGoalkeepers(sgk1, sgk2, sgk3)
                .withSubstituteDefenders(sd1, sd2, sd3)
                .withSubstituteMidfielders(sm1, sm2, sm3)
                .withSubstituteForwards(sf1, sf2, sf3);

        // Stub repos
        when(context.getMatchDayRepository().getEarliestUpcomingMatchDay(league)).thenReturn(Optional.of(matchDay));
        when(context.getMatchRepository().getMatchBy(matchDay, team)).thenReturn(Optional.of(correctMatch));

        assertThatThrownBy(() -> userService.saveLineUp(lineUp)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("The match is not correct");
        verifyNoMoreInteractions(lineUpRepository);
    }


    @Test
	void testSaveLineUp_PlayerNotInTeam() {
        FantaUser admin = new FantaUser("admin@test.com", "pwd");
        FantaUser user = new FantaUser("user@test.com", "pwd");
        FantaUser user2 = new FantaUser("user2@test.com", "pwd");
        League league = new League(admin, "Test League", "L003");
        MatchDay matchDay = new MatchDay("MD2",2, MatchDay.Status.FUTURE, league);
        MatchDay previousMatchDay = new MatchDay("MD1",1, MatchDay.Status.PAST, league);
        HashSet<Contract> contracts = new HashSet<>();
        FantaTeam team = new FantaTeam("Dream Team", league, 30, user, contracts);
        FantaTeam team2 = new FantaTeam("Dream Team2", league, 30, user2, null);
        Match match = new Match(matchDay, team, team2);
        Match previousMatch = new Match(matchDay, team2, team);
        Result previousResult = new Result(70, 60, 1, 0, previousMatch);


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

        when(context.getMatchDayRepository().getEarliestUpcomingMatchDay(league)).thenReturn(Optional.of(matchDay));
        when(context.getMatchRepository().getMatchBy(matchDay, team)).thenReturn(Optional.of(match));
        when(context.getMatchDayRepository().getLatestEndedMatchDay(league)).thenReturn(Optional.of(previousMatchDay));
        when(context.getMatchRepository().getMatchBy(previousMatchDay, team)).thenReturn(Optional.of(previousMatch));
        when(context.getResultsRepository().getResultFor(previousMatch)).thenReturn(Optional.of(previousResult));
        when(context.getLineUpRepository().getLineUpByMatchAndTeam(match, team)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> userService.saveLineUp(lineUp)).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("does not belong to FantaTeam");
	}

	@Test
	void testSaveLineUp_PreviousMatchNotGraded() {
        FantaUser admin = new FantaUser("admin@test.com", "pwd");
        FantaUser user = new FantaUser("user@test.com", "pwd");
        FantaUser user2 = new FantaUser("user2@test.com", "pwd");
        League league = new League(admin, "Test League", "L003");
        MatchDay matchDay = new MatchDay("MD2",2, MatchDay.Status.FUTURE, league);
        MatchDay previousMatchDay = new MatchDay("MD1",1, MatchDay.Status.PAST, league);
        HashSet<Contract> contracts = new HashSet<>();
        FantaTeam team = new FantaTeam("Dream Team", league, 30, user, contracts);
        FantaTeam team2 = new FantaTeam("Dream Team2", league, 30, user2, null);
        Match match = new Match(matchDay, team, team2);
        Match previousMatch = new Match(matchDay, team2, team);

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

        when(context.getMatchDayRepository().getEarliestUpcomingMatchDay(league)).thenReturn(Optional.of(matchDay));
        when(context.getMatchRepository().getMatchBy(matchDay, team)).thenReturn(Optional.of(match));
        when(context.getMatchDayRepository().getLatestEndedMatchDay(league)).thenReturn(Optional.of(previousMatchDay));
        when(context.getMatchRepository().getMatchBy(previousMatchDay, team)).thenReturn(Optional.of(previousMatch));
        when(context.getResultsRepository().getResultFor(previousMatch)).thenReturn(Optional.empty());
        when(context.getLineUpRepository().getLineUpByMatchAndTeam(match, team)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> userService.saveLineUp(lineUp)).isInstanceOf(UnsupportedOperationException.class)
				.hasMessageContaining("The grades for the previous match were not calculated");
	}

	@Test
	void testSaveLineUp_AlreadyExistsLineUp() {
        FantaUser admin = new FantaUser("admin@test.com", "pwd");
        FantaUser user = new FantaUser("user@test.com", "pwd");
        FantaUser user2 = new FantaUser("user2@test.com", "pwd");
        League league = new League(admin, "Test League", "L003");
        MatchDay matchDay = new MatchDay("MD2",2, MatchDay.Status.FUTURE, league);
        MatchDay previousMatchDay = new MatchDay("MD1",1, MatchDay.Status.PAST, league);
        HashSet<Contract> contracts = new HashSet<>();
        FantaTeam team = new FantaTeam("Dream Team", league, 30, user, contracts);
        FantaTeam team2 = new FantaTeam("Dream Team2", league, 30, user2, null);
        Match match = new Match(matchDay, team, team2);
        Match previousMatch = new Match(matchDay, team2, team);
        Result previousResult = new Result(70, 60, 1, 0, previousMatch);
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

		players.stream().map(player -> new Contract(team, player)).forEach(contracts::add);
		
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


        when(context.getMatchDayRepository().getEarliestUpcomingMatchDay(league)).thenReturn(Optional.of(matchDay));
        when(context.getMatchRepository().getMatchBy(matchDay, team)).thenReturn(Optional.of(match));
        when(context.getMatchDayRepository().getLatestEndedMatchDay(league)).thenReturn(Optional.of(previousMatchDay));
        when(context.getMatchRepository().getMatchBy(previousMatchDay, team)).thenReturn(Optional.of(previousMatch));
        when(context.getResultsRepository().getResultFor(previousMatch)).thenReturn(Optional.of(previousResult));
        when(context.getLineUpRepository().getLineUpByMatchAndTeam(match, team)).thenReturn(Optional.of(oldLineUp));

		userService.saveLineUp(lineUp);

		// verify that the old lineup is deleted and the new one saved
		verify(context.getLineUpRepository()).deleteLineUp(any(LineUp.class));
		verify(context.getLineUpRepository()).saveLineUp(lineUp);
	}

	@Test
	void testGetAllPlayers() {
		Player p1 = new Player.Goalkeeper("Alessandro", "Buongiorno", Player.Club.NAPOLI);
		Player p2 = new Player.Goalkeeper("Nico", "Paz", Player.Club.COMO);
		when(context.getPlayerRepository().findAll()).thenReturn(Set.of(p1, p2));

		Set<Player> result = userService.getAllPlayers();
		assertThat(result).containsExactlyInAnyOrder(p1, p2);
	}

	@Test
	void testGetPlayersBySurname() {
		Player p = new Player.Goalkeeper("Mile", "Svilar", Player.Club.ROMA);
		when(context.getPlayerRepository().findBySurname("Svilar")).thenReturn(List.of(p));

		List<Player> result = userService.getPlayersBySurname("Svilar");
		assertThat(result).containsExactly(p);
	}

	@Test
	void testGetAllMatches() {
        FantaUser user = new FantaUser("user@test.com", "pwd");
        FantaUser user1 = new FantaUser("mail", "psw");
        League league = new League(user, "Test League", "L005");
		MatchDay day1 = new MatchDay("1 giornata", 1, MatchDay.Status.FUTURE, league);
        FantaTeam team = new FantaTeam("Team", league, 0, user, null);
        FantaTeam team1 = new FantaTeam("Team1", league, 0, user1, null);
		Match m1 = new Match(day1, team, team1);
		when(context.getMatchDayRepository().getAllMatchDays(league)).thenReturn(List.of(day1));
		when(context.getMatchRepository().getAllMatchesIn(day1)).thenReturn(List.of(m1));

		Map<MatchDay, List<Match>> result = userService.getAllMatches(league);
		assertThat(result.get(day1)).containsExactly(m1);
	}

	@Test
	void testGetStandings() {
        FantaUser user = new FantaUser("user@test.com", "pwd");
        FantaUser user1 = new FantaUser("mail", "psw");
        League league = new League(user, "Test League", "L005");
        FantaTeam team = new FantaTeam("Team", league, 10, user, null);
        FantaTeam team1 = new FantaTeam("Team1", league, 20, user1, null);
        when(teamRepository.getAllTeams(league)).thenReturn(Set.of(team, team1));

		List<FantaTeam> standings = userService.getStandings(league);

		assertThat(standings).containsExactly(team1, team);
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
        FantaUser user1 = new FantaUser("mail", "psw");
        League league = new League(user, "Test League", "L005");
        HashSet<Contract> contracts = new HashSet<>();
        HashSet<Contract> contracts1 = new HashSet<>();
        FantaTeam team = new FantaTeam("FantaTeam", league, 0, user, contracts);
        FantaTeam team1 = new FantaTeam("FantaTeam1", league, 0, user1, contracts1);
        Player p1 = new Player.Goalkeeper("Alessandro", "Buongiorno", Player.Club.NAPOLI);
        Player p2 = new Player.Goalkeeper("Nico", "Paz", Player.Club.COMO);
        Contract contract = new Contract(team, p1);
        Contract contract1 = new Contract(team1, p2);
		Proposal p = new Proposal(contract, contract1);
        contracts.add(contract);
        contracts1.add(contract1);
		when(context.getProposalRepository().getProposalsFor(team)).thenReturn(Set.of(p));

		Set<Proposal> result = userService.getAllTeamProposals(team);
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
        FantaUser user = new FantaUser("user@test.com", "pwd");
        FantaUser user1 = new FantaUser("mail", "psw");
        FantaUser user2 = new FantaUser("mail2", "psw2");
        League league = new League(user, "Test League", "L005");
        HashSet<Contract> contracts = new HashSet<>();
        HashSet<Contract> contracts1 = new HashSet<>();
        FantaTeam team = new FantaTeam("FantaTeam", league, 0, user, contracts);
        FantaTeam team1 = new FantaTeam("FantaTeam1", league, 0, user1, contracts1);
        FantaTeam team2 = new FantaTeam("FantaTeam2", league, 0, user2, null);
        Player p1 = new Player.Goalkeeper("Alessandro", "Buongiorno", Player.Club.NAPOLI);
        Player p2 = new Player.Goalkeeper("Nico", "Paz", Player.Club.COMO);
        Contract offeredContract = new Contract(team, p1);
        Contract requestedContract = new Contract(team1, p2);
        Proposal proposal = new Proposal(offeredContract, requestedContract);
        contracts.add(offeredContract);
        contracts1.add(requestedContract);

        when(proposalRepository.getProposalBy(offeredContract, requestedContract)).thenReturn(Optional.of(proposal));
		assertThatThrownBy(() -> userService.acceptProposal(proposal, team2))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("You are not involved in this proposal");
	}

	@Test
	void testAcceptProposal() {
        FantaUser user = new FantaUser("user@test.com", "pwd");
        FantaUser user1 = new FantaUser("mail", "psw");
        League league = new League(user, "Test League", "L005");
        HashSet<Contract> contracts = new HashSet<>();
        HashSet<Contract> contracts1 = new HashSet<>();
        FantaTeam team = new FantaTeam("FantaTeam", league, 0, user, contracts);
        FantaTeam team1 = new FantaTeam("FantaTeam1", league, 0, user1, contracts1);
        Player p1 = new Player.Goalkeeper("Alessandro", "Buongiorno", Player.Club.NAPOLI);
        Player p2 = new Player.Goalkeeper("Nico", "Paz", Player.Club.COMO);
        Contract offeredContract = new Contract(team, p1);
        Contract requestedContract = new Contract(team1, p2);
        Proposal proposal = new Proposal(offeredContract, requestedContract);
        contracts.add(offeredContract);
        contracts1.add(requestedContract);
        Contract receivedContract = new Contract(team, p2);
        Contract givenContract = new Contract(team1, p1);

        when(proposalRepository.getProposalBy(proposal.getOfferedContract(), proposal.getRequestedContract())).thenReturn(Optional.of(proposal));

		// Run test
		userService.acceptProposal(proposal, team1);

		// Verify repository interactions
		verify(context.getContractRepository()).deleteContract(requestedContract);
		verify(context.getContractRepository()).deleteContract(offeredContract);
		verify(context.getContractRepository()).saveContract(receivedContract);
        verify(context.getContractRepository()).saveContract(givenContract);
		verify(context.getProposalRepository()).deleteProposal(proposal);
        verifyNoMoreInteractions(contractRepository);
        verifyNoMoreInteractions(resultRepository);
	}

	@Test
	void testAcceptProposal_ContractsMissing() {
        FantaUser user = new FantaUser("user@test.com", "pwd");
        FantaUser user1 = new FantaUser("mail", "psw");
        League league = new League(user, "Test League", "L005");
        HashSet<Contract> contracts = new HashSet<>();
        HashSet<Contract> contracts1 = new HashSet<>();
        FantaTeam team = new FantaTeam("FantaTeam", league, 0, user, contracts);
        FantaTeam team1 = new FantaTeam("FantaTeam1", league, 0, user1, contracts1);
        Player p1 = new Player.Goalkeeper("Alessandro", "Buongiorno", Player.Club.NAPOLI);
        Player p2 = new Player.Goalkeeper("Nico", "Paz", Player.Club.COMO);
        Contract offeredContract = new Contract(team, p1);
        Contract requestedContract = new Contract(team1, p2);
        Proposal proposal = new Proposal(offeredContract, requestedContract);
        contracts.add(offeredContract);

		// Create a simple subclass of UserService for testing that overrides
		// searchContract

        when(proposalRepository.getProposalBy(proposal.getOfferedContract(), proposal.getRequestedContract())).thenReturn(Optional.of(proposal));


        assertThatThrownBy(() -> userService.acceptProposal(proposal, team1))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("One or both players do not play anymore");
        verifyNoMoreInteractions(contractRepository);
	}

    @Test
    void testAcceptProposal_ProposalNotEsists() {
        FantaUser user = new FantaUser("user@test.com", "pwd");
        FantaUser user1 = new FantaUser("mail", "psw");
        League league = new League(user, "Test League", "L005");
        HashSet<Contract> contracts = new HashSet<>();
        HashSet<Contract> contracts1 = new HashSet<>();
        FantaTeam team = new FantaTeam("FantaTeam", league, 0, user, contracts);
        FantaTeam team1 = new FantaTeam("FantaTeam1", league, 0, user1, contracts1);
        Player p1 = new Player.Goalkeeper("Alessandro", "Buongiorno", Player.Club.NAPOLI);
        Player p2 = new Player.Goalkeeper("Nico", "Paz", Player.Club.COMO);
        Contract offeredContract = new Contract(team, p1);
        Contract requestedContract = new Contract(team1, p2);
        Proposal proposal = new Proposal(offeredContract, requestedContract);
        contracts.add(offeredContract);
        contracts1.add(requestedContract);

        when(proposalRepository.getProposalBy(proposal.getOfferedContract(), proposal.getRequestedContract())).thenReturn(Optional.empty());


        assertThatThrownBy(() -> userService.acceptProposal(proposal, team1))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Proposal does not exists");
        verifyNoMoreInteractions(contractRepository);
        verify(proposalRepository).getProposalBy(proposal.getOfferedContract(), proposal.getRequestedContract());
    }

	@Test
	void testRejectProposal() {
        FantaUser user = new FantaUser("user@test.com", "pwd");
        FantaUser user1 = new FantaUser("mail", "psw");
        League league = new League(user, "Test League", "L005");
        HashSet<Contract> contracts = new HashSet<>();
        HashSet<Contract> contracts1 = new HashSet<>();
        FantaTeam team = new FantaTeam("FantaTeam", league, 0, user, contracts);
        FantaTeam team1 = new FantaTeam("FantaTeam1", league, 0, user1, contracts1);
        Player p1 = new Player.Goalkeeper("Alessandro", "Buongiorno", Player.Club.NAPOLI);
        Player p2 = new Player.Goalkeeper("Nico", "Paz", Player.Club.COMO);
        Contract offeredContract = new Contract(team, p1);
        Contract requestedContract = new Contract(team1, p2);
        Proposal proposal = new Proposal(offeredContract, requestedContract);
        contracts.add(offeredContract);
        contracts1.add(requestedContract);
        when(proposalRepository.getProposalBy(proposal.getOfferedContract(), proposal.getRequestedContract())).thenReturn(Optional.of(proposal));

        // Call the method under test
		userService.rejectProposal(proposal, team1);

		// Verify repository interactions
		verify(context.getProposalRepository()).deleteProposal(proposal);
	}

	@Test
	void testRejectProposal_RequestedNotInvolved() {
        FantaUser user = new FantaUser("user@test.com", "pwd");
        FantaUser user1 = new FantaUser("mail", "psw");
        FantaUser user2 = new FantaUser("mail2", "psw");
        League league = new League(user, "Test League", "L005");
        HashSet<Contract> contracts = new HashSet<>();
        HashSet<Contract> contracts1 = new HashSet<>();
        FantaTeam team = new FantaTeam("FantaTeam", league, 0, user, contracts);
        FantaTeam team1 = new FantaTeam("FantaTeam1", league, 0, user1, contracts1);
        FantaTeam team2 = new FantaTeam("team2", league, 0, user2, null);
        Player p1 = new Player.Goalkeeper("Alessandro", "Buongiorno", Player.Club.NAPOLI);
        Player p2 = new Player.Goalkeeper("Nico", "Paz", Player.Club.COMO);
        Contract offeredContract = new Contract(team, p1);
        Contract requestedContract = new Contract(team1, p2);
        Proposal proposal = new Proposal(offeredContract, requestedContract);
        contracts.add(offeredContract);
        contracts1.add(requestedContract);
        when(proposalRepository.getProposalBy(proposal.getOfferedContract(), proposal.getRequestedContract())).thenReturn(Optional.of(proposal));

        assertThatThrownBy(() -> userService.rejectProposal(proposal, team2))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("You are not involved in this proposal");
	}

    @Test
    void testRejectProposal_ProposalNotExists() {
        FantaUser user = new FantaUser("user@test.com", "pwd");
        FantaUser user1 = new FantaUser("mail", "psw");
        League league = new League(user, "Test League", "L005");
        HashSet<Contract> contracts = new HashSet<>();
        HashSet<Contract> contracts1 = new HashSet<>();
        FantaTeam team = new FantaTeam("FantaTeam", league, 0, user, contracts);
        FantaTeam team1 = new FantaTeam("FantaTeam1", league, 0, user1, contracts1);
        Player p1 = new Player.Goalkeeper("Alessandro", "Buongiorno", Player.Club.NAPOLI);
        Player p2 = new Player.Goalkeeper("Nico", "Paz", Player.Club.COMO);
        Contract offeredContract = new Contract(team, p1);
        Contract requestedContract = new Contract(team1, p2);
        Proposal proposal = new Proposal(offeredContract, requestedContract);
        contracts.add(offeredContract);
        contracts1.add(requestedContract);
        when(proposalRepository.getProposalBy(proposal.getOfferedContract(), proposal.getRequestedContract())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.rejectProposal(proposal, team1))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Proposal does not exists");

        verify(proposalRepository).getProposalBy(proposal.getOfferedContract(), proposal.getRequestedContract());
    }

	@Test
	void testCreateProposal_DifferentRoles() {
        FantaUser user = new FantaUser("user@test.com", "pwd");
        FantaUser user1 = new FantaUser("mail", "psw");
        League league = new League(user, "Test League", "L005");
        HashSet<Contract> contracts = new HashSet<>();
        HashSet<Contract> contracts1 = new HashSet<>();
        FantaTeam team = new FantaTeam("FantaTeam", league, 0, user, contracts);
        FantaTeam team1 = new FantaTeam("FantaTeam1", league, 0, user1, contracts1);
        Player p1 = new Player.Defender("Alessandro", "Buongiorno", Player.Club.NAPOLI);
        Player p2 = new Player.Midfielder("Nico", "Paz", Player.Club.COMO);
        Contract offeredContract = new Contract(team, p1);
        Contract requestedContract = new Contract(team1, p2);
        contracts.add(offeredContract);
        contracts1.add(requestedContract);
		assertThatThrownBy(() -> userService.createProposal(p2, p1, team, team1))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("The players don't have the same role");
        verifyNoMoreInteractions(proposalRepository);
	}

	@Test
	void testCreateProposal_EveryContractMissing() {
        FantaUser user = new FantaUser("user@test.com", "pwd");
        FantaUser user1 = new FantaUser("mail", "psw");
        League league = new League(user, "Test League", "L005");
        HashSet<Contract> contracts = new HashSet<>();
        HashSet<Contract> contracts1 = new HashSet<>();
        FantaTeam team = new FantaTeam("FantaTeam", league, 0, user, contracts);
        FantaTeam team1 = new FantaTeam("FantaTeam1", league, 0, user1, contracts1);
        Player p1 = new Player.Midfielder("Christian", "Pulisic", Player.Club.MILAN);
        Player p2 = new Player.Midfielder("Nico", "Paz", Player.Club.COMO);
        when(teamRepository.getFantaTeamByUserAndLeague(league, user)).thenReturn(Optional.of(team));
        when(teamRepository.getFantaTeamByUserAndLeague(league, user1)).thenReturn(Optional.of(team1));

        boolean proposal = userService.createProposal(p2, p1, team, team1);
        assertThat(proposal).isFalse();
        verifyNoMoreInteractions(proposalRepository);
	}

	@Test
	void testCreateProposal_RequestedContractMissing() {
        FantaUser user = new FantaUser("user@test.com", "pwd");
        FantaUser user1 = new FantaUser("mail", "psw");
        League league = new League(user, "Test League", "L005");
        HashSet<Contract> contracts = new HashSet<>();
        HashSet<Contract> contracts1 = new HashSet<>();
        FantaTeam team = new FantaTeam("FantaTeam", league, 0, user, contracts);
        FantaTeam team1 = new FantaTeam("FantaTeam1", league, 0, user1, contracts1);
        Player p1 = new Player.Midfielder("Christian", "Pulisic", Player.Club.MILAN);
        Player p2 = new Player.Midfielder("Nico", "Paz", Player.Club.COMO);
        Contract offeredContract = new Contract(team, p1);
        contracts.add(offeredContract);
        when(teamRepository.getFantaTeamByUserAndLeague(league, user)).thenReturn(Optional.of(team));
        when(teamRepository.getFantaTeamByUserAndLeague(league, user1)).thenReturn(Optional.of(team1));



		assertThat(userService.createProposal(p2, p1, team, team1)).isFalse();
        verifyNoMoreInteractions(proposalRepository);
	}

	@Test
	void testCreateProposal_OfferedContractMissing() {
        FantaUser user = new FantaUser("user@test.com", "pwd");
        FantaUser user1 = new FantaUser("mail", "psw");
        League league = new League(user, "Test League", "L005");
        HashSet<Contract> contracts = new HashSet<>();
        HashSet<Contract> contracts1 = new HashSet<>();
        FantaTeam team = new FantaTeam("FantaTeam", league, 0, user, contracts);
        FantaTeam team1 = new FantaTeam("FantaTeam1", league, 0, user1, contracts1);
        Player p1 = new Player.Midfielder("Christian", "Pulisic", Player.Club.MILAN);
        Player p2 = new Player.Midfielder("Nico", "Paz", Player.Club.COMO);
        Contract requestedContract = new Contract(team1, p2);
        contracts1.add(requestedContract);
        when(teamRepository.getFantaTeamByUserAndLeague(league, user)).thenReturn(Optional.of(team));
        when(teamRepository.getFantaTeamByUserAndLeague(league, user1)).thenReturn(Optional.of(team1));


        assertThat(userService.createProposal(p2, p1, team, team1)).isFalse();
        verifyNoMoreInteractions(proposalRepository);
	}

	@Test
	void testCreateProposal_AlreadyExists() {
        FantaUser user = new FantaUser("user@test.com", "pwd");
        FantaUser user1 = new FantaUser("mail", "psw");
        League league = new League(user, "Test League", "L005");
        HashSet<Contract> contracts = new HashSet<>();
        HashSet<Contract> contracts1 = new HashSet<>();
        FantaTeam team = new FantaTeam("FantaTeam", league, 0, user, contracts);
        FantaTeam team1 = new FantaTeam("FantaTeam1", league, 0, user1, contracts1);
        Player p1 = new Player.Midfielder("Christian", "Pulisic", Player.Club.MILAN);
        Player p2 = new Player.Midfielder("Nico", "Paz", Player.Club.COMO);
        Contract requestedContract = new Contract(team1, p2);
        Contract offeredContract = new Contract(team, p1);
        contracts.add(offeredContract);
        contracts1.add(requestedContract);
        when(teamRepository.getFantaTeamByUserAndLeague(league, user)).thenReturn(Optional.of(team));
        when(teamRepository.getFantaTeamByUserAndLeague(league, user1)).thenReturn(Optional.of(team1));

		when(context.getProposalRepository().getProposalBy(offeredContract, requestedContract))
				.thenReturn(Optional.of(new Proposal(offeredContract,requestedContract)));

		assertThatThrownBy(() -> userService.createProposal(p2, p1, team, team1))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("The proposal already exists");
        verify(proposalRepository).getProposalBy(offeredContract, requestedContract);
	}

	@Test
	void testCreateProposal_HappyPath() {
        FantaUser user = new FantaUser("user@test.com", "pwd");
        FantaUser user1 = new FantaUser("mail", "psw");
        League league = new League(user, "Test League", "L005");
        HashSet<Contract> contracts = new HashSet<>();
        HashSet<Contract> contracts1 = new HashSet<>();
        FantaTeam team = new FantaTeam("FantaTeam", league, 0, user, contracts);
        FantaTeam team1 = new FantaTeam("FantaTeam1", league, 0, user1, contracts1);
        Player p1 = new Player.Midfielder("Christian", "Pulisic", Player.Club.MILAN);
        Player p2 = new Player.Midfielder("Nico", "Paz", Player.Club.COMO);
        Contract requestedContract = new Contract(team1, p2);
        Contract offeredContract = new Contract(team, p1);
        contracts.add(offeredContract);
        contracts1.add(requestedContract);
        when(teamRepository.getFantaTeamByUserAndLeague(league, user)).thenReturn(Optional.of(team));
        when(teamRepository.getFantaTeamByUserAndLeague(league, user1)).thenReturn(Optional.of(team1));
        when(context.getProposalRepository().getProposalBy(offeredContract, requestedContract))
				.thenReturn(Optional.empty());

		assertThat(userService.createProposal(p2, p1, team, team1)).isTrue();
	}

    @Test
    void testCreateProposal_SameTeams() {
        FantaUser user = new FantaUser("user@test.com", "pwd");
        League league = new League(user, "Test League", "L005");
        HashSet<Contract> contracts = new HashSet<>();
        FantaTeam team = new FantaTeam("FantaTeam", league, 0, user, contracts);
        Player p1 = new Player.Midfielder("Christian", "Pulisic", Player.Club.MILAN);
        Player p2 = new Player.Midfielder("Nico", "Paz", Player.Club.COMO);

        assertThatThrownBy(() -> userService.createProposal(p2, p1, team, team))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("You can't exchange a player with yourself");
        verifyNoMoreInteractions(proposalRepository);
    }

    @Test
    void testCreateProposal_MyTeamNotExisting() {
        FantaUser user = new FantaUser("user@test.com", "pwd");
        FantaUser user1 = new FantaUser("mail", "psw");
        League league = new League(user, "Test League", "L005");
        HashSet<Contract> contracts = new HashSet<>();
        HashSet<Contract> contracts1 = new HashSet<>();
        FantaTeam team = new FantaTeam("FantaTeam", league, 0, user, contracts);
        FantaTeam team1 = new FantaTeam("FantaTeam1", league, 0, user1, contracts1);
        Player p1 = new Player.Midfielder("Christian", "Pulisic", Player.Club.MILAN);
        Player p2 = new Player.Midfielder("Nico", "Paz", Player.Club.COMO);
        Contract requestedContract = new Contract(team1, p2);
        Contract offeredContract = new Contract(team, p1);
        contracts.add(offeredContract);
        contracts1.add(requestedContract);
        when(teamRepository.getFantaTeamByUserAndLeague(league, user)).thenReturn(Optional.empty());
        when(teamRepository.getFantaTeamByUserAndLeague(league, user1)).thenReturn(Optional.of(team1));
        when(context.getProposalRepository().getProposalBy(offeredContract, requestedContract))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.createProposal(p2, p1, team, team1))
                .isInstanceOf(RuntimeException.class).hasMessageContaining("One or both teams do not exists in the league");
        verifyNoMoreInteractions(proposalRepository);
    }

    @Test
    void testCreateProposal_OpponentTeamNotExisting() {
        FantaUser user = new FantaUser("user@test.com", "pwd");
        FantaUser user1 = new FantaUser("mail", "psw");
        League league = new League(user, "Test League", "L005");
        HashSet<Contract> contracts = new HashSet<>();
        HashSet<Contract> contracts1 = new HashSet<>();
        FantaTeam team = new FantaTeam("FantaTeam", league, 0, user, contracts);
        FantaTeam team1 = new FantaTeam("FantaTeam1", league, 0, user1, contracts1);
        Player p1 = new Player.Midfielder("Christian", "Pulisic", Player.Club.MILAN);
        Player p2 = new Player.Midfielder("Nico", "Paz", Player.Club.COMO);
        Contract requestedContract = new Contract(team1, p2);
        Contract offeredContract = new Contract(team, p1);
        contracts.add(offeredContract);
        contracts1.add(requestedContract);
        when(teamRepository.getFantaTeamByUserAndLeague(league, user)).thenReturn(Optional.of(team));
        when(teamRepository.getFantaTeamByUserAndLeague(league, user1)).thenReturn(Optional.empty());
        when(context.getProposalRepository().getProposalBy(offeredContract, requestedContract))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.createProposal(p2, p1, team, team1))
                .isInstanceOf(RuntimeException.class).hasMessageContaining("One or both teams do not exists in the league");
        verifyNoMoreInteractions(proposalRepository);
    }

    @Test
    void testCreateProposal_BothTeamNotExisting() {
        FantaUser user = new FantaUser("user@test.com", "pwd");
        FantaUser user1 = new FantaUser("mail", "psw");
        League league = new League(user, "Test League", "L005");
        HashSet<Contract> contracts = new HashSet<>();
        HashSet<Contract> contracts1 = new HashSet<>();
        FantaTeam team = new FantaTeam("FantaTeam", league, 0, user, contracts);
        FantaTeam team1 = new FantaTeam("FantaTeam1", league, 0, user1, contracts1);
        Player p1 = new Player.Midfielder("Christian", "Pulisic", Player.Club.MILAN);
        Player p2 = new Player.Midfielder("Nico", "Paz", Player.Club.COMO);
        Contract requestedContract = new Contract(team1, p2);
        Contract offeredContract = new Contract(team, p1);
        contracts.add(offeredContract);
        contracts1.add(requestedContract);
        when(teamRepository.getFantaTeamByUserAndLeague(league, user)).thenReturn(Optional.empty());
        when(teamRepository.getFantaTeamByUserAndLeague(league, user1)).thenReturn(Optional.empty());
        when(context.getProposalRepository().getProposalBy(offeredContract, requestedContract))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.createProposal(p2, p1, team, team1))
                .isInstanceOf(RuntimeException.class).hasMessageContaining("One or both teams do not exists in the league");
        verifyNoMoreInteractions(proposalRepository);
    }

    @Test
    void testCreateProposal_MyTeamIncorrect() {
        FantaUser user = new FantaUser("user@test.com", "pwd");
        FantaUser user1 = new FantaUser("mail", "psw");
        FantaUser user2 = new FantaUser("mail2", "psw");
        League league = new League(user, "Test League", "L005");
        HashSet<Contract> contracts = new HashSet<>();
        HashSet<Contract> contracts1 = new HashSet<>();
        FantaTeam team = new FantaTeam("FantaTeam", league, 0, user, contracts);
        FantaTeam team1 = new FantaTeam("FantaTeam1", league, 0, user1, contracts1);
        FantaTeam team2 = new FantaTeam("FantaTeam2", league, 0, user2, null);
        Player p1 = new Player.Midfielder("Christian", "Pulisic", Player.Club.MILAN);
        Player p2 = new Player.Midfielder("Nico", "Paz", Player.Club.COMO);
        Contract requestedContract = new Contract(team1, p2);
        Contract offeredContract = new Contract(team, p1);
        contracts.add(offeredContract);
        contracts1.add(requestedContract);
        when(teamRepository.getFantaTeamByUserAndLeague(league, user)).thenReturn(Optional.of(team2));
        when(teamRepository.getFantaTeamByUserAndLeague(league, user1)).thenReturn(Optional.of(team1));
        when(context.getProposalRepository().getProposalBy(offeredContract, requestedContract))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.createProposal(p2, p1, team, team1))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("One or both teams are incorrect");
        verifyNoMoreInteractions(proposalRepository);
    }

    @Test
    void testCreateProposal_OpponentTeamIncorrect() {
        FantaUser user = new FantaUser("user@test.com", "pwd");
        FantaUser user1 = new FantaUser("mail", "psw");
        FantaUser user2 = new FantaUser("mail2", "psw");
        League league = new League(user, "Test League", "L005");
        HashSet<Contract> contracts = new HashSet<>();
        HashSet<Contract> contracts1 = new HashSet<>();
        FantaTeam team = new FantaTeam("FantaTeam", league, 0, user, contracts);
        FantaTeam team1 = new FantaTeam("FantaTeam1", league, 0, user1, contracts1);
        FantaTeam team2 = new FantaTeam("FantaTeam2", league, 0, user2, null);
        Player p1 = new Player.Midfielder("Christian", "Pulisic", Player.Club.MILAN);
        Player p2 = new Player.Midfielder("Nico", "Paz", Player.Club.COMO);
        Contract requestedContract = new Contract(team1, p2);
        Contract offeredContract = new Contract(team, p1);
        contracts.add(offeredContract);
        contracts1.add(requestedContract);
        when(teamRepository.getFantaTeamByUserAndLeague(league, user)).thenReturn(Optional.of(team));
        when(teamRepository.getFantaTeamByUserAndLeague(league, user1)).thenReturn(Optional.of(team2));
        when(context.getProposalRepository().getProposalBy(offeredContract, requestedContract))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.createProposal(p2, p1, team, team1))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("One or both teams are incorrect");
        verifyNoMoreInteractions(proposalRepository);
    }

    @Test
    void testCreateProposal_BothTeamIncorrect() {
        FantaUser user = new FantaUser("user@test.com", "pwd");
        FantaUser user1 = new FantaUser("mail", "psw");
        FantaUser user2 = new FantaUser("mail2", "psw");
        League league = new League(user, "Test League", "L005");
        HashSet<Contract> contracts = new HashSet<>();
        HashSet<Contract> contracts1 = new HashSet<>();
        FantaTeam team = new FantaTeam("FantaTeam", league, 0, user, contracts);
        FantaTeam team1 = new FantaTeam("FantaTeam1", league, 0, user1, contracts1);
        FantaTeam team2 = new FantaTeam("FantaTeam2", league, 0, user2, null);
        Player p1 = new Player.Midfielder("Christian", "Pulisic", Player.Club.MILAN);
        Player p2 = new Player.Midfielder("Nico", "Paz", Player.Club.COMO);
        Contract requestedContract = new Contract(team1, p2);
        Contract offeredContract = new Contract(team, p1);
        contracts.add(offeredContract);
        contracts1.add(requestedContract);
        when(teamRepository.getFantaTeamByUserAndLeague(league, user)).thenReturn(Optional.of(team2));
        when(teamRepository.getFantaTeamByUserAndLeague(league, user1)).thenReturn(Optional.of(team2));
        when(context.getProposalRepository().getProposalBy(offeredContract, requestedContract))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.createProposal(p2, p1, team, team1))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("One or both teams are incorrect");
        verifyNoMoreInteractions(proposalRepository);
    }



	@Test
	void testGetAllMatchGrades() {
        FantaUser user = new FantaUser("user@test.com", "pwd");
        FantaUser user1 = new FantaUser("mail", "psw");
        League league = new League(user, "Test League", "L003");
        MatchDay matchDay = new MatchDay("MD1",1, MatchDay.Status.FUTURE, league);
        FantaTeam team = new FantaTeam("FantaTeam", league, 0, user, null);
        FantaTeam team1 = new FantaTeam("FantaTeam1", league, 0, user1, null);
		Match match = new Match(matchDay, team, team1);
        Player p1 = new Player.Midfielder("Christian", "Pulisic", Player.Club.MILAN);
		Grade grade = new Grade(p1, matchDay, 10);

		when(context.getGradeRepository().getAllGrades(matchDay)).thenReturn(List.of(grade));
		List<Grade> result = userService.getAllMatchGrades(match);
		assertThat(result).containsExactly(grade);
        verify(gradeRepository).getAllGrades(matchDay);
	}

	@Test
	void testGetAllFantaTeams() {
        FantaUser admin = new FantaUser("admin@mail", "psw");
		League league = new League(admin, "My League", "L999");
		FantaTeam t1 = new FantaTeam("Team 1", league, 0, new FantaUser("u1", "pwd"), null);
		FantaTeam t2 = new FantaTeam("Team 2", league, 0, new FantaUser("u2", "pwd"), null);

		when(context.getTeamRepository().getAllTeams(league)).thenReturn(Set.of(t1, t2));

		Set<FantaTeam> result = userService.getAllFantaTeams(league);

		assertThat(result).containsExactlyInAnyOrder(t1, t2);
		verify(teamRepository).getAllTeams(league);
	}

}
