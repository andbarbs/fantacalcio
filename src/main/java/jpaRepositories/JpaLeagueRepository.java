package jpaRepositories;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import businessLogic.repositories.LeagueRepository;
import domainModel.FantaUser;
import domainModel.League;
import domainModel.League_;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

public class JpaLeagueRepository extends BaseJpaRepository implements LeagueRepository {

    public JpaLeagueRepository(EntityManager em) {
		super(em);
	}

	@Override
	public Optional<League> getLeagueByCode(String leagueCode) {
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<League> criteriaQuery = cb.createQuery(League.class);
        Root<League> root = criteriaQuery.from(League.class);

        criteriaQuery.where(
                cb.and(
                        cb.equal(root.get(League_.leagueCode), leagueCode)
                )
        );
        
        return getEntityManager().createQuery(criteriaQuery).getResultList().stream().findFirst();

	}

	@Override
	public boolean saveLeague(League league) {
		getEntityManager().persist(league);
		return true;
	}

	@Override
	public List<League> getLeaguesByUser(FantaUser user) {
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<League> criteriaQuery = cb.createQuery(League.class);
        Root<League> root = criteriaQuery.from(League.class);

        criteriaQuery.where(
                cb.and(
                        cb.equal(root.get(League_.leagueCode), leagueCode)
                )
        );
        
        return getEntityManager().createQuery(criteriaQuery).getResultList();
		
	}

}
