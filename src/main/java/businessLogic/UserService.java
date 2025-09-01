package businessLogic;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import domainModel.*;

public class UserService {

	protected final TransactionManager transactionManager;

	public UserService(TransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	// League

	public void createLeague(String leagueName, FantaUser admin, NewsPaper newsPaper, String leagueCode) {
		transactionManager.inTransaction((context) -> {
			if(context.getLeagueRepository().getLeagueByCode(leagueCode).isEmpty()) {
				League league = new League(admin, leagueName, newsPaper, leagueCode);
				context.getLeagueRepository().saveLeague(league);
			}else{
				throw new IllegalArgumentException("A league with the same league code already exists");
			}
		});
	}

	public void joinLeague(FantaTeam fantaTeam, League league) {
		 transactionManager.inTransaction((context) -> {
			 FantaUser user = fantaTeam.getFantaManager();
			 List<League> UserLeagues = context.getLeagueRepository().getLeaguesByUser(user);
			 if(UserLeagues.contains(league)) {
				 throw new IllegalArgumentException("You have already a team in this league");
			 } else {
				 context.getTeamRepository().saveTeam(fantaTeam);
			 }
		 });
	}

	// Matches

	public Map<MatchDaySerieA, Set<Match>> getAllMatches(League league) {
		return transactionManager.fromTransaction(
				(context) -> context.getMatchRepository().getAllMatches(league));
	}

	public Match getNextMatch(League league, FantaTeam fantaTeam, LocalDate date) {
		return transactionManager.fromTransaction((context) -> {
			Optional<MatchDaySerieA> previousMatchDay = context.getMatchDayRepository().getPreviousMatchDay(date);
			if (previousMatchDay.isPresent()) {
				Match previousMatch = context.getMatchRepository().getMatchByMatchDay(previousMatchDay.get(), league, fantaTeam);
				Optional<Result> result = context.getResultsRepository().getResult(previousMatch);
				if(result.isEmpty()){
					throw new RuntimeException("The results for the previous match have not been calculated yet");
				}
			}
			Optional<MatchDaySerieA> nextMatchDay = context.getMatchDayRepository().getNextMatchDay(date);
			if(nextMatchDay.isEmpty()){
				throw new RuntimeException("The league ended");
			}else{
				return context.getMatchRepository().getMatchByMatchDay(nextMatchDay.get(), league, fantaTeam);
			}
		});
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


	// Proposals

	public List<Proposal> getAllTeamProposals(League league, FantaTeam team) {
		return transactionManager.fromTransaction(
				(context) -> context.getProposalRepository().getMyProposals(league, team));
	}

	public void acceptProposal(Proposal.PendingProposal proposal) {
		transactionManager.inTransaction(
				(context) -> {
					Contract receivedContract = new Contract(proposal.getOfferedContract().getTeam(),
							proposal.getRequestedContract().getPlayer());
					Contract givenContract = new Contract(proposal.getRequestedContract().getTeam(),
							proposal.getOfferedContract().getPlayer());
					context.getContractRepository().deleteContract(proposal.getRequestedContract());
					context.getContractRepository().deleteContract(proposal.getOfferedContract());
					context.getContractRepository().saveContract(receivedContract);
					context.getContractRepository().saveContract(givenContract);
					context.getProposalRepository().deleteProposal(proposal);
				});
	}

	public void rejectProposal(Proposal.PendingProposal proposal) {
		transactionManager.inTransaction(
				(context) -> {
					Proposal.RejectedProposal rejectedProposal = new Proposal.
							RejectedProposal(proposal.getOfferedContract(), proposal.getRequestedContract());
					context.getProposalRepository().deleteProposal(proposal);
					context.getProposalRepository().saveProposal(rejectedProposal);
				});
	}

	public boolean createProposal(Player requestedPlayer, Player offeredPlayer, FantaTeam myTeam,
			FantaTeam opponentTeam) {
		if (!requestedPlayer.getClass().equals(offeredPlayer.getClass())) {
			throw new IllegalArgumentException("The players don't have the same role");
		}

		return transactionManager.fromTransaction((context) -> {
			Optional<Contract> requestedContract = opponentTeam.getContracts().stream().filter(c -> c.getTeam().equals(opponentTeam) && c.getPlayer().equals(requestedPlayer)).findFirst();
			Optional<Contract> offeredContract = myTeam.getContracts().stream()
							.filter(c -> c.getTeam().equals(myTeam) && c.getPlayer().equals(offeredPlayer))
							.findFirst();
			if(requestedContract.isPresent() && offeredContract.isPresent()){
				Proposal newProposal = new Proposal.PendingProposal(offeredContract.get(), requestedContract.get());

				if (context.getProposalRepository().getProposal(offeredContract.get(), requestedContract.get()).isEmpty()) {
					throw new IllegalArgumentException("The proposal already exists");
				}
				return context.getProposalRepository().saveProposal(newProposal);
			} else{
				return false;
			}
		});
	}

	// Standings

	public List<FantaTeam> getStandings(League league) {
		return getAllFantaTeams(league).stream().sorted(Comparator.comparing(FantaTeam::getPoints).reversed())
				.collect(Collectors.toList());
	}

	// Teams

	public List<FantaTeam> getAllFantaTeams(League league) {
		return transactionManager.fromTransaction(
				(context) -> context.getTeamRepository().getAllTeams(league));
	}

	public FantaTeam getFantaTeamByUserAndLeague(League league, FantaUser user) {
		return transactionManager.fromTransaction((context)->
				context.getTeamRepository().getFantaTeamByUserAndLeague(league, user));
	}

	// Grades

	public List<Grade> getAllMatchGrades(League league, Match match) {
		return transactionManager.fromTransaction(
				(context) -> context.getGradeRepository().getAllMatchGrades(match));
	}

	//Results

	public Optional<Result> getResultByMatch(League league, Match match) {
		return transactionManager.fromTransaction((context)-> context.getResultsRepository().getResult(match));

	}

	public Optional<LineUp> getLineUpByMatch(League league, Match match, FantaTeam fantaTeam) {
		return transactionManager.fromTransaction((context) ->
				context.getLineUpRepository().getLineUpByMatchAndTeam(match, fantaTeam));
    }

	public void saveLineUp(LineUp lineUp){
		transactionManager.inTransaction((context)-> {
			LocalDate today = LocalDate.now();
			DayOfWeek day = today.getDayOfWeek();
			
			Match match = lineUp.getMatch();
			LocalDate matchDate = match.getMatchDaySerieA().getDate();
			if(today.isAfter(matchDate))
				throw new UnsupportedOperationException("Can't modify the lineup after the match is over");
			
			if(day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY)
				throw new UnsupportedOperationException("Can't modify the lineup during Saturday and Sunday");

			Optional<MatchDaySerieA> previousMatchDay = context.getMatchDayRepository().getPreviousMatchDay(matchDate);
			if(previousMatchDay.isPresent()){
				Match previousMatch = context.getMatchRepository().getMatchByMatchDay(previousMatchDay.get(),lineUp.getTeam().getLeague(), lineUp.getTeam());
				Optional<Result> previousMatchResult = context.getResultsRepository().getResult(previousMatch);
				if(previousMatchResult.isEmpty()){
					throw new UnsupportedOperationException("The grades for the previous match were not calculated");
				}
			}
			
			League league = lineUp.getTeam().getLeague();
			FantaUser user = lineUp.getTeam().getFantaManager();
			FantaTeam team = getFantaTeamByUserAndLeague(league, user);

			// Collect all players from the LineUp
			Set<Player> allPlayers = new HashSet<>();
			allPlayers.addAll(lineUp.extract().starterGoalkeepers());
			allPlayers.addAll(lineUp.extract().starterDefenders());
			allPlayers.addAll(lineUp.extract().starterMidfielders());
			allPlayers.addAll(lineUp.extract().starterForwards());
			allPlayers.addAll(lineUp.extract().substituteGoalkeepers());
			allPlayers.addAll(lineUp.extract().substituteDefenders());
			allPlayers.addAll(lineUp.extract().substituteMidfielders());
			allPlayers.addAll(lineUp.extract().substituteForwards());

			// Collect all players contracted to the team
			Set<Player> teamPlayers = new HashSet<>();
			teamPlayers.addAll(team.extract().goalkeepers());
			teamPlayers.addAll(team.extract().defenders());
			teamPlayers.addAll(team.extract().midfielders());
			teamPlayers.addAll(team.extract().forwards());

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
