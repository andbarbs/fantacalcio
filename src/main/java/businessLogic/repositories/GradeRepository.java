package businessLogic.repositories;

import java.util.List;

import domainModel.Grade;
import domainModel.Match;
import domainModel.MatchDaySerieA;
import domainModel.NewsPaper;
import domainModel.Player;

public interface GradeRepository {

	List<Grade> getAllMatchGrades(Match match);
	
	void setGrade(Player player, MatchDaySerieA matchDay, double mark, int goals, int assists, NewsPaper newsPaper);

}
