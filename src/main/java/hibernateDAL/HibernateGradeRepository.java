package hibernateDAL;

import java.util.List;

import org.hibernate.SessionFactory;

import businessLogic.DAL.GradeRepository;
import domainModel.Grade;

public class HibernateGradeRepository extends HibernateEntityRepository implements GradeRepository {

	public HibernateGradeRepository(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	@Override
	public List<Grade> getAllGrades() {
		return getSessionFactory().fromTransaction(
				session -> session.createSelectionQuery("from Grade", Grade.class).getResultList());
	}

}
