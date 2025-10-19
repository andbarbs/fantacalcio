package business.ports.repository;

import java.util.List;
import java.util.Optional;

import domain.League;
import domain.MatchDay;

public interface MatchDayRepository {
	
	List<MatchDay> getAllMatchDays(League league);

	Optional<MatchDay> getLatestEndedMatchDay(League league);

	Optional<MatchDay> getEarliestUpcomingMatchDay(League league);

	Optional<MatchDay> getOngoingMatchDay(League league);

	void saveMatchDay(MatchDay matchDay);

	void updateMatchDay(MatchDay detached);

}
