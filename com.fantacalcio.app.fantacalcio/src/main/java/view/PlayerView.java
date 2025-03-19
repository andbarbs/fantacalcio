package view;

import java.util.List;

import model.Player;

public interface PlayerView extends View {

	void showAllPlayers(List<Player> players);

	// void showPlayerById(Player player);
}
