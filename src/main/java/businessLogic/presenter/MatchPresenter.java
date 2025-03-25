package businessLogic.presenter;

import businessLogic.DAL.repository.MatchRepository;
import view.MatchView;

public class MatchPresenter {

	private MatchView matchView;
	private MatchRepository matchRepository;

	public MatchPresenter(MatchView matchView, MatchRepository matchRepository) {
		this.matchView = matchView;
		this.matchRepository = matchRepository;
	}

	public void showNextMatch() {
		matchView.showMatch(matchRepository.getNextMatch());
	}
	
	public void showMatch(String date) {
		matchView.showMatch(matchRepository.getMatchInDate(date));
	}
	
}
