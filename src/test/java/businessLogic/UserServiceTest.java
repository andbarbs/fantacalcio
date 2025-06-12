package businessLogic;

import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import businessLogic.repositories.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.answer;

import domainModel.Contract;
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
    private MatchRepository matchRepository;
    @Mock
    private LeagueRepository leagueRepository;
    @Mock
    private PlayerRepository playerRepository;
    @Mock
    private TeamRepository teamRepository;
    @Mock
    private GradeRepository gradeRepository;
    @Mock
    private ProposalRepository proposalRepository;
    @Mock
    private ContractRepository contractRepository;

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
						(Function<TransactionContext, ?> code) -> code.apply(context)));
		userService = new UserService(transactionManager);
	}

    @Test
    void testGetAllMatches_delegatesToRepository() {
        User user = new User("testMail", "password");
        League league = new League(user, "testLeague", new NewsPaper("gazzetta"), "001");
        MatchDaySerieA matchDay = new MatchDaySerieA("giornata1", LocalDate.of(2020, 01, 01));
        Set<Contract> contractsA = new HashSet<>();
        Set<Contract> contractsB = new HashSet<>();
        Map<MatchDaySerieA, Set<Match>> expectedMatches = new HashMap<>();
        expectedMatches.put(matchDay, new HashSet<Match>(List.of(
        		new Match(
        				matchDay, 
        				new FantaTeam("TeamA", league, 10, user, contractsA), 
        				new FantaTeam("TeamB", league, 10, user, contractsB)))));
        //TODO controlla che un user ha due team

        // Stub the repository call
        when(matchRepository.getAllMatches(eq(league))).thenReturn(expectedMatches);

        // Call the service method.
        Map<MatchDaySerieA, Set<Match>> actualMatches = userService.getAllMatches(league);

        // Verify that the repository method was called once, and with the correct league.
        verify(matchRepository, times(1)).getAllMatches(eq(league));

        // Assert that the result from the service is as expected.
        assertThat(expectedMatches).containsAllEntriesOf(actualMatches);
    }

 
}
