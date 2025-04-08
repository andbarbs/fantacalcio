package businessLogic.presenter;

import domainModel.Proposal;
import businessLogic.abstractView.ProposalView;

public class ProposalPresenter {

	private ProposalView proposalView;
	public ProposalPresenter(ProposalView playerSwitchView) {
		this.proposalView = playerSwitchView;
	}
	
	/*
	// si devono mostrare i giocatori da scambiare: si usa una playerListView/Presenter???
	public void startProposal(Player myPlayer, Player hisPlayer) {
		
	}
	*/
	
	// non so come fare questi 2
	public void addProposal(Proposal proposal) {
		proposalView.addProposal(proposal);
	}
	
	public void acceptProposal(Proposal proposal) {
		proposalView.acceptProposal(proposal);
	}
}
