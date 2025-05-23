package businessLogic.presenter;

import java.util.List;

import businessLogic.abstractDAL.repository.AbstractJpaTeamRepository;
import businessLogic.abstractView.LeagueTableView;
import domainModel.FantaTeam;
import domainModel.League;

public class StandingsPresenter {

	private LeagueTableView leagueTableView;
	private AbstractJpaTeamRepository abstractJpaTeamRepository;

	public StandingsPresenter(LeagueTableView leagueTableView, AbstractJpaTeamRepository abstractJpaTeamRepository) {
		this.leagueTableView = leagueTableView;
		this.abstractJpaTeamRepository = abstractJpaTeamRepository;
	}
	
	public void showLeagueTable(League actualLeague) {
		List<FantaTeam> teams = abstractJpaTeamRepository.getAllTeams(, actualLeague, );
		// sort teams before passing to the View
		leagueTableView.showLeagueTable(teams); // poi dovrà essere chiamato il SessionBean
	}
}
