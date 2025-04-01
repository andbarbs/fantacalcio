package businessLogic.abstractDAL.repository;

import java.util.List;

import domainModel.Grade;

public interface GradeRepository {
	List<Grade> getAllGrades();
}
