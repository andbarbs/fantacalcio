package hibernateDAL;

import java.util.List;

import org.hibernate.SessionFactory;

import businessLogic.abstractDAL.repository.PlayerRepository;
import domainModel.Player;
import domainModel.Player_;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public class HibernatePlayerRepository extends HibernateEntityRepository implements PlayerRepository {

	public HibernatePlayerRepository(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	@Override
	public List<Player> findAll() {
		return getSessionFactory().fromTransaction(
				session -> {
					CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
					CriteriaQuery<Player> criteriaQuery = criteriaBuilder.createQuery(Player.class);
					Root<Player> root = criteriaQuery.from(Player.class);
					criteriaQuery.select(root);

					return session.createQuery(criteriaQuery).getResultList();
				});
	}

	@Override
	public Player findById(int id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean addPlayer(Player newPlayer) {
		
		List<Player> existing  = getSessionFactory().fromTransaction(session -> {
			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<Player> criteriaQuery = criteriaBuilder.createQuery(Player.class);
			Root<Player> root = criteriaQuery.from(Player.class);

			// Use Metamodel-generated fields for name and surname
			Predicate namePredicate = criteriaBuilder.equal(root.get(Player_.name), newPlayer.getName());
			Predicate surnamePredicate = criteriaBuilder.equal(root.get(Player_.surname), newPlayer.getSurname());

			// Combine the predicates
			criteriaQuery.select(root).where(criteriaBuilder.and(namePredicate, surnamePredicate));

			// Execute the query
			return session.createQuery(criteriaQuery).getResultList();
		});
		
		if(!existing.isEmpty()) {
			return false;
		}
		
		getSessionFactory().inTransaction(session -> session.persist(newPlayer));		
		return true;
	}

	@Override
	public List<Player> findBySurname(String surname) {
		// TODO Auto-generated method stub
		return null;
	}

}
