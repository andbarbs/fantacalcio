package businessLogic.DAL;

import domainModel.Contratto;
import domainModel.FantaTeam;
import domainModel.PropostaDiScambio;

import java.util.Iterator;

public interface PropostaDAO {
    Iterator<PropostaDiScambio> getProposte(FantaTeam team, Boolean isReceived);
    void save(Contratto proposto, Contratto richiesto);
}
