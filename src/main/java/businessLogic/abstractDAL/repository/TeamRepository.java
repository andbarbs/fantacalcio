package businessLogic.abstractDAL.repository;

import java.util.List;

import domainModel.FantaTeam;

public interface TeamRepository {

	public List<FantaTeam> getAllTeams();

}
