package dal.repository.jpa;

import java.util.List;

import business.ports.repository.FieldingRepository;
import domain.Fielding;
import domain.LineUp;
import domain.Fielding_;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

public class JpaFieldingRepository extends BaseJpaRepository implements FieldingRepository {

	public JpaFieldingRepository(EntityManager em) {
		super(em);
	}

	@Override
	public List<Fielding> getAllFieldings(LineUp lineUp) {
		EntityManager entityManager = getEntityManager();
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Fielding> criteriaQuery = criteriaBuilder.createQuery(Fielding.class);
		Root<Fielding> root = criteriaQuery.from(Fielding.class);

		criteriaQuery.select(root).where(criteriaBuilder.and(
				criteriaBuilder.equal(root.get(Fielding_.lineUp), lineUp)));

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

}
