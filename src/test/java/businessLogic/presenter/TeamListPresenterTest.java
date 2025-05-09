package businessLogic.presenter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import businessLogic.SessionBean;
import businessLogic.abstractDAL.repository.TeamRepository;
import businessLogic.abstractView.TeamListView;
import domainModel.FantaTeam;
import domainModel.League;

import java.util.ArrayList;
import java.util.List;

public class TeamListPresenterTest {

    private TeamListPresenter teamListPresenter;
    private TeamListView teamListView;
    private TeamRepository teamRepository;
    private List<FantaTeam> teams;

    @BeforeEach
    public void setup() {
        teamListView = mock(TeamListView.class);
        teamRepository = mock(TeamRepository.class);
        teamListPresenter = new TeamListPresenter(teamListView, teamRepository);
        teams = new ArrayList<>();
    }

    @Test
    public void testAllTeams() {
        League fakeLeague = null;
		when(teamRepository.getAllTeams(fakeLeague)).thenReturn(teams);
        teamListPresenter.allTeams(fakeLeague);
        verify(teamListView).showAllTeams(teams);
    }
}
