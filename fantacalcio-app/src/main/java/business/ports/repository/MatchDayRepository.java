package business.ports.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import domain.MatchDay;

public interface MatchDayRepository {
	
	List<MatchDay> getAllMatchDays();

	Optional<MatchDay> getPreviousMatchDay(LocalDate date);

	Optional<MatchDay> getNextMatchDay(LocalDate date);

	Optional<MatchDay> getMatchDay(LocalDate date);

	void saveMatchDay(MatchDay matchDay);

}
