package businessLogic.DAL.repository;

import model.League;

public interface LeagueRepository {

	League getLeagueByName(String leagueName);

	void add(League league);

}
