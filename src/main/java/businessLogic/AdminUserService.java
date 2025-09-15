package businessLogic;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import domainModel.*;

public class AdminUserService extends UserService {

	public AdminUserService(TransactionManager transactionManager) {
		super(transactionManager);
	}

	public void setPlayerToTeam(FantaTeam team, Player player) {
		//TODO controlla che il team non abbia già  25 player di cui 3 portieri 8 difensori e 8 centrocampisti e 6 attaccanti e testa
		transactionManager
				.inTransaction((context) -> context.getContractRepository().saveContract(new Contract(team, player)));
	}

	//TODO aggiungi rimuovi player controlla se il player è nel team e se lo è lo toglie altrimenti eccezione

	public List<NewsPaper> getAllNewspapers() {
		return transactionManager.fromTransaction((context) -> context.getNewspaperRepository().getAllNewspapers());
	}

	public void setNewspaperForLeague(NewsPaper newspaper, League adminLeague) {
		transactionManager.inTransaction((context) -> {
			context.getNewspaperRepository().setNewsPaperForLeague(newspaper, adminLeague);
		});
	}

	public void generateCalendar(League league) {
		transactionManager.inTransaction((context) -> {
			List<FantaTeam> teams = context.getTeamRepository().getAllTeams(league);
			List<List<FantaTeam[]>> schedule = generateFixedRounds(teams, 38);
			List<MatchDaySerieA> matchDaySerieA = context.getMatchDayRepository().getAllMatchDays();
			List<Match> matches = createMatches(schedule, matchDaySerieA);
			for(Match match: matches) {
				context.getMatchRepository().saveMatch(match);
			}
		});

	}
	
