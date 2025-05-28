package businessLogic;

import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.*;
import java.util.function.BiFunction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.answer;


import businessLogic.abstractRepositories.*;
import domainModel.FantaTeam;
import domainModel.League;
import domainModel.Match;
import domainModel.MatchDaySerieA;
import domainModel.NewsPaper;
import domainModel.User;
import jakarta.persistence.EntityManager;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private AbstractJpaMatchRepository matchRepository;
    @Mock
    private AbstractJpaLeagueRepository leagueRepository;
    @Mock
    private AbstractJpaPlayerRepository playerRepository;
    @Mock
    private AbstractJpaTeamRepository teamRepository;
    @Mock
    private AbstractJpaGradeRepository gradeRepository;
    @Mock
    private AbstractJpaProposalRepository proposalRepository;
    @Mock
    private AbstractJpaContractRepository contractRepository;

    @InjectMocks
    private TransactionContext context;
    
    @Mock
    EntityManager jpaEntityManager;
    
    @Mock
    TransactionManager transactionManager;
    
    private UserService userService;
    
	@BeforeEach
	public void setup() {
		// garantisce che la lambda passata a TransactionManager 
		// venga eseguita su Context e EntityManager mockati
		when(transactionManager.fromTransaction(any()))
				.thenAnswer(answer(
						(BiFunction<TransactionContext, EntityManager, ?> code) -> code.apply(context, jpaEntityManager)));
//		doAnswer(answer((BiFunction<TransactionContext, EntityManager, ?> code) -> code.apply(context, jpaEntityManager)))
//			.when(transactionManager).inTransaction(any());
		userService = new UserService(transactionManager);
	}

    @Test
    void testGetAllMatches_delegatesToRepository() {
        League league = new League(new User(), "testLeague", new NewsPaper(), "001");
        MatchDaySerieA matchDay = new MatchDaySerieA("giornata1", LocalDate.of(2020, 01, 01));
        Map<MatchDaySerieA, Set<Match>> expectedMatches = new HashMap<>();
        expectedMatches.put(matchDay, new HashSet<Match>(List.of(
        		new Match(matchDay, new FantaTeam(), new FantaTeam()))));

        // Stub the repository call
        when(matchRepository.getAllMatches(any(), eq(league))).thenReturn(expectedMatches);

        // Call the service method.
        Map<MatchDaySerieA, Set<Match>> actualMatches = userService.getAllMatches(league);

        // Verify that the repository method was called once, and with the correct league.
        verify(matchRepository, times(1)).getAllMatches(any(), eq(league));

        // Assert that the result from the service is as expected.
        assertThat(expectedMatches).containsAllEntriesOf(actualMatches);
    }

 
}
