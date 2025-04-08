package businessLogic.abstractDAL.repository;

import java.util.List;

import domainModel.MatchDaySerieA;

public interface MatchDayRepository {
	List<MatchDaySerieA> getAllMatchDays();
}
