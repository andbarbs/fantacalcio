package dal.transaction.jpa;

import java.util.function.Consumer;
import java.util.function.Function;

import business.ports.transaction.TransactionManager;
import dal.repository.jpa.JpaContractRepository;
import dal.repository.jpa.JpaFantaTeamRepository;
import dal.repository.jpa.JpaFantaUserRepository;
import dal.repository.jpa.JpaFieldingRepository;
import dal.repository.jpa.JpaGradeRepository;
import dal.repository.jpa.JpaLeagueRepository;
import dal.repository.jpa.JpaLineUpRepository;
import dal.repository.jpa.JpaMatchDayRepository;
import dal.repository.jpa.JpaMatchRepository;
import dal.repository.jpa.JpaNewsPaperRepository;
import dal.repository.jpa.JpaPlayerRepository;
import dal.repository.jpa.JpaProposalRepository;
import dal.repository.jpa.JpaResultsRepository;
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

		EntityManager em = emFactory.createEntityManager();
		EntityTransaction transaction = em.getTransaction();
		try {
			transaction.begin();
			T result = code.apply(new TransactionContext(
										new JpaLeagueRepository(em), 
										new JpaMatchRepository(em),
										new JpaPlayerRepository(em), 
										new JpaFantaTeamRepository(em), 
										new JpaGradeRepository(em),
										new JpaProposalRepository(em), 
										new JpaContractRepository(em), 
										new JpaResultsRepository(em),
										new JpaFieldingRepository(em), 
										new JpaLineUpRepository(em), 
										new JpaMatchDayRepository(em),
										new JpaNewsPaperRepository(em), 
										new JpaFantaUserRepository(em)));
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
