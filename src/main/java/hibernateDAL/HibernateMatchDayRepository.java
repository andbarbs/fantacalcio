package hibernateDAL;

import java.util.List;

import org.hibernate.SessionFactory;

import businessLogic.DAL.MatchDayRepository;
import domainModel.MatchDaySerieA;

public class HibernateMatchDayRepository extends HibernateEntityRepository implements MatchDayRepository {
	
	public HibernateMatchDayRepository(SessionFactory sessionFactory) {
		super(sessionFactory);
	}	
	
	@Override
	public List<MatchDaySerieA> getAllMatchDays() {
		return getSessionFactory().fromTransaction(
				session -> session.createSelectionQuery("from MatchDaySerieA", MatchDaySerieA.class).getResultList());
	}

}
