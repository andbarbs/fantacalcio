package businessLogic.abstractDAL;

import domainModel.FantaTeam;
import domainModel.Player;
import domainModel.Match;
import domainModel.LineUp;

import java.util.Iterator;

public interface LineUpDAO {
    Iterator<LineUp> getLineUps(Match match, FantaTeam team);
    void save(Match match, Player player, Boolean starter, float mark);
}
