package jpaRepositories;

import java.util.List;

import businessLogic.abstractRepositories.MatchDayRepository;
import domainModel.MatchDaySerieA;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

public class JpaMatchDayRepository extends BaseJpaRepository implements MatchDayRepository {
		
	public JpaMatchDayRepository(EntityManager em) {
		super(em);
	}

	@Override
	public List<MatchDaySerieA> getAllMatchDays() {		
		EntityManager entityManager = getEntityManager();
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<MatchDaySerieA> criteriaQuery = criteriaBuilder.createQuery(MatchDaySerieA.class);
		Root<MatchDaySerieA> root = criteriaQuery.from(MatchDaySerieA.class);
		criteriaQuery.select(root);

		return entityManager.createQuery(criteriaQuery).getResultList();	
	}

}
