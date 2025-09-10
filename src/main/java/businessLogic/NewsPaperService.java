package businessLogic;

import domainModel.Grade;
import domainModel.MatchDaySerieA;
import domainModel.Player;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

public class NewsPaperService {

	protected final TransactionManager transactionManager;

	public NewsPaperService(TransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	public void setVoteToPlayers(Set<Grade> grades) {
		transactionManager.inTransaction((context) -> {
			Optional<MatchDaySerieA> matchDaySerieA = getMatchDay();
			if (matchDaySerieA.isEmpty()) {
				throw new RuntimeException("Now you can't assign the votes");
			}
			for (Grade grade : grades) {
				if (!(grade.getMatchDay().equals(matchDaySerieA.get()))) {
					throw new RuntimeException("The match date is not correct");
				}
				if (grade.getMark() < -5 || grade.getMark() > 25) {
					throw new IllegalArgumentException("Marks must be between -5 and 25");
				}
			}
			for (Grade grade : grades) {
				context.getGradeRepository().saveGrade(grade);
			}
		});
	}

	public Set<Player> getPlayersToGrade(Player.Club club) {
		return transactionManager.fromTransaction((context) -> context.getPlayerRepository().findByClub(club));
	}

	public Optional<MatchDaySerieA> getMatchDay() {
		return transactionManager.fromTransaction((context) -> {
			LocalDate now = LocalDate.now();
			DayOfWeek dayOfWeek = now.getDayOfWeek();
			Optional<MatchDaySerieA> matchDaySerieA = Optional.empty();
			if (dayOfWeek == DayOfWeek.SATURDAY) {
				matchDaySerieA = context.getMatchDayRepository().getNextMatchDay(now);
			} else if (dayOfWeek == DayOfWeek.SUNDAY) {
				matchDaySerieA = context.getMatchDayRepository().getNextMatchDay(now);
				if (matchDaySerieA.isEmpty()) {
					matchDaySerieA = context.getMatchDayRepository().getPreviousMatchDay(now);
				}
			} else if (dayOfWeek == DayOfWeek.MONDAY) {
				matchDaySerieA = context.getMatchDayRepository().getPreviousMatchDay(now);
			}
			return matchDaySerieA;
		});
	}

}
