package businessLogic.repositories;

import domainModel.*;

public interface ContractRepository {

	Contract getContract(FantaTeam team, Player player);
	
}
