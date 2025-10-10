package dal.repository.jpa;

import java.util.Optional;

import business.ports.repository.ResultsRepository;
import domain.Match;
import domain.Result;
import domain.Result_;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

public class JpaResultsRepository extends BaseJpaRepository implements ResultsRepository {

	public JpaResultsRepository(EntityManager em) {
		super(em);
	}

	@Override
	public Optional<Result> getResult(Match match) {
    	EntityManager em = getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Result> query = cb.createQuery(Result.class);
        Root<Result> root = query.from(Result.class);

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
