package business.ports.repository;

import java.util.List;
import java.util.Optional;

import domain.FantaTeam;
import domain.FantaUser;
import domain.League;

public interface LeagueRepository {

	Optional<League> getLeagueByCode(String leagueCode);

	boolean saveLeague(League league);

	List<League> getLeaguesByUser(FantaUser user);
	
	List<FantaTeam> getAllTeams(League league);
	
}
