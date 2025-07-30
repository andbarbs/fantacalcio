package businessLogic.repositories;

import java.util.Optional;

import domainModel.*;

public interface ContractRepository {

	Optional<Contract> getContract(FantaTeam team, Player player);

	void deleteContract(Contract contract);

	void saveContract(Contract contract);

}
