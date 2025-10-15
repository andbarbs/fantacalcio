package business;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import business.ports.repository.ContractRepository;
import business.ports.repository.FantaTeamRepository;
import business.ports.repository.GradeRepository;
import business.ports.repository.LeagueRepository;
import business.ports.repository.MatchRepository;
import business.ports.repository.PlayerRepository;
import business.ports.repository.ProposalRepository;
import business.ports.transaction.TransactionManager;
import business.ports.transaction.TransactionManager.TransactionContext;
import jakarta.persistence.EntityManager;

@Tag("mockito-agent")
@ExtendWith(MockitoExtension.class)
public class FantaUserServiceTest {

    @Mock
    private MatchRepository matchRepository;
    @Mock
    private LeagueRepository leagueRepository;
    @Mock
    private PlayerRepository playerRepository;
    @Mock
    private FantaTeamRepository teamRepository;
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
    
//    private UserService userService;
    
//	@BeforeEach
//	public void setup() {
//		// garantisce che la lambda passata a TransactionManager 
//		// venga eseguita su Context e EntityManager mockati
//		when(transactionManager.fromTransaction(any()))
//				.thenAnswer(answer(
//						(Function<TransactionContext, ?> code) -> code.apply(context)));
//		userService = new UserService(transactionManager);
//	}

//    @Test
//    void testGetAllMatches_delegatesToRepository() {
//        FantaUser fantaUser = new FantaUser("testMail", "password");
//        League league = new League(fantaUser, "testLeague", new NewsPaper("gazzetta"), "001");
//        MatchDaySerieA matchDay = new MatchDaySerieA("giornata1", LocalDate.of(2020, 01, 01));
//        Set<Contract> contractsA = new HashSet<>();
//        Set<Contract> contractsB = new HashSet<>();
//        Map<MatchDaySerieA, Set<Match>> expectedMatches = new HashMap<>();
//        expectedMatches.put(matchDay, new HashSet<Match>(List.of(
//        		new Match(
//        				matchDay, 
//        				new FantaTeam("TeamA", league, 10, fantaUser, contractsA),
//        				new FantaTeam("TeamB", league, 10, fantaUser, contractsB)))));
//
//
//        // Stub the repository call
//        when(matchRepository.getAllMatches(eq(league))).thenReturn(expectedMatches);
//
//        // Call the service method.
//        Map<MatchDaySerieA, List<Match>> actualMatches = userService.getAllMatches(league);
//
//        // Verify that the repository method was called once, and with the correct league.
//        verify(matchRepository, times(1)).getAllMatches(eq(league));
//
//        // Assert that the result from the service is as expected.
//        assertThat(expectedMatches).containsExactlyInAnyOrderEntriesOf(actualMatches);
//    }

 
}
