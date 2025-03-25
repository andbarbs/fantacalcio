package businessLogic.abstractDAL;

import domainModel.Contratto;
import domainModel.FantaTeam;
import domainModel.Player;

import java.util.Iterator;

public interface ContrattoDAO {
    Iterator<Contratto> getRosa(FantaTeam team);
    Iterator<Contratto> getContratti(FantaTeam team, Player.Ruolo ruolo);
    void save(FantaTeam team, Player player);
    void delete(Contratto contratto);
}
