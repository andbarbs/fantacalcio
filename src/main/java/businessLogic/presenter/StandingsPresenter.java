package businessLogic.presenter;

import businessLogic.abstractDAL.repository.TeamRepository;
import businessLogic.abstractView.LeagueTableView;
import domainModel.League;

public class StandingsPresenter {

	private LeagueTableView leagueTableView;
	private TeamRepository teamRepository;

	public StandingsPresenter(LeagueTableView leagueTableView, TeamRepository teamRepository) {
		this.leagueTableView = leagueTableView;
		this.teamRepository = teamRepository;
	}
	
	public void showLeagueTable(League actualLeague) {
		leagueTableView.showLeagueTable(teamRepository.getAllTeams(actualLeague)); // poi dovrà essere chiamato il SessionBean
	}
}
