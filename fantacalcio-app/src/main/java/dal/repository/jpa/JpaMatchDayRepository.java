package dal.repository.jpa;

import java.util.List;
import java.util.Optional;

import business.ports.repository.MatchDayRepository;
import domain.League;
import domain.MatchDaySerieA;
import domain.MatchDaySerieA_;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
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
	public Optional<MatchDaySerieA> getPreviousMatchDay(League league) {
		EntityManager em = getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<MatchDaySerieA> cq = cb.createQuery(MatchDaySerieA.class);

        Root<MatchDaySerieA> matchDay = cq.from(MatchDaySerieA.class);

        cq.select(matchDay)
                .where(
                        cb.and(
                                cb.equal(matchDay.get(MatchDaySerieA_.LEAGUE), league),
                                cb.equal(matchDay.get(MatchDaySerieA_.STATUS), MatchDaySerieA.Status.PAST)
                        )
                )
                .orderBy(cb.desc(matchDay.get(MatchDaySerieA_.NUMBER)));

        List<MatchDaySerieA> result = em.createQuery(cq)
                .setMaxResults(1)
                .getResultList();

        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
	}

	@Override
	public Optional<MatchDaySerieA> getNextMatchDay(League league) {
		EntityManager em= getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<MatchDaySerieA> cq = cb.createQuery(MatchDaySerieA.class);

        Root<MatchDaySerieA> matchDay = cq.from(MatchDaySerieA.class);

        cq.select(matchDay)
                .where(
                        cb.and(
                                cb.equal(matchDay.get(MatchDaySerieA_.LEAGUE), league),
                                cb.equal(matchDay.get(MatchDaySerieA_.STATUS), MatchDaySerieA.Status.FUTURE)
                        )
                )
                .orderBy(cb.asc(matchDay.get(MatchDaySerieA_.NUMBER)));

        List<MatchDaySerieA> result = em.createQuery(cq)
                .setMaxResults(1)
                .getResultList();

        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
	}

	@Override
	public Optional<MatchDaySerieA> getMatchDay(League league) {
		EntityManager em = getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<MatchDaySerieA> cq = cb.createQuery(MatchDaySerieA.class);

        Root<MatchDaySerieA> matchDay = cq.from(MatchDaySerieA.class);

        cq.select(matchDay)
                .where(
                        cb.and(
                                cb.equal(matchDay.get(MatchDaySerieA_.LEAGUE), league),
                                cb.equal(matchDay.get(MatchDaySerieA_.STATUS), MatchDaySerieA.Status.PRESENT)
                        )
                );

        List<MatchDaySerieA> result = em.createQuery(cq)
                .setMaxResults(1)
                .getResultList();

        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
	}

	@Override
	public void saveMatchDay(MatchDaySerieA matchDay) {
		getEntityManager().persist(matchDay);
	}

}
