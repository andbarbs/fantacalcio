package businessLogic.repositories;

import java.util.List;
import domainModel.FantaTeam;
import domainModel.League;
import domainModel.Match;
import domainModel.MatchDaySerieA;

public interface MatchRepository {

	Match getMatchByMatchDay(MatchDaySerieA matchDaySerieA, League league, FantaTeam fantaTeam);
	
	List<Match> getAllMatchesByMatchDay(MatchDaySerieA matchDaySerieA, League league);

	void saveMatch(Match match);
	
}
