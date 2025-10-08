package presenters.abstractViews;

import java.util.List;

import domain.FantaTeam;
import domain.Player;

public interface TeamListView {

	void showAllTeams(List<FantaTeam> allTeams);

	void showTeamPlayers(FantaTeam clickedTeam);

	void showTeamPlayers(String name, List<Player> playersByTeam);

}
