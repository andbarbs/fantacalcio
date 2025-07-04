package jpaRepositories;

import businessLogic.repositories.TeamRepository;
import domainModel.FantaTeam;
import domainModel.FantaUser;
import domainModel.League;
import jakarta.persistence.EntityManager;

import java.util.Set;

public class JpaFantaTeamRepository extends BaseJpaRepository implements TeamRepository {
    public JpaFantaTeamRepository(EntityManager em) {
        super(em);
    }

    @Override
    public Set<FantaTeam> getAllTeams(League league) {
        return Set.of();
    }

    @Override
    public boolean saveTeam(FantaTeam team) {
        return false;
    }

    @Override
    public FantaTeam getFantaTeamByUserAndLeague(League league, FantaUser user) {
        return null;
    }
}
