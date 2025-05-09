package businessLogic.presenter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import businessLogic.abstractDAL.repository.MatchRepository;
import businessLogic.abstractView.MatchView;
import domainModel.Match;

public class MatchPresenterTest {

    private MatchPresenter matchPresenter;
    private MatchView matchView;
    private MatchRepository matchRepository;

    @BeforeEach
    public void setup() {
        matchView = mock(MatchView.class);
        matchRepository = mock(MatchRepository.class);
        matchPresenter = new MatchPresenter(matchView, matchRepository);
    }

    @Test
    public void testShowNextMatch() {
        Match nextMatch = new Match();
        when(matchRepository.getNextMatch()).thenReturn(nextMatch);
        matchPresenter.showNextMatch();
        verify(matchView).showMatch(nextMatch);
    }

    @Test
    public void testShowMatch() {
        String date = "2021-12-12";
        Match matchOnDate = new Match();
        when(matchRepository.getMatchInDate(date)).thenReturn(matchOnDate);
        matchPresenter.showMatch(date);
        verify(matchView).showMatch(matchOnDate);
    }
}