	// package-private for tests
	List<List<FantaTeam[]>> generateSchedule(List<FantaTeam> teams) {
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

	public List<Match> createMatches(List<List<FantaTeam[]>> schedule, List<MatchDaySerieA> matchDays) {
		List<Match> matches = new ArrayList<>();

		if (schedule.size() != matchDays.size()) {
			throw new IllegalArgumentException("Schedule rounds and matchDays must have the same size");
		}

		for (int roundIndex = 0; roundIndex < schedule.size(); roundIndex++) {
			MatchDaySerieA matchDay = matchDays.get(roundIndex);
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
		//check if the user is the admin of the league
		if(!(league.getAdmin().equals(user)))
			throw new IllegalArgumentException("You are not the admin of the league");
		transactionManager.inTransaction((context) -> {
			//find the oldest match with no result
			LocalDate localDate = LocalDate.now();
			Optional<MatchDaySerieA> previousMatchDay = context.getMatchDayRepository().getPreviousMatchDay(localDate);
			if(previousMatchDay.isEmpty()){
				throw new RuntimeException("The season hasn't started yet");
			}
			Optional<MatchDaySerieA> matchDayToCalculate = getNextMatchDayToCalculate(localDate, context, league, user);
			if(!(matchDayToCalculate.isPresent())){
				throw new RuntimeException("There are no results to calculate");
			}
			if(!isLegalToCalculateResults(localDate)){
				throw new RuntimeException("The matches are not finished yet");
			}
			List<Match> allMatches = context.getMatchRepository().getAllMatchesByMatchDay(matchDayToCalculate.get(), league);
			for (Match match : allMatches) {
				List<Grade> allMatchGrades = context.getGradeRepository().getAllMatchGrades(match, league.getNewsPaper());
				Optional<LineUp> lineUp1 = context.getLineUpRepository().getLineUpByMatchAndTeam(match, match.getTeam1());
				Optional<LineUp> lineUp2 = context.getLineUpRepository().getLineUpByMatchAndTeam(match, match.getTeam2());
				Map<Player, Grade> gradesByPlayer = allMatchGrades.stream()
						.collect(Collectors.toMap(Grade::getPlayer, g -> g));
				double resultTeam1 = 0;
				double resultTeam2 = 0;
				if(lineUp1.isPresent()){
					resultTeam1 = getTeamResult(lineUp1.get(), gradesByPlayer);
				}
				if(lineUp2.isPresent()){
					resultTeam2 = getTeamResult(lineUp2.get(), gradesByPlayer);
				}
				int goalTeam1 = goals(resultTeam1);
				int goalTeam2 = goals(resultTeam2);
				Result result = new Result(resultTeam1, resultTeam2, goalTeam1, goalTeam2, match);
				if(goalTeam1 > goalTeam2){
					match.getTeam1().setPoints(match.getTeam1().getPoints() + 3);
				} else if(goalTeam1 < goalTeam2){
					match.getTeam2().setPoints(match.getTeam2().getPoints() + 3);
				} else if(goalTeam1 == goalTeam2){
					match.getTeam1().setPoints(match.getTeam1().getPoints() + 1);
					match.getTeam2().setPoints(match.getTeam2().getPoints() + 1);
				}
				context.getResultsRepository().saveResult(result);
			}
		});
	}

	private double getTeamResult(LineUp lineUp, Map<Player, Grade> gradesByPlayer) {
		double result = 0;
		result += calculateRoleResults(lineUp.extract().starterGoalkeepers(),
				lineUp.extract().substituteGoalkeepers(),
				gradesByPlayer);
		result += calculateRoleResults(lineUp.extract().starterDefenders(),
				lineUp.extract().substituteDefenders(),
				gradesByPlayer);
		result += calculateRoleResults(lineUp.extract().starterMidfielders(),
				lineUp.extract().substituteMidfielders(),
				gradesByPlayer);
		result += calculateRoleResults(lineUp.extract().starterForwards(),
				lineUp.extract().substituteForwards(),
				gradesByPlayer);

		return result;
	}

	private double calculateRoleResults(
			Set<? extends Player> starters,
			List<? extends Player> substitutes,
			Map<Player, Grade> gradesByPlayer) {

		double result = 0;
		int benchPositionToLook = 0;

		for (Player starter : starters) {
			Grade grade = gradesByPlayer.get(starter);
			if (grade != null) {
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
		if (points < 66.0) return 0;
		return 1 + (int) Math.floor((points - 66.0) / 6.0);
	}

	private boolean isLegalToCalculateResults(LocalDate matchDate){
		LocalDate now = today();
		LocalDate legalDate;
		DayOfWeek dayOfWeek = matchDate.getDayOfWeek();
		if(dayOfWeek == DayOfWeek.SATURDAY){
			legalDate = matchDate.plusDays(3);
		} else if (dayOfWeek == DayOfWeek.SUNDAY) {
			legalDate = matchDate.plusDays(2);
		} else{
			legalDate = now.plusDays(1);
		}
		return !now.isBefore(legalDate);
	}
	
	protected LocalDate today() {
		return LocalDate.now();
	}

	private Optional<MatchDaySerieA> getNextMatchDayToCalculate(LocalDate localDate, TransactionContext context, League league, FantaUser user) {
		Optional<MatchDaySerieA> matchDayToCalculate;
		boolean found = false;
		while(!found){
			Optional<MatchDaySerieA> previousMatchDay = context.getMatchDayRepository().getPreviousMatchDay(localDate);
			if(previousMatchDay.isPresent()){
				Match previousMatch = context.getMatchRepository().getMatchByMatchDay(previousMatchDay.get(), league, getFantaTeamByUserAndLeague(league, user));
				Optional<Result> previousMatchResult = context.getResultsRepository().getResult(previousMatch);
				if(previousMatchResult.isPresent()){
					found = true;
				} else {
					localDate = previousMatchDay.get().getDate();
				}
			} else {
				found = true;
			}
		}
		matchDayToCalculate = context.getMatchDayRepository().getMatchDay(localDate);
		return matchDayToCalculate;
	}
}