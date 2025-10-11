package business.ports.repository;

import java.util.List;
import java.util.Optional;

import domain.League;
import domain.MatchDay;

public interface MatchDayRepository {
	
	List<MatchDay> getAllMatchDays(League league);

	Optional<MatchDay> getPreviousMatchDay(League league);

	Optional<MatchDay> getNextMatchDay(League league);

	Optional<MatchDay> getMatchDay(League league);

	void saveMatchDay(MatchDay matchDay);

}
