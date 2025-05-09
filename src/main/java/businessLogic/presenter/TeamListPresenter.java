package businessLogic.presenter;

import businessLogic.abstractDAL.repository.TeamRepository;
import businessLogic.abstractView.TeamListView;
import domainModel.League;

public class TeamListPresenter {

	private TeamListView teamListView;
	private TeamRepository teamRepository;

	public TeamListPresenter(TeamListView teamListView, TeamRepository teamRepository) {
		this.teamListView = teamListView;
		this.teamRepository = teamRepository;
	}

	public void allTeams(League actualLeague) {
		teamListView.showAllTeams(teamRepository.getAllTeams(actualLeague)); // dovrà essere modificato chiamando il SessionBean
	}

}
