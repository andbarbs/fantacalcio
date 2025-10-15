package business;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static java.util.stream.IntStream.range;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Tag("mockito-agent")
@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

	private @Mock TransactionManager transactionManager;
	private @Mock TransactionContext context;
	
	private AdminUserService adminUserService;

	// Repositories
	private @Mock MatchRepository matchRepository;
	private @Mock GradeRepository gradeRepository;
	private @Mock LineUpRepository lineUpRepository;
	private @Mock ResultsRepository resultRepository;
	private @Mock MatchDayRepository matchDayRepository;
	private @Mock FantaTeamRepository fantaTeamRepository;
	private @Mock LeagueRepository leagueRepository;
	private @Mock PlayerRepository playerRepository;
	private @Mock ProposalRepository proposalRepository;
	private @Mock ContractRepository contractRepository;

	@BeforeEach
	void setUp() {

		// Setup inTransaction
		doAnswer(invocation -> {
			Consumer<TransactionContext> code = invocation.getArgument(0);
			code.accept(context);
			return null;
		}).when(transactionManager).inTransaction(any());

		adminUserService = new AdminUserService(transactionManager);
	}
	
	@Nested
	@DisplayName("can assign Players to Teams")
	class AssignPlayers {	
		
		@Test
		void testSetPlayerToTeam_SavesContract_WhenBelowLimits() {

			// GIVEN the necessary Repositories are made available by the TransactionContext
			when(context.getContractRepository()).thenReturn(contractRepository);

			// AND
			FantaTeam team = new FantaTeam("Team", null, 0, null, new HashSet<>());
			Goalkeeper player = new Goalkeeper("Gigi", "Buffon", Player.Club.JUVENTUS);

			// WHEN the SUT is used to assign a Player to a Team
			adminUserService.setPlayerToTeam(team, player);

			// THEN the player is successfully assigned
			ArgumentCaptor<Contract> contract = ArgumentCaptor.forClass(Contract.class);
			verify(contractRepository).saveContract(contract.capture());
			verifyNoMoreInteractions(contractRepository);

			//
			assertThat(contract.getValue().getPlayer()).isEqualTo(player);
			assertThat(contract.getValue().getTeam()).isEqualTo(team);
		}
		
		@Nested
		@DisplayName("error cases")
		class AssignPlayerErrorCases {
			
			@Test
			void testSetPlayerToTeam_Throws_WhenTeamHas25Players() {
				
				// GIVEN a Team already has 25 Contracts
				Set<Contract> contracts = new HashSet<Contract>();
				FantaTeam team = new FantaTeam("Team", null, 0, null, contracts);
				IntStream.range(0, 25).forEach(i -> {
					Player p = new Midfielder("Player" + i, "Test", Player.Club.ATALANTA);
					contracts.add(new Contract(team, p));
				});
				
				// WHEN the SUT is used to assign a Player to that Team
				Player newPlayer = new Defender("New", "Player", Player.Club.BOLOGNA);
				ThrowingCallable shouldThrow = () -> adminUserService.setPlayerToTeam(team, newPlayer);
				
				// THEN an error is thrown
				assertThatThrownBy(shouldThrow).isInstanceOf(UnsupportedOperationException.class)
				.hasMessageContaining("Maximum 25 players");
			}
			
			@Test
			void testSetPlayerToTeam_DoesNotSave_WhenGoalkeepersLimitReached() {
				
				// GIVEN a Team already has max Goalkeepers
				int maxNumGoalkeepers = 3;
				Set<Contract> contracts = new HashSet<Contract>();
				FantaTeam team = new FantaTeam("Team", null, 0, null, contracts);
				IntStream.range(0, maxNumGoalkeepers).forEach(i -> contracts
						.add(new Contract(team, new Goalkeeper("Goalkeeper" + i, "Test", Player.Club.ATALANTA))));
				
				// WHEN the SUT is used to assign a further Goalkeeper to that Team
				Goalkeeper newGk = new Goalkeeper("New", "Keeper", Player.Club.ROMA);
				adminUserService.setPlayerToTeam(team, newGk);
				
				// THEN no new Contract is saved
				verify(contractRepository, never()).saveContract(any());
			}
			
			@Test
			void testSetPlayerToTeam_DoesNotSave_WhenDefendersLimitReached() {
				
				// GIVEN a Team already has max Defenders
				int maxNumDefenders = 8;
				Set<Contract> contracts = new HashSet<Contract>();
				FantaTeam team = new FantaTeam("Team", null, 0, null, contracts);
				IntStream.range(0, maxNumDefenders).forEach(
						i -> contracts.add(new Contract(team, new Defender("Defender" + i, "Test", Player.Club.ATALANTA))));
				
				// WHEN the SUT is used to assign a further Goalkeeper to that Team
				Defender excessive = new Defender("New", "Defender", Player.Club.ROMA);
				adminUserService.setPlayerToTeam(team, excessive);
				
				// THEN no new Contract is saved
				verify(contractRepository, never()).saveContract(any());
			}
			
			@Test
			void testSetPlayerToTeam_DoesNotSave_WhenMidfieldersLimitReached() {
				
				// GIVEN a Team already has max Midfielders
				int maxNumMidfielders = 8;
				Set<Contract> contracts = new HashSet<Contract>();
				FantaTeam team = new FantaTeam("Team", null, 0, null, contracts);
				IntStream.range(0, maxNumMidfielders).forEach(i -> contracts
						.add(new Contract(team, new Midfielder("Midfielder" + i, "Test", Player.Club.ATALANTA))));
				
				// WHEN the SUT is used to assign a further Goalkeeper to that Team
				Midfielder excessive = new Midfielder("New", "Midfielder", Player.Club.ROMA);
				adminUserService.setPlayerToTeam(team, excessive);
				
				// THEN no new Contract is saved
				verify(contractRepository, never()).saveContract(any());
			}
			
			@Test
			void testSetPlayerToTeam_DoesNotSave_WhenForwardsLimitReached() {
				
				// GIVEN a Team already has max Forwards
				int maxNumForwards = 6;
				Set<Contract> contracts = new HashSet<Contract>();
				FantaTeam team = new FantaTeam("Team", null, 0, null, contracts);
				IntStream.range(0, maxNumForwards).forEach(
						i -> contracts.add(new Contract(team, new Forward("Forward" + i, "Test", Player.Club.ATALANTA))));
				
				// WHEN the SUT is used to assign a further Goalkeeper to that Team
				Forward excessive = new Forward("New", "Forward", Player.Club.ROMA);
				adminUserService.setPlayerToTeam(team, excessive);
				
				// THEN no new Contract is saved
				verify(contractRepository, never()).saveContract(any());
			}
		}
		
	}
	
	@Nested
	@DisplayName("can remove Players from Teams")
	class RemovePlayers {	
		
		@Test
		void testRemovePlayerFromTeam_WhenContractExists() {
			
			// GIVEN the necessary Repositories are returned by the TransactionContext
			when(context.getContractRepository()).thenReturn(contractRepository);
			
			// AND a Contract is instantiated for a Player with a Team
			FantaTeam team = new FantaTeam("Team", null, 0, null, null);
			Player player = new Forward("Cristiano", "Ronaldo", Player.Club.JUVENTUS);
			Contract contract = new Contract(team, player);
			
			// AND the ContractRepository reports that Player as hired with the Team
			when(contractRepository.getContract(team, player)).thenReturn(Optional.of(contract));
			
			// WHEN the SUT is used to remove a Contract for that Player with the Team
			adminUserService.removePlayerFromTeam(team, player);
			
			// THEN the ContractRepository is asked to delete that Contract
			verify(contractRepository).deleteContract(contract);
		}
		
		@Test
		void testRemovePlayerFromTeam_WhenContractDoesNotExist() {
			
			// GIVEN the necessary Repositories are returned by the TransactionContext
			when(context.getContractRepository()).thenReturn(contractRepository);
			
			// AND the ContractRepository reports a Player as not hired with a Team
			FantaTeam team = new FantaTeam("Team", null, 0, null, null);
			Player player = new Defender("Giorgio", "Chiellini", Player.Club.JUVENTUS);
			when(contractRepository.getContract(team, player)).thenReturn(Optional.empty());
			
			// WHEN the SUT is used to remove a Contract for that Player with the given Team
			adminUserService.removePlayerFromTeam(team, player);
			
			// THEN the ContractRepository is not asked to delete any Contract
			verify(contractRepository, never()).deleteContract(any());
		}
	}
	
	@Nested
	@DisplayName("can generate a League's calendar")
	class GenerateCalendar {	
		
		@Test
		@DisplayName("when the League is made up of 8 Teams")
		void testGenerateCalendar_SavesMatches() {
			
			// GIVEN the necessary Repositories are returned by the TransactionContext
			when(context.getMatchRepository()).thenReturn(matchRepository);
			when(context.getMatchDayRepository()).thenReturn(matchDayRepository);
			when(context.getTeamRepository()).thenReturn(fantaTeamRepository);
			
			// GIVEN TeamRepository returns n teams as the league's
			FantaUser admin = new FantaUser(null, null);
			League league = new League(admin, "Serie A", null);
			
			int numberOfTeams = 8;
			int daysForRR = numberOfTeams - 1;
			
			List<FantaTeam> teams = range(0, numberOfTeams)
					.mapToObj(i -> new FantaTeam("Team" + (i + 1), null, 0, null, null))
					.toList();
			when(fantaTeamRepository.getAllTeams(league)).thenReturn(Set.copyOf(teams));	
			
			// AND GIVEN MatchDayRepository returns 20 MatchDay instances as the league's
			when(matchDayRepository.getAllMatchDays(league)).thenReturn(range(0, MatchDay.MATCH_DAYS_IN_LEAGUE)
					.mapToObj(i -> new MatchDay("MatchDay", i, MatchDay.Status.FUTURE, league)).toList());
			
			// WHEN the Service is asked to generate the league's calendar
			adminUserService.generateCalendar(league);
			
			// THEN MatchRepository is asked to persist the correct number of Matches
			ArgumentCaptor<Match> matchCaptor = ArgumentCaptor.forClass(Match.class);
			verify(matchRepository, times(MatchDay.MATCH_DAYS_IN_LEAGUE * (numberOfTeams / 2)))
					.saveMatch(matchCaptor.capture());
			verifyNoMoreInteractions(matchRepository);
			
			// AND persisted Match instances are such that:
			List<Match> allMatches = matchCaptor.getAllValues();
			
			// 1. every FantaTeam plays on every MatchDay
			allMatches.stream()
				.collect(Collectors.groupingBy(Match::getMatchDay,
					Collectors.flatMapping(match -> Stream.<FantaTeam>of(match.getTeam1(), match.getTeam2()),
							Collectors.toSet())))
				.values().stream()
				.forEach(teamsInDay -> assertThat(teamsInDay).containsExactlyInAnyOrderElementsOf(teams));
			
			// 2. matches in the 'outward' round are round-robin couples
			Set<Set<FantaTeam>> roundRobin = range(0, teams.size()).boxed()
					.flatMap(i -> range(i + 1, teams.size()).mapToObj(j -> Set.of(teams.get(i), teams.get(j))))
					.collect(Collectors.toSet());
			
			Set<List<FantaTeam>> outwardPairings = allMatches.stream()
					.filter(match -> range(0, daysForRR).boxed().toList().contains(match.getMatchDay().getNumber()))
					.map(match -> List.of(match.getTeam1(), match.getTeam2())).collect(toSet());
			
			assertThat(outwardPairings.stream().map(Set::copyOf).collect(toSet()))
					.containsExactlyInAnyOrderElementsOf(roundRobin);
			
			// 3. pairings in the 'return' round are reverses of those in 'outward'
			Set<List<FantaTeam>> returnPairings = allMatches.stream()
					.filter(match -> range(daysForRR, 2 * daysForRR).boxed().toList().contains(match.getMatchDay().getNumber()))
					.map(match -> List.of(match.getTeam1(), match.getTeam2())).collect(toSet());
			
			assertThat(returnPairings).containsExactlyInAnyOrderElementsOf(
					outwardPairings.stream().map(pair -> List.of(pair.get(1), pair.get(0))).collect(toSet()));
			
			// 4. 'second outward' pairings are taken form 'first outward'
			Set<List<FantaTeam>> secondOutwardPairings = allMatches.stream()
					.filter(match -> range(2 * daysForRR, MatchDay.MATCH_DAYS_IN_LEAGUE).boxed().toList()
							.contains(match.getMatchDay().getNumber()))
					.map(match -> List.of(match.getTeam1(), match.getTeam2())).collect(toSet());
			
			assertThat(secondOutwardPairings).isSubsetOf(outwardPairings);
		}
		
		@Nested
		@DisplayName("error cases")
		class GenerateCalendarError {
			
			@Test
			void testGenerateCalendar_LessThanTwoTeams_Throws() {
				
				// GIVEN the necessary Repositories are returned by the TransactionContext
				when(context.getTeamRepository()).thenReturn(fantaTeamRepository);
				
				// AND a League is reported as having strictly less than 2 Teams
				FantaUser admin = new FantaUser(null, null);
				League league = new League(admin, "Serie A", null);
				FantaTeam onlyTeam = new FantaTeam("Solo", null, 0, null, null);
				when(fantaTeamRepository.getAllTeams(league)).thenReturn(Set.of(onlyTeam));
				
				// WHEN the SUT is used to generate the League's calendar
				ThrowingCallable shouldThrow = () -> adminUserService.generateCalendar(league);
				
				// THEN an Error is thrown
				assertThatThrownBy(shouldThrow).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("At least 2 teams are required");
				
				// AND no Match is saved
				verify(matchRepository, never()).saveMatch(any());
			}
			
			@Test
			void testGenerateCalendar_OddNumberOfTeams_Throws() {
				
				// GIVEN the necessary Repositories are returned by the TransactionContext
				when(context.getTeamRepository()).thenReturn(fantaTeamRepository);
				
				// GIVEN a League is reported as having an odd number of Teams
				FantaUser admin = new FantaUser(null, null);
				League league = new League(admin, "Serie A", null);
				FantaTeam t1 = new FantaTeam("Team1", null, 0, null, null);
				FantaTeam t2 = new FantaTeam("Team2", null, 0, null, null);
				FantaTeam t3 = new FantaTeam("Team3", null, 0, null, null);
				
				when(fantaTeamRepository.getAllTeams(league)).thenReturn(Set.of(t1, t2, t3));
				
				// WHEN the SUT is used to generate the League's calendar
				ThrowingCallable shouldThrow = () -> adminUserService.generateCalendar(league);
				
				// THEN an Error is thrown
				assertThatThrownBy(shouldThrow).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Number of teams must be even");
				
				// AND no Match is saved
				verify(matchRepository, never()).saveMatch(any());
			}
		}
	}
	
	@Nested
	@DisplayName("can compute and save the Results for a MatchDay")
	class CalculateResults {
		
		@Test
		void testCalculateGrades_SeasonNotStarted_Throws() {
			
			// GIVEN the necessary Repositories are returned by the TransactionContext
			when(context.getMatchDayRepository()).thenReturn(matchDayRepository);

			// AND a League has no ongoing MatchDay
			FantaUser admin = new FantaUser(null, null);
			League league = new League(admin, "Serie A", null);

			when(matchDayRepository.getLatestEndedMatchDay(league)).thenReturn(Optional.empty());

			// WHEN the SUT is used to calculate the ongoing MatchDay's results
			ThrowingCallable shouldThrow = () -> adminUserService.calculateResults(league);

			// THEN an error is thrown
			assertThatThrownBy(shouldThrow).isInstanceOf(RuntimeException.class)
					.hasMessageContaining("The season hasn't started yet");

			// AND no Result is saved
			verify(resultRepository, never()).saveResult(any());
		}
		
		@Test
		void testCalculateGrades_SavesResultsAndUpdatesPoints(
				@Mock LineUp lineUp1, @Mock LineUpViewer lineUpViewer1,
				@Mock LineUp lineUp2, @Mock LineUpViewer lineUpViewer2) {
	
			// GIVEN the necessary Repositories are returned by the TransactionContext
			when(context.getMatchRepository()).thenReturn(matchRepository);
			when(context.getGradeRepository()).thenReturn(gradeRepository);
			when(context.getLineUpRepository()).thenReturn(lineUpRepository);
			when(context.getResultsRepository()).thenReturn(resultRepository);
			when(context.getMatchDayRepository()).thenReturn(matchDayRepository);
			
			// AND a given League is reported as having a latest-ended MatchDay
			FantaUser admin = new FantaUser("admin@example.com", "pwd");
			League league = new League(admin, "Serie A", "1234");
			MatchDay latestEnded = new MatchDay("2 giornata", 2, MatchDay.Status.PAST, league);			
			when(matchDayRepository.getLatestEndedMatchDay(league)).thenReturn(Optional.of(latestEnded));
			
			// AND one Match is reported as associated with the latest-ended MatchDay
			HashSet<Contract> contracts1 = new HashSet<Contract>();
			FantaTeam team1 = new FantaTeam("Team1", league, 0, admin, contracts1);
			HashSet<Contract> contracts2 = new HashSet<Contract>();
			FantaTeam team2 = new FantaTeam("Team2", league, 0, admin, contracts2);			
			Match match = new Match(latestEnded, team1, team2);
			when(matchRepository.getAllMatchesIn(latestEnded)).thenReturn(List.of(match));
			
			// AND Matches associated with the latest-ended MatchDay have no associated Result
			when(resultRepository.getResultFor(match)).thenReturn(Optional.empty());
			
			// AND LineUps are reported for that Match which involve two given Players
			Goalkeeper gk1 = new Goalkeeper("G1", "Alpha", Player.Club.ATALANTA);
			when(lineUp1.extract()).thenReturn(lineUpViewer1);
			when(lineUpViewer1.starterGoalkeepers()).thenReturn(Set.of(gk1));
			when(lineUpRepository.getLineUpByMatchAndTeam(match, team1)).thenReturn(Optional.of(lineUp1));
			
			Goalkeeper gk2 = new Goalkeeper("G2", "Beta", Player.Club.BOLOGNA);
			when(lineUp2.extract()).thenReturn(lineUpViewer2);
			when(lineUpViewer2.starterGoalkeepers()).thenReturn(Set.of(gk2));			
			when(lineUpRepository.getLineUpByMatchAndTeam(match, team2)).thenReturn(Optional.of(lineUp2));
			
			// AND the two given Players are assigned to the two Teams 
			contracts1.add(new Contract(team1, gk1));
			contracts2.add(new Contract(team2, gk2));			
			
			// AND two Grades are reported for the given Players in the latest-ended MatchDay
			Grade grade1 = new Grade(gk1, latestEnded, 70.0);
			Grade grade2 = new Grade(gk2, latestEnded, 60.0);
			when(gradeRepository.getAllGrades(latestEnded)).thenReturn(List.of(grade1, grade2));
			
			// WHEN the SUT is used to calculate the League's results
			adminUserService.calculateResults(league);
			
			// THEN ResultRepository is asked to save the correct Results
			verify(resultRepository).saveResult(new Result(70, 60, 1, 0, match));
			
			// AND the points for both Teams are correctly updated
			assertThat(team1.getPoints()).isEqualTo(3);
			assertThat(team2.getPoints()).isEqualTo(0);
		}
	}
	
	@Nested
	@DisplayName("can advance game state for a League")
	class MoveForwardMatchDays {
		
		@BeforeEach
		void stubContext() {
			
			// GIVEN the necessary Repositories are returned by the TransactionContext
			when(context.getMatchDayRepository()).thenReturn(matchDayRepository);
		}
		
		@Nested
		@DisplayName("starting the earliest-upcoming MatchDay")
		class StartMatchDay {
			
			@Test
			@DisplayName("happy case")
			void testStartMatchDay() {
				
				// GIVEN a League reports an earliest-upcoming MatchDay
				FantaUser admin = new FantaUser("admin@example.com", "pwd");
				League league = new League(admin, "Serie A", "1234");
				MatchDay earliestUpcoming = new MatchDay("2 giornata", 2, MatchDay.Status.FUTURE, league);			
				when(matchDayRepository.getEarliestUpcomingMatchDay(league)).thenReturn(Optional.of(earliestUpcoming));
				
				// WHEN the SUT is used to start the League's MatchDay
				adminUserService.startMatchDay(league);
				
				// THEN the MatchDay's status is updated correctly
				ArgumentCaptor<MatchDay> matchDay = ArgumentCaptor.forClass(MatchDay.class);
				verify(matchDayRepository).updateMatchDay(matchDay.capture());
				assertThat(matchDay.getValue().getStatus()).isEqualTo(MatchDay.Status.PRESENT);
			}
			
			@Test
			@DisplayName("the League has no earliest-upcoming MatchDay")
			void testStartMatchDay_error() {
				
				// GIVEN a League reports no earliest-upcoming MatchDay
				FantaUser admin = new FantaUser("admin@example.com", "pwd");
				League league = new League(admin, "Serie A", "1234");		
				when(matchDayRepository.getEarliestUpcomingMatchDay(league)).thenReturn(Optional.empty());
				
				// WHEN the SUT is used to start the League's MatchDay
				ThrowingCallable shouldThrow = () -> adminUserService.startMatchDay(league);
				
				// THEN an error is thrown
				assertThatThrownBy(shouldThrow).isInstanceOf(IllegalArgumentException.class)
						.hasMessageContaining("no more MatchDays to play");

				// AND MatchDayRepository is not contacted
				verifyNoMoreInteractions(matchDayRepository);
			}
			
			@Test
			@DisplayName("the League's latest-ended MatchDay has not been given all Results")
			void testStartMatchDay_error2() {
				
				// GIVEN the necessary Repositories are returned by the TransactionContext
				when(context.getMatchRepository()).thenReturn(matchRepository);
				when(context.getResultsRepository()).thenReturn(resultRepository);
				
				// GIVEN a League's latest-ended MatchDay is reported as missing some Results
				FantaUser admin = new FantaUser("admin@example.com", "pwd");
				League league = new League(admin, "Serie A", "1234");
				MatchDay latestEnded = new MatchDay("1 giornata", 1, MatchDay.Status.PAST, league);	
				when(matchDayRepository.getLatestEndedMatchDay(league)).thenReturn(Optional.of(latestEnded));
				MatchDay earliestUpcoming = new MatchDay("2 giornata", 2, MatchDay.Status.FUTURE, league);			
				when(matchDayRepository.getEarliestUpcomingMatchDay(league)).thenReturn(Optional.of(earliestUpcoming));
				
				FantaTeam team1 = new FantaTeam("Team1", league, 0, admin, null);
				FantaTeam team2 = new FantaTeam("Team2", league, 0, admin, null);			
				Match match12 = new Match(latestEnded, team1, team2);
				FantaTeam team3 = new FantaTeam("Team1", league, 0, admin, null);
				FantaTeam team4 = new FantaTeam("Team2", league, 0, admin, null);			
				Match match34 = new Match(latestEnded, team3, team4);
				
				when(matchRepository.getAllMatchesIn(latestEnded)).thenReturn(List.of(match12, match34));
				when(resultRepository.getResultFor(match12)).thenReturn(Optional.of(new Result(3, 0, 50, 50, match12)));
				when(resultRepository.getResultFor(match34)).thenReturn(Optional.empty());
				
				// WHEN the SUT is used to start the League's MatchDay
				ThrowingCallable shouldThrow = () -> adminUserService.startMatchDay(league);
				
				// THEN an error is thrown
				assertThatThrownBy(shouldThrow).isInstanceOf(IllegalArgumentException.class)
						.hasMessageContaining("calculate the results before advancing the game state");

				// AND MatchDayRepository is not contacted
				verifyNoMoreInteractions(matchDayRepository);
			}
		}
		
		@Nested
		@DisplayName("ending the ongoing MatchDay")
		class EndMatchDay {
			
			@Test
			@DisplayName("happy case")
			void testEndMatchDay() {
				
				// GIVEN a League reports an ongoing MatchDay
				FantaUser admin = new FantaUser("admin@example.com", "pwd");
				League league = new League(admin, "Serie A", "1234");
				MatchDay ongoingMatchDay = new MatchDay("2 giornata", 2, MatchDay.Status.PRESENT, league);			
				when(matchDayRepository.getOngoingMatchDay(league)).thenReturn(Optional.of(ongoingMatchDay));
				
				// WHEN the SUT is used to end the League's MatchDay
				adminUserService.endMatchDay(league);
				
				// THEN the MatchDay's status is updated correctly
				ArgumentCaptor<MatchDay> matchDay = ArgumentCaptor.forClass(MatchDay.class);
				verify(matchDayRepository).updateMatchDay(matchDay.capture());
				assertThat(matchDay.getValue().getStatus()).isEqualTo(MatchDay.Status.PAST);
			}
			
			@Test
			@DisplayName("the League has no ongoing MatchDay")
			void testEndMatchDay_error() {
				
				// GIVEN a League reports no ongoing MatchDay
				FantaUser admin = new FantaUser("admin@example.com", "pwd");
				League league = new League(admin, "Serie A", "1234");		
				when(matchDayRepository.getOngoingMatchDay(league)).thenReturn(Optional.empty());
				
				// WHEN the SUT is used to start the League's MatchDay
				ThrowingCallable shouldThrow = () -> adminUserService.endMatchDay(league);
				
				// THEN an error is thrown
				assertThatThrownBy(shouldThrow).isInstanceOf(IllegalArgumentException.class)
						.hasMessageContaining("no MatchDay to end");

				// AND MatchDayRepository is not contacted
				verifyNoMoreInteractions(matchDayRepository);
			}
		}
	}
}

