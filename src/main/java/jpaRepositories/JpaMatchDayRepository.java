package jpaRepositories;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import businessLogic.repositories.MatchDayRepository;
import domainModel.MatchDaySerieA;
import jakarta.persistence.EntityManager;
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
	public List<MatchDaySerieA> getAllMatchDays() {		
		EntityManager entityManager = getEntityManager();
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<MatchDaySerieA> criteriaQuery = criteriaBuilder.createQuery(MatchDaySerieA.class);
		Root<MatchDaySerieA> root = criteriaQuery.from(MatchDaySerieA.class);
		criteriaQuery.select(root);
		//TODO controllare che la lista sia in ordine ovvero prima ho la prima giornata poi la seconda etc etc
		return entityManager.createQuery(criteriaQuery).getResultList();	
	}

	@Override
	public Optional<MatchDaySerieA> getPreviousMatchDay(LocalDate date) {
		EntityManager entityManager = getEntityManager();
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<MatchDaySerieA> criteriaQuery = criteriaBuilder.createQuery(MatchDaySerieA.class);
		Root<MatchDaySerieA> root = criteriaQuery.from(MatchDaySerieA.class);

		Predicate beforeDate = criteriaBuilder.lessThan(root.get("date"), date);
		criteriaQuery.select(root).where(beforeDate);

		criteriaQuery.orderBy(criteriaBuilder.desc(root.get("date")));

		TypedQuery<MatchDaySerieA> query = entityManager.createQuery(criteriaQuery);
		query.setMaxResults(1);

		List<MatchDaySerieA> resultList = query.getResultList();
		return resultList.isEmpty() ? Optional.empty() : Optional.of(resultList.get(0));
	}

	@Override
	public Optional<MatchDaySerieA> getNextMatchDay(LocalDate date) {
		EntityManager entityManager = getEntityManager();
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<MatchDaySerieA> criteriaQuery = criteriaBuilder.createQuery(MatchDaySerieA.class);
		Root<MatchDaySerieA> root = criteriaQuery.from(MatchDaySerieA.class);

		Predicate afterDate = criteriaBuilder.greaterThan(root.get("date"), date);
		criteriaQuery.select(root).where(afterDate);

		criteriaQuery.orderBy(criteriaBuilder.asc(root.get("date")));

		TypedQuery<MatchDaySerieA> query = entityManager.createQuery(criteriaQuery);
		query.setMaxResults(1);

		List<MatchDaySerieA> resultList = query.getResultList();
		return resultList.isEmpty() ? Optional.empty() : Optional.of(resultList.get(0));
	}

	@Override
	public Optional<MatchDaySerieA> getMatchDay(LocalDate date) {
		//TODO implementa
		return Optional.empty();
	}
}
