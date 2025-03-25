package businessLogic.presenter;

import businessLogic.abstractDAL.repository.TeamRepository;
import businessLogic.abstractView.LeagueTableView;

public class LeagueTablePresenter {

	private LeagueTableView leagueTableView;
	private TeamRepository teamRepository;

	public LeagueTablePresenter(LeagueTableView leagueTableView, TeamRepository teamRepository) {
		this.leagueTableView = leagueTableView;
		this.teamRepository = teamRepository;
	}
	
	public void showLeagueTable() {
		leagueTableView.showLeagueTable(teamRepository.getAllTeams());
	}
}
