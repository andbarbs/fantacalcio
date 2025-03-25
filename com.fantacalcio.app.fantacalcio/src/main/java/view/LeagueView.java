package view;

import model.League;

public interface LeagueView {

	void joinLeague(League league);

	void showError(String string);

	void newLeagueCreated(League existingLeague);

	void showLeague(League leagueByName);

}
