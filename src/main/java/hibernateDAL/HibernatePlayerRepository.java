package hibernateDAL;

import java.util.List;

import org.hibernate.SessionFactory;

import businessLogic.abstractDAL.repository.PlayerRepository;
import domainModel.Player;
import domainModel.Player_;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
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
	public boolean addPlayer(Player newPlayer) {		
		return getSessionFactory().fromTransaction(session -> 
		{
			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<Player> criteriaQuery = criteriaBuilder.createQuery(Player.class);
			Root<Player> root = criteriaQuery.from(Player.class);

			criteriaQuery.select(root).where(criteriaBuilder.and(
					criteriaBuilder.equal(root.get(Player_.name), newPlayer.getName()), 
					criteriaBuilder.equal(root.get(Player_.surname), newPlayer.getSurname())));

			if (session.createQuery(criteriaQuery).getResultList().isEmpty()) {
				session.persist(newPlayer);
				return true;
			}
			return false;			
		});
	}

	@Override
	public List<Player> findBySurname(String surname) {
		return getSessionFactory().fromTransaction(session ->
		{
			CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
			CriteriaQuery<Player> criteriaQuery = criteriaBuilder.createQuery(Player.class);
			Root<Player> root = criteriaQuery.from(Player.class);

			criteriaQuery.select(root).where(criteriaBuilder.and(
					criteriaBuilder.equal(root.get(Player_.surname), surname)));

			return session.createQuery(criteriaQuery).getResultList();
		});

	}

}
