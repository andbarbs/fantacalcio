package jpaRepositories;

import java.util.Set;

import businessLogic.repositories.NewsPaperRepository;
import domainModel.League;
import domainModel.NewsPaper;
import jakarta.persistence.EntityManager;

public class JpaNewsPaperRepository extends BaseJpaRepository implements NewsPaperRepository {

	public JpaNewsPaperRepository(EntityManager em) {
		super(em);
	}

	@Override
	public Set<NewsPaper> getAllNewspapers() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setNewsPaperForLeague(NewsPaper newspaper, League league) {
		// TODO Auto-generated method stub

	}

}
