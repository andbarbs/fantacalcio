package jpaRepositories;

import java.util.List;

import businessLogic.repositories.PlayerRepository;
import domainModel.Player;
import domainModel.Player_;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

public class JpaPlayerRepository extends BaseJpaRepository implements PlayerRepository {

	public JpaPlayerRepository(EntityManager em) {
		super(em);
	}

	@Override
	public List<Player> findAll() {
		EntityManager entityManager = getEntityManager();
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Player> criteriaQuery = criteriaBuilder.createQuery(Player.class);
		Root<Player> root = criteriaQuery.from(Player.class);
		criteriaQuery.select(root);

		return entityManager.createQuery(criteriaQuery).getResultList();		
	}

	@Override
	public boolean addPlayer(Player newPlayer) {
		EntityManager entityManager = getEntityManager();
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Player> criteriaQuery = criteriaBuilder.createQuery(Player.class);
		Root<Player> root = criteriaQuery.from(Player.class);

		criteriaQuery.select(root).where(criteriaBuilder.and(
				criteriaBuilder.equal(root.get(Player_.name), newPlayer.getName()), 
				criteriaBuilder.equal(root.get(Player_.surname), newPlayer.getSurname())));

		if (entityManager.createQuery(criteriaQuery).getResultList().isEmpty()) {
			entityManager.persist(newPlayer);
			return true;
		}
		return false;
	}

	@Override
	public List<Player> findBySurname(String surname) {
		EntityManager entityManager = getEntityManager();
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Player> criteriaQuery = criteriaBuilder.createQuery(Player.class);
		Root<Player> root = criteriaQuery.from(Player.class);

		criteriaQuery.select(root).where(criteriaBuilder.and(
				criteriaBuilder.equal(root.get(Player_.surname), surname)));

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<Player> findByTeam(String team) {
		EntityManager entityManager = getEntityManager();
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Player> criteriaQuery = criteriaBuilder.createQuery(Player.class);
		Root<Player> root = criteriaQuery.from(Player.class);

		criteriaQuery.select(root).where(criteriaBuilder.and(
				criteriaBuilder.equal(root.get(Player_.team), team)));

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

}
