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
				(context, em) -> context.getLeagueRepository().getLeagueByCode(em, leagueName));
	}

	// Matches

	public Map<MatchDaySerieA, Set<Match>> getAllMatches(League league) {
		return transactionManager.fromTransaction(
				(context, em) -> context.getMatchRepository().getAllMatches(em, league));
	}

	// Players

	public List<Player> getAllPlayers() {
		return transactionManager.fromTransaction(
				(context, em) -> context.getPlayerRepository().findAll(em));
	}

	public List<Player> getPlayersBySurname(String surname) {
		return transactionManager.fromTransaction(
				(context, em) -> context.getPlayerRepository().findBySurname(em, surname));
	}
	
	public List<Player> getPlayersByTeam(FantaTeam team) {
		return transactionManager.fromTransaction(
				(context, em) -> context.getPlayerRepository().findByTeam(em, team));
	}

	// Proposals

	public List<Proposal> getAllTeamProposals(League league, FantaTeam team) {
		return transactionManager.fromTransaction(
				(context, em) -> context.getProposalRepository().getMyProposals(em, league, team));
	}

	public void acceptProposal(Proposal proposal) {
		transactionManager.inTransaction(
				(context, em) -> context.getProposalRepository().acceptProposal(em, proposal));
	}

	public boolean rejectProposal(Proposal proposal) {
		return transactionManager.fromTransaction(
				(context, em) -> context.getProposalRepository().rejectedProposal(em, proposal));
	}

	public boolean createProposal(Player requestedPlayer, Player offeredPlayer, FantaTeam myTeam,
			FantaTeam opponentTeam) {
		if (!requestedPlayer.getClass().equals(offeredPlayer.getClass())) {
			throw new IllegalArgumentException("The players don't have the same role");
		}

		return transactionManager.fromTransaction(
				(context, em) -> {
			Contract requestedContract = context.getContractRepository().getContract(em, opponentTeam, requestedPlayer);
			Contract offeredContract = context.getContractRepository().getContract(em, myTeam, offeredPlayer);

			Proposal newProposal = new Proposal.PendingProposal(offeredContract, requestedContract);

			if (context.getProposalRepository().proposalExists(em, newProposal)) {
				throw new IllegalArgumentException("The proposal already exists");
			}

			return context.getProposalRepository().saveProposal(em, newProposal);
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
				(context, em) -> context.getTeamRepository().getAllTeams(em, league));
	}

	// Grades

	public List<Grade> getAllMatchGrades(League league, Match match) {
		return transactionManager.fromTransaction(
				(context, em) -> context.getGradeRepository().getAllMatchGrades(em, match, league));
	}

}
