package presenters.abstractViews;

import java.util.List;

import domain.Proposal;

public interface ProposalView {

	void addProposal(Proposal proposal);

	void acceptProposal(Proposal proposal);

	void rejectProposal(Proposal proposal);

	void showAllProposals(List<Proposal> myProposals);

}
