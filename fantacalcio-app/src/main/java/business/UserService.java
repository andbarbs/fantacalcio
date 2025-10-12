package business;

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
				throw new UnsupportedOperationException("Maximum 8 teams per league");

			FantaUser user = fantaTeam.getFantaManager();
			Set<League> UserLeagues = leagueRepository.getLeaguesByMember(user);
			if (UserLeagues.contains(league)) {
				throw new IllegalArgumentException("You have already a team in this league");
			} else {
				context.getTeamRepository().saveTeam(fantaTeam);
			}
		});
	}

    //TODO Testa
    //Mi fido che la lista sia stata passata bene
    public void joinLeagueAsJournalist(League league, FantaUser journalist) {
        transactionManager.inTransaction((context) -> {
            if (league.getNewsPaper() != null) {
                throw new IllegalStateException("La lega ha già un giornale associato!");
            }
            league.setNewsPaper(journalist);
        });
    }

	// Matches

	public Map<MatchDay, List<Match>> getAllMatches(League league) {

		return transactionManager.fromTransaction((context) -> {

			List<MatchDay> allMatchDays = context.getMatchDayRepository().getAllMatchDays(league);
			Map<MatchDay, List<Match>> map = new HashMap<>();

			for (MatchDay matchDay : allMatchDays) {
				map.put(matchDay, context.getMatchRepository().getAllMatchesIn(matchDay));
			}

			return map;

		});
	}

    //TODO mi sa non serve a niente
	public Optional<Match> getNextMatch(League league, FantaTeam fantaTeam) {
		// TODO molto da vedere
		return transactionManager.fromTransaction((context) -> {
			Optional<MatchDay> previousMatchDay = context.getMatchDayRepository().getLatestEndedMatchDay(league);
			if (previousMatchDay.isPresent()) {
				Optional<Match> previousMatch = context.getMatchRepository().getMatchBy(previousMatchDay.get(),
						fantaTeam);
				Optional<Result> result = context.getResultsRepository().getResult(previousMatch.get());
				if (result.isEmpty()) {
					throw new RuntimeException("The results for the previous match have not been calculated yet");
				}
			}
			Optional<MatchDay> nextMatchDay = context.getMatchDayRepository().getEarliestUpcomingMatchDay(league);
			if (nextMatchDay.isEmpty()) {
				throw new RuntimeException("The league ended");
			} else {
				return context.getMatchRepository().getMatchBy(nextMatchDay.get(), fantaTeam);
			}
		});
	}

	// Players

	public Set<Player> getAllPlayers() {
		return transactionManager.fromTransaction((context) -> context.getPlayerRepository().findAll());
	}

	public List<Player> getPlayersBySurname(String surname) {
		return transactionManager.fromTransaction((context) -> context.getPlayerRepository().findBySurname(surname));
	}

	// Proposals

    //TODO league non serve
	public Set<Proposal> getAllTeamProposals(League league, FantaTeam team) {
		return transactionManager
				.fromTransaction((context) -> context.getProposalRepository().getProposalsFor(team));
	}

    //TODO ritesta
	public void acceptProposal(Proposal proposal, FantaTeam fantaTeam) {
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
                context.getProposalRepository().deleteProposal(proposal);
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

	public void rejectProposal(Proposal proposal, FantaTeam fantaTeam) {
		transactionManager.inTransaction((context) -> {
			FantaTeam requestingTeam = proposal.getRequestedContract().getTeam();
			FantaTeam offeringTeam = proposal.getOfferedContract().getTeam();

			if (!requestingTeam.isSameTeam(fantaTeam) && !offeringTeam.isSameTeam(fantaTeam)) {
				throw new IllegalArgumentException("You are not involved in this proposal");
			}
			context.getProposalRepository().deleteProposal(proposal);
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
				Proposal newProposal = new Proposal(offeredContract.get(), requestedContract.get());

				if (context.getProposalRepository().getProposalBy(offeredContract.get(), requestedContract.get())
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
		// TODO what if no Teams exist?
		return getAllFantaTeams(league)
				.stream()
				.sorted(Comparator.comparing(FantaTeam::getPoints).reversed())
				.collect(Collectors.toList());
	}

	// Teams

	public Set<FantaTeam> getAllFantaTeams(League league) {
		return transactionManager.fromTransaction((context) -> context.getTeamRepository().getAllTeams(league));
	}

	public Optional<FantaTeam> getFantaTeamByUserAndLeague(League league, FantaUser user) {
		return transactionManager
				.fromTransaction((context) -> context.getTeamRepository().getFantaTeamByUserAndLeague(league, user));
	}

	// Grades

	public List<Grade> getAllMatchGrades(Match match) {
		return transactionManager
				.fromTransaction((context) -> context.getGradeRepository().getAllMatchGrades(match.getMatchDaySerieA()));
	}

	// Results

	public Optional<Result> getResultByMatch(Match match) {
		return transactionManager.fromTransaction((context) -> context.getResultsRepository().getResult(match));

	}

	public Optional<LineUp> getLineUpByMatch(Match match, FantaTeam fantaTeam) {
		return transactionManager
				.fromTransaction((context) -> context.getLineUpRepository().getLineUpByMatchAndTeam(match, fantaTeam));
	}

	public void saveLineUp(LineUp lineUp) {
		transactionManager.inTransaction((context) -> {
			// Check if is a valid date to save the lineUp
			Match match = lineUp.getMatch();
            if(match.getMatchDaySerieA().getStatus() == MatchDay.Status.PAST ||
                    match.getMatchDaySerieA().getStatus() == MatchDay.Status.PRESENT){
                throw new UnsupportedOperationException("Can't modify the lineup after the match is over or is being played");
            }
            Optional<MatchDay> nextMatchDay = context.getMatchDayRepository().getEarliestUpcomingMatchDay(lineUp.getTeam().getLeague());
            if (nextMatchDay.isEmpty()) {
                throw new RuntimeException("The league ended");
            }
            if(!nextMatchDay.get().equals(match.getMatchDaySerieA())) {
                throw new RuntimeException("The matchDay of the lineUp is incorrect");
            }

			FantaTeam team = lineUp.getTeam();

			// Check if is legal to save the lineUP
			Optional<MatchDay> previousMatchDay = context.getMatchDayRepository().getLatestEndedMatchDay(match.getMatchDaySerieA().getLeague());
			if (previousMatchDay.isPresent()) {
				Optional<Match> previousMatch = context.getMatchRepository().getMatchBy(previousMatchDay.get(), 
						team);
				Optional<Result> previousMatchResult = context.getResultsRepository().getResult(previousMatch.get());

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
