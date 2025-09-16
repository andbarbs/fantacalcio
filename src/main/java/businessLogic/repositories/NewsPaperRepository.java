package businessLogic.repositories;

import java.util.List;
import java.util.Optional;

import domainModel.NewsPaper;

public interface NewsPaperRepository {

	List<NewsPaper> getAllNewspapers();

	Optional<NewsPaper> getNewspaper(String name);

	void saveNewsPaper(NewsPaper newsPaper);

}
