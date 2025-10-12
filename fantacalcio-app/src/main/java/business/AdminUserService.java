package business;

import java.util.*;
import java.util.stream.Collectors;

import business.ports.repository.ContractRepository;
import business.ports.transaction.TransactionManager;
import business.ports.transaction.TransactionManager.TransactionContext;
import domain.*;
import domain.Player.Defender;
import domain.Player.Forward;
import domain.Player.Goalkeeper;
import domain.Player.Midfielder;
import domain.Player.PlayerVisitor;

public class AdminUserService extends UserService {

	public AdminUserService(TransactionManager transactionManager) {
		super(transactionManager);
	}

    //TODO ritesta da Spostare nell'UserService
	public void createLeague(String leagueName, FantaUser admin, String leagueCode) {
		transactionManager.inTransaction((context) -> {
			if (context.getLeagueRepository().getLeagueByCode(leagueCode).isEmpty()) {
				League league = new League(admin, leagueName, leagueCode);
				context.getLeagueRepository().saveLeague(league);
                for(int i = 1; i < 21; i++){
                    context.getMatchDayRepository().saveMatchDay(new MatchDay("MatchDay "+ i, i, MatchDay.Status.FUTURE, league));
                }
			} else {
				throw new IllegalArgumentException("A league with the same league code already exists");
			}
		});
	}

	public void setPlayerToTeam(FantaTeam team, Player player) {
		transactionManager.inTransaction((context) -> {

			if (team.getContracts().size() >= 25)
				throw new UnsupportedOperationException("Maximum 25 players can be in a FantaTeam");

			final int maxGoalkeepeers = 3;
			final int maxDefenders = 8;
			final int maxMidfielders = 8;
			final int maxForward = 6;
			int actualGoalkeepers = team.extract().goalkeepers().size();
			int actualDefenders = team.extract().defenders().size();
			int actualMidfielders = team.extract().midfielders().size();
			int actualForwards = team.extract().forwards().size();

			var visitor = new PlayerVisitor() {

				private int role = -1;

				@Override
				public void visitGoalkeeper(Goalkeeper goalkeeper) {
					role = 0;
				}

				@Override
				public void visitDefender(Defender defender) {
					role = 1;
				}

				@Override
				public void visitMidfielder(Midfielder midfielder) {
					role = 2;
				}

				@Override
				public void visitForward(Forward forward) {
					role = 3;
				}
			};

			player.accept(visitor);
			if ((visitor.role == 0 && maxGoalkeepeers > actualGoalkeepers)
					|| (visitor.role == 1 && maxDefenders > actualDefenders)
					|| (visitor.role == 2 && maxMidfielders > actualMidfielders)
					|| (visitor.role == 3 && maxForward > actualForwards))
				context.getContractRepository().saveContract(new Contract(team, player));
		});
	}

	public void removePlayerFromTeam(FantaTeam team, Player player) {
		transactionManager.inTransaction((context) -> {
			ContractRepository contractRepository = context.getContractRepository();
			Optional<Contract> contract = contractRepository.getContract(team, player);
			if (contract.isPresent())
				contractRepository.deleteContract(contract.get());
		});
	}

	public void generateCalendar(League league) {
		transactionManager.inTransaction((context) -> {
			List<FantaTeam> teams = List.copyOf(context.getTeamRepository().getAllTeams(league));
			// TODO what if there are no Teams?
			List<List<FantaTeam[]>> schedule = generateFixedRounds(teams, 20);
			List<MatchDay> matchDay = context.getMatchDayRepository().getAllMatchDays(league);
			List<Match> matches = createMatches(schedule, matchDay);
			for (Match match : matches) {
				context.getMatchRepository().saveMatch(match);
			}
		});

	}

