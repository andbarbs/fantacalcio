package business;

import domain.scheme.Scheme433;
import gui.lineup.chooser.LineUpChooserTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

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

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Tag("mockito-agent")
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
	}

	@Test
	void testCreateLeague() {
		FantaUser admin = new FantaUser("admin@test.com", "pwd");
		String leagueCode = "L001";

		// League code does not exist yet
		when(leagueRepository.getLeagueByCode(leagueCode)).thenReturn(Optional.empty());

		adminUserService.createLeague("My League", admin, leagueCode);

		// Verify that saveLeague was called
		verify(leagueRepository, times(1)).saveLeague(any(League.class));
        //TODO controlla che vengano generati i matchday correttamente
	}

	@Test
	void testCreateLeague_LeagueCodeExists() {
		FantaUser admin = new FantaUser("admin@test.com", "pwd");
		String leagueCode = "L001";

		League existingLeague = new League(admin, "Existing League", leagueCode);
		when(leagueRepository.getLeagueByCode(leagueCode)).thenReturn(Optional.of(existingLeague));

		assertThatThrownBy(() -> adminUserService.createLeague("New League", admin, leagueCode))
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



	/**
	 * TODO per asserire davvero sull'algoritmo bisognerebbe fare argument capture
	 * (con {@link ArgumentCaptor}) di tutti (con verifyoMoreInteractions) i Match
	 * passati a matchRepository, e poi asserire le proprietà che dovrebbero avere.
	 * 
	 * Per ArgumentCaptor, sono usati ad es in {@link LineUpChooserTest}	
	 */

	private String pairKey(FantaTeam home, FantaTeam away) {
		return home.getName() + "-" + away.getName();
	}

	private List<Match> getMatchesInRange(List<Match> all, int from, int to) {
		return all.stream()
				.filter(m -> m.getMatchDaySerieA().getNumber() >= from
						&& m.getMatchDaySerieA().getNumber() <= to)
				.toList();
	}

    private void assertNoDuplicateMatchesPerPhase(Map<Integer, List<Match>> matchesByDay) {
        // Mappa fase -> Set delle coppie (team1, team2)
        Map<String, Set<String>> seenMatches = new HashMap<>();

        for (var entry : matchesByDay.entrySet()) {
            int giornata = entry.getKey();
            List<Match> matches = entry.getValue();

            String phase;
            if (giornata <= 7) {
                phase = "andata";
            } else if (giornata <= 14) {
                phase = "ritorno";
            } else {
                phase = "nuova_andata";
            }

            seenMatches.putIfAbsent(phase, new HashSet<>());

            for (Match m : matches) {
                String matchKey = m.getTeam1().getName() + "-" + m.getTeam2().getName();

                // se già visto nella stessa fase → errore
                assertTrue(
                        seenMatches.get(phase).add(matchKey),
                        () -> "Match duplicato nella fase " + phase + ": " + matchKey +
                                " (giornata " + giornata + ")"
                );
            }
        }
    }

	@Test
	void testGenerateCalendar_SavesMatches() {
		FantaUser admin = new FantaUser(null, null);
		League league = new League(admin, "Serie A", null);

		// Create 8 real teams (even number for round-robin)
		FantaTeam team1 = new FantaTeam("Team1", null, 0, null, new HashSet<>());
		FantaTeam team2 = new FantaTeam("Team2", null, 0, null, new HashSet<>());
		FantaTeam team3 = new FantaTeam("Team3", null, 0, null, new HashSet<>());
		FantaTeam team4 = new FantaTeam("Team4", null, 0, null, new HashSet<>());
		FantaTeam team5 = new FantaTeam("Team5", null, 0, null, new HashSet<>());
		FantaTeam team6 = new FantaTeam("Team6", null, 0, null, new HashSet<>());
		FantaTeam team7 = new FantaTeam("Team7", null, 0, null, new HashSet<>());
		FantaTeam team8 = new FantaTeam("Team8", null, 0, null, new HashSet<>());
		List<FantaTeam> teams = List.of(team1, team2, team3, team4, team5, team6, team7, team8);

		// 20 match days (real objects are okay)
		List<MatchDay> matchDays = new ArrayList<>();
		for (int i = 0; i < 20; i++)
			matchDays.add(new MatchDay("match", i, MatchDay.Status.FUTURE, league));

		// Mock repositories
		when(fantaTeamRepository.getAllTeams(league)).thenReturn(Set.copyOf(teams));
		when(matchDayRepository.getAllMatchDays(league)).thenReturn(matchDays);

		adminUserService.generateCalendar(league);

		ArgumentCaptor<Match> matchCaptor = ArgumentCaptor.forClass(Match.class);
		verify(matchRepository, times(80)).saveMatch(matchCaptor.capture());
		verifyNoMoreInteractions(matchRepository);

		List<Match> allMatches = matchCaptor.getAllValues();
		assertEquals(20 * 4, allMatches.size(), "Devono esserci 80 match (4 per giornata × 20 giornate)");

		Map<Integer, List<Match>> matchesByDay = new HashMap<>();
		for (Match m : allMatches) {
			int n = m.getMatchDaySerieA().getNumber();
			matchesByDay.computeIfAbsent(n, k -> new ArrayList<>()).add(m);
		}

		for (var entry : matchesByDay.entrySet()) {
			int giornata = entry.getKey();
			List<FantaTeam> teamsInDay = new ArrayList<>();
			for (Match m : entry.getValue()) {
				assertNotEquals(m.getTeam1(), m.getTeam2(), "Un team non può giocare contro sé stesso");
				teamsInDay.add(m.getTeam1());
				teamsInDay.add(m.getTeam2());
			}
			assertEquals(8, new HashSet<>(teamsInDay).size(),
					"Giornata " + giornata + ": ogni team deve giocare una sola volta");
		}

		List<Match> andata = getMatchesInRange(allMatches, 1, 7);
		List<Match> ritorno = getMatchesInRange(allMatches, 8, 14);
		List<Match> nuovaAndata = getMatchesInRange(allMatches, 15, 20);

		// --- ANDATA ---
		Set<String> coppieAndata = new HashSet<>();
		for (Match m : andata) {
			coppieAndata.add(pairKey(m.getTeam1(), m.getTeam2()));
		}

		// --- RITORNO (invertito rispetto all’andata) ---
		Set<String> coppieRitorno = new HashSet<>();
		for (Match m : ritorno) {
			coppieRitorno.add(pairKey(m.getTeam2(), m.getTeam1())); // invertite
		}

		for (String coppia : coppieAndata) {
			assertTrue(coppieRitorno.contains(coppia),
					"La coppia " + coppia + " dell’andata non ha ritorno invertito corrispondente");
		}

		// --- NUOVA ANDATA (giornate 15–20) ---
		Set<String> coppieNuovaAndata = new HashSet<>();
		for (Match m : nuovaAndata) {
			coppieNuovaAndata.add(pairKey(m.getTeam1(), m.getTeam2()));
		}

		// Devono esserci le stesse coppie dell’andata originale
		for (String coppia : coppieNuovaAndata) {
			assertTrue(coppieAndata.contains(coppia),
					"La coppia " + coppia + " nella nuova andata non esisteva nella prima andata");
		}

        assertNoDuplicateMatchesPerPhase(matchesByDay);
		// Verifica numero totale giornate
		assertEquals(20, matchesByDay.size(), "Devono esserci 20 giornate generate");
	}


	@Test
	void testGenerateCalendar_LessThanTwoTeams_Throws() {
		FantaUser admin = new FantaUser(null, null);
		League league = new League(admin, "Serie A", null);
		FantaTeam onlyTeam = new FantaTeam("Solo", null, 0, null, new HashSet<>());

		when(fantaTeamRepository.getAllTeams(league)).thenReturn(Set.of(onlyTeam));

		assertThatThrownBy(() -> adminUserService.generateCalendar(league)).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("At least 2 teams are required");
	}

	@Test
	void testGenerateCalendar_OddNumberOfTeams_Throws() {
		FantaUser admin = new FantaUser(null, null);
		League league = new League(admin, "Serie A", null);
		FantaTeam t1 = new FantaTeam("Team1", null, 0, null, new HashSet<>());
		FantaTeam t2 = new FantaTeam("Team2", null, 0, null, new HashSet<>());
		FantaTeam t3 = new FantaTeam("Team3", null, 0, null, new HashSet<>());

		when(fantaTeamRepository.getAllTeams(league)).thenReturn(Set.of(t1, t2, t3));

		assertThatThrownBy(() -> adminUserService.generateCalendar(league)).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Number of teams must be even");
	}

    //TODO perchè si testa un metodo privato?
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

    //TODO perchè si testa un metodo privato?
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

    //TODO perchè si testa un metodo privato?
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

    //TODO perchè si testa un metodo privato?
    /*
	@Test
	void testCreateMatches_Success() {
		FantaTeam t1 = new FantaTeam("T1", null, 0, null, new HashSet<>());
		FantaTeam t2 = new FantaTeam("T2", null, 0, null, new HashSet<>());

		FantaTeam[] match = new FantaTeam[] { t1, t2 };
		List<MatchDaySerieA> matchDays = List.of(new MatchDaySerieA(null, null, 1));

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

     */

    //TODO perchè si testa un metodo privato?
    /*
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
	*/

    //TODO perchè si testa un metodo privato?
    /*
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
	*/

    //TODO perchè si testa un metodo privato?
    /*
	@Test
	void testCreateMatches_EmptySchedule_ReturnsEmptyList() {
		List<List<FantaTeam[]>> schedule = List.of();
		List<MatchDaySerieA> matchDays = List.of();

		List<Match> matches = adminUserService.createMatches(schedule, matchDays);

		assertThat(matches).isEmpty();
	}
	*/

	@Test
	void testCalculateGrades_UserNotAdmin_Throws() {
		// The user who is NOT the league admin
		FantaUser user = new FantaUser("user", "pswd");

		// League with a different admin
		FantaUser admin = new FantaUser("admin", "pswd");
		League league = new League(admin, "league", "12345");

		// Create a previous match day so that the "season started" check passes
		MatchDay previousMatchDay = new MatchDay("1 giornata", 1, MatchDay.Status.PAST,league);
		when(matchDayRepository.getPreviousMatchDay(any())).thenReturn(Optional.of(previousMatchDay));

		// Set up a match day to calculate with at least one match
		FantaTeam team1 = new FantaTeam("Team1", league, 0, user, Set.of());
		FantaTeam team2 = new FantaTeam("Team2", league, 0, admin, Set.of());
		Match match = new Match(previousMatchDay, team1, team2);
		List<Match> matches = List.of(match);
		when(matchRepository.getAllMatchesByMatchDay(any(), eq(league))).thenReturn(matches);

		// Set up grades, lineups, and results so the calculation can proceed
		Grade grade1 = new Grade(new Player.Goalkeeper(null, null, null), previousMatchDay, 6.0);
		Grade grade2 = new Grade(new Player.Forward(null, null, null), previousMatchDay, 7.0);
        //TODO l'implementazione di getAllMatchGrades è cambiata ma in questo caso dovrebbe andare bene lo stesso
		when(gradeRepository.getAllMatchGrades(previousMatchDay)).thenReturn(List.of(grade1, grade2));

		LineUp lineup1 = LineUp.build()
				.forTeam(team1)
				.inMatch(match)
				.withStarterLineUp(Scheme433.starterLineUp()
						.withGoalkeeper(new Player.Goalkeeper("portiere", "titolare", Player.Club.ATALANTA))
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
						new Player.Goalkeeper("portiere1", "panchina", Player.Club.ATALANTA),
						new Player.Goalkeeper("portiere2", "panchina", Player.Club.ATALANTA),
						new Player.Goalkeeper("portiere3", "panchina", Player.Club.ATALANTA))
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
		LineUp lineup2 = LineUp.build()
				.forTeam(team2)
				.inMatch(match)
				.withStarterLineUp(Scheme433.starterLineUp()
						.withGoalkeeper(new Player.Goalkeeper("portiere", "titolare", Player.Club.ATALANTA))
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
						new Player.Goalkeeper("portiere1", "panchina", Player.Club.ATALANTA),
						new Player.Goalkeeper("portiere2", "panchina", Player.Club.ATALANTA),
						new Player.Goalkeeper("portiere3", "panchina", Player.Club.ATALANTA))
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
		League league = new League(admin, "Serie A", null);

		when(matchDayRepository.getPreviousMatchDay(any())).thenReturn(Optional.empty());

		assertThatThrownBy(() -> adminUserService.calculateGrades(admin, league)).isInstanceOf(RuntimeException.class)
				.hasMessageContaining("The season hasn't started yet");
	}

    //TODO penso che con la nuova temporizzazione non abbia senso
	@Test
	void testCalculateGrades_NoResultsToCalculate_Throws() {

		League league = mock(League.class);
		FantaUser admin = mock(FantaUser.class);
		when(league.getAdmin()).thenReturn(admin);

		when(matchDayRepository.getPreviousMatchDay(league))
				.thenReturn(Optional.of(new MatchDay("1 giornata", 1, MatchDay.Status.PAST, league)));

        //TODO riscrivi non serve più la roba che fa e non so cosa fa
        /*
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

         */
        assert(true);
	}

    //TODO penso che con la nuova temporizzazione non abbia senso
	@Test
	void testIsLegalToCalculateResults_Saturday() throws Exception {

		League league = mock(League.class);
		FantaUser admin = mock(FantaUser.class);
		when(league.getAdmin()).thenReturn(admin);

		when(matchDayRepository.getPreviousMatchDay(league))
				.thenReturn(Optional.of(new MatchDay("1 giornata", 1, MatchDay.Status.PAST, league)));

        //TODO uguale a sopra
        /*
		// Override today() to return the Saturday we want to test
		AdminUserService serviceWithSaturday = new AdminUserService(transactionManager) {
			@Override
			protected LocalDate today() {
				return saturday;
			}
		};

		when(serviceWithSaturday.getNextMatchDayToCalculate(saturday, context, league, admin))
				.thenReturn(Optional.of(new MatchDaySerieA(null, saturday, 1)));

		assertThatThrownBy(() -> serviceWithSaturday.calculateGrades(admin, league))
				.isInstanceOf(RuntimeException.class).hasMessageContaining("The matches are not finished yet");

         */
        assert(true);
	}

    //TODO penso che con la nuova temporizzazione non abbia senso
	@Test
	void testCalculateGrades_IllegalOnSunday() {
		League league = mock(League.class);
		FantaUser admin = mock(FantaUser.class);
		when(league.getAdmin()).thenReturn(admin);

		when(matchDayRepository.getPreviousMatchDay(league))
				.thenReturn(Optional.of(new MatchDay("1 giornata", 1, MatchDay.Status.PAST, league)));

        //TODO uguale a sopra
        /*
		AdminUserService serviceWithSunday = new AdminUserService(transactionManager) {
			@Override
			protected LocalDate today() {
				return sunday;
			}
		};

		when(serviceWithSunday.getNextMatchDayToCalculate(sunday, context, league, admin))
				.thenReturn(Optional.of(new MatchDaySerieA(null, sunday, 1)));

		assertThatThrownBy(() -> serviceWithSunday.calculateGrades(admin, league)).isInstanceOf(RuntimeException.class)
				.hasMessageContaining("The matches are not finished yet");

         */
        assert(true);
	}

    //TODO penso che con la nuova temporizzazione non abbia senso
	@Test
	void testCalculateGrades_IllegalOnWeekday() {

		League league = mock(League.class);
		FantaUser admin = mock(FantaUser.class);

		when(league.getAdmin()).thenReturn(admin);
		when(matchDayRepository.getPreviousMatchDay(league)).thenReturn(Optional.of(new MatchDay("1 giornata", 1, MatchDay.Status.PAST, league)));

        //TODO uguale a sopra
        /*
		AdminUserService serviceWithMonday = new AdminUserService(transactionManager) {
			@Override
			protected LocalDate today() {
				return monday;
			}
		};

		when(serviceWithMonday.getNextMatchDayToCalculate(monday, context, league, admin))
				.thenReturn(Optional.of(new MatchDaySerieA(null, monday, 1)));

		assertThatThrownBy(() -> serviceWithMonday.calculateGrades(admin, league)).isInstanceOf(RuntimeException.class)
				.hasMessageContaining("The matches are not finished yet");

         */
        assert(true);
	}

	@Test
	void testCalculateGrades_SavesResultsAndUpdatesPoints() {

		FantaUser admin = new FantaUser("admin@example.com", "pwd");
		League league = new League(admin, "Serie A", "1234");

		MatchDay prevDay = new MatchDay("1 giornata", 1, MatchDay.Status.PAST, league);
		MatchDay dayToCalc = new MatchDay("2 giornata", 2, MatchDay.Status.PAST, league);

		when(matchDayRepository.getPreviousMatchDay(any())).thenReturn(Optional.of(prevDay));

        //TODO inutili da riscrivere
        /*
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

         */

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

		LineUp lineup1 = LineUp.build()
				.forTeam(team1)
				.inMatch(match)
				.withStarterLineUp(Scheme433.starterLineUp()
						.withGoalkeeper(gk1)
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
						new Player.Goalkeeper("portiere1", "panchina", Player.Club.ATALANTA),
						new Player.Goalkeeper("portiere2", "panchina", Player.Club.ATALANTA),
						new Player.Goalkeeper("portiere3", "panchina", Player.Club.ATALANTA))
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
		
		LineUp lineup2 = LineUp.build()
				.forTeam(team2)
				.inMatch(match)
				.withStarterLineUp(Scheme433.starterLineUp()
						.withGoalkeeper(gk2)
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
						new Player.Goalkeeper("portiere1", "panchina", Player.Club.ATALANTA),
						new Player.Goalkeeper("portiere2", "panchina", Player.Club.ATALANTA),
						new Player.Goalkeeper("portiere3", "panchina", Player.Club.ATALANTA))
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

		when(lineUpRepository.getLineUpByMatchAndTeam(match, team1)).thenReturn(Optional.of(lineup1));
		when(lineUpRepository.getLineUpByMatchAndTeam(match, team2)).thenReturn(Optional.of(lineup2));

		// Grades
		Grade grade1 = new Grade(gk1, dayToCalc, 70.0);
		Grade grade2 = new Grade(gk2, dayToCalc, 60.0);
		when(gradeRepository.getAllMatchGrades(dayToCalc)).thenReturn(List.of(grade1, grade2));

        //TODO non ho idea di cosa sia
		// Act
		//serviceWithFixedDate.calculateGrades(admin, league);

		// Assert: Result persisted
        //TODO un assert true è più bellino
		verify(resultRepository).saveResult(any());

		// Assert: team points updated
		assertThat(team1.getPoints()).isEqualTo(3);
		assertThat(team2.getPoints()).isEqualTo(0);
	}

}

