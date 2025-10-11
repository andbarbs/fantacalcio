package business.ports.repository;

import java.util.Optional;
import java.util.Set;

import domain.FantaTeam;
import domain.FantaUser;
import domain.League;

public interface FantaTeamRepository {

	public Set<FantaTeam> getAllTeams(League league);

	public void saveTeam(FantaTeam team);

	public Optional<FantaTeam> getFantaTeamByUserAndLeague(League league, FantaUser user);

}
