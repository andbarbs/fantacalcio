package jpaRepositories;

import businessLogic.repositories.MatchRepository;
import domainModel.FantaTeam;
import domainModel.League;
import domainModel.Match;
import domainModel.MatchDaySerieA;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class JpaMatchRepository extends BaseJpaRepository implements MatchRepository {

    public JpaMatchRepository(EntityManager em) {super(em);}

    @Override
    public Match getMatchByMatchDay(MatchDaySerieA matchDaySerieA, League league, FantaTeam fantaTeam) {
        return null;
    }

    @Override
    public List<Match> getAllMatchesByMatchDay(MatchDaySerieA matchDaySerieA, League league) {
        return List.of();
    }

    @Override
    public Map<MatchDaySerieA, Set<Match>> getAllMatches(League league) {
        return Map.of();
    }

    @Override
    public void saveMatch(Match match) {

    }
}
