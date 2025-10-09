package business;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import business.ports.transaction.TransactionManager;
import domain.Grade;
import domain.MatchDay;
import domain.Player;

public class NewsPaperService {

	protected final TransactionManager transactionManager;

	public NewsPaperService(TransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	public void setVoteToPlayers(Set<Grade> grades) {
		transactionManager.inTransaction((context) -> {
			Optional<MatchDay> matchDaySerieA = getMatchDay();
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

	public Optional<MatchDay> getMatchDay() {
	    return transactionManager.fromTransaction((context) -> {
	        LocalDate now = today();
	        DayOfWeek dayOfWeek = now.getDayOfWeek();
	        Optional<MatchDay> matchDaySerieA = Optional.empty();
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

	protected LocalDate today() {
		return LocalDate.now();
	}

}
