package businessLogic;

import domainModel.Grade;
import domainModel.Player;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.Set;

public class NewsPaperService {

	protected final TransactionManager transactionManager;

	public NewsPaperService(TransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	public void setVoteToPlayers(Set<Grade> grades) {
		transactionManager.inTransaction((context)->{
			for (Grade grade : grades) {
				if(grade.getMark() < -5 || grade.getMark() >25){
					throw new IllegalArgumentException("Marks must be between -5 and 25");
				}
			}
			for (Grade grade : grades) {
				context.getGradeRepository().saveGrade(grade);
			}
		});
	}
	public void getPlayersToGrade() {
	}

}
