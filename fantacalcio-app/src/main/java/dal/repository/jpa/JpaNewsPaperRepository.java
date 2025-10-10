package dal.repository.jpa;

import java.util.List;
import java.util.Optional;

import business.ports.repository.NewsPaperRepository;
import domain.NewsPaper;
import domain.NewsPaper_;
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
	public Optional<NewsPaper> getNewspaper(String name) {
		EntityManager em = getEntityManager();
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<NewsPaper> query = cb.createQuery(NewsPaper.class);
		Root<NewsPaper> root = query.from(NewsPaper.class);

		query.select(root).where(cb.equal(root.get(NewsPaper_.name), name));

		return em.createQuery(query).getResultList().stream().findFirst();
	}

	@Override
	public void saveNewsPaper(NewsPaper newsPaper) {
		getEntityManager().persist(newsPaper);
	}

}
