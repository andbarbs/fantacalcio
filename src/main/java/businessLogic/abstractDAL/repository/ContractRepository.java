package businessLogic.abstractDAL.repository;

import domainModel.League;
import domainModel.Player;
import domainModel.Proposal;

public interface ContractRepository {

	Proposal getProposal(League actualLeague, Player player1, Player player2);
	
}
