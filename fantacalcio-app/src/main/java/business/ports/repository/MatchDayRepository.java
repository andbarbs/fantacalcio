package business.ports.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import domain.MatchDaySerieA;

public interface MatchDayRepository {
	
	List<MatchDaySerieA> getAllMatchDays();

	Optional<MatchDaySerieA> getPreviousMatchDay(LocalDate date);

	Optional<MatchDaySerieA> getNextMatchDay(LocalDate date);

	Optional<MatchDaySerieA> getMatchDay(LocalDate date);

	void saveMatchDay(MatchDaySerieA matchDay);

}
