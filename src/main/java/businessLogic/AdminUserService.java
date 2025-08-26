package businessLogic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import domainModel.Contract;
import domainModel.FantaTeam;
import domainModel.League;
import domainModel.NewsPaper;
import domainModel.Player;
import domainModel.Result;

public class AdminUserService extends UserService {

	public AdminUserService(TransactionManager transactionManager) {
		super(transactionManager);
	}

	public void setPlayerToTeam(FantaTeam team, Player player) {
		transactionManager
				.inTransaction((context) -> context.getContractRepository().saveContract(new Contract(team, player)));
	}

	public Set<NewsPaper> getAllNewspapers() {
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
			// ora dobbiamo salvare la schedule in qualche modo
		});
		
	}
	
	// AI generated
	private List<List<FantaTeam[]>> generateSchedule(List<FantaTeam> teams) {
        int n = teams.size();
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
                    matches.add(new FantaTeam[]{away, home});
                } else {
                    matches.add(new FantaTeam[]{home, away});
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
                secondLeg.add(new FantaTeam[]{match[1], match[0]});
            }
            rounds.add(secondLeg);
        }

        return rounds;
    }
	
	// AI generated
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


	public void calculateGrades() {

	}

	public void saveResult(Result result) {
		transactionManager.inTransaction((context) -> context.getResultsRepository().saveResult(result));
	}
}
