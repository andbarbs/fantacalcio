package businessLogic.abstractDAL.repository;

import domainModel.*;
import jakarta.persistence.EntityManager;

public interface AbstractJpaContractRepository {

	Proposal getProposal(League actualLeague, Player player1, Player player2);

	Contract getContract(EntityManager em, FantaTeam team, Player player);
	
}
