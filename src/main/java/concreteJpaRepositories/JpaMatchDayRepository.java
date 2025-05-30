package concreteJpaRepositories;

import java.util.List;

import org.hibernate.SessionFactory;

import businessLogic.abstractRepositories.MatchDayRepository;
import domainModel.MatchDaySerieA;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

public class JpaMatchDayRepository implements MatchDayRepository {
	
	public JpaMatchDayRepository(SessionFactory sessionFactory) {
	}	
	
	@Override
	public List<MatchDaySerieA> getAllMatchDays() {		
		CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
		CriteriaQuery<MatchDaySerieA> criteriaQuery = criteriaBuilder.createQuery(MatchDaySerieA.class);
		Root<MatchDaySerieA> root = criteriaQuery.from(MatchDaySerieA.class);
		criteriaQuery.select(root);

		return session.createQuery(criteriaQuery).getResultList();	
	}

}
