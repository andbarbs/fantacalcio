package business.ports.repository;

import java.util.List;

import domain.FantaTeam;
import domain.League;
import domain.Match;
import domain.MatchDaySerieA;

public interface MatchRepository {

	Match getMatchByMatchDay(MatchDaySerieA matchDaySerieA, League league, FantaTeam fantaTeam);
	
	List<Match> getAllMatchesByMatchDay(MatchDaySerieA matchDaySerieA, League league);

	void saveMatch(Match match);
	
}
