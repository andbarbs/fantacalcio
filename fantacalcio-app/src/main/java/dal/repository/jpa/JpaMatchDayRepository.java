package dal.repository.jpa;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import business.ports.repository.MatchDayRepository;
import domain.MatchDay;
import domain.MatchDay_;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public class JpaMatchDayRepository extends BaseJpaRepository implements MatchDayRepository {
		
	public JpaMatchDayRepository(EntityManager em) {
		super(em);
	}

	@Override
	public List<MatchDay> getAllMatchDays() {
		EntityManager entityManager = getEntityManager();
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<MatchDay> criteriaQuery = criteriaBuilder.createQuery(MatchDay.class);
		Root<MatchDay> root = criteriaQuery.from(MatchDay.class);

		criteriaQuery.select(root);
		criteriaQuery.orderBy(criteriaBuilder.asc(root.get(MatchDay_.DATE)));

		return entityManager.createQuery(criteriaQuery).getResultList();
	}


	@Override
	public Optional<MatchDay> getPreviousMatchDay(LocalDate date) {
		EntityManager entityManager = getEntityManager();
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<MatchDay> criteriaQuery = criteriaBuilder.createQuery(MatchDay.class);
		Root<MatchDay> root = criteriaQuery.from(MatchDay.class);

		Predicate beforeDate = criteriaBuilder.lessThan(root.get("date"), date);
		criteriaQuery.select(root).where(beforeDate);

		criteriaQuery.orderBy(criteriaBuilder.desc(root.get("date")));

		TypedQuery<MatchDay> query = entityManager.createQuery(criteriaQuery);
		query.setMaxResults(1);

		List<MatchDay> resultList = query.getResultList();
		return resultList.isEmpty() ? Optional.empty() : Optional.of(resultList.get(0));
	}

	@Override
	public Optional<MatchDay> getNextMatchDay(LocalDate date) {
		EntityManager entityManager = getEntityManager();
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<MatchDay> criteriaQuery = criteriaBuilder.createQuery(MatchDay.class);
		Root<MatchDay> root = criteriaQuery.from(MatchDay.class);

		Predicate afterDate = criteriaBuilder.greaterThan(root.get("date"), date);
		criteriaQuery.select(root).where(afterDate);

		criteriaQuery.orderBy(criteriaBuilder.asc(root.get("date")));

		TypedQuery<MatchDay> query = entityManager.createQuery(criteriaQuery);
		query.setMaxResults(1);

		List<MatchDay> resultList = query.getResultList();
		return resultList.isEmpty() ? Optional.empty() : Optional.of(resultList.get(0));
	}

	@Override
	public Optional<MatchDay> getMatchDay(LocalDate date) {
		EntityManager entityManager = getEntityManager();
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<MatchDay> criteriaQuery = criteriaBuilder.createQuery(MatchDay.class);
		Root<MatchDay> root = criteriaQuery.from(MatchDay.class);

		// WHERE date = :date
		criteriaQuery.select(root)
				.where(criteriaBuilder.equal(root.get(MatchDay_.date), date));

		try {
			MatchDay result = entityManager.createQuery(criteriaQuery).getSingleResult();
			return Optional.of(result);
		} catch (NoResultException e) {
			return Optional.empty();
		}
	}

	@Override
	public void saveMatchDay(MatchDay matchDay) {
		getEntityManager().persist(matchDay);
	}

}
