package jpaRepositories;

import businessLogic.repositories.MatchRepository;
import domainModel.FantaTeam;
import domainModel.League;
import domainModel.Match;
import domainModel.MatchDaySerieA;
import domainModel.Match_;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import java.util.List;

public class JpaMatchRepository extends BaseJpaRepository implements MatchRepository {

    public JpaMatchRepository(EntityManager em) {super(em);}

    @Override
    public Match getMatchByMatchDay(MatchDaySerieA matchDaySerieA, League league, FantaTeam fantaTeam) {
    	EntityManager em = getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Match> query = cb.createQuery(Match.class);
        Root<Match> root = query.from(Match.class);

        query.select(root).where(
                cb.and(
                    cb.equal(root.get(Match_.matchDaySerieA), matchDaySerieA),
                    cb.or(
                        cb.equal(root.get(Match_.team1), fantaTeam),
                        cb.equal(root.get(Match_.team2), fantaTeam)))
        );

        return em.createQuery(query).getResultList().getFirst();
    }

    @Override
    public List<Match> getAllMatchesByMatchDay(MatchDaySerieA matchDaySerieA, League league) {
    	EntityManager em = getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Match> query = cb.createQuery(Match.class);
        Root<Match> root = query.from(Match.class);

        query.select(root).where(
                cb.and(
                    cb.equal(root.get(Match_.matchDaySerieA), matchDaySerieA))
        );

        return em.createQuery(query).getResultList();
    }

    @Override
    public void saveMatch(Match match) {
    	getEntityManager().persist(match);
    }
}
