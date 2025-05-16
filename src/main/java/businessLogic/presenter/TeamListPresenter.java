package businessLogic.presenter;

import businessLogic.abstractDAL.PlayerRepository;
import businessLogic.abstractDAL.repository.TeamRepository;
import businessLogic.abstractView.TeamListView;
import domainModel.FantaTeam;
import domainModel.League;

public class TeamListPresenter {

	private TeamListView teamListView;
	private TeamRepository teamRepository;
	private PlayerRepository playerRepository;

	public TeamListPresenter(TeamListView teamListView, TeamRepository teamRepository, PlayerRepository playerRepository) {
		this.teamListView = teamListView;
		this.teamRepository = teamRepository;
		this.playerRepository = playerRepository;
	}

	public void allTeams(League actualLeague) {
		teamListView.showAllTeams(teamRepository.getAllTeams(actualLeague)); // dovrà essere modificato chiamando il SessionBean
	}

	public void showTeam(FantaTeam clickedTeam) {
		teamListView.showTeamPlayers(clickedTeam.getName(), playerRepository.getPlayersByTeam(clickedTeam));
	}
}
