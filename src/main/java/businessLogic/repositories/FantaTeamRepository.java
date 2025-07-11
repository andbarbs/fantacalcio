package businessLogic.repositories;

import java.util.List;
import domainModel.FantaTeam;
import domainModel.FantaUser;
import domainModel.League;

public interface FantaTeamRepository {

	public List<FantaTeam> getAllTeams(League league);

	public void saveTeam(FantaTeam team);

	public FantaTeam getFantaTeamByUserAndLeague(League league, FantaUser user);

}
