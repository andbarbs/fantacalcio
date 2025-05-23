package businessLogic.presenter;

import businessLogic.abstractDAL.PlayerRepository;
import businessLogic.abstractDAL.repository.AbstractJpaTeamRepository;
import businessLogic.abstractView.TeamListView;
import domainModel.FantaTeam;
import domainModel.League;

public class TeamListPresenter {

	private TeamListView teamListView;
	private AbstractJpaTeamRepository abstractJpaTeamRepository;
	private PlayerRepository playerRepository;

	public TeamListPresenter(TeamListView teamListView, AbstractJpaTeamRepository abstractJpaTeamRepository, PlayerRepository playerRepository) {
		this.teamListView = teamListView;
		this.abstractJpaTeamRepository = abstractJpaTeamRepository;
		this.playerRepository = playerRepository;
	}

	public void allTeams(League actualLeague) {
		teamListView.showAllTeams(abstractJpaTeamRepository.getAllTeams(, actualLeague, )); // dovrà essere modificato chiamando il SessionBean
	}

	public void showTeam(FantaTeam clickedTeam) {
		teamListView.showTeamPlayers(clickedTeam.getName(), playerRepository.getPlayersByTeam(clickedTeam));
	}
}
