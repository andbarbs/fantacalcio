package jpaRepositories;

import businessLogic.repositories.ContractRepository;
import domainModel.Contract;
import domainModel.FantaTeam;
import domainModel.Player;
import jakarta.persistence.EntityManager;

public class JpaContractRepository extends BaseJpaRepository implements ContractRepository {
    public JpaContractRepository(EntityManager em) {
        super(em);
    }
    @Override
    public Contract getContract(FantaTeam team, Player player) {

        return null;
    }

    @Override
    public void deleteContract(Contract contract) {

    }

    @Override
    public void saveContract(Contract contract) {

    }
}
