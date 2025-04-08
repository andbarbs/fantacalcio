package businessLogic.abstractDAL;

import domainModel.League;
import domainModel.NewsPaper;
import domainModel.User;

import java.util.Iterator;

public interface LeagueDAO {
    Iterator<League> getLeagues(User user);
    void save(User admin, String name, NewsPaper newsPaper);

}
