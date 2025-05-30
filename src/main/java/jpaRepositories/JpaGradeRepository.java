package jpaRepositories;

import java.util.List;

import domainModel.League;
import domainModel.Match;
import businessLogic.abstractRepositories.GradeRepository;
import domainModel.Grade;
import domainModel.Grade_;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;

public class JpaGradeRepository extends BaseJpaRepository implements GradeRepository {

	public JpaGradeRepository(EntityManager em) {
		super(em);
	}

	@Override
	public List<Grade> getAllMatchGrades(Match match, League league) {
		EntityManager entityManager = getEntityManager();
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Grade> query = builder.createQuery(Grade.class);
		Root<Grade> gradeRoot = query.from(Grade.class);

		gradeRoot.fetch(Grade_.player, JoinType.INNER);
		gradeRoot.fetch(Grade_.matchDay, JoinType.INNER);

		query.select(gradeRoot);
		
		return entityManager.createQuery(query).getResultList();
//		return getSessionFactory().fromTransaction(session -> 
//		{
//			CriteriaBuilder builder = session.getCriteriaBuilder();
//			CriteriaQuery<Grade> query = builder.createQuery(Grade.class);
//			Root<Grade> gradeRoot = query.from(Grade.class);
//
//			gradeRoot.fetch(Grade_.player, JoinType.INNER);
//			gradeRoot.fetch(Grade_.matchDay, JoinType.INNER);
//
//			query.select(gradeRoot);
//			
//			return session.createSelectionQuery(query).setReadOnly(true).getResultList();
//		});
	}

}
