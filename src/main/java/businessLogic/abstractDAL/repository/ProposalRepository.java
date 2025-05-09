package businessLogic.abstractDAL.repository;

import domainModel.Proposal;

public interface ProposalRepository {

	void acceptedProposal(Proposal proposal);

	void rejectedProposal(Proposal proposal);	

}
