package businessLogic;

import businessLogic.abstractRepositories.AbstractJpaContractRepository;
import businessLogic.abstractRepositories.AbstractJpaGradeRepository;
import businessLogic.abstractRepositories.AbstractJpaLeagueRepository;
import businessLogic.abstractRepositories.AbstractJpaMatchRepository;
import businessLogic.abstractRepositories.AbstractJpaPlayerRepository;
import businessLogic.abstractRepositories.AbstractJpaProposalRepository;
import businessLogic.abstractRepositories.AbstractJpaTeamRepository;
import jakarta.persistence.EntityManagerFactory;

public class TransactionContext {
	private final EntityManagerFactory entityManagerFactory;
	private final AbstractJpaLeagueRepository leagueRepository;
	private final AbstractJpaMatchRepository matchRepository;
	private final AbstractJpaPlayerRepository playerRepository;
	private final AbstractJpaTeamRepository teamRepository;
	private final AbstractJpaGradeRepository gradeRepository;
	private final AbstractJpaProposalRepository proposalRepository;
	private final AbstractJpaContractRepository contractRepository;

	public TransactionContext(EntityManagerFactory entityManagerFactory, AbstractJpaLeagueRepository leagueRepository,
					   AbstractJpaMatchRepository matchRepository, AbstractJpaPlayerRepository playerRepository,
					   AbstractJpaTeamRepository teamRepository, AbstractJpaGradeRepository gradeRepository,
					   AbstractJpaProposalRepository proposalRepository, AbstractJpaContractRepository contractRepository) {
		this.entityManagerFactory = entityManagerFactory;
		this.leagueRepository = leagueRepository;
		this.matchRepository = matchRepository;
		this.playerRepository = playerRepository;
		this.teamRepository = teamRepository;
		this.gradeRepository = gradeRepository;
		this.proposalRepository = proposalRepository;
		this.contractRepository = contractRepository;
	}

	public EntityManagerFactory getEntityManagerFactory() {
		return entityManagerFactory;
	}

	public AbstractJpaLeagueRepository getLeagueRepository() {
		return leagueRepository;
	}

	public AbstractJpaMatchRepository getMatchRepository() {
		return matchRepository;
	}

	public AbstractJpaPlayerRepository getPlayerRepository() {
		return playerRepository;
	}

	public AbstractJpaTeamRepository getTeamRepository() {
		return teamRepository;
	}

	public AbstractJpaGradeRepository getGradeRepository() {
		return gradeRepository;
	}

	public AbstractJpaProposalRepository getProposalRepository() {
		return proposalRepository;
	}

	public AbstractJpaContractRepository getContractRepository() {
		return contractRepository;
	}
	
	
}
