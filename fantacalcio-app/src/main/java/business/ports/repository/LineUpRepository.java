package business.ports.repository;

import java.util.Optional;

import domain.FantaTeam;
import domain.LineUp;
import domain.Match;

public interface LineUpRepository {

	void saveLineUp(LineUp lineUp);

	void deleteLineUp(LineUp lineUp);

	Optional<LineUp> getLineUpByMatchAndTeam(Match match, FantaTeam fantaTeam);
	
}
