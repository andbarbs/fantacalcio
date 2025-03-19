package view;

import model.League;

public interface LeagueView {

	void joinLeague(League leagueByName);

	void showError(String string);

	void newLeagueCreated(League existingLeague);

}
