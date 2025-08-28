package jpaRepositories;

import java.util.List;
import java.util.Map;
import java.util.Set;

import businessLogic.repositories.MatchRepository;
import domainModel.FantaTeam;
import domainModel.League;
import domainModel.Match;
import domainModel.MatchDaySerieA;
import jakarta.persistence.EntityManager;

public class JpaMatchRepository extends BaseJpaRepository implements MatchRepository {

	public JpaMatchRepository(EntityManager em) {
		super(em);
	}

	@Override
	public Match getMatchByMatchDay(MatchDaySerieA matchDaySerieA, League league, FantaTeam fantaTeam) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Match> getAllMatchesByMatchDay(MatchDaySerieA matchDaySerieA, League league) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<MatchDaySerieA, Set<Match>> getAllMatches(League league) {
		// TODO Auto-generated method stub
		return null;
	}

}
