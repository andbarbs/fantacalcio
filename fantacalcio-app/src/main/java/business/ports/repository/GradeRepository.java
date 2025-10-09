package business.ports.repository;

import java.util.List;

import domain.Grade;
import domain.Match;

public interface GradeRepository {

	List<Grade> getAllMatchGrades(Match match, NewsPaper newsPaper);
	
	void saveGrade(Grade grade);

}
