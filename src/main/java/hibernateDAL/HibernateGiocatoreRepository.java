package hibernateDAL;

import java.util.Arrays;
import java.util.List;

import org.hibernate.SessionFactory;

import businessLogic.abstractDAL.GiocatoreRepository;
import domainModel.Player;

public class HibernateGiocatoreRepository implements GiocatoreRepository {

	private SessionFactory sessionFactory;

	public HibernateGiocatoreRepository(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@Override
	public List<Player> getAllGiocatori() {
		// TODO Auto-generated method stub
		return Arrays.asList();
	}

}
