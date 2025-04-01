package businessLogic.abstractDAL;

import domainModel.FantaTeam;
import domainModel.League;
import domainModel.Match;

import java.time.LocalDateTime;
import java.util.Iterator;

public interface MatchDao {
    Iterator<Match> getMatches(League lega);
    Match getNextMatch(FantaTeam team, LocalDateTime dateAndTime);
}
