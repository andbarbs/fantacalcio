package presenter;

import model.Proposal;
import repository.ProposalRepository;
import view.ProposalView;

public class ProposalPresenter {

	private ProposalView proposalView;
	private ProposalRepository proposalRepository;

	public ProposalPresenter(ProposalView proposalView, ProposalRepository proposalRepository) {
		this.proposalView = proposalView;
		this.proposalRepository = proposalRepository;
	}
	
	public void startProposal() {
		// si devono mostrare i giocatori da scambiare: si usa una playerListView/Presenter???
	}
	
	// non so come fare questi 2
	public void addProposal(Proposal proposal) {
		proposalView.addProposal(proposal);
	}
	
	public void acceptProposal(Proposal proposal) {
		proposalView.acceptProposal(proposal);
	}
}
