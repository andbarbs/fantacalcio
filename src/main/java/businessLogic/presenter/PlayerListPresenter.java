package businessLogic.presenter;

import businessLogic.abstractDAL.repository.PlayerRepository;
import businessLogic.abstractView.PlayerView;

public class PlayerListPresenter {

	private PlayerView playerView;
	private PlayerRepository playerRepository;

	public PlayerListPresenter(PlayerView playerView, PlayerRepository playerRepository) {
		this.playerView = playerView;
		this.playerRepository = playerRepository;
	}

	public void showAllPlayers() {
		playerView.showAllPlayers(playerRepository.findAll());
	}

	public void showPlayersWithSurname(String surname) {
		playerView.showPlayersBySurname(playerRepository.findBySurname(surname));
	}

}
