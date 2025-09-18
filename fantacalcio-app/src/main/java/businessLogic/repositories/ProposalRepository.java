package businessLogic.repositories;

import java.util.List;
import java.util.Optional;

import domainModel.Contract;
import domainModel.FantaTeam;
import domainModel.League;
import domainModel.MatchDaySerieA;
import domainModel.Proposal;

public interface ProposalRepository {

	void deleteProposal(Proposal proposal);

	List<Proposal> getMyProposals(League actualLeague, FantaTeam myTeam);

	boolean proposalExists(Proposal proposal);

	boolean saveProposal(Proposal proposal);

	Optional<MatchDaySerieA> getProposal(Contract contract, Contract contract2);

}
