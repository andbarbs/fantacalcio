package business.ports.repository;

import java.util.List;
import java.util.Optional;

import domain.Contract;
import domain.FantaTeam;
import domain.League;
import domain.Proposal;

public interface ProposalRepository {

	boolean deleteProposal(Proposal proposal);

	List<Proposal> getMyProposals(League actualLeague, FantaTeam myTeam);

	Optional<Proposal> getProposal(Contract offeredContract, Contract requestedContract);

	boolean saveProposal(Proposal proposal);

}
