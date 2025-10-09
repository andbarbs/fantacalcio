package dal.repository.jpa;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import business.ports.repository.MatchDayRepository;
import domain.League;
import domain.MatchDaySerieA;
import domain.MatchDaySerieA_;
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
	public List<MatchDaySerieA> getAllMatchDays(League league) {
		EntityManager em = getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<MatchDaySerieA> cq = cb.createQuery(MatchDaySerieA.class);

        Root<MatchDaySerieA> matchDay = cq.from(MatchDaySerieA.class);

        cq.select(matchDay)
                .where(cb.equal(matchDay.get(MatchDaySerieA_.LEAGUE), league))
                .orderBy(cb.asc(matchDay.get(MatchDaySerieA_.NUMBER)));

        return em.createQuery(cq).getResultList();
	}

//TODO vanno aggiustate tutte rimuovendo la data e controllando number
	@Override
	public Optional<MatchDaySerieA> getPreviousMatchDay() {
		EntityManager em = getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<MatchDaySerieA> cq = cb.createQuery(MatchDaySerieA.class);

        Root<MatchDaySerieA> matchDay = cq.from(MatchDaySerieA.class);

        cq.select(matchDay)
                .where(cb.equal(matchDay.get(MatchDaySerieA_.STATUS), MatchDaySerieA.Status.PAST))
                .orderBy(cb.desc(matchDay.get(MatchDaySerieA_.NUMBER)));

        List<MatchDaySerieA> result = em.createQuery(cq)
                .setMaxResults(1)
                .getResultList();
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
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
		EntityManager entityManager = getEntityManager();
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<MatchDaySerieA> criteriaQuery = criteriaBuilder.createQuery(MatchDaySerieA.class);
		Root<MatchDaySerieA> root = criteriaQuery.from(MatchDaySerieA.class);

		// WHERE date = :date
		criteriaQuery.select(root)
				.where(criteriaBuilder.equal(root.get(MatchDaySerieA_.date), date));

		try {
			MatchDaySerieA result = entityManager.createQuery(criteriaQuery).getSingleResult();
			return Optional.of(result);
		} catch (NoResultException e) {
			return Optional.empty();
		}
	}

	@Override
	public void saveMatchDay(MatchDaySerieA matchDay) {
		getEntityManager().persist(matchDay);
	}

}
