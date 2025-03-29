package businessLogic.DAL;

import domainModel.FantaLega;
import domainModel.FantaTeam;
import domainModel.Match;
import java.time.LocalDateTime;
import java.util.Iterator;

public interface MatchDao {
    Iterator<Match> getMatches(FantaLega lega);
    Match getNextMatch(FantaTeam team, LocalDateTime dateAndTime);
}
