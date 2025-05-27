package businessLogic.presenter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import businessLogic.abstractRepositories.AbstractJpaTeamRepository;
import presenters.abstractViews.LeagueTableView;
import domainModel.FantaTeam;
import domainModel.League;
import presenters.StandingsPresenter;

import java.util.ArrayList;
import java.util.List;

public class StandingsPresenterTest {
/*
    private StandingsPresenter leagueTablePresenter;
    private LeagueTableView leagueTableView;
    private AbstractJpaTeamRepository abstractJpaTeamRepository;
    private List<FantaTeam> teams;

    @BeforeEach
    public void setup() {
        leagueTableView = mock(LeagueTableView.class);
        abstractJpaTeamRepository = mock(AbstractJpaTeamRepository.class);
        leagueTablePresenter = new StandingsPresenter(leagueTableView, abstractJpaTeamRepository);
        teams = new ArrayList<>();
    }

    @Test
    public void testShowLeagueTable() {
    	League fakeLeague = null;
        when(abstractJpaTeamRepository.getAllTeams(, fakeLeague, )).thenReturn(teams);
        leagueTablePresenter.showLeagueTable(fakeLeague);
        verify(leagueTableView).showLeagueTable(teams);
    }

 */
}

