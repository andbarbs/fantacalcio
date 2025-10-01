package businessLogic.repositories;

import domainModel.FantaTeam;
import domainModel.LineUp;
import domainModel.Match;

import java.util.Optional;

public interface LineUpRepository {

	void saveLineUp(LineUp lineUp);

	void deleteLineUp(LineUp lineUp);

	Optional<LineUp> getLineUpByMatchAndTeam(Match match, FantaTeam fantaTeam);
	
}
