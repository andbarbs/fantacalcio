package dal.repository.jpa;

import java.util.Optional;

import business.ports.repository.ResultsRepository;
import domain.FantaTeam_;
import domain.League_;
import domain.Match;
import domain.MatchDay_;
import domain.Match_;
import domain.Result;
import domain.Result_;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.Root;

public class JpaResultsRepository extends BaseJpaRepository implements ResultsRepository {

	public JpaResultsRepository(EntityManager em) {
		super(em);
	}

	@Override
	public Optional<Result> getResultFor(Match match) {
    	EntityManager em = getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Result> query = cb.createQuery(Result.class);
        Root<Result> root = query.from(Result.class);        
        
        // deep fetching
        Fetch<Result, Match> matchFetch = root.fetch(Result_.match);        
        matchFetch.fetch(Match_.matchDay).fetch(MatchDay_.league).fetch(League_.admin);
        matchFetch.fetch(Match_.team1).fetch(FantaTeam_.fantaManager);
        matchFetch.fetch(Match_.team2).fetch(FantaTeam_.fantaManager);

        query.select(root).where(
                cb.equal(root.get(Result_.match), match)
        );

        return em.createQuery(query).getResultList().stream().findFirst();
	}

	@Override
	public void saveResult(Result result) {
		getEntityManager().persist(result);
	}

}
