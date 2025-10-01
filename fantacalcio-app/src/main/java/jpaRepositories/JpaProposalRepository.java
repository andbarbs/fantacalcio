package jpaRepositories;

import domainModel.Contract;
import domainModel.Contract_;
import domainModel.FantaTeam;
import domainModel.FantaTeam_;
import domainModel.League;
import domainModel.Proposal;
import domainModel.Proposal_;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;

import java.util.List;
import java.util.Optional;

import businessLogic.repositories.ProposalRepository;

public class JpaProposalRepository extends BaseJpaRepository implements ProposalRepository {

    public JpaProposalRepository(EntityManager em) {
		super(em);
		}

    @Override
    public boolean deleteProposal(Proposal proposal) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaDelete<Proposal> delete = cb.createCriteriaDelete(Proposal.class);
        Root<Proposal> root = delete.from(Proposal.class);

        delete.where(
                cb.and(
                        cb.equal(root.get(Proposal_.offeredContract), proposal.getOfferedContract()),
                        cb.equal(root.get(Proposal_.requestedContract), proposal.getRequestedContract())
                )
        );

        return getEntityManager().createQuery(delete).executeUpdate() != 0;
    }

    @Override
    public List<Proposal> getMyProposals(League actualLeague, FantaTeam myTeam) {
        EntityManager em = getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Proposal> query = cb.createQuery(Proposal.class);
        Root<Proposal> root = query.from(Proposal.class);

        Join<Proposal, Contract> offeredJoin = root.join(Proposal_.offeredContract);
        Join<Proposal, Contract> requestedJoin = root.join(Proposal_.requestedContract);
        Join<Contract, FantaTeam> offeredTeam = offeredJoin.join(Contract_.team);
        Join<Contract, FantaTeam> requestedTeam = requestedJoin.join(Contract_.team);

        query.select(root).where(
            cb.and(
                cb.equal(offeredTeam.get(FantaTeam_.league), actualLeague),
                cb.or(
                    cb.equal(offeredTeam, myTeam),
                    cb.equal(requestedTeam, myTeam)
                )
            )
        );

        return em.createQuery(query).getResultList();
    }


    @Override
    public boolean saveProposal(Proposal proposal) {
       getEntityManager().persist(proposal);
       return true;
    }

	@Override
	public Optional<Proposal> getProposal(Contract offeredContract, Contract requestedContract) {
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Proposal> criteriaQuery = cb.createQuery(Proposal.class);
        Root<Proposal> root = criteriaQuery.from(Proposal.class);

        criteriaQuery.where(
                cb.and(
                        cb.equal(root.get(Proposal_.offeredContract), offeredContract),
                        cb.equal(root.get(Proposal_.requestedContract), requestedContract)
                )
        );

        return getEntityManager().createQuery(criteriaQuery).getResultList().stream().findFirst();
	}
}
