package ORM;

import domainModel.FantaTeam;
import domainModel.Giocatore;

import java.util.Iterator;

public interface GiocatoreDAO {
    Iterator<Giocatore> getAllGiocatori();
}
