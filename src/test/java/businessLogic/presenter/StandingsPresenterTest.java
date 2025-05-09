package businessLogic.presenter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import businessLogic.abstractDAL.repository.TeamRepository;
import businessLogic.abstractView.LeagueTableView;
import domainModel.FantaTeam;

import java.util.ArrayList;
import java.util.List;

public class LeagueTablePresenterTest {

    private LeagueTablePresenter leagueTablePresenter;
    private LeagueTableView leagueTableView;
    private TeamRepository teamRepository;
    private List<FantaTeam> teams;

    @BeforeEach
    public void setup() {
        leagueTableView = mock(LeagueTableView.class);
        teamRepository = mock(TeamRepository.class);
        leagueTablePresenter = new LeagueTablePresenter(leagueTableView, teamRepository);
        teams = new ArrayList<>();
    }

    @Test
    public void testShowLeagueTable() {
        when(teamRepository.getAllTeams()).thenReturn(teams);
        leagueTablePresenter.showLeagueTable();
        verify(leagueTableView).showLeagueTable(teams);
    }
}

