package concreteJpaRepositories;

import java.util.List;

import businessLogic.abstractRepositories.PlayerRepository;
import domainModel.FantaTeam;
import domainModel.Player;
import domainModel.Player_;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

public class JpaPlayerRepository implements PlayerRepository {

	public JpaPlayerRepository() {
	}

	@Override
	public List<Player> findAll() {
		CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
		CriteriaQuery<Player> criteriaQuery = criteriaBuilder.createQuery(Player.class);
		Root<Player> root = criteriaQuery.from(Player.class);
		criteriaQuery.select(root);

		return session.createQuery(criteriaQuery).getResultList();		
	}

	@Override
	public boolean addPlayer(Player newPlayer) {		
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
	public List<Player> findBySurname(String surname) {
		CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
		CriteriaQuery<Player> criteriaQuery = criteriaBuilder.createQuery(Player.class);
		Root<Player> root = criteriaQuery.from(Player.class);

		criteriaQuery.select(root).where(criteriaBuilder.and(
				criteriaBuilder.equal(root.get(Player_.surname), surname)));

		return session.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<Player> findByTeam(FantaTeam team) {
		// TODO Auto-generated method stub
		return null;
	}

}
