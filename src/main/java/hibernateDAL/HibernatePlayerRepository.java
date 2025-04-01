package hibernateDAL;

import java.util.Arrays;
import java.util.List;

import org.hibernate.SessionFactory;

import businessLogic.abstractDAL.PlayerRepository;
import domainModel.Player;

public class HibernatePlayerRepository implements PlayerRepository {

	private SessionFactory sessionFactory;

	public HibernatePlayerRepository(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@Override
	public List<Player> getAllPlayers() {
		// TODO Auto-generated method stub
		return Arrays.asList();
	}


}
