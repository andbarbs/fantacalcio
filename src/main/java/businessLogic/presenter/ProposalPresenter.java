package businessLogic.presenter;

import domainModel.FantaTeam;
import domainModel.League;
import domainModel.Player;
import domainModel.Proposal;
import businessLogic.SessionBean;
import businessLogic.abstractDAL.repository.ContractRepository;
import businessLogic.abstractDAL.repository.ProposalRepository;
import businessLogic.abstractView.ProposalView;

public class ProposalPresenter {

	private ProposalView proposalView;
	private ProposalRepository proposalRepository;
	private ContractRepository contractRepository;

	public ProposalPresenter(ProposalView proposalView, ProposalRepository proposalRepository, ContractRepository contractRepository) {
		this.proposalView = proposalView;
		this.proposalRepository = proposalRepository;
		this.contractRepository = contractRepository;
	}

	public void showAllProposals(League actualLeague, FantaTeam myTeam) {
		proposalView.showAllProposals(proposalRepository.getMyProposals(actualLeague, myTeam));
	}
	
	public void addProposal(League actualLeague, Player player1, Player player2) {
		Proposal proposal = contractRepository.getProposal(actualLeague, player1, player2);
		proposalView.addProposal(proposal);
	}

	public void acceptProposal(Proposal proposal) {
		proposalRepository.acceptedProposal(proposal);
		proposalView.acceptProposal(proposal);
	}
	
	public void rejectProposal(Proposal proposal) {
		proposalRepository.rejectedProposal(proposal);
		proposalView.rejectProposal(proposal);
	}
}
