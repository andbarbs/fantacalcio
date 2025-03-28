package hibernateDAL;

import java.util.List;

import org.hibernate.SessionFactory;

import businessLogic.DAL.GiocatoreRepository;
import domainModel.Giocatore;

public class HibernateGiocatoreRepository implements GiocatoreRepository {

	private SessionFactory sessionFactory;

	public HibernateGiocatoreRepository(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@Override
	public List<Giocatore> getAllGiocatori() {
		return sessionFactory.fromTransaction(
				session -> session.createSelectionQuery("from Giocatore", Giocatore.class).getResultList());
	}

}
