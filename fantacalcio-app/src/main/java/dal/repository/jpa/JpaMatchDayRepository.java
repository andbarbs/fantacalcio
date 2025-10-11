package dal.repository.jpa;

import java.util.List;
import java.util.Optional;

import business.ports.repository.MatchDayRepository;
import domain.League;
import domain.MatchDay;
import domain.MatchDay_;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

public class JpaMatchDayRepository extends BaseJpaRepository implements MatchDayRepository {
		
	public JpaMatchDayRepository(EntityManager em) {
		super(em);
	}

	@Override
	public List<MatchDay> getAllMatchDays(League league) {
		EntityManager em = getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<MatchDay> cq = cb.createQuery(MatchDay.class);

        Root<MatchDay> matchDay = cq.from(MatchDay.class);

        cq.select(matchDay)
                .where(cb.equal(matchDay.get(MatchDay_.LEAGUE), league))
                .orderBy(cb.asc(matchDay.get(MatchDay_.NUMBER)));

        return em.createQuery(cq).getResultList();
	}

//TODO vanno aggiustate tutte rimuovendo la data e controllando number
	@Override
	public Optional<MatchDay> getPreviousMatchDay(League league) {
		EntityManager em = getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<MatchDay> cq = cb.createQuery(MatchDay.class);

        Root<MatchDay> matchDay = cq.from(MatchDay.class);

        cq.select(matchDay)
                .where(
                        cb.and(
                                cb.equal(matchDay.get(MatchDay_.LEAGUE), league),
                                cb.equal(matchDay.get(MatchDay_.STATUS), MatchDay.Status.PAST)
                        )
                )
                .orderBy(cb.desc(matchDay.get(MatchDay_.NUMBER)));

        List<MatchDay> result = em.createQuery(cq)
                .setMaxResults(1)
                .getResultList();

        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
	}

	@Override
	public Optional<MatchDay> getNextMatchDay(League league) {
		EntityManager em= getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<MatchDay> cq = cb.createQuery(MatchDay.class);

        Root<MatchDay> matchDay = cq.from(MatchDay.class);

        cq.select(matchDay)
                .where(
                        cb.and(
                                cb.equal(matchDay.get(MatchDay_.LEAGUE), league),
                                cb.equal(matchDay.get(MatchDay_.STATUS), MatchDay.Status.FUTURE)
                        )
                )
                .orderBy(cb.asc(matchDay.get(MatchDay_.NUMBER)));

        List<MatchDay> result = em.createQuery(cq)
                .setMaxResults(1)
                .getResultList();

        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
	}

	@Override
	public Optional<MatchDay> getMatchDay(League league) {
		EntityManager em = getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<MatchDay> cq = cb.createQuery(MatchDay.class);

        Root<MatchDay> matchDay = cq.from(MatchDay.class);

        cq.select(matchDay)
                .where(
                        cb.and(
                                cb.equal(matchDay.get(MatchDay_.LEAGUE), league),
                                cb.equal(matchDay.get(MatchDay_.STATUS), MatchDay.Status.PRESENT)
                        )
                );

        List<MatchDay> result = em.createQuery(cq)
                .setMaxResults(1)
                .getResultList();

        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
	}

	@Override
	public void saveMatchDay(MatchDay matchDay) {
		getEntityManager().persist(matchDay);
	}

}
