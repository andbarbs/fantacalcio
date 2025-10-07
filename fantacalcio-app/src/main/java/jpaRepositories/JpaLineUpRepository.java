package jpaRepositories;

import businessLogic.repositories.LineUpRepository;
import domainModel.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Fetch;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;

import java.util.List;
import java.util.Optional;

public class JpaLineUpRepository extends BaseJpaRepository implements LineUpRepository {
	
    public JpaLineUpRepository(EntityManager em) {
        super(em);
    }

    @Override
    public void saveLineUp(LineUp lineUp) {
        getEntityManager().persist(lineUp);
    }

    @Override
    public void deleteLineUp(LineUp lineUp) {
    	EntityManager entityManager = getEntityManager();
    	LineUp managed = entityManager.merge(lineUp);
        entityManager.remove(managed);
    }

	/**
	 * when present, the {@link LineUp} instance will be deep-fetched all the way to
	 * fielded {@link Player}s
	 */
    @Override
    public Optional<LineUp> getLineUpByMatchAndTeam(Match match, FantaTeam fantaTeam) {
        EntityManager em = getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<LineUp> query = cb.createQuery(LineUp.class);
        Root<LineUp> root = query.from(LineUp.class);

        // Fetch the single-valued 'match' association
        root.fetch(LineUp_.match);

        // 1. Fetch the 'fieldings' collection from LineUp
        // We use Fetch<LineUp, Fielding> to indicate the types in the join
        Fetch<LineUp, Fielding> fieldingFetch = root.fetch(LineUp_.fieldings, JoinType.LEFT);

        // 2. IMPORTANT: Fetch the 'player' association FROM the fieldingFetch
        fieldingFetch.fetch(Fielding_.player, JoinType.LEFT);

        query.select(root).where(
                cb.equal(root.get(LineUp_.match), match),
                cb.equal(root.get(LineUp_.team), fantaTeam)
        ).distinct(true);

        List<LineUp> result = em.createQuery(query).getResultList();
        return result.stream().findFirst();
    }
}
