package businessLogic.presenter;

import domainModel.League;
import businessLogic.abstractDAL.repository.LeagueRepository;
import businessLogic.abstractView.LeagueView;

public class LeaguePresenter {
	
	private LeagueView leagueView;
	private LeagueRepository leagueRepository;

	public LeaguePresenter(LeagueView leagueView, LeagueRepository leagueRepository) {
		this.leagueView = leagueView;
		this.leagueRepository = leagueRepository;
	}
	
	public void createLeague(League league) {
		League existingLeague = leagueRepository.getLeagueByName(league.getName());
		if (existingLeague != null)
			leagueView.showError(league.getName() + " già esistente");
		
		leagueRepository.add(league);
		leagueView.newLeagueCreated(existingLeague);
	}
	
	public void joinLeague(String leagueName) {
		leagueView.joinLeague(leagueRepository.getLeagueByName(leagueName));
	}

}
