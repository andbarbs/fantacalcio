package businessLogic;

import businessLogic.abstractRepositories.ContractRepository;
import businessLogic.abstractRepositories.GradeRepository;
import businessLogic.abstractRepositories.LeagueRepository;
import businessLogic.abstractRepositories.MatchRepository;
import businessLogic.abstractRepositories.PlayerRepository;
import businessLogic.abstractRepositories.ProposalRepository;
import businessLogic.abstractRepositories.TeamRepository;
import jakarta.persistence.EntityManagerFactory;

public class TransactionContext {
	private final EntityManagerFactory entityManagerFactory;
	private final LeagueRepository leagueRepository;
	private final MatchRepository matchRepository;
	private final PlayerRepository playerRepository;
	private final TeamRepository teamRepository;
	private final GradeRepository gradeRepository;
	private final ProposalRepository proposalRepository;
	private final ContractRepository contractRepository;

	public TransactionContext(EntityManagerFactory entityManagerFactory, LeagueRepository leagueRepository,
					   MatchRepository matchRepository, PlayerRepository playerRepository,
					   TeamRepository teamRepository, GradeRepository gradeRepository,
					   ProposalRepository proposalRepository, ContractRepository contractRepository) {
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

	public LeagueRepository getLeagueRepository() {
		return leagueRepository;
	}

	public MatchRepository getMatchRepository() {
		return matchRepository;
	}

	public PlayerRepository getPlayerRepository() {
		return playerRepository;
	}

	public TeamRepository getTeamRepository() {
		return teamRepository;
	}

	public GradeRepository getGradeRepository() {
		return gradeRepository;
	}

	public ProposalRepository getProposalRepository() {
		return proposalRepository;
	}

	public ContractRepository getContractRepository() {
		return contractRepository;
	}
	
	
}
