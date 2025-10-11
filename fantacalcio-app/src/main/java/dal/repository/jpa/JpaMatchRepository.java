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
        root.fetch(Match_.MATCH_DAY).fetch(MatchDay_.LEAGUE).fetch(League_.ADMIN);
        root.fetch(Match_.TEAM1).fetch(FantaTeam_.FANTA_MANAGER);
        root.fetch(Match_.TEAM2).fetch(FantaTeam_.FANTA_MANAGER);

        query.select(root).where(
                cb.and(
                    cb.equal(root.get(Match_.MATCH_DAY), matchDay),
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
        
        root.fetch(Match_.MATCH_DAY).fetch(MatchDay_.LEAGUE).fetch(League_.ADMIN);
        root.fetch(Match_.TEAM1).fetch(FantaTeam_.FANTA_MANAGER);
        root.fetch(Match_.TEAM2).fetch(FantaTeam_.FANTA_MANAGER);

        query.select(root).where(
                cb.and(
                    cb.equal(root.get(Match_.MATCH_DAY), matchDay))
        );

        return em.createQuery(query).getResultList();
    }

    @Override
    public void saveMatch(Match match) {
    	getEntityManager().persist(match);
    }
}
