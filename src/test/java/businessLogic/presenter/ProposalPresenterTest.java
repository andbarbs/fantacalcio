package businessLogic.presenter;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import businessLogic.abstractRepositories.ContractRepository;
import businessLogic.abstractRepositories.ProposalRepository;
import presenters.abstractViews.ProposalView;
import domainModel.League;
import domainModel.Proposal;
import domainModel.Player;
import presenters.ProposalPresenter;

class ProposalPresenterTest {
/*
	private ProposalPresenter proposalPresenter;
	private AbstractJpaProposalRepository abstractJpaProposalRepository;
	private AbstractJpaContractRepository abstractJpaContractRepository;
	private ProposalView proposalView;
	
	@BeforeEach
	public void setup() {
		abstractJpaContractRepository = mock(AbstractJpaContractRepository.class);
		abstractJpaProposalRepository = mock(AbstractJpaProposalRepository.class);
		proposalView = mock(ProposalView.class);
		proposalPresenter = new ProposalPresenter(proposalView, abstractJpaProposalRepository, abstractJpaContractRepository);
	}
	
	@Test
	void testAddProposal() {
		Proposal proposal = new Proposal();
		League league = new League();
		Player player1 = new Player();
		Player player2 = new Player();
		when(abstractJpaContractRepository.getProposal(league, player1, player2)).thenReturn(proposal);
		proposalPresenter.addProposal(league, player1, player2);
		verify(proposalView).addProposal(proposal);
	}
	
	@Test
	void testAcceptProposal() {
		Proposal proposal = new Proposal();
		proposalPresenter.acceptProposal(proposal);
		verify(abstractJpaProposalRepository).acceptedProposal(, proposal);
		verify(proposalView).acceptProposal(proposal);
	}
	
	@Test
	void testRejectProposal() {
		Proposal proposal = new Proposal();
		proposalPresenter.rejectProposal(proposal);
		verify(abstractJpaProposalRepository).rejectedProposal(, proposal);
		verify(proposalView).rejectProposal(proposal);
	}

 */

}
