package businessLogic.abstractDAL.repository;

import java.util.Set;

import domainModel.FantaTeam;
import domainModel.League;
import jakarta.persistence.EntityManager;

public interface AbstractJpaTeamRepository {

	public Set<FantaTeam> getAllTeams(EntityManager em, League league);

}
