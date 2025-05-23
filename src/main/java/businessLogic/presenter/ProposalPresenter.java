package businessLogic.presenter;

import domainModel.FantaTeam;
import domainModel.League;
import domainModel.Player;
import domainModel.Proposal;
import businessLogic.abstractDAL.repository.ContractRepository;
import businessLogic.abstractDAL.repository.AbstarctJpaProposalRepository;
import businessLogic.abstractView.ProposalView;

public class ProposalPresenter {

	private ProposalView proposalView;
	private AbstarctJpaProposalRepository abstarctJpaProposalRepository;
	private ContractRepository contractRepository;

	public ProposalPresenter(ProposalView proposalView, AbstarctJpaProposalRepository abstarctJpaProposalRepository, ContractRepository contractRepository) {
		this.proposalView = proposalView;
		this.abstarctJpaProposalRepository = abstarctJpaProposalRepository;
		this.contractRepository = contractRepository;
	}

	public void showAllProposals(League actualLeague, FantaTeam myTeam) {
		proposalView.showAllProposals(abstarctJpaProposalRepository.getMyProposals(, actualLeague, myTeam));
	}
	
	public void addProposal(League actualLeague, Player player1, Player player2) {
		Proposal proposal = contractRepository.getProposal(actualLeague, player1, player2);
		proposalView.addProposal(proposal);
	}

	public void acceptProposal(Proposal proposal) {
		abstarctJpaProposalRepository.acceptedProposal(, proposal);
		proposalView.acceptProposal(proposal);
	}
	
	public void rejectProposal(Proposal proposal) {
		abstarctJpaProposalRepository.rejectedProposal(, proposal);
		proposalView.rejectProposal(proposal);
	}
}
