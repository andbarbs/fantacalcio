package jpaRepositories;

import java.util.Optional;

import businessLogic.repositories.ContractRepository;
import domainModel.Contract;
import domainModel.Contract_;
import domainModel.FantaTeam;
import domainModel.Player;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

public class JpaContractRepository extends BaseJpaRepository implements ContractRepository {
    public JpaContractRepository(EntityManager em) {
        super(em);
    }
    @Override
    public Optional<Contract> getContract(FantaTeam team, Player player) {
    	EntityManager em = getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Contract> query = cb.createQuery(Contract.class);
        Root<Contract> root = query.from(Contract.class);

        query.select(root).where(
                cb.equal(root.get(Contract_.team), team),
                cb.equal(root.get(Contract_.player), player)
        );

        return em.createQuery(query).getResultList().stream().findFirst();
    }

    @Override
    public void deleteContract(Contract contract) {
    	CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaDelete<Contract> delete = cb.createCriteriaDelete(Contract.class);
        Root<Contract> root = delete.from(Contract.class);

        delete.where(
                cb.and(
                        cb.equal(root.get(Contract_.player), contract.getPlayer()),
                        cb.equal(root.get(Contract_.team), contract.getTeam())
                )
        );

        getEntityManager().createQuery(delete).executeUpdate();
    }

    @Override
    public void saveContract(Contract contract) {
    	getEntityManager().persist(contract);
    }
}
