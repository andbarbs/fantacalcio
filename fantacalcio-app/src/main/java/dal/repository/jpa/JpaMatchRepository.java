package dal.repository.jpa;

import domain.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import java.util.List;
import java.util.Optional;

import business.ports.repository.MatchRepository;

public class JpaMatchRepository extends BaseJpaRepository implements MatchRepository {

    public JpaMatchRepository(EntityManager em) {super(em);}

    @Override
    public Optional<Match> getMatchBy(MatchDay matchDay, FantaTeam fantaTeam) {
    	EntityManager em = getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Match> query = cb.createQuery(Match.class);
        Root<Match> root = query.from(Match.class);
        
        // deep fetching
        root.fetch(Match_.matchDay).fetch(MatchDay_.league).fetch(League_.admin);
        root.fetch(Match_.team1).fetch(FantaTeam_.fantaManager);
        root.fetch(Match_.team2).fetch(FantaTeam_.fantaManager);

        query.select(root).where(
                cb.and(
                    cb.equal(root.get(Match_.matchDay), matchDay),
                    cb.or(
                        cb.equal(root.get(Match_.team1), fantaTeam),
                        cb.equal(root.get(Match_.team2), fantaTeam)))
        );

        return em.createQuery(query).getResultStream().findFirst();
    }

    @Override
    public List<Match> getAllMatchesIn(MatchDay matchDay) {
    	EntityManager em = getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Match> query = cb.createQuery(Match.class);
        Root<Match> root = query.from(Match.class);
        
        // deep fetching
        root.fetch(Match_.matchDay).fetch(MatchDay_.league).fetch(League_.admin);
        root.fetch(Match_.team1).fetch(FantaTeam_.fantaManager);
        root.fetch(Match_.team2).fetch(FantaTeam_.fantaManager);

        query.select(root).where(
                cb.and(
                    cb.equal(root.get(Match_.matchDay), matchDay))
        );

        return em.createQuery(query).getResultList();
    }

    @Override
    public void saveMatch(Match match) {
    	getEntityManager().persist(match);
    }
}
