package dal.repository.jpa;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import business.ports.repository.PlayerRepository;
import domain.Contract;
import domain.Contract_;
import domain.FantaTeam;
import domain.FantaTeam_;
import domain.League;
import domain.Player;
import domain.Player_;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;

public class JpaPlayerRepository extends BaseJpaRepository implements PlayerRepository {

	public JpaPlayerRepository(EntityManager em) {
		super(em);
	}

	@Override
	public Set<Player> findAll() {
		EntityManager entityManager = getEntityManager();
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Player> criteriaQuery = criteriaBuilder.createQuery(Player.class);
		Root<Player> root = criteriaQuery.from(Player.class);
		criteriaQuery.select(root);

		return entityManager.createQuery(criteriaQuery).getResultStream().collect(Collectors.toSet());		
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

		criteriaQuery.select(root)
				.where(criteriaBuilder.and(criteriaBuilder.equal(root.get(Player_.surname), surname)));

		return entityManager.createQuery(criteriaQuery).getResultStream().sorted(Comparator.comparing(Player::getName))
				.toList();
	}

	@Override
	public Set<Player> findByClub(Player.Club team) {
		EntityManager entityManager = getEntityManager();
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Player> criteriaQuery = criteriaBuilder.createQuery(Player.class);
		Root<Player> root = criteriaQuery.from(Player.class);

		criteriaQuery.select(root).where(criteriaBuilder.and(
				criteriaBuilder.equal(root.get(Player_.club), team)));

		return Set.copyOf(entityManager.createQuery(criteriaQuery).getResultList());
	}
	
	@Override
	public Set<Player> getAllInLeague(League league) {
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		CriteriaQuery<Player> criteriaQuery = cb.createQuery(Player.class);
		Root<Contract> root = criteriaQuery.from(Contract.class);

		// joins, for query logic
		Join<Contract, FantaTeam> teamJoin = root.join(Contract_.team);
		Join<FantaTeam, League> leagueJoin = teamJoin.join(FantaTeam_.league);

		// deep fetching

		criteriaQuery.select(root.get(Contract_.player)).where(cb.equal(leagueJoin, league)).distinct(true);

		// 8. Create and execute the TypedQuery
		return Set.copyOf(getEntityManager().createQuery(criteriaQuery).getResultList());
	}
}
