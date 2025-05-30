package businessLogic;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import domainModel.*;

public class UserService {

	private final TransactionManager transactionManager;

	public UserService(TransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	// League

	public League existingLeague(String leagueName) {
		return transactionManager.fromTransaction(
				(context) -> context.getLeagueRepository().getLeagueByCode(leagueName));
	}

	// Matches

	public Map<MatchDaySerieA, Set<Match>> getAllMatches(League league) {
		return transactionManager.fromTransaction(
				(context) -> context.getMatchRepository().getAllMatches(league));
	}

	// Players

	public List<Player> getAllPlayers() {
		return transactionManager.fromTransaction(
				(context) -> context.getPlayerRepository().findAll());
	}

	public List<Player> getPlayersBySurname(String surname) {
		return transactionManager.fromTransaction(
				(context) -> context.getPlayerRepository().findBySurname(surname));
	}
	
	public List<Player> getPlayersByTeam(FantaTeam team) {
		return transactionManager.fromTransaction(
				(context) -> context.getPlayerRepository().findByTeam(team));
	}

	// Proposals

	public List<Proposal> getAllTeamProposals(League league, FantaTeam team) {
		return transactionManager.fromTransaction(
				(context) -> context.getProposalRepository().getMyProposals(league, team));
	}

	public void acceptProposal(Proposal proposal) {
		transactionManager.inTransaction(
				(context) -> context.getProposalRepository().acceptProposal(proposal));
	}

	public boolean rejectProposal(Proposal proposal) {
		return transactionManager.fromTransaction(
				(context) -> context.getProposalRepository().rejectedProposal(proposal));
	}

	public boolean createProposal(Player requestedPlayer, Player offeredPlayer, FantaTeam myTeam,
			FantaTeam opponentTeam) {
		if (!requestedPlayer.getClass().equals(offeredPlayer.getClass())) {
			throw new IllegalArgumentException("The players don't have the same role");
		}

		return transactionManager.fromTransaction(
				(context) -> {
			Contract requestedContract = context.getContractRepository().getContract(opponentTeam, requestedPlayer);
			Contract offeredContract = context.getContractRepository().getContract(myTeam, offeredPlayer);

			Proposal newProposal = new Proposal.PendingProposal(offeredContract, requestedContract);

			if (context.getProposalRepository().proposalExists(newProposal)) {
				throw new IllegalArgumentException("The proposal already exists");
			}

			return context.getProposalRepository().saveProposal(newProposal);
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
		return transactionManager.fromTransaction(
				(context) -> context.getTeamRepository().getAllTeams(league));
	}

	// Grades

	public List<Grade> getAllMatchGrades(League league, Match match) {
		return transactionManager.fromTransaction(
				(context) -> context.getGradeRepository().getAllMatchGrades(match, league));
	}

}
