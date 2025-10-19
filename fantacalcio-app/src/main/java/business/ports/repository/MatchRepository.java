package business.ports.repository;

import java.util.List;
import java.util.Optional;

import domain.FantaTeam;
import domain.Match;
import domain.MatchDay;

public interface MatchRepository {

	Optional<Match> getMatchBy(MatchDay matchDay, FantaTeam fantaTeam);
	
	List<Match> getAllMatchesIn(MatchDay matchDay);

	void saveMatch(Match match);
	
}
