package business;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import business.ports.repository.GradeRepository;
import business.ports.repository.LeagueRepository;
import business.ports.repository.MatchDayRepository;
import business.ports.repository.PlayerRepository;
import business.ports.transaction.TransactionManager;
import business.ports.transaction.TransactionManager.TransactionContext;
import domain.*;
import domain.Player.Club;
import domain.Player.Forward;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@Tag("mockito-agent")
@ExtendWith(MockitoExtension.class)
class NewsPaperServiceTest {

	private @Mock TransactionManager transactionManager;
	private @Mock TransactionContext context;
	
	private NewsPaperService service;

	// Repositories	
	private @Mock GradeRepository gradeRepository;
	private @Mock MatchDayRepository matchDayRepository;
	private @Mock PlayerRepository playerRepository;
	private @Mock LeagueRepository leagueRepository;
	
	@BeforeEach
	void setUp() {
		
		// fake TransactionManager that processes lambdas on mock Context
		transactionManager = new TransactionManager() {
			
			@Override
			public void inTransaction(Consumer<TransactionContext> code) {
				code.accept(context);
			}
			
			@Override
			public <T> T fromTransaction(Function<TransactionContext, T> code) {
				return code.apply(context);
			}
		};

		// instantiates SUT
		service = new NewsPaperService(transactionManager);
	}

    //TODO ricontrollare logica dovrebbe essere tutta corretta l'ha rivista andre
	@Test
	void testSetVoteToPlayers_NoMatchDay() {
		
		// GIVEN the necessary Repositories are made available by the TransactionContext
		when(context.getMatchDayRepository()).thenReturn(matchDayRepository);
		
		// GIVEN no ongoing MatchDay exists for the League refd by Grades
		FantaUser manager = new FantaUser("manager@example.com", "securePass");
		League league = new League(manager, "Serie A", "code");
		MatchDay futureMatchDay = new MatchDay("2 giornata", 2, MatchDay.Status.FUTURE, league);
		Forward player = new Player.Forward("Francesco", "Totti", Club.ROMA);
		
		// AND the LeagueRepository 
		when(matchDayRepository.getOngoingMatchDay(league)).thenReturn(Optional.empty());
		
		// WHEN the SUT us used to save grades whose League has no ongoing MatchDay
		Grade incorrect = new Grade(player, futureMatchDay, 10);
		ThrowingCallable shouldThrow = () -> service.save(Set.of(incorrect));
		
		// THEN an exception  is thrown
		assertThatThrownBy(shouldThrow).isInstanceOf(RuntimeException.class)
				.hasMessageContaining("Now you can't assign the votes");
	}

	@Test
	void testSetVoteToPlayers_MultipleGrades() {
		
		// GIVEN the necessary Repositories are made available by the TransactionContext
		when(context.getMatchDayRepository()).thenReturn(matchDayRepository);
		when(context.getGradeRepository()).thenReturn(gradeRepository);
		
		// AND 
		FantaUser manager = new FantaUser("manager@example.com", "securePass");
		League league = new League(manager, "Serie A", "code");
		MatchDay ongoingMatchDay = new MatchDay("2 giornata", 2, MatchDay.Status.PRESENT, league);
		Forward player1 = new Player.Forward("Francesco", "Totti", Club.ROMA);
		Player player2 = new Player.Midfielder("Kevin", "De Bruyne", Club.NAPOLI);
		
		// AND
		when(matchDayRepository.getOngoingMatchDay(league)).thenReturn(Optional.of(ongoingMatchDay));
		
		// WHEN the SUT is used to sav ethem
		Grade grade1 = new Grade(player1, ongoingMatchDay, 10);
		Grade grade2 = new Grade(player2, ongoingMatchDay, 7);
		service.save(Set.of(grade1, grade2));
		
		// THEN they are persisted
		verify(gradeRepository).saveGrade(grade1);
		verify(gradeRepository).saveGrade(grade2);
		verifyNoMoreInteractions(gradeRepository);
	}

	@Test
	void testSetVoteToPlayers_WrongMatchDay() {
		
		// GIVEN the necessary Repositories are made available by the TransactionContext
		when(context.getMatchDayRepository()).thenReturn(matchDayRepository);
		
		// AND Grades reference a MatchDay that is not the League's ongoing
		FantaUser manager = new FantaUser("manager@example.com", "securePass");
		League league = new League(manager, "Serie A", "code");
		MatchDay pastMatchDay = new MatchDay("1 giornata", 1, MatchDay.Status.PAST, league);
		MatchDay ongoingMatchDay = new MatchDay("2 giornata", 2, MatchDay.Status.PRESENT, league);
		Forward player = new Player.Forward("Francesco", "Totti", Club.ROMA);
		
		// AND the LeagueRepository 
		when(matchDayRepository.getOngoingMatchDay(league)).thenReturn(Optional.of(ongoingMatchDay));
		
		// WHEN the SUT us used to save grades that reference a past Matchday
		Grade incorrect = new Grade(player, pastMatchDay, 10);
		ThrowingCallable shouldThrow = () -> service.save(Set.of(incorrect));
		
		// THEN an exception  is thrown
		assertThatThrownBy(shouldThrow).isInstanceOf(RuntimeException.class)
		.hasMessageContaining("matchDay is not the present one");
		
		// ADN
		verifyNoMoreInteractions(gradeRepository);
	}

