package dal.repository.jpa;

import domain.Contract_;
import domain.FantaTeam_;
import domain.Proposal_;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import business.ports.repository.ProposalRepository;
import domain.Contract;
import domain.FantaTeam;
import domain.League_;
import domain.Proposal;

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
    public Set<Proposal> getProposalsFor(FantaTeam myTeam) {
        EntityManager em = getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Proposal> query = cb.createQuery(Proposal.class);
        Root<Proposal> root = query.from(Proposal.class);

        // joining, for query logic
        Join<Proposal, Contract> offeredJoin = root.join(Proposal_.OFFERED_CONTRACT);
        Join<Proposal, Contract> requestedJoin = root.join(Proposal_.REQUESTED_CONTRACT);
        Join<Contract, FantaTeam> offeredTeam = offeredJoin.join(Contract_.TEAM);
        Join<Contract, FantaTeam> requestedTeam = requestedJoin.join(Contract_.TEAM);
        
        // deep fetching
        var offeredTeamFetch = root.fetch(Proposal_.OFFERED_CONTRACT).fetch(Contract_.TEAM);
		offeredTeamFetch.fetch(FantaTeam_.LEAGUE).fetch(League_.ADMIN);
		offeredTeamFetch.fetch(FantaTeam_.FANTA_MANAGER);
        root.fetch(Proposal_.OFFERED_CONTRACT).fetch(Contract_.PLAYER);
        var requestedTeamFetch = root.fetch(Proposal_.REQUESTED_CONTRACT).fetch(Contract_.TEAM);
		requestedTeamFetch.fetch(FantaTeam_.LEAGUE).fetch(League_.ADMIN);
		requestedTeamFetch.fetch(FantaTeam_.FANTA_MANAGER);
        root.fetch(Proposal_.REQUESTED_CONTRACT).fetch(Contract_.PLAYER);

        query.select(root).where(
            cb.and(
                cb.or(
                    cb.equal(offeredTeam, myTeam),
                    cb.equal(requestedTeam, myTeam)
                )
            )
        );

        return em.createQuery(query).getResultStream().collect(Collectors.toSet());
    }


    @Override
    public boolean saveProposal(Proposal proposal) {
       getEntityManager().persist(proposal);
       return true;
    }

	@Override
	public Optional<Proposal> getProposalBy(Contract offeredContract, Contract requestedContract) {
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Proposal> criteriaQuery = cb.createQuery(Proposal.class);
        Root<Proposal> root = criteriaQuery.from(Proposal.class);
        
        // deep fetching
        var offeredTeamFetch = root.fetch(Proposal_.OFFERED_CONTRACT).fetch(Contract_.TEAM);
		offeredTeamFetch.fetch(FantaTeam_.LEAGUE).fetch(League_.ADMIN);
		offeredTeamFetch.fetch(FantaTeam_.FANTA_MANAGER);
        root.fetch(Proposal_.OFFERED_CONTRACT).fetch(Contract_.PLAYER);
        var requestedTeamFetch = root.fetch(Proposal_.REQUESTED_CONTRACT).fetch(Contract_.TEAM);
		requestedTeamFetch.fetch(FantaTeam_.LEAGUE).fetch(League_.ADMIN);
		requestedTeamFetch.fetch(FantaTeam_.FANTA_MANAGER);
        root.fetch(Proposal_.REQUESTED_CONTRACT).fetch(Contract_.PLAYER);

        criteriaQuery.where(
                cb.and(
                        cb.equal(root.get(Proposal_.offeredContract), offeredContract),
                        cb.equal(root.get(Proposal_.requestedContract), requestedContract)
                )
        );

        return getEntityManager().createQuery(criteriaQuery).getResultList().stream().findFirst();
	}
}
