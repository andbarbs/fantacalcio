package businessLogic.abstractView;

import java.util.List;

import domainModel.FantaTeam;
import domainModel.Player;

public interface TeamListView {

	void showAllTeams(List<FantaTeam> allTeams);

	void showTeamPlayers(FantaTeam clickedTeam);

	void showTeamPlayers(String name, List<Player> playersByTeam);

}
