package business.ports.repository;

import java.util.List;

import domain.FantaTeam;
import domain.FantaUser;
import domain.League;

public interface FantaTeamRepository {

	public List<FantaTeam> getAllTeams(League league);

	public void saveTeam(FantaTeam team);

	public FantaTeam getFantaTeamByUserAndLeague(League league, FantaUser user);

}
