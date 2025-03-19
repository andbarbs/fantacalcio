package presenter;

import repository.TeamRepository;
import view.TeamListView;

public class TeamListPresenter {

	private TeamListView teamListView;
	private TeamRepository teamRepository;

	public TeamListPresenter(TeamListView teamListView, TeamRepository teamRepository) {
		this.teamListView = teamListView;
		this.teamRepository = teamRepository;
	}

	public void allTeams() {
		teamListView.showAllTeams(teamRepository.getAllTeams());
	}

}
