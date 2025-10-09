package business.ports.repository;

import java.util.List;
import java.util.Optional;

public interface NewsPaperRepository {

	List<NewsPaper> getAllNewspapers();

	Optional<NewsPaper> getNewspaper(String name);

	void saveNewsPaper(NewsPaper newsPaper);

}
