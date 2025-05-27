package concreteJpaRepositories;

import businessLogic.abstractRepositories.AbstractJpaProposalRepository;
import domainModel.FantaTeam;
import domainModel.League;
import domainModel.Proposal;
import domainModel.Proposal_;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.Root;

import java.util.List;

public class JpaProposalRepository implements AbstractJpaProposalRepository {

    public JpaProposalRepository() {}

    @Override
    public void acceptProposal(EntityManager em, Proposal proposal) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaDelete<Proposal> delete = cb.createCriteriaDelete(Proposal.class);
        Root<Proposal> root = delete.from(Proposal.class);

        delete.where(
                cb.and(
                        cb.equal(root.get(Proposal_.offedredContract), proposal.getOffedredContract()),
                        cb.equal(root.get(Proposal_.requestedContract), proposal.getRequestedContract())
                )
        );

        em.createQuery(delete).executeUpdate();
    }


    @Override
    public boolean rejectedProposal(EntityManager em, Proposal proposal) {
        return false;
    }

    @Override
    public List<Proposal> getMyProposals(EntityManager em, League actualLeague, FantaTeam myTeam) {
        return List.of();
    }

    @Override
    public boolean proposalExists(EntityManager em, Proposal proposal) {
        return false;
    }

    @Override
    public boolean saveProposal(EntityManager em, Proposal proposal) {
        return false;
    }
}
