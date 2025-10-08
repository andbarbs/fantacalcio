package presenters.abstractViews;

import domain.League;

public interface LeagueView {

	void joinLeague(League leagueByName);

	void showError(String string);

	void newLeagueCreated(League existingLeague);

}
