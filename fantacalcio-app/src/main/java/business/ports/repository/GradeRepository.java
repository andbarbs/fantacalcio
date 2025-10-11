package business.ports.repository;

import java.util.List;

import domain.Grade;
import domain.MatchDay;

public interface GradeRepository {

	List<Grade> getAllMatchGrades(MatchDay matchDay);
	
	void saveGrade(Grade grade);

}
