package businessLogic.presenter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import businessLogic.abstractDAL.repository.AbstractJpaTeamRepository;
import businessLogic.abstractView.TeamListView;
import domainModel.FantaTeam;
import domainModel.League;

import java.util.ArrayList;
import java.util.List;

public class TeamListPresenterTest {

    private TeamListPresenter teamListPresenter;
    private TeamListView teamListView;
    private AbstractJpaTeamRepository abstractJpaTeamRepository;
    private List<FantaTeam> teams;

    @BeforeEach
    public void setup() {
        teamListView = mock(TeamListView.class);
        abstractJpaTeamRepository = mock(AbstractJpaTeamRepository.class);
        teamListPresenter = new TeamListPresenter(teamListView, abstractJpaTeamRepository, null);
        teams = new ArrayList<>();
    }

    @Test
    public void testAllTeams() {
        League fakeLeague = null;
		when(abstractJpaTeamRepository.getAllTeams(, fakeLeague, )).thenReturn(teams);
        teamListPresenter.allTeams(fakeLeague);
        verify(teamListView).showAllTeams(teams);
    }
}
