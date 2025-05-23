package businessLogic.abstractDAL.repository;

import java.util.Set;

import domainModel.Grade;
import domainModel.League;
import domainModel.Match;
import jakarta.persistence.EntityManager;

public interface AbstractJpaGradeRepository {
	Set<Grade> getAllMatchGrades(EntityManager session, Match match, League league);
}
