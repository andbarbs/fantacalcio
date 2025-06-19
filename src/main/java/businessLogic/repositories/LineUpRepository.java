package businessLogic.repositories;

import domainModel.FantaTeam;
import domainModel.League;
import domainModel.LineUp;
import domainModel.Match;

import java.util.List;
import java.util.Optional;

public interface LineUpRepository {
    List<LineUp> getLineUps();
    void saveLineUp(LineUp lineUp);
    void deleteLineUp(LineUp lineUp);
    Optional<LineUp> getLineUpByMatchAndTeam(League league, Match match, FantaTeam fantaTeam);
}
