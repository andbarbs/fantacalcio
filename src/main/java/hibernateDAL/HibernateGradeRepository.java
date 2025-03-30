package hibernateDAL;

import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;

import businessLogic.DAL.GradeRepository;
import domainModel.Grade;
import domainModel.Grade_;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;

public class HibernateGradeRepository extends HibernateEntityRepository implements GradeRepository {

	public HibernateGradeRepository(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

//	@Override
//	public List<Grade> getAllGrades() {
//		return getSessionFactory().fromTransaction(
//				session -> session.createSelectionQuery(
//						"from Grade join fetch giocatore join fetch matchDay", Grade.class)
//									.setReadOnly(true)
//									.getResultList());
//	}
	
	@Override
	public List<Grade> getAllGrades() {
		return getSessionFactory().fromTransaction(session -> {
			HibernateCriteriaBuilder builder = session.getCriteriaBuilder();
			CriteriaQuery<Grade> query = builder.createQuery(Grade.class);
			Root<Grade> gradeRoot = query.from(Grade.class);

			gradeRoot.fetch(Grade_.giocatore, JoinType.INNER);
			gradeRoot.fetch(Grade_.matchDay, JoinType.INNER);

			query.select(gradeRoot);
			
			return session.createSelectionQuery(query).setReadOnly(true).getResultList();
		});
	}

}
