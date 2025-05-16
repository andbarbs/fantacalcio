package businessLogic.abstractDAL.repository;

import java.util.List;

import domainModel.Grade;
import jakarta.persistence.EntityManager;

public interface AbstractJpaGradeRepository {
	List<Grade> getAllGrades(EntityManager session);
}
