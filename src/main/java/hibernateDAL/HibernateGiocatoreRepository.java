package hibernateDAL;

import java.util.List;

import org.hibernate.SessionFactory;

import businessLogic.DAL.GiocatoreRepository;
import domainModel.Giocatore;

public class HibernateGiocatoreRepository extends HibernateEntityRepository implements GiocatoreRepository {

	public HibernateGiocatoreRepository(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	@Override
	public List<Giocatore> getAllGiocatori() {
		return getSessionFactory().fromTransaction(
				session -> session.createSelectionQuery("from Giocatore", Giocatore.class).getResultList());
	}

}
