package businessLogic.abstractRepositories;

import java.util.List;

import domainModel.FantaTeam;
import domainModel.League;
import domainModel.Proposal;
import jakarta.persistence.EntityManager;

public interface AbstractJpaProposalRepository {

	void acceptProposal(EntityManager em, Proposal proposal);

	boolean rejectedProposal(EntityManager em, Proposal proposal);

	List<Proposal> getMyProposals(EntityManager em, League actualLeague, FantaTeam myTeam);

	boolean proposalExists(EntityManager em, Proposal proposal);

	boolean saveProposal(EntityManager em, Proposal proposal);

}
