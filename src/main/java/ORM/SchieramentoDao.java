package ORM;

import domainModel.FantaTeam;
import domainModel.Giocatore;
import domainModel.Match;
import domainModel.Schieramento;

import java.util.Iterator;

public interface SchieramentoDao {
    Iterator<Schieramento> getSchieramento(Match match, FantaTeam team);
    void save(Match match, Giocatore giocatore, Boolean titolarita, float voto);
}
