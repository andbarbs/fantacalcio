package businessLogic.abstractRepositories;

import java.util.List;
import java.util.Map;
import java.util.Set;

import domainModel.League;
import domainModel.Match;
import domainModel.MatchDaySerieA;
import jakarta.persistence.EntityManager;

public interface AbstractJpaMatchRepository {

	Match getNextMatch();

	Match getMatchInDate(String date);

	List<Match> getEveryMatch(); // da togliere

	List<Match> getPlayedMatches();

	List<Match> getFutureMatches();

	Map<MatchDaySerieA, Set<Match>> getAllMatches(EntityManager session, League league);

}
