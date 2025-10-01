package businessLogic;

import java.util.function.Consumer;
import java.util.function.Function;

public interface TransactionManager {

	public <T> T fromTransaction(Function<TransactionContext, T> code);

	public void inTransaction(Consumer<TransactionContext> code);

}
