package businessLogic;

import java.util.function.Consumer;
import java.util.function.Function;

import concreteJpaRepositories.JpaGradeRepository;
import concreteJpaRepositories.JpaMatchDayRepository;
import concreteJpaRepositories.JpaPlayerRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

public class JpaTransactionManager implements TransactionManager {
	
	private final EntityManagerFactory emFactory;

	public JpaTransactionManager(EntityManagerFactory emFactory) {
		this.emFactory = emFactory;
	}

	@Override
	public <T> T fromTransaction(Function<TransactionContext, T> code) {
		TransactionContext context = new TransactionContext(
				new JpaGradeRepository(null), 
				new JpaMatchDayRepository(null), 
				new JpaPlayerRepository());
		EntityManager em = emFactory.createEntityManager();
		EntityTransaction transaction = em.getTransaction();
		try {
			transaction.begin();
			T result = code.apply(context);
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
	public void inTransaction(Consumer<TransactionContext> code) {
		TransactionContext context = new TransactionContext(
				new JpaGradeRepository(null), 
				new JpaMatchDayRepository(null), 
				new JpaPlayerRepository());
		EntityManager em = emFactory.createEntityManager();
		EntityTransaction transaction = em.getTransaction();
		try {
			transaction.begin();
			code.accept(context);
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
