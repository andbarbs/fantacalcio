package businessLogic.presenter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import businessLogic.abstractDAL.repository.MatchRepository;
import businessLogic.abstractView.MatchCalendarView;
import domainModel.Match;

import java.util.ArrayList;
import java.util.List;

public class MatchCalendarPresenterTest {

    private MatchCalendarPresenter matchCalendarPresenter;
    private MatchCalendarView matchCalendarView;
    private MatchRepository matchRepository;
    private List<Match> matches;

    @BeforeEach
    public void setup() {
        matchCalendarView = mock(MatchCalendarView.class);
        matchRepository = mock(MatchRepository.class);
        matchCalendarPresenter = new MatchCalendarPresenter(matchCalendarView, matchRepository);
        matches = new ArrayList<>();
    }

    @Test
    public void testShowCalendar() {
        when(matchRepository.getEveryMatch()).thenReturn(matches);
        matchCalendarPresenter.showCalendar();
        verify(matchCalendarView).showCalendar(matches);
    }

    @Test
    public void testShowPlayedMatches() {
        when(matchRepository.getPlayedMatches()).thenReturn(matches);
        matchCalendarPresenter.showPlayedMatches();
        verify(matchCalendarView).showCalendar(matches);
    }

    @Test
    public void testShowFutureMatches() {
        when(matchRepository.getFutureMatches()).thenReturn(matches);
        matchCalendarPresenter.showFutureMatches();
        verify(matchCalendarView).showCalendar(matches);
    }
}

