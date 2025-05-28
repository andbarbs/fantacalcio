package businessLogic;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

public class JpaTransactionManager implements TransactionManager {
	
	private final EntityManagerFactory emFactory;

	public JpaTransactionManager(EntityManagerFactory emFactory) {
		this.emFactory = emFactory;
	}

	@Override
	public <T> T fromTransaction(BiFunction<TransactionContext, EntityManager, T> code) {
		TransactionContext context = null;
		EntityManager em = emFactory.createEntityManager();
		EntityTransaction transaction = em.getTransaction();
		try {
			transaction.begin();
			T result = code.apply(context, em);
			transaction.commit();
			return result;
		} catch (Exception e) {
			if (transaction.isActive()) {
				transaction.rollback();
			}
			throw e;
		} finally {
			em.close();
		}
	}

	@Override
	public void inTransaction(BiConsumer<TransactionContext, EntityManager> code) {
		TransactionContext context = null;
		EntityManager em = emFactory.createEntityManager();
		EntityTransaction transaction = em.getTransaction();
		try {
			transaction.begin();
			code.accept(context, em);
			transaction.commit();
		} catch (Exception e) {
			if (transaction.isActive()) {
				transaction.rollback();
			}
			throw e;
		} finally {
			em.close();
		}		
	}
}
