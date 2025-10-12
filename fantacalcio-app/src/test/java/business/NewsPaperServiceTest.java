package business;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import business.ports.repository.GradeRepository;
import business.ports.repository.MatchDayRepository;
import business.ports.repository.PlayerRepository;
import business.ports.transaction.TransactionManager;
import business.ports.transaction.TransactionManager.TransactionContext;
import domain.*;
import domain.Player.Club;
import domain.Player.Forward;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@Tag("mockito-agent")
class NewsPaperServiceTest {

	private TransactionManager transactionManager;
	private TransactionContext context;
	private NewsPaperService service;

	private GradeRepository gradeRepository;
	private PlayerRepository playerRepository;
	private MatchDayRepository matchDayRepository;
	
	@BeforeEach
	void setUp() {
		transactionManager = mock(TransactionManager.class);
		context = mock(TransactionContext.class);

		doAnswer(invocation -> {
			@SuppressWarnings("unchecked")
			java.util.function.Consumer<TransactionContext> code = (java.util.function.Consumer<TransactionContext>) invocation
					.getArgument(0);
			code.accept(context);
			return null;
		}).when(transactionManager).inTransaction(any());

		when(transactionManager.fromTransaction(any())).thenAnswer(invocation -> {
			@SuppressWarnings("unchecked")
			java.util.function.Function<TransactionContext, Object> code = (java.util.function.Function<TransactionContext, Object>) invocation
					.getArgument(0);
			return code.apply(context);
		});

		service = new NewsPaperService(transactionManager);

		

		gradeRepository = mock(GradeRepository.class);
		playerRepository = mock(PlayerRepository.class);
		matchDayRepository = mock(MatchDayRepository.class);

		when(context.getGradeRepository()).thenReturn(gradeRepository);
		when(context.getPlayerRepository()).thenReturn(playerRepository);
		when(context.getMatchDayRepository()).thenReturn(matchDayRepository);

	}

    //TODO ricontrollare logica
	@Test
	void testSetVoteToPlayers_NoMatchDay() {
		
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
		
		// GIVEN 
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
		
		// GIVEN Grades reference a MatchDay that is not the League's ongoing
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

		// GIVEN
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
		
		// GIVEN
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
		
		// GIVEN
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
		// GIVEN
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
