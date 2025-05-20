package businessLogic;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.hibernate.SessionFactory;

import businessLogic.abstractDAL.repository.AbstractJpaMatchRepository;
import domainModel.League;
import domainModel.Match;
import domainModel.MatchDaySerieA;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

public class Service {	
	
	private SessionFactory sessionFactory;
	
	private AbstractJpaMatchRepository matchRepository;	

	public Service(SessionFactory sessionFactory, AbstractJpaMatchRepository matchRepository) {
		this.sessionFactory = sessionFactory;
		this.matchRepository = matchRepository;
	}



	public Map<MatchDaySerieA, Set<Match>> getAllMatches(League league) {
	    return fromSession(sessionFactory, em -> matchRepository.getAllMatches(em, league));
	}

	
	static <T> T fromSession(EntityManagerFactory factory, Function<EntityManager, T> work) {
	    EntityManager em = factory.createEntityManager();
	    EntityTransaction transaction = em.getTransaction();
	    try {
	        transaction.begin();
	        T result = work.apply(em);
	        transaction.commit();
	        return result;
	    } catch (Exception e) {
	        if (transaction.isActive()) {
	            transaction.rollback();
	        }
	        throw e;
	    } finally {
	        em.close();
	    }
	}




}
