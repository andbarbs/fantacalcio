package businessLogic.repositories;

import domainModel.FantaTeam;
import domainModel.FantaUser;
import domainModel.League;

import java.util.List;
import java.util.Optional;

public interface LeagueRepository {

	Optional<League> getLeagueByCode(String leagueCode);

	boolean saveLeague(League league);

	List<League> getLeaguesByUser(FantaUser user);
	
	List<FantaTeam> getAllTeams(League league);
	
}
