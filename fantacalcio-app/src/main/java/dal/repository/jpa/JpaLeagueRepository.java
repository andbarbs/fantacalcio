package dal.repository.jpa;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import business.ports.repository.LeagueRepository;
import domain.FantaTeam;
import domain.FantaUser;
import domain.League;
import domain.FantaTeam_;
import domain.League_;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;

public class JpaLeagueRepository extends BaseJpaRepository implements LeagueRepository {

	public JpaLeagueRepository(EntityManager em) {
		super(em);
	}

	@Override
	public Optional<League> getLeagueByCode(String leagueCode) {
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		CriteriaQuery<League> criteriaQuery = cb.createQuery(League.class);
		Root<League> root = criteriaQuery.from(League.class);
		
        root.fetch(League_.admin);

		criteriaQuery.where(cb.and(cb.equal(root.get(League_.leagueCode), leagueCode)));

		return getEntityManager().createQuery(criteriaQuery).getResultList().stream().findFirst();

	}

	@Override
	public boolean saveLeague(League league) {
		getEntityManager().persist(league);
		return true;
	}

	@Override
	public Set<League> getLeaguesByMember(FantaUser user) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<League> cq = cb.createQuery(League.class);

        Root<FantaTeam> teamRoot = cq.from(FantaTeam.class);

        // join, for query logic
        Join<FantaTeam, League> leagueJoin = teamRoot.join(FantaTeam_.league);

        // deep fetching
        leagueJoin.fetch(League_.admin);

        cq.select(leagueJoin)
                .distinct(true)
                .where(cb.equal(teamRoot.get(FantaTeam_.fantaManager), user));

        return getEntityManager().createQuery(cq).getResultStream().collect(Collectors.toSet());
	}

	@Override
	public List<FantaTeam> getAllTeams(League league) {
	    CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
	    CriteriaQuery<FantaTeam> query = cb.createQuery(FantaTeam.class);
	    Root<FantaTeam> root = query.from(FantaTeam.class);
	    
	    // deep fetching
	    root.fetch(FantaTeam_.fantaManager);
	    root.fetch(FantaTeam_.league).fetch(League_.admin);

	    query.select(root).where(cb.equal(root.get(FantaTeam_.league), league));

	    return getEntityManager().createQuery(query).getResultList();
	}

    //TODO testa
    @Override
    public List<League> getLeaguesByJournalist(FantaUser journalist) {
        return List.of();
    }
}
