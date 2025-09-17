package businessLogic;

import java.util.function.Consumer;
import java.util.function.Function;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jpaRepositories.JpaContractRepository;
import jpaRepositories.JpaFantaTeamRepository;
import jpaRepositories.JpaFantaUserRepository;
import jpaRepositories.JpaFieldingRepository;
import jpaRepositories.JpaGradeRepository;
import jpaRepositories.JpaLeagueRepository;
import jpaRepositories.JpaLineUpRepository;
import jpaRepositories.JpaMatchDayRepository;
import jpaRepositories.JpaMatchRepository;
import jpaRepositories.JpaNewsPaperRepository;
import jpaRepositories.JpaPlayerRepository;
import jpaRepositories.JpaProposalRepository;
import jpaRepositories.JpaResultsRepository;

public class JpaTransactionManager implements TransactionManager {

	private final EntityManagerFactory emFactory;

	public JpaTransactionManager(EntityManagerFactory emFactory) {
		this.emFactory = emFactory;
	}

	@Override
	public <T> T fromTransaction(Function<TransactionContext, T> code) {

		EntityManager em = emFactory.createEntityManager();
		TransactionContext context = new TransactionContext(emFactory, new JpaLeagueRepository(em),
				new JpaMatchRepository(em), new JpaPlayerRepository(em), new JpaFantaTeamRepository(em),
				new JpaGradeRepository(em), new JpaProposalRepository(em), new JpaContractRepository(em),
				new JpaResultsRepository(em), new JpaFieldingRepository(em), new JpaLineUpRepository(em),
				new JpaMatchDayRepository(em), new JpaNewsPaperRepository(em), new JpaFantaUserRepository(em));

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
		fromTransaction((context) -> {
			code.accept(context);
			return true;
		});
	}
}
