package businessLogic.abstractDAL.repository;

import java.util.List;

import domainModel.Match;

public interface MatchRepository {

	Match getNextMatch();

	Match getMatchInDate(String date);

	List<Match> getEveryMatch();

	List<Match> getPlayedMatches();

	List<Match> getFutureMatches();

}
