package businessLogic.abstractDAL;

import domainModel.FantaTeam;
import domainModel.League;
import domainModel.User;

import java.util.Iterator;

public interface TeamDAO {
    Iterator<FantaTeam> getTeams(League lega);
    void save(League lega, User fantaManager);
}
