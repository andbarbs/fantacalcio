package businessLogic.repositories;

import domainModel.League;

import java.util.Optional;

public interface LeagueRepository {

	Optional<League> getLeagueByCode(String leagueCode);

	boolean addLeague(League league);

}
