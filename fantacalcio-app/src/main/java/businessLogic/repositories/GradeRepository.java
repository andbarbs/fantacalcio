package businessLogic.repositories;

import java.util.List;

import domainModel.Grade;
import domainModel.League;
import domainModel.Match;
import domainModel.NewsPaper;

public interface GradeRepository {
	List<Grade> getAllMatchGrades(Match match, League league);

	List<Grade> getAllMatchGrades(Match match, NewsPaper newsPaper);
}
