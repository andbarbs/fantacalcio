package businessLogic.repositories;

import java.util.List;

import domainModel.FantaTeam;
import domainModel.League;
import domainModel.Proposal;

public interface ProposalRepository {

	void acceptProposal(Proposal proposal);

	boolean rejectedProposal(Proposal proposal);

	List<Proposal> getMyProposals(League actualLeague, FantaTeam myTeam);

	boolean proposalExists(Proposal proposal);

	boolean saveProposal(Proposal proposal);

}
