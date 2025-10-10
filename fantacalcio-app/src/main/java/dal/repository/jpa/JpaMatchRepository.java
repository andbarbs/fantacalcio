package dal.repository.jpa;

import domain.Match_;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import java.util.List;

import business.ports.repository.MatchRepository;
import domain.FantaTeam;
import domain.League;
import domain.Match;
import domain.MatchDay;

public class JpaMatchRepository extends BaseJpaRepository implements MatchRepository {

    public JpaMatchRepository(EntityManager em) {super(em);}

    @Override
    public Match getMatchByMatchDay(MatchDay matchDaySerieA, League league, FantaTeam fantaTeam) {
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
    public List<Match> getAllMatchesByMatchDay(MatchDay matchDaySerieA, League league) {
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
