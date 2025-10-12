package business;

import java.util.Optional;
import java.util.Set;

import business.ports.transaction.TransactionManager;
import domain.Grade;
import domain.League;
import domain.MatchDay;
import domain.Player;

public class NewsPaperService {

	protected final TransactionManager transactionManager;

	public NewsPaperService(TransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	public void save(Set<Grade> grades) {
		transactionManager.inTransaction((context) -> {
            Grade anyGrade = grades.stream().findAny().orElseThrow(() -> new RuntimeException("No grades found"));
			Optional<MatchDay> matchDaySerieA = context.getMatchDayRepository().getOngoingMatchDay(anyGrade.getMatchDay().getLeague());
			if (matchDaySerieA.isEmpty()) {
				throw new RuntimeException("Now you can't assign the votes");
			}
			for (Grade grade : grades) {
				if (!(grade.getMatchDay().equals(matchDaySerieA.get()))) {
					throw new RuntimeException("The matchDay is not the present one or is of another League");
				}
				if (grade.getMark() <= -5 || grade.getMark() >= 25) {
					throw new IllegalArgumentException("Marks must be between -5 and 25");
				}
			}
			for (Grade grade : grades) {
				context.getGradeRepository().saveGrade(grade);
			}
		});
	}

	public Set<Player> getPlayersToGrade(League league) {
		return transactionManager.fromTransaction((context) -> context.getPlayerRepository().getAllInLeague(league));
	}

}
