package jpaRepositories;

import java.util.List;
import businessLogic.repositories.NewsPaperRepository;
import domainModel.NewsPaper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

public class JpaNewsPaperRepository extends BaseJpaRepository implements NewsPaperRepository {

	public JpaNewsPaperRepository(EntityManager em) {
		super(em);
	}

	@Override
	public List<NewsPaper> getAllNewspapers() {
    	EntityManager em = getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<NewsPaper> query = cb.createQuery(NewsPaper.class);
        Root<NewsPaper> root = query.from(NewsPaper.class);

        query.select(root);

        return em.createQuery(query).getResultList();
	}

}
