package businessLogic.abstractRepositories;

import java.util.List;

import domainModel.MatchDaySerieA;

public interface MatchDayRepository {
	List<MatchDaySerieA> getAllMatchDays();
}
