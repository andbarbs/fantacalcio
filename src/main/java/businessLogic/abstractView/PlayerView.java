package businessLogic.abstractView;

import java.util.List;

import domainModel.Player;

public interface PlayerView extends View {

	void showAllPlayers(List<Player> players);

	void showPlayersBySurname(List<Player> bySurname);

	// void showPlayerById(Player player);
}
