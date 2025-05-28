package businessLogic;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import jakarta.persistence.EntityManager;



public interface TransactionManager {	
	
	public <T> T fromTransaction(BiFunction<TransactionContext, EntityManager, T> code);
	
	public void inTransaction(BiConsumer<TransactionContext, EntityManager> code);

}
