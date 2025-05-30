package concreteJpaRepositories;

import java.util.List;

import domainModel.League;
import domainModel.Match;
import org.hibernate.SessionFactory;
import businessLogic.abstractRepositories.GradeRepository;
import domainModel.Grade;
import domainModel.Grade_;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;

public class JpaGradeRepository implements GradeRepository {

	public JpaGradeRepository(SessionFactory sessionFactory) {
	}
	
	@Override
	public List<Grade> getAllMatchGrades(Match match, League league) {
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Grade> query = builder.createQuery(Grade.class);
		Root<Grade> gradeRoot = query.from(Grade.class);

		gradeRoot.fetch(Grade_.player, JoinType.INNER);
		gradeRoot.fetch(Grade_.matchDay, JoinType.INNER);

		query.select(gradeRoot);
		
		return session.createQuery(query).getResultList();
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
