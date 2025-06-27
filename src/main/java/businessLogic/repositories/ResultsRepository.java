package businessLogic.repositories;

import domainModel.Match;
import domainModel.Result;

import java.util.Optional;

public interface ResultsRepository {
    Optional<Result> getResult(Match match);
    void saveResult(Result result);
}
