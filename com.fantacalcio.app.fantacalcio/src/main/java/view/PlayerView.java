package view;

import java.util.List;

import model.Player;

public interface PlayerView extends View {

	void showPlayers(List<Player> players);

	void showPlayerInfos(Player findById);

}