	@Test
	void testSetVoteToPlayers_InvalidMarkTooLow() {
		
		// GIVEN the necessary Repositories are made available by the TransactionContext
		when(context.getMatchDayRepository()).thenReturn(matchDayRepository);

		// AND
		FantaUser manager = new FantaUser("manager@example.com", "securePass");
		League league = new League(manager, "Serie A", "code");
		MatchDay ongoingMatchDay = new MatchDay("2 giornata", 2, MatchDay.Status.PRESENT, league);
		Forward player = new Player.Forward("Francesco", "Totti", Club.ROMA);

		// AND the LeagueRepository
		when(matchDayRepository.getOngoingMatchDay(league)).thenReturn(Optional.of(ongoingMatchDay));

		// WHEN the SUT is used to save a Grade that has too low mark
		Grade incorrect = new Grade(player, ongoingMatchDay, -10);
		ThrowingCallable shouldThrow = () -> service.save(Set.of(incorrect));

		// THEN
		assertThatThrownBy(shouldThrow).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Marks must be between -5 and 25");
		
		// ADN
		verifyNoMoreInteractions(gradeRepository);
	}
	
	@Test
	void testSetVoteToPlayers_InvalidMarkTooHigh() {
		
		// GIVEN the necessary Repositories are made available by the TransactionContext
		when(context.getMatchDayRepository()).thenReturn(matchDayRepository);
		
		// AND
		FantaUser manager = new FantaUser("manager@example.com", "securePass");
		League league = new League(manager, "Serie A", "code");
		MatchDay ongoingMatchDay = new MatchDay("2 giornata", 2, MatchDay.Status.PRESENT, league);
		Forward player = new Player.Forward("Francesco", "Totti", Club.ROMA);

		// AND the LeagueRepository
		when(matchDayRepository.getOngoingMatchDay(league)).thenReturn(Optional.of(ongoingMatchDay));

		// WHEN the SUT is used to save a Grade that has too high mark
		Grade incorrect = new Grade(player, ongoingMatchDay, 30);
		ThrowingCallable shouldThrow = () -> service.save(Set.of(incorrect));

		// THEN
		assertThatThrownBy(shouldThrow).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Marks must be between -5 and 25");
		
		// ADN
		verifyNoMoreInteractions(gradeRepository);
	}

	@Test
	void testSetVoteToPlayers_BoundaryMarks_Min() {
		
		// GIVEN the necessary Repositories are made available by the TransactionContext
		when(context.getMatchDayRepository()).thenReturn(matchDayRepository);
		
		// AND
		FantaUser manager = new FantaUser("manager@example.com", "securePass");
		League league = new League(manager, "Serie A", "code");
		MatchDay ongoingMatchDay = new MatchDay("2 giornata", 2, MatchDay.Status.PRESENT, league);
		Forward player = new Player.Forward("Francesco", "Totti", Club.ROMA);

		// AND the LeagueRepository
		when(matchDayRepository.getOngoingMatchDay(league)).thenReturn(Optional.of(ongoingMatchDay));

		// WHEN the SUT is used to save a Grade that has too low mark
		Grade incorrect = new Grade(player, ongoingMatchDay, -5);
		ThrowingCallable shouldThrow = () -> service.save(Set.of(incorrect));

		// THEN
		assertThatThrownBy(shouldThrow).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Marks must be between -5 and 25");
		
		// ADN
		verifyNoMoreInteractions(gradeRepository);
	}

	@Test
	void testSetVoteToPlayers_BoundaryMarks_Max() {
		
		// GIVEN the necessary Repositories are made available by the TransactionContext
		when(context.getMatchDayRepository()).thenReturn(matchDayRepository);
		
		// AND
		FantaUser manager = new FantaUser("manager@example.com", "securePass");
		League league = new League(manager, "Serie A", "code");
		MatchDay ongoingMatchDay = new MatchDay("2 giornata", 2, MatchDay.Status.PRESENT, league);
		Forward player = new Player.Forward("Francesco", "Totti", Club.ROMA);

		// AND the LeagueRepository
		when(matchDayRepository.getOngoingMatchDay(league)).thenReturn(Optional.of(ongoingMatchDay));

		// WHEN the SUT is used to save a Grade that has too high mark
		Grade incorrect = new Grade(player, ongoingMatchDay, 25);
		ThrowingCallable shouldThrow = () -> service.save(Set.of(incorrect));

		// THEN
		assertThatThrownBy(shouldThrow).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Marks must be between -5 and 25");
		
		// ADN
		verifyNoMoreInteractions(gradeRepository);
	}

	@Test
	void testGetPlayersToGrade() {
		
		// GIVEN the necessary Repositories are made available by the TransactionContext
		when(context.getPlayerRepository()).thenReturn(playerRepository);
		
		// GIVEN
		FantaUser manager = new FantaUser("manager@example.com", "securePass");
		League league = new League(manager, "Serie A", "code");
		
		Forward player = new Player.Forward("Francesco", "Totti", Club.ROMA);
		
		when(context.getPlayerRepository().getAllInLeague(league)).thenReturn(Set.of(player));

		// WHEN
		Set<Player> players = service.getPlayersToGrade(league);

		// THEN
		assertThat(players).containsExactly(player);
	}
}
