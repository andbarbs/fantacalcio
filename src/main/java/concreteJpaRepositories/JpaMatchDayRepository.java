package concreteJpaRepositories;

import java.util.List;

import org.hibernate.SessionFactory;

import businessLogic.abstractRepositories.AbstractJpaMatchDayRepository;
import domainModel.MatchDaySerieA;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

public class JpaMatchDayRepository implements AbstractJpaMatchDayRepository {
	
	public JpaMatchDayRepository(SessionFactory sessionFactory) {
	}	
	
	@Override
	public List<MatchDaySerieA> getAllMatchDays(EntityManager session) {		
		CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
		CriteriaQuery<MatchDaySerieA> criteriaQuery = criteriaBuilder.createQuery(MatchDaySerieA.class);
		Root<MatchDaySerieA> root = criteriaQuery.from(MatchDaySerieA.class);
		criteriaQuery.select(root);

		return session.createQuery(criteriaQuery).getResultList();	
	}

}
