package business.ports.transaction;

import java.util.function.Consumer;
import java.util.function.Function;

import business.ports.repository.ContractRepository;
import business.ports.repository.FantaTeamRepository;
import business.ports.repository.FantaUserRepository;
import business.ports.repository.FieldingRepository;
import business.ports.repository.GradeRepository;
import business.ports.repository.LeagueRepository;
import business.ports.repository.LineUpRepository;
import business.ports.repository.MatchDayRepository;
import business.ports.repository.MatchRepository;
import business.ports.repository.PlayerRepository;
import business.ports.repository.ProposalRepository;
import business.ports.repository.ResultsRepository;

public interface TransactionManager {

	public <T> T fromTransaction(Function<TransactionContext, T> code);

	public void inTransaction(Consumer<TransactionContext> code);
	
	public static final class TransactionContext {
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
		private final FantaUserRepository fantaUserRepository;

		public TransactionContext(LeagueRepository leagueRepository,
								  MatchRepository matchRepository, PlayerRepository playerRepository,
								  FantaTeamRepository teamRepository, GradeRepository gradeRepository,
								  ProposalRepository proposalRepository, ContractRepository contractRepository,
								  ResultsRepository resultsRepository, FieldingRepository fieldingRepository,
								  LineUpRepository lineUpRepository,  MatchDayRepository matchDayRepository
								  , FantaUserRepository fantaUserRepository) {
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
			this.fantaUserRepository = fantaUserRepository;
			
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

		public FantaUserRepository getFantaUserRepository() {
			return fantaUserRepository;
		}
	}
}
