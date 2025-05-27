package businessLogic.abstractRepositories;

import domainModel.League;
import jakarta.persistence.EntityManager;

public interface AbstractJpaLeagueRepository {

	League getLeagueByCode(EntityManager em, String leagueCode);

	boolean add(League league);

}