	// package-private for tests
	private List<List<FantaTeam[]>> generateSchedule(List<FantaTeam> teams) {
		int n = teams.size();

		if (n < 2) {
			throw new IllegalArgumentException("At least 2 teams are required to generate a schedule.");
		}

		if (n % 2 != 0) {
			throw new IllegalArgumentException("Number of teams must be even. Add a BYE if needed.");
		}

		List<FantaTeam> rotating = new ArrayList<>(teams.subList(0, n - 1));
		FantaTeam fixed = teams.get(n - 1);

		List<List<FantaTeam[]>> rounds = new ArrayList<>();

		// First leg
		for (int r = 0; r < n - 1; r++) {
			List<FantaTeam[]> matches = new ArrayList<>();
			int half = n / 2;

			List<FantaTeam> left = new ArrayList<>();
			left.add(fixed);
			left.addAll(rotating.subList(0, half - 1));

			List<FantaTeam> right = new ArrayList<>(rotating.subList(half - 1, rotating.size()));
			Collections.reverse(right);

			for (int i = 0; i < half; i++) {
				FantaTeam home = left.get(i);
				FantaTeam away = right.get(i);

				// Alternate home/away
				if (r % 2 == 1) {
					matches.add(new FantaTeam[] { away, home });
				} else {
					matches.add(new FantaTeam[] { home, away });
				}
			}

			rounds.add(matches);
			Collections.rotate(rotating, 1); // rotate
		}

		// Second leg (mirror)
		int originalSize = rounds.size();
		for (int r = 0; r < originalSize; r++) {
			List<FantaTeam[]> secondLeg = new ArrayList<>();
			for (FantaTeam[] match : rounds.get(r)) {
				secondLeg.add(new FantaTeam[] { match[1], match[0] });
			}
			rounds.add(secondLeg);
		}

		return rounds;
	}

	private List<List<FantaTeam[]>> generateFixedRounds(List<FantaTeam> teams, int targetRounds) {
		List<List<FantaTeam[]>> base = generateSchedule(teams); // double round robin
		List<List<FantaTeam[]>> full = new ArrayList<>();

		while (full.size() < targetRounds) {
			full.addAll(base); // repeat schedule
		}

		if (full.size() > targetRounds) {
			full = new ArrayList<>(full.subList(0, targetRounds)); // trim
		}
		return full;
	}

	//TODO dovrebbe essere privato viene usato in generate calendar
	private List<Match> createMatches(List<List<FantaTeam[]>> schedule, List<MatchDay> matchDays) {
		List<Match> matches = new ArrayList<>();

		if (schedule.size() != matchDays.size()) {
			throw new IllegalArgumentException("Schedule rounds and matchDays must have the same size");
		}

		for (int roundIndex = 0; roundIndex < schedule.size(); roundIndex++) {
			MatchDay matchDay = matchDays.get(roundIndex);
			List<FantaTeam[]> round = schedule.get(roundIndex);

			for (FantaTeam[] pairing : round) {
				FantaTeam home = pairing[0];
				FantaTeam away = pairing[1];
				matches.add(new Match(matchDay, home, away));
			}
		}
		return matches;
	}

	public void calculateGrades(FantaUser user, League league) {
		// check if the user is the admin of the league
		if (!(league.getAdmin().equals(user)))
			throw new IllegalArgumentException("You are not the admin of the league");
		transactionManager.inTransaction((context) -> {
			// find the oldest match with no result
			Optional<MatchDay> previousMatchDay = context.getMatchDayRepository().getLatestEndedMatchDay(league);
			if (previousMatchDay.isEmpty()) {
				throw new RuntimeException("The season hasn't started yet");
			}
            if(allMatchesHaveResult(previousMatchDay.get(), context)){
                throw new RuntimeException("The results have already been calculated");
            }


			List<Match> allMatches = context.getMatchRepository().getAllMatchesIn(previousMatchDay.get());
            List<Grade> allGrades = context.getGradeRepository().getAllMatchGrades(previousMatchDay.get());
			for (Match match : allMatches) {
				
				System.out.println("Considering match: " + match.getMatchDaySerieA().getName() + ", " + match.getTeam1().getName() + ", " + match.getTeam2().getName());
				
				List<Grade> allMatchGrades = findGradesForMatch(match, allGrades);
				Optional<LineUp> lineUp1 = context.getLineUpRepository().getLineUpByMatchAndTeam(match,
						match.getTeam1());
				Optional<LineUp> lineUp2 = context.getLineUpRepository().getLineUpByMatchAndTeam(match,
						match.getTeam2());
				Map<Player, Grade> gradesByPlayer = allMatchGrades.stream()
						.collect(Collectors.toMap(Grade::getPlayer, g -> g));
				double resultTeam1 = 0;
				double resultTeam2 = 0;
				
				System.out.println("Check if lineups exist");
				if (lineUp1.isPresent()) {
					
					System.out.println("lineUp1 OK");
					resultTeam1 = getTeamResult(lineUp1.get(), gradesByPlayer);
					
				}
				if (lineUp2.isPresent()) {
					
					System.out.println("lineUp2 OK");
					resultTeam2 = getTeamResult(lineUp2.get(), gradesByPlayer);
					
				}
				int goalTeam1 = goals(resultTeam1);
				int goalTeam2 = goals(resultTeam2);
				if (goalTeam1 > goalTeam2) {
					match.getTeam1().setPoints(match.getTeam1().getPoints() + 3);
				} else if (goalTeam1 < goalTeam2) {
					match.getTeam2().setPoints(match.getTeam2().getPoints() + 3);
				} else if (goalTeam1 == goalTeam2) {
					match.getTeam1().setPoints(match.getTeam1().getPoints() + 1);
					match.getTeam2().setPoints(match.getTeam2().getPoints() + 1);
				}
				
				System.out.println("Result saved with: " + resultTeam1 + ", " + resultTeam2);
				context.getResultsRepository()
						.saveResult(new Result(resultTeam1, resultTeam2, goalTeam1, goalTeam2, match));
			}
		});
	}

