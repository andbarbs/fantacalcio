package businessLogic.abstractDAL.repository;

import java.util.List;

import domainModel.FantaTeam;
import domainModel.League;
import domainModel.Proposal;

public interface ProposalRepository {

	void acceptedProposal(Proposal proposal);

	void rejectedProposal(Proposal proposal);

	List<Proposal> getMyProposals(League actualLeague, FantaTeam myTeam);	

}
