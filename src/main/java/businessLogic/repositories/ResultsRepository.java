package businessLogic.repositories;

import domainModel.Match;
import domainModel.Result;

public interface ResultsRepository {
    Result getResult(Match match);
    void saveResult(Result result);
}
