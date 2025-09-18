package jpaRepositories;

import java.util.List;

import businessLogic.repositories.GradeRepository;
import domainModel.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

public class JpaGradeRepository extends BaseJpaRepository implements GradeRepository {

	public JpaGradeRepository(EntityManager em) {
		super(em);
	}

	@Override
	public List<Grade> getAllMatchGrades(Match match, NewsPaper newsPaper) {
		EntityManager em = getEntityManager();
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Grade> cq = cb.createQuery(Grade.class);
		Root<Grade> gradeRoot = cq.from(Grade.class);

		cq.where(
				cb.and(
						cb.equal(gradeRoot.get(Grade_.matchDay), match.getMatchDaySerieA()),
						cb.equal(gradeRoot.get(Grade_.newsPaper), newsPaper)
				)
		);

		cq.select(gradeRoot).distinct(true); // avoid duplicates

		return em.createQuery(cq).getResultList();
	}

	@Override
	public void saveGrade(Grade grade) {
		getEntityManager().persist(grade);
	}

}
