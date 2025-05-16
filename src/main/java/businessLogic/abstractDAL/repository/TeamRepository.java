package businessLogic.abstractDAL.repository;

import java.util.List;

import domainModel.FantaTeam;
import domainModel.League;

public interface TeamRepository {

	public List<FantaTeam> getAllTeams(League league);

}
