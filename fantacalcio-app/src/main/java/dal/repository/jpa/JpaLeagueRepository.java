package dal.repository.jpa;

import java.util.List;
import java.util.Optional;

import business.ports.repository.LeagueRepository;
import domain.FantaTeam;
import domain.FantaUser;
import domain.League;
import domain.FantaTeam_;
import domain.League_;
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

		criteriaQuery.where(cb.and(cb.equal(root.get(League_.leagueCode), leagueCode)));

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
		Root<FantaTeam> root = criteriaQuery.from(FantaTeam.class);

		criteriaQuery.select(root.get(FantaTeam_.league)).distinct(true)
				.where(cb.equal(root.get(FantaTeam_.fantaManager), user));

		return getEntityManager().createQuery(criteriaQuery).getResultList();

	}

	@Override
	public List<FantaTeam> getAllTeams(League league) {
	    CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
	    CriteriaQuery<FantaTeam> query = cb.createQuery(FantaTeam.class);
	    Root<FantaTeam> root = query.from(FantaTeam.class);

	    query.select(root).where(cb.equal(root.get(FantaTeam_.league), league));

	    return getEntityManager().createQuery(query).getResultList();
	}


}
