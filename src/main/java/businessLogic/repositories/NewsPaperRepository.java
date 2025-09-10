package businessLogic.repositories;

import java.util.List;
import domainModel.League;
import domainModel.NewsPaper;

public interface NewsPaperRepository {

	List<NewsPaper> getAllNewspapers();

	void setNewsPaperForLeague(NewsPaper newspaper, League league);

}
