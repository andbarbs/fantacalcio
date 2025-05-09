package businessLogic.presenter;

import java.util.List;

import businessLogic.abstractDAL.repository.TeamRepository;
import businessLogic.abstractView.LeagueTableView;
import domainModel.FantaTeam;
import domainModel.League;

public class StandingsPresenter {

	private LeagueTableView leagueTableView;
	private TeamRepository teamRepository;

	public StandingsPresenter(LeagueTableView leagueTableView, TeamRepository teamRepository) {
		this.leagueTableView = leagueTableView;
		this.teamRepository = teamRepository;
	}
	
	public void showLeagueTable(League actualLeague) {
		List<FantaTeam> teams = teamRepository.getAllTeams(actualLeague);
		// sort teams before passing to the View
		leagueTableView.showLeagueTable(teams); // poi dovrà essere chiamato il SessionBean
	}
}
