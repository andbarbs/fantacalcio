package businessLogic.abstractRepositories;

import java.util.Set;

import domainModel.FantaTeam;
import domainModel.League;

public interface TeamRepository {

	public Set<FantaTeam> getAllTeams(League league);

	public boolean saveTeam(FantaTeam team);

}
