package businessLogic.repositories;

import java.util.List;

import domainModel.Grade;
import domainModel.Match;
import domainModel.NewsPaper;

public interface GradeRepository {

	List<Grade> getAllMatchGrades(Match match, NewsPaper newsPaper);
	
	void saveGrade(Grade grade);

}
