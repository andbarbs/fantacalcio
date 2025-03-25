package businessLogic.abstractDAL;

import domainModel.FantaTeam;
import domainModel.Player;
import domainModel.Match;
import domainModel.Schieramento;

import java.util.Iterator;

public interface SchieramentoDao {
    Iterator<Schieramento> getSchieramento(Match match, FantaTeam team);
    void save(Match match, Player player, Boolean titolarita, float voto);
}
