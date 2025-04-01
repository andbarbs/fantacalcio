package businessLogic.abstractDAL;

import domainModel.Contract;
import domainModel.FantaTeam;
import domainModel.Proposal;

import java.util.Iterator;

public interface ProposalDAO {
    Iterator<Proposal> getProposal(FantaTeam team, Boolean isReceived);
    void save(Contract proposed, Contract requested);
}
