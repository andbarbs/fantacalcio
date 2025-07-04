package businessLogic.repositories;

import java.util.List;
import java.util.Set;

import domainModel.FantaTeam;
import domainModel.FantaUser;
import domainModel.League;

public interface TeamRepository {

	public List<FantaTeam> getAllTeams(League league);

	public void saveTeam(FantaTeam team);

	public FantaTeam getFantaTeamByUserAndLeague(League league, FantaUser user);

}
