package businessLogic.repositories;

import java.util.List;

import domainModel.Grade;
import domainModel.League;
import domainModel.Match;

public interface GradeRepository {

	List<Grade> getAllMatchGrades(Match match, League league);

}
