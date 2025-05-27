package businessLogic;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import businessLogic.abstractRepositories.*;
import domainModel.*;
import org.hibernate.SessionFactory;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

public class UserService {

	private SessionFactory sessionFactory;
	private AbstractJpaLeagueRepository leagueRepository;
	private AbstractJpaMatchRepository matchRepository;
	private AbstractJpaPlayerRepository playerRepository;
	private AbstractJpaTeamRepository teamRepository;
	private AbstractJpaGradeRepository gradeRepository;
	private AbstractJpaProposalRepository proposalRepository;
	private AbstractJpaContractRepository contractRepository;

	protected UserService() {
	}

	public UserService(SessionFactory sessionFactory, AbstractJpaLeagueRepository leagueRepository,
			AbstractJpaMatchRepository matchRepository, AbstractJpaPlayerRepository playerRepository,
			AbstractJpaTeamRepository teamRepository, AbstractJpaGradeRepository gradeRepository,
			AbstractJpaProposalRepository proposalRepository, AbstractJpaContractRepository contractRepository) {
		this.sessionFactory = sessionFactory;
		this.leagueRepository = leagueRepository;
		this.matchRepository = matchRepository;
		this.playerRepository = playerRepository;
		this.teamRepository = teamRepository;
		this.gradeRepository = gradeRepository;
		this.proposalRepository = proposalRepository;
		this.contractRepository = contractRepository;
	}

	static <T> T fromSession(EntityManagerFactory factory, Function<EntityManager, T> work) {
		EntityManager em = factory.createEntityManager();
		EntityTransaction transaction = em.getTransaction();
		try {
			transaction.begin();
			T result = work.apply(em);
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

	// League

	public League existingLeague(String leagueName) {
		return fromSession(sessionFactory, em -> leagueRepository.getLeagueByCode(em, leagueName));
	}

	// Matches

	public Map<MatchDaySerieA, Set<Match>> getAllMatches(League league) {
		return fromSession(sessionFactory, em -> matchRepository.getAllMatches(em, league));
	}

	// Players

	public List<Player> getAllPlayers() {
		return fromSession(sessionFactory, em -> playerRepository.findAll(em));
	}

	public List<Player> getPlayersBySurname(String surname) {
		return fromSession(sessionFactory, em -> playerRepository.findBySurname(em, surname));
	}
	
	public List<Player> getPlayersByTeam(FantaTeam team) {
		return fromSession(sessionFactory, em -> playerRepository.findByTeam(em, team));
	}

	// Proposals

	public List<Proposal> getAllTeamProposals(League league, FantaTeam team) {
		return fromSession(sessionFactory, em -> proposalRepository.getMyProposals(em, league, team));
	}

	public boolean acceptProposal(Proposal proposal) {
		// come gestiamo lo swap dei contratti?
		return fromSession(sessionFactory, em -> proposalRepository.acceptedProposal(em, proposal));
	}

	public boolean rejectProposal(Proposal proposal) {
		return fromSession(sessionFactory, em -> proposalRepository.rejectedProposal(em, proposal));
	}

	public boolean createProposal(Player requestedPlayer, Player offeredPlayer, FantaTeam myTeam,
			FantaTeam opponentTeam) {
		if (!requestedPlayer.getClass().equals(offeredPlayer.getClass())) {
			throw new IllegalArgumentException("The players don't have the same role");
		}

		return fromSession(sessionFactory, em -> {
			Contract requestedContract = contractRepository.getContract(em, opponentTeam, requestedPlayer);
			Contract offeredContract = contractRepository.getContract(em, myTeam, offeredPlayer);

			Proposal newProposal = new Proposal(offeredContract, requestedContract, Proposal.Status.PENDING);

			if (proposalRepository.proposalExists(em, newProposal)) {
				throw new IllegalArgumentException("The proposal already exists");
			}

			return proposalRepository.saveProposal(em, newProposal);
		});
	}

	// Standings

	public List<FantaTeam> getStandings(League league) {
		Set<FantaTeam> teams = getAllFantaTeams(league);

		return teams.stream().sorted(Comparator.comparing(FantaTeam::getPoints).reversed())
				.collect(Collectors.toList());
	}

	// Teams

	public Set<FantaTeam> getAllFantaTeams(League league) {
		return fromSession(sessionFactory, em -> teamRepository.getAllTeams(em, league));
	}

	// Grades

	public List<Grade> getAllMatchGrades(League league, Match match) {
		return fromSession(sessionFactory, em -> gradeRepository.getAllMatchGrades(em, match, league));
	}

}
