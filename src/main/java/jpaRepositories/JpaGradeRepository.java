package jpaRepositories;

import java.util.List;

import businessLogic.repositories.GradeRepository;
import domainModel.Match;
import domainModel.MatchDaySerieA;
import domainModel.NewsPaper;
import domainModel.Player;
import domainModel.Grade;
import domainModel.Grade_;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;

public class JpaGradeRepository extends BaseJpaRepository implements GradeRepository {

	public JpaGradeRepository(EntityManager em) {
		super(em);
	}

	@Override
	public List<Grade> getAllMatchGrades(Match match) {
		EntityManager entityManager = getEntityManager();
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Grade> query = builder.createQuery(Grade.class);
		Root<Grade> gradeRoot = query.from(Grade.class);

		gradeRoot.fetch(Grade_.player, JoinType.INNER);
		gradeRoot.fetch(Grade_.matchDay, JoinType.INNER);

		query.select(gradeRoot);
		
		return entityManager.createQuery(query).getResultList();
	}

	@Override
	public void setGrade(Player player, MatchDaySerieA matchDay, double mark, int goals, int assists,
			NewsPaper newsPaper) {
		// TODO Auto-generated method stub

	}

}
