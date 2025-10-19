package dal.repository.jpa;

import domain.Contract_;
import domain.FantaTeam_;
import domain.Proposal_;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Fetch;
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
        Join<Proposal, Contract> offeredJoin = root.join(Proposal_.offeredContract);
        Join<Proposal, Contract> requestedJoin = root.join(Proposal_.requestedContract);
        Join<Contract, FantaTeam> offeredTeam = offeredJoin.join(Contract_.team);
        Join<Contract, FantaTeam> requestedTeam = requestedJoin.join(Contract_.team);
        
        // deep fetching
        Fetch<Contract, FantaTeam> offeredTeamFetch = root.fetch(Proposal_.offeredContract).fetch(Contract_.team);
		offeredTeamFetch.fetch(FantaTeam_.league).fetch(League_.admin);
		offeredTeamFetch.fetch(FantaTeam_.fantaManager);
        root.fetch(Proposal_.offeredContract).fetch(Contract_.player);
        Fetch<Contract, FantaTeam> requestedTeamFetch = root.fetch(Proposal_.requestedContract).fetch(Contract_.team);
		requestedTeamFetch.fetch(FantaTeam_.league).fetch(League_.admin);
		requestedTeamFetch.fetch(FantaTeam_.fantaManager);
        root.fetch(Proposal_.requestedContract).fetch(Contract_.player);

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
    public void saveProposal(Proposal proposal) {
       getEntityManager().persist(proposal);

    }

	@Override
	public Optional<Proposal> getProposalBy(Contract offeredContract, Contract requestedContract) {
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Proposal> criteriaQuery = cb.createQuery(Proposal.class);
        Root<Proposal> root = criteriaQuery.from(Proposal.class);
        
        // deep fetching
        Fetch<Contract, FantaTeam> offeredTeamFetch = root.fetch(Proposal_.offeredContract).fetch(Contract_.team);
		offeredTeamFetch.fetch(FantaTeam_.league).fetch(League_.admin);
		offeredTeamFetch.fetch(FantaTeam_.fantaManager);
        root.fetch(Proposal_.offeredContract).fetch(Contract_.player);
        Fetch<Contract, FantaTeam> requestedTeamFetch = root.fetch(Proposal_.requestedContract).fetch(Contract_.team);
		requestedTeamFetch.fetch(FantaTeam_.league).fetch(League_.admin);
		requestedTeamFetch.fetch(FantaTeam_.fantaManager);
        root.fetch(Proposal_.requestedContract).fetch(Contract_.player);

        criteriaQuery.where(
                cb.and(
                        cb.equal(root.get(Proposal_.offeredContract), offeredContract),
                        cb.equal(root.get(Proposal_.requestedContract), requestedContract)
                )
        );

        return getEntityManager().createQuery(criteriaQuery).getResultList().stream().findFirst();
	}
}
