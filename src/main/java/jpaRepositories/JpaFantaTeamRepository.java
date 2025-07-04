package jpaRepositories;

import businessLogic.repositories.TeamRepository;
import domainModel.FantaTeam;
import domainModel.FantaTeam_;
import domainModel.FantaUser;
import domainModel.League;
import domainModel.LineUp;
import domainModel.LineUp_;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import java.util.List;

public class JpaFantaTeamRepository extends BaseJpaRepository implements TeamRepository {
	public JpaFantaTeamRepository(EntityManager em) {
		super(em);
	}

	@Override
	public List<FantaTeam> getAllTeams(League league) {
		EntityManager entityManager = getEntityManager();
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<FantaTeam> criteriaQuery = criteriaBuilder.createQuery(FantaTeam.class);
		Root<FantaTeam> root = criteriaQuery.from(FantaTeam.class);
		criteriaQuery.select(root);

		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public void saveTeam(FantaTeam team) {
		getEntityManager().persist(team);
	}

	@Override
	public FantaTeam getFantaTeamByUserAndLeague(League league, FantaUser user) {
		EntityManager em = getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<FantaTeam> query = cb.createQuery(FantaTeam.class);
        Root<FantaTeam> root = query.from(FantaTeam.class);

        query.select(root).where(
                cb.equal(root.get(FantaTeam_.league), league),
                cb.equal(root.get(FantaTeam_.fantaManager), user)
        );

        return em.createQuery(query).getSingleResult();
	}
}
