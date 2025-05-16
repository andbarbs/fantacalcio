package hibernateDAL;

import java.util.List;

import businessLogic.abstractDAL.repository.AbstractJpaPlayerRepository;
import domainModel.Player;
import domainModel.Player_;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

public class JpaPlayerRepository implements AbstractJpaPlayerRepository {

	public JpaPlayerRepository() {
	}

	@Override
	public List<Player> findAll(EntityManager session) {
		CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
		CriteriaQuery<Player> criteriaQuery = criteriaBuilder.createQuery(Player.class);
		Root<Player> root = criteriaQuery.from(Player.class);
		criteriaQuery.select(root);

		return session.createQuery(criteriaQuery).getResultList();		
	}

	@Override
	public boolean addPlayer(EntityManager session, Player newPlayer) {		
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
	}

	@Override
	public List<Player> findBySurname(EntityManager session, String surname) {
		CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
		CriteriaQuery<Player> criteriaQuery = criteriaBuilder.createQuery(Player.class);
		Root<Player> root = criteriaQuery.from(Player.class);

		criteriaQuery.select(root).where(criteriaBuilder.and(
				criteriaBuilder.equal(root.get(Player_.surname), surname)));

		return session.createQuery(criteriaQuery).getResultList();
	}

}