    private boolean allMatchesHaveResult(MatchDay matchDay, TransactionContext context) {
        // recupera tutti i match della giornata e della lega corrispondente
        List<Match> matches = context.getMatchRepository().getAllMatchesIn(matchDay);

        // controlla che per ogni match ci sia un result
        return matches.stream()
                .allMatch(match -> context.getResultsRepository().getResult(match).isPresent());
    }

    private List<Grade> findGradesForMatch(Match match, List<Grade> allGrades) {
        // Ottieni tutti i player dei due team
        Set<Player> matchPlayers = new HashSet<>();
        matchPlayers.addAll(match.getTeam1().getContracts().stream()
                .map(Contract::getPlayer)
                .toList());
        matchPlayers.addAll(match.getTeam2().getContracts().stream()
                .map(Contract::getPlayer)
                .toList());

        // Filtra i grade solo per i player dei due team
        return allGrades.stream()
                .filter(grade -> matchPlayers.contains(grade.getPlayer()))
                .toList();
    }


    private double getTeamResult(LineUp lineUp, Map<Player, Grade> gradesByPlayer) {
		double result = 0;
		result += calculateRoleResults(lineUp.extract().starterGoalkeepers(), lineUp.extract().substituteGoalkeepers(),
				gradesByPlayer);
		result += calculateRoleResults(lineUp.extract().starterDefenders(), lineUp.extract().substituteDefenders(),
				gradesByPlayer);
		result += calculateRoleResults(lineUp.extract().starterMidfielders(), lineUp.extract().substituteMidfielders(),
				gradesByPlayer);
		result += calculateRoleResults(lineUp.extract().starterForwards(), lineUp.extract().substituteForwards(),
				gradesByPlayer);

		return result;
	}

	private double calculateRoleResults(Set<? extends Player> starters, List<? extends Player> substitutes,
			Map<Player, Grade> gradesByPlayer) {

		double result = 0;
		int benchPositionToLook = 0;

		System.out.println("Inizio calcolo risultati formazione");
		for (Player starter : starters) {
			System.out.println("Primo giocatore considerato");
			Grade grade = gradesByPlayer.get(starter);
			if (grade != null) {
				System.out.println("Sommo " + grade.getMark());
				result += grade.getMark();
			} else {
				boolean found = false;
				while (!found && benchPositionToLook < substitutes.size()) {
					Player substitute = substitutes.get(benchPositionToLook);
					Grade subGrade = gradesByPlayer.get(substitute);
					if (subGrade != null) {
						result += subGrade.getMark();
						found = true;
					}
					benchPositionToLook++;
				}
			}
		}

		return result;
	}

	private int goals(double points) {
		if (points < 66.0)
			return 0;
		return 1 + (int) Math.floor((points - 66.0) / 6.0);
	}

    //TODO testa
    public void startMatchDay(League league) {
        transactionManager.inTransaction((context)->{
            Optional<MatchDay> matchDayToPlay = context.getMatchDayRepository().getEarliestUpcomingMatchDay(league);
            if (matchDayToPlay.isPresent()) {
                Optional<MatchDay> previousMatchDay = context.getMatchDayRepository().getLatestEndedMatchDay(league);
                if(previousMatchDay.isPresent()) {
                    if(!allMatchesHaveResult(previousMatchDay.get(), context)) {
                        throw new RuntimeException("You have to calculate the results before advancing the game state");
                    }
                }
                matchDayToPlay.get().setStatus(MatchDay.Status.PRESENT);
            } else{
                throw new RuntimeException("The are no more matchdays to play");
            }
        });
    }

    //TODO testa
    public void endMatchDay(League league) {
        transactionManager.inTransaction((context)->{
            Optional<MatchDay> matchDayToEnd = context.getMatchDayRepository().getOngoingMatchDay(league);
            if (matchDayToEnd.isPresent()) {
                matchDayToEnd.get().setStatus(MatchDay.Status.PAST);
            }else {
                throw new RuntimeException("The is no matchDay to end");
            }
        });
    }


}