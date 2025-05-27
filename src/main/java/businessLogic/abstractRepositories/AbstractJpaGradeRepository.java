package businessLogic.abstractRepositories;

import java.util.List;

import domainModel.Grade;
import domainModel.League;
import domainModel.Match;
import jakarta.persistence.EntityManager;

public interface AbstractJpaGradeRepository {
	List<Grade> getAllMatchGrades(EntityManager session, Match match, League league);
}
