package businessLogic;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import businessLogic.abstractDAL.repository.AbstractJpaMatchRepository;
import domainModel.League;
import domainModel.Match;
import domainModel.MatchDaySerieA;

@ExtendWith(MockitoExtension.class)
public class ServiceTest {

    @Mock
    private AbstractJpaMatchRepository matchRepository;

    // Assume MatchService uses the static fromSession/inSession method internally.
    // There might also be other dependencies like the EntityManagerFactory.
    @InjectMocks
    private Service service;

    @Test
    void testGetAllMatches_delegatesToRepository() {
        League league = new League(); // your test league instance
        Map<MatchDaySerieA, Set<Match>> expectedMatches = new HashMap<>();
        // Populate your expectedMatches map for testing logic

        // Stub the repository call.
        // As long as fromSession returns the value produced by matchRepository.getAllMatches,
        // you can simply simulate that dependency.
        when(matchRepository.getAllMatches(any(), eq(league))).thenReturn(expectedMatches);

        // Call the service method.
        Map<MatchDaySerieA, Set<Match>> actualMatches = service.getAllMatches(league);

        // Verify that the repository method was called once, and with the correct league.
        verify(matchRepository, times(1)).getAllMatches(any(), eq(league));

        // Assert that the result from the service is as expected.
        assertEquals(expectedMatches, actualMatches);
    }
}
