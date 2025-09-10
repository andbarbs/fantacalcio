package jpaRepositories;

import java.util.List;
import businessLogic.repositories.NewsPaperRepository;
import domainModel.League;
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

	@Override
	public void setNewsPaperForLeague(NewsPaper newspaper, League league) { // Non ha senso perché la lega ha il newspaper nel costruttore, al massimo si mette un setter
		// TODO Auto-generated method stub

	}

}
