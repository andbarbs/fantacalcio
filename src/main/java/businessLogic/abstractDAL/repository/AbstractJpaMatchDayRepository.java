package businessLogic.abstractDAL.repository;

import java.util.List;

import domainModel.MatchDaySerieA;
import jakarta.persistence.EntityManager;

public interface AbstractJpaMatchDayRepository {
	List<MatchDaySerieA> getAllMatchDays(EntityManager session);
}
