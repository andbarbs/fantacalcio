package jpaRepositories;

import domainModel.FantaTeam;
import domainModel.League;
import domainModel.Proposal;
import domainModel.Proposal_;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import java.util.List;

import businessLogic.repositories.ProposalRepository;

public class JpaProposalRepository extends BaseJpaRepository implements ProposalRepository {

    public JpaProposalRepository(EntityManager em) {
		super(em);
		}

    @Override
    public void deleteProposal(Proposal proposal) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaDelete<Proposal> delete = cb.createCriteriaDelete(Proposal.class);
        Root<Proposal> root = delete.from(Proposal.class);

        delete.where(
                cb.and(
                        cb.equal(root.get(Proposal_.offeredContract), proposal.getOfferedContract()),
                        cb.equal(root.get(Proposal_.requestedContract), proposal.getRequestedContract())
                )
        );

        getEntityManager().createQuery(delete).executeUpdate();
    }

    @Override
    public List<Proposal> getMyProposals(League actualLeague, FantaTeam myTeam) {
    	EntityManager entityManager = getEntityManager();
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Proposal> criteriaQuery = criteriaBuilder.createQuery(Proposal.class);
		Root<Proposal> root = criteriaQuery.from(Proposal.class);
		criteriaQuery.select(root);

		return entityManager.createQuery(criteriaQuery).getResultList();
    }

    //TODO check if it is useful
    @Override
    public boolean proposalExists(Proposal proposal) {
        return false;
    }

    @Override
    public void saveProposal(Proposal proposal) {
        getEntityManager().persist(proposal);;
    }
}
