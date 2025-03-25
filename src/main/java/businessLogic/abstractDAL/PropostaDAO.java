package businessLogic.abstractDAL;

import domainModel.Contratto;
import domainModel.FantaTeam;
import domainModel.Proposal;

import java.util.Iterator;

public interface PropostaDAO {
    Iterator<Proposal> getProposte(FantaTeam team, Boolean isReceived);
    void save(Contratto proposto, Contratto richiesto);
}
