package businessLogic.repositories;

import java.util.List;
import java.util.Map;
import java.util.Set;

import domainModel.FantaTeam;
import domainModel.League;
import domainModel.Match;
import domainModel.MatchDaySerieA;

public interface MatchRepository {

	Match getNextMatch();

	Match getMatchByMatchDay(MatchDaySerieA matchDaySerieA, League league, FantaTeam fantaTeam);

	List<Match> getEveryMatch(); // da togliere

	List<Match> getPlayedMatches();

	List<Match> getFutureMatches();

	Map<MatchDaySerieA, Set<Match>> getAllMatches(League league);

}
