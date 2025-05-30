package businessLogic.repositories;

import domainModel.League;

public interface LeagueRepository {

	League getLeagueByCode(String leagueCode);

	boolean add(League league);

}
