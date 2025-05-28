package businessLogic;

import java.util.function.Consumer;
import java.util.function.Function;

import businessLogic.abstractRepositories.AbstractJpaGradeRepository;
import businessLogic.abstractRepositories.AbstractJpaMatchDayRepository;
import businessLogic.abstractRepositories.AbstractJpaPlayerRepository;



public interface TransactionManager {
	
	public class TransactionContext {
	    private final AbstractJpaGradeRepository gradeRepository;
	    private final AbstractJpaMatchDayRepository matchDayRepository;
	    private final AbstractJpaPlayerRepository playerRepository;
	    
		public TransactionContext(AbstractJpaGradeRepository gradeRepository,
				AbstractJpaMatchDayRepository matchDayRepository, AbstractJpaPlayerRepository playerRepository) {
			this.gradeRepository = gradeRepository;
			this.matchDayRepository = matchDayRepository;
			this.playerRepository = playerRepository;
		}

		public AbstractJpaGradeRepository getGradeRepository() {
			return gradeRepository;
		}

		public AbstractJpaMatchDayRepository getMatchDayRepository() {
			return matchDayRepository;
		}

		public AbstractJpaPlayerRepository getPlayerRepository() {
			return playerRepository;
		}	    
	}
	
	
	public <T> T fromTransaction(Function<TransactionContext, T> code);
	
	public void inTransaction(Consumer<TransactionContext> code);

}
