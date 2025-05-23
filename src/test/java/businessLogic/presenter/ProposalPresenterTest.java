package businessLogic.presenter;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import businessLogic.abstractDAL.repository.ContractRepository;
import businessLogic.abstractDAL.repository.AbstarctJpaProposalRepository;
import businessLogic.abstractView.ProposalView;
import domainModel.League;
import domainModel.Proposal;
import domainModel.Player;

class ProposalPresenterTest {

	private ProposalPresenter proposalPresenter;
	private AbstarctJpaProposalRepository abstarctJpaProposalRepository;
	private ContractRepository contractRepository;
	private ProposalView proposalView;
	
	@BeforeEach
	public void setup() {
		contractRepository = mock(ContractRepository.class);
		abstarctJpaProposalRepository = mock(AbstarctJpaProposalRepository.class);
		proposalView = mock(ProposalView.class);
		proposalPresenter = new ProposalPresenter(proposalView, abstarctJpaProposalRepository, contractRepository);
	}
	
	@Test
	void testAddProposal() {
		Proposal proposal = new Proposal();
		League league = new League();
		Player player1 = new Player();
		Player player2 = new Player();
		when(contractRepository.getProposal(league, player1, player2)).thenReturn(proposal);
		proposalPresenter.addProposal(league, player1, player2);
		verify(proposalView).addProposal(proposal);
	}
	
	@Test
	void testAcceptProposal() {
		Proposal proposal = new Proposal();
		proposalPresenter.acceptProposal(proposal);
		verify(abstarctJpaProposalRepository).acceptedProposal(, proposal);
		verify(proposalView).acceptProposal(proposal);
	}
	
	@Test
	void testRejectProposal() {
		Proposal proposal = new Proposal();
		proposalPresenter.rejectProposal(proposal);
		verify(abstarctJpaProposalRepository).rejectedProposal(, proposal);
		verify(proposalView).rejectProposal(proposal);
	}

}
