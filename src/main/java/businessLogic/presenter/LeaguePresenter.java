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
		if (leagueRepository.add(league))
			leagueView.newLeagueCreated(league);
		else
			leagueView.showError(league.getName() + " già esistente");
	}
	
	public void joinLeague(String leagueCode) {
		leagueView.joinLeague(leagueRepository.getLeagueByCode(leagueCode));
	}

}
