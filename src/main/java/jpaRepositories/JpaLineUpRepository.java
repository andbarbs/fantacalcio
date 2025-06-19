package jpaRepositories;

import businessLogic.repositories.LineUpRepository;
import domainModel.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import java.util.List;
import java.util.Optional;

public class JpaLineUpRepository extends BaseJpaRepository implements LineUpRepository {
    public JpaLineUpRepository(EntityManager em) {
        super(em);
    }

    @Override
    public List<LineUp> getLineUps() {
        return List.of();
    }

    @Override
    public void saveLineUp(LineUp lineUp) {
        getEntityManager().persist(lineUp);
    }

    @Override
    public void deleteLineUp(LineUp lineUp) {

    }

    @Override
    public Optional<LineUp> getLineUpByMatchAndTeam(League league, Match match, FantaTeam fantaTeam) {
        EntityManager em = getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<LineUp> query = cb.createQuery(LineUp.class);
        Root<LineUp> root = query.from(LineUp.class);

        query.select(root).where(
                cb.equal(root.get(LineUp_.match), match),
                cb.equal(root.get(LineUp_.team), fantaTeam)
        );

        List<LineUp> result = em.createQuery(query).getResultList();
        return result.stream().findFirst();
    }
}
