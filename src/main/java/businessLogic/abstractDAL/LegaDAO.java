package businessLogic.abstractDAL;

import domainModel.League;
import domainModel.Testata;
import domainModel.Utente;

import java.util.Iterator;

public interface LegaDAO {
    Iterator<League> getLegas(Utente utente);
    void save(Utente admin, String name, Testata testata);

}
