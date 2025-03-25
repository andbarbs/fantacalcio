package presenter;

import model.FantaCoach;
import model.League;
import repository.LeagueRepository;
import view.LeagueView;

public class LeaguePresenter {

	private LeagueView leagueView;
	private LeagueRepository leagueRepository;
	private FantaCoach fantaCoach;

	public LeaguePresenter(LeagueView leagueView, LeagueRepository leagueRepository, FantaCoach fantaCoach) {
		this.leagueView = leagueView;
		this.leagueRepository = leagueRepository;
		this.fantaCoach = fantaCoach;
	}

	public void createLeague(League league) {
		League existingLeague = leagueRepository.getLeagueByName(league.getName());
		if (existingLeague != null)
			leagueView.showError(league.getName() + " già esistente");
		else {
			leagueRepository.add(league);
			leagueRepository.setAdmin(fantaCoach);
			leagueView.newLeagueCreated(existingLeague);
		}
	}

	public void showLeague(League league) {
		leagueView.showLeague(leagueRepository.getLeagueByName(league.getName()));
	}

	public void joinLeague(League league) {
		boolean leagueIsFull = leagueRepository.isLeagueFull(league);
		if (leagueIsFull)
			leagueView.showError(league.getName() + " è già piena");
		else {
			leagueRepository.addFantaCoach(league, fantaCoach);
			leagueView.joinLeague(league);
		}
	}

}
