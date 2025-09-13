package businessLogic.repositories;

import domainModel.FantaUser;
import domainModel.League;

import java.util.Optional;
import java.util.Set;

public interface LeagueRepository {

	Optional<League> getLeagueByCode(String leagueCode);

	boolean addLeague(League league);

	Set<League> getLeaguesByUser(FantaUser user);

}
