package businessLogic.presenter;

import domainModel.Proposal;
import businessLogic.abstractDAL.repository.ProposalRepository;
import businessLogic.abstractView.PlayerSwitchView;

public class PlayerSwitchPresenter {

	private PlayerSwitchView playerSwitchView;
	private ProposalRepository proposalRepository;

	public PlayerSwitchPresenter(PlayerSwitchView playerSwitchView, ProposalRepository proposalRepository) {
		this.playerSwitchView = playerSwitchView;
		this.proposalRepository = proposalRepository;
	}
	
	public void startProposal() {
		// si devono mostrare i giocatori da scambiare: si usa una playerListView/Presenter???
	}
	
	// non so come fare questi 2
	public void addProposal(Proposal proposal) {
		playerSwitchView.addProposal(proposal);
	}
	
	public void acceptProposal(Proposal proposal) {
		playerSwitchView.acceptProposal(proposal);
	}
}
