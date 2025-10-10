package business;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import business.ports.repository.LeagueRepository;
import business.ports.transaction.TransactionManager;
import domain.*;

public class UserService {

	protected final TransactionManager transactionManager;

	public UserService(TransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	// League

	public void joinLeague(FantaTeam fantaTeam, League league) {
		transactionManager.inTransaction((context) -> {

			LeagueRepository leagueRepository = context.getLeagueRepository();

			final int maxFantaTeamsPerLeague = 8;
			if (leagueRepository.getAllTeams(league).size() >= maxFantaTeamsPerLeague)
				throw new UnsupportedOperationException("Maximum 12 teams per league");

			FantaUser user = fantaTeam.getFantaManager();
			List<League> UserLeagues = leagueRepository.getLeaguesByUser(user);
			if (UserLeagues.contains(league)) {
				throw new IllegalArgumentException("You have already a team in this league");
			} else {
				context.getTeamRepository().saveTeam(fantaTeam);
			}
		});
	}

	// Matches

	public Map<MatchDay, List<Match>> getAllMatches(League league) {

		return transactionManager.fromTransaction((context) -> {

			List<MatchDay> allMatchDays = context.getMatchDayRepository().getAllMatchDays();
			Map<MatchDay, List<Match>> map = new HashMap<MatchDay, List<Match>>();

			for (MatchDay matchDay : allMatchDays) {
				map.put(matchDay, context.getMatchRepository().getAllMatchesByMatchDay(matchDay, league));
			}

			return map;

		});
	}

	public Match getNextMatch(League league, FantaTeam fantaTeam, LocalDate date) {
		return transactionManager.fromTransaction((context) -> {
			Optional<MatchDay> previousMatchDay = context.getMatchDayRepository().getPreviousMatchDay(date);
			if (previousMatchDay.isPresent()) {
				Match previousMatch = context.getMatchRepository().getMatchByMatchDay(previousMatchDay.get(), league,
						fantaTeam);
				Optional<Result> result = context.getResultsRepository().getResult(previousMatch);
				if (result.isEmpty()) {
					throw new RuntimeException("The results for the previous match have not been calculated yet");
				}
			}
			Optional<MatchDay> nextMatchDay = context.getMatchDayRepository().getNextMatchDay(date);
			if (nextMatchDay.isEmpty()) {
				throw new RuntimeException("The league ended");
			} else {
				return context.getMatchRepository().getMatchByMatchDay(nextMatchDay.get(), league, fantaTeam);
			}
		});
	}

	// Players

	public List<Player> getAllPlayers() {
		return transactionManager.fromTransaction((context) -> context.getPlayerRepository().findAll());
	}

	public List<Player> getPlayersBySurname(String surname) {
		return transactionManager.fromTransaction((context) -> context.getPlayerRepository().findBySurname(surname));
	}

	// Proposals

	public List<Proposal> getAllTeamProposals(League league, FantaTeam team) {
		return transactionManager
				.fromTransaction((context) -> context.getProposalRepository().getMyProposals(league, team));
	}

	public void acceptProposal(Proposal.PendingProposal proposal, FantaTeam fantaTeam) {
		transactionManager.inTransaction((context) -> {
			FantaTeam requestingTeam = proposal.getRequestedContract().getTeam();
			FantaTeam offeringTeam = proposal.getOfferedContract().getTeam();
			Player offeredPlayer = proposal.getOfferedContract().getPlayer();
			Player requestedPlayer = proposal.getRequestedContract().getPlayer();
			if (!requestingTeam.isSameTeam(fantaTeam)) {
				throw new IllegalArgumentException("You are not involved in this proposal");
			}
			Optional<Contract> requestedContract = searchContract(fantaTeam, requestedPlayer);
			Optional<Contract> offeredContract = searchContract(offeringTeam, offeredPlayer);

			if (requestedContract.isEmpty() || offeredContract.isEmpty()) {
				rejectProposal(proposal, fantaTeam);
				throw new IllegalArgumentException("One or both players do not play anymore in the teams");
			}
			Contract receivedContract = new Contract(proposal.getOfferedContract().getTeam(),
					proposal.getRequestedContract().getPlayer());
			Contract givenContract = new Contract(proposal.getRequestedContract().getTeam(),
					proposal.getOfferedContract().getPlayer());
			context.getProposalRepository().deleteProposal(proposal);
			context.getContractRepository().deleteContract(proposal.getRequestedContract());
			context.getContractRepository().deleteContract(proposal.getOfferedContract());
			context.getContractRepository().saveContract(receivedContract);
			context.getContractRepository().saveContract(givenContract);
		});
	}

	public void rejectProposal(Proposal.PendingProposal proposal, FantaTeam fantaTeam) {
		transactionManager.inTransaction((context) -> {
			FantaTeam requestingTeam = proposal.getRequestedContract().getTeam();
			FantaTeam offeringTeam = proposal.getOfferedContract().getTeam();

			if (!requestingTeam.isSameTeam(fantaTeam) && !offeringTeam.isSameTeam(fantaTeam)) {
				throw new IllegalArgumentException("You are not involved in this proposal");
			}
			Proposal.RejectedProposal rejectedProposal = new Proposal.RejectedProposal(proposal.getOfferedContract(),
					proposal.getRequestedContract());
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
			Optional<Contract> requestedContract = searchContract(opponentTeam, requestedPlayer);
			Optional<Contract> offeredContract = searchContract(myTeam, offeredPlayer);

			if (requestedContract.isPresent() && offeredContract.isPresent()) {
				Proposal newProposal = new Proposal.PendingProposal(offeredContract.get(), requestedContract.get());

				if (context.getProposalRepository().getProposal(offeredContract.get(), requestedContract.get())
						.isPresent()) {
					throw new IllegalArgumentException("The proposal already exists");
				}
				return context.getProposalRepository().saveProposal(newProposal);
			} else {
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
		return transactionManager.fromTransaction((context) -> context.getTeamRepository().getAllTeams(league));
	}

	public FantaTeam getFantaTeamByUserAndLeague(League league, FantaUser user) {
		return transactionManager
				.fromTransaction((context) -> context.getTeamRepository().getFantaTeamByUserAndLeague(league, user));
	}

	// Grades

	public List<Grade> getAllMatchGrades(Match match, NewsPaper newsPaper) {
		return transactionManager
				.fromTransaction((context) -> context.getGradeRepository().getAllMatchGrades(match, newsPaper));
	}

	// Results

	public Optional<Result> getResultByMatch(Match match) {
		return transactionManager.fromTransaction((context) -> context.getResultsRepository().getResult(match));

	}

	public Optional<LineUp> getLineUpByMatch(Match match, FantaTeam fantaTeam) {
		return transactionManager
				.fromTransaction((context) -> context.getLineUpRepository().getLineUpByMatchAndTeam(match, fantaTeam));
	}

	protected LocalDate today() {
		return LocalDate.now();
	}

	public void saveLineUp(LineUp lineUp) {
		transactionManager.inTransaction((context) -> {
			LocalDate today = today();
			DayOfWeek day = today.getDayOfWeek();

			// Check if is a valid date to save the lineUp
			Match match = lineUp.getMatch();
			LocalDate matchDate = match.getMatchDay().getDate();
			if (today.isAfter(matchDate))
				throw new UnsupportedOperationException("Can't modify the lineup after the match is over");

			if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY)
				throw new UnsupportedOperationException("Can't modify the lineup during Saturday and Sunday");

			League league = lineUp.getTeam().getLeague();
			FantaTeam team = lineUp.getTeam();

			// Check if is legal to save the lineUP
			Optional<MatchDay> previousMatchDay = context.getMatchDayRepository().getPreviousMatchDay(matchDate);
			if (previousMatchDay.isPresent()) {
				Match previousMatch = context.getMatchRepository().getMatchByMatchDay(previousMatchDay.get(), league,
						team);
				Optional<Result> previousMatchResult = context.getResultsRepository().getResult(previousMatch);

				if (previousMatchResult.isEmpty()) {
					throw new UnsupportedOperationException("The grades for the previous match were not calculated");
				}
			}

			// Collect all players from the LineUp
			Set<Player> fieldedPlayers = new HashSet<>();
			fieldedPlayers.addAll(lineUp.extract().starterGoalkeepers());
			fieldedPlayers.addAll(lineUp.extract().starterDefenders());
			fieldedPlayers.addAll(lineUp.extract().starterMidfielders());
			fieldedPlayers.addAll(lineUp.extract().starterForwards());
			fieldedPlayers.addAll(lineUp.extract().substituteGoalkeepers());
			fieldedPlayers.addAll(lineUp.extract().substituteDefenders());
			fieldedPlayers.addAll(lineUp.extract().substituteMidfielders());
			fieldedPlayers.addAll(lineUp.extract().substituteForwards());

			// Collect all players contracted to the team			
			Set<Player> teamPlayers = new HashSet<>();
			teamPlayers.addAll(team.extract().goalkeepers());
			teamPlayers.addAll(team.extract().defenders());
			teamPlayers.addAll(team.extract().midfielders());
			teamPlayers.addAll(team.extract().forwards());

			// Validate ownership
			for (Player player : fieldedPlayers) {
				if (!teamPlayers.contains(player)) {
					throw new IllegalArgumentException(
							"Player " + player + " does not belong to FantaTeam " + team.getName());
				}
			}

			context.getLineUpRepository().getLineUpByMatchAndTeam(match, team)
					.ifPresent(lineup -> context.getLineUpRepository().deleteLineUp(lineup));

			context.getLineUpRepository().saveLineUp(lineUp);	
		});
	}

	protected Optional<Contract> searchContract(FantaTeam team, Player player) {
		return team.getContracts().stream().filter(c -> c.getTeam().equals(team) && c.getPlayer().equals(player))
				.findFirst();
	}
}
