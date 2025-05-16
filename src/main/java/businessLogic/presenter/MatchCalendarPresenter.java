package businessLogic.presenter;

import businessLogic.abstractDAL.repository.MatchRepository;
import businessLogic.abstractView.MatchCalendarView;
import domainModel.Match;

public class MatchCalendarPresenter {
	
	private MatchCalendarView matchCalendarView;
	private MatchRepository matchRepository;

	public MatchCalendarPresenter(MatchCalendarView matchCalendarView, MatchRepository matchRepository) {
		this.matchCalendarView = matchCalendarView;
		this.matchRepository = matchRepository;
	}
	
	public void showCalendar() {
		matchCalendarView.showCalendar(matchRepository.getEveryMatch());
	}
	
	public void showPlayedMatches() {
		matchCalendarView.showCalendar(matchRepository.getPlayedMatches());
	}
	
	public void showFutureMatches() {
		matchCalendarView.showCalendar(matchRepository.getFutureMatches());
	}
	
	public void showMatch(Match clickedMatch) {
		matchCalendarView.showMatch(clickedMatch);
	}

}
