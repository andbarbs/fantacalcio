package dal.repository.jpa;

import domain.FantaTeam_;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import business.ports.repository.FantaTeamRepository;
import domain.FantaTeam;
import domain.FantaUser;
import domain.League;
import domain.League_;

public class JpaFantaTeamRepository extends BaseJpaRepository implements FantaTeamRepository {
	
	public JpaFantaTeamRepository(EntityManager em) {
		super(em);
	}

	@Override
	public Set<FantaTeam> getAllTeams(League league) {
		
		EntityManager entityManager = getEntityManager();
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<FantaTeam> criteriaQuery = criteriaBuilder.createQuery(FantaTeam.class);
		Root<FantaTeam> root = criteriaQuery.from(FantaTeam.class);
		
		// deep fetching
		root.fetch(FantaTeam_.FANTA_MANAGER);
        root.fetch(FantaTeam_.LEAGUE).fetch(League_.ADMIN);        

		criteriaQuery.select(root).where(criteriaBuilder.and(
				criteriaBuilder.equal(root.get(FantaTeam_.league), league)));

		return entityManager.createQuery(criteriaQuery).getResultStream().collect(Collectors.toSet());
	}

	@Override
	public void saveTeam(FantaTeam team) {
		getEntityManager().persist(team);
	}

	@Override
	public Optional<FantaTeam> getFantaTeamByUserAndLeague(League league, FantaUser user) {
		EntityManager em = getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<FantaTeam> query = cb.createQuery(FantaTeam.class);
        Root<FantaTeam> root = query.from(FantaTeam.class);
        
        // deep fetching
 		root.fetch(FantaTeam_.FANTA_MANAGER);
        root.fetch(FantaTeam_.LEAGUE).fetch(League_.ADMIN);  

        query.select(root).where(
                cb.equal(root.get(FantaTeam_.league), league),
                cb.equal(root.get(FantaTeam_.fantaManager), user)
        );

        return em.createQuery(query).getResultStream().findFirst();
	}
}
