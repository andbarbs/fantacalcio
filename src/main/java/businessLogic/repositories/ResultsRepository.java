package businessLogic.repositories;

import java.util.Optional;

import domainModel.Match;
import domainModel.Result;

public interface ResultsRepository {
    Optional<Result> getResult(Match match);
    void saveResult(Result result);
}
