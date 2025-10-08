package business;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import business.ports.repository.GradeRepository;
import business.ports.repository.MatchDayRepository;
import business.ports.repository.PlayerRepository;
import business.ports.transaction.TransactionManager;
import business.ports.transaction.TransactionManager.TransactionContext;
import domain.*;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@Tag("mockito-agent")
class NewsPaperServiceTest {

	private TransactionManager transactionManager;
	private TransactionContext context;
	private NewsPaperService service;

	private Grade grade;
	private MatchDaySerieA matchDay;
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

		grade = mock(Grade.class);
		matchDay = mock(MatchDaySerieA.class);

		when(grade.getMatchDay()).thenReturn(matchDay);
		when(grade.getMark()).thenReturn(10.0);

		gradeRepository = mock(GradeRepository.class);
		playerRepository = mock(PlayerRepository.class);
		matchDayRepository = mock(MatchDayRepository.class);

		when(context.getGradeRepository()).thenReturn(gradeRepository);
		when(context.getPlayerRepository()).thenReturn(playerRepository);
		when(context.getMatchDayRepository()).thenReturn(matchDayRepository);

	}

	@Test
	void testSetVoteToPlayers_NoMatchDay() {
		NewsPaperService spyService = spy(service);
		doReturn(Optional.empty()).when(spyService).getMatchDay();

		assertThatThrownBy(() -> spyService.setVoteToPlayers(Set.of(grade))).isInstanceOf(RuntimeException.class)
				.hasMessageContaining("Now you can't assign the votes");
	}

	@Test
	void testSetVoteToPlayers_MultipleGrades() {
		Grade grades = mock(Grade.class);
		when(grades.getMatchDay()).thenReturn(matchDay);
		when(grades.getMark()).thenReturn(15.0);

		NewsPaperService spyService = spy(service);
		doReturn(Optional.of(matchDay)).when(spyService).getMatchDay();

		spyService.setVoteToPlayers(Set.of(grade, grades));

		verify(gradeRepository).saveGrade(grade);
		verify(gradeRepository).saveGrade(grades);
	}

	@Test
	void testSetVoteToPlayers_WrongMatchDay() {
		NewsPaperService spyService = spy(service);
		MatchDaySerieA otherDay = mock(MatchDaySerieA.class);
		doReturn(Optional.of(otherDay)).when(spyService).getMatchDay();

		assertThatThrownBy(() -> spyService.setVoteToPlayers(Set.of(grade))).isInstanceOf(RuntimeException.class)
				.hasMessageContaining("The match date is not correct");
	}

	@Test
	void testSetVoteToPlayers_InvalidMarkTooLow() {
		NewsPaperService spyService = spy(service);
		doReturn(Optional.of(matchDay)).when(spyService).getMatchDay();
		when(grade.getMatchDay()).thenReturn(matchDay);
		when(grade.getMark()).thenReturn(-10.0); // invalid

		assertThatThrownBy(() -> spyService.setVoteToPlayers(Set.of(grade)))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Marks must be between -5 and 25");
	}
	
	@Test
	void testSetVoteToPlayers_InvalidMarkTooHigh() {
		NewsPaperService spyService = spy(service);
		doReturn(Optional.of(matchDay)).when(spyService).getMatchDay();
		when(grade.getMatchDay()).thenReturn(matchDay);
		when(grade.getMark()).thenReturn(30.0); // invalid

		assertThatThrownBy(() -> spyService.setVoteToPlayers(Set.of(grade)))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Marks must be between -5 and 25");
	}

	@Test
	void testSetVoteToPlayers_BoundaryMarks_Min() {
		Grade minGrade = mock(Grade.class);
		when(minGrade.getMatchDay()).thenReturn(matchDay);
		when(minGrade.getMark()).thenReturn(-5.0);

		NewsPaperService spyService = spy(service);
		doReturn(Optional.of(matchDay)).when(spyService).getMatchDay();

		spyService.setVoteToPlayers(Set.of(minGrade));

		// Verify save is called once
		verify(gradeRepository).saveGrade(minGrade);
	}

	@Test
	void testSetVoteToPlayers_BoundaryMarks_Max() {
		Grade maxGrade = mock(Grade.class);
		when(maxGrade.getMatchDay()).thenReturn(matchDay);
		when(maxGrade.getMark()).thenReturn(25.0);

		NewsPaperService spyService = spy(service);
		doReturn(Optional.of(matchDay)).when(spyService).getMatchDay();

		spyService.setVoteToPlayers(Set.of(maxGrade));

		// Verify save is called once
		verify(gradeRepository).saveGrade(maxGrade);
	}

	@Test
	void testSetVoteToPlayers_HappyPath() {
		// Spy the service to mock getMatchDay
		NewsPaperService spyService = spy(service);
		doReturn(Optional.of(matchDay)).when(spyService).getMatchDay();

		// Mock the grade
		when(grade.getMatchDay()).thenReturn(matchDay);
		when(grade.getMark()).thenReturn(10.0);

		// Run the method
		spyService.setVoteToPlayers(Set.of(grade));

		// Verify the save was called
		verify(gradeRepository).saveGrade(grade);
	}

	@Test
	void testSetVoteToPlayers_Error_NoUnexpectedRepoCalls() {
		NewsPaperService spyService = spy(service);
		doReturn(Optional.empty()).when(spyService).getMatchDay();

		assertThatThrownBy(() -> spyService.setVoteToPlayers(Set.of(grade))).isInstanceOf(RuntimeException.class)
				.hasMessageContaining("Now you can't assign the votes");

		// Verify no grade was saved
		verifyNoInteractions(gradeRepository);
	}

	@Test
	void testSetVoteToPlayers_Error_WrongMatchDay_NoUnexpectedRepoCalls() {
		NewsPaperService spyService = spy(service);
		MatchDaySerieA otherDay = mock(MatchDaySerieA.class);
		doReturn(Optional.of(otherDay)).when(spyService).getMatchDay();

		assertThatThrownBy(() -> spyService.setVoteToPlayers(Set.of(grade))).isInstanceOf(RuntimeException.class)
				.hasMessageContaining("The match date is not correct");

		// Verify no grade was saved
		verifyNoInteractions(gradeRepository);
	}

	@Test
	void testSetVoteToPlayers_Error_InvalidMark_NoUnexpectedRepoCalls() {
		NewsPaperService spyService = spy(service);
		doReturn(Optional.of(matchDay)).when(spyService).getMatchDay();
		when(grade.getMark()).thenReturn(30.0); // invalid

		assertThatThrownBy(() -> spyService.setVoteToPlayers(Set.of(grade)))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Marks must be between -5 and 25");

		// Verify no grade was saved
		verifyNoInteractions(gradeRepository);
	}

	@Test
	void testGetPlayersToGrade() {
		Player player = mock(Player.class);
		when(context.getPlayerRepository().findByClub(Player.Club.JUVENTUS)).thenReturn(Set.of(player));

		Set<Player> players = service.getPlayersToGrade(Player.Club.JUVENTUS);

		assertThat(players).containsExactly(player);
	}

	@Test
	void testGetMatchDay_Saturday() {
		LocalDate saturday = LocalDate.of(2025, 9, 20); // Saturday
		MatchDaySerieA next = mock(MatchDaySerieA.class);

		NewsPaperService spyService = spy(service);

		// Override today()
		doReturn(saturday).when(spyService).today();

		// Stub repository calls
		when(context.getMatchDayRepository().getNextMatchDay(saturday)).thenReturn(Optional.of(next));

		Optional<MatchDaySerieA> result = spyService.getMatchDay();

		assertThat(result).contains(next);
	}

	@Test
	void testGetMatchDay_SundayNextPresent() {
		LocalDate sunday = LocalDate.of(2025, 9, 21); // Sunday
		MatchDaySerieA next = mock(MatchDaySerieA.class);

		NewsPaperService spyService = spy(service);
		doReturn(sunday).when(spyService).today();

		when(context.getMatchDayRepository().getNextMatchDay(sunday)).thenReturn(Optional.of(next));

		Optional<MatchDaySerieA> result = spyService.getMatchDay();

		assertThat(result).contains(next);
	}

	@Test
	void testGetMatchDay_SundayNextEmpty() {
		LocalDate sunday = LocalDate.of(2025, 9, 21); // Sunday
		MatchDaySerieA prev = mock(MatchDaySerieA.class);

		NewsPaperService spyService = spy(service);
		doReturn(sunday).when(spyService).today();

		when(context.getMatchDayRepository().getNextMatchDay(sunday)).thenReturn(Optional.empty());
		when(context.getMatchDayRepository().getPreviousMatchDay(sunday)).thenReturn(Optional.of(prev));

		Optional<MatchDaySerieA> result = spyService.getMatchDay();

		assertThat(result).contains(prev);
	}

	@Test
	void testGetMatchDay_Monday() {
		LocalDate monday = LocalDate.of(2025, 9, 22); // Monday
		MatchDaySerieA prev = mock(MatchDaySerieA.class);

		NewsPaperService spyService = spy(service);
		doReturn(monday).when(spyService).today();

		when(context.getMatchDayRepository().getPreviousMatchDay(monday)).thenReturn(Optional.of(prev));

		Optional<MatchDaySerieA> result = spyService.getMatchDay();

		assertThat(result).contains(prev);
	}
	
	@Test
	void testGetMatchDay_Tuesday_ReturnsEmpty() {
	    LocalDate tuesday = LocalDate.of(2025, 9, 23); // Tuesday
	    NewsPaperService spyService = spy(service);
	    doReturn(tuesday).when(spyService).today();

	    Optional<MatchDaySerieA> result = spyService.getMatchDay();

	    assertThat(result).isEmpty();
	    verifyNoInteractions(matchDayRepository);
	}

}
