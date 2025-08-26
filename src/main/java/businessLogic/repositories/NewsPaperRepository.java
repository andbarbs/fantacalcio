package businessLogic.repositories;

import java.util.Set;

import domainModel.League;
import domainModel.NewsPaper;

public interface NewsPaperRepository {

	Set<NewsPaper> getAllNewspapers();

	void setNewsPaper(NewsPaper newspaper, League league);

}
