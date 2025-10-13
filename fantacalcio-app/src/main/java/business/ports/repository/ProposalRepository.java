package business.ports.repository;

import java.util.Optional;
import java.util.Set;

import domain.Contract;
import domain.FantaTeam;
import domain.Proposal;

public interface ProposalRepository {

	boolean deleteProposal(Proposal proposal);

	// TODO if you want a List, introduce Proposal ordering!
	Set<Proposal> getProposalsFor(FantaTeam myTeam);

	Optional<Proposal> getProposalBy(Contract offeredContract, Contract requestedContract);

	void saveProposal(Proposal proposal);

}
