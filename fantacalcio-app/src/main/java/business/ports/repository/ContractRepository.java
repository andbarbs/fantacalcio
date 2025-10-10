package business.ports.repository;

import java.util.Optional;

import domain.*;

public interface ContractRepository {

	Optional<Contract> getContract(FantaTeam team, Player player);

	void deleteContract(Contract contract);

	void saveContract(Contract contract);

}
