package presenters.abstractViews;

import java.util.List;

import domainModel.Match;

public interface MatchCalendarView {

	void showCalendar(List<Match> everyMatch);

	void showMatch(Match clickedMatch);

}
