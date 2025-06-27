package businessLogic;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
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

	public void joinLeague(FantaTeam fantaTeam) {
		 transactionManager.inTransaction((context) -> context.getTeamRepository().saveTeam(fantaTeam));
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

	//Results

	public Result getResultByMatch(League league, Match match) {
		return transactionManager.fromTransaction((context)-> context.getResultsRepository().getResult(match));

	}
	//TODO spostarlo nell'admin service
// va nell'admin
/*	public void saveResult(Result result) {
		transactionManager.inTransaction((context) -> context.getResultsRepository().saveResult(result));
	}
*/
	public Optional<LineUp> getLineUpByMatch(League league, Match match, FantaTeam fantaTeam) {
		return transactionManager.fromTransaction((context) ->
				context.getLineUpRepository().getLineUpByMatchAndTeam(league, match, fantaTeam));
    }

	public void saveLineUp(LineUp lineUp, LocalDate today){
		transactionManager.inTransaction((context)-> {
			
			DayOfWeek day = today.getDayOfWeek();
			
			Match match = lineUp.getMatch();
			if(today.isAfter(match.getMatchDaySerieA().getDate()))
				throw new UnsupportedOperationException("Can't modify the lineup after the match is over");
			
			if(day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY)
				throw new UnsupportedOperationException("Can't modify the lineup during Saturday and Sunday");
			
			FantaTeam team = lineUp.getTeam();
			LineUpViewer lineUpViewer = lineUp.extract();
			FantaTeamViewer teamViewer = new FantaTeamViewer(team);	

			// Collect all players from the LineUp
			Set<Player> allPlayers = new HashSet<>();
			allPlayers.addAll(lineUpViewer.starterGoalkeepers());
			allPlayers.addAll(lineUpViewer.starterDefenders());
			allPlayers.addAll(lineUpViewer.starterMidfielders());
			allPlayers.addAll(lineUpViewer.starterForwards());
			allPlayers.addAll(lineUpViewer.substituteGoalkeepers());
			allPlayers.addAll(lineUpViewer.substituteDefenders());
			allPlayers.addAll(lineUpViewer.substituteMidfielders());
			allPlayers.addAll(lineUpViewer.substituteForwards());

			// Collect all players contracted to the team
			Set<Player> teamPlayers = new HashSet<>();
			teamPlayers.addAll(teamViewer.goalkeepers());
			teamPlayers.addAll(teamViewer.defenders());
			teamPlayers.addAll(teamViewer.midfielders());
			teamPlayers.addAll(teamViewer.forwards());

			// Validate ownership
			for (Player player : allPlayers) {
				if (!teamPlayers.contains(player)) {
					throw new IllegalArgumentException("Player " + player + " does not belong to FantaTeam " + team.getName());
				}
			}
			
			if(getLineUpByMatch(team.getLeague(), match, team).isPresent())
				context.getLineUpRepository().deleteLineUp(lineUp);
			
			context.getLineUpRepository().saveLineUp(lineUp);});
	}


}
