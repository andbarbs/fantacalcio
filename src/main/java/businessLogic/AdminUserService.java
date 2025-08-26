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
			context.getNewspaperRepository().setNewsPaper(newspaper, adminLeague);
		});
	}

	public void generateCalendar(League league) {
		transactionManager.inTransaction((context) -> {
			List<FantaTeam> teams = context.getTeamRepository().getAllTeams(league);
			
			int n = teams.size();
	        if (n % 2 != 0) {
	            throw new IllegalArgumentException("Number of teams must be even. Add a BYE if needed.");
	        }

	        List<FantaTeam> rotating = new ArrayList<FantaTeam>(teams.subList(0, n - 1));
	        FantaTeam fixed = teams.get(n - 1);

	        List<List<String[]>> rounds = new ArrayList<>();

	        // First leg (single round robin)
	        for (int r = 0; r < n - 1; r++) {
	            List<String[]> matches = new ArrayList<>();
	            int half = n / 2;

	            List<FantaTeam> left = new ArrayList<>();
	            left.add(fixed);
	            left.addAll(rotating.subList(0, half - 1));

	            List<FantaTeam> right = new ArrayList<>(rotating.subList(half - 1, rotating.size()));
	            Collections.reverse(right);

	            for (int i = 0; i < half; i++) {
	                FantaTeam home = left.get(i);
	                FantaTeam away = right.get(i);

	                // Flip home/away every other round for balance
	                if (r % 2 == 1) {
	                    matches.add(new String[]{away.getName(), home.getName()});
	                } else {
	                    matches.add(new String[]{home.getName(), away.getName()});
	                }
	            }

	            rounds.add(matches);

	            // Rotate
	            Collections.rotate(rotating, 1);
	        }

	        // Second leg (mirror with swapped home/away)
	        int originalSize = rounds.size();
	        for (int r = 0; r < originalSize; r++) {
	            List<String[]> secondLeg = new ArrayList<>();
	            for (String[] match : rounds.get(r)) {
	                secondLeg.add(new String[]{match[1], match[0]}); // swap
	            }
	            rounds.add(secondLeg);
	        }

	        // ora devo salvare i round da qualche parte
			
		});
		
	}

	public void calculateGrades() {
		
	}

	public void saveResult(Result result) {
		transactionManager.inTransaction((context) -> context.getResultsRepository().saveResult(result));
	}
}
