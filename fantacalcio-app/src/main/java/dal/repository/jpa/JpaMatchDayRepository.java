package dal.repository.jpa;

import java.util.List;
import java.util.Optional;

import business.ports.repository.MatchDayRepository;
import domain.League;
import domain.League_;
import domain.MatchDay;
import domain.MatchDay_;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
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
        matchDay.fetch(MatchDay_.league).fetch(League_.ADMIN, JoinType.LEFT);

        cq.select(matchDay)
                .where(cb.equal(matchDay.get(MatchDay_.LEAGUE), league))
                .orderBy(cb.asc(matchDay.get(MatchDay_.NUMBER)));

        return em.createQuery(cq).getResultList();
	}

	@Override
	public Optional<MatchDay> getLatestEndedMatchDay(League league) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<MatchDay> cq = cb.createQuery(MatchDay.class);

        Root<MatchDay> root = cq.from(MatchDay.class);
        root.fetch(MatchDay_.LEAGUE).fetch(League_.ADMIN, JoinType.LEFT);

        cq.select(root)
                .where(
                        cb.and(
                                cb.equal(root.get(MatchDay_.league), league),
                                cb.equal(root.get(MatchDay_.status), MatchDay.Status.PAST)
                        )
                )
                .orderBy(cb.desc(root.get(MatchDay_.number)));

        List<MatchDay> results = getEntityManager()
                .createQuery(cq)
                .setMaxResults(1)
                .getResultList();

        return results.stream().findFirst();
	}

	@Override
	public Optional<MatchDay> getEarliestUpcomingMatchDay(League league) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<MatchDay> cq = cb.createQuery(MatchDay.class);

        Root<MatchDay> root = cq.from(MatchDay.class);
        root.fetch(MatchDay_.LEAGUE).fetch(League_.ADMIN, JoinType.LEFT);

        cq.select(root)
                .where(
                        cb.and(
                                cb.equal(root.get(MatchDay_.league), league),
                                cb.equal(root.get(MatchDay_.status), MatchDay.Status.FUTURE)
                        )
                )
                .orderBy(cb.asc(root.get(MatchDay_.number))); // lowest number first

        List<MatchDay> results = getEntityManager()
                .createQuery(cq)
                .setMaxResults(1)
                .getResultList();

        return results.stream().findFirst();
	}

	@Override
	public Optional<MatchDay> getOngoingMatchDay(League league) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<MatchDay> cq = cb.createQuery(MatchDay.class);

        Root<MatchDay> root = cq.from(MatchDay.class);
        root.fetch(MatchDay_.LEAGUE).fetch(League_.ADMIN, JoinType.LEFT);

        cq.select(root)
                .where(
                        cb.and(
                                cb.equal(root.get(MatchDay_.league), league),
                                cb.equal(root.get(MatchDay_.status), MatchDay.Status.PRESENT)
                        )
                );

        List<MatchDay> results = getEntityManager()
                .createQuery(cq)
                .setMaxResults(1) // optional safety guard
                .getResultList();

        return results.stream().findFirst();
	}

	@Override
	public void saveMatchDay(MatchDay matchDay) {
		getEntityManager().persist(matchDay);
	}

}
