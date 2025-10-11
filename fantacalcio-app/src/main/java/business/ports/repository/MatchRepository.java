package business.ports.repository;

import java.util.List;

import domain.FantaTeam;
import domain.League;
import domain.Match;
import domain.MatchDay;

public interface MatchRepository {

	Match getMatchByMatchDay(MatchDay matchDay, League league, FantaTeam fantaTeam);
	
	List<Match> getAllMatchesByMatchDay(MatchDay matchDay, League league);

	void saveMatch(Match match);
	
}
