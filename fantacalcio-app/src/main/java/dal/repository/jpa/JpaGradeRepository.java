package dal.repository.jpa;

import java.util.List;

import business.ports.repository.GradeRepository;
import domain.*;
import domain.Grade_;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

public class JpaGradeRepository extends BaseJpaRepository implements GradeRepository {

	public JpaGradeRepository(EntityManager em) {
		super(em);
	}

	@Override
	public List<Grade> getAllMatchGrades(MatchDaySerieA matchDay) {
		EntityManager em = getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Grade> cq = cb.createQuery(Grade.class);

        Root<Grade> grade = cq.from(Grade.class);

        cq.select(grade)
                .where(cb.equal(grade.get(Grade_.MATCH_DAY), matchDay));

        return em.createQuery(cq).getResultList();
	}

	@Override
	public void saveGrade(Grade grade) {
		getEntityManager().persist(grade);
	}

}
