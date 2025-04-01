package hibernateDAL;

import java.util.List;

import org.hibernate.SessionFactory;

import businessLogic.abstractDAL.repository.PlayerRepository;
import domainModel.Player;

public class HibernatePlayerRepository extends HibernateEntityRepository implements PlayerRepository {

	public HibernatePlayerRepository(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	@Override
	public List<Player> findAll() {
		return getSessionFactory().fromTransaction(
				session -> session.createSelectionQuery("from Player", Player.class).getResultList());
	}

	@Override
	public Player findById(int id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean addPlayer(Player newPlayer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<Player> findBySurname(String surname) {
		// TODO Auto-generated method stub
		return null;
	}

}
