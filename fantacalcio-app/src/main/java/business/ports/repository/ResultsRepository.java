package business.ports.repository;

import java.util.Optional;

import domain.Match;
import domain.Result;

public interface ResultsRepository {
	
	Optional<Result> getResult(Match match);

	void saveResult(Result result);

}
