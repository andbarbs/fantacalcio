package businessLogic.repositories;

import java.util.List;
import java.util.Map;
import java.util.Set;

import domainModel.FantaTeam;
import domainModel.League;
import domainModel.Match;
import domainModel.MatchDaySerieA;

public interface MatchRepository {

	Match getMatchByMatchDay(MatchDaySerieA matchDaySerieA, League league, FantaTeam fantaTeam);
	
	List<Match> getAllMatchesByMatchDay(MatchDaySerieA matchDaySerieA, League league);

	Map<MatchDaySerieA, Set<Match>> getAllMatches(League league);
	
}
