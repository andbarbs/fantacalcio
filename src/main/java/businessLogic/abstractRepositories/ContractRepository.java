package businessLogic.abstractRepositories;

import domainModel.*;

public interface ContractRepository {

	Contract getContract(FantaTeam team, Player player);
	
}
