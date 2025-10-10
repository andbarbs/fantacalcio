package business.ports.repository;

import java.util.List;
import java.util.Optional;

import domain.League;
import domain.MatchDaySerieA;

public interface MatchDayRepository {
	
	List<MatchDaySerieA> getAllMatchDays(League league);

	Optional<MatchDaySerieA> getPreviousMatchDay(League league);

	Optional<MatchDaySerieA> getNextMatchDay(League league);

	Optional<MatchDaySerieA> getMatchDay(League league);

	void saveMatchDay(MatchDaySerieA matchDay);

}
