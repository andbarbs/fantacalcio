package businessLogic.repositories;

import java.util.List;
import java.util.Optional;

import domainModel.Contract;
import domainModel.FantaTeam;
import domainModel.League;
import domainModel.Proposal;

public interface ProposalRepository {

	boolean deleteProposal(Proposal proposal);

	List<Proposal> getMyProposals(League actualLeague, FantaTeam myTeam);

	Optional<Proposal> getProposal(Contract offeredContract, Contract requestedContract);

	boolean saveProposal(Proposal proposal);

}
