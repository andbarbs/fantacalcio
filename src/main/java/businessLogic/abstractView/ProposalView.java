package businessLogic.abstractView;

import domainModel.Proposal;

public interface ProposalView {

	void addProposal(Proposal proposal);

	void acceptProposal(Proposal proposal);

	void rejectProposal(Proposal proposal);

}
