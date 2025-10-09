package business.ports.repository;

import java.util.List;

import domain.FantaTeam;
import domain.League;
import domain.Match;
import domain.MatchDay;

public interface MatchRepository {

	Match getMatchByMatchDay(MatchDay matchDaySerieA, League league, FantaTeam fantaTeam);
	
	List<Match> getAllMatchesByMatchDay(MatchDay matchDaySerieA, League league);

	void saveMatch(Match match);
	
}
