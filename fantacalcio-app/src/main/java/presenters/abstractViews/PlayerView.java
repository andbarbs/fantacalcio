package presenters.abstractViews;

import java.util.List;

import domain.Player;

public interface PlayerView extends View {

	void showAllPlayers(List<Player> players);

	void showPlayersBySurname(List<Player> bySurname);

}
