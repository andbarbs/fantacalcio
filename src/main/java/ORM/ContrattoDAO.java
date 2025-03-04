package ORM;

import domainModel.Contratto;
import domainModel.FantaTeam;
import domainModel.Giocatore;

import java.util.Iterator;

public interface ContrattoDAO {
    Iterator<Contratto> getRosa(FantaTeam team);
    Iterator<Contratto> getContratti(FantaTeam team, Giocatore.Ruolo ruolo);
    void save(FantaTeam team, Giocatore giocatore);
    void delete(Contratto contratto);
}
