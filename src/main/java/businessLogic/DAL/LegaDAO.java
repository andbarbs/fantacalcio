package businessLogic.DAL;

import domainModel.FantaLega;
import domainModel.Testata;
import domainModel.Utente;

import java.util.Iterator;

public interface LegaDAO {
    Iterator<FantaLega> getLegas(Utente utente);
    void save(Utente admin, String name, Testata testata);

}
