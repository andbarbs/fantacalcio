package business.ports.repository;

import java.util.List;

import domain.Grade;
import domain.Match;
import domain.MatchDaySerieA;

public interface GradeRepository {

	List<Grade> getAllMatchGrades(MatchDaySerieA matchDay);
	
	void saveGrade(Grade grade);

}
