package ORM;

import domainModel.FantaLega;
import domainModel.FantaTeam;
import domainModel.Utente;

import java.util.Iterator;

public interface TeamDAO {
    Iterator<FantaTeam> getTeams(FantaLega lega);
    void save(FantaLega lega, Utente fantaAllenatore);
}
