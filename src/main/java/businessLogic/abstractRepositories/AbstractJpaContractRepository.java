package businessLogic.abstractRepositories;

import domainModel.*;
import jakarta.persistence.EntityManager;

public interface AbstractJpaContractRepository {

	Contract getContract(EntityManager em, FantaTeam team, Player player);
	
}
