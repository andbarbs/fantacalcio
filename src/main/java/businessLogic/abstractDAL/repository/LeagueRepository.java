package businessLogic.abstractDAL.repository;

import domainModel.League;

public interface LeagueRepository {

	League getLeagueByName(String leagueName);

	void add(League league);

}
