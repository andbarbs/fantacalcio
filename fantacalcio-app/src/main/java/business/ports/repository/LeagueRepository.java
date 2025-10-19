package business.ports.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import domain.FantaTeam;
import domain.FantaUser;
import domain.League;
import domain.Player;

public interface LeagueRepository {

	Optional<League> getLeagueByCode(String leagueCode);

	boolean saveLeague(League league);

	Set<League> getLeaguesByMember(FantaUser user);
	
	List<FantaTeam> getAllTeams(League league);

    Set<League> getLeaguesByJournalist(FantaUser journalist);
    
	public Set<Player> getAllInLeague(League league);
}
