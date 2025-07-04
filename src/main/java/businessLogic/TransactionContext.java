package businessLogic;

import businessLogic.repositories.*;
import jakarta.persistence.EntityManagerFactory;

public class TransactionContext {
	private final EntityManagerFactory entityManagerFactory;
	private final LeagueRepository leagueRepository;
	private final MatchRepository matchRepository;
	private final PlayerRepository playerRepository;
	private final FantaTeamRepository teamRepository;
	private final GradeRepository gradeRepository;
	private final ProposalRepository proposalRepository;
	private final ContractRepository contractRepository;
	private final ResultsRepository resultsRepository;
	private final FieldingRepository fieldingRepository;
	private final LineUpRepository lineUpRepository;
	private final MatchDayRepository matchDayRepository;

	public TransactionContext(EntityManagerFactory entityManagerFactory, LeagueRepository leagueRepository,
							  MatchRepository matchRepository, PlayerRepository playerRepository,
							  FantaTeamRepository teamRepository, GradeRepository gradeRepository,
							  ProposalRepository proposalRepository, ContractRepository contractRepository,
							  ResultsRepository resultsRepository, FieldingRepository fieldingRepository,
							  LineUpRepository lineUpRepository,  MatchDayRepository matchDayRepository) {
		this.entityManagerFactory = entityManagerFactory;
		this.leagueRepository = leagueRepository;
		this.matchRepository = matchRepository;
		this.playerRepository = playerRepository;
		this.teamRepository = teamRepository;
		this.gradeRepository = gradeRepository;
		this.proposalRepository = proposalRepository;
		this.contractRepository = contractRepository;
		this.resultsRepository = resultsRepository;
		this.fieldingRepository = fieldingRepository;
		this.lineUpRepository = lineUpRepository;
		this.matchDayRepository = matchDayRepository;
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

	public FantaTeamRepository getTeamRepository() {
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

	public ResultsRepository getResultsRepository() {
		return resultsRepository;
	}

	public FieldingRepository getFieldingRepository() {
		return fieldingRepository;
	}

	public LineUpRepository getLineUpRepository() {
		return lineUpRepository;
	}

	public MatchDayRepository getMatchDayRepository() {
		return matchDayRepository;
	}
}
